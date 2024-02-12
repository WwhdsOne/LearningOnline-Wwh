package com.xuecheng;

import com.xuecheng.content.model.dto.CourseCategoryDTO;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/11 20:38
 * @description 课程分类查询测试类
 **/
@Slf4j
@SpringBootTest
class CourseCategoryServiceImplTest {

    @Autowired
    CourseCategoryService courseCategoryService;

    @Test
    public void test(){
        List<CourseCategoryDTO> courseCategoryDTOS = courseCategoryService.queryTreeNodes("1");
        courseCategoryDTOS.forEach(System.out::println);
    }
}
