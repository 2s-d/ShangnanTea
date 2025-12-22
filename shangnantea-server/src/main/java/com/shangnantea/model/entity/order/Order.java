package com.shangnantea.model.entity.order;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单实体类
 */
@Data
public class Order {
    /**
     * 订单状态常量
     */
    public static final Integer STATUS_PENDING_PAYMENT = 0;  // 待付款
    public static final Integer STATUS_PENDING_SHIPMENT = 1; // 待发货
    public static final Integer STATUS_PENDING_RECEIPT = 2;  // 待收货
    public static final Integer STATUS_COMPLETED = 3;        // 已完成
    public static final Integer STATUS_CANCELLED = 4;        // 已取消
    public static final Integer STATUS_REFUNDED = 5;         // 已退款

    /**
     * 订单编号
     */
    private String id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 店铺ID
     */
    private String shopId;
    
    /**
     * 茶叶ID
     */
    private String teaId;
    
    /**
     * 茶叶名称
     */
    private String teaName;
    
    /**
     * 规格ID
     */
    private Integer specId;
    
    /**
     * 规格名称
     */
    private String specName;
    
    /**
     * 茶叶图片
     */
    private String teaImage;
    
    /**
     * 购买数量
     */
    private Integer quantity;
    
    /**
     * 单价
     */
    private BigDecimal price;
    
    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 收货地址ID
     */
    private Integer addressId;
    
    /**
     * 订单状态(0待付款,1待发货,2待收货,3已完成,4已取消,5已退款)
     */
    private Integer status;
    
    /**
     * 支付时间
     */
    private Date paymentTime;
    
    /**
     * 支付方式
     */
    private String paymentMethod;
    
    /**
     * 物流公司
     */
    private String logisticsCompany;
    
    /**
     * 物流单号
     */
    private String logisticsNumber;
    
    /**
     * 发货时间
     */
    private Date shippingTime;
    
    /**
     * 完成时间
     */
    private Date completionTime;
    
    /**
     * 取消时间
     */
    private Date cancelTime;
    
    /**
     * 取消原因
     */
    private String cancelReason;
    
    /**
     * 买家留言
     */
    private String buyerMessage;
    
    /**
     * 买家是否已评价
     */
    private Integer buyerRate;
    
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
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 获取订单状态文本描述
     */
    public String getStatusText() {
        if (status == null) return "未知状态";
        
        switch(status) {
            case 0: return "待付款";
            case 1: return "待发货";
            case 2: return "待收货";
            case 3: return "已完成";
            case 4: return "已取消";
            case 5: return "已退款";
            default: return "未知状态";
        }
    }
} 