package com.shangnantea.model.vo;

import lombok.Data;

/**
 * 茶叶分类VO
 */
@Data
public class CategoryVO {
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
}
