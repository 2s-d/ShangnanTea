package com.shangnantea.model.vo.forum;

import lombok.Data;

/**
 * 回复视图对象
 */
@Data
public class ReplyVO {
    /**
     * 回复ID
     */
    private String id;
    
    /**
     * 帖子ID
     */
    private String postId;
    
    /**
     * 回复用户ID
     */
    private String userId;
    
    /**
     * 回复用户名
     */
    private String username;
    
    /**
     * 回复用户头像
     */
    private String avatar;
    
    /**
     * 回复内容
     */
    private String content;
    
    /**
     * 父回复ID
     */
    private String parentId;
    
    /**
     * 回复目标用户ID
     */
    private String toUserId;
    
    /**
     * 回复目标用户名
     */
    private String toUsername;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;
    
    /**
     * 创建时间
     */
    private String createTime;
}
