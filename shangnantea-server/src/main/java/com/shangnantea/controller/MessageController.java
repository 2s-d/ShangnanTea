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
@RequestMapping("/api/messages")
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
        return Result.success(messageService.listSessions(userId));
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
    public Result<ChatSession> createOrGetSession(@RequestParam String targetId, @RequestParam Integer type) {
        // TODO: 获取当前登录用户ID
        String userId = "当前登录用户ID"; // 应从登录信息中获取
        return Result.success(messageService.createOrGetSession(userId, targetId, type));
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
        return Result.success(messageService.listMessages(sessionId, pageParam));
    }

    /**
     * 发送消息
     */
    @PostMapping("/messages")
    public Result<ChatMessage> sendMessage(@RequestBody ChatMessage message) {
        // TODO: 设置发送者ID为当前登录用户ID
        message.setSenderId("当前登录用户ID"); // 应从登录信息中获取
        return Result.success(messageService.sendMessage(message));
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
            @RequestParam(defaultValue = "20") Integer size) {
        // TODO: 获取当前登录用户ID
        String userId = "当前登录用户ID"; // 应从登录信息中获取
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        return Result.success(messageService.listNotifications(userId, pageParam));
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
     * 获取未读通知数量
     */
    @GetMapping("/notifications/unread-count")
    public Result<Integer> countUnreadNotifications() {
        // TODO: 获取当前登录用户ID
        String userId = "当前登录用户ID"; // 应从登录信息中获取
        return Result.success(messageService.countUnreadNotifications(userId));
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