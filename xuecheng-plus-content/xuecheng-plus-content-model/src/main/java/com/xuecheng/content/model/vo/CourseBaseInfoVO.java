package com.xuecheng.content.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 课程详情视图对象。
 */
@Data
@ApiModel(value = "CourseBaseInfoVO", description = "课程详情视图对象")
public class CourseBaseInfoVO extends CourseBaseVO {

    @ApiModelProperty("收费规则，对应数据字典")
    private String charge;

    @ApiModelProperty("价格")
    private Float price;

    @ApiModelProperty("原价")
    private Float originalPrice;

    @ApiModelProperty("咨询qq")
    private String qq;

    @ApiModelProperty("微信")
    private String wechat;

    @ApiModelProperty("电话")
    private String phone;

    @ApiModelProperty("有效期天数")
    private Integer validDays;

    @ApiModelProperty("大分类名称")
    private String mtName;

    @ApiModelProperty("小分类名称")
    private String stName;
}
