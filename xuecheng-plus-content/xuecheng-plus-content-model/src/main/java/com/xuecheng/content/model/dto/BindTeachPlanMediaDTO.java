package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/20 18:23
 * @description 绑定媒资和教学计划
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "BindTeachplanMediaDTO", description = "教学计划-媒资绑定提交数据")
public class BindTeachPlanMediaDTO {
    @ApiModelProperty(value = "媒资文件id", required = true)
    private String mediaId;

    @ApiModelProperty(value = "媒资文件名称", required = true)
    private String fileName;

    @ApiModelProperty(value = "课程计划标识", required = true)
    private Long teachplanId;
}
