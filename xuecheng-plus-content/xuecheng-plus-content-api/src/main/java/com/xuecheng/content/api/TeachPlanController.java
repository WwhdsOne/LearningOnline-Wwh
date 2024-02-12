package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryDTO;
import com.xuecheng.content.model.dto.TeachplanDTO;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/12 19:14
 * @description 课程计划管理接口
 **/
@Api(value = "课程计划管理接口",tags = "课程计划管理接口")
@RestController
@Slf4j
public class TeachPlanController {


    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId",name = "课程Id",required = true,dataType = "Long",paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDTO> getTreeNodes(@PathVariable Long courseId){

        return null;
    }
}
