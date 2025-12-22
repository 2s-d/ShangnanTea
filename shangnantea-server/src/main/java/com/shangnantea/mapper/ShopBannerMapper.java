package com.shangnantea.mapper;

import com.shangnantea.model.entity.shop.ShopBanner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 店铺Banner Mapper接口
 */
@Mapper
public interface ShopBannerMapper {
    
    /**
     * 根据ID查询
     */
    ShopBanner selectById(@Param("id") Integer id);
    
    /**
     * 根据店铺ID查询Banner列表
     */
    List<ShopBanner> selectByShopId(@Param("shopId") String shopId);
    
    /**
     * 插入记录
     */
    int insert(ShopBanner banner);
    
    /**
     * 更新记录
     */
    int updateById(ShopBanner banner);
    
    /**
     * 删除记录
     */
    int deleteById(@Param("id") Integer id);
}
