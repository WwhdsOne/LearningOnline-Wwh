package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryDTO;
import com.xuecheng.content.model.dto.SaveTeachPlanDTO;
import com.xuecheng.content.model.dto.TeachplanDTO;
import com.xuecheng.content.service.CourseCategoryService;
import com.xuecheng.content.service.TeachPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    TeachPlanService teachPlanService;

    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId",name = "课程Id",required = true,dataType = "Long",paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDTO> getTreeNodes(@PathVariable Long courseId){
        return teachPlanService.findTeachPlanTree(courseId);
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachPlan(@RequestBody SaveTeachPlanDTO saveTeachPlanDTO){
        teachPlanService.saveTeachPlan(saveTeachPlanDTO);
    }

    @ApiOperation("课程计划修改")
    @DeleteMapping("/teachplan/{teachPlanId}")
    public void deleteTeachPlan(@PathVariable Long teachPlanId){
        teachPlanService.deleteById(teachPlanId);
    }

    @ApiOperation("课程计划顺序调整")
    @PostMapping("/teachplan/{direction}/{teachPlanId}")
    public void modifyOrder(@PathVariable String direction,@PathVariable Long teachPlanId){
        teachPlanService.updateOrderByDirectionAndId(direction,teachPlanId);
    }
}
