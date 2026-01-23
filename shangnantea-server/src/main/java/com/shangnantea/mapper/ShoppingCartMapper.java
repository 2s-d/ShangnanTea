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
    
    /**
     * 根据用户ID、茶叶ID和规格ID查询购物车项
     *
     * @param userId 用户ID
     * @param teaId 茶叶ID
     * @param specId 规格ID
     * @return 购物车项
     */
    ShoppingCart selectByUserIdAndTeaIdAndSpecId(String userId, String teaId, Integer specId);
} 