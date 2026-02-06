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
     * @param userId 用户ID
     * @param followId 被关注对象ID
     * @param followType 关注类型
     * @return 关注记录
     */
    UserFollow selectByUserIdAndFollowId(@Param("userId") String userId, 
                                         @Param("followId") String followId,
                                         @Param("followType") String followType);
    
    /**
     * 检查是否已关注（粉丝关系）
     *
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @return 关注记录，如果未关注则返回null
     */
    UserFollow selectByFollowerAndFollowed(@Param("followerId") String followerId, 
                                           @Param("followedId") String followedId);
    
    /**
     * 统计某个对象的关注数/粉丝数
     *
     * @param followType 关注类型 (user/shop等)
     * @param followId 被关注对象ID
     * @return 关注数
     */
    Integer countByFollow(@Param("followType") String followType, @Param("followId") String followId);
    
    /**
     * 根据用户ID、关注类型和关注ID删除关注记录
     * @param userId 用户ID
     * @param followType 关注类型
     * @param followId 被关注对象ID
     * @return 删除的记录数
     */
    int deleteByUserIdAndFollow(@Param("userId") String userId, @Param("followType") String followType, @Param("followId") String followId);
    
    /**
     * 根据用户ID删除所有关注记录（用于删除用户时清理数据）
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteByUserId(@Param("userId") String userId);
} 