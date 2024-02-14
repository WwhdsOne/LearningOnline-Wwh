package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachPlanDTO;
import com.xuecheng.content.model.dto.TeachplanDTO;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/14 17:45
 * @description 教学计划相关操作
 **/
@Service
@Slf4j
public class TeachPlanServiceImpl implements TeachPlanService {

    @Autowired
    TeachplanMapper teachplanMapper;

    public List<TeachplanDTO> findTeachPlanTree(Long courseId){
        return teachplanMapper.selectTreeNodes(courseId);
    }

    /**
     * 统计同类课程个数
     * @param courseId
     * @param parentId
     * @return 同类课程总数
     */
    private Integer getTeachPlanCount(Long courseId,Long parentId){
        LambdaQueryWrapper<Teachplan> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Teachplan::getCourseId,courseId);
        lambdaQueryWrapper.eq(Teachplan::getParentid,parentId);
        //计算个数
        return teachplanMapper.selectCount(lambdaQueryWrapper);
    }
    @Override
    public void saveTeachPlan(SaveTeachPlanDTO saveTeachPlanDTO) {
        //通过课程ID判断是否是新增还是修改
        Long teachplanId = saveTeachPlanDTO.getId();
        if(teachplanId == null){
            //新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachPlanDTO,teachplan);
            //确定排序字段
            //select count(*) from teachplan where course_id=117 and parentid=268
            Long courseId = saveTeachPlanDTO.getCourseId();
            Long parentId = saveTeachPlanDTO.getParentid();
            Integer count = getTeachPlanCount(courseId,parentId);
            teachplan.setOrderby(count + 1);
            teachplanMapper.insert(teachplan);
        }else{
            //修改
            Teachplan teachplan = teachplanMapper.selectById(teachplanId);
            BeanUtils.copyProperties(saveTeachPlanDTO,teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }
}
