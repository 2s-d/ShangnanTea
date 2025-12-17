package com.shangnantea.model.entity.user;

import lombok.Data;

import java.util.Date;

/**
 * 用户实体类
 */
@Data
public class User {
    /**
     * 用户ID
     */
    private String id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 昵称
     */
    private String nickname;
    
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
     * 状态（1-正常，0-禁用，2-注销）
     */
    private Integer status;
    
    /**
     * 是否删除（0-未删除，1-已删除）
     */
    private Integer isDeleted;
    
    /**
     * 性别（1-男，2-女，0-未知）
     */
    private Integer gender;
    
    /**
     * 生日
     */
    private Date birthday;
    
    /**
     * 个人简介
     */
    private String bio;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 