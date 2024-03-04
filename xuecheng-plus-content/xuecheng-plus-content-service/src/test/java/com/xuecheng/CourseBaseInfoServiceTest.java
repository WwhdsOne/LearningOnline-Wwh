package com.xuecheng;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDTO;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/8 19:34
 * @description 课程基础信息查询
 **/
@SpringBootTest
public class CourseBaseInfoServiceTest {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Test
    public void testCourseBaseInfoService(){
        //拼装分页查询条件
        QueryCourseParamsDTO queryCourseParamsDto = new QueryCourseParamsDTO();
        queryCourseParamsDto.setCourseName("java");//课程名称为查询条件
        //课程审核状态
        queryCourseParamsDto.setAuditStatus("202004");//202004代表审核通过,内容在xcplus_system架构中
        //创建分页查询类
        PageParams pageParams = new PageParams(1L,2L);

        PageResult<CourseBase> result = courseBaseInfoService.queryCourseBaseList(233L, pageParams, queryCourseParamsDto);
        result.getItems().forEach(System.out::println);
    }
}
