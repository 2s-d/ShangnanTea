package com.shangnantea.model.vo.user;

import lombok.Data;

/**
 * 验证码响应VO
 * 用于在开发/测试环境下返回验证码
 */
@Data
public class VerificationCodeVO {
    /**
     * 验证码（仅在开发/测试环境下返回）
     */
    private String code;
    
    /**
     * 提示信息
     */
    private String message;
    
    public VerificationCodeVO(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public VerificationCodeVO(String code) {
        this.code = code;
        this.message = "验证码已发送（开发环境）";
    }
}
