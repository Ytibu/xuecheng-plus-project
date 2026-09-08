package com.xuecheng.content.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程计划树视图对象。
 */
@Data
@ApiModel(value = "TeachplanVO", description = "课程计划树视图对象")
public class TeachplanVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("课程计划名称")
    private String pname;

    @ApiModelProperty("课程计划父级Id")
    private Long parentid;

    @ApiModelProperty("层级，分为1、2、3级")
    private Integer grade;

    @ApiModelProperty("课程类型：1视频、2文档")
    private String mediaType;

    @ApiModelProperty("开始直播时间")
    private LocalDateTime startTime;

    @ApiModelProperty("直播结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty("章节及课程介绍")
    private String description;

    @ApiModelProperty("时长，单位时:分:秒")
    private String timelength;

    @ApiModelProperty("排序字段")
    private Integer orderby;

    @ApiModelProperty("课程标识")
    private Long courseId;

    @ApiModelProperty("课程发布标识")
    private Long coursePubId;

    @ApiModelProperty("状态（1正常 0删除）")
    private Integer status;

    @ApiModelProperty("是否支持试学或预览（试看）")
    private String isPreview;

    @ApiModelProperty("创建时间")
    private LocalDateTime createDate;

    @ApiModelProperty("修改时间")
    private LocalDateTime changeDate;

    @ApiModelProperty("课程计划关联媒资")
    private TeachplanMediaVO teachplanMedia;

    @ApiModelProperty("子课程计划")
    private List<TeachplanVO> teachPlanTreeNodes;
}
