package com.shangnantea.model.vo.user;

import java.util.Date;

/**
 * 商家认证状态值对象，用于前端展示认证信息
 */
public class CertificationStatusVO {
    
    /**
     * 认证ID
     */
    private Integer id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 申请店铺名称
     */
    private String shopName;
    
    /**
     * 营业执照
     */
    private String businessLicense;
    
    /**
     * 身份证正面照
     */
    private String idCardFront;
    
    /**
     * 身份证背面照
     */
    private String idCardBack;
    
    /**
     * 状态(0待审核,1已通过,2已拒绝)
     */
    private Integer status;
    
    /**
     * 拒绝原因
     */
    private String rejectReason;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getShopName() {
        return shopName;
    }
    
    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
    
    public String getBusinessLicense() {
        return businessLicense;
    }
    
    public void setBusinessLicense(String businessLicense) {
        this.businessLicense = businessLicense;
    }
    
    public String getIdCardFront() {
        return idCardFront;
    }
    
    public void setIdCardFront(String idCardFront) {
        this.idCardFront = idCardFront;
    }
    
    public String getIdCardBack() {
        return idCardBack;
    }
    
    public void setIdCardBack(String idCardBack) {
        this.idCardBack = idCardBack;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getRejectReason() {
        return rejectReason;
    }
    
    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    public Date getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
