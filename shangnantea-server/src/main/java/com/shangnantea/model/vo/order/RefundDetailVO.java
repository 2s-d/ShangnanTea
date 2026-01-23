package com.shangnantea.model.vo.order;

import lombok.Data;

import java.util.Date;

/**
 * 退款详情VO
 */
@Data
public class RefundDetailVO {
    /**
     * 订单ID
     */
    private String orderId;
    
    /**
     * 退款状态:0无退款申请,1申请中,2已同意,3已拒绝
     */
    private Integer refundStatus;
    
    /**
     * 用户退款原因
     */
    private String refundReason;
    
    /**
     * 商家/管理员拒绝原因
     */
    private String refundRejectReason;
    
    /**
     * 退款申请时间
     */
    private Date refundApplyTime;
    
    /**
     * 退款处理时间
     */
    private Date refundProcessTime;
    
    /**
     * 订单总金额
     */
    private java.math.BigDecimal totalAmount;
    
    /**
     * 订单状态
     */
    private Integer orderStatus;
}
