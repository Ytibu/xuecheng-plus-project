package com.xuecheng.content.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 课程分类树视图对象。
 */
@Data
@ApiModel(value = "CourseCategoryTreeVO", description = "课程分类树视图对象")
public class CourseCategoryTreeVO {

    @ApiModelProperty("主键")
    private String id;

    @ApiModelProperty("分类名称")
    private String name;

    @ApiModelProperty("分类标签，默认和名称一样")
    private String label;

    @ApiModelProperty("父结点id")
    private String parentid;

    @ApiModelProperty("是否显示")
    private Integer isShow;

    @ApiModelProperty("排序字段")
    private Integer orderby;

    @ApiModelProperty("是否叶子")
    private Integer isLeaf;

    @ApiModelProperty("子分类")
    private List<CourseCategoryTreeVO> childrenTreeNodes;
}
