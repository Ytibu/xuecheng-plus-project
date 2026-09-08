package com.xuecheng.content.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 修改课程信息入参视图对象。
 */
@Data
@ApiModel(value = "EditCourseVO", description = "修改课程基本信息")
public class EditCourseVO extends AddCourseVO {

    @ApiModelProperty(value = "课程id", required = true)
    private Long id;
}
