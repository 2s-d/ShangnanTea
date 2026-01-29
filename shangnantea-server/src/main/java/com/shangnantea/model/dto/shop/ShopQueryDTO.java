package com.shangnantea.model.dto.shop;

import lombok.Data;

/**
 * 店铺查询DTO
 * 用于接收店铺列表查询参数
 */
@Data
public class ShopQueryDTO {
    
    /**
     * 当前页码，从1开始
     */
    private Integer page = 1;
    
    /**
     * 每页记录数
     */
    private Integer size = 10;
    
    /**
     * 搜索关键词（店铺名称、描述等）
     */
    private String keyword;
    
    /**
     * 最低评分筛选
     */
    private Double rating;
    
    /**
     * 排序字段（rating, salesCount, followCount）
     */
    private String sortBy;
    
    /**
     * 排序方向（asc, desc），默认为desc
     */
    private String sortOrder = "desc";
}
