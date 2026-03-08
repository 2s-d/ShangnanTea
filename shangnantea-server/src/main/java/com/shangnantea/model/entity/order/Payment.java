package com.shangnantea.model.entity.order;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付单实体类（支付批次）
 * <p>
 * 用于支持“多订单一次支付”：多个订单通过 orders.payment_id 关联到同一条 payments 记录。
 * </p>
 */
@Data
public class Payment {
    /**
     * 支付状态：待支付
     */
    public static final Integer STATUS_PENDING = 0;
    /**
     * 支付状态：支付成功
     */
    public static final Integer STATUS_SUCCESS = 1;
    /**
     * 支付状态：支付失败
     */
    public static final Integer STATUS_FAILED = 2;
    /**
     * 支付状态：已关闭（取消/超时等）
     */
    public static final Integer STATUS_CLOSED = 3;

    /**
     * 支付单号（业务主键，同时作为支付宝 out_trade_no）
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 本次支付总金额（多订单汇总）
     */
    private BigDecimal totalAmount;

    /**
     * 支付状态：0待支付 1成功 2失败 3关闭
     */
    private Integer status;

    /**
     * 支付方式：alipay/wechat 等
     */
    private String paymentMethod;

    /**
     * 本次支付包含的订单数量
     */
    private Integer orderCount;

    /**
     * 第三方交易号（如支付宝 trade_no）
     */
    private String channelTradeNo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 支付成功时间
     */
    private Date successTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}

