package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDTO;
import com.xuecheng.content.model.po.CoursePublish;

import java.io.File;

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

    /**
     * @description 课程静态化
     * @param courseId  课程id
     * @return File 静态化文件
     * @author Mr.M
     * @date 2022/9/23 16:59
     */
    File generateCourseHtml(Long courseId);
    /**
     * @description 上传课程静态化页面
     * @param file  静态化文件
     * @return void
     * @author Mr.M
     * @date 2022/9/23 16:59
     */
    void  uploadCourseHtml(Long courseId, File file) throws Exception;

    /**
     * @description 课程发布
     * @param courseId 课程id
     * @return void
     */

    CoursePublish getCoursePublish(Long courseId);

    /**
     * @description 获取课程发布信息
     * @param courseId 课程id
     * @return com.xuecheng.content.model.po.CoursePublish 课程发布信息
     */

    CoursePublish getCoursePublishCache(Long courseId);
}
