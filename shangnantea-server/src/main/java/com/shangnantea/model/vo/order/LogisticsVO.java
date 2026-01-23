package com.shangnantea.model.vo.order;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 物流信息VO
 */
@Data
public class LogisticsVO {
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
     * 物流轨迹列表（预留字段，当前返回null）
     */
    private List<LogisticsTrace> traces;
    
    /**
     * 物流轨迹内部类
     */
    @Data
    public static class LogisticsTrace {
        /**
         * 时间
         */
        private Date time;
        
        /**
         * 状态描述
         */
        private String status;
    }
}
