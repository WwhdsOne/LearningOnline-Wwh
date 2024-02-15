package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.TeachplanDTO;
import com.xuecheng.content.model.po.Teachplan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    //课程计划查询
    List<TeachplanDTO> selectTreeNodes(Long courseId);

    //根据方向和课程计划ID调整顺序
    void updateOrderByDirectionAndId(@Param("direction") String direction,@Param("teachplan") Teachplan teachplan);
}
