package com.shangnantea.model.entity.shop;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 店铺实体类
 */
@Data
public class Shop {
    /**
     * 店铺ID
     */
    private String id;
    
    /**
     * 店主用户ID
     */
    private String ownerId;
    
    /**
     * 店铺名称
     */
    private String shopName;
    
    /**
     * 店铺logo
     */
    private String logo;
    
    /**
     * 店铺banner
     */
    private String banner;
    
    /**
     * 店铺简介
     */
    private String description;
    
    /**
     * 店铺公告
     */
    private String announcement;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * 省份
     */
    private String province;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 区县
     */
    private String district;
    
    /**
     * 详细地址
     */
    private String address;
    
    /**
     * 营业执照图片
     */
    private String businessLicense;
    
    /**
     * 状态（1-正常，0-关闭）
     */
    private Integer status;
    
    /**
     * 平均评分
     */
    private BigDecimal rating;
    
    /**
     * 评分数量
     */
    private Integer ratingCount;
    
    /**
     * 总销量
     */
    private Integer salesCount;
    
    /**
     * 关注数/粉丝数
     */
    private Integer followCount;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 