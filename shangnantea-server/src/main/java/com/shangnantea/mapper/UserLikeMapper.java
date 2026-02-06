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
     *
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 点赞记录
     */
    UserLike selectByUserIdAndTarget(@Param("userId") String userId, @Param("targetType") String targetType, @Param("targetId") String targetId);
    
    /**
     * 统计某个对象的点赞数
     *
     * @param targetType 目标类型 (post/reply/review等)
     * @param targetId 目标ID
     * @return 点赞数
     */
    Integer countByTarget(@Param("targetType") String targetType, @Param("targetId") String targetId);
    
    /**
     * 根据用户ID、目标类型和目标ID删除点赞记录
     *
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 删除的记录数
     */
    int deleteByUserIdAndTarget(@Param("userId") String userId, @Param("targetType") String targetType, @Param("targetId") String targetId);
    
    /**
     * 根据用户ID删除所有点赞记录（用于删除用户时清理数据）
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteByUserId(@Param("userId") String userId);
} 