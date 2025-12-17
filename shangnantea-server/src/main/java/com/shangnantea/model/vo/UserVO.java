package com.shangnantea.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 用户视图对象，用于返回给前端展示
 */
@Data
public class UserVO {
    /**
     * 用户ID
     */
    private String id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码（通常不返回给前端）
     */
    private String password;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 角色（1-管理员，2-普通用户，3-商家）
     */
    private Integer role;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 