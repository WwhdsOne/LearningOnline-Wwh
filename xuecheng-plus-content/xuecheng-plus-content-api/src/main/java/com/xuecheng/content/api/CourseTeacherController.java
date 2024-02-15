package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/15 15:38
 * @description 课程老师管理接口
 **/
@RestController
@Slf4j
@Api(value = "课程老师管理接口",tags = "课程老师管理接口")
public class CourseTeacherController {

    @Autowired
    CourseTeacherService courseTeacherService;
    @ApiOperation("课程老师查询")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> getTeachersByCourseId(@PathVariable Long courseId){
        return courseTeacherService.getTeachersByCourseId(courseId);
    }

    @ApiOperation("课程老师新增")
    @PostMapping("/courseTeacher")
    public CourseTeacher saveTeacher(@RequestBody CourseTeacher courseTeacher){
        Long companyId = 1232141425L;
        return courseTeacherService.insertTeacher(companyId,courseTeacher);
    }

    @ApiOperation("课程老师修改")
    @PutMapping("/courseTeacher")
    public CourseTeacher modifyTeacher(@RequestBody CourseTeacher courseTeacher){
        Long companyId = 1232141425L;
        return courseTeacherService.updateTeacher(companyId,courseTeacher);
    }

    @ApiOperation("课程老师删除")
    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    public void deleteTeacher(@PathVariable Long courseId,@PathVariable Long teacherId){
        Long companyId = 1232141425L;
        courseTeacherService.deleteByCourseIdAndTeacherId(companyId,courseId,teacherId);
    }
}
