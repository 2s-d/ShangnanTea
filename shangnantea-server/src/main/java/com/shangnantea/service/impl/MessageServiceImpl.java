package com.shangnantea.service.impl;

import com.shangnantea.common.api.Result;
import com.shangnantea.mapper.ShopMapper;
import com.shangnantea.mapper.ChatMessageMapper;
import com.shangnantea.mapper.ChatSessionMapper;
import com.shangnantea.mapper.UserNotificationMapper;
import com.shangnantea.model.entity.message.ChatMessage;
import com.shangnantea.model.entity.message.ChatSession;
import com.shangnantea.model.entity.shop.Shop;
import com.shangnantea.model.entity.message.UserNotification;
import com.shangnantea.model.vo.message.MessageVO;
import com.shangnantea.model.vo.message.NotificationVO;
import com.shangnantea.model.vo.message.UnreadCountVO;
import com.shangnantea.security.context.UserContext;
import com.shangnantea.service.MessageService;
import com.shangnantea.utils.FileUploadUtils;
import com.shangnantea.utils.UserPreferenceRegistry;
import com.shangnantea.utils.NotificationUtils;
import com.shangnantea.utils.StatisticsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private com.shangnantea.mapper.ForumReplyMapper forumReplyMapper;
    
    @Autowired
    private com.shangnantea.mapper.TeaReviewMapper teaReviewMapper;
    
    @Autowired
    private com.shangnantea.mapper.UserFollowMapper userFollowMapper;
    
    @Autowired
    private com.shangnantea.mapper.UserFavoriteMapper userFavoriteMapper;

    @Autowired
    private com.shangnantea.mapper.UserSettingMapper userSettingMapper;
    
    @Autowired
    private ShopMapper shopMapper;
    
    @Autowired
    private com.shangnantea.mapper.UserLikeMapper userLikeMapper;
    
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;
    
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
    public Result<Object> getContacts() {
        try {
            logger.info("获取联系人列表请求");
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取联系人列表失败：用户未登录");
                return Result.failure(7100);
            }
            
            // 2. 查询关注列表（用户+店铺）
            List<com.shangnantea.model.entity.user.UserFollow> followList = userFollowMapper.selectByUserId(userId, null);
            
            // 3. 转换为联系人VO列表（包含在线状态）
            List<Map<String, Object>> contactList = new ArrayList<>();
            for (com.shangnantea.model.entity.user.UserFollow follow : followList) {
                Map<String, Object> contact = new HashMap<>();
                contact.put("id", follow.getFollowId());
                contact.put("type", follow.getFollowType()); // "user" 或 "shop"
                contact.put("name", follow.getTargetName());
                
                // 处理头像/Logo
                String avatar = follow.getTargetAvatar();
                if (avatar != null && !avatar.trim().isEmpty()) {
                    if (avatar.startsWith("http://") || avatar.startsWith("https://")) {
                        contact.put("avatar", avatar);
                    } else {
                        contact.put("avatar", FileUploadUtils.generateAccessUrl(avatar, baseUrl));
                    }
                } else {
                    contact.put("avatar", null);
                }
                
                // 如果是店铺，查询店铺信息获取logo和店主ID
                String onlineUserId = null;
                if ("shop".equals(follow.getFollowType())) {
                    Shop shop = shopMapper.selectById(follow.getFollowId());
                    if (shop != null) {
                        String logo = shop.getLogo();
                        if (logo != null && !logo.trim().isEmpty()) {
                            if (logo.startsWith("http://") || logo.startsWith("https://")) {
                                contact.put("logo", logo);
                            } else {
                                contact.put("logo", FileUploadUtils.generateAccessUrl(logo, baseUrl));
                            }
                        }
                        // 店铺联系人需要 ownerId 用于WebSocket在线状态增量更新
                        contact.put("ownerId", shop.getOwnerId());
                        onlineUserId = shop.getOwnerId(); // 店铺的在线状态看店主
                    }
                } else {
                    onlineUserId = follow.getFollowId(); // 用户的在线状态看自己
                }
                
                // 查询在线状态（由WebSocket维护online:user:* key）
                boolean online = false;
                try {
                    if (redisTemplate != null && onlineUserId != null && !onlineUserId.trim().isEmpty()) {
                        String onlineKey = "online:user:" + onlineUserId;
                        online = Boolean.TRUE.equals(redisTemplate.hasKey(onlineKey));
                    }
                } catch (Exception e) {
                    logger.debug("查询在线状态失败，不影响流程: userId={}, error={}", onlineUserId, e.getMessage());
                }
                contact.put("online", online);
                
                contactList.add(contact);
            }
            
            logger.info("获取联系人列表成功, userId: {}, count: {}", userId, contactList.size());
            return Result.success(200, contactList);
            
        } catch (Exception e) {
            logger.error("获取联系人列表失败，系统异常", e);
            return Result.failure(7100);
        }
    }
    
    @Override
    public Result<Object> searchUsers(String keyword, Integer page, Integer pageSize) {
        try {
            logger.info("全局用户搜索请求, keyword: {}, page: {}, pageSize: {}", keyword, page, pageSize);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("搜索用户失败：用户未登录");
                return Result.failure(7101);
            }
            
            // 2. 参数验证和修正
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10;
            }
            if (pageSize > 50) {
                pageSize = 50; // 限制最大页大小
            }
            
            Integer offset = (page - 1) * pageSize;
            
            // 3. 如果关键词为空，返回空列表
            if (keyword == null || keyword.trim().isEmpty()) {
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("list", new ArrayList<>());
                resultData.put("total", 0);
                resultData.put("page", page);
                resultData.put("pageSize", pageSize);
                return Result.success(200, resultData);
            }
            
            // 4. 搜索用户（支持ID和昵称搜索）
            // 使用UserMapper的selectByPage方法，但只搜索用户（role和status传null）
            List<com.shangnantea.model.entity.user.User> users = userMapper.selectByPage(
                keyword.trim(), null, null, offset, pageSize);
            long total = userMapper.countByCondition(keyword.trim(), null, null);
            
            // 5. 转换为VO列表（包含在线状态）
            List<Map<String, Object>> userList = new ArrayList<>();
            for (com.shangnantea.model.entity.user.User user : users) {
                // 排除自己
                if (user.getId().equals(userId)) {
                    continue;
                }
                
                Map<String, Object> userVO = new HashMap<>();
                userVO.put("id", user.getId());
                userVO.put("username", user.getUsername());
                userVO.put("nickname", user.getNickname());
                
                // 处理头像URL
                String avatar = user.getAvatar();
                if (avatar != null && !avatar.trim().isEmpty()) {
                    if (avatar.startsWith("http://") || avatar.startsWith("https://")) {
                        userVO.put("avatar", avatar);
                    } else {
                        userVO.put("avatar", FileUploadUtils.generateAccessUrl(avatar, baseUrl));
                    }
                } else {
                    userVO.put("avatar", null);
                }
                
                // 查询在线状态（由WebSocket维护online:user:* key）
                boolean online = false;
                try {
                    if (redisTemplate != null) {
                        String onlineKey = "online:user:" + user.getId();
                        online = Boolean.TRUE.equals(redisTemplate.hasKey(onlineKey));
                    }
                } catch (Exception e) {
                    logger.debug("查询在线状态失败，不影响流程: userId={}, error={}", user.getId(), e.getMessage());
                }
                userVO.put("online", online);
                
                // 检查是否已关注
                com.shangnantea.model.entity.user.UserFollow follow = userFollowMapper.selectByUserIdAndFollowId(
                    userId, user.getId(), "user");
                userVO.put("isFollowed", follow != null);
                
                userList.add(userVO);
            }
            
            // 6. 构造返回数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("list", userList);
            resultData.put("total", total);
            resultData.put("page", page);
            resultData.put("pageSize", pageSize);
            
            logger.info("全局用户搜索成功, userId: {}, keyword: {}, total: {}", userId, keyword, total);
            return Result.success(200, resultData);
            
        } catch (Exception e) {
            logger.error("全局用户搜索失败，系统异常", e);
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
            
            // 9. 创建私信通知：给接收者发送通知
            NotificationUtils.createMessageNotification(
                receiverId, senderId, session.getId(), content
            );
            
            // 10. 构造返回数据（转换为MessageVO）
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
            
            // 2. 解析查询参数（兼容 pageSize / size）
            Integer page = params.get("page") != null ?
                Integer.parseInt(params.get("page").toString()) : 1;
            Object pageSizeObj = params.get("pageSize") != null ? params.get("pageSize") : params.get("size");
            Integer pageSize = pageSizeObj != null ? Integer.parseInt(pageSizeObj.toString()) : 10;
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
            // 兼容前端分页回显（不影响老前端）
            resultData.put("page", page);
            resultData.put("pageSize", pageSize);
            
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
            // 返回 code=7003，data=null
            return Result.success(7003);
            
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
        vo.setTargetId(notification.getTargetId());
        vo.setTargetType(notification.getTargetType());
        vo.setExtraData(notification.getExtraData());
        vo.setIsRead(notification.getIsRead() != null ? notification.getIsRead() : 0);
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
            // 返回 code=7004，data=null
            return Result.success(7004);
            
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
            // 返回 code=7005，data=null
            return Result.success(7005);
            
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
                
                // 补充在线状态（由WebSocket维护online:user:* key）
                boolean targetOnline = false;
                try {
                    if (redisTemplate != null && targetUserId != null) {
                        String onlineKey = "online:user:" + targetUserId;
                        targetOnline = Boolean.TRUE.equals(redisTemplate.hasKey(onlineKey));
                    }
                } catch (Exception e) {
                    targetOnline = false;
                }
                sessionVO.put("targetOnline", targetOnline);
                
                // 查询对方用户信息（不返回用户名，只返回昵称和头像）
                com.shangnantea.model.entity.user.User targetUser = userMapper.selectById(targetUserId);
                if (targetUser != null) {
                    // 不返回用户名（敏感数据）
                    sessionVO.put("targetNickname", targetUser.getNickname());
                    // 处理头像URL
                    String targetAvatar = targetUser.getAvatar();
                    if (targetAvatar != null && !targetAvatar.trim().isEmpty()) {
                        if (targetAvatar.startsWith("http://") || targetAvatar.startsWith("https://")) {
                            sessionVO.put("targetAvatar", targetAvatar);
                        } else {
                            sessionVO.put("targetAvatar", FileUploadUtils.generateAccessUrl(targetAvatar, baseUrl));
                        }
                    } else {
                        sessionVO.put("targetAvatar", null);
                    }
                } else {
                    // 不返回用户名（敏感数据）
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
                
                // 对于店铺会话，查询并返回店铺信息（店铺LOGO、名称等）
                if ("shop".equals(session.getSessionType()) || "customer".equals(session.getSessionType())) {
                    try {
                        Shop shop = shopMapper.selectByOwnerId(targetUserId);
                        if (shop != null) {
                            sessionVO.put("shopId", shop.getId());
                            sessionVO.put("shopName", shop.getShopName());
                            // 店铺会话显示店铺LOGO，而不是店主头像
                            String shopLogo = shop.getLogo();
                            if (shopLogo != null && !shopLogo.trim().isEmpty()) {
                                if (shopLogo.startsWith("http://") || shopLogo.startsWith("https://")) {
                                    sessionVO.put("shopAvatar", shopLogo);
                                } else {
                                    sessionVO.put("shopAvatar", FileUploadUtils.generateAccessUrl(shopLogo, baseUrl));
                                }
                            } else {
                                sessionVO.put("shopAvatar", null);
                            }
                            // 店铺会话的名称使用店铺名称
                            sessionVO.put("targetNickname", shop.getShopName());
                            // 店铺会话的头像使用店铺LOGO
                            sessionVO.put("targetAvatar", sessionVO.get("shopAvatar"));
                        }
                    } catch (Exception e) {
                        logger.warn("查询店铺信息失败, ownerId: {}, error: {}", targetUserId, e.getMessage());
                    }
                }
                
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
                
                // 查询发送者用户信息（不返回用户名，只返回昵称和头像）
                com.shangnantea.model.entity.user.User sender = userMapper.selectById(message.getSenderId());
                if (sender != null) {
                    // 不返回用户名（敏感数据）
                    messageVO.put("senderNickname", sender.getNickname());
                    // 处理头像URL
                    String senderAvatar = sender.getAvatar();
                    if (senderAvatar != null && !senderAvatar.trim().isEmpty()) {
                        if (senderAvatar.startsWith("http://") || senderAvatar.startsWith("https://")) {
                            messageVO.put("senderAvatar", senderAvatar);
                        } else {
                            messageVO.put("senderAvatar", FileUploadUtils.generateAccessUrl(senderAvatar, baseUrl));
                        }
                    } else {
                        messageVO.put("senderAvatar", null);
                    }
                } else {
                    // 不返回用户名（敏感数据）
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

            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("创建聊天会话失败：用户未登录");
                return Result.failure(7113);
            }
            if (targetId == null || targetId.trim().isEmpty()) {
                logger.warn("创建聊天会话失败：目标ID为空");
                return Result.failure(7113);
            }

            String type = targetType != null ? targetType.trim().toLowerCase() : "private";
            boolean isCustomer = "shop".equals(type) || "customer".equals(type);

            if (!isCustomer && userId.equals(targetId)) {
                logger.warn("创建聊天会话失败：不能和自己创建会话");
                return Result.failure(7113);
            }

            ChatSession existingSession = null;
            String sessionId;
            String initiatorId;
            String receiverId;
            String normalizedType;

            if (isCustomer) {
                // 店铺客服会话：sessionId = shopId_userId_customer，initiator/receiver 均为用户ID（店主代为回复）
                Shop shop = shopMapper.selectById(targetId);
                if (shop == null) {
                    logger.warn("创建聊天会话失败：店铺不存在, shopId: {}", targetId);
                    return Result.failure(7113);
                }
                String ownerId = shop.getOwnerId();
                if (ownerId == null || userId.equals(ownerId)) {
                    logger.warn("创建聊天会话失败：无效客服会话");
                    return Result.failure(7113);
                }
                sessionId = ChatSessionIdBuilder.customerSessionId(targetId, userId);
                initiatorId = userId;
                receiverId = ownerId;
                normalizedType = "customer";
                existingSession = sessionMapper.selectById(sessionId);
                if (existingSession == null) {
                    existingSession = sessionMapper.selectByUserIdsAnyStatus(userId, ownerId);
                    if (existingSession != null && !"customer".equals(existingSession.getSessionType())) {
                        existingSession = null;
                    }
                }
            } else {
                // 私聊：userId_userId_private，双方均为用户ID
                sessionId = ChatSessionIdBuilder.privateSessionId(userId, targetId);
                initiatorId = userId.compareTo(targetId) <= 0 ? userId : targetId;
                receiverId = userId.compareTo(targetId) <= 0 ? targetId : userId;
                normalizedType = "private";
                existingSession = sessionMapper.selectById(sessionId);
                if (existingSession == null) {
                    existingSession = sessionMapper.selectByUserIdsAnyStatus(userId, targetId);
                    if (existingSession != null && !"private".equals(existingSession.getSessionType())) {
                        existingSession = null;
                    }
                }
            }

            if (existingSession != null) {
                if (existingSession.getStatus() != null && existingSession.getStatus() == 0) {
                    existingSession.setStatus(1);
                    existingSession.setUpdateTime(new Date());
                    sessionMapper.updateById(existingSession);
                    logger.info("恢复已隐藏的会话, sessionId: {}", existingSession.getId());
                }
                                Map<String, Object> sessionVO = new HashMap<>();
                sessionVO.put("id", existingSession.getId());
                String otherUserId = existingSession.getInitiatorId().equals(userId)
                    ? existingSession.getReceiverId() : existingSession.getInitiatorId();
                sessionVO.put("targetUserId", otherUserId);
                sessionVO.put("lastMessage", existingSession.getLastMessage());
                sessionVO.put("lastMessageTime", existingSession.getLastMessageTime());
                sessionVO.put("sessionType", existingSession.getSessionType());
                sessionVO.put("createTime", existingSession.getCreateTime());
                return Result.success(7006, sessionVO);            }

            ChatSession session = new ChatSession();
            session.setId(sessionId);
            session.setInitiatorId(initiatorId);
            session.setReceiverId(receiverId);
            session.setSessionType(normalizedType);
            session.setLastMessage("");
            session.setLastMessageTime(new Date());
            session.setInitiatorUnread(0);
            session.setReceiverUnread(0);
            session.setIsPinned(0);
            session.setStatus(1);
            session.setCreateTime(new Date());
            session.setUpdateTime(new Date());

            int result = sessionMapper.insert(session);
            if (result <= 0) {
                logger.error("创建聊天会话失败：数据库插入失败");
                return Result.failure(7113);
            }

            Map<String, Object> sessionVO = new HashMap<>();
            sessionVO.put("id", session.getId());
            sessionVO.put("targetUserId", isCustomer ? shopMapper.selectById(targetId).getOwnerId() : targetId);
            sessionVO.put("lastMessage", session.getLastMessage());
            sessionVO.put("lastMessageTime", session.getLastMessageTime());
            sessionVO.put("sessionType", session.getSessionType());
            sessionVO.put("createTime", session.getCreateTime());
            logger.info("创建聊天会话成功, sessionId: {}", session.getId());
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
            // 返回 code=7007，data=null
            return Result.success(7007);
            
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
            // 返回 code=7008，data=null
            return Result.success(7008);
            
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
            boolean isSelf = currentUserId != null && currentUserId.equals(userId);
            if (currentUserId != null && !isSelf) {
                com.shangnantea.model.entity.user.UserFollow follow = 
                    userFollowMapper.selectByFollowerAndFollowed(currentUserId, userId);
                isFollowed = (follow != null);
            }
            
            // 4. 读取个人主页可见性（默认允许查看）
            boolean profileVisible = true;
            if (!isSelf) {
                String visibilityKey = UserPreferenceRegistry.DISPLAY_PROFILE_VISIBLE.getKey();
                com.shangnantea.model.entity.user.UserSetting setting =
                    userSettingMapper.selectByUserIdAndKey(userId, visibilityKey);
                if (setting != null) {
                    Object parsed = UserPreferenceRegistry.DISPLAY_PROFILE_VISIBLE.parseStoredValue(
                        setting.getSettingValue()
                    );
                    profileVisible = parsed instanceof Boolean ? (Boolean) parsed : true;
                } else {
                    // 未设置时使用默认值
                    Object parsed = UserPreferenceRegistry.DISPLAY_PROFILE_VISIBLE.parseStoredValue(
                        UserPreferenceRegistry.DISPLAY_PROFILE_VISIBLE.getDefaultValue()
                    );
                    profileVisible = parsed instanceof Boolean ? (Boolean) parsed : true;
                }
            }
            
            // 5. 组装返回数据
            Map<String, Object> userProfile = new HashMap<>();
            
            // 用户基本信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("nickname", user.getNickname());
            // 头像字段：数据库中可能是相对路径，这里统一转换为前端可直接访问的完整URL
            String avatar = user.getAvatar();
            if (avatar != null && !avatar.trim().isEmpty()) {
                if (avatar.startsWith("http://") || avatar.startsWith("https://")) {
                    userInfo.put("avatar", avatar);
                } else {
                    userInfo.put("avatar", FileUploadUtils.generateAccessUrl(avatar, baseUrl));
                }
            } else {
                userInfo.put("avatar", null);
            }
            userInfo.put("role", user.getRole());
            userInfo.put("gender", user.getGender());
            userInfo.put("currentLocation", user.getCurrentLocation());
            userInfo.put("bio", user.getBio());
            userInfo.put("bio", user.getBio());
            userInfo.put("createTime", user.getCreateTime());

            // 仅在本人查看时返回用户名；他人主页一律不暴露 username
            if (isSelf) {
                userInfo.put("username", user.getUsername());
            }
            
            // 如果是商家角色，附带店铺信息
            if (user.getRole() != null && user.getRole() == 3) {
                Shop shop = shopMapper.selectByOwnerId(userId);
                if (shop != null) {
                    userInfo.put("shopId", shop.getId());
                    userInfo.put("shopName", shop.getShopName());
                }
            }
            userProfile.put("user", userInfo);
            
            // 是否已关注
            userProfile.put("isFollowed", isFollowed);
            // 个人主页可见性
            userProfile.put("profileVisible", profileVisible || isSelf);
            
            logger.info("获取用户主页信息成功, userId: {}, profileVisible: {}, isFollowed: {}", 
                    userId, profileVisible, isFollowed);
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
                forumPostMapper.selectByUserId(userId, 0, 10, "latest");
            
            // 3. 转换为动态VO
            List<Map<String, Object>> dynamicList = new ArrayList<>();
            for (com.shangnantea.model.entity.forum.ForumPost post : posts) {
                Map<String, Object> dynamic = new HashMap<>();
                dynamic.put("id", post.getId());
                dynamic.put("type", "post"); // 动态类型：帖子
                dynamic.put("title", post.getTitle());
                dynamic.put("content", post.getSummary() != null ? post.getSummary() : post.getContent());
                // 处理封面图片URL
                String coverImage = post.getCoverImage();
                if (coverImage != null && !coverImage.trim().isEmpty()) {
                    if (coverImage.startsWith("http://") || coverImage.startsWith("https://")) {
                        dynamic.put("coverImage", coverImage);
                    } else {
                        dynamic.put("coverImage", FileUploadUtils.generateAccessUrl(coverImage, baseUrl));
                    }
                } else {
                    dynamic.put("coverImage", null);
                }
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
            
            // 2. 检查主页可见性（默认允许查看）
            String currentUserId = UserContext.getCurrentUserId();
            boolean isSelf = currentUserId != null && currentUserId.equals(userId);
            boolean profileVisible = true;
            if (!isSelf) {
                String visibilityKey = UserPreferenceRegistry.DISPLAY_PROFILE_VISIBLE.getKey();
                com.shangnantea.model.entity.user.UserSetting setting =
                    userSettingMapper.selectByUserIdAndKey(userId, visibilityKey);
                if (setting != null) {
                    Object parsed = UserPreferenceRegistry.DISPLAY_PROFILE_VISIBLE.parseStoredValue(
                        setting.getSettingValue()
                    );
                    profileVisible = parsed instanceof Boolean ? (Boolean) parsed : true;
                } else {
                    Object parsed = UserPreferenceRegistry.DISPLAY_PROFILE_VISIBLE.parseStoredValue(
                        UserPreferenceRegistry.DISPLAY_PROFILE_VISIBLE.getDefaultValue()
                    );
                    profileVisible = parsed instanceof Boolean ? (Boolean) parsed : true;
                }
            }
            
            long postCount = 0L;
            long followingCount = 0L;
            long followerCount = 0L;
            long favoriteCount = 0L;
            long likeCount = 0L;
            
            if (isSelf || profileVisible) {
                // 3. 统计各项数据
                postCount = forumPostMapper.countByUserId(userId);
                
                // 关注数量：用户关注 + 店铺关注
                long userFollowingCount = userFollowMapper.countFollowingByUserId(userId);
                long shopFollowingCount = userFollowMapper.selectByUserId(userId, "shop").size();
                followingCount = userFollowingCount + shopFollowingCount;
                
                followerCount = userFollowMapper.countFollowersByUserId(userId);
                favoriteCount = userFavoriteMapper.countByUserId(userId);
                
                // 获赞数统计：帖子获赞数 + 评论获赞数
                try {
                    // 3.1 统计用户发布的帖子的获赞总数
                    List<com.shangnantea.model.entity.forum.ForumPost> userPosts = 
                        forumPostMapper.selectByUserId(userId, 0, Integer.MAX_VALUE, null);
                    long postLikeCount = 0L;
                    if (userPosts != null && !userPosts.isEmpty()) {
                        for (com.shangnantea.model.entity.forum.ForumPost post : userPosts) {
                            Integer likes = userLikeMapper.countByTarget("post", String.valueOf(post.getId()));
                            if (likes != null) {
                                postLikeCount += likes;
                            }
                        }
                    }
                    
                    // 3.2 统计用户发布的评论的获赞总数
                    List<com.shangnantea.model.entity.forum.ForumReply> userReplies = 
                        forumReplyMapper.selectByUserId(userId);
                    long replyLikeCount = 0L;
                    if (userReplies != null && !userReplies.isEmpty()) {
                        for (com.shangnantea.model.entity.forum.ForumReply reply : userReplies) {
                            Integer likes = userLikeMapper.countByTarget("reply", String.valueOf(reply.getId()));
                            if (likes != null) {
                                replyLikeCount += likes;
                            }
                        }
                    }
                    
                    // 3.3 获赞总数 = 帖子获赞数 + 评论获赞数
                    likeCount = postLikeCount + replyLikeCount;
                } catch (Exception e) {
                    logger.warn("统计获赞数失败, userId: {}", userId, e);
                    likeCount = 0L;
                }
            }
            
            // 4. 组装返回数据
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("postCount", postCount);
            statistics.put("followingCount", followingCount);
            statistics.put("followerCount", followerCount);
            statistics.put("favoriteCount", favoriteCount);
            statistics.put("likeCount", likeCount);
            
            logger.info("获取用户统计数据成功, userId: {}, postCount: {}, followingCount: {}, followerCount: {}, favoriteCount: {}, likeCount: {}", 
                    userId, postCount, followingCount, followerCount, favoriteCount, likeCount);
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
            
            // 1. 获取当前登录用户ID（用于权限校验）
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
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

            // 3. 目标用户ID：可选参数 userId，未传时默认当前登录用户
            String targetUserId = params.get("userId") != null 
                    ? params.get("userId").toString().trim() 
                    : currentUserId;
            if (targetUserId == null || targetUserId.isEmpty()) {
                targetUserId = currentUserId;
            }
            
            // 4. 参数验证和修正
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            if (size > 100) size = 100;
            
            Integer offset = (page - 1) * size;
            
            // 5. 查询用户帖子列表
            List<com.shangnantea.model.entity.forum.ForumPost> posts = 
                forumPostMapper.selectByUserId(targetUserId, offset, size, "latest");
            long total = forumPostMapper.countByUserId(targetUserId);
            
            // 6. 转换为VO
            List<Map<String, Object>> postList = new ArrayList<>();
            for (com.shangnantea.model.entity.forum.ForumPost post : posts) {
                Map<String, Object> postVO = new HashMap<>();
                postVO.put("id", post.getId());
                postVO.put("title", post.getTitle());
                postVO.put("content", post.getContent());
                postVO.put("summary", post.getSummary());
                // 处理封面图片URL
                String coverImage = post.getCoverImage();
                if (coverImage != null && !coverImage.trim().isEmpty()) {
                    if (coverImage.startsWith("http://") || coverImage.startsWith("https://")) {
                        postVO.put("coverImage", coverImage);
                    } else {
                        postVO.put("coverImage", FileUploadUtils.generateAccessUrl(coverImage, baseUrl));
                    }
                } else {
                    postVO.put("coverImage", null);
                }
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
            
            // 7. 构造返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("list", postList);
            result.put("total", total);
            
            logger.info("获取用户帖子列表成功, userId: {}, page: {}, size: {}, sortBy: {}, total: {}", 
                    targetUserId, page, size, sortBy, total);
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
            
            // 1. 获取当前登录用户ID
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                logger.warn("获取用户评价记录失败：用户未登录");
                return Result.failure(7124);
            }
            
            // 2. 解析分页参数
            Integer page = params.get("page") != null ? 
                Integer.parseInt(params.get("page").toString()) : 1;
            Integer size = params.get("size") != null ? 
                Integer.parseInt(params.get("size").toString()) : 10;
            
            // 3. 目标用户ID：可选参数 userId，未传时默认当前登录用户
            String targetUserId = params.get("userId") != null 
                    ? params.get("userId").toString().trim() 
                    : currentUserId;
            if (targetUserId == null || targetUserId.isEmpty()) {
                targetUserId = currentUserId;
            }
            
            // 4. 参数验证和修正
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            if (size > 100) size = 100;
            
            Integer offset = (page - 1) * size;
            
            // 5. 查询用户评价记录
            List<com.shangnantea.model.entity.tea.TeaReview> reviews = 
                teaReviewMapper.selectByUserId(targetUserId, offset, size);
            long total = teaReviewMapper.countByUserId(targetUserId);
            
            // 5. 转换为VO
            List<Map<String, Object>> reviewList = new ArrayList<>();
            for (com.shangnantea.model.entity.tea.TeaReview review : reviews) {
                Map<String, Object> reviewVO = new HashMap<>();
                reviewVO.put("id", review.getId());
                reviewVO.put("teaId", review.getTeaId());
                reviewVO.put("orderId", review.getOrderId());
                reviewVO.put("content", review.getContent());
                reviewVO.put("rating", review.getRating());
                // 处理图片列表URL
                String imagesStr = review.getImages();
                if (imagesStr != null && !imagesStr.trim().isEmpty()) {
                    List<String> imageList = new ArrayList<>();
                    String[] imageArray = imagesStr.split(",");
                    for (String imageUrl : imageArray) {
                        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                            if (imageUrl.trim().startsWith("http://") || imageUrl.trim().startsWith("https://")) {
                                imageList.add(imageUrl.trim());
                            } else {
                                imageList.add(FileUploadUtils.generateAccessUrl(imageUrl.trim(), baseUrl));
                            }
                        }
                    }
                    reviewVO.put("images", imageList);
                } else {
                    reviewVO.put("images", new ArrayList<>());
                }
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
                    targetUserId, page, size, total);
            return Result.success(200, result);
            
        } catch (Exception e) {
            logger.error("获取用户评价记录失败，系统异常", e);
            return Result.failure(7124);
        }
    }
    
    /**
     * 创建或获取会话
     * 
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param sessionTypeInt 会话类型（1-私聊，2-客服等）
     * @return 会话对象
     */
    private ChatSession createOrGetSession(String senderId, String receiverId, Integer sessionTypeInt) {
        try {
            // 1. 先尝试查找已存在的会话
            ChatSession existingSession = sessionMapper.selectByUserIds(senderId, receiverId);
            if (existingSession != null) {
                logger.debug("找到已存在的会话: sessionId={}", existingSession.getId());
                return existingSession;
            }
            
            // 2. 创建新会话
            ChatSession newSession = new ChatSession();
            String sessionId = UUID.randomUUID().toString().replace("-", "");
            newSession.setId(sessionId);
            newSession.setInitiatorId(senderId);
            newSession.setReceiverId(receiverId);
            
            // 转换会话类型：1 -> "private", 2 -> "customer"
            String sessionType = (sessionTypeInt == 1) ? "private" : "customer";
            newSession.setSessionType(sessionType);
            newSession.setStatus(1); // 正常状态
            newSession.setInitiatorUnread(0);
            newSession.setReceiverUnread(0);
            newSession.setIsPinned(0);
            newSession.setCreateTime(new Date());
            newSession.setUpdateTime(new Date());
            
            // 3. 保存到数据库
            int result = sessionMapper.insert(newSession);
            if (result > 0) {
                logger.info("创建新会话成功: sessionId={}, senderId={}, receiverId={}", 
                        sessionId, senderId, receiverId);
                return newSession;
            } else {
                logger.error("创建会话失败: 数据库插入失败");
                return null;
            }
            
        } catch (Exception e) {
            logger.error("创建或获取会话失败: 系统异常", e);
            return null;
        }
    }
} 