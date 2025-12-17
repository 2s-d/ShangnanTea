package com.shangnantea.service;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.model.entity.message.ChatMessage;
import com.shangnantea.model.entity.message.ChatSession;
import com.shangnantea.model.entity.message.UserNotification;

import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService {
    
    /**
     * 获取聊天会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatSession> listSessions(String userId);
    
    /**
     * 获取会话详情
     *
     * @param id 会话ID
     * @return 会话信息
     */
    ChatSession getSessionById(Long id);
    
    /**
     * 创建聊天会话
     *
     * @param session 会话信息
     * @return 会话信息
     */
    ChatSession createSession(ChatSession session);
    
    /**
     * 创建用户与用户/商家之间的会话
     *
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 类型
     * @return 会话信息
     */
    ChatSession createOrGetSession(String userId, String targetId, Integer type);
    
    /**
     * 获取会话的消息列表
     *
     * @param sessionId 会话ID
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<ChatMessage> listMessages(Long sessionId, PageParam pageParam);
    
    /**
     * 发送消息
     *
     * @param message 消息信息
     * @return 消息信息
     */
    ChatMessage sendMessage(ChatMessage message);
    
    /**
     * 将消息标记为已读
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markMessagesAsRead(Long sessionId, String userId);
    
    /**
     * 获取未读消息数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    int countUnreadMessages(String userId);
    
    /**
     * 获取用户通知列表
     *
     * @param userId 用户ID
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<UserNotification> listNotifications(String userId, PageParam pageParam);
    
    /**
     * 发送通知
     *
     * @param notification 通知信息
     * @return 通知信息
     */
    UserNotification sendNotification(UserNotification notification);
    
    /**
     * 发送系统通知
     *
     * @param userId 用户ID
     * @param title 标题
     * @param content 内容
     * @param type 类型
     * @param relatedId 关联ID
     * @return 通知信息
     */
    UserNotification sendSystemNotification(String userId, String title, String content, Integer type, String relatedId);
    
    /**
     * 将通知标记为已读
     *
     * @param id 通知ID
     * @return 是否成功
     */
    boolean markNotificationAsRead(Long id);
    
    /**
     * 批量将通知标记为已读
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAllNotificationsAsRead(String userId);
    
    /**
     * 获取未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    int countUnreadNotifications(String userId);
} 