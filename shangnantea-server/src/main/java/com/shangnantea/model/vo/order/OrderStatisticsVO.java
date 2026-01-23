package com.shangnantea.model.vo.order;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单统计VO
 */
@Data
public class OrderStatisticsVO {
    
    /**
     * 订单总数
     */
    private Integer totalOrders;
    
    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 订单状态分布
     * key: 状态值（0-待支付, 1-待发货, 2-待收货, 3-已完成, 4-已取消, 5-已退款）
     * value: 该状态的订单数量
     */
    private Map<String, Integer> statusDistribution;
    
    /**
     * 订单趋势数据
     */
    private List<TrendData> trend;
    
    /**
     * 趋势数据内部类
     */
    @Data
    public static class TrendData {
        /**
         * 日期
         */
        private String date;
        
        /**
         * 订单数量
         */
        private Integer orders;
        
        /**
         * 订单金额
         */
        private BigDecimal amount;
    }
}
