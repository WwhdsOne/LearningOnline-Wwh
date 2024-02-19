package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {
    /**
     * 根据shardIndex查询媒资处理列表
     *
     * @param sharedTotal 分片总数
     * @param shardIndex  分片下标
     * @param failCount   失败次数
     * @return 媒资处理列表
     */
    @Select("select * from media_process m " +
            "where (m.status = 1 or m.status = 3) " +
            "and m.fail_count < 3 " +
            "and m.id % #{sharedTotal} = #{shardIndex} limit #{processors}")
    List<MediaProcess> selectProcessByShardIndex(@Param("sharedTotal") int sharedTotal,
                                                 @Param("shardIndex") int shardIndex,
                                                 @Param("processors") int processors);

    /**
     * 开启一个任务
     *
     * @param id 任务id
     * @return 更新记录数
     */
    @Update("update media_process m set m.status='4' where (m.status='1' or m.status='3') and m.fail_count<3 and m.id=#{id}")
    int startTask(@Param("id") long id);


}
