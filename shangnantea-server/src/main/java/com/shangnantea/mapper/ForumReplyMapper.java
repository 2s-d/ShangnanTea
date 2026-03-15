package com.shangnantea.mapper;

import com.shangnantea.model.entity.forum.ForumReply;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 论坛回复Mapper接口
 */
public interface ForumReplyMapper extends BaseMapper<ForumReply, Long> {
    /**
     * 根据帖子ID查询回复列表
     * @param postId 帖子ID
     * @return 回复列表
     */
    List<ForumReply> selectByPostId(@Param("postId") Long postId);
    
    /**
     * 根据用户ID查询该用户发布的所有评论
     * @param userId 用户ID
     * @return 评论列表
     */
    List<ForumReply> selectByUserId(@Param("userId") String userId);
}
