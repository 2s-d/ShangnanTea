package com.shangnantea.mapper;

import com.shangnantea.model.entity.user.UserFollow;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户关注Mapper接口
 */
public interface UserFollowMapper extends BaseMapper<UserFollow, Integer> {
    
    /**
     * 根据用户ID查询关注列表
     *
     * @param userId 用户ID
     * @param followType 关注类型（可选）
     * @return 关注列表
     */
    List<UserFollow> selectByUserId(@Param("userId") String userId, @Param("followType") String followType);
    
    /**
     * 检查是否已关注
     *
     * @param userId 用户ID
     * @param followId 被关注对象ID
     * @param followType 关注类型
     * @return 关注记录
     */
    UserFollow selectByUserIdAndFollowId(@Param("userId") String userId, 
                                         @Param("followId") String followId,
                                         @Param("followType") String followType);
} 