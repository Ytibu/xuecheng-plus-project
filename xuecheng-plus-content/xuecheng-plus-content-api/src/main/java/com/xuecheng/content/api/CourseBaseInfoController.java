package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Api(value = "课程信息管理接口",tags = "课程信息管理接口")
@RestController
public class CourseBaseInfoController {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程查询接口")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required=false) QueryCourseParamsDto queryCourseParamsDto)
    {
        log.warn("要查询课程的课程信息:{}", queryCourseParamsDto);
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            throw new RuntimeException("未获取到用户身份，请重新登录"); // 或走统一异常/401
        }
        Long companyId = StringUtils.isNotEmpty(user.getCompanyId()) ? Long.parseLong(user.getCompanyId()) : null;
        return courseBaseInfoService.QueryCourseBaseList(companyId, pageParams, queryCourseParamsDto);
    }

    @ApiOperation("新增课程")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidationGroups.Insert.class) AddCourseDto addCourseDto)
    {
        log.warn("机构新增课程信息:{}", addCourseDto);
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            throw new RuntimeException("未获取到用户身份，请重新登录"); // 或走统一异常/401
        }
        Long companyId = StringUtils.isNotEmpty(user.getCompanyId()) ? Long.parseLong(user.getCompanyId()) : null;
        return courseBaseInfoService.createCourseBase(companyId, addCourseDto);
    }


    /**
     * 根据单个课程id查询课程信息
     * @param courseId 课程id
     * @return 课程信息
     */
    @ApiOperation("根据课程id查询课程信息")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseInfo(@PathVariable("courseId") Long courseId)
    {
        log.warn("根据课程id：{}查询课程信息", courseId);
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }

    @ApiOperation("修改课程信息")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBaseInfo(@RequestBody @Validated(ValidationGroups.Update.class) EditCourseDto editCourseDto)
    {
        log.warn("要修改的课程信息:{}", editCourseDto);
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            throw new RuntimeException("未获取到用户身份，请重新登录"); // 或走统一异常/401
        }
        Long companyId = StringUtils.isNotEmpty(user.getCompanyId()) ? Long.parseLong(user.getCompanyId()) : null;
        return courseBaseInfoService.updateCourseBase(companyId, editCourseDto);
    }

}
