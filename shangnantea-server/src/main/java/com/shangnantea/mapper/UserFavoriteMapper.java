package com.shangnantea.mapper;

import com.shangnantea.model.entity.user.UserFavorite;
import org.apache.ibatis.annotations.Param;

/**
 * 用户收藏Mapper接口
 */
public interface UserFavoriteMapper extends BaseMapper<UserFavorite, Integer> {
    /**
     * 根据用户ID和收藏项查询收藏记录
     * @param userId 用户ID
     * @param itemType 收藏项类型
     * @param itemId 收藏项ID
     * @return 收藏记录
     */
    UserFavorite selectByUserIdAndItem(@Param("userId") String userId, 
                                       @Param("itemType") String itemType, 
                                       @Param("itemId") String itemId);
} 