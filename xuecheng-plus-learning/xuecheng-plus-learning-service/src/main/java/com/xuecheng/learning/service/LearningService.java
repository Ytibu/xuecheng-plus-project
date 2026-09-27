package com.xuecheng.learning.service;

import com.xuecheng.base.model.RestResponse;

public interface LearningService {

    /**
     * 根据信息获取用户是否可以学习该课程资源
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param teachPlanId 课程计划ID
     * @param mediaId 媒资ID
     * @return 信息
     */
    RestResponse<String> getVideo(String userId, Long courseId, Long teachPlanId, String mediaId);
}
