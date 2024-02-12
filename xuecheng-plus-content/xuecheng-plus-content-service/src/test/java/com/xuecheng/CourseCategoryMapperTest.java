package com.xuecheng;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryDTO;
import com.xuecheng.content.model.dto.QueryCourseParamsDTO;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/8 19:34
 * @description 课程基础信息查询
 **/
@SpringBootTest
public class CourseCategoryMapperTest {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Test
    public void testCategoryMapper(){
        List<CourseCategoryDTO> courseCategoryDTOS = courseCategoryMapper.selectTreeNodes("1");
        courseCategoryDTOS.forEach(System.out::println);
    }
}
