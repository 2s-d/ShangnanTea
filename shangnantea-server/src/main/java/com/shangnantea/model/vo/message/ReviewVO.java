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
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户头像
     */
    private String avatar;
    
    /**
     * 评价图片列表
     */
    private java.util.List<String> images;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 创建时间
     */
    private Date createTime;
}
