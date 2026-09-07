package com.xuecheng.content.api;


import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(value = "师资信息操作接口", tags = "师资信息操作接口")
@RestController
public class CourseTeacherController {

    @Autowired
    private CourseTeacherService courseTeacherService;

    @ApiOperation("查看指定教师的师资信息")
    @GetMapping("/courseTeacher/list/{courseTeacherId}")
    public List<CourseTeacherDto> getCourseTeacher(@PathVariable Long courseTeacherId)
    {
        log.info("查询教师信息courseTeacherId:{}", courseTeacherId);
        return courseTeacherService.getCourseTeacher(courseTeacherId);
    }

    @ApiOperation("增加教师信息")
    @PostMapping("/courseTeacher")
    public void addCourseTeacher(@RequestBody CourseTeacherDto courseTeacherDto)
    {
        log.warn("新增教师信息courseTeacherDto:{}", courseTeacherDto);
        courseTeacherService.addCourseTeacher(courseTeacherDto);
    }

    @ApiOperation("删除指定课程下的教师信息")
    @DeleteMapping("/courseTeacher/course/{courseId}/{courseTeacherId}")
    public void deleteCourseTeacher(@PathVariable Long courseId, @PathVariable Long courseTeacherId)
    {
        log.warn("删除课程:courseId:{}下的教师courseTeacherId:{}", courseId, courseTeacherId);
        courseTeacherService.deleteCourseTeacher(courseId, courseTeacherId);
    }
}
