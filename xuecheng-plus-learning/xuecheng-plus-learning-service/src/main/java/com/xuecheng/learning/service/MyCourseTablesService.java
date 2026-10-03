package com.xuecheng.learning.service;

import com.xuecheng.base.model.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcCourseTables;

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

    /**
     * 确认保存选课是否成功
     * @param chooseCourseId 选课ID
     * @return 成功与否
     */
    boolean saveChooseCourseSuccess(String chooseCourseId);

    /**
     * 查询指定用户的课程表
     * @param params 查询参数
     * @return 课程表
     */
    PageResult<XcCourseTables> myCourseTable(MyCourseTableParams params);

}
