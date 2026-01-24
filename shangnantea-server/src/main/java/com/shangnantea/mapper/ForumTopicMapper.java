package com.shangnantea.mapper;

import com.shangnantea.model.entity.forum.ForumTopic;
import org.apache.ibatis.annotations.Param;

/**
 * 论坛主题Mapper接口
 */
public interface ForumTopicMapper extends BaseMapper<ForumTopic, Integer> {
    
    /**
     * 批量查询版块（通过ID列表）
     *
     * @param ids 版块ID列表
     * @return 版块列表
     */
    java.util.List<ForumTopic> selectByIds(@Param("ids") java.util.List<Integer> ids);
} 