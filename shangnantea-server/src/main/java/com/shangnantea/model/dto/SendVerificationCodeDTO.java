package com.shangnantea.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 发送验证码数据传输对象
 */
@Data
public class SendVerificationCodeDTO {
    /**
     * 联系方式（手机号或邮箱）
     */
    @NotBlank(message = "联系方式不能为空")
    private String contact;
    
    /**
     * 联系方式类型（phone/email）
     */
    @NotBlank(message = "联系方式类型不能为空")
    @Pattern(regexp = "^(phone|email)$", message = "联系方式类型只能是phone或email")
    private String contactType;
    
    /**
     * 使用场景（register/reset_password/change_phone）
     */
    @NotBlank(message = "使用场景不能为空")
    @Pattern(regexp = "^(register|reset_password|change_phone)$", message = "使用场景只能是register、reset_password或change_phone")
    private String sceneType;
}
