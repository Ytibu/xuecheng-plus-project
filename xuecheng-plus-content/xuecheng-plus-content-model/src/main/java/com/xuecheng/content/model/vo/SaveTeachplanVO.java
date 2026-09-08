package com.xuecheng.content.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 保存课程计划入参视图对象。
 */
@Data
@ApiModel(value = "SaveTeachplanVO", description = "保存课程计划入参视图对象")
public class SaveTeachplanVO {

    @ApiModelProperty("课程计划id")
    private Long id;

    @ApiModelProperty("课程计划名称")
    private String pname;

    @ApiModelProperty("课程计划父级Id")
    private Long parentid;

    @ApiModelProperty("层级，分为1、2、3级")
    private Integer grade;

    @ApiModelProperty("课程类型：1视频、2文档")
    private String mediaType;

    @ApiModelProperty("课程标识")
    private Long courseId;

    @ApiModelProperty("课程发布标识")
    private Long coursePubId;

    @ApiModelProperty("是否支持试学或预览（试看）")
    private String isPreview;
}
