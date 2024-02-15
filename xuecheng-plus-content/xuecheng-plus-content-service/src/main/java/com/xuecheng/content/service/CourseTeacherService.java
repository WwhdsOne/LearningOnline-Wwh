package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/15 16:37
 * @description 课程老师相关接口
 **/
public interface CourseTeacherService {
    /**
     * 根据课程ID查询老师
     * @param courseId
     * @return 老师列表
     */
    List<CourseTeacher> getTeachersByCourseId(Long courseId);

    /**
     * 新增教师
     * @param courseTeacher
     * @return 新增教师属性
     */
    CourseTeacher insertTeacher(Long companyId,CourseTeacher courseTeacher);

    /**
     * 根据ID修改教师
     * @param courseTeacher
     * @return 修改教师属性
     */
    CourseTeacher updateTeacher(Long companyId,CourseTeacher courseTeacher);

    /**
     * 根据课程和教师ID删除教师
     * @param companyId
     * @param courseId
     * @param teacherId
     */
    void deleteByCourseIdAndTeacherId(Long companyId, Long courseId, Long teacherId);
}
