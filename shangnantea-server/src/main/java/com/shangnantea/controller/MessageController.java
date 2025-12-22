package com.shangnantea.controller;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.common.api.Result;
import com.shangnantea.model.entity.message.ChatMessage;
import com.shangnantea.model.entity.message.ChatSession;
import com.shangnantea.model.entity.message.UserNotification;
import com.shangnantea.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制器
 */
@RestController
@RequestMapping({"/api/messages", "/api/message"})
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 获取当前用户的聊天会话列表
     */
    @GetMapping("/sessions")
    public Result<List<ChatSession>> listSessions() {
        // TODO: 获取当前登录用户ID
        String userId = "当前登录用户ID"; // 应从登录信息中获取
        
        // 返回测试数据 - Task Group A
        java.util.List<ChatSession> sessions = new java.util.ArrayList<>();
        
        // 会话1：好友聊天
        ChatSession session1 = new ChatSession();
        session1.setId("session_1");
        session1.setInitiatorId(userId);
        session1.setReceiverId("user_123");
        session1.setLastMessage("你好，请问这款龙井茶怎么样？");
        session1.setLastMessageTime(new java.util.Date(System.currentTimeMillis() - 3600000)); // 1小时前
        session1.setInitiatorUnread(0);
        session1.setReceiverUnread(2);
        session1.setSessionType("private");
        session1.setStatus(1);
        session1.setCreateTime(new java.util.Date(System.currentTimeMillis() - 86400000)); // 1天前
        sessions.add(session1);
        
        // 会话2：店铺客服
        ChatSession session2 = new ChatSession();
        session2.setId("session_2");
        session2.setInitiatorId(userId);
        session2.setReceiverId("shop_101");
        session2.setLastMessage("您好，有什么可以帮到您的吗？");
        session2.setLastMessageTime(new java.util.Date(System.currentTimeMillis() - 7200000)); // 2小时前
        session2.setInitiatorUnread(0);
        session2.setReceiverUnread(0);
        session2.setSessionType("customer");
        session2.setStatus(1);
        session2.setCreateTime(new java.util.Date(System.currentTimeMillis() - 172800000)); // 2天前
        sessions.add(session2);
        
        // 会话3：另一个好友
        ChatSession session3 = new ChatSession();
        session3.setId("session_3");
        session3.setInitiatorId(userId);
        session3.setReceiverId("user_456");
        session3.setLastMessage("谢谢你的推荐！");
        session3.setLastMessageTime(new java.util.Date(System.currentTimeMillis() - 14400000)); // 4小时前
        session3.setInitiatorUnread(0);
        session3.setReceiverUnread(0);
        session3.setSessionType("private");
        session3.setStatus(1);
        session3.setCreateTime(new java.util.Date(System.currentTimeMillis() - 259200000)); // 3天前
        sessions.add(session3);
        
        return Result.success(sessions);
    }

    /**
     * 获取会话详情
     */
    @GetMapping("/sessions/{id}")
    public Result<ChatSession> getSessionById(@PathVariable Long id) {
        return Result.success(messageService.getSessionById(id));
    }

    /**
     * 创建或获取与指定用户/商家的会话
     */
    @PostMapping("/sessions")
    public Result<ChatSession> createOrGetSession(@RequestParam String targetId, @RequestParam String targetType) {
        // TODO: 获取当前登录用户ID
        String userId = "当前登录用户ID"; // 应从登录信息中获取
        
        // 返回测试数据 - Task Group A
        ChatSession session = new ChatSession();
        session.setId("session_" + System.currentTimeMillis()); // 使用时间戳作为ID
        session.setInitiatorId(userId);
        session.setReceiverId(targetId);
        session.setCreateTime(new java.util.Date());
        session.setInitiatorUnread(0);
        session.setReceiverUnread(0);
        session.setLastMessageTime(new java.util.Date());
        session.setStatus(1);
        
        // 根据targetType设置不同的会话类型和消息
        if ("shop".equals(targetType)) {
            session.setSessionType("customer");
            session.setLastMessage("您好，有什么可以帮到您的吗？");
        } else if ("user".equals(targetType)) {
            session.setSessionType("private");
            session.setLastMessage("");
        }
        
        return Result.success(session);
    }

    /**
     * 获取会话的消息列表
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<PageResult<ChatMessage>> listMessages(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        
        // 返回测试数据 - Task Group A
        PageResult<ChatMessage> result = new PageResult<>();
        result.setTotal(15L);
        result.setPageNum(page);
        result.setPageSize(size);
        
        java.util.List<ChatMessage> messages = new java.util.ArrayList<>();
        
        // 根据sessionId生成不同的测试消息
        String sessionIdStr = sessionId.toString();
        if (sessionId == 1L) {
            // 会话1的消息（与茶友小王）
            ChatMessage msg1 = new ChatMessage();
            msg1.setId(1L);
            msg1.setSessionId("session_1");
            msg1.setSenderId("user_123");
            msg1.setReceiverId("当前登录用户ID");
            msg1.setContent("你好，请问这款龙井茶怎么样？");
            msg1.setContentType("text");
            msg1.setCreateTime(new java.util.Date(System.currentTimeMillis() - 3600000));
            msg1.setIsRead(0);
            messages.add(msg1);
            
            ChatMessage msg2 = new ChatMessage();
            msg2.setId(2L);
            msg2.setSessionId("session_1");
            msg2.setSenderId("当前登录用户ID");
            msg2.setReceiverId("user_123");
            msg2.setContent("这款龙井茶品质很不错，口感清香回甘，值得推荐！");
            msg2.setContentType("text");
            msg2.setCreateTime(new java.util.Date(System.currentTimeMillis() - 3300000));
            msg2.setIsRead(1);
            messages.add(msg2);
            
            ChatMessage msg3 = new ChatMessage();
            msg3.setId(3L);
            msg3.setSessionId("session_1");
            msg3.setSenderId("user_123");
            msg3.setReceiverId("当前登录用户ID");
            msg3.setContent("谢谢！那我买一些试试");
            msg3.setContentType("text");
            msg3.setCreateTime(new java.util.Date(System.currentTimeMillis() - 3000000));
            msg3.setIsRead(0);
            messages.add(msg3);
        } else if (sessionId == 2L) {
            // 会话2的消息（与秦岭茗茶客服）
            ChatMessage msg1 = new ChatMessage();
            msg1.setId(4L);
            msg1.setSessionId("session_2");
            msg1.setSenderId("shop_101");
            msg1.setReceiverId("当前登录用户ID");
            msg1.setContent("您好，有什么可以帮到您的吗？");
            msg1.setContentType("text");
            msg1.setCreateTime(new java.util.Date(System.currentTimeMillis() - 7200000));
            msg1.setIsRead(1);
            messages.add(msg1);
            
            ChatMessage msg2 = new ChatMessage();
            msg2.setId(5L);
            msg2.setSessionId("session_2");
            msg2.setSenderId("当前登录用户ID");
            msg2.setReceiverId("shop_101");
            msg2.setContent("我想了解一下你们店的铁观音");
            msg2.setContentType("text");
            msg2.setCreateTime(new java.util.Date(System.currentTimeMillis() - 7000000));
            msg2.setIsRead(1);
            messages.add(msg2);
        } else {
            // 其他会话的默认消息
            ChatMessage msg1 = new ChatMessage();
            msg1.setId(6L);
            msg1.setSessionId("session_3");
            msg1.setSenderId("user_456");
            msg1.setReceiverId("当前登录用户ID");
            msg1.setContent("谢谢你的推荐！");
            msg1.setContentType("text");
            msg1.setCreateTime(new java.util.Date(System.currentTimeMillis() - 14400000));
            msg1.setIsRead(1);
            messages.add(msg1);
        }
        
        result.setList(messages);
        return Result.success(result);
    }

    /**
     * 发送消息
     */
    @PostMapping("/messages")
    public Result<ChatMessage> sendMessage(@RequestBody ChatMessage message) {
        // TODO: 设置发送者ID为当前登录用户ID
        message.setSenderId("当前登录用户ID"); // 应从登录信息中获取
        
        // 返回测试数据 - Task Group A
        ChatMessage sentMessage = new ChatMessage();
        sentMessage.setId(System.currentTimeMillis()); // 使用时间戳作为ID
        sentMessage.setSessionId(message.getSessionId());
        sentMessage.setSenderId("当前登录用户ID");
        sentMessage.setReceiverId(message.getReceiverId());
        sentMessage.setContent(message.getContent());
        sentMessage.setContentType(message.getContentType() != null ? message.getContentType() : "text");
        sentMessage.setCreateTime(new java.util.Date());
        sentMessage.setIsRead(0);
        
        return Result.success(sentMessage);
    }

    /**
     * 兼容前端占位：/message/send
     */
    @PostMapping("/send")
    public Result<ChatMessage> sendMessageCompat(@RequestBody ChatMessage message) {
        return sendMessage(message);
    }

    /**
     * 兼容前端占位：/message/delete
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteMessageCompat(@RequestBody(required = false) Object body) {
        // TODO-SCRIPT: 后续实现消息删除/批量删除
        return Result.success(true);
    }

    /**
     * 将会话中的消息标记为已读
     */
    @PutMapping("/sessions/{sessionId}/read")
    public Result<Boolean> markMessagesAsRead(@PathVariable Long sessionId) {
        // TODO: 获取当前登录用户ID
        String userId = "当前登录用户ID"; // 应从登录信息中获取
        return Result.success(messageService.markMessagesAsRead(sessionId, userId));
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/messages/unread-count")
    public Result<Integer> countUnreadMessages() {
        // TODO: 获取当前登录用户ID
        String userId = "当前登录用户ID"; // 应从登录信息中获取
        return Result.success(messageService.countUnreadMessages(userId));
    }

    /**
     * 获取通知列表
     */
    @GetMapping("/notifications")
    public Result<PageResult<UserNotification>> listNotifications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String type) {
        // TODO: 获取当前登录用户ID
        String userId = "当前登录用户ID"; // 应从登录信息中获取
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        
        // 返回测试数据 - Task Group 0
        PageResult<UserNotification> result = new PageResult<>();
        result.setTotal(25L);
        result.setPageNum(page);
        result.setPageSize(size);
        
        // 创建测试通知数据 - 四种通知类型
        java.util.List<UserNotification> notifications = new java.util.ArrayList<>();
        
        // 类型1：帖子回复或@通知
        UserNotification notification1 = new UserNotification();
        notification1.setId(1L);
        notification1.setUserId(userId);
        notification1.setType("post_reply");
        notification1.setTitle("您的帖子收到新回复");
        notification1.setContent("用户"茶友小王"回复了您的帖子《龙井茶的冲泡技巧》");
        notification1.setIsRead(0); // 0=未读, 1=已读
        notification1.setCreateTime(new java.util.Date());
        notification1.setTargetType("post");
        notification1.setTargetId("123");
        notifications.add(notification1);
        
        // 类型2：纯文本通知
        UserNotification notification2 = new UserNotification();
        notification2.setId(2L);
        notification2.setUserId(userId);
        notification2.setType("system_announcement");
        notification2.setTitle("系统维护通知");
        notification2.setContent("系统将于今晚23:00-01:00进行维护升级，期间可能影响部分功能使用，敬请谅解。");
        notification2.setIsRead(1); // 已读
        notification2.setCreateTime(new java.util.Date(System.currentTimeMillis() - 86400000)); // 1天前
        notifications.add(notification2);
        
        // 类型3：带外部链接通知
        UserNotification notification3 = new UserNotification();
        notification3.setId(3L);
        notification3.setUserId(userId);
        notification3.setType("external_link");
        notification3.setTitle("新品推荐");
        notification3.setContent("春季新茶上市，点击查看详情");
        notification3.setIsRead(0); // 未读
        notification3.setCreateTime(new java.util.Date(System.currentTimeMillis() - 3600000)); // 1小时前
        notification3.setExtraData("{\"externalUrl\":\"https://example.com/spring-tea\"}");
        notifications.add(notification3);
        
        // 类型4：含操作通知
        UserNotification notification4 = new UserNotification();
        notification4.setId(4L);
        notification4.setUserId(userId);
        notification4.setType("merchant_verification");
        notification4.setTitle("商家认证审核通过");
        notification4.setContent("恭喜您的商家认证已通过审核，现在可以发布商品了！");
        notification4.setIsRead(0); // 未读
        notification4.setCreateTime(new java.util.Date(System.currentTimeMillis() - 7200000)); // 2小时前
        notification4.setExtraData("{\"actionType\":\"confirm\",\"actionText\":\"确认\"}");
        notifications.add(notification4);
        
        // 添加更多测试数据
        for (int i = 5; i <= 10; i++) {
            UserNotification notification = new UserNotification();
            notification.setId((long) i);
            notification.setUserId(userId);
            notification.setType("system_announcement");
            notification.setTitle("测试通知 " + i);
            notification.setContent("这是第 " + i + " 条测试通知内容");
            notification.setIsRead(i % 3 == 0 ? 1 : 0); // 每3条有1条已读
            notification.setCreateTime(new java.util.Date(System.currentTimeMillis() - i * 3600000L)); // i小时前
            notifications.add(notification);
        }
        
        result.setList(notifications);
        return Result.success(result);
    }

    /**
     * 将通知标记为已读
     */
    @PutMapping("/notifications/{id}/read")
    public Result<Boolean> markNotificationAsRead(@PathVariable Long id) {
        return Result.success(messageService.markNotificationAsRead(id));
    }

    /**
     * 将所有通知标记为已读
     */
    @PutMapping("/notifications/read-all")
    public Result<Boolean> markAllNotificationsAsRead() {
        // TODO: 获取当前登录用户ID
        String userId = "当前登录用户ID"; // 应从登录信息中获取
        return Result.success(messageService.markAllNotificationsAsRead(userId));
    }

    /**
     * 获取通知详情
     */
    @GetMapping("/notifications/{id}")
    public Result<UserNotification> getNotificationDetail(@PathVariable Long id) {
        // 返回测试数据 - Task Group 0
        UserNotification notification = new UserNotification();
        notification.setId(id);
        notification.setUserId("当前登录用户ID");
        notification.setType("post_reply");
        notification.setTitle("您的帖子收到新回复");
        notification.setContent("用户"茶友小王"回复了您的帖子《龙井茶的冲泡技巧》：\n\n"感谢分享！我按照您的方法试了一下，茶汤确实更香了。请问水温控制在多少度比较合适？"");
        notification.setIsRead(0);
        notification.setCreateTime(new java.util.Date());
        notification.setTargetType("post");
        notification.setTargetId("123");
        
        return Result.success(notification);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/notifications/{id}")
    public Result<Boolean> deleteNotification(@PathVariable Long id) {
        // 返回测试数据 - Task Group 0
        return Result.success(true);
    }

    /**
     * 批量标记通知为已读
     */
    @PutMapping("/notifications/batch-read")
    public Result<Boolean> batchMarkNotificationsAsRead(@RequestBody java.util.List<Long> ids) {
        // 返回测试数据 - Task Group 0
        return Result.success(true);
    }

    /**
     * 批量删除通知
     */
    @DeleteMapping("/notifications/batch")
    public Result<Boolean> batchDeleteNotifications(@RequestBody java.util.List<Long> ids) {
        // 返回测试数据 - Task Group 0
        return Result.success(true);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/notifications/unread-count")
    public Result<Integer> countUnreadNotifications() {
        // TODO: 获取当前登录用户ID
        String userId = "当前登录用户ID"; // 应从登录信息中获取
        // 返回测试数据 - Task Group 0
        return Result.success(8); // 8条未读通知
    }

    /**
     * 聊天功能完善相关API - Task Group B
     */
    
    /**
     * 置顶会话
     */
    @PutMapping("/sessions/{sessionId}/pin")
    public Result<Boolean> pinChatSession(@PathVariable Long sessionId) {
        // TODO: 获取当前登录用户ID
        String userId = "当前登录用户ID"; // 应从登录信息中获取
        
        // 返回测试数据 - Task Group B
        return Result.success(true);
    }
    
    /**
     * 删除会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Boolean> deleteChatSession(@PathVariable Long sessionId) {
        // TODO: 获取当前登录用户ID
        String userId = "当前登录用户ID"; // 应从登录信息中获取
        
        // 返回测试数据 - Task Group B
        return Result.success(true);
    }
    
    /**
     * 发送图片消息
     */
    @PostMapping("/messages/image")
    public Result<ChatMessage> sendImageMessage(
            @RequestParam("sessionId") String sessionId,
            @RequestParam("receiverId") String receiverId,
            @RequestParam("image") org.springframework.web.multipart.MultipartFile image) {
        // TODO: 处理图片上传，保存到文件服务器，获取图片URL
        
        // 返回测试数据 - Task Group B
        ChatMessage sentMessage = new ChatMessage();
        sentMessage.setId(System.currentTimeMillis()); // 使用时间戳作为ID
        sentMessage.setSessionId(sessionId);
        sentMessage.setSenderId("当前登录用户ID");
        sentMessage.setReceiverId(receiverId);
        sentMessage.setContent("https://via.placeholder.com/300x200?text=图片消息"); // 模拟图片URL
        sentMessage.setContentType("image");
        sentMessage.setCreateTime(new java.util.Date());
        sentMessage.setIsRead(0);
        
        return Result.success(sentMessage);
    }
    
    /**
     * 个人主页相关API - Task Group C
     */
    
    /**
     * 获取用户主页信息
     */
    @GetMapping("/user/profile/{userId}")
    public Result<java.util.Map<String, Object>> getUserProfile(@PathVariable String userId) {
        // 返回测试数据 - Task Group C
        java.util.Map<String, Object> profile = new java.util.HashMap<>();
        
        // 基本信息
        profile.put("id", userId);
        profile.put("username", "user_" + userId);
        profile.put("nickname", userId.equals("123") ? "茶香四溢" : 
                                userId.equals("456") ? "茶艺小能手" : 
                                userId.equals("789") ? "普洱控" : "茶友" + userId);
        profile.put("avatar", "https://via.placeholder.com/100x100?text=用户" + userId);
        profile.put("bio", userId.equals("123") ? "热爱茶文化，专注品茶20年" : 
                          userId.equals("456") ? "茶艺师，喜欢分享茶叶知识" : 
                          userId.equals("789") ? "普洱茶收藏爱好者" : "茶叶爱好者");
        profile.put("role", userId.equals("456") ? "merchant" : "user"); // 456是商家
        profile.put("registerTime", new java.util.Date(System.currentTimeMillis() - 365L * 24 * 3600 * 1000)); // 1年前注册
        
        // 商家用户的店铺链接
        if ("merchant".equals(profile.get("role"))) {
            profile.put("shopId", "shop_" + userId);
            profile.put("shopName", "茶艺小能手的茶叶店");
        }
        
        return Result.success(profile);
    }
    
    /**
     * 获取用户动态
     */
    @GetMapping("/user/profile/{userId}/dynamic")
    public Result<java.util.Map<String, Object>> getUserDynamic(@PathVariable String userId) {
        // 返回测试数据 - Task Group C
        java.util.Map<String, Object> dynamic = new java.util.HashMap<>();
        
        // 最近发布的帖子
        java.util.List<java.util.Map<String, Object>> recentPosts = new java.util.ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            java.util.Map<String, Object> post = new java.util.HashMap<>();
            post.put("id", "post_" + userId + "_" + i);
            post.put("title", userId.equals("123") ? "龙井茶的冲泡技巧分享" + i : 
                             userId.equals("456") ? "茶艺表演心得" + i : 
                             "普洱茶收藏经验" + i);
            post.put("content", "这是用户" + userId + "发布的第" + i + "篇帖子内容...");
            post.put("createTime", new java.util.Date(System.currentTimeMillis() - i * 24 * 3600 * 1000L)); // i天前
            post.put("likeCount", 10 + i * 5);
            post.put("commentCount", 3 + i * 2);
            recentPosts.add(post);
        }
        dynamic.put("recentPosts", recentPosts);
        
        // 最近发表的评论
        java.util.List<java.util.Map<String, Object>> recentComments = new java.util.ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            java.util.Map<String, Object> comment = new java.util.HashMap<>();
            comment.put("id", "comment_" + userId + "_" + i);
            comment.put("content", "这是用户" + userId + "的第" + i + "条评论，很有见解！");
            comment.put("postTitle", "关于茶叶保存的讨论" + i);
            comment.put("createTime", new java.util.Date(System.currentTimeMillis() - i * 12 * 3600 * 1000L)); // i*12小时前
            recentComments.add(comment);
        }
        dynamic.put("recentComments", recentComments);
        
        return Result.success(dynamic);
    }
    
    /**
     * 获取用户统计数据
     */
    @GetMapping("/user/profile/{userId}/statistics")
    public Result<java.util.Map<String, Object>> getUserStatistics(@PathVariable String userId) {
        // 返回测试数据 - Task Group C
        java.util.Map<String, Object> statistics = new java.util.HashMap<>();
        
        // 根据用户ID生成不同的统计数据
        int baseNum = Integer.parseInt(userId.replaceAll("\\D", "")) % 100; // 提取数字部分
        
        statistics.put("postCount", 15 + baseNum); // 发帖数
        statistics.put("likeCount", 120 + baseNum * 10); // 获得的点赞数
        statistics.put("favoriteCount", 8 + baseNum / 10); // 收藏数
        statistics.put("followingCount", 25 + baseNum / 5); // 关注数
        statistics.put("followerCount", 35 + baseNum / 3); // 粉丝数
        statistics.put("commentCount", 45 + baseNum * 2); // 评论数
        
        return Result.success(statistics);
    }

    /**
     * 发布内容管理相关API - Task Group F
     */
    
    /**
     * 获取用户发布的帖子列表
     */
    @GetMapping("/user/posts")
    public Result<PageResult<java.util.Map<String, Object>>> getUserPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy) {
        // TODO: 获取当前登录用户ID
        String userId = "当前登录用户ID"; // 应从登录信息中获取
        
        // 返回测试数据 - Task Group F
        PageResult<java.util.Map<String, Object>> result = new PageResult<>();
        result.setTotal(15L);
        result.setPageNum(page);
        result.setPageSize(size);
        
        java.util.List<java.util.Map<String, Object>> posts = new java.util.ArrayList<>();
        
        // 创建测试帖子数据
        for (int i = 1; i <= Math.min(size, 15); i++) {
            java.util.Map<String, Object> post = new java.util.HashMap<>();
            post.put("id", "post_" + userId + "_" + i);
            post.put("title", getPostTitle(i));
            post.put("summary", getPostSummary(i));
            post.put("content", getPostContent(i));
            post.put("createTime", new java.util.Date(System.currentTimeMillis() - i * 24 * 3600 * 1000L)); // i天前
            post.put("updateTime", new java.util.Date(System.currentTimeMillis() - i * 12 * 3600 * 1000L)); // i*12小时前
            post.put("viewCount", 50 + i * 20);
            post.put("replyCount", 5 + i * 3);
            post.put("likeCount", 10 + i * 5);
            post.put("status", i % 4 == 0 ? "pinned" : i % 3 == 0 ? "featured" : "normal"); // 置顶、精华、普通
            post.put("categoryId", "category_" + (i % 3 + 1)); // 分类ID
            post.put("categoryName", getCategoryName(i % 3 + 1)); // 分类名称
            posts.add(post);
        }
        
        // 根据sortBy排序
        if ("viewCount".equals(sortBy)) {
            posts.sort((a, b) -> ((Integer) b.get("viewCount")).compareTo((Integer) a.get("viewCount")));
        } else if ("replyCount".equals(sortBy)) {
            posts.sort((a, b) -> ((Integer) b.get("replyCount")).compareTo((Integer) a.get("replyCount")));
        } else if ("likeCount".equals(sortBy)) {
            posts.sort((a, b) -> ((Integer) b.get("likeCount")).compareTo((Integer) a.get("likeCount")));
        } else {
            // 默认按创建时间排序
            posts.sort((a, b) -> ((java.util.Date) b.get("createTime")).compareTo((java.util.Date) a.get("createTime")));
        }
        
        result.setList(posts);
        return Result.success(result);
    }
    
    /**
     * 获取用户评价记录
     */
    @GetMapping("/user/reviews")
    public Result<PageResult<java.util.Map<String, Object>>> getUserReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        // TODO: 获取当前登录用户ID
        String userId = "当前登录用户ID"; // 应从登录信息中获取
        
        // 返回测试数据 - Task Group F
        PageResult<java.util.Map<String, Object>> result = new PageResult<>();
        result.setTotal(8L);
        result.setPageNum(page);
        result.setPageSize(size);
        
        java.util.List<java.util.Map<String, Object>> reviews = new java.util.ArrayList<>();
        
        // 创建测试评价数据
        for (int i = 1; i <= Math.min(size, 8); i++) {
            java.util.Map<String, Object> review = new java.util.HashMap<>();
            review.put("id", "review_" + userId + "_" + i);
            review.put("teaId", "tea_" + i);
            review.put("teaName", getTeaName(i));
            review.put("teaImage", "https://via.placeholder.com/200x200?text=茶叶" + i);
            review.put("shopId", "shop_" + (i % 3 + 1));
            review.put("shopName", getShopName(i % 3 + 1));
            review.put("rating", 3 + (i % 3)); // 3-5星评分
            review.put("content", getReviewContent(i));
            review.put("createTime", new java.util.Date(System.currentTimeMillis() - i * 7 * 24 * 3600 * 1000L)); // i周前
            review.put("hasShopReply", i % 2 == 0); // 商家是否回复
            review.put("shopReply", i % 2 == 0 ? "感谢您的评价，我们会继续努力提供优质的茶叶！" : null);
            review.put("shopReplyTime", i % 2 == 0 ? new java.util.Date(System.currentTimeMillis() - (i-1) * 7 * 24 * 3600 * 1000L) : null);
            reviews.add(review);
        }
        
        result.setList(reviews);
        return Result.success(result);
    }
    
    // 辅助方法：生成测试数据
    private String getPostTitle(int index) {
        String[] titles = {
            "分享我最近喝过的安化黑茶，口感超赞！",
            "求推荐适合夏天喝的茶，清热解暑的那种",
            "新手冲泡白茶总是苦涩，有什么技巧吗？",
            "龙井茶的冲泡技巧分享，水温很关键",
            "普洱茶收藏心得，如何辨别好茶？",
            "铁观音的香型分类，你都了解吗？",
            "茶具选择指南，紫砂壶vs盖碗",
            "茶叶保存方法大全，避免受潮变质"
        };
        return titles[(index - 1) % titles.length];
    }
    
    private String getPostSummary(int index) {
        String[] summaries = {
            "最近入手了几款安化黑茶，冲泡后口感醇厚，回甘很持久，特别是金花茯砖茶，口感层次非常丰富...",
            "天气越来越热了，想找一些适合夏季饮用的茶，最好是有清热解暑功效的。有没有茶友能推荐一些？",
            "刚开始接触白茶，买了一些白毫银针，但冲泡出来总是有些苦涩，不知道是水温问题还是时间问题？求大神指点！",
            "龙井茶冲泡需要注意水温控制，一般85-90度最佳，时间不宜过长，否则会影响口感...",
            "收藏普洱茶多年，总结了一些辨别好茶的经验，主要看外形、汤色、香气、滋味等方面...",
            "铁观音有清香型、浓香型、陈香型等，每种香型都有不同的特点和冲泡方法...",
            "选择茶具很重要，紫砂壶适合泡乌龙茶、普洱茶，盖碗更适合绿茶、白茶...",
            "茶叶保存要注意防潮、避光、密封，不同类型的茶叶保存方法也有所不同..."
        };
        return summaries[(index - 1) % summaries.length];
    }
    
    private String getPostContent(int index) {
        return getPostSummary(index) + "\n\n详细内容请查看帖子详情页面...";
    }
    
    private String getCategoryName(int categoryId) {
        String[] categories = {"茶叶品鉴", "冲泡技巧", "茶文化交流"};
        return categories[categoryId - 1];
    }
    
    private String getTeaName(int index) {
        String[] teaNames = {
            "西湖龙井特级",
            "安溪铁观音",
            "云南普洱熟茶",
            "福鼎白毫银针",
            "武夷大红袍",
            "黄山毛峰",
            "六安瓜片",
            "祁门红茶"
        };
        return teaNames[(index - 1) % teaNames.length];
    }
    
    private String getShopName(int shopId) {
        String[] shopNames = {"秦岭茗茶", "江南茶庄", "云雾茶行"};
        return shopNames[shopId - 1];
    }
    
    private String getReviewContent(int index) {
        String[] contents = {
            "茶叶品质很好，香气浓郁，口感醇厚，包装也很精美，值得推荐！",
            "第一次买这个品牌的茶，口感不错，性价比很高，会继续关注。",
            "老板服务很好，茶叶质量也不错，冲泡后香气扑鼻，回甘明显。",
            "收到茶叶后立即试泡了，汤色清亮，滋味甘甜，确实是好茶！",
            "包装很用心，茶叶新鲜度很好，口感层次丰富，非常满意。",
            "朋友推荐的这款茶，确实不错，香气持久，回味悠长。",
            "性价比很高的一款茶，适合日常饮用，会回购的。",
            "茶叶品质稳定，每次购买都很满意，是我的常备茶叶。"
        };
        return contents[(index - 1) % contents.length];
    }
    
    /**
     * 客服相关API
     */
    
    /**
     * 获取客服会话列表 (仅商家权限)
     */
    @GetMapping("/customer-service/sessions")
    public Result<List<ChatSession>> listCustomerServiceSessions() {
        // TODO: 获取当前登录商家ID
        String shopId = "当前登录商家ID"; // 应从登录信息中获取
        
        // TODO: 实现商家查看其店铺的客服会话列表
        return Result.success(null);
    }
    
    /**
     * 回复客服消息 (仅商家权限)
     */
    @PostMapping("/customer-service/messages")
    public Result<ChatMessage> replyCustomerService(@RequestBody ChatMessage message) {
        // TODO: 验证当前登录用户是否为商家，并有权限回复该会话
        
        // TODO: 设置发送者为商家ID
        message.setSenderId("当前登录商家ID");
        
        return Result.success(messageService.sendMessage(message));
    }
} 