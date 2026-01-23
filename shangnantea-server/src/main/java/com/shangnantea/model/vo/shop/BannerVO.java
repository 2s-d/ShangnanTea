package com.shangnantea.model.vo.shop;

import lombok.Data;

/**
 * 店铺Banner VO
 */
@Data
public class BannerVO {
    /**
     * Banner ID
     */
    private Integer id;
    
    /**
     * Banner图片URL
     */
    private String imageUrl;
    
    /**
     * Banner标题
     */
    private String title;
    
    /**
     * 点击跳转链接
     */
    private String linkUrl;
    
    /**
     * 排序值
     */
    private Integer sortOrder;
}
