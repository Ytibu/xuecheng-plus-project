package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

public interface CourseTeacherService extends IService<CourseTeacher> {

    /**
     * 根据课程id查询教师信息集合
     * @param courseId 课程Id
     * @return 教师集合
     */
    List<CourseTeacherDto> getCourseTeacher(Long courseId);

    /**
     * 新增教师西悉尼并返回详细信息
     * @param courseTeacherDto 教师信息
     * @return 详细信息
     */
    CourseTeacherDto addCourseTeacher(CourseTeacherDto courseTeacherDto);

    /**
     * 删除指定课程下的指定教师
     * @param courseId 课程id
     * @param courseTeacherId 唯一的教师id
     */
    void deleteCourseTeacher(Long courseId, Long courseTeacherId);
}
