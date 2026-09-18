package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.feignclient.CourseIndex;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;


@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    private CoursePublishService coursePublishService;
    @Autowired
    private SearchServiceClient searchServiceClient;
    @Autowired
    private CoursePublishMapper coursePublishMapper;

    @XxlJob("CoursePublishJobHandler")
    public void CoursePublishJobHandler()
    {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        process(shardIndex, shardTotal, "course_publish", 30, 60);
    }

    @Override
    public boolean execute(MqMessage mqMessage)
    {
        long courseId = Long.parseLong(mqMessage.getBusinessKey1());

        // 上传静态文件到minio
        generateCourseHtml(mqMessage, courseId);

        // es建立索引
        saveCourseIndex(mqMessage, courseId);

        // redis写入缓存


        return true;
    }

    /**
     * 实现课程信息的静态化
     * @param mqMessage
     * @param courseId
     */
    private void generateCourseHtml(MqMessage mqMessage, Long courseId)
    {
        //任务幂等性确认

        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();

        int stageOne = mqMessageService.getStageOne(taskId);
        if(stageOne > 0){
            log.debug("课程静态化任务完成，无需处理...");
            return;
        }

        File file =  coursePublishService.generateCourseHtml(courseId);
        if(file == null){
            XueChengPlusException.cast("生成课程静态化页面为空");
        }
        coursePublishService.uploadCourseHtml(courseId, file);

        //执行课程静态化
        mqMessageService.completedStageOne(taskId);

    }

    /**
     * 建立es的课程索引
     * @param mqMessage
     * @param courseId
     */
    private void saveCourseIndex(MqMessage mqMessage, Long courseId)
    {
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if(stageTwo > 0){
            log.debug("课程索引信息已经建立，无需执行...");
            return;
        }

        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish, courseIndex);

        Boolean add = searchServiceClient.add(courseIndex);
        if(add == false){
            XueChengPlusException.cast("远程调用课程索引添加失败");
        }

        mqMessageService.completedStageTwo(taskId);
    }

}
