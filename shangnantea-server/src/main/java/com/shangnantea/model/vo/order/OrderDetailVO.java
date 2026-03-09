package com.shangnantea.model.vo.order;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * 订单详情VO
 */
@Data
public class OrderDetailVO {
    /**
     * 订单ID
     */
    private String id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 订单状态(0待付款,1待发货,2待收货,3已完成,4已取消,5退款中,6已退款)
     */
    private Integer status;
    
    /**
     * 订单总金额
     */
    private BigDecimal totalPrice;
    
    /**
     * 茶叶ID
     */
    private String teaId;
    
    /**
     * 茶叶名称
     */
    private String teaName;
    
    /**
     * 茶叶图片
     */
    private String teaImage;
    
    /**
     * 规格ID
     */
    private Integer specId;
    
    /**
     * 规格名称
     */
    private String specName;
    
    /**
     * 购买数量
     */
    private Integer quantity;
    
    /**
     * 单价
     */
    private BigDecimal price;
    
    /**
     * 收货地址信息
     */
    private Map<String, Object> address;
    
    /**
     * 支付方式
     */
    private String paymentMethod;
    
    /**
     * 支付时间
     */
    private Date paymentTime;
    
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
     * 买家留言
     */
    private String buyerMessage;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 店铺ID
     */
    private String shopId;
    
    /**
     * 是否已评价(0未评价,1已评价)
     */
    private Integer isReviewed;
    
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
     * 支付单号（支付批次ID）
     */
    private String paymentId;
    
    /**
     * 支付单状态：0待支付 1成功 2失败 3关闭
     */
    private Integer paymentStatus;
    
    /**
     * 取消时间
     */
    private Date cancelTime;
    
    /**
     * 取消原因
     */
    private String cancelReason;
}
