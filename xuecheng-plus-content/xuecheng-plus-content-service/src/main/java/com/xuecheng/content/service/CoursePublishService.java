package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDTO;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/26 16:47
 * @description 课程预览业务接口
 **/
public interface CoursePublishService {
    /**
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CoursePreviewDto
     * @description 获取课程预览信息
     * @author Mr.M
     * @date 2022/9/16 15:36
     */
    CoursePreviewDTO getCoursePreviewInfo(Long courseId);

    /**
     * @description 提交审核
     * @param courseId  课程id
     * @return void
     * @author Mr.M
     * @date 2022/9/18 10:31
     */
    void commitAudit(Long companyId, Long courseId);

    /**
     * @description 课程发布接口
     * @param companyId 机构id
     * @param courseId 课程id
     * @return void
     * @author Mr.M
     * @date 2022/9/20 16:23
     */
    void publish(Long companyId, Long courseId);



}
