package com.shangnantea.mapper;

import com.shangnantea.model.entity.message.ChatMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天消息Mapper接口
 */
public interface ChatMessageMapper extends BaseMapper<ChatMessage, Long> {
    /**
     * 根据用户ID查询聊天消息列表（支持分页）
     * 查询发送给该用户或该用户发送的消息
     *
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 消息列表
     */
    List<ChatMessage> selectByUserId(@Param("userId") String userId,
                                     @Param("offset") Integer offset,
                                     @Param("limit") Integer limit);
    
    /**
     * 统计用户的聊天消息数量
     *
     * @param userId 用户ID
     * @return 数量
     */
    long countByUserId(@Param("userId") String userId);
    
    /**
     * 统计用户的未读聊天消息数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    long countUnreadByUserId(@Param("userId") String userId);
} 