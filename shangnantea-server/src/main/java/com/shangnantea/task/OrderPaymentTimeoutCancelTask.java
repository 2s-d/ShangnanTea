package com.shangnantea.task;

import com.shangnantea.mapper.OrderMapper;
import com.shangnantea.mapper.PaymentMapper;
import com.shangnantea.model.entity.order.Order;
import com.shangnantea.model.entity.order.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 未支付超时自动取消任务
 * <p>
 * 规则（基于现有后端支付单模型 payments）：
 * - 支付单创建后进入待支付（status=0），支付成功后 status=1
 * - 超时未支付：关闭支付单（status=3），并将该支付单下所有“待付款订单”置为已取消（status=4）
 * - 取消原因固定写入："订单超时，自动取消"
 * </p>
 *
 * 注意：当前 createOrder 仅校验库存，不扣减库存，因此待付款超时取消无需恢复库存。
 */
@Component
public class OrderPaymentTimeoutCancelTask {
    private static final Logger logger = LoggerFactory.getLogger(OrderPaymentTimeoutCancelTask.class);

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 支付超时时长（分钟），默认15分钟。
     * 可在 application.yml 中配置：order.payment-timeout-minutes
     */
    @Value("${order.payment-timeout-minutes:15}")
    private int paymentTimeoutMinutes;

    /**
     * 每10分钟扫描一次超时支付单（避免过于频繁）。
     */
    @Scheduled(cron = "0 */10 * * * ?")
    @Transactional
    public void cancelTimeoutPayments() {
        if (paymentTimeoutMinutes <= 0) {
            logger.warn("未支付超时自动取消任务已跳过：paymentTimeoutMinutes配置不合法: {}", paymentTimeoutMinutes);
            return;
        }

        final long cutoffMillis = System.currentTimeMillis() - (long) paymentTimeoutMinutes * 60L * 1000L;
        final Date cutoff = new Date(cutoffMillis);

        List<Payment> expiredPayments = paymentMapper.selectPendingBefore(cutoff);
        if (expiredPayments == null || expiredPayments.isEmpty()) {
            return;
        }

        for (Payment payment : expiredPayments) {
            try {
                closePaymentAndCancelOrders(payment);
            } catch (Exception e) {
                logger.error("超时关闭支付单失败: paymentId={}", payment != null ? payment.getId() : "null", e);
            }
        }
    }

    private void closePaymentAndCancelOrders(Payment payment) {
        if (payment == null || payment.getId() == null) return;

        // 再次校验：仅处理仍为待支付
        if (payment.getStatus() == null || !Payment.STATUS_PENDING.equals(payment.getStatus())) {
            return;
        }

        String paymentId = payment.getId();
        Date now = new Date();

        // 1) 关闭支付单
        payment.setStatus(Payment.STATUS_CLOSED);
        payment.setRemark("订单超时，自动取消");
        payment.setUpdateTime(now);
        paymentMapper.updateById(payment);

        // 2) 取消该支付单下所有待付款订单
        List<Order> orders = orderMapper.selectByPaymentId(paymentId);
        if (orders == null || orders.isEmpty()) {
            logger.warn("超时支付单下无订单: paymentId={}", paymentId);
            return;
        }

        int cancelledCount = 0;
        for (Order order : orders) {
            if (order == null) continue;
            if (order.getStatus() != null && order.getStatus() == Order.STATUS_PENDING_PAYMENT) {
                order.setStatus(Order.STATUS_CANCELLED);
                order.setCancelReason("订单超时，自动取消");
                order.setCancelTime(now);
                order.setUpdateTime(now);
                orderMapper.updateById(order);
                cancelledCount++;
            }
        }

        logger.info("超时支付单已关闭并取消订单: paymentId={}, cancelledOrders={}", paymentId, cancelledCount);
    }
}

