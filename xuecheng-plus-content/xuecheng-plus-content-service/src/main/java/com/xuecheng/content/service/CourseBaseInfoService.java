package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
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
}
