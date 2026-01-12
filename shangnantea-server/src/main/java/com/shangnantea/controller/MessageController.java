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
import java.util.Map;

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
        List<ChatSession> result = messageService.listSessions();
        return Result.success(result);
    }

    /**
     * 获取会话详情
     */
    @GetMapping("/sessions/{id}")
    public Result<ChatSession> getSessionById(@PathVariable Long id) {
        ChatSession result = messageService.getSessionById(id);
        return Result.success(result);
    }

    /**
     * 创建或获取与指定用户/商家的会话
     */
    @PostMapping("/sessions")
    public Result<ChatSession> createOrGetSession(@RequestParam String targetId, @RequestParam String targetType) {
        ChatSession result = messageService.createOrGetSession(targetId, targetType);
        return Result.success(result);
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
        PageResult<ChatMessage> result = messageService.listMessages(sessionId, pageParam);
        return Result.success(result);
    }

    /**
     * 发送消息
     */
    @PostMapping("/messages")
    public Result<ChatMessage> sendMessage(@RequestBody ChatMessage message) {
        ChatMessage result = messageService.sendMessage(message);
        return Result.success(result);
    }

    /**
     * 兼容前端占位：/message/send
     */
    @PostMapping("/send")
    public Result<ChatMessage> sendMessageCompat(@RequestBody ChatMessage message) {
        ChatMessage result = messageService.sendMessage(message);
        return Result.success(result);
    }

    /**
     * 兼容前端占位：/message/delete
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteMessageCompat(@RequestBody(required = false) Object body) {
        Boolean result = messageService.deleteMessageCompat(body);
        return Result.success(result);
    }

    /**
     * 将会话中的消息标记为已读
     */
    @PutMapping("/sessions/{sessionId}/read")
    public Result<Boolean> markMessagesAsRead(@PathVariable Long sessionId) {
        Boolean result = messageService.markMessagesAsRead(sessionId);
        return Result.success(result);
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/messages/unread-count")
    public Result<Integer> countUnreadMessages() {
        Integer result = messageService.countUnreadMessages();
        return Result.success(result);
    }

    /**
     * 获取通知列表
     */
    @GetMapping("/notifications")
    public Result<PageResult<UserNotification>> listNotifications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String type) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<UserNotification> result = messageService.listNotifications(type, pageParam);
        return Result.success(result);
    }

    /**
     * 将通知标记为已读
     */
    @PutMapping("/notifications/{id}/read")
    public Result<Boolean> markNotificationAsRead(@PathVariable Long id) {
        Boolean result = messageService.markNotificationAsRead(id);
        return Result.success(result);
    }

    /**
     * 将所有通知标记为已读
     */
    @PutMapping("/notifications/read-all")
    public Result<Boolean> markAllNotificationsAsRead() {
        Boolean result = messageService.markAllNotificationsAsRead();
        return Result.success(result);
    }

    /**
     * 获取通知详情
     */
    @GetMapping("/notifications/{id}")
    public Result<UserNotification> getNotificationDetail(@PathVariable Long id) {
        UserNotification result = messageService.getNotificationDetail(id);
        return Result.success(result);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/notifications/{id}")
    public Result<Boolean> deleteNotification(@PathVariable Long id) {
        Boolean result = messageService.deleteNotification(id);
        return Result.success(result);
    }

    /**
     * 批量标记通知为已读
     */
    @PutMapping("/notifications/batch-read")
    public Result<Boolean> batchMarkNotificationsAsRead(@RequestBody List<Long> ids) {
        Boolean result = messageService.batchMarkNotificationsAsRead(ids);
        return Result.success(result);
    }

    /**
     * 批量删除通知
     */
    @DeleteMapping("/notifications/batch")
    public Result<Boolean> batchDeleteNotifications(@RequestBody List<Long> ids) {
        Boolean result = messageService.batchDeleteNotifications(ids);
        return Result.success(result);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/notifications/unread-count")
    public Result<Integer> countUnreadNotifications() {
        Integer result = messageService.countUnreadNotifications();
        return Result.success(result);
    }

    /**
     * 置顶会话
     */
    @PutMapping("/sessions/{sessionId}/pin")
    public Result<Boolean> pinChatSession(@PathVariable Long sessionId) {
        Boolean result = messageService.pinChatSession(sessionId);
        return Result.success(result);
    }
    
    /**
     * 删除会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Boolean> deleteChatSession(@PathVariable Long sessionId) {
        Boolean result = messageService.deleteChatSession(sessionId);
        return Result.success(result);
    }
    
    /**
     * 发送图片消息
     */
    @PostMapping("/messages/image")
    public Result<ChatMessage> sendImageMessage(
            @RequestParam("sessionId") String sessionId,
            @RequestParam("receiverId") String receiverId,
            @RequestParam("image") org.springframework.web.multipart.MultipartFile image) {
        ChatMessage result = messageService.sendImageMessage(sessionId, receiverId, image);
        return Result.success(result);
    }
    
    /**
     * 获取用户主页信息
     */
    @GetMapping("/user/profile/{userId}")
    public Result<Map<String, Object>> getUserProfile(@PathVariable String userId) {
        Map<String, Object> result = messageService.getUserProfile(userId);
        return Result.success(result);
    }
    
    /**
     * 获取用户动态
     */
    @GetMapping("/user/profile/{userId}/dynamic")
    public Result<Map<String, Object>> getUserDynamic(@PathVariable String userId) {
        Map<String, Object> result = messageService.getUserDynamic(userId);
        return Result.success(result);
    }
    
    /**
     * 获取用户统计数据
     */
    @GetMapping("/user/profile/{userId}/statistics")
    public Result<Map<String, Object>> getUserStatistics(@PathVariable String userId) {
        Map<String, Object> result = messageService.getUserStatistics(userId);
        return Result.success(result);
    }

    /**
     * 获取用户发布的帖子列表
     */
    @GetMapping("/user/posts")
    public Result<PageResult<Map<String, Object>>> getUserPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<Map<String, Object>> result = messageService.getUserPosts(sortBy, pageParam);
        return Result.success(result);
    }
    
    /**
     * 获取用户评价记录
     */
    @GetMapping("/user/reviews")
    public Result<PageResult<Map<String, Object>>> getUserReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<Map<String, Object>> result = messageService.getUserReviews(pageParam);
        return Result.success(result);
    }

    /**
     * 获取客服会话列表 (仅商家权限)
     */
    @GetMapping("/customer-service/sessions")
    public Result<List<ChatSession>> listCustomerServiceSessions() {
        List<ChatSession> result = messageService.listCustomerServiceSessions();
        return Result.success(result);
    }

    /**
     * 回复客服消息 (仅商家权限)
     */
    @PostMapping("/customer-service/messages")
    public Result<ChatMessage> replyCustomerService(@RequestBody ChatMessage message) {
        ChatMessage result = messageService.replyCustomerService(message);
        return Result.success(result);
    }
}
