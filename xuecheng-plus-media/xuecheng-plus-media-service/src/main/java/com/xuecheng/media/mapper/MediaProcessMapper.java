package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    /**
     * 查询所有待处理的任务
     * @param shardTotal
     * @param shardIndex
     * @param count
     * @return
     */
    @Select("select * from media_process t where t.id % #{shardTotal} = #{shardIndex} and (t.status = 1 or t.status = 3) and t.fail_count < 3 limit #{count}")
    List<MediaProcess> selectListByShardIndex(@Param("shardTotal") int shardTotal, @Param("shardIndex") int shardIndex, @Param("count") int count);

    /**
     * 取任务执行
     * @param id 任务ID
     * @return 结果
     */
    @Update("update media_process m set m.status = '4' where (m.status='1' or m.status='3') and m.fail_count < 3 and m.id=#{id}")
    int startTask(@Param("id") Long id);
}
