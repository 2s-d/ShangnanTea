package com.shangnantea.model.entity.shop;

import lombok.Data;

import java.util.Date;

/**
 * 商家认证申请实体类
 */
@Data
public class ShopCertification {
    /**
     * 认证ID
     */
    private Integer id;
    
    /**
     * 申请用户ID
     */
    private String userId;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 身份证号
     */
    private String idCard;
    
    /**
     * 身份证正面照
     */
    private String idCardFront;
    
    /**
     * 身份证背面照
     */
    private String idCardBack;
    
    /**
     * 营业执照
     */
    private String businessLicense;
    
    /**
     * 申请店铺名称
     */
    private String shopName;
    
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
     * 申请理由
     */
    private String applyReason;
    
    /**
     * 状态(0待审核,1已通过,2已拒绝)
     */
    private Integer status;
    
    /**
     * 审核管理员ID
     */
    private String adminId;
    
    /**
     * 拒绝原因
     */
    private String rejectReason;
    
    /**
     * 关联的系统通知ID
     */
    private Integer notificationId;
    
    /**
     * 用户是否已确认通知
     */
    private Integer notificationConfirmed;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 