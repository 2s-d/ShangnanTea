package com.shangnantea.mapper;

import com.shangnantea.model.entity.forum.ForumTopic;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
    List<ForumTopic> selectByIds(@Param("ids") List<Integer> ids);
    
    /**
     * 查询启用的版块列表（按排序值升序）
     *
     * @param status 状态（1=启用）
     * @return 版块列表
     */
    List<ForumTopic> selectByStatus(@Param("status") Integer status);
    
    /**
     * 根据名称查询版块（用于检查重复）
     *
     * @param name 版块名称
     * @return 版块对象，不存在返回null
     */
    ForumTopic selectByName(@Param("name") String name);
    
    /**
     * 根据名称查询版块（排除指定ID）
     *
     * @param name 版块名称
     * @param excludeId 要排除的版块ID
     * @return 版块对象，不存在返回null
     */
    ForumTopic selectByNameExcludeId(@Param("name") String name, @Param("excludeId") Integer excludeId);
} 