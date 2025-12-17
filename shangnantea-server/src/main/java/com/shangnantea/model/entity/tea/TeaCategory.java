package com.shangnantea.model.entity.tea;

import lombok.Data;

import java.util.Date;

/**
 * 茶叶分类实体类
 */
@Data
public class TeaCategory {
    /**
     * 分类ID
     */
    private Integer id;
    
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 父分类ID
     */
    private Integer parentId;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 分类图标
     */
    private String icon;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 