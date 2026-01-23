package com.shangnantea.mapper;

import com.shangnantea.model.entity.shop.ShopReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 店铺评价 Mapper接口
 */
@Mapper
public interface ShopReviewMapper {
    
    /**
     * 根据ID查询
     */
    ShopReview selectById(@Param("id") Integer id);
    
    /**
     * 根据店铺ID分页查询评价列表
     */
    List<ShopReview> selectByShopId(@Param("shopId") String shopId,
                                     @Param("offset") Integer offset,
                                     @Param("pageSize") Integer pageSize);
    
    /**
     * 统计店铺评价总数
     */
    Long countByShopId(@Param("shopId") String shopId);
    
    /**
     * 查询用户对店铺的评价
     */
    ShopReview selectByUserIdAndShopId(@Param("userId") String userId,
                                        @Param("shopId") String shopId);
    
    /**
     * 插入记录
     */
    int insert(ShopReview review);
    
    /**
     * 更新记录
     */
    int updateById(ShopReview review);
    
    /**
     * 删除记录
     */
    int deleteById(@Param("id") Integer id);
}
