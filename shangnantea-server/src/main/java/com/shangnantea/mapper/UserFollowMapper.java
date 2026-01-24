package com.shangnantea.mapper;

import com.shangnantea.model.entity.user.UserFollow;
import org.apache.ibatis.annotations.Param;

/**
 * 用户关注Mapper接口
 */
public interface UserFollowMapper extends BaseMapper<UserFollow, Integer> {
    /**
     * 统计用户的关注数（我关注的人数）
     *
     * @param userId 用户ID
     * @return 关注数
     */
    long countFollowingByUserId(@Param("userId") String userId);
    
    /**
     * 统计用户的粉丝数（关注我的人数）
     *
     * @param userId 用户ID
     * @return 粉丝数
     */
    long countFollowersByUserId(@Param("userId") String userId);
    
    /**
     * 检查是否已关注
     *
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @return 关注记录，如果未关注则返回null
     */
    UserFollow selectByFollowerAndFollowed(@Param("followerId") String followerId, 
                                           @Param("followedId") String followedId);
} 