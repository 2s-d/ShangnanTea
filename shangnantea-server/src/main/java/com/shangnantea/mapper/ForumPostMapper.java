package com.shangnantea.mapper;

import com.shangnantea.model.entity.forum.ForumPost;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 论坛帖子Mapper接口
 */
public interface ForumPostMapper extends BaseMapper<ForumPost, Long> {
    /**
     * 查询正常状态的帖子（支持版块、关键词和用户ID过滤）
     *
     * @param topicId 版块ID（可选）
     * @param keyword 关键词（可选）
     * @param userId 用户ID（可选）
     * @param status 状态（1=正常）
     * @return 帖子列表
     */
    List<ForumPost> selectPublishedPosts(@Param("topicId") Integer topicId, 
                                         @Param("keyword") String keyword,
                                         @Param("userId") String userId,
                                         @Param("status") Integer status);
    
    /**
     * 查询待审核的帖子
     *
     * @param status 状态（0=待审核）
     * @return 帖子列表
     */
    List<ForumPost> selectByStatus(@Param("status") Integer status);
    
    /**
     * 统计指定版块今日发布的帖子数
     *
     * @param topicId 版块ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param status 状态（1=已发布）
     * @return 帖子数量
     */
    int countTodayPosts(@Param("topicId") Integer topicId,
                        @Param("startTime") Date startTime,
                        @Param("endTime") Date endTime,
                        @Param("status") Integer status);

    /**
     * 根据用户ID查询帖子列表（分页）
     *
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @param sortBy 排序方式（latest-最新，hot-最热）
     * @return 帖子列表
     */
    List<ForumPost> selectByUserId(@Param("userId") String userId, 
                                    @Param("offset") Integer offset, 
                                    @Param("limit") Integer limit,
                                    @Param("sortBy") String sortBy);
    
    /**
     * 统计用户发布的帖子数量
     *
     * @param userId 用户ID
     * @return 帖子数量
     */
    long countByUserId(@Param("userId") String userId);
    
    /**
     * 统计指定版块的帖子数量（仅统计已发布的帖子，status=1）
     *
     * @param topicId 版块ID
     * @return 帖子数量
     */
    long countByTopicId(@Param("topicId") Integer topicId);
    
    /**
     * 批量统计所有版块的帖子数量（仅统计已发布的帖子，status=1）
     * 返回List，每个元素是包含topicId和postCount的Map
     *
     * @return 版块统计列表，每个Map包含topicId和postCount
     */
    List<java.util.Map<String, Object>> countByAllTopics();
} 