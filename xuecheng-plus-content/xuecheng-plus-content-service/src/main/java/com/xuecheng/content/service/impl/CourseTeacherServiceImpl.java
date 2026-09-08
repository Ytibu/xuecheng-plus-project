package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XuechengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher> implements CourseTeacherService {

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    /**
     * 根据课程Id查看课程下所有的教师信息
     * @param courseId 课程Id
     * @return 教师信息集合
     */
    @Override
    public List<CourseTeacherDto> getCourseTeacher(Long courseId)
    {

        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        List<CourseTeacher> teachers = courseTeacherMapper.selectList(queryWrapper);

        ArrayList<CourseTeacherDto> courseTeacherDtos = new ArrayList<>(teachers.size());
        for (CourseTeacher teacher : teachers) {
            CourseTeacherDto courseTeacherDto = new CourseTeacherDto();
            BeanUtils.copyProperties(teacher, courseTeacherDto);
            courseTeacherDtos.add(courseTeacherDto);
        }

        return courseTeacherDtos;
    }

    @Override
    public CourseTeacherDto addCourseTeacher(CourseTeacherDto courseTeacherDto)
    {
        CourseTeacher courseTeacher = new CourseTeacher();
        BeanUtils.copyProperties(courseTeacherDto, courseTeacher);
        int insert = courseTeacherMapper.insert(courseTeacher);
        if (insert <= 0){
            XuechengPlusException.cast("插入数据失败");
        }
        CourseTeacher teacher = courseTeacherMapper.selectById(courseTeacher.getId());

        CourseTeacherDto resultDto = new CourseTeacherDto();
        BeanUtils.copyProperties(teacher, resultDto);

        return resultDto;
    }

    @Override
    public void deleteCourseTeacher(Long courseId, Long courseTeacherId)
    {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(CourseTeacher::getCourseId, courseId)
                .eq(CourseTeacher::getId, courseTeacherId);

        courseTeacherMapper.delete(queryWrapper);
    }
}
