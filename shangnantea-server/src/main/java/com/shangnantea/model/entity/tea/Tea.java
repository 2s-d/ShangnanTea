package com.shangnantea.model.entity.tea;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 茶叶实体类
 */
@Data
public class Tea {
    /**
     * 茶叶ID
     */
    private String id;
    
    /**
     * 茶叶名称
     */
    private String name;
    
    /**
     * 所属商家ID
     */
    private String shopId;
    
    /**
     * 分类ID
     */
    private Integer categoryId;
    
    /**
     * 基础价格
     */
    private BigDecimal price;
    
    /**
     * 茶叶描述
     */
    private String description;
    
    /**
     * 详细介绍
     */
    private String detail;
    
    /**
     * 产地
     */
    private String origin;
    
    /**
     * 总库存
     */
    private Integer stock;
    
    /**
     * 销量
     */
    private Integer sales;
    
    /**
     * 主图
     */
    private String mainImage;
    
    /**
     * 状态(0下架,1上架)
     */
    private Integer status;
    
    /**
     * 是否删除
     */
    private Integer isDeleted;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 