package com.shangnantea.mapper;

import com.shangnantea.model.entity.forum.ForumPost;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 论坛帖子Mapper接口
 */
public interface ForumPostMapper extends BaseMapper<ForumPost, Long> {
    /**
     * 查询正常状态的帖子（支持版块和关键词过滤）
     * @param topicId 版块ID（可选）
     * @param keyword 关键词（可选）
     * @param status 状态（1=正常）
     * @return 帖子列表
     */
    List<ForumPost> selectPublishedPosts(@Param("topicId") Integer topicId, 
                                         @Param("keyword") String keyword, 
                                         @Param("status") Integer status);
    
    /**
     * 查询待审核的帖子
     * @param status 状态（0=待审核）
     * @return 帖子列表
     */
    List<ForumPost> selectByStatus(@Param("status") Integer status);
} 