package com.shangnantea.mapper;

import com.shangnantea.model.entity.shop.ShopCertification;
import org.apache.ibatis.annotations.Param;

/**
 * 商家认证Mapper接口
 */
public interface ShopCertificationMapper extends BaseMapper<ShopCertification, Integer> {
    
    /**
     * 根据用户ID查询认证信息
     *
     * @param userId 用户ID
     * @return 认证信息
     */
    ShopCertification selectByUserId(@Param("userId") String userId);
} 