package com.xuecheng.learning.service;

import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;

/**
 * 选课相关的接口
 */
public interface MyCourseTablesService {

    /**
     * 添加选课
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 选课信息
     */
    XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    /**
     * 判断学习资格
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 课程表信息
     */
    XcCourseTablesDto getLearningStatus(String userId, Long courseId);
}
