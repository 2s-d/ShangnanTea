package com.shangnantea.utils;

import com.shangnantea.mapper.UserFavoriteMapper;
import com.shangnantea.mapper.UserFollowMapper;
import com.shangnantea.mapper.UserLikeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 统计数据工具类
 * 用于动态计算点赞数、收藏数、关注数等统计数据
 * 
 * @author ShangnanTea
 * @since 2026-01-29
 */
@Component
public class StatisticsUtils {
    
    @Autowired
    private UserLikeMapper userLikeMapper;
    
    @Autowired
    private UserFavoriteMapper userFavoriteMapper;
    
    @Autowired
    private UserFollowMapper userFollowMapper;
    
    /**
     * 获取点赞数
     * 
     * @param targetType 目标类型 (post/reply/review/article等)
     * @param targetId 目标ID
     * @return 点赞数，如果查询失败返回0
     */
    public Integer getLikeCount(String targetType, String targetId) {
        if (targetType == null || targetId == null) {
            return 0;
        }
        try {
            Integer count = userLikeMapper.countByTarget(targetType, targetId);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * 获取收藏数
     * 
     * @param itemType 收藏项类型 (post/article等)
     * @param itemId 收藏项ID
     * @return 收藏数，如果查询失败返回0
     */
    public Integer getFavoriteCount(String itemType, String itemId) {
        if (itemType == null || itemId == null) {
            return 0;
        }
        try {
            Integer count = userFavoriteMapper.countByItem(itemType, itemId);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * 获取关注数/粉丝数
     * 
     * @param followType 关注类型 (user/shop等)
     * @param followId 被关注对象ID
     * @return 关注数，如果查询失败返回0
     */
    public Integer getFollowCount(String followType, String followId) {
        if (followType == null || followId == null) {
            return 0;
        }
        try {
            Integer count = userFollowMapper.countByFollow(followType, followId);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * 批量获取帖子的点赞数和收藏数
     * 
     * @param postId 帖子ID
     * @return 数组 [点赞数, 收藏数]
     */
    public Integer[] getPostCounts(String postId) {
        Integer likeCount = getLikeCount("post", postId);
        Integer favoriteCount = getFavoriteCount("post", postId);
        return new Integer[]{likeCount, favoriteCount};
    }
    
    /**
     * 批量获取文章的点赞数和收藏数
     * 
     * @param articleId 文章ID
     * @return 数组 [点赞数, 收藏数]
     */
    public Integer[] getArticleCounts(String articleId) {
        Integer likeCount = getLikeCount("article", articleId);
        Integer favoriteCount = getFavoriteCount("article", articleId);
        return new Integer[]{likeCount, favoriteCount};
    }
}
