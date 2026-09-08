package com.xuecheng.content.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程教师信息视图对象。
 */
@Data
@ApiModel(value = "CourseTeacherVO", description = "课程教师信息视图对象")
public class CourseTeacherVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("课程标识")
    private Long courseId;

    @ApiModelProperty("教师名称")
    private String teacherName;

    @ApiModelProperty("教师职位")
    private String position;

    @ApiModelProperty("教师简介")
    private String introduction;

    @ApiModelProperty("照片")
    private String photograph;

    @ApiModelProperty("创建时间")
    private LocalDateTime createDate;
}
