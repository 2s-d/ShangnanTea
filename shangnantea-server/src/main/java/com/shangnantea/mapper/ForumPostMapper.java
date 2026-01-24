package com.shangnantea.mapper;

import com.shangnantea.model.entity.forum.ForumPost;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 论坛帖子Mapper接口
 */
public interface ForumPostMapper extends BaseMapper<ForumPost, Long> {
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
} 