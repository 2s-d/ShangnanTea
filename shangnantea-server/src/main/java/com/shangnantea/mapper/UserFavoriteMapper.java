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
     * 根据用户ID和收藏项查询收藏记录
     *
     * @param userId 用户ID
     * @param itemType 收藏项类型
     * @param itemId 收藏项ID
     * @return 收藏记录
     */
    UserFavorite selectByUserIdAndItem(@Param("userId") String userId, @Param("itemType") String itemType, @Param("itemId") String itemId);
    
    /**
     * 统计用户的收藏数
     *
     * @param userId 用户ID
     * @return 收藏数
     */
    long countByUserId(@Param("userId") String userId);
    
    /**
     * 统计某个对象的收藏数
     *
     * @param itemType 收藏项类型 (post/article等)
     * @param itemId 收藏项ID
     * @return 收藏数
     */
    Integer countByItem(@Param("itemType") String itemType, @Param("itemId") String itemId);
    
    /**
     * 根据用户ID、收藏类型和收藏项ID删除收藏记录
     * @param userId 用户ID
     * @param itemType 收藏项类型
     * @param itemId 收藏项ID
     * @return 删除的记录数
     */
    int deleteByUserIdAndItem(@Param("userId") String userId, @Param("itemType") String itemType, @Param("itemId") String itemId);
    
    /**
     * 根据用户ID删除所有收藏记录（用于删除用户时清理数据）
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteByUserId(@Param("userId") String userId);
} 