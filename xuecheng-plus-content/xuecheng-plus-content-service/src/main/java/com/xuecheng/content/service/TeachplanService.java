package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
 * 课程计划管理
 */
public interface TeachplanService {

    /**
     * 课程计划查询
     * @param courseId 课程id
     * @return 查询结果
     */
    List<TeachplanDto> selectTeachplanTree(Long courseId);

}
