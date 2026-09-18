package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        // 1. 有 id → 更新
        if (courseTeacherDto.getId() != null) {
            CourseTeacher exist = courseTeacherMapper.selectById(courseTeacherDto.getId());
            if (exist != null) {
                courseTeacherMapper.updateById(courseTeacherDto);
                CourseTeacher teacher = courseTeacherMapper.selectById(courseTeacherDto.getId());
                CourseTeacherDto resultDto = new CourseTeacherDto();
                BeanUtils.copyProperties(teacher, resultDto);
                return resultDto;   // ← 必须 return
            }
            // id 传了但库里没有，落到下面走新增
        }

        // 2. 无 id → 先按唯一索引查重，再决定 insert 还是 update
        LambdaQueryWrapper<CourseTeacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseTeacher::getCourseId, courseTeacherDto.getCourseId())
                .eq(CourseTeacher::getTeacherName, courseTeacherDto.getTeacherName());
        CourseTeacher exist = courseTeacherMapper.selectOne(wrapper);

        CourseTeacher courseTeacher = new CourseTeacher();
        BeanUtils.copyProperties(courseTeacherDto, courseTeacher);

        if (exist != null) {
            // 已存在同课程同教师，改为更新
            courseTeacher.setId(exist.getId());
            courseTeacherMapper.updateById(courseTeacher);
        } else {
            courseTeacher.setCreateDate(LocalDateTime.now());
            int insert = courseTeacherMapper.insert(courseTeacher);
            if (insert <= 0) {
                XueChengPlusException.cast("插入数据失败");
            }
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
