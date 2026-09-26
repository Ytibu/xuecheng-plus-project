package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MyCourseTablesServiceImpl implements MyCourseTablesService {

    @Autowired
    private XcChooseCourseMapper xcChooseCourseMapper;
    @Autowired
    private XcCourseTablesMapper xcCourseTablesMapper;
    @Autowired
    private ContentServiceClient contentServiceClient;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId)
    {
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (coursepublish == null) {
            XueChengPlusException.cast("课程不存在");
        }

        String charge = coursepublish.getCharge();
        XcChooseCourse xcChooseCourse;
        if("201000".equals(charge)){
            // 免费课程，向选课记录表写入内容，向我的课程表写入内容
            xcChooseCourse = addFreeCourse(userId, coursepublish);
            XcCourseTables xcCourseTables = addCourseTables(xcChooseCourse);
        }else {
            // 收费课程，选课记录写入内容
            xcChooseCourse = addChargeCourse(userId, coursepublish);
        }

        // 获取学习资格
        XcCourseTablesDto learningStatus = getLearningStatus(userId, courseId);

        // 构造返回值
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(xcChooseCourse, xcChooseCourseDto);
        // 设置学习资格
        xcChooseCourseDto.setLearnStatus(learningStatus.getLearnStatus());

        return xcChooseCourseDto;
    }

    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId)
    {
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();

        // 向课程表查找信息，没有则说明没有选课或者没有支付，设置状态
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        if(xcCourseTables == null){
            xcCourseTablesDto.setLearnStatus("702002");
            return xcCourseTablesDto;
        }

        BeanUtils.copyProperties(xcCourseTables,xcCourseTablesDto);
        //是否过期,true过期，false未过期
        boolean isExpires = xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        if(!isExpires){
            //正常学习
            xcCourseTablesDto.setLearnStatus("702001");

        }else{
            //已过期
            xcCourseTablesDto.setLearnStatus("702003");
        }
        return xcCourseTablesDto;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveChooseCourseSuccess(String chooseCourseId) {

        // 选课记录
        XcChooseCourse xcChooseCourse = xcChooseCourseMapper.selectById(chooseCourseId);
        if(xcChooseCourse == null){
            log.warn("选课记录不存在:{}",chooseCourseId);
            return false;
        }

        // 选课状态（已选课或未支付）
        String status = xcChooseCourse.getStatus();
        if(status.equals("701002")){
            xcChooseCourse.setStatus("701001");
            int i = xcChooseCourseMapper.updateById(xcChooseCourse);
            if(i <= 0){
                log.warn("添加选课记录失败:{}",chooseCourseId);
                XueChengPlusException.cast("添加选课记录失败");
            }

            XcCourseTables xcCourseTables = addCourseTables(xcChooseCourse);
            if(xcCourseTables == null){
                log.warn("课程表保存失败:{}",chooseCourseId);
            }
        }

        return true;
    }


    //添加免费课程,免费课程加入选课记录表、我的课程表
    public XcChooseCourse addFreeCourse(String userId, CoursePublish coursepublish)
    {
        Long courseId = coursepublish.getId();
        LambdaQueryWrapper<XcChooseCourse> eq = new LambdaQueryWrapper<XcChooseCourse>().eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, courseId)
                .eq(XcChooseCourse::getOrderType, "700001")
                .eq(XcChooseCourse::getStatus, "701001");

        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(eq);
        if(!xcChooseCourses.isEmpty()){
            return xcChooseCourses.get(0);
        }

        // TODO 数据未完整填充
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(courseId);
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700001");
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setValidDays(365);
        xcChooseCourse.setStatus("701001");
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));

        int insert = xcChooseCourseMapper.insert(xcChooseCourse);
        if(insert<=0){
            XueChengPlusException.cast("添加选课记录失败");
        }

        return xcChooseCourse;
    }

    /**
     * 根据课程和用户查询我的可充中某一门课程
     * @param userId  用户ID
     * @param courseId 课程ID
     * @return 课程表信息
     */
    public XcCourseTables getXcCourseTables(String userId, Long courseId)
    {
        return xcCourseTablesMapper.selectOne(
                new LambdaQueryWrapper<XcCourseTables>()
                        .eq(XcCourseTables::getUserId, userId)
                        .eq(XcCourseTables::getCourseId, courseId)
        );
    }

    //添加收费课程
    public XcChooseCourse addChargeCourse(String userId, CoursePublish coursepublish)
    {
        Long courseId = coursepublish.getId();
        LambdaQueryWrapper<XcChooseCourse> eq = new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, courseId)
                .eq(XcChooseCourse::getOrderType, "700002")
                .eq(XcChooseCourse::getStatus, "701002");
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(eq);
        if(!xcChooseCourses.isEmpty()){
            return xcChooseCourses.get(0);
        }

        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(courseId);
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700002");
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setValidDays(365);
        xcChooseCourse.setStatus("701002");
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));

        int insert = xcChooseCourseMapper.insert(xcChooseCourse);
        if(insert<=0){
            XueChengPlusException.cast("添加选课记录失败");
        }

        return xcChooseCourse;
    }
    //添加到我的课程表
    public XcCourseTables addCourseTables(XcChooseCourse xcChooseCourse)
    {
        String status = xcChooseCourse.getStatus();
        if(!"701001".equals(status)){
            XueChengPlusException.cast("选课没有成功添加到课程表中");
        }

        XcCourseTables xcCourseTables = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if(xcCourseTables != null){
            return xcCourseTables;
        }

        xcCourseTables = new XcCourseTables();
        BeanUtils.copyProperties(xcChooseCourse, xcCourseTables);
        xcCourseTables.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTables.setCourseType(xcChooseCourse.getOrderType());
        xcCourseTables.setCreateDate(LocalDateTime.now());
        int insert = xcCourseTablesMapper.insert(xcCourseTables);
        if(insert<=0){
            XueChengPlusException.cast("添加我的课程表失败");
        }

        return xcCourseTables;
    }
}
