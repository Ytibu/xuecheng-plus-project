package com.xuecheng.content.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程计划媒资视图对象。
 */
@Data
@ApiModel(value = "TeachplanMediaVO", description = "课程计划媒资视图对象")
public class TeachplanMediaVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("媒资文件id")
    private String mediaId;

    @ApiModelProperty("课程计划标识")
    private Long teachplanId;

    @ApiModelProperty("课程标识")
    private Long courseId;

    @ApiModelProperty("媒资文件原始名称")
    private String mediaFilename;

    @ApiModelProperty("创建时间")
    private LocalDateTime createDate;

    @ApiModelProperty("创建人")
    private String createPeople;

    @ApiModelProperty("修改人")
    private String changePeople;
}
