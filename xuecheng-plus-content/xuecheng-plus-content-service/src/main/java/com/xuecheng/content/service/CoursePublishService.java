package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;

import java.io.File;

public interface CoursePublishService {

    /**
     * 预览课程
     * @param courseId 课程Id
     * @return 预览课程的信息
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 提交审核
     * @param companyId 机构Id
     * @param courseId 课程Id
     */
    void commitAudit(Long companyId,Long courseId);

    /**
     * 课程发布
     * @param companyId 机构Id
     * @param courseId 课程Id
     */
    void publish(Long companyId, Long courseId);

    /**
     * 查询课程发布信息
     * @param courseId 课程Id
     * @return 课程发布信息
     */
    CoursePublish getCoursePublish(Long courseId);

    /**
     * 生成课程静态页面
     * @param courseId 课程Id
     * @return 生成的页面文件
     */
    File generateCourseHtml(Long courseId);

    /**
     * 课程静态页面存储在minio上
     * @param courseId 课程Id
     * @param file 课程文件
     */
    void  uploadCourseHtml(Long courseId,File file);


    CoursePreviewDto getCourseInfo(Long courseId);
}
