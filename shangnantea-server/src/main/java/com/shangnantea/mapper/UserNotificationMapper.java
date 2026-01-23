package com.shangnantea.mapper;

import com.shangnantea.model.entity.message.UserNotification;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户通知Mapper接口
 */
public interface UserNotificationMapper extends BaseMapper<UserNotification, Long> {
    /**
     * 根据用户ID和类型查询通知列表（支持分页）
     *
     * @param userId 用户ID
     * @param type 通知类型（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 通知列表
     */
    List<UserNotification> selectByUserIdAndType(@Param("userId") String userId, 
                                                 @Param("type") String type,
                                                 @Param("offset") Integer offset,
                                                 @Param("limit") Integer limit);
    
    /**
     * 统计用户的通知数量
     *
     * @param userId 用户ID
     * @param type 通知类型（可选）
     * @return 数量
     */
    long countByUserIdAndType(@Param("userId") String userId, @Param("type") String type);
    
    /**
     * 统计用户的未读通知数量
     *
     * @param userId 用户ID
     * @param type 通知类型（可选）
     * @return 未读数量
     */
    long countUnreadByUserIdAndType(@Param("userId") String userId, @Param("type") String type);
} 