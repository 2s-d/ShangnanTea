package com.shangnantea.model.entity.shop;

import lombok.Data;

import java.util.Date;

/**
 * 店铺Banner实体类
 */
@Data
public class ShopBanner {
    /**
     * Banner ID
     */
    private Integer id;
    
    /**
     * 店铺ID
     */
    private String shopId;
    
    /**
     * Banner图片URL
     */
    private String imageUrl;
    
    /**
     * 点击跳转链接
     */
    private String linkUrl;
    
    /**
     * Banner标题
     */
    private String title;
    
    /**
     * 排序值
     */
    private Integer sortOrder;
    
    /**
     * 状态:1显示,0隐藏
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}
