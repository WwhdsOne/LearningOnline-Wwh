package com.xuecheng.content.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/26 16:44
 * @description 课程预览DTO
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoursePreviewDTO {
    //课程基本信息,营销信息(两信息合并)
    private CourseBaseInfoDTO courseBase;
    //课程教学计划信息
    private List<TeachplanDTO> teachPlans;
    //师资信息暂时不添加
}
