package com.shangnantea.model.entity.user;

import lombok.Data;

import java.util.Date;

/**
 * 用户地址实体类
 */
@Data
public class UserAddress {
    /**
     * 地址ID
     */
    private Integer id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 收货人姓名
     */
    private String receiverName;
    
    /**
     * 收货人电话
     */
    private String receiverPhone;
    
    /**
     * 省份
     */
    private String province;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 区/县
     */
    private String district;
    
    /**
     * 详细地址
     */
    private String detailAddress;
    
    /**
     * 是否默认地址（0-非默认，1-默认）
     */
    private Integer isDefault;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 