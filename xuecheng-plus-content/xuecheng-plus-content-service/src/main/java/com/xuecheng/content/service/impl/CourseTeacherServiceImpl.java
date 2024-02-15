package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/15 16:37
 * @description 课程老师操作Service层
 **/
@Service
@Slf4j
public class CourseTeacherServiceImpl implements CourseTeacherService{


    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Override
    public List<CourseTeacher> getTeachersByCourseId(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CourseTeacher::getCourseId,courseId);
        return courseTeacherMapper.selectList(lambdaQueryWrapper);
    }

    private CourseBase getVaildCourseBase(Long companyId,Long courseId){
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            XueChengPlusException.cast("课程不存在");
        }
        if(!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("本机构只能修改本机构的教师");
        }
        return courseBase;
    }

    @Override
    public CourseTeacher insertTeacher(Long companyId,CourseTeacher courseTeacher) {
        getVaildCourseBase(companyId,courseTeacher.getCourseId());
        courseTeacher.setCreateDate(LocalDateTime.now());
        courseTeacherMapper.insert(courseTeacher);
        return courseTeacher;
    }

    @Override
    public CourseTeacher updateTeacher(Long companyId,CourseTeacher courseTeacher) {
        getVaildCourseBase(companyId,courseTeacher.getCourseId());
        courseTeacherMapper.updateById(courseTeacher);
        return courseTeacher;
    }

    @Override
    public void deleteByCourseIdAndTeacherId(Long companyId, Long courseId, Long teacherId) {
        getVaildCourseBase(companyId,courseId);
        courseTeacherMapper.deleteById(teacherId);
    }
}
