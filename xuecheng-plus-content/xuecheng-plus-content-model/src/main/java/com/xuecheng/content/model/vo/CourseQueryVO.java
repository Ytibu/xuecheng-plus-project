package com.xuecheng.content.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 课程分页查询条件视图对象。
 */
@Data
@ToString
@ApiModel(value = "CourseQueryVO", description = "课程分页查询条件")
public class CourseQueryVO {

    @ApiModelProperty("审核状态")
    private String auditStatus;

    @ApiModelProperty("课程名称")
    private String courseName;

    @ApiModelProperty("发布状态")
    private String publishStatus;
}
