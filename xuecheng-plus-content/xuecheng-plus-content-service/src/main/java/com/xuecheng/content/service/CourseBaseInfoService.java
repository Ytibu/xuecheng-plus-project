package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

public interface CourseBaseInfoService {


    /**
     * 课程分页查询
     * @param pageParams 查询参数（当前页，页面条数）
     * @param courseParamsDto 查询条件
     * @return 封装好的返回数据
     */
    PageResult<CourseBase> QueryCourseBaseList(PageParams pageParams, QueryCourseParamsDto courseParamsDto);

    /**
     * 新增课程
     * @param companyId 所属机构ID
     * @param addCourseDto 具体的课程信息
     * @return 课程信息
     */
    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);


    /**
     * 根据课程id查询课程信息
     * @param courseId 课程ID
     * @return 查询到的课程信息
     */
    CourseBaseInfoDto getCourseBaseInfo(Long courseId);


    /**
     * 修改课程信息
     * @param companyId 机构ID
     * @param editCourseDto 课程信息
     * @return 修改后课程信息
     */
    CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto);

}
