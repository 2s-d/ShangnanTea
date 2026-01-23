package com.shangnantea.model.vo.shop;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 店铺评价 VO
 */
@Data
public class ShopReviewVO {
    /**
     * 评价ID
     */
    private Integer id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户头像
     */
    private String avatar;
    
    /**
     * 评分(1-5)
     */
    private Integer rating;
    
    /**
     * 评价内容
     */
    private String content;
    
    /**
     * 评价图片列表
     */
    private List<String> images;
    
    /**
     * 创建时间
     */
    private Date createTime;
}
