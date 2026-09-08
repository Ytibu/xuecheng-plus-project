package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
 * 课程计划管理
 */
public interface TeachPlanService extends IService<Teachplan> {

    /**
     * 课程计划查询
     * @param courseId 课程id
     * @return 查询结果
     */
    List<TeachplanDto> selectTeachplanTree(Long courseId);


    /**
     * 新增/修改/保存课程计划
     * @param saveTeachplanDto 课程计划信息
     */
    void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    /**
     * 删除指定的课程计划章节
     * @param id 课程计划章节
     */
    void deleteTeachplan(Long id);

    /**
     * 向上移动指定章节
     * @param id 章节id
     */
    void moveUpTeachplan(Long id);

    /**
     * 向下移动指定章节
     * @param id 章节id
     */
    void moveDownTeachplan(Long id);

}
