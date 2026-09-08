package com.xuecheng.content.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程列表项视图对象。
 */
@Data
@ApiModel(value = "CourseBaseVO", description = "课程基础信息视图对象")
public class CourseBaseVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("机构ID")
    private Long companyId;

    @ApiModelProperty("机构名称")
    private String companyName;

    @ApiModelProperty("课程名称")
    private String name;

    @ApiModelProperty("适用人群")
    private String users;

    @ApiModelProperty("课程标签")
    private String tags;

    @ApiModelProperty("大分类")
    private String mt;

    @ApiModelProperty("小分类")
    private String st;

    @ApiModelProperty("课程等级")
    private String grade;

    @ApiModelProperty("教育模式(common普通，record录播，live直播等)")
    private String teachmode;

    @ApiModelProperty("课程介绍")
    private String description;

    @ApiModelProperty("课程图片")
    private String pic;

    @ApiModelProperty("创建时间")
    private LocalDateTime createDate;

    @ApiModelProperty("修改时间")
    private LocalDateTime changeDate;

    @ApiModelProperty("创建人")
    private String createPeople;

    @ApiModelProperty("更新人")
    private String changePeople;

    @ApiModelProperty("审核状态")
    private String auditStatus;

    @ApiModelProperty("课程发布状态：未发布、已发布、下线")
    private String status;
}
