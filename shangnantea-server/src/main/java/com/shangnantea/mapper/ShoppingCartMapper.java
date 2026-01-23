package com.shangnantea.mapper;

import com.shangnantea.model.entity.order.ShoppingCart;

import java.util.List;

/**
 * 购物车Mapper接口
 */
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart, Integer> {
    
    /**
     * 根据用户ID查询购物车列表
     *
     * @param userId 用户ID
     * @return 购物车列表
     */
    List<ShoppingCart> selectByUserId(String userId);
} 