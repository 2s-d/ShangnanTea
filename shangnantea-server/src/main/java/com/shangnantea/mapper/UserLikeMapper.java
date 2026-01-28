package com.shangnantea.mapper;

import com.shangnantea.model.entity.user.UserLike;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户点赞Mapper接口
 */
public interface UserLikeMapper extends BaseMapper<UserLike, Integer> {
    /**
     * 查询用户的点赞列表
     */
    List<UserLike> selectByUserId(String userId);
    
    /**
     * 根据用户ID和点赞类型查询点赞列表
     */
    List<UserLike> selectByUserIdAndType(@Param("userId") String userId, @Param("targetType") String targetType);
    
    /**
     * 查询用户是否点赞了某个对象
     */
    UserLike selectByUserIdAndTarget(@Param("userId") String userId, @Param("targetType") String targetType, @Param("targetId") String targetId);
} 