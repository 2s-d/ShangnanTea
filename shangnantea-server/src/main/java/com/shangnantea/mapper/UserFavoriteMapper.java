package com.shangnantea.mapper;

import com.shangnantea.model.entity.user.UserFavorite;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户收藏Mapper接口
 */
public interface UserFavoriteMapper extends BaseMapper<UserFavorite, Integer> {
    /**
     * 根据用户ID查询收藏列表
     */
    List<UserFavorite> selectByUserId(String userId);
    
    /**
     * 根据用户ID和收藏类型查询收藏列表
     */
    List<UserFavorite> selectByUserIdAndType(@Param("userId") String userId, @Param("itemType") String itemType);
    
    /**
     * 检查是否已收藏
     */
    UserFavorite selectByUserIdAndItem(@Param("userId") String userId, @Param("itemType") String itemType, @Param("itemId") String itemId);
} 