package com.shangnantea.model.vo.message;

import lombok.Data;

/**
 * 用户主页信息VO
 */
@Data
public class UserProfileVO {
    /**
     * 用户信息
     */
    private UserInfo user;
    
    /**
     * 是否已关注
     */
    private Boolean isFollowed;
    
    /**
     * 统计数据
     */
    private Statistics statistics;
    
    /**
     * 用户基本信息
     */
    @Data
    public static class UserInfo {
        /**
         * 用户ID
         */
        private String id;
        
        /**
         * 用户名
         */
        private String username;
        
        /**
         * 昵称
         */
        private String nickname;
        
        /**
         * 头像
         */
        private String avatar;
        
        /**
         * 角色（1管理员，2普通用户）
         */
        private Integer role;
    }
    
    /**
     * 统计数据
     */
    @Data
    public static class Statistics {
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
    }
}
