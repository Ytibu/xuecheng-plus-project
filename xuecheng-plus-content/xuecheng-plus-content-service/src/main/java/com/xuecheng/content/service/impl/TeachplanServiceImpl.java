package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XuechengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements TeachplanService{
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    /**
     * 课程计划查询
     * @param courseId 课程id
     * @return 查询结果
     */
    @Override
    public List<TeachplanDto> selectTeachplanTree(Long courseId)
    {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    /**
     * 新增/修改/保存课程计划
     * @param saveTeachplanDto 课程计划信息
     */
    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto)
    {
        //课程计划id判断是新增还是保存和修改
        Long teachplanId = saveTeachplanDto.getId();
        if(teachplanId == null){
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);

            Long parentId = saveTeachplanDto.getParentid();
            Long courseId = saveTeachplanDto.getCourseId();
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper = queryWrapper.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, parentId);
            Integer count = teachplanMapper.selectCount(queryWrapper);

            teachplan.setOrderby(count + 1);

            teachplanMapper.insert(teachplan);
        }else{
            Teachplan teachplan = teachplanMapper.selectById(teachplanId);
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }

    /**
     * 删除章节，章存在节则禁止删除，节删除必须删除关联媒资
     * @param id 课程计划章节
     */
    @Override
    @Transactional
    public void deleteTeachplan(Long id)
    {
        // 先查询是否存在，并根据是否为章来判断是否有节点关联，从而抛出异常
        Teachplan teachplan = teachplanMapper.selectById(id);
        Integer grade = teachplan.getGrade();
        if(grade == 1){
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper = queryWrapper.eq(Teachplan::getParentid, id);
            Integer count = teachplanMapper.selectCount(queryWrapper);
            if(count > 0){
                XuechengPlusException.cast("该章存在节点内容，禁止直接删除");
            }
        }

        // 删除章节
        teachplanMapper.deleteById(id);

        // 删除章节关联的媒资
        LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachplanMedia::getTeachplanId, teachplan.getId());
        teachplanMediaMapper.delete(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveUpTeachplan(Long id)
    {
        // 1. 查询并校验当前记录
        Teachplan current = getById(id);
        if (current == null || current.getStatus() == null || current.getStatus() != 1) {
            XuechengPlusException.cast("记录不存在或已删除");
        }

        // 2. 查找同父级下相邻的上一条记录
        Teachplan prev = getOne(new LambdaQueryWrapper<Teachplan>()
                .eq(Teachplan::getCourseId, current.getCourseId())
                .eq(Teachplan::getParentid, current.getParentid())
                .eq(Teachplan::getStatus, 1)
                .lt(Teachplan::getOrderby, current.getOrderby())
                .orderByDesc(Teachplan::getOrderby)
                .last("LIMIT 1"));
        if (prev == null) {
            XuechengPlusException.cast("已经是第一条，无法上移");
        }

        // 3. 与上一条记录交换排序值
        exchangeOrderby(current, prev);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveDownTeachplan(Long id)
    {
        // 1. 查询并校验当前记录
        Teachplan current = getById(id);
        if (current == null || current.getStatus() == null || current.getStatus() != 1) {
            XuechengPlusException.cast("记录不存在或已删除");
        }

        // 2. 查找同父级下相邻的下一条记录
        Teachplan next = getOne(new LambdaQueryWrapper<Teachplan>()
                .eq(Teachplan::getCourseId, current.getCourseId())
                .eq(Teachplan::getParentid, current.getParentid())
                .eq(Teachplan::getStatus, 1)
                .gt(Teachplan::getOrderby, current.getOrderby())
                .orderByAsc(Teachplan::getOrderby)
                .last("LIMIT 1"));
        if (next == null) {
            XuechengPlusException.cast("已经是最后一条，无法下移");
        }

        // 3. 与下一条记录交换排序值
        exchangeOrderby(current, next);
    }

    /**
     * 交换两条记录的orderby排序值。
     * 只更新orderby字段，并把读取到的旧orderby作为更新条件：
     * 一旦记录被并发修改或删除导致更新未生效，立即抛出异常使整个事务回滚，避免产生重复/错乱的排序。
     */
    private void exchangeOrderby(Teachplan current, Teachplan target)
    {
        boolean updateCurrent = update(new LambdaUpdateWrapper<Teachplan>()
                .set(Teachplan::getOrderby, target.getOrderby())
                .eq(Teachplan::getId, current.getId())
                .eq(Teachplan::getOrderby, current.getOrderby()));
        boolean updateTarget = update(new LambdaUpdateWrapper<Teachplan>()
                .set(Teachplan::getOrderby, current.getOrderby())
                .eq(Teachplan::getId, target.getId())
                .eq(Teachplan::getOrderby, target.getOrderby()));
        if (!updateCurrent || !updateTarget) {
            XuechengPlusException.cast("操作失败，数据已被他人修改，请刷新后重试");
        }
    }



}
