package com.shangnantea.model.entity.forum;

import lombok.Data;

import java.util.Date;

/**
 * 论坛回复实体类
 */
@Data
public class ForumReply {
    /**
     * 回复ID
     */
    private Long id;
    
    /**
     * 帖子ID
     */
    private Long postId;
    
    /**
     * 回复用户ID
     */
    private String userId;
    
    /**
     * 回复内容
     */
    private String content;
    
    /**
     * 父回复ID，为空表示一级回复
     */
    private Long parentId;
    
    /**
     * 回复目标用户ID
     */
    private String toUserId;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 状态(1正常,0待审核,2已删除)
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 