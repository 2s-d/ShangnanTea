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
     * 收藏目标名称(冗余字段，优化列表展示)
     */
    private String targetName;
    
    /**
     * 收藏目标图片(冗余字段，优化列表展示)
     */
    private String targetImage;
    
    /**
     * 创建时间
     */
    private Date createTime;
} 