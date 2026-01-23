package com.shangnantea.model.vo.message;

import lombok.Data;

/**
 * 用户统计数据VO
 */
@Data
public class UserStatisticsVO {
    /**
     * 帖子数
     */
    private Integer postCount;
    
    /**
     * 关注数
     */
    private Integer followingCount;
    
    /**
     * 粉丝数
     */
    private Integer followerCount;
    
    /**
     * 收藏数
     */
    private Integer favoriteCount;
}
