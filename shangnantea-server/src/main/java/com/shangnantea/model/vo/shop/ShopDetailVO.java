package com.shangnantea.model.vo.shop;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 店铺详情视图对象
 * 用于返回给前端展示店铺详细信息
 */
@Data
public class ShopDetailVO {
    
    /**
     * 店铺ID
     */
    private String id;
    
    /**
     * 店铺名称
     */
    private String name;
    
    /**
     * 店铺Logo URL
     */
    private String logo;
    
    /**
     * 店铺描述
     */
    private String description;
    
    /**
     * 平均评分
     */
    private BigDecimal rating;
    
    /**
     * 销量
     */
    private Integer salesCount;
    
    /**
     * 关注数/粉丝数
     */
    private Integer followCount;
    
    /**
     * 店主用户ID
     */
    private String ownerId;
}
