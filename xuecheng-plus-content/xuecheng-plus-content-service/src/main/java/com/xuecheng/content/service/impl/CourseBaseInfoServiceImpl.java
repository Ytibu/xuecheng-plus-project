package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;


    /**
     * 分页查询
     * @param pageParams 查询参数（当前页，每页信息数目）
     * @param courseParamsDto 查询条件
     * @return 返回数据
     */
    @Override
    public PageResult<CourseBase> QueryCourseBaseList(Long companyId, PageParams pageParams, QueryCourseParamsDto courseParamsDto) {

        // 拼装查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        // 模糊匹配
        queryWrapper.like(StringUtils.isNotEmpty(courseParamsDto.getCourseName()), CourseBase::getName, courseParamsDto.getCourseName());
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, courseParamsDto.getAuditStatus());

        // TODO 按照课程发布状态查询（业务上不确定，先按照已发布课程查询）
        queryWrapper.eq(CourseBase::getCompanyId, companyId);

        Long pageSize = pageParams.getPageSize();
        Long pageNo = pageParams.getPageNo();
        Page<CourseBase> page = new Page<>(pageNo, pageSize);

        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        List<CourseBase> items = pageResult.getRecords();
        long total = pageResult.getTotal();

        return new PageResult<>(items, total, pageNo, pageSize);
    }

    /**
     * 新增课程
     * @param companyId 所属机构ID
     * @param addCourseDto 具体的课程信息
     * @return 写入结果
     */
    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {

        // 参数合法性校验：课程名称不能为空，
        if(StringUtils.isBlank(addCourseDto.getName())){
            throw new RuntimeException("课程名称为空");
        }

        // 向课程基本信息表course_base写入数据
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto, courseBase);
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        courseBase.setAuditStatus("202002");
        courseBase.setSt("203001");
        int insert = courseBaseMapper.insert(courseBase);
        if(insert <= 0){
            throw new RuntimeException("添加课程失败");
        }

        // 向课程营销表course_market写入数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(addCourseDto, courseMarket);
        Long courseId = courseBase.getId();
        courseMarket.setId(courseId);

        // 保存营销信息
        saveCourseMarket(courseMarket);
        return  getCourseBaseInfo(courseId);
    }

    /**
     * 将课程信息插入到营销信息表中
     * @param courseId 课程id
     * @return 课程信息
     */
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId)
    {
        // 查询课程基本信息数据库中是否有这个课程id对应的课程
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            return null;
        }

        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if(courseMarket != null){
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }

        CourseCategory mtObj = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(mtObj.getName());
        CourseCategory stObj = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(stObj.getName());

        return courseBaseInfoDto;
    }

    /**
     * 修改课程信息
     * @param companyId 机构ID
     * @param editCourseDto 课程信息
     * @return 修改后课程信息
     */
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto)
    {
        // 数据合法校验
        Long courseId = editCourseDto.getId();
        // 根据前端传来的课程的id查询是否存在该课程
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            XueChengPlusException.cast("课程不存在");
        }
        // 判断该课程的所属结构是否属于本机构
        if(!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("课程不属于本机构");
        }

        // 封装课程数据，根据id修改信息
        BeanUtils.copyProperties(editCourseDto, courseBase);
        courseBase.setCreateDate(LocalDateTime.now());
        // 修改数据库信息
        int i = courseBaseMapper.updateById(courseBase);
        if(i <= 0){
            XueChengPlusException.cast("修改课程失败");
        }

        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (courseMarket != null) {
            // 修改：将DTO数据拷贝到已存在的实体对象
            BeanUtils.copyProperties(editCourseDto, courseMarket);
            // 确保courseId不被覆盖（如果DTO中也有courseId字段）
            courseMarket.setId(courseId);
            // 执行更新
            courseMarketMapper.updateById(courseMarket);
        } else {
            // 新增：创建新对象并设置courseId
            courseMarket = new CourseMarket();
            BeanUtils.copyProperties(editCourseDto, courseMarket);
            courseMarket.setId(courseId);
            // 执行插入
            courseMarketMapper.insert(courseMarket);
        }

        return getCourseBaseInfo(courseId);
    }

    /**
     * 保存营销信息到数据库中
     * @param courseMarket 营销信息
     * @return 影响行树
     */
    private int saveCourseMarket(CourseMarket courseMarket)
    {
        String charge = courseMarket.getCharge();
        if(StringUtils.isBlank(charge)){
            throw new RuntimeException("课程收费规则为空");
        }
        if(charge.equals("201001")){
            if(courseMarket.getPrice() == null || courseMarket.getPrice() <= 0){
                throw new RuntimeException("课程价格设置不合理");
            }
        }

        Long id = courseMarket.getId();
        CourseMarket courseMarket1 = courseMarketMapper.selectById(id);
        if(courseMarket1 == null){
            return courseMarketMapper.insert(courseMarket);
        }else{
            BeanUtils.copyProperties(courseMarket, courseMarket1);
            courseMarket1.setId(id);
            return courseMarketMapper.updateById(courseMarket1);
        }
    }
}
