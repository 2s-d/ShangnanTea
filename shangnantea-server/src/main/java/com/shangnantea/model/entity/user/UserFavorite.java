package com.shangnantea.model.entity.user;

import lombok.Data;

import java.util.Date;

/**
 * 用户收藏实体类
 */
@Data
public class UserFavorite {
    /**
     * 收藏ID
     */
    private Integer id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 收藏项类型(tea, post, tea_article)
     */
    private String itemType;
    
    /**
     * 收藏项ID
     */
    private String itemId;
    
    /**
     * 创建时间
     */
    private Date createTime;
} 