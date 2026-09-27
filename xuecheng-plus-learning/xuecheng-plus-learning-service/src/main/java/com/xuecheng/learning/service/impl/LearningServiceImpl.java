package com.xuecheng.learning.service.impl;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LearningServiceImpl implements LearningService {

    @Autowired
    private MyCourseTablesService myCourseTablesService;
    @Autowired
    private ContentServiceClient contentServiceClient;
    @Autowired
    private MediaServiceClient mediaServiceClient;

    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachPlanId, String mediaId) {

        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if(coursepublish == null){
            return RestResponse.validfail("课程不存在");
        }

        if(StringUtils.isEmpty(userId)){
            XcCourseTablesDto courseTablesDto = myCourseTablesService.getLearningStatus(userId, courseId);
            String learnStatus = courseTablesDto.getLearnStatus();
            if("702002".equals(learnStatus)){
                return RestResponse.validfail("无法学习，没有选课或选课后未支付");
            }else if("702003".equals(learnStatus)){
                return RestResponse.validfail("课程过期，需要申请续期或重新支付");
            }else {
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            }

        }

        String charge = coursepublish.getCharge();
        if("201000".equals(charge)){
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }
        return RestResponse.validfail("该课程需要购买");
    }
}
