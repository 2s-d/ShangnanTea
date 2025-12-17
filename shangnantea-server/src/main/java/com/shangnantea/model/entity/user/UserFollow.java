package com.shangnantea.model.entity.user;

import lombok.Data;

import java.util.Date;

/**
 * 用户关注实体类
 */
@Data
public class UserFollow {
    /**
     * 关注ID
     */
    private Integer id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 关注类型(shop, user)
     */
    private String followType;
    
    /**
     * 被关注对象ID
     */
    private String followId;
    
    /**
     * 创建时间
     */
    private Date createTime;
} 