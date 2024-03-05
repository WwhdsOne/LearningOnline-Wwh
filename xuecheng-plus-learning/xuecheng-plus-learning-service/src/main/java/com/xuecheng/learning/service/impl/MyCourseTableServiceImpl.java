package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTableService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/3/5 17:41
 * @description 我的课程表服务
 **/
@Service
@Slf4j
public class MyCourseTableServiceImpl implements MyCourseTableService {
    @Autowired
    XcChooseCourseMapper xcChooseCourseMapper;
    @Autowired
    XcCourseTablesMapper xcCourseTablesMapper;
    @Autowired
    ContentServiceClient contentServiceClient;


    @Transactional
    @Override
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {
        //远程调用服务查看课程收费规则
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if ( coursepublish == null ) {
            //课程不存在
            XueChengPlusException.cast("课程不存在");
        }
        //获取收费规则
        String charge = coursepublish.getCharge();
        XcChooseCourse xcChooseCourse = null;
        if ( charge.equals("201000") ) {
            //免费课程
            //向选课记录表添加数据
            xcChooseCourse = addFreeCoruse(userId, coursepublish);
            //向我的课程表添加数据
            XcCourseTables xcCourseTables = addCourseTabls(xcChooseCourse);

        } else {
            //收费课程
            //向选课记录表添加数据
            xcChooseCourse = addChargeCoruse(userId, coursepublish);
        }
        //判断学生学习资格
        XcCourseTablesDto learningStatus = getLearningStatus(userId, courseId);
        //构造返回结果
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(xcChooseCourse, xcChooseCourseDto);
        //设置学习状态
        xcChooseCourseDto.setLearnStatus(learningStatus.getLearnStatus());
        return xcChooseCourseDto;
    }

    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        //查询我的课程表,查不到说明没有资格学习
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        //最终返回结果
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        if( xcCourseTables == null ){
            //{"code":"702002","desc":"没有选课或选课后没有支付"}
            xcCourseTablesDto.setLearnStatus("702002");
            return xcCourseTablesDto;
        }
        //查询到了之后,判断是否过期
        LocalDateTime validtimeEnd = xcCourseTables.getValidtimeEnd();
        if( validtimeEnd.isBefore(LocalDateTime.now()) ){
            //{"code":"702003","desc":"选课已过期"}
            xcCourseTablesDto.setLearnStatus("702003");
            BeanUtils.copyProperties(xcCourseTables, xcCourseTablesDto);
        }else{
            xcCourseTablesDto.setLearnStatus("702001");
            BeanUtils.copyProperties(xcCourseTables, xcCourseTablesDto);
        }
        return xcCourseTablesDto;
    }

    //添加免费课程,免费课程加入选课记录表、我的课程表
    public XcChooseCourse addFreeCoruse(String userId, CoursePublish coursepublish) {
        //如果存在免费选课记录且选课为成功状态,直接返回
        LambdaQueryWrapper<XcChooseCourse> eq = new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursepublish.getId())
                .eq(XcChooseCourse::getOrderType, "700001") //免费课程
                .eq(XcChooseCourse::getStatus, "701001");//选课成功
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(eq);
        //同一个人同一门课程只能选一次,但数据库没有主键约束可能有多次,这里只取第一次
        if( !xcChooseCourses.isEmpty() ){
            return xcChooseCourses.get(0);
        }
        //向选课记录写数据
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700001");//免费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setValidDays(365); //暂时硬编码
        xcChooseCourse.setStatus("701001");//选课成功
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        int insert = xcChooseCourseMapper.insert(xcChooseCourse);
        if( insert <= 0 ){
            XueChengPlusException.cast("添加选课失败");
        }
        return xcChooseCourse;
    }

    //添加收费课程
    public XcChooseCourse addChargeCoruse(String userId, CoursePublish coursepublish) {
        //如果存在收费选课记录且选课状态为待支付，直接返回
        LambdaQueryWrapper<XcChooseCourse> eq = new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursepublish.getId())
                .eq(XcChooseCourse::getOrderType, "700002") //收费课程
                .eq(XcChooseCourse::getStatus, "701002");//选课成功
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(eq);
        //同一个人同一门课程只能选一次,但数据库没有主键约束可能有多次,这里只取第一次
        if( !xcChooseCourses.isEmpty() ){
            return xcChooseCourses.get(0);
        }
        //向选课记录写数据
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700002");//免费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setValidDays(365); //暂时硬编码
        xcChooseCourse.setStatus("701002");//选课成功
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        int insert = xcChooseCourseMapper.insert(xcChooseCourse);
        if( insert <= 0 ){
            XueChengPlusException.cast("添加选课失败");
        }
        return xcChooseCourse;
    }

    //添加到我的课程表
    public XcCourseTables addCourseTabls(XcChooseCourse xcChooseCourse) {
        //选课成功才能向我的课程表添加数据
        if( !xcChooseCourse.getStatus().equals("701001") ){
            XueChengPlusException.cast("选课失败,无法添加到我的课程表");
        }
        XcCourseTables xcCourseTables = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if( xcCourseTables != null ){
            return xcCourseTables;
        }
        xcCourseTables = new XcCourseTables();
        BeanUtils.copyProperties(xcChooseCourse, xcCourseTables);
        xcCourseTables.setChooseCourseId(xcChooseCourse.getId());//记录选课记录id
        xcCourseTables.setCourseType(xcChooseCourse.getOrderType());//课程类型
        xcCourseTables.setUpdateDate(LocalDateTime.now());
        int insert = xcCourseTablesMapper.insert(xcCourseTables);
        if( insert <= 0 ){
            XueChengPlusException.cast("添加我的课程表失败");
        }
        return xcCourseTables;
    }

    //查询学习我的课程表
    public XcCourseTables getXcCourseTables(String userId,Long courseId){
        return xcCourseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>()
                .eq(XcCourseTables::getUserId, userId)
                .eq(XcCourseTables::getCourseId, courseId));
    }

}
