package com.xuecheng.content.service.impl;

import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;
    @Autowired
    private TeachPlanService teachPlanService;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId)
    {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();

        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfo);
        List<TeachplanDto> teachplanDtos = teachPlanService.selectTeachplanTree(courseId);
        coursePreviewDto.setTeachplans(teachplanDtos);

        log.warn("查询到信息为:{}", coursePreviewDto);
        return coursePreviewDto;
    }
}
