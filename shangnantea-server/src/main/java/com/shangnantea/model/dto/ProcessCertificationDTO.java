package com.shangnantea.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 审核认证申请数据传输对象
 */
@Data
public class ProcessCertificationDTO {
    /**
     * 审核状态(1-通过, 2-拒绝)
     */
    @NotNull(message = "审核状态不能为空")
    private Integer status;
    
    /**
     * 审核意见/拒绝原因
     */
    @Size(max = 500, message = "审核意见长度不能超过500个字符")
    private String message;
}
