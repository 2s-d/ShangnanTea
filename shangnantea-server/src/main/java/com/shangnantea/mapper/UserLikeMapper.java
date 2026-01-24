package com.shangnantea.mapper;

import com.shangnantea.model.entity.user.UserLike;
import org.apache.ibatis.annotations.Param;

/**
 * 用户点赞Mapper接口
 */
public interface UserLikeMapper extends BaseMapper<UserLike, Integer> {
    /**
     * 根据用户ID和目标查询点赞记录
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 点赞记录
     */
    UserLike selectByUserIdAndTarget(@Param("userId") String userId, 
                                     @Param("targetType") String targetType, 
                                     @Param("targetId") String targetId);
} 