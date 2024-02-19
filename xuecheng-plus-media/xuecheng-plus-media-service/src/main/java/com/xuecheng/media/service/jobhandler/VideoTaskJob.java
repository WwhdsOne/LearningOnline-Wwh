package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

/**
 * XxlJob开发示例（Bean模式）
 * <p>
 * 开发步骤：
 * 1、任务开发：在Spring Bean实例中，开发Job方法；
 * 2、注解配置：为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法", destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 * 3、执行日志：需要通过 "XxlJobHelper.log" 打印执行日志；
 * 4、任务结果：默认任务结果为 "成功" 状态，不需要主动设置；如有诉求，比如设置任务结果为失败，可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
 *
 * @author xuxueli 2019-12-11 21:52:51
 */
@Component
@Slf4j
public class VideoTaskJob {

    @Autowired
    MediaFileProcessService mediaFileProcessService;

    @Autowired
    MediaFileService mediaFileService;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegPath;

    /**
     * 视频处理任务
     */
    @XxlJob("videoJobHandler")
    public void shardingJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();  //执行器的序号, 从0开始编号
        int shardTotal = XxlJobHelper.getShardTotal();  //执行器的总数


        List<MediaProcess> mediaProcessList;
        int size = 0;
        try {
            //取出cpu核心数作为一次处理数据的条数
            int processors = Runtime.getRuntime().availableProcessors();
            //一次处理视频数量不要超过cpu核心数
            mediaProcessList = mediaFileProcessService.getProcessByShardIndex(shardTotal, shardIndex, processors);
            size = mediaProcessList.size();
            log.debug("取出待处理视频任务{}条", size);
            if ( size <= 0 ) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        //计数器数量为任务数量
        CountDownLatch countDownLatch = new CountDownLatch(size);
        //启动一个线程池
        //线程池参数是任务数量
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        mediaProcessList.forEach(mediaProcess -> {
            executorService.execute(() -> {
                try {
                    Long taskId = mediaProcess.getId();
                    boolean b = mediaFileProcessService.startTask(taskId);
                    //====抢占任务失败=====
                    if ( !b ) {
                        log.debug("抢占任务失败:{}", taskId);
                        return;
                    }
                    //====抢占任务成功=====


                    //文件ID就是md5值
                    String fileId = mediaProcess.getFileId();
                    //桶
                    String bucket = mediaProcess.getBucket();
                    //文件名称
                    String objectName = mediaProcess.getFilePath();
                    //下载minio文件到本地
                    File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
                    if ( file == null ) {
                        log.debug("下载文件失败,任务id:{},buckerName:{},objectName:{}", taskId, bucket, objectName);
                        //保存任务失败的结果
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载视频到本地失败");
                        return;
                    }
                    //源avi视频路径
                    String videoPath = file.getAbsolutePath();
                    //替换后的mp4视频路径和url
                    String mp4Name = getFilePath(fileId, "mp4");
                    //先创建一个临时文件作为转换后的文件
                    File mp4File;
                    try {
                        mp4File = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.info("创建临时文件失败:{}", e.getMessage());
                        //保存任务失败的结果
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件失败");
                        return;
                    }
                    //本地mp4绝对路径
                    String mp4Path = mp4File.getAbsolutePath();
                    //创建工具类对象
                    //成功返回success,失败返回失败原因
                    Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpegPath, videoPath, mp4Name, mp4Path);
                    String result = mp4VideoUtil.generateMp4();
                    if ( !result.equals("success") ) {
                        log.debug("视频转码失败,失败原因:{},buketName:{},objectName:{}", result, bucket, objectName);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "视频转码失败");
                    }
                    //上传到minio
                    boolean b1;
                    try {
                        b1 = mediaFileService.addMediaFilesToMinIO(mp4File.getAbsolutePath(), "video/mp4", bucket, mp4Name);
                    } catch (Exception e) {
                        log.debug("上传到minio失败,任务id:{},buckerName:{},objectName:{}", taskId, bucket, objectName);
                        throw new RuntimeException(e);
                    }
                    if ( !b1 ) {
                        log.debug("上传到minio失败,任务id:{},buckerName:{},objectName:{}", taskId, bucket, objectName);
                        //保存任务失败的结果
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "上传到minio失败");
                        return;
                    }
                    //拼装地址
                    String url = getFilePath(fileId, "mp4");

                    //保存任务成功的结果
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, "任务成功");


                    //更新media_file中的url地址和文件路径
                    MediaFiles mediaFiles = new MediaFiles();
                    mediaFiles.setId(fileId);
                    mediaFiles.setUrl(url);
                    mediaFiles.setFilePath(mp4Name);
                    mediaFileService.updateById(mediaFiles);

                } finally {
                    //计数器减一
                    countDownLatch.countDown();
                }
            });
        });
        //阻塞
        //当计数器为0则继续执行
        //出现部分异常时,最多阻塞30分钟
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    private String getFilePath(String fileMd5, String fileExtension) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "." + fileExtension;
    }


}
