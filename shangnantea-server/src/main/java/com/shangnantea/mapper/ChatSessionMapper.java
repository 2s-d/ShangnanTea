package com.shangnantea.mapper;

import com.shangnantea.model.entity.message.ChatSession;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天会话Mapper接口
 */
public interface ChatSessionMapper extends BaseMapper<ChatSession, String> {
    /**
     * 根据用户ID查询会话列表
     * 查询用户作为发起者或接收者的所有会话
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatSession> selectByUserId(@Param("userId") String userId);
    
    /**
     * 根据两个用户ID查询会话
     * 查询两个用户之间的会话（不区分发起者和接收者）
     *
     * @param userId1 用户ID1
     * @param userId2 用户ID2
     * @return 会话对象
     */
    ChatSession selectByUserIds(@Param("userId1") String userId1, @Param("userId2") String userId2);
} 