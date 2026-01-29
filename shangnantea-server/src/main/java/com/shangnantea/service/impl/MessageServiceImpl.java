package com.shangnantea.service.impl;

import com.shangnantea.common.api.Result;
import com.shangnantea.mapper.ChatMessageMapper;
import com.shangnantea.mapper.ChatSessionMapper;
import com.shangnantea.mapper.UserNotificationMapper;
import com.shangnantea.model.entity.message.ChatMessage;
import com.shangnantea.model.entity.message.ChatSession;
import com.shangnantea.model.entity.message.UserNotification;
import com.shangnantea.model.vo.message.MessageVO;
import com.shangnantea.model.vo.message.NotificationVO;
import com.shangnantea.model.vo.message.UnreadCountVO;
import com.shangnantea.security.context.UserContext;
import com.shangnantea.service.MessageService;
import com.shangnantea.utils.FileUploadUtils;
import com.shangnantea.utils.StatisticsUtils;
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
    
    @Autowired
    private com.shangnantea.mapper.UserMapper userMapper;
    
    @Autowired
    private com.shangnantea.mapper.ForumPostMapper forumPostMapper;
    
    @Autowired
    private com.shangnantea.mapper.TeaReviewMapper teaReviewMapper;
    
    @Autowired
    private com.shangnantea.mapper.UserFollowMapper userFollowMapper;
    
    @Autowired
    private com.shangnantea.mapper.UserFavoriteMapper userFavoriteMapper;
    
    @Autowired
    private StatisticsUtils statisticsUtils;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
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
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> sendMessage(Map<String, Object> data) {
        try {
            logger.info("发送消息请求, data: {}", data);
            
            // 1. 获取当前用户ID（发送者）
            String senderId = UserContext.getCurrentUserId();
            if (senderId == null) {
                logger.warn("发送消息失败：用户未登录");
                return Result.failure(7102);
            }
            
            // 2. 提取并验证参数
            String receiverId = data.get("receiverId") != null ? data.get("receiverId").toString() : null;
            String content = data.get("content") != null ? data.get("content").toString() : null;
            String type = data.get("type") != null ? data.get("type").toString() : "text";
            
            // 3. 参数验证
            if (receiverId == null || receiverId.trim().isEmpty()) {
                logger.warn("发送消息失败：接收者ID为空");
                return Result.failure(7102);
            }
            
            if (content == null || content.trim().isEmpty()) {
                logger.warn("发送消息失败：消息内容为空");
                return Result.failure(7102);
            }
            
            if (content.length() > 1000) {
                logger.warn("发送消息失败：消息内容超过1000字符, length: {}", content.length());
                return Result.failure(7102);
            }
            
            // 4. 验证不能给自己发消息
            if (senderId.equals(receiverId)) {
                logger.warn("发送消息失败：不能给自己发消息, userId: {}", senderId);
                return Result.failure(7102);
            }
            
            // 5. 创建或获取会话
            // 注意：这里简化处理，假设会话类型为1（用户对用户）
            ChatSession session = createOrGetSession(senderId, receiverId, 1);
            if (session == null) {
                logger.error("发送消息失败：创建会话失败, senderId: {}, receiverId: {}", senderId, receiverId);
                return Result.failure(7102);
            }
            
            // 6. 创建消息记录
            ChatMessage message = new ChatMessage();
            message.setSessionId(session.getId());
            message.setSenderId(senderId);
            message.setReceiverId(receiverId);
            message.setContent(content);
            message.setContentType(type);
            message.setIsRead(0); // 未读
            message.setCreateTime(new Date());
            
            // 7. 保存消息到数据库
            int result = messageMapper.insert(message);
            if (result <= 0) {
                logger.error("发送消息失败：数据库插入失败, senderId: {}, receiverId: {}", senderId, receiverId);
                return Result.failure(7102);
            }
            
            // 8. 更新会话的最后消息信息
            session.setLastMessage(type.equals("image") ? "[图片]" : content);
            session.setLastMessageTime(new Date());
            session.setUpdateTime(new Date());
            sessionMapper.updateById(session);
            
            // 9. 构造返回数据（转换为MessageVO）
            MessageVO vo = new MessageVO();
            vo.setId(message.getId());
            vo.setType(type);
            vo.setTitle(null);
            vo.setContent(content);
            vo.setIsRead(false);
            vo.setCreateTime(message.getCreateTime());
            
            logger.info("发送消息成功, senderId: {}, receiverId: {}, messageId: {}", 
                    senderId, receiverId, message.getId());
            return Result.success(7000, vo);
            
        } catch (Exception e) {
            logger.error("发送消息失败，系统异常", e);
            return Result.failure(7102);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> markAsRead(Map<String, Object> data) {
        try {
            logger.info("标记消息已读请求, data: {}", data);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("标记消息已读失败：用户未登录");
                return Result.failure(7103);
            }
            
            // 2. 提取并验证参数
            Object messageIdsObj = data.get("messageIds");
            if (messageIdsObj == null) {
                logger.warn("标记消息已读失败：消息ID列表为空");
                return Result.failure(7103);
            }
            
            // 3. 转换消息ID列表
            List<Long> messageIds = new ArrayList<>();
            if (messageIdsObj instanceof List) {
                List<?> list = (List<?>) messageIdsObj;
                if (list.isEmpty()) {
                    logger.warn("标记消息已读失败：消息ID列表为空");
                    return Result.failure(7103);
                }
                for (Object obj : list) {
                    try {
                        if (obj instanceof Number) {
                            messageIds.add(((Number) obj).longValue());
                        } else {
                            messageIds.add(Long.parseLong(obj.toString()));
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("标记消息已读失败：无效的消息ID, id: {}", obj);
                        return Result.failure(7103);
                    }
                }
            } else {
                logger.warn("标记消息已读失败：消息ID列表格式错误");
                return Result.failure(7103);
            }
            
            // 4. 批量标记消息为已读
            int successCount = 0;
            Date now = new Date();
            
            for (Long messageId : messageIds) {
                // 先尝试从ChatMessage表查询
                ChatMessage chatMessage = messageMapper.selectById(messageId);
                if (chatMessage != null) {
                    // 验证权限：用户必须是接收者
                    if (!userId.equals(chatMessage.getReceiverId())) {
                        logger.warn("标记消息已读失败：无权限标记该消息, userId: {}, messageId: {}", userId, messageId);
                        continue; // 跳过无权限的消息
                    }
                    
                    // 如果消息未读，标记为已读
                    if (chatMessage.getIsRead() == null || chatMessage.getIsRead() == 0) {
                        chatMessage.setIsRead(1);
                        chatMessage.setReadTime(now);
                        messageMapper.updateById(chatMessage);
                        successCount++;
                        logger.debug("聊天消息已标记为已读, messageId: {}", messageId);
                    } else {
                        successCount++; // 已经是已读状态，也算成功
                    }
                    continue;
                }
                
                // 如果不是聊天消息，尝试从UserNotification表查询
                UserNotification notification = notificationMapper.selectById(messageId);
                if (notification != null) {
                    // 验证权限：用户必须是接收者
                    if (!userId.equals(notification.getUserId())) {
                        logger.warn("标记消息已读失败：无权限标记该通知, userId: {}, messageId: {}", userId, messageId);
                        continue; // 跳过无权限的通知
                    }
                    
                    // 如果通知未读，标记为已读
                    if (notification.getIsRead() == null || notification.getIsRead() == 0) {
                        notification.setIsRead(1);
                        notification.setReadTime(now);
                        notificationMapper.updateById(notification);
                        successCount++;
                        logger.debug("通知已标记为已读, messageId: {}", messageId);
                    } else {
                        successCount++; // 已经是已读状态，也算成功
                    }
                    continue;
                }
                
                // 消息不存在
                logger.warn("标记消息已读失败：消息不存在, messageId: {}", messageId);
            }
            
            // 5. 判断是否全部成功
            if (successCount == 0) {
                logger.warn("标记消息已读失败：没有成功标记任何消息, userId: {}", userId);
                return Result.failure(7103);
            }
            
            logger.info("标记消息已读成功, userId: {}, 成功数量: {}/{}", userId, successCount, messageIds.size());
            return Result.success(7001, null);
            
        } catch (Exception e) {
            logger.error("标记消息已读失败，系统异常", e);
            return Result.failure(7103);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> deleteMessages(Map<String, Object> data) {
        try {
            logger.info("删除消息请求, data: {}", data);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("删除消息失败：用户未登录");
                return Result.failure(7104);
            }
            
            // 2. 提取并验证参数
            Object messageIdsObj = data.get("messageIds");
            if (messageIdsObj == null) {
                logger.warn("删除消息失败：消息ID列表为空");
                return Result.failure(7104);
            }
            
            // 3. 转换消息ID列表
            List<Long> messageIds = new ArrayList<>();
            if (messageIdsObj instanceof List) {
                List<?> list = (List<?>) messageIdsObj;
                if (list.isEmpty()) {
                    logger.warn("删除消息失败：消息ID列表为空");
                    return Result.failure(7104);
                }
                for (Object obj : list) {
                    try {
                        if (obj instanceof Number) {
                            messageIds.add(((Number) obj).longValue());
                        } else {
                            messageIds.add(Long.parseLong(obj.toString()));
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("删除消息失败：无效的消息ID, id: {}", obj);
                        return Result.failure(7104);
                    }
                }
            } else {
                logger.warn("删除消息失败：消息ID列表格式错误");
                return Result.failure(7104);
            }
            
            // 4. 批量删除消息
            int successCount = 0;
            
            for (Long messageId : messageIds) {
                // 先尝试从ChatMessage表查询
                ChatMessage chatMessage = messageMapper.selectById(messageId);
                if (chatMessage != null) {
                    // 验证权限：用户必须是发送者或接收者
                    if (!userId.equals(chatMessage.getSenderId()) && !userId.equals(chatMessage.getReceiverId())) {
                        logger.warn("删除消息失败：无权限删除该消息, userId: {}, messageId: {}", userId, messageId);
                        continue; // 跳过无权限的消息
                    }
                    
                    // 删除消息
                    int result = messageMapper.deleteById(messageId);
                    if (result > 0) {
                        successCount++;
                        logger.debug("聊天消息已删除, messageId: {}", messageId);
                    }
                    continue;
                }
                
                // 如果不是聊天消息，尝试从UserNotification表查询
                UserNotification notification = notificationMapper.selectById(messageId);
                if (notification != null) {
                    // 验证权限：用户必须是接收者
                    if (!userId.equals(notification.getUserId())) {
                        logger.warn("删除消息失败：无权限删除该通知, userId: {}, messageId: {}", userId, messageId);
                        continue; // 跳过无权限的通知
                    }
                    
                    // 删除通知
                    int result = notificationMapper.deleteById(messageId);
                    if (result > 0) {
                        successCount++;
                        logger.debug("通知已删除, messageId: {}", messageId);
                    }
                    continue;
                }
                
                // 消息不存在
                logger.warn("删除消息失败：消息不存在, messageId: {}", messageId);
            }
            
            // 5. 判断是否全部失败
            if (successCount == 0) {
                logger.warn("删除消息失败：没有成功删除任何消息, userId: {}", userId);
                return Result.failure(7104);
            }
            
            logger.info("删除消息成功, userId: {}, 成功数量: {}/{}", userId, successCount, messageIds.size());
            return Result.success(7002, null);
            
        } catch (Exception e) {
            logger.error("删除消息失败，系统异常", e);
            return Result.failure(7104);
        }
    }
    
    @Override
    public Result<Object> getUnreadCount() {
        try {
            logger.info("获取未读消息数量请求");
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取未读消息数量失败：用户未登录");
                return Result.failure(7105);
            }
            
            // 2. 统计聊天消息未读数量
            long chatUnreadCount = messageMapper.countUnreadByUserId(userId);
            
            // 3. 统计系统通知未读数量
            long systemUnreadCount = notificationMapper.countUnreadByUserIdAndType(userId, null);
            
            // 4. 计算总数
            long totalUnreadCount = chatUnreadCount + systemUnreadCount;
            
            // 5. 构造返回数据
            UnreadCountVO vo = new UnreadCountVO(
                (int) totalUnreadCount,
                (int) systemUnreadCount,
                (int) chatUnreadCount
            );
            
            logger.info("获取未读消息数量成功, userId: {}, total: {}, system: {}, chat: {}", 
                    userId, totalUnreadCount, systemUnreadCount, chatUnreadCount);
            return Result.success(200, vo);
            
        } catch (Exception e) {
            logger.error("获取未读消息数量失败，系统异常", e);
            return Result.failure(7105);
        }
    }
    
    @Override
    public Result<Object> getNotifications(Map<String, Object> params) {
        try {
            logger.info("获取通知列表请求, params: {}", params);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取通知列表失败：用户未登录");
                return Result.failure(7106);
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
            
            // 4. 查询通知列表
            List<UserNotification> notifications = notificationMapper.selectByUserIdAndType(
                userId, type, offset, pageSize);
            long total = notificationMapper.countByUserIdAndType(userId, type);
            
            // 5. 转换为VO
            List<NotificationVO> notificationList = notifications.stream()
                .map(this::convertNotificationToNotificationVO)
                .collect(Collectors.toList());
            
            // 6. 构造返回数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("list", notificationList);
            resultData.put("total", total);
            
            logger.info("获取通知列表成功, userId: {}, type: {}, total: {}", userId, type, total);
            return Result.success(200, resultData);
            
        } catch (Exception e) {
            logger.error("获取通知列表失败，系统异常", e);
            return Result.failure(7106);
        }
    }
    
    @Override
    public Result<Object> getNotificationDetail(String id) {
        try {
            logger.info("获取通知详情请求, id: {}", id);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取通知详情失败：用户未登录");
                return Result.failure(7107);
            }
            
            // 2. 解析通知ID
            Long notificationId;
            try {
                notificationId = Long.parseLong(id);
            } catch (NumberFormatException e) {
                logger.warn("获取通知详情失败：无效的通知ID, id: {}", id);
                return Result.failure(7107);
            }
            
            // 3. 查询通知
            UserNotification notification = notificationMapper.selectById(notificationId);
            if (notification == null) {
                logger.warn("获取通知详情失败：通知不存在, notificationId: {}", notificationId);
                return Result.failure(7107);
            }
            
            // 4. 验证权限：用户必须是接收者
            if (!userId.equals(notification.getUserId())) {
                logger.warn("获取通知详情失败：无权限访问, userId: {}, notificationId: {}", userId, notificationId);
                return Result.failure(7107);
            }
            
            // 5. 如果通知未读，标记为已读
            if (notification.getIsRead() == null || notification.getIsRead() == 0) {
                notification.setIsRead(1);
                notification.setReadTime(new Date());
                notificationMapper.updateById(notification);
                logger.info("通知已标记为已读, notificationId: {}", notificationId);
            }
            
            // 6. 转换为VO并返回
            NotificationVO vo = convertNotificationToNotificationVO(notification);
            logger.info("获取通知详情成功, userId: {}, notificationId: {}", userId, notificationId);
            return Result.success(200, vo);
            
        } catch (Exception e) {
            logger.error("获取通知详情失败，系统异常", e);
            return Result.failure(7107);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deleteNotification(String id) {
        try {
            logger.info("删除通知请求, id: {}", id);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("删除通知失败：用户未登录");
                return Result.failure(7108);
            }
            
            // 2. 解析通知ID
            Long notificationId;
            try {
                notificationId = Long.parseLong(id);
            } catch (NumberFormatException e) {
                logger.warn("删除通知失败：无效的通知ID, id: {}", id);
                return Result.failure(7108);
            }
            
            // 3. 查询通知
            UserNotification notification = notificationMapper.selectById(notificationId);
            if (notification == null) {
                logger.warn("删除通知失败：通知不存在, notificationId: {}", notificationId);
                return Result.failure(7108);
            }
            
            // 4. 验证权限：用户必须是接收者
            if (!userId.equals(notification.getUserId())) {
                logger.warn("删除通知失败：无权限删除, userId: {}, notificationId: {}", userId, notificationId);
                return Result.failure(7108);
            }
            
            // 5. 删除通知
            int result = notificationMapper.deleteById(notificationId);
            if (result <= 0) {
                logger.error("删除通知失败：数据库删除失败, notificationId: {}", notificationId);
                return Result.failure(7108);
            }
            
            logger.info("删除通知成功, userId: {}, notificationId: {}", userId, notificationId);
            return Result.success(7003, true);
            
        } catch (Exception e) {
            logger.error("删除通知失败，系统异常", e);
            return Result.failure(7108);
        }
    }
    
    /**
     * 将UserNotification转换为NotificationVO
     */
    private NotificationVO convertNotificationToNotificationVO(UserNotification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setType(notification.getType());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setIsRead(notification.getIsRead() != null && notification.getIsRead() == 1);
        vo.setCreateTime(notification.getCreateTime());
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> batchMarkNotificationsAsRead(List<Long> ids) {
        try {
            logger.info("批量标记通知已读请求, ids: {}", ids);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("批量标记通知已读失败：用户未登录");
                return Result.failure(7109);
            }
            
            // 2. 验证参数
            if (ids == null || ids.isEmpty()) {
                logger.warn("批量标记通知已读失败：通知ID列表为空");
                return Result.failure(7109);
            }
            
            // 3. 批量标记为已读
            int successCount = 0;
            Date now = new Date();
            
            for (Long id : ids) {
                UserNotification notification = notificationMapper.selectById(id);
                if (notification == null) {
                    logger.warn("批量标记通知已读：通知不存在, id: {}", id);
                    continue;
                }
                
                // 验证权限：用户必须是接收者
                if (!userId.equals(notification.getUserId())) {
                    logger.warn("批量标记通知已读：无权限标记该通知, userId: {}, id: {}", userId, id);
                    continue;
                }
                
                // 如果通知未读，标记为已读
                if (notification.getIsRead() == null || notification.getIsRead() == 0) {
                    notification.setIsRead(1);
                    notification.setReadTime(now);
                    notificationMapper.updateById(notification);
                    successCount++;
                } else {
                    successCount++; // 已经是已读状态，也算成功
                }
            }
            
            // 4. 判断是否全部失败
            if (successCount == 0) {
                logger.warn("批量标记通知已读失败：没有成功标记任何通知, userId: {}", userId);
                return Result.failure(7109);
            }
            
            logger.info("批量标记通知已读成功, userId: {}, 成功数量: {}/{}", userId, successCount, ids.size());
            return Result.success(7004, true);
            
        } catch (Exception e) {
            logger.error("批量标记通知已读失败，系统异常", e);
            return Result.failure(7109);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> batchDeleteNotifications(List<Long> ids) {
        try {
            logger.info("批量删除通知请求, ids: {}", ids);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("批量删除通知失败：用户未登录");
                return Result.failure(7110);
            }
            
            // 2. 验证参数
            if (ids == null || ids.isEmpty()) {
                logger.warn("批量删除通知失败：通知ID列表为空");
                return Result.failure(7110);
            }
            
            // 3. 批量删除通知
            int successCount = 0;
            
            for (Long id : ids) {
                UserNotification notification = notificationMapper.selectById(id);
                if (notification == null) {
                    logger.warn("批量删除通知：通知不存在, id: {}", id);
                    continue;
                }
                
                // 验证权限：用户必须是接收者
                if (!userId.equals(notification.getUserId())) {
                    logger.warn("批量删除通知：无权限删除该通知, userId: {}, id: {}", userId, id);
                    continue;
                }
                
                // 删除通知
                int result = notificationMapper.deleteById(id);
                if (result > 0) {
                    successCount++;
                }
            }
            
            // 4. 判断是否全部失败
            if (successCount == 0) {
                logger.warn("批量删除通知失败：没有成功删除任何通知, userId: {}", userId);
                return Result.failure(7110);
            }
            
            logger.info("批量删除通知成功, userId: {}, 成功数量: {}/{}", userId, successCount, ids.size());
            return Result.success(7005, true);
            
        } catch (Exception e) {
            logger.error("批量删除通知失败，系统异常", e);
            return Result.failure(7110);
        }
    }
    
    @Override
    public Result<Object> getChatSessions() {
        try {
            logger.info("获取聊天会话列表请求");
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取聊天会话列表失败：用户未登录");
                return Result.failure(7111);
            }
            
            // 2. 查询用户的所有会话（作为发起者或接收者）
            List<ChatSession> sessions = sessionMapper.selectByUserId(userId);
            
            // 3. 转换为VO，并查询对方用户信息
            List<Map<String, Object>> sessionList = new ArrayList<>();
            for (ChatSession session : sessions) {
                Map<String, Object> sessionVO = new HashMap<>();
                sessionVO.put("id", session.getId());
                
                // 确定对方用户ID
                String targetUserId = userId.equals(session.getInitiatorId()) ? 
                    session.getReceiverId() : session.getInitiatorId();
                sessionVO.put("targetUserId", targetUserId);
                
                // 查询对方用户信息
                com.shangnantea.model.entity.user.User targetUser = userMapper.selectById(targetUserId);
                if (targetUser != null) {
                    sessionVO.put("targetUsername", targetUser.getUsername());
                    sessionVO.put("targetNickname", targetUser.getNickname());
                    sessionVO.put("targetAvatar", targetUser.getAvatar());
                } else {
                    sessionVO.put("targetUsername", "未知用户");
                    sessionVO.put("targetNickname", "未知用户");
                    sessionVO.put("targetAvatar", null);
                }
                
                // 确定未读数
                Integer unreadCount = userId.equals(session.getInitiatorId()) ? 
                    session.getInitiatorUnread() : session.getReceiverUnread();
                sessionVO.put("unreadCount", unreadCount != null ? unreadCount : 0);
                
                // 添加置顶状态
                sessionVO.put("isPinned", session.getIsPinned() != null && session.getIsPinned() == 1);
                
                sessionVO.put("lastMessage", session.getLastMessage());
                sessionVO.put("lastMessageTime", session.getLastMessageTime());
                sessionVO.put("sessionType", session.getSessionType());
                sessionVO.put("createTime", session.getCreateTime());
                
                sessionList.add(sessionVO);
            }
            
            // 4. 数据库已按置顶状态和时间排序，无需再次排序
            
            logger.info("获取聊天会话列表成功, userId: {}, count: {}", userId, sessionList.size());
            return Result.success(200, sessionList);
            
        } catch (Exception e) {
            logger.error("获取聊天会话列表失败，系统异常", e);
            return Result.failure(7111);
        }
    }
    
    @Override
    public Result<Object> getChatHistory(Map<String, Object> params) {
        try {
            logger.info("获取聊天记录请求, params: {}", params);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取聊天记录失败：用户未登录");
                return Result.failure(7112);
            }
            
            // 2. 解析参数
            String sessionId = params.get("sessionId") != null ? 
                params.get("sessionId").toString() : null;
            Integer page = params.get("page") != null ? 
                Integer.parseInt(params.get("page").toString()) : 1;
            Integer pageSize = params.get("pageSize") != null ? 
                Integer.parseInt(params.get("pageSize").toString()) : 20;
            
            // 3. 验证参数
            if (sessionId == null || sessionId.trim().isEmpty()) {
                logger.warn("获取聊天记录失败：会话ID为空");
                return Result.failure(7112);
            }
            
            // 参数修正
            if (page < 1) page = 1;
            if (pageSize < 1) pageSize = 20;
            if (pageSize > 100) pageSize = 100;
            
            // 4. 验证用户是否有权限查看该会话
            ChatSession session = sessionMapper.selectById(sessionId);
            if (session == null) {
                logger.warn("获取聊天记录失败：会话不存在, sessionId: {}", sessionId);
                return Result.failure(7112);
            }
            
            if (!userId.equals(session.getInitiatorId()) && !userId.equals(session.getReceiverId())) {
                logger.warn("获取聊天记录失败：无权限访问该会话, userId: {}, sessionId: {}", userId, sessionId);
                return Result.failure(7112);
            }
            
            // 5. 查询聊天记录
            Integer offset = (page - 1) * pageSize;
            List<ChatMessage> messages = messageMapper.selectBySessionId(sessionId, offset, pageSize);
            long total = messageMapper.countBySessionId(sessionId);
            
            // 6. 转换为VO，并查询发送者用户信息
            List<Map<String, Object>> messageList = new ArrayList<>();
            for (ChatMessage message : messages) {
                Map<String, Object> messageVO = new HashMap<>();
                messageVO.put("id", message.getId());
                messageVO.put("sessionId", message.getSessionId());
                messageVO.put("senderId", message.getSenderId());
                messageVO.put("receiverId", message.getReceiverId());
                
                // 查询发送者用户信息
                com.shangnantea.model.entity.user.User sender = userMapper.selectById(message.getSenderId());
                if (sender != null) {
                    messageVO.put("senderUsername", sender.getUsername());
                    messageVO.put("senderNickname", sender.getNickname());
                    messageVO.put("senderAvatar", sender.getAvatar());
                } else {
                    messageVO.put("senderUsername", "未知用户");
                    messageVO.put("senderNickname", "未知用户");
                    messageVO.put("senderAvatar", null);
                }
                
                messageVO.put("content", message.getContent());
                messageVO.put("contentType", message.getContentType());
                messageVO.put("isRead", message.getIsRead() != null && message.getIsRead() == 1);
                messageVO.put("createTime", message.getCreateTime());
                
                messageList.add(messageVO);
            }
            
            // 7. 构造返回数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("list", messageList);
            resultData.put("total", total);
            
            logger.info("获取聊天记录成功, userId: {}, sessionId: {}, total: {}", userId, sessionId, total);
            return Result.success(200, resultData);
            
        } catch (Exception e) {
            logger.error("获取聊天记录失败，系统异常", e);
            return Result.failure(7112);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> createChatSession(String targetId, String targetType) {
        try {
            logger.info("创建聊天会话请求, targetId: {}, targetType: {}", targetId, targetType);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("创建聊天会话失败：用户未登录");
                return Result.failure(7113);
            }
            
            // 2. 验证参数
            if (targetId == null || targetId.trim().isEmpty()) {
                logger.warn("创建聊天会话失败：目标用户ID为空");
                return Result.failure(7113);
            }
            
            // 3. 验证不能和自己创建会话
            if (userId.equals(targetId)) {
                logger.warn("创建聊天会话失败：不能和自己创建会话, userId: {}", userId);
                return Result.failure(7113);
            }
            
            // 4. 检查是否已存在会话
            ChatSession existingSession = sessionMapper.selectByUserIds(userId, targetId);
            if (existingSession != null) {
                logger.info("会话已存在，返回现有会话, sessionId: {}", existingSession.getId());
                
                // 构造返回数据
                Map<String, Object> sessionVO = new HashMap<>();
                sessionVO.put("id", existingSession.getId());
                sessionVO.put("targetUserId", targetId);
                sessionVO.put("lastMessage", existingSession.getLastMessage());
                sessionVO.put("lastMessageTime", existingSession.getLastMessageTime());
                sessionVO.put("sessionType", existingSession.getSessionType());
                sessionVO.put("createTime", existingSession.getCreateTime());
                
                return Result.success(7006, sessionVO);
            }
            
            // 5. 创建新会话
            ChatSession session = new ChatSession();
            session.setId(UUID.randomUUID().toString());
            session.setInitiatorId(userId);
            session.setReceiverId(targetId);
            session.setSessionType(targetType != null ? targetType : "private");
            session.setLastMessage("");
            session.setLastMessageTime(new Date());
            session.setInitiatorUnread(0);
            session.setReceiverUnread(0);
            session.setIsPinned(0); // 默认不置顶
            session.setStatus(1);
            session.setCreateTime(new Date());
            session.setUpdateTime(new Date());
            
            int result = sessionMapper.insert(session);
            if (result <= 0) {
                logger.error("创建聊天会话失败：数据库插入失败, userId: {}, targetId: {}", userId, targetId);
                return Result.failure(7113);
            }
            
            // 6. 构造返回数据
            Map<String, Object> sessionVO = new HashMap<>();
            sessionVO.put("id", session.getId());
            sessionVO.put("targetUserId", targetId);
            sessionVO.put("lastMessage", session.getLastMessage());
            sessionVO.put("lastMessageTime", session.getLastMessageTime());
            sessionVO.put("sessionType", session.getSessionType());
            sessionVO.put("createTime", session.getCreateTime());
            
            logger.info("创建聊天会话成功, userId: {}, targetId: {}, sessionId: {}", 
                    userId, targetId, session.getId());
            return Result.success(7006, sessionVO);
            
        } catch (Exception e) {
            logger.error("创建聊天会话失败，系统异常", e);
            return Result.failure(7113);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> pinChatSession(String sessionId) {
        try {
            logger.info("置顶聊天会话请求, sessionId: {}", sessionId);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("置顶聊天会话失败：用户未登录");
                return Result.failure(7114);
            }
            
            // 2. 验证参数
            if (sessionId == null || sessionId.trim().isEmpty()) {
                logger.warn("置顶聊天会话失败：会话ID为空");
                return Result.failure(7114);
            }
            
            // 3. 验证会话是否存在
            ChatSession session = sessionMapper.selectById(sessionId);
            if (session == null) {
                logger.warn("置顶聊天会话失败：会话不存在, sessionId: {}", sessionId);
                return Result.failure(7114);
            }
            
            // 4. 验证用户是否有权限操作该会话
            if (!userId.equals(session.getInitiatorId()) && !userId.equals(session.getReceiverId())) {
                logger.warn("置顶聊天会话失败：无权限操作该会话, userId: {}, sessionId: {}", userId, sessionId);
                return Result.failure(7114);
            }
            
            // 5. 切换置顶状态
            Integer currentPinned = session.getIsPinned();
            Integer newPinned = (currentPinned != null && currentPinned == 1) ? 0 : 1;
            
            session.setIsPinned(newPinned);
            session.setUpdateTime(new Date());
            
            int result = sessionMapper.updateById(session);
            if (result <= 0) {
                logger.error("置顶聊天会话失败：数据库更新失败, sessionId: {}", sessionId);
                return Result.failure(7114);
            }
            
            logger.info("置顶聊天会话成功, userId: {}, sessionId: {}, isPinned: {}", userId, sessionId, newPinned);
            return Result.success(7007, true);
            
        } catch (Exception e) {
            logger.error("置顶聊天会话失败，系统异常", e);
            return Result.failure(7114);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deleteChatSession(String sessionId) {
        try {
            logger.info("删除聊天会话请求, sessionId: {}", sessionId);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("删除聊天会话失败：用户未登录");
                return Result.failure(7115);
            }
            
            // 2. 验证参数
            if (sessionId == null || sessionId.trim().isEmpty()) {
                logger.warn("删除聊天会话失败：会话ID为空");
                return Result.failure(7115);
            }
            
            // 3. 验证会话是否存在
            ChatSession session = sessionMapper.selectById(sessionId);
            if (session == null) {
                logger.warn("删除聊天会话失败：会话不存在, sessionId: {}", sessionId);
                return Result.failure(7115);
            }
            
            // 4. 验证用户是否有权限删除该会话
            if (!userId.equals(session.getInitiatorId()) && !userId.equals(session.getReceiverId())) {
                logger.warn("删除聊天会话失败：无权限删除该会话, userId: {}, sessionId: {}", userId, sessionId);
                return Result.failure(7115);
            }
            
            // 5. 删除会话（软删除，更新status为0）
            session.setStatus(0);
            session.setUpdateTime(new Date());
            int result = sessionMapper.updateById(session);
            if (result <= 0) {
                logger.error("删除聊天会话失败：数据库更新失败, sessionId: {}", sessionId);
                return Result.failure(7115);
            }
            
            logger.info("删除聊天会话成功, userId: {}, sessionId: {}", userId, sessionId);
            return Result.success(7008, true);
            
        } catch (Exception e) {
            logger.error("删除聊天会话失败，系统异常", e);
            return Result.failure(7115);
        }
    }
    
    @Override
    public Result<Object> getUserProfile(String userId) {
        try {
            logger.info("获取用户主页信息请求, userId: {}", userId);
            
            // 1. 验证参数
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("获取用户主页信息失败：用户ID为空");
                return Result.failure(7119);
            }
            
            // 2. 查询用户基本信息
            com.shangnantea.model.entity.user.User user = userMapper.selectById(userId);
            if (user == null) {
                logger.warn("获取用户主页信息失败：用户不存在, userId: {}", userId);
                return Result.failure(7120);
            }
            
            // 3. 获取当前登录用户ID，检查是否已关注
            String currentUserId = UserContext.getCurrentUserId();
            boolean isFollowed = false;
            if (currentUserId != null && !currentUserId.equals(userId)) {
                com.shangnantea.model.entity.user.UserFollow follow = 
                    userFollowMapper.selectByFollowerAndFollowed(currentUserId, userId);
                isFollowed = (follow != null);
            }
            
            // 4. 统计数据
            long postCount = forumPostMapper.countByUserId(userId);
            long followingCount = userFollowMapper.countFollowingByUserId(userId);
            long followerCount = userFollowMapper.countFollowersByUserId(userId);
            
            // 5. 组装返回数据
            Map<String, Object> userProfile = new HashMap<>();
            
            // 用户基本信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("role", user.getRole());
            userInfo.put("bio", user.getBio());
            userProfile.put("user", userInfo);
            
            // 是否已关注
            userProfile.put("isFollowed", isFollowed);
            
            // 统计数据
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("postCount", postCount);
            statistics.put("followingCount", followingCount);
            statistics.put("followerCount", followerCount);
            userProfile.put("statistics", statistics);
            
            logger.info("获取用户主页信息成功, userId: {}, postCount: {}, followingCount: {}, followerCount: {}", 
                    userId, postCount, followingCount, followerCount);
            return Result.success(200, userProfile);
            
        } catch (Exception e) {
            logger.error("获取用户主页信息失败，系统异常", e);
            return Result.failure(7119);
        }
    }
    
    @Override
    public Result<Object> getUserDynamic(String userId) {
        try {
            logger.info("获取用户动态请求, userId: {}", userId);
            
            // 1. 验证参数
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("获取用户动态失败：用户ID为空");
                return Result.failure(7121);
            }
            
            // 2. 查询用户最新帖子（作为动态）
            // 查询最近10条帖子
            List<com.shangnantea.model.entity.forum.ForumPost> posts = 
                forumPostMapper.selectByUserId(userId, 0, 10);
            
            // 3. 转换为动态VO
            List<Map<String, Object>> dynamicList = new ArrayList<>();
            for (com.shangnantea.model.entity.forum.ForumPost post : posts) {
                Map<String, Object> dynamic = new HashMap<>();
                dynamic.put("id", post.getId());
                dynamic.put("type", "post"); // 动态类型：帖子
                dynamic.put("title", post.getTitle());
                dynamic.put("content", post.getSummary() != null ? post.getSummary() : post.getContent());
                dynamic.put("coverImage", post.getCoverImage());
                dynamic.put("viewCount", post.getViewCount());
                // 使用动态计算获取点赞数
                dynamic.put("likeCount", statisticsUtils.getLikeCount("post", String.valueOf(post.getId())));
                dynamic.put("replyCount", post.getReplyCount());
                dynamic.put("createTime", post.getCreateTime());
                
                dynamicList.add(dynamic);
            }
            
            // 4. 构造返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("list", dynamicList);
            result.put("total", dynamicList.size());
            
            logger.info("获取用户动态成功, userId: {}, count: {}", userId, dynamicList.size());
            return Result.success(200, result);
            
        } catch (Exception e) {
            logger.error("获取用户动态失败，系统异常", e);
            return Result.failure(7121);
        }
    }
    
    @Override
    public Result<Object> getUserStatistics(String userId) {
        try {
            logger.info("获取用户统计数据请求, userId: {}", userId);
            
            // 1. 验证参数
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("获取用户统计数据失败：用户ID为空");
                return Result.failure(7122);
            }
            
            // 2. 统计各项数据
            long postCount = forumPostMapper.countByUserId(userId);
            long followingCount = userFollowMapper.countFollowingByUserId(userId);
            long followerCount = userFollowMapper.countFollowersByUserId(userId);
            long favoriteCount = userFavoriteMapper.countByUserId(userId);
            
            // 3. 组装返回数据
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("postCount", postCount);
            statistics.put("followingCount", followingCount);
            statistics.put("followerCount", followerCount);
            statistics.put("favoriteCount", favoriteCount);
            
            logger.info("获取用户统计数据成功, userId: {}, postCount: {}, followingCount: {}, followerCount: {}, favoriteCount: {}", 
                    userId, postCount, followingCount, followerCount, favoriteCount);
            return Result.success(200, statistics);
            
        } catch (Exception e) {
            logger.error("获取用户统计数据失败，系统异常", e);
            return Result.failure(7122);
        }
    }
    
    @Override
    public Result<Object> getUserPosts(Map<String, Object> params) {
        try {
            logger.info("获取用户帖子列表请求, params: {}", params);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取用户帖子列表失败：用户未登录");
                return Result.failure(7123);
            }
            
            // 2. 解析分页参数
            Integer page = params.get("page") != null ? 
                Integer.parseInt(params.get("page").toString()) : 1;
            Integer size = params.get("size") != null ? 
                Integer.parseInt(params.get("size").toString()) : 10;
            String sortBy = params.get("sortBy") != null ? 
                params.get("sortBy").toString() : "latest";
            
            // 3. 参数验证和修正
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            if (size > 100) size = 100;
            
            Integer offset = (page - 1) * size;
            
            // 4. 查询用户帖子列表
            List<com.shangnantea.model.entity.forum.ForumPost> posts = 
                forumPostMapper.selectByUserId(userId, offset, size);
            long total = forumPostMapper.countByUserId(userId);
            
            // 5. 转换为VO
            List<Map<String, Object>> postList = new ArrayList<>();
            for (com.shangnantea.model.entity.forum.ForumPost post : posts) {
                Map<String, Object> postVO = new HashMap<>();
                postVO.put("id", post.getId());
                postVO.put("title", post.getTitle());
                postVO.put("content", post.getContent());
                postVO.put("summary", post.getSummary());
                postVO.put("coverImage", post.getCoverImage());
                postVO.put("viewCount", post.getViewCount());
                postVO.put("replyCount", post.getReplyCount());
                // 使用动态计算获取点赞数和收藏数
                postVO.put("likeCount", statisticsUtils.getLikeCount("post", String.valueOf(post.getId())));
                postVO.put("favoriteCount", statisticsUtils.getFavoriteCount("post", String.valueOf(post.getId())));
                postVO.put("isSticky", post.getIsSticky());
                postVO.put("isEssence", post.getIsEssence());
                postVO.put("status", post.getStatus());
                postVO.put("createTime", post.getCreateTime());
                postVO.put("updateTime", post.getUpdateTime());
                
                postList.add(postVO);
            }
            
            // 6. 构造返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("list", postList);
            result.put("total", total);
            
            logger.info("获取用户帖子列表成功, userId: {}, page: {}, size: {}, sortBy: {}, total: {}", 
                    userId, page, size, sortBy, total);
            return Result.success(200, result);
            
        } catch (Exception e) {
            logger.error("获取用户帖子列表失败，系统异常", e);
            return Result.failure(7123);
        }
    }
    
    @Override
    public Result<Object> getUserReviews(Map<String, Object> params) {
        try {
            logger.info("获取用户评价记录请求, params: {}", params);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取用户评价记录失败：用户未登录");
                return Result.failure(7124);
            }
            
            // 2. 解析分页参数
            Integer page = params.get("page") != null ? 
                Integer.parseInt(params.get("page").toString()) : 1;
            Integer size = params.get("size") != null ? 
                Integer.parseInt(params.get("size").toString()) : 10;
            
            // 3. 参数验证和修正
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            if (size > 100) size = 100;
            
            Integer offset = (page - 1) * size;
            
            // 4. 查询用户评价记录
            List<com.shangnantea.model.entity.tea.TeaReview> reviews = 
                teaReviewMapper.selectByUserId(userId, offset, size);
            long total = teaReviewMapper.countByUserId(userId);
            
            // 5. 转换为VO
            List<Map<String, Object>> reviewList = new ArrayList<>();
            for (com.shangnantea.model.entity.tea.TeaReview review : reviews) {
                Map<String, Object> reviewVO = new HashMap<>();
                reviewVO.put("id", review.getId());
                reviewVO.put("teaId", review.getTeaId());
                reviewVO.put("orderId", review.getOrderId());
                reviewVO.put("content", review.getContent());
                reviewVO.put("rating", review.getRating());
                reviewVO.put("images", review.getImages());
                reviewVO.put("reply", review.getReply());
                reviewVO.put("replyTime", review.getReplyTime());
                reviewVO.put("isAnonymous", review.getIsAnonymous());
                // 使用动态计算获取点赞数
                reviewVO.put("likeCount", statisticsUtils.getLikeCount("review", String.valueOf(review.getId())));
                reviewVO.put("createTime", review.getCreateTime());
                reviewVO.put("updateTime", review.getUpdateTime());
                
                reviewList.add(reviewVO);
            }
            
            // 6. 构造返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("list", reviewList);
            result.put("total", total);
            
            logger.info("获取用户评价记录成功, userId: {}, page: {}, size: {}, total: {}", 
                    userId, page, size, total);
            return Result.success(200, result);
            
        } catch (Exception e) {
            logger.error("获取用户评价记录失败，系统异常", e);
            return Result.failure(7124);
        }
    }
} 