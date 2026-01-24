package com.shangnantea.mapper;

import com.shangnantea.model.entity.user.UserFavorite;
import org.apache.ibatis.annotations.Param;

/**
 * 用户收藏Mapper接口
 */
public interface UserFavoriteMapper extends BaseMapper<UserFavorite, Integer> {
    /**
     * 统计用户的收藏数
     *
     * @param userId 用户ID
     * @return 收藏数
     */
    long countByUserId(@Param("userId") String userId);
} 