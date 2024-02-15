package com.xuecheng.content.model.po;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import java.util.Date;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;


/**
*
* @TableName teachplan_work
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeachplanWork implements Serializable {

    /**
     * 主键
     */
    @NotNull(message = "[主键]不能为空")
    @ApiModelProperty("主键")
    private Long id;
    /**
     * 作业信息标识
     */
    @NotNull(message = "[作业信息标识]不能为空")
    @ApiModelProperty("作业信息标识")
    private Long workId;
    /**
     * 作业标题
     */
    @NotBlank(message = "[作业标题]不能为空")
    @Size(max = 60, message = "编码长度不能超过60")
    @ApiModelProperty("作业标题")
    @Length(max = 60, message = "编码长度不能超过60")
    private String workTitle;
    /**
     * 课程计划标识
     */
    @NotNull(message = "[课程计划标识]不能为空")
    @ApiModelProperty("课程计划标识")
    private Long teachplanId;
    /**
     * 课程标识
     */
    @ApiModelProperty("课程标识")
    private Long courseId;
    /**
     *
     */
    @ApiModelProperty("")
    private Date createDate;
    /**
     *
     */
    @ApiModelProperty("")
    private Long coursePubId;
}
