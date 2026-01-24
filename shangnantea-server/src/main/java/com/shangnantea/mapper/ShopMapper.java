package com.shangnantea.mapper;

import com.shangnantea.model.entity.shop.Shop;

/**
 * 店铺Mapper接口
 */
public interface ShopMapper extends BaseMapper<Shop, String> {
    /**
     * 根据店主用户ID查询店铺
     * @param ownerId 店主用户ID
     * @return 店铺信息
     */
    Shop selectByOwnerId(String ownerId);
} 