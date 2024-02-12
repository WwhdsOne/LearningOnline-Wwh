package com.xuecheng;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDTO;
import com.xuecheng.content.model.po.CourseBase;
import org.apache.commons.lang3.StringUtils;
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
public class CourseBaseMapperTest {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Test
    public void testCourseBaseMapper(){

        //拼装分页查询条件
        QueryCourseParamsDTO queryCourseParamsDto = new QueryCourseParamsDTO();
        queryCourseParamsDto.setCourseName("java");//课程名称为查询条件

        //封装查询条件
        LambdaQueryWrapper<CourseBase> courseBaseLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //根据课程名称模糊查询,sql为course_base.name like '%?%'
        courseBaseLambdaQueryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),
                CourseBase::getName,
                queryCourseParamsDto.getCourseName());
        //根据课程状态查询,sql为course_base.audit_status = ?
        courseBaseLambdaQueryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),
                CourseBase::getAuditStatus,
                queryCourseParamsDto.getAuditStatus());

        //创建分页查询类
        PageParams pageParams = new PageParams(1L,2L);
        //创建分页对象,参数为当前页码,每页记录数
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //获取分页查询结果
        Page<CourseBase> courseBasePage = courseBaseMapper.selectPage(page, courseBaseLambdaQueryWrapper);
        //获取数据列表
        List<CourseBase> records = courseBasePage.getRecords();
        //获取记录总数
        Long total = courseBasePage.getTotal();

        PageResult<CourseBase> result = new PageResult<>(records,total,pageParams.getPageNo(), pageParams.getPageSize());

        System.out.println(result);
    }

}
