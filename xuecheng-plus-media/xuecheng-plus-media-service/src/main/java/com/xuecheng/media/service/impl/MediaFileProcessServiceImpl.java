package com.xuecheng.media.service.impl;

import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/19 16:58
 * @description 媒资处理任务业务类
 **/
@Service
@Slf4j
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;


    /**
     * 根据shardIndex查询媒资处理列表
     * @param sharedTotal 分片总数
     * @param shardIndex 分片下标
     * @param failCount 失败次数
     * @return 媒资处理列表
     */
    @Override
    public List<MediaProcess> getProcessByShardIndex(int sharedTotal, int shardIndex, int processors) {
        return mediaProcessMapper.selectProcessByShardIndex(sharedTotal, shardIndex, processors);
    }

    /**
     * 开启一个任务
     * @param id 任务id
     * @return 更新记录数
     */
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result > 0;
    }


    /**
     * 保存任务结果
     * @param taskId  任务id
     * @param status 任务状态
     * @param fileId  文件id
     * @param url url
     * @param errorMsg 错误信息
     */
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        //查询要更新的任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);

        if(mediaProcess == null){
            log.error("任务不存在，taskId:{}",taskId);
            return;
        }
        //如果任务执行失败
        if("3".equals(status)){
            //更新media_process表中的状态
            mediaProcess.setStatus("3");
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);//失败次数+1
            mediaProcess.setErrormsg(errorMsg);
            //更新media_process表中的状态
            mediaProcessMapper.updateById(mediaProcess);
            return;
        }


        //==========如果任务执行成功==============
        //更新media_file表中的url
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        mediaFiles.setUrl(url);
        mediaFilesMapper.updateById(mediaFiles);
        //更新media_process表中的状态,2是成功
        mediaProcess.setStatus("2");
        //更新完成时间
        mediaProcess.setFinishDate(LocalDateTime.now());
        //更新media_process的url
        mediaProcess.setUrl(url);
        mediaProcessMapper.updateById(mediaProcess);
        //向media_process_history中插入一条记录
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess,mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);

        //从media_process删除此任务
        mediaProcessMapper.deleteById(taskId);

    }
}
