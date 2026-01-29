package com.shangnantea.mapper;

import com.shangnantea.model.entity.shop.Shop;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 店铺Mapper接口
 */
public interface ShopMapper extends BaseMapper<Shop, String> {
    
    /**
     * 分页查询店铺列表
     * 支持关键词搜索、评分筛选、排序
     *
     * @param keyword 搜索关键词
     * @param rating 最低评分
     * @param sortBy 排序字段
     * @param sortOrder 排序方向
     * @param offset 分页偏移量
     * @param pageSize 每页记录数
     * @return 店铺列表
     */
    List<Shop> selectShopList(@Param("keyword") String keyword,
                              @Param("rating") Double rating,
                              @Param("sortBy") String sortBy,
                              @Param("sortOrder") String sortOrder,
                              @Param("offset") Integer offset,
                              @Param("pageSize") Integer pageSize);
    
    /**
     * 统计店铺总数（用于分页）
     *
     * @param keyword 搜索关键词
     * @param rating 最低评分
     * @return 店铺总数
     */
    Long countShopList(@Param("keyword") String keyword,
                       @Param("rating") Double rating);
    
    /**
     * 根据店铺名称查询店铺
     *
     * @param shopName 店铺名称
     * @return 店铺信息
     */
    Shop selectByShopName(@Param("shopName") String shopName);
    
    /**
     * 根据用户ID查询店铺
     *
     * @param userId 用户ID
     * @return 店铺信息
     */
    Shop selectByUserId(@Param("userId") String userId);
    
    /**
     * 根据店主用户ID查询店铺
     *
     * @param ownerId 店主用户ID
     * @return 店铺信息
     */
    Shop selectByOwnerId(@Param("ownerId") String ownerId);
} 