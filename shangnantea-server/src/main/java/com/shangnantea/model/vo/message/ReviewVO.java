package com.shangnantea.model.vo.message;

import lombok.Data;

import java.util.Date;

/**
 * 评价VO
 */
@Data
public class ReviewVO {
    /**
     * 评价ID
     */
    private Long id;
    
    /**
     * 订单ID
     */
    private String orderId;
    
    /**
     * 评分（1-5星）
     */
    private Integer rating;
    
    /**
     * 评价内容
     */
    private String content;
    
    /**
     * 创建时间
     */
    private Date createTime;
}
