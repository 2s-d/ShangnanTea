package com.shangnantea.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 提交商家认证数据传输对象
 */
@Data
public class SubmitShopCertificationDTO {
    /**
     * 店铺名称
     */
    @NotBlank(message = "店铺名称不能为空")
    @Size(max = 100, message = "店铺名称长度不能超过100个字符")
    private String shopName;
    
    /**
     * 营业执照图片路径
     */
    @NotBlank(message = "营业执照不能为空")
    private String businessLicense;
    
    /**
     * 身份证正面照片路径
     */
    @NotBlank(message = "身份证正面照片不能为空")
    private String idCardFront;
    
    /**
     * 身份证反面照片路径
     */
    @NotBlank(message = "身份证反面照片不能为空")
    private String idCardBack;
    
    /**
     * 真实姓名
     */
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;
    
    /**
     * 身份证号
     */
    @Size(max = 18, message = "身份证号长度不能超过18个字符")
    private String idCard;
    
    /**
     * 联系电话
     */
    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String contactPhone;
    
    /**
     * 省份
     */
    @Size(max = 50, message = "省份长度不能超过50个字符")
    private String province;
    
    /**
     * 城市
     */
    @Size(max = 50, message = "城市长度不能超过50个字符")
    private String city;
    
    /**
     * 区县
     */
    @Size(max = 50, message = "区县长度不能超过50个字符")
    private String district;
    
    /**
     * 详细地址
     */
    @Size(max = 200, message = "详细地址长度不能超过200个字符")
    private String address;
    
    /**
     * 申请理由
     */
    @Size(max = 500, message = "申请理由长度不能超过500个字符")
    private String applyReason;
}
