package com.shangnantea.mapper;

import com.shangnantea.model.entity.shop.ShopReview;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 店铺评价Mapper接口
 */
public interface ShopReviewMapper extends BaseMapper<ShopReview, Integer> {
    
    /**
     * 根据店铺ID和用户ID查询评价
     *
     * @param shopId 店铺ID
     * @param userId 用户ID
     * @return 评价对象，如果不存在则返回null
     */
    ShopReview selectByShopIdAndUserId(@Param("shopId") String shopId,
                                        @Param("userId") String userId);
    
    /**
     * 根据店铺ID分页查询评价列表
     *
     * @param shopId   店铺ID
     * @param offset   偏移量
     * @param pageSize 每页数量
     * @return 评价列表
     */
    List<ShopReview> selectByShopIdWithPage(@Param("shopId") String shopId,
                                            @Param("offset") Integer offset,
                                            @Param("pageSize") Integer pageSize);
    
    /**
     * 统计某个店铺的评价总数
     *
     * @param shopId 店铺ID
     * @return 评价总数
     */
    int countByShopId(@Param("shopId") String shopId);
    
    /**
     * 统计某个店铺所有评分的总和（用于重新计算平均分）
     *
     * @param shopId 店铺ID
     * @return 评分总和
     */
    Integer sumRatingByShopId(@Param("shopId") String shopId);
}
