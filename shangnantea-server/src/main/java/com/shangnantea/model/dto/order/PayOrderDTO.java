package com.shangnantea.model.dto.order;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 支付订单DTO
 */
@Data
public class PayOrderDTO {
    /**
     * 订单ID
     */
    @NotBlank(message = "订单ID不能为空")
    private String orderId;
    
    /**
     * 支付方式: alipay, wechat, balance
     */
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;
}
