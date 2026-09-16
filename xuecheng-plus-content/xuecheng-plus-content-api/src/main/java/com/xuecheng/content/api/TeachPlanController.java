package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(value = "课程计划编辑接口", tags = "课程计划编辑接口")
@RestController
public class TeachPlanController {

    @Autowired
    private TeachPlanService teachplanService;

    @ApiOperation("查询课程计划信息")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId)
    {
        log.warn("查询课程courseId：{}的课程计划", courseId);
        return teachplanService.selectTeachplanTree(courseId);
    }

    @ApiOperation("修改课程计划信息")
    @PostMapping("/teachplan")
    public void saveTeachPlan(@RequestBody SaveTeachplanDto saveTeachplanDto)
    {
        log.warn("修改课程计划为：{}", saveTeachplanDto);
        teachplanService.saveTeachplan(saveTeachplanDto);
    }

    @ApiOperation("删除指定的章节")
    @DeleteMapping("/teachplan/{teachPlanId}")
    public void deleteTeachPlan(@PathVariable Long teachPlanId)
    {
        log.warn("删除的章节为：{}", teachPlanId);
        teachplanService.deleteTeachplan(teachPlanId);
    }

    @ApiOperation("向上移动章节顺序")
    @PostMapping("/teachplan/moveup/{id}")
    public void moveUpTeachplan( @PathVariable Long id)
    {
        log.warn("向上移动章节:id={}", id);
        teachplanService.moveUpTeachplan(id);
    }

    @ApiOperation("向下移动章节顺序")
    @PostMapping("/teachplan/movedown/{id}")
    public void moveDownTeachplan(@PathVariable Long id)
    {
        log.warn("向下移动章节:id={}", id);
        teachplanService.moveDownTeachplan(id);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto)
    {
        log.warn("绑定信息：bindTeachplanMediaDto={}", bindTeachplanMediaDto);
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

}
