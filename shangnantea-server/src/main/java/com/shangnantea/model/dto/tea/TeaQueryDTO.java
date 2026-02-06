package com.shangnantea.model.dto.tea;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 茶叶查询DTO
 */
@Data
public class TeaQueryDTO {
    /**
     * 页码
     */
    private Integer page;
    
    /**
     * 每页大小
     */
    private Integer pageSize;
    
    /**
     * 分类ID
     */
    private Integer categoryId;
    
    /**
     * 关键词（搜索茶叶名称、描述等）
     */
    private String keyword;
    
    /**
     * 店铺ID
     */
    private String shopId;
    
    /**
     * 排序字段
     */
    private String sortBy;
    
    /**
     * 排序顺序（asc/desc）
     */
    private String sortOrder;
    
    /**
     * 最低价格
     */
    private BigDecimal priceMin;
    
    /**
     * 最高价格
     */
    private BigDecimal priceMax;
    
    /**
     * 产地
     */
    private String origin;
    
    /**
     * 最低评分
     */
    private BigDecimal rating;
    
    /**
     * 状态（0-下架，1-上架）
     */
    private Integer status;
}
