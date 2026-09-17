package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;

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
}
