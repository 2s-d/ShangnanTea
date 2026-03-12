package com.shangnantea.mapper;

import com.shangnantea.model.entity.order.Payment;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 支付单Mapper接口
 */
public interface PaymentMapper extends BaseMapper<Payment, String> {
    /**
     * 查询在指定时间之前创建、且仍处于待支付状态的支付单
     *
     * @param cutoff 截止时间（create_time < cutoff）
     * @return 支付单列表
     */
    List<Payment> selectPendingBefore(@Param("cutoff") Date cutoff);
}

