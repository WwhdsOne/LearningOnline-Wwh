package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDTO;
import com.xuecheng.content.model.dto.CourseBaseInfoDTO;
import com.xuecheng.content.model.dto.EditCourseDTO;
import com.xuecheng.content.model.dto.QueryCourseParamsDTO;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/8 19:34
 * @description 课程基础信息查询
 **/
@Api(value = "课程信息管理接口",tags = "课程信息管理接口")
@RestController
@Slf4j
public class CourseBaseInfoController{


    @Autowired
    CourseBaseInfoService courseBaseInfoService;
    @ApiOperation("课程分页查询接口")
    @PostMapping("/course/list")
    public PageResult list(PageParams params, @RequestBody(required = false) QueryCourseParamsDTO queryCourseParamsDto){
        return courseBaseInfoService.queryCourseBaseList(params,queryCourseParamsDto);
    }

    @ApiOperation("新增课程接口")
    @PostMapping("/course")
    public CourseBaseInfoDTO createCourseBase(@RequestBody @Validated(ValidationGroups.Inster.class) AddCourseDTO addCourseDTO){
        //机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 1232141425L;
        return courseBaseInfoService.createCourseBaseInfo(companyId,addCourseDTO);
    }

    @ApiOperation("单个课程查询接口")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDTO getCourseById(@PathVariable Long courseId){
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }

    @ApiOperation("修改课程")
    @PutMapping("/course")
    public CourseBaseInfoDTO modifyCourseBase(@RequestBody @Validated(ValidationGroups.Update.class)EditCourseDTO editCourseDTO){
        //机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 1232141425L;
        return courseBaseInfoService.updateCourseBase(companyId,editCourseDTO);
    }
}
