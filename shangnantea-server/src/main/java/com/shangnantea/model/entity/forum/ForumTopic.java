package com.shangnantea.model.entity.forum;

import lombok.Data;

import java.util.Date;

/**
 * 论坛版块实体类
 */
@Data
public class ForumTopic {
    /**
     * 版块ID
     */
    private Integer id;
    
    /**
     * 版块名称
     */
    private String name;
    
    /**
     * 版块描述
     */
    private String description;
    
    /**
     * 版块图标
     */
    private String icon;
    
    /**
     * 版块封面
     */
    private String cover;
    
    /**
     * 版主用户ID
     */
    private String userId;
    
    /**
     * 排序值
     */
    private Integer sortOrder;
    
    /**
     * 帖子数量
     */
    private Integer postCount;
    
    /**
     * 状态(1启用,0禁用)
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