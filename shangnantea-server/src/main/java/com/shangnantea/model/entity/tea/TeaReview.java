package com.shangnantea.model.entity.tea;

import lombok.Data;

import java.util.Date;

/**
 * 茶叶评价实体类
 */
@Data
public class TeaReview {
    /**
     * 评价ID
     */
    private Integer id;
    
    /**
     * 茶叶ID
     */
    private String teaId;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 订单ID
     */
    private String orderId;
    
    /**
     * 评价内容
     */
    private String content;
    
    /**
     * 评分(1-5)
     */
    private Integer rating;
    
    /**
     * 评价图片,JSON格式
     */
    private String images;
    
    /**
     * 商家回复
     */
    private String reply;
    
    /**
     * 回复时间
     */
    private Date replyTime;
    
    /**
     * 是否匿名
     */
    private Integer isAnonymous;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 