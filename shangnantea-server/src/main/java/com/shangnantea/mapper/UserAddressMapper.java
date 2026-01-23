package com.shangnantea.mapper;

import com.shangnantea.model.entity.user.UserAddress;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户地址Mapper接口
 */
public interface UserAddressMapper extends BaseMapper<UserAddress, Integer> {
    
    /**
     * 根据用户ID查询所有地址
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<UserAddress> selectByUserId(@Param("userId") String userId);
} 