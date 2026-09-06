package com.xuecheng.content.api;

import com.xuecheng.base.exception.XuechengPlusException;
import com.xuecheng.base.model.Result;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(value = "课程计划编辑接口", tags = "课程计划编辑接口")
@RestController
public class TeachplanController {

    @Autowired
    private TeachplanService teachplanService;

    @ApiOperation("查询课程计划信息")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public Result<List<TeachplanDto>> getTreeNodes(@PathVariable Long courseId)
    {
        log.warn("查询课程：{}的课程计划", courseId);
        return Result.success(teachplanService.selectTeachplanTree(courseId));
    }

    @ApiOperation("修改课程计划信息")
    @PostMapping("/teachplan")
    public Result<Void> saveTeachPlan(@RequestBody SaveTeachplanDto saveTeachplanDto)
    {
        log.warn("修改课程计划为：{}", saveTeachplanDto);
        teachplanService.saveTeachplan(saveTeachplanDto);
        return Result.success();
    }

    @ApiOperation("删除指定的章节")
    @DeleteMapping("/teachplan/{teachplanId}")
    public Result<Void> deleteTeachPlan(@PathVariable Long teachplanId)
    {
        log.warn("删除的章节为：{}", teachplanId);
        teachplanService.deleteTeachplan(teachplanId);
        return Result.success();
    }


    @ApiOperation("向上移动章节顺序")
    @PostMapping("/teachplan/moveup/{id}")
    public Result<Void> moveUpTeachplan( @PathVariable Long id)
    {
        teachplanService.moveUpTeachplan(id);
        return Result.success();
    }

    @ApiOperation("向下移动章节顺序")
    @PostMapping("/teachplan/movedown/{id}")
    public Result<Void> moveDownTeachplan(@PathVariable Long id)
    {
        teachplanService.moveDownTeachplan(id);
        return Result.success();
    }


}
