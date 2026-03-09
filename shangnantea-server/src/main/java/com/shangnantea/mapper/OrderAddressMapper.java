package com.shangnantea.mapper;

import com.shangnantea.model.entity.order.OrderAddress;

/**
 * 订单地址快照 Mapper
 */
public interface OrderAddressMapper extends BaseMapper<OrderAddress, Integer> {

    /**
     * 根据订单ID查询订单地址
     *
     * @param orderId 订单ID
     * @return 订单地址快照
     */
    OrderAddress selectByOrderId(String orderId);
}

