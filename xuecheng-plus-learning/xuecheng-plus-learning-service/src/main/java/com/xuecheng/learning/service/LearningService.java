package com.xuecheng.learning.service;

import com.xuecheng.base.model.RestResponse;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/3/10 16:13
 * @description 在线学习相关接口
 **/
public interface LearningService {
    /**
     * 获取视频
     * @param userId     用户id
     * @param courseId   课程id
     * @param teachplanId 计划id
     * @param mediaId    媒体id
     * @return RestResponse<String> 视频播放地址
     */
    RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId);
}
