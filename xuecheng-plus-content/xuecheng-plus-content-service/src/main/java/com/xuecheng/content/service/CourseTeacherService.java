package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

public interface CourseTeacherService extends IService<CourseTeacher> {

    /**
     * 根据课程id查询教师信息集合
     * @param courseTeacherId 课程Id
     * @return 教师集合
     */
    List<CourseTeacherDto> getCourseTeacher(Long courseTeacherId);

    /**
     * 新增教师西悉尼并返回详细信息
     * @param courseTeacherDto 教师信息
     * @return 详细信息
     */
    CourseTeacherDto addCourseTeacher(CourseTeacherDto courseTeacherDto);


    void deleteCourseTeacher(Long courseId, Long courseTeacherId);
}
