package com.shangnantea.model.vo.user;

/**
 * 地址信息值对象，用于前端展示地址信息
 */
public class AddressVO {
    
    /**
     * 地址ID
     */
    private Integer id;
    
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
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getReceiverName() {
        return receiverName;
    }
    
    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }
    
    public String getReceiverPhone() {
        return receiverPhone;
    }
    
    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }
    
    public String getProvince() {
        return province;
    }
    
    public void setProvince(String province) {
        this.province = province;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getDistrict() {
        return district;
    }
    
    public void setDistrict(String district) {
        this.district = district;
    }
    
    public String getDetailAddress() {
        return detailAddress;
    }
    
    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }
    
    public Integer getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }
}
