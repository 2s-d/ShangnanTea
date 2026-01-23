package com.shangnantea.model.entity.shop;

import lombok.Data;

import java.util.Date;

/**
 * 店铺评价实体类
 */
@Data
public class ShopReview {
    /**
     * 评价ID
     */
    private Integer id;
    
    /**
     * 店铺ID
     */
    private String shopId;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 评分(1-5)
     */
    private Integer rating;
    
    /**
     * 评价内容
     */
    private String content;
    
    /**
     * 评价图片(JSON数组)
     */
    private String images;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}
