package com.shangnantea.model.entity.order;

import lombok.Data;

import java.util.Date;

/**
 * 购物车实体类
 */
@Data
public class ShoppingCart {
    /**
     * 购物车ID
     */
    private Integer id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 茶叶ID
     */
    private String teaId;
    
    /**
     * 规格ID
     */
    private Integer specId;
    
    /**
     * 数量
     */
    private Integer quantity;
    
    /**
     * 是否选中
     */
    private Integer selected;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 