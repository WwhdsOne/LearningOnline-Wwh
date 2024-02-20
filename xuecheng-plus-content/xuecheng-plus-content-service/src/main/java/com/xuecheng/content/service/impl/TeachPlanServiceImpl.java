package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachPlanMediaDTO;
import com.xuecheng.content.model.dto.SaveTeachPlanDTO;
import com.xuecheng.content.model.dto.TeachplanDTO;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;


    public List<TeachplanDTO> findTeachPlanTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    /**
     * 统计同类课程个数
     *
     * @param courseId
     * @param parentId
     * @return 同类课程总数
     */
    private Integer getTeachPlanCount(Long courseId, Long parentId) {
        LambdaQueryWrapper<Teachplan> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Teachplan::getCourseId, courseId);
        lambdaQueryWrapper.eq(Teachplan::getParentid, parentId);
        //计算个数
        return teachplanMapper.selectCount(lambdaQueryWrapper);
    }

    @Override
    public void saveTeachPlan(SaveTeachPlanDTO saveTeachPlanDTO) {
        //通过课程ID判断是否是新增还是修改
        Long teachplanId = saveTeachPlanDTO.getId();
        if ( teachplanId == null ) {
            //新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachPlanDTO, teachplan);
            //确定排序字段
            //select count(*) from teachplan where course_id=117 and parentid=268
            Long courseId = saveTeachPlanDTO.getCourseId();
            Long parentId = saveTeachPlanDTO.getParentid();
            Integer count = getTeachPlanCount(courseId, parentId);
            teachplan.setOrderby(count + 1);
            teachplanMapper.insert(teachplan);
        } else {
            //修改
            Teachplan teachplan = teachplanMapper.selectById(teachplanId);
            BeanUtils.copyProperties(saveTeachPlanDTO, teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long teachPlanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
        //当其为一级章节时,判断其是否有子章节存在
        if ( teachplan.getParentid() == 0 ) {
            Long parentId = teachplan.getId();
            Long courseId = teachplan.getCourseId();
            Integer count = getTeachPlanCount(courseId, parentId);
            if ( count == 0 ) {
                teachplanMapper.deleteById(teachplan);
            } else {
                XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
            }
        } else {
            //当其为二级章节时,连同视频资源一起删除
            teachplanMapper.deleteById(teachPlanId);
            LambdaQueryWrapper<TeachplanMedia> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(TeachplanMedia::getTeachplanId, teachPlanId);
            teachplanMediaMapper.delete(lambdaQueryWrapper);
        }
    }

    @Override
    @Transactional
    public void updateOrderByDirectionAndId(String direction, Long teachPlanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
//        LambdaQueryWrapper<Teachplan> isExist = new LambdaQueryWrapper<>();
//        isExist.eq(Teachplan::getCourseId,teachplan.getCourseId());
//        isExist.eq(Teachplan::getParentid,teachplan.getParentid());
//        if( Objects.equals(direction, "moveup") ){
//            isExist.eq(Teachplan::getOrderby,teachplan.getOrderby()-1);
//            Teachplan teachPlanBefore = teachplanMapper.selectOne(isExist);
//            if(teachPlanBefore != null){
//                teachplan.setOrderby(teachplan.getOrderby()-1);
//                teachPlanBefore.setOrderby(teachPlanBefore.getOrderby()+1);
//                teachplanMapper.updateById(teachplan);
//                teachplanMapper.updateById(teachPlanBefore);
//            }
//        }else if(Objects.equals(direction, "movedown")){
//            isExist.eq(Teachplan::getOrderby,teachplan.getOrderby()+1);
//            Teachplan teachPlanAfter = teachplanMapper.selectOne(isExist);
//            if(teachPlanAfter != null){
//                teachplan.setOrderby(teachplan.getOrderby());
//                teachPlanAfter.setOrderby(teachPlanAfter.getOrderby()-1);
//                teachplanMapper.updateById(teachplan);
//                teachplanMapper.updateById(teachPlanAfter);
//            }
//        }else{
//            XueChengPlusException.cast("参数错误,请输入正确move参数");
//        }
        teachplanMapper.updateOrderByDirectionAndId(direction, teachplanMapper.selectById(teachPlanId));
    }

    @Transactional
    @Override
    public TeachplanMedia associationMedia(BindTeachPlanMediaDTO bindTeachPlanMediaDTO) {
        //合法性校验
        Long teachplanId = bindTeachPlanMediaDTO.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if ( teachplan == null ) {
            XueChengPlusException.cast("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if ( grade != 2 ) {
            XueChengPlusException.cast("只允许第二级教学计划绑定媒资文件");
        }
        //课程id
        Long courseId = teachplan.getCourseId();


        //删除原有记录,根据课程id删除对应媒资
        LambdaQueryWrapper<TeachplanMedia> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TeachplanMedia::getTeachplanId, bindTeachPlanMediaDTO.getTeachplanId());
        teachplanMediaMapper.delete(lambdaQueryWrapper);
        //添加原有记录
        //再添加教学计划与媒资的绑定关系
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaFilename(bindTeachPlanMediaDTO.getFileName());
        teachplanMedia.setMediaId(bindTeachPlanMediaDTO.getMediaId());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);
        return teachplanMedia;
    }

    @Override
    public void unbindMedia(Long teachPlanId, String mediaId) {
        //删除原有记录,根据课程id删除对应媒资
        LambdaQueryWrapper<TeachplanMedia> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TeachplanMedia::getTeachplanId, teachPlanId);
        lambdaQueryWrapper.eq(TeachplanMedia::getMediaId, mediaId);
        teachplanMediaMapper.delete(lambdaQueryWrapper);
    }
}
