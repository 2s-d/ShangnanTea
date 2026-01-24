package com.shangnantea.mapper;

import com.shangnantea.model.entity.shop.ShopCertification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商家认证Mapper接口
 */
@Mapper
public interface ShopCertificationMapper extends BaseMapper<ShopCertification, Integer> {
    
    /**
     * 根据用户ID查询认证信息
     *
     * @param userId 用户ID
     * @return 认证信息
     */
    ShopCertification selectByUserId(@Param("userId") String userId);
    
    /**
     * 根据ID查询认证信息
     *
     * @param id 认证ID
     * @return 认证信息
     */
    ShopCertification selectById(@Param("id") Integer id);
    
    /**
     * 分页查询认证列表
     *
     * @param status 状态
     * @param offset 偏移量
     * @param pageSize 每页数量
     * @return 认证列表
     */
    List<ShopCertification> selectByPage(@Param("status") Integer status, 
                                         @Param("offset") Integer offset, 
                                         @Param("pageSize") Integer pageSize);
    
    /**
     * 统计认证数量
     *
     * @param status 状态
     * @return 认证数量
     */
    int countByCondition(@Param("status") Integer status);
    
    /**
     * 更新认证信息
     *
     * @param certification 认证信息
     * @return 影响行数
     */
    int update(ShopCertification certification);
} 