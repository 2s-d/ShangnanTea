package com.shangnantea.service.impl;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.common.api.Result;
import com.shangnantea.mapper.ChatMessageMapper;
import com.shangnantea.mapper.ChatSessionMapper;
import com.shangnantea.mapper.UserNotificationMapper;
import com.shangnantea.model.entity.message.ChatMessage;
import com.shangnantea.model.entity.message.ChatSession;
import com.shangnantea.model.entity.message.UserNotification;
import com.shangnantea.model.vo.message.MessageVO;
import com.shangnantea.security.context.UserContext;
import com.shangnantea.service.MessageService;
import com.shangnantea.utils.FileUploadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 消息服务实现类
 */
@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Autowired
    private ChatSessionMapper sessionMapper;
    
    @Autowired
    private ChatMessageMapper messageMapper;
    
    @Autowired
    private UserNotificationMapper notificationMapper;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Override
    public List<ChatSession> listSessions(String userId) {
        // TODO: 实现获取聊天会话列表的逻辑
        return null; // 待实现
    }
    
    @Override
    public ChatSession getSessionById(String id) {
        // TODO: 实现获取会话详情的逻辑
        return sessionMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public ChatSession createSession(ChatSession session) {
        // TODO: 实现创建聊天会话的逻辑
        Date now = new Date();
        session.setCreateTime(now);
        session.setUpdateTime(now);
        session.setLastMessageTime(now); // 使用lastMessageTime代替lastActiveTime
        if (session.getId() == null) {
            session.setId(UUID.randomUUID().toString());
        }
        session.setStatus(1); // 默认状态为有效
        sessionMapper.insert(session);
        return session;
    }
    
    @Override
    @Transactional
    public ChatSession createOrGetSession(String userId, String targetId, Integer type) {
        // TODO: 实现创建用户与用户/商家之间的会话的逻辑
        // 先查询是否已存在会话
        
        // 如果不存在则创建新会话
        ChatSession session = new ChatSession();
        session.setInitiatorId(userId); // 使用initiatorId代替userId
        session.setReceiverId(targetId); // 使用receiverId代替targetId
        session.setSessionType(type.toString()); // 使用sessionType代替type
        // 会话名称可以从目标用户/商家名称获取
        
        return createSession(session);
    }
    
    @Override
    public PageResult<ChatMessage> listMessages(Long sessionId, PageParam pageParam) {
        // TODO: 实现获取会话的消息列表的逻辑
        return new PageResult<>();
    }
    
    @Override
    @Transactional
    public ChatMessage sendMessage(ChatMessage message) {
        // TODO: 实现发送消息的逻辑
        Date now = new Date();
        message.setCreateTime(now);
        message.setIsRead(0); // 未读
        messageMapper.insert(message);
        
        // 更新会话的最后活动时间和最后消息
        ChatSession session = sessionMapper.selectById(message.getSessionId());
        if (session != null) {
            session.setLastMessage(message.getContent()); // 使用lastMessage代替lastMessageId
            session.setLastMessageTime(now); // 使用lastMessageTime代替lastActiveTime
            session.setUpdateTime(now);
            sessionMapper.updateById(session);
        }
        
        return message;
    }
    
    @Override
    @Transactional
    public boolean markMessagesAsRead(Long sessionId, String userId) {
        // TODO: 实现将消息标记为已读的逻辑
        return false; // 待实现
    }
    
    @Override
    public int countUnreadMessages(String userId) {
        // TODO: 实现获取未读消息数量的逻辑
        return 0; // 待实现
    }
    
    @Override
    public PageResult<UserNotification> listNotifications(String userId, PageParam pageParam) {
        // TODO: 实现获取用户通知列表的逻辑
        return new PageResult<>();
    }
    
    @Override
    @Transactional
    public UserNotification sendNotification(UserNotification notification) {
        // TODO: 实现发送通知的逻辑
        Date now = new Date();
        notification.setCreateTime(now);
        notification.setIsRead(0); // 未读
        notificationMapper.insert(notification);
        return notification;
    }
    
    @Override
    @Transactional
    public UserNotification sendSystemNotification(String userId, String title, String content, Integer type, String relatedId) {
        // TODO: 实现发送系统通知的逻辑
        UserNotification notification = new UserNotification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType("system"); // 设置为system类型
        notification.setTargetId(relatedId); // 使用targetId代替relatedId
        
        return sendNotification(notification);
    }
    
    @Override
    @Transactional
    public boolean markNotificationAsRead(Long id) {
        // TODO: 实现将通知标记为已读的逻辑
        UserNotification notification = notificationMapper.selectById(id);
        if (notification == null) {
            return false;
        }
        
        notification.setIsRead(1); // 已读
        notification.setReadTime(new Date());
        
        return notificationMapper.updateById(notification) > 0;
    }
    
    @Override
    @Transactional
    public boolean markAllNotificationsAsRead(String userId) {
        // TODO: 实现批量将通知标记为已读的逻辑
        return false; // 待实现
    }
    
    @Override
    public int countUnreadNotifications(String userId) {
        // TODO: 实现获取未读通知数量的逻辑
        return 0; // 待实现
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> sendImageMessage(String sessionId, String receiverId, MultipartFile image) {
        try {
            // 1. 获取当前用户ID
            String senderId = UserContext.getCurrentUserId();
            if (senderId == null) {
                logger.warn("图片消息发送失败: 用户未登录");
                return Result.failure(7116); // 图片消息发送失败
            }
            
            // 2. 验证会话是否存在
            ChatSession session = sessionMapper.selectById(sessionId);
            if (session == null) {
                logger.warn("图片消息发送失败: 会话不存在, sessionId: {}", sessionId);
                return Result.failure(7117); // 图片消息发送失败
            }
            
            // 3. 调用工具类上传图片（硬编码type为"messages"）
            String relativePath = FileUploadUtils.uploadImage(image, "messages");
            
            // 4. 生成访问URL
            String accessUrl = FileUploadUtils.generateAccessUrl(relativePath, baseUrl);
            
            // 5. 创建聊天消息记录
            ChatMessage message = new ChatMessage();
            // message.setId() - 让数据库自动生成ID
            message.setSessionId(sessionId);
            message.setSenderId(senderId);
            message.setReceiverId(receiverId);
            message.setContent(accessUrl); // 存储完整的访问URL
            message.setContentType("image"); // 标识为图片消息
            message.setIsRead(0); // 未读
            message.setCreateTime(new Date());
            
            // 6. 保存消息到数据库
            int result = messageMapper.insert(message);
            if (result <= 0) {
                logger.error("图片消息发送失败: 数据库插入失败, sessionId: {}", sessionId);
                return Result.failure(7118); // 图片消息发送失败
            }
            
            // 7. 更新会话的最后消息信息
            session.setLastMessage("[图片]"); // 显示为[图片]
            session.setLastMessageTime(new Date());
            session.setUpdateTime(new Date());
            sessionMapper.updateById(session);
            
            // 8. 构造返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("messageId", message.getId());
            responseData.put("content", accessUrl);
            responseData.put("contentType", "image");
            responseData.put("createTime", message.getCreateTime());
            
            logger.info("图片消息发送成功: sessionId: {}, messageId: {}, path: {}", 
                    sessionId, message.getId(), relativePath);
            return Result.success(7009, responseData); // 图片消息发送成功
            
        } catch (Exception e) {
            logger.error("图片消息发送失败: 系统异常", e);
            return Result.failure(7118); // 图片消息发送失败
        }
    }
    
    @Override
    public Result<Object> getMessages(Map<String, Object> params) {
        try {
            logger.info("获取消息列表请求, params: {}", params);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取消息列表失败：用户未登录");
                return Result.failure(7100);
            }
            
            // 2. 解析查询参数
            Integer page = params.get("page") != null ? 
                Integer.parseInt(params.get("page").toString()) : 1;
            Integer pageSize = params.get("pageSize") != null ? 
                Integer.parseInt(params.get("pageSize").toString()) : 10;
            String type = params.get("type") != null ? 
                params.get("type").toString() : null;
            
            // 3. 参数验证和修正
            if (page < 1) {
                page = 1;
            }
            if (pageSize < 1) {
                pageSize = 10;
            }
            if (pageSize > 100) {
                pageSize = 100; // 限制最大页大小
            }
            
            Integer offset = (page - 1) * pageSize;
            
            // 4. 根据type查询不同类型的消息
            List<MessageVO> messageList = new ArrayList<>();
            long total = 0;
            
            if (type == null || "system".equals(type) || "notification".equals(type)) {
                // 查询通知消息（system和notification都从UserNotification表查询）
                String notificationType = "system".equals(type) ? "system" : type;
                List<UserNotification> notifications = notificationMapper.selectByUserIdAndType(
                    userId, notificationType, offset, pageSize);
                total = notificationMapper.countByUserIdAndType(userId, notificationType);
                
                // 转换为MessageVO
                messageList = notifications.stream().map(this::convertNotificationToVO)
                    .collect(Collectors.toList());
            } else if ("chat".equals(type)) {
                // 查询聊天消息
                List<ChatMessage> chatMessages = messageMapper.selectByUserId(userId, offset, pageSize);
                total = messageMapper.countByUserId(userId);
                
                // 转换为MessageVO
                messageList = chatMessages.stream().map(this::convertChatMessageToVO)
                    .collect(Collectors.toList());
            } else {
                // 无效的type参数
                logger.warn("获取消息列表失败：无效的消息类型, type: {}", type);
                return Result.failure(7100);
            }
            
            // 5. 构造返回数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("list", messageList);
            resultData.put("total", total);
            
            logger.info("获取消息列表成功, userId: {}, type: {}, total: {}", userId, type, total);
            return Result.success(200, resultData);
            
        } catch (Exception e) {
            logger.error("获取消息列表失败，系统异常", e);
            return Result.failure(7100);
        }
    }
    
    /**
     * 将UserNotification转换为MessageVO
     */
    private MessageVO convertNotificationToVO(UserNotification notification) {
        MessageVO vo = new MessageVO();
        vo.setId(notification.getId());
        vo.setType(notification.getType() != null ? notification.getType() : "notification");
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setIsRead(notification.getIsRead() != null && notification.getIsRead() == 1);
        vo.setCreateTime(notification.getCreateTime());
        return vo;
    }
    
    /**
     * 将ChatMessage转换为MessageVO
     */
    private MessageVO convertChatMessageToVO(ChatMessage message) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setType("chat");
        vo.setTitle(null); // 聊天消息没有标题
        vo.setContent(message.getContent());
        vo.setIsRead(message.getIsRead() != null && message.getIsRead() == 1);
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }
    
    @Override
    public Result<Object> getMessageDetail(String id) {
        try {
            logger.info("获取消息详情请求, id: {}", id);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取消息详情失败：用户未登录");
                return Result.failure(7101);
            }
            
            // 2. 解析消息ID
            Long messageId;
            try {
                messageId = Long.parseLong(id);
            } catch (NumberFormatException e) {
                logger.warn("获取消息详情失败：无效的消息ID, id: {}", id);
                return Result.failure(7101);
            }
            
            // 3. 先尝试从ChatMessage表查询
            ChatMessage chatMessage = messageMapper.selectById(messageId);
            if (chatMessage != null) {
                // 验证权限：用户必须是发送者或接收者
                if (!userId.equals(chatMessage.getSenderId()) && !userId.equals(chatMessage.getReceiverId())) {
                    logger.warn("获取消息详情失败：无权限访问, userId: {}, messageId: {}", userId, messageId);
                    return Result.failure(7101);
                }
                
                // 如果是接收者且消息未读，标记为已读
                if (userId.equals(chatMessage.getReceiverId()) && 
                    (chatMessage.getIsRead() == null || chatMessage.getIsRead() == 0)) {
                    chatMessage.setIsRead(1);
                    chatMessage.setReadTime(new Date());
                    messageMapper.updateById(chatMessage);
                    logger.info("消息已标记为已读, messageId: {}", messageId);
                }
                
                // 转换为VO并返回
                MessageVO vo = convertChatMessageToVO(chatMessage);
                logger.info("获取消息详情成功（聊天消息）, userId: {}, messageId: {}", userId, messageId);
                return Result.success(200, vo);
            }
            
            // 4. 如果不是聊天消息，尝试从UserNotification表查询
            UserNotification notification = notificationMapper.selectById(messageId);
            if (notification != null) {
                // 验证权限：用户必须是接收者
                if (!userId.equals(notification.getUserId())) {
                    logger.warn("获取消息详情失败：无权限访问, userId: {}, messageId: {}", userId, messageId);
                    return Result.failure(7101);
                }
                
                // 如果通知未读，标记为已读
                if (notification.getIsRead() == null || notification.getIsRead() == 0) {
                    notification.setIsRead(1);
                    notification.setReadTime(new Date());
                    notificationMapper.updateById(notification);
                    logger.info("通知已标记为已读, messageId: {}", messageId);
                }
                
                // 转换为VO并返回
                MessageVO vo = convertNotificationToVO(notification);
                logger.info("获取消息详情成功（通知）, userId: {}, messageId: {}", userId, messageId);
                return Result.success(200, vo);
            }
            
            // 5. 消息不存在
            logger.warn("获取消息详情失败：消息不存在, messageId: {}", messageId);
            return Result.failure(7101);
            
        } catch (Exception e) {
            logger.error("获取消息详情失败，系统异常", e);
            return Result.failure(7101);
        }
    }
} 