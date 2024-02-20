package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachPlanMediaDTO;
import com.xuecheng.content.model.dto.SaveTeachPlanDTO;
import com.xuecheng.content.model.dto.TeachplanDTO;
import com.xuecheng.content.model.po.TeachplanMedia;

import java.util.List;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/14 17:45
 * @description 教学计划接口
 **/
public interface TeachPlanService {
    /**
     * 根据课程ID查询教学计划
     * @param courseId 课程ID
     * @return 教学计划
     */
    List<TeachplanDTO> findTeachPlanTree(Long courseId);

    /**
     * 新增或修改教学计划
     * @param saveTeachPlanDTO 保存教学计划信息
     */
    void saveTeachPlan(SaveTeachPlanDTO saveTeachPlanDTO);

    /**
     * 根据ID删除课程
     * @param teachPlanId 课程ID
     */
    void deleteById(Long teachPlanId);

    /**
     * 根据方向和ID修改课程计划顺序
     * @param direction 方向
     * @param teachPlanId 课程计划ID
     */
    void updateOrderByDirectionAndId(String direction, Long teachPlanId);

    /**
     * 课程计划和媒资信息绑定
     * @param bindTeachPlanMediaDTO 绑定信息
     */
    TeachplanMedia associationMedia(BindTeachPlanMediaDTO bindTeachPlanMediaDTO);

    /**
     * 课程计划和媒资信息解绑
     * @param teachPlanId 课程计划ID
     * @param mediaId 媒资ID
     */
    void unbindMedia(Long teachPlanId, String mediaId);
}
