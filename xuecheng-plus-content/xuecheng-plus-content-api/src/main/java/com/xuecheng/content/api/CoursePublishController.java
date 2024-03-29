package com.xuecheng.content.api;

import com.alibaba.fastjson.JSON;
import com.xuecheng.content.model.dto.CourseBaseInfoDTO;
import com.xuecheng.content.model.dto.CoursePreviewDTO;
import com.xuecheng.content.model.dto.TeachplanDTO;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/26 15:42
 * @description 课程预览接口
 **/
@RestController
@Slf4j
public class CoursePublishController {

    @Autowired
    private CoursePublishService coursePublishService;

    /**
     * 课程预览
     *
     * @param courseId 课程id
     * @return ModelAndView 课程预览页面
     */
    @ApiOperation(value = "课程预览", tags = "课程预览")
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId) {
        ModelAndView modelAndView = new ModelAndView();
        //获取预览数据
        CoursePreviewDTO coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        modelAndView.addObject("model", coursePreviewInfo);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }

    @ApiOperation(value = "提交审核", tags = "提交审核")
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId) {
        Long companyId = 1232141425L;
        coursePublishService.commitAudit(companyId, courseId);
    }

    /**
     * @description 课程预览，发布
     * @author Mr.M
     * @date 2022/9/16 14:48
     * @version 1.0
     */
    @ApiOperation("课程发布")
    @PostMapping("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId) {
        Long companyId = 1232141425L;
        coursePublishService.publish(companyId, courseId);
    }

    /**
     * @param courseId 课程id
     * @return void
     * @description 课程预览，发布，内部调用不用token
     */
    @ApiOperation("查询课程发布信息")
    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId) {
        return coursePublishService.getCoursePublish(courseId);
    }

    @ApiOperation("获取课程发布信息")
    @ResponseBody
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDTO getCoursePublish(@PathVariable("courseId") Long courseId) {
        //封装数据
        CoursePreviewDTO coursePreviewDTO = new CoursePreviewDTO();


        //查询课程发布表
        //CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);


        //查询课程基本信息(缓存)
        CoursePublish coursePublish = coursePublishService.getCoursePublishCache(courseId);
        if(coursePublish == null){
            return coursePreviewDTO;
        }
        //向dto中封装数据
        CourseBaseInfoDTO courseBaseInfoDTO = new CourseBaseInfoDTO();
        BeanUtils.copyProperties(coursePublish, courseBaseInfoDTO);
        coursePreviewDTO.setCourseBase(courseBaseInfoDTO);
        //查询课程计划
        //从CouseBaseInfoDTO中获取课程计划
        String teachPlanJson = coursePublish.getTeachplan();
        List<TeachplanDTO> teachplanDTOS = JSON.parseArray(teachPlanJson, TeachplanDTO.class);
        coursePreviewDTO.setTeachPlans(teachplanDTOS);
        return coursePreviewDTO;
    }
}

