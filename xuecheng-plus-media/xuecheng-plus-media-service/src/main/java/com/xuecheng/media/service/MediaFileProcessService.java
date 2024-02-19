package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/19 16:57
 * @description 媒资处理任务业务类
 **/
public interface MediaFileProcessService {
    /**
     * 根据shardIndex查询媒资处理列表
     * @param sharedTotal 分片总数
     * @param shardIndex 分片下标
     * @param failCount 失败次数
     * @return 媒资处理列表
     */
    List<MediaProcess> getProcessByShardIndex(int sharedTotal, int shardIndex,int processors);

    /**
     * @description 保存任务结果
     * @param taskId  任务id
     * @param status 任务状态
     * @param fileId  文件id
     * @param url url
     * @param errorMsg 错误信息
     * @return void
     * @author Mr.M
     * @date 2022/10/15 11:29
     */
    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);

    /**
     * 开启一个任务
     * @param id 任务id
     * @return 更新记录数
     */
    boolean startTask(long id);
}
