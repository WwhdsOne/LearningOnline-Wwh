package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachPlanDTO;
import com.xuecheng.content.model.dto.TeachplanDTO;

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
     * @param courseId
     * @return 教学计划
     */
    List<TeachplanDTO> findTeachPlanTree(Long courseId);

    /**
     * 新增或修改教学计划
     * @param saveTeachPlanDTO
     */
    void saveTeachPlan(SaveTeachPlanDTO saveTeachPlanDTO);

    /**
     * 根据ID删除课程
     * @param teachPlanId
     */
    void deleteById(Long teachPlanId);

    /**
     * 根据方向和ID修改课程计划顺序
     * @param direction
     * @param teachPlanId
     */
    void updateOrderByDirectionAndId(String direction, Long teachPlanId);
}
