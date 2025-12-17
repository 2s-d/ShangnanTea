package com.shangnantea.model.entity.user;

import lombok.Data;

import java.util.Date;

/**
 * 用户设置实体类
 */
@Data
public class UserSetting {
    /**
     * 设置ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 设置键名
     */
    private String settingKey;
    
    /**
     * 设置值
     */
    private String settingValue;
    
    /**
     * 值类型(string,number,boolean,json)
     */
    private String settingType;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 