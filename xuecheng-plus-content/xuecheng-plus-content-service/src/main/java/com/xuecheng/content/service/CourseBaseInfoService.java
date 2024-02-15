package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDTO;
import com.xuecheng.content.model.dto.CourseBaseInfoDTO;
import com.xuecheng.content.model.dto.EditCourseDTO;
import com.xuecheng.content.model.dto.QueryCourseParamsDTO;
import com.xuecheng.content.model.po.CourseBase;

/**
 * @author Mr.M
 * @version 1.0
 * @description 课程基本信息管理业务接口
 * @date 2022/9/6 21:42
 */
public interface CourseBaseInfoService {

    /**
     * 课程分页查询
     * @param pageParams 分页参数
     * @param queryCourseParamsDto 条件条件
     * @return 查询结果
     */
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDTO queryCourseParamsDto);

    /**
     * 新增课程
     * @param companyId 课程id
     * @param addCourseDTO 课程信息
     * @return 课程详细信息
     */
    CourseBaseInfoDTO createCourseBaseInfo(Long companyId, AddCourseDTO addCourseDTO);

    /**
     * 根据ID查询课程
     * @param courseId 课程Id
     * @return 课程详细信息
     */
    CourseBaseInfoDTO getCourseBaseInfo(Long courseId);

    /**
     * 修改课程
     * @param companyId 机构Id
     * @param editCourseDto 课程修改信息
     * @return 课程详细信息
     */
    CourseBaseInfoDTO updateCourseBase(Long companyId, EditCourseDTO editCourseDto);

    /**
     * 删除课程及相关内容
     * @param courseId
     */
    void deleteCourseById(Long courseId);
}
