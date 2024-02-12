package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/12 19:09
 * @description 课程计划类DTO
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class TeachplanDTO extends Teachplan {
    //获取媒资信息
    private TeachplanMedia teachplanMedia;
    //获取课程计划的子节点
    private List<TeachplanDTO> teachPlanTreeNodes;

}
