package com.xuecheng.content.api;


import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

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
        coursePublishService.commitAudit(1232141425L, courseId);
    }

    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping ("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId)
    {
        log.warn("发布课程courseId: {}", courseId);
        coursePublishService.publish(1232141425L, courseId);
    }

    @ApiOperation("查询课程发布信息")
    @ResponseBody
    @GetMapping ("/r/coursepublish/{courseId}")
    public CoursePublish getCoursePublish(@PathVariable("courseId") Long courseId)
    {
        log.warn("查询课程发布信息courseId: {}", courseId);
        return coursePublishService.getCoursePublish(courseId);
    }
}
