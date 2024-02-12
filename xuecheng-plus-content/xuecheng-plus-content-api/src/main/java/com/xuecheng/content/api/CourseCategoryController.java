package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryDTO;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/11 19:01
 * @description 课程分类信息查询
 **/
@Api(value = "课程分类管理接口",tags = "课程分类管理接口")
@RestController
@Slf4j
public class CourseCategoryController {

    @Autowired
    CourseCategoryService courseCategoryService;
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryDTO> queryTreeNodes(){
        return courseCategoryService.queryTreeNodes("1");
    }
}
