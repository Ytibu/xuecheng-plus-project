package com.xuecheng.content.api;


import com.alibaba.fastjson.JSON;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;


/**
 * 课程发布
 */
@Slf4j
@Controller
public class CoursePublishController {

    @Autowired
    private CoursePublishService coursePublishService;

    /**
     * 预览课程
     * @param courseId 课程Id
     * @return 课程预览模板
     */
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){

        ModelAndView modelAndView = new ModelAndView();
        CoursePreviewDto coursePreviewDto = coursePublishService.getCoursePreviewInfo(courseId);
        modelAndView.addObject("model",coursePreviewDto);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }

    /**
     * 课程提交
     * @param courseId 课程Id
     */
    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId)
    {
        log.warn("提交课程courseId：{}", courseId);
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            throw new RuntimeException("未获取到用户身份，请重新登录"); // 或走统一异常/401
        }
        Long companyId = StringUtils.isNotEmpty(user.getCompanyId()) ? Long.parseLong(user.getCompanyId()) : null;
        coursePublishService.commitAudit(companyId, courseId);
    }

    // 发布指定课程
    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping ("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId)
    {
        log.warn("发布课程courseId: {}", courseId);
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            throw new RuntimeException("未获取到用户身份，请重新登录"); // 或走统一异常/401
        }
        Long companyId = StringUtils.isNotEmpty(user.getCompanyId()) ? Long.parseLong(user.getCompanyId()) : null;
        coursePublishService.publish(companyId, courseId);
    }

    // 查询课程发布信息（内部）：审核人员进行课程审核，不需要权限等校验
    @ApiOperation("查询课程发布信息")
    @ResponseBody
    @GetMapping ("/r/coursepublish/{courseId}")
    public CoursePublish getCoursePublishInfo(@PathVariable("courseId") Long courseId)
    {
        log.warn("业务内部查询课程发布信息courseId: {}", courseId);
        return coursePublishService.getCoursePublish(courseId);
    }

    // 用户获取课程信息（对外）：用户获取课程从而学习课程，要进行权限等审核
    @ApiOperation("用户获取课程信息")
    @ResponseBody
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getCoursePublish(@PathVariable("courseId") Long courseId)
    {
        log.warn("用户外部获取课程发布信息courseId: {}", courseId);
        return coursePublishService.getCoursePreview(courseId);
    }
}
