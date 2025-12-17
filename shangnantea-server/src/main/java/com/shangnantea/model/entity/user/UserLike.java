package com.shangnantea.model.entity.user;

import lombok.Data;

import java.util.Date;

/**
 * 用户点赞实体类
 */
@Data
public class UserLike {
    /**
     * 点赞ID
     */
    private Integer id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 点赞目标类型(post,reply,article)
     */
    private String targetType;
    
    /**
     * 点赞目标ID
     */
    private String targetId;
    
    /**
     * 创建时间
     */
    private Date createTime;
} 