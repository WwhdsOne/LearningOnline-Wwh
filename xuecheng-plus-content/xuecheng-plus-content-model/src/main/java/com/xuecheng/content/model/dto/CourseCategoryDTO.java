package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/11 18:57
 * @description 课程分类数据传输类型
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseCategoryDTO extends CourseCategory implements java.io.Serializable{
    //子节点
    List<CourseCategoryDTO> childrenTreeNodes;
}
