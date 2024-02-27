package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDTO;
import com.xuecheng.content.model.dto.CoursePreviewDTO;
import com.xuecheng.content.model.dto.TeachplanDTO;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachPlanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/26 16:48
 * @description 课程预览业务实现类
 **/
@Service
@Slf4j
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @Autowired
    private TeachPlanService teachPlanService;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CoursePublishMapper coursePublishMapper;

    @Autowired
    private MqMessageService mqMessageService;


    @Override
    public CoursePreviewDTO getCoursePreviewInfo(Long courseId) {
        CoursePreviewDTO coursePreviewDTO = new CoursePreviewDTO();
        //查询课程基本信息
        coursePreviewDTO.setCourseBase(courseBaseInfoService.getCourseBaseInfo(courseId));
        //查询课程教学计划信息
        coursePreviewDTO.setTeachPlans(teachPlanService.findTeachPlanTree(courseId));
        return coursePreviewDTO;
    }

    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {
        //查询课程
        //1.对已提交审核的课程不允许提交审核。
        CourseBaseInfoDTO courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        if ( courseBaseInfo == null ) {
            XueChengPlusException.cast("课程不存在");
        }
        //审核状态
        String auditStatus = courseBaseInfo.getAuditStatus();
        //若课程状态为已提交则不允许提交
        if ( "202003".equals(auditStatus) ) {
            XueChengPlusException.cast("课程已提交,不允许重复提交");
        }
        //2.没有上传图片不允许提交审核。
        String pic = courseBaseInfo.getPic();
        if ( StringUtils.isEmpty(pic) ) {
            XueChengPlusException.cast("请上传课程图片");
        }
        //查询课程计划
        //3.没有添加课程计划不允许提交审核。
        List<TeachplanDTO> teachPlanTree = teachPlanService.findTeachPlanTree(courseId);
        if ( teachPlanTree == null || teachPlanTree.isEmpty() ) {
            XueChengPlusException.cast("请编写课程计划");
        }
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        //将课程基本信息,营销信息,教学计划信息等插入课程预发布表
        //营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String courseMarketJsonString = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJsonString);
        //计划信息
        String teachPlanJsonString = JSON.toJSONString(teachPlanTree);
        coursePublishPre.setTeachplan(teachPlanJsonString);
        //设置状态为已提交
        coursePublishPre.setStatus("202003");
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        //保存到课程预发布表
        CoursePublishPre coursePublishPreObj = coursePublishPreMapper.selectById(courseId);
        if ( coursePublishPreObj == null ) {
            //插入
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            //更新
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //更新课程状态为已提交
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    @Override
    public void publish(Long companyId, Long courseId) {
        //查询课程预发布信息
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if ( coursePublishPre == null ) {
            XueChengPlusException.cast("课程预发布信息不存在");
        }
        //课程预发布信息状态
        String status = coursePublishPre.getStatus();
        //检查是否通过审核
        if ( !"202004".equals(status) ) {
            XueChengPlusException.cast("课程未通过审核,不能发布");
        }
        //向课程发布表写入数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);

        //发布课程
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(coursePublishPre, courseBase);
        //先查询课程发布表
        CoursePublish coursePublishObj = coursePublishMapper.selectById(courseId);
        if ( coursePublishObj == null ) {
            //插入
            coursePublishMapper.insert(coursePublish);
        } else {
            //更新
            coursePublishMapper.updateById(coursePublish);
        }

        //向消息表写记录
        saveCoursePublishMessage(courseId);

        //将预发布表信息删除
        coursePublishPreMapper.deleteById(courseId);
    }

    /**
     * @param courseId 课程id
     * @return void
     * @description 保存消息表记录
     * @author Mr.M
     * @date 2022/9/20 16:32
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish",
                String.valueOf(courseId),
                null,
                null
        );
        if ( mqMessage == null ) {
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
    }

}
