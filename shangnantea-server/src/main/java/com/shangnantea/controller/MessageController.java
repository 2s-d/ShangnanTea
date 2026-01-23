package com.shangnantea.controller;

import com.shangnantea.common.api.Result;
import com.shangnantea.security.annotation.RequiresLogin;
import com.shangnantea.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 消息控制器
 * 处理聊天会话、消息、通知、用户主页等功能
 * 参考：前端 message.js 和 code-message-mapping.md
 */
@RestController
@RequestMapping({"/message", "/api/message"})
@Validated
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageService messageService;

    // ==================== 消息基础操作 ====================

    /**
     * 获取消息列表
     * 路径: GET /message/list
     * 成功码: 200, 失败码: 7100
     *
     * @param params 查询参数（page, pageSize, type）
     * @return 消息列表
     */
    @GetMapping("/list")
    @RequiresLogin
    public Result<Object> getMessages(@RequestParam Map<String, Object> params) {
        logger.info("获取消息列表请求, params: {}", params);
        return messageService.getMessages(params);
    }

    /**
     * 获取消息详情
     * 路径: GET /message/messages/{id}
     * 成功码: 200, 失败码: 7101
     *
     * @param id 消息ID
     * @return 消息详情
     */
    @GetMapping("/messages/{id}")
    @RequiresLogin
    public Result<Object> getMessageDetail(@PathVariable String id) {
        logger.info("获取消息详情请求: {}", id);
        return messageService.getMessageDetail(id);
    }

    /**
     * 发送图片消息
     * 路径: POST /message/messages/image
     * 成功码: 7003, 失败码: 7103, 1103, 1104
     *
     * @param sessionId 会话ID
     * @param receiverId 接收者ID
     * @param image 图片文件
     * @return 发送结果
     */
    @PostMapping("/messages/image")
    @RequiresLogin
    public Result<Object> sendImageMessage(
            @RequestParam("sessionId") String sessionId,
            @RequestParam("receiverId") String receiverId,
            @RequestParam("image") MultipartFile image) {
        logger.info("发送图片消息请求, sessionId: {}, receiverId: {}, 文件名: {}", 
                sessionId, receiverId, image.getOriginalFilename());
        return messageService.sendImageMessage(sessionId, receiverId, image);
    }

    /**
     * 发送消息
     * 路径: POST /message/send
     * 成功码: 7000, 失败码: 7100
     *
     * @param data 消息数据 {receiverId, content, type}
     * @return 发送结果
     */
    @PostMapping("/send")
    @RequiresLogin
    public Result<Object> sendMessage(@RequestBody Map<String, Object> data) {
        logger.info("发送消息请求");
        return messageService.sendMessage(data);
    }

    /**
     * 标记消息为已读
     * 路径: POST /message/read
     * 成功码: 7010, 失败码: 7110
     *
     * @param data 消息ID数据 {messageIds}
     * @return 标记结果
     */
    @PostMapping("/read")
    @RequiresLogin
    public Result<Object> markAsRead(@RequestBody Map<String, Object> data) {
        logger.info("标记消息已读请求");
        return messageService.markAsRead(data);
    }

    /**
     * 删除消息
     * 路径: POST /message/delete
     * 成功码: 7012, 失败码: 1100
     *
     * @param data 消息ID数据 {messageIds}
     * @return 删除结果
     */
    @PostMapping("/delete")
    @RequiresLogin
    public Result<Object> deleteMessages(@RequestBody Map<String, Object> data) {
        logger.info("删除消息请求");
        return messageService.deleteMessages(data);
    }

    /**
     * 获取未读消息数量
     * 路径: GET /message/unread-count
     * 成功码: 200, 失败码: 7101
     *
     * @return 未读消息数量
     */
    @GetMapping("/unread-count")
    @RequiresLogin
    public Result<Object> getUnreadCount() {
        logger.info("获取未读消息数量请求");
        return messageService.getUnreadCount();
    }

    // ==================== 通知管理 ====================

    /**
     * 获取通知列表
     * 路径: GET /message/notifications
     * 成功码: 200, 失败码: 7101
     *
     * @param params 查询参数 {page, size, type}
     * @return 通知列表
     */
    @GetMapping("/notifications")
    @RequiresLogin
    public Result<Object> getNotifications(@RequestParam Map<String, Object> params) {
        logger.info("获取通知列表请求, params: {}", params);
        return messageService.getNotifications(params);
    }

    /**
     * 批量标记通知为已读
     * 路径: PUT /message/notifications/batch-read
     * 成功码: 7011, 失败码: 7110
     *
     * @param ids 通知ID列表
     * @return 标记结果
     */
    @PutMapping("/notifications/batch-read")
    @RequiresLogin
    public Result<Boolean> batchMarkNotificationsAsRead(@RequestBody List<Long> ids) {
        logger.info("批量标记通知已读请求, ids: {}", ids);
        return messageService.batchMarkNotificationsAsRead(ids);
    }

    /**
     * 批量删除通知
     * 路径: DELETE /message/notifications/batch
     * 成功码: 7013, 失败码: 1100
     *
     * @param ids 通知ID列表
     * @return 删除结果
     */
    @DeleteMapping("/notifications/batch")
    @RequiresLogin
    public Result<Boolean> batchDeleteNotifications(@RequestBody List<Long> ids) {
        logger.info("批量删除通知请求, ids: {}", ids);
        return messageService.batchDeleteNotifications(ids);
    }

    /**
     * 获取通知详情
     * 路径: GET /message/notifications/{id}
     * 成功码: 200, 失败码: 7101
     *
     * @param id 通知ID
     * @return 通知详情
     */
    @GetMapping("/notifications/{id}")
    @RequiresLogin
    public Result<Object> getNotificationDetail(@PathVariable String id) {
        logger.info("获取通知详情请求: {}", id);
        return messageService.getNotificationDetail(id);
    }

    /**
     * 删除通知
     * 路径: DELETE /message/notifications/{id}
     * 成功码: 7012, 失败码: 1100
     *
     * @param id 通知ID
     * @return 删除结果
     */
    @DeleteMapping("/notifications/{id}")
    @RequiresLogin
    public Result<Boolean> deleteNotification(@PathVariable String id) {
        logger.info("删除通知请求: {}", id);
        return messageService.deleteNotification(id);
    }

    // ==================== 聊天会话 ====================

    /**
     * 获取聊天会话列表
     * 路径: GET /message/list/sessions
     * 成功码: 200, 失败码: 7101
     *
     * @return 会话列表
     */
    @GetMapping("/list/sessions")
    @RequiresLogin
    public Result<Object> getChatSessions() {
        logger.info("获取聊天会话列表请求");
        return messageService.getChatSessions();
    }

    /**
     * 获取聊天记录
     * 路径: GET /message/list/history
     * 成功码: 200, 失败码: 7101
     *
     * @param params 查询参数 {userId, page, size}
     * @return 聊天记录
     */
    @GetMapping("/list/history")
    @RequiresLogin
    public Result<Object> getChatHistory(@RequestParam Map<String, Object> params) {
        logger.info("获取聊天记录请求, params: {}", params);
        return messageService.getChatHistory(params);
    }

    /**
     * 创建聊天会话
     * 路径: POST /message/sessions
     * 成功码: 1000, 失败码: 1100
     *
     * @param targetId 目标用户/商家ID
     * @param targetType 目标类型
     * @return 创建结果
     */
    @PostMapping("/sessions")
    @RequiresLogin
    public Result<Object> createChatSession(@RequestParam String targetId, @RequestParam String targetType) {
        logger.info("创建聊天会话请求, targetId: {}, targetType: {}", targetId, targetType);
        return messageService.createChatSession(targetId, targetType);
    }

    /**
     * 置顶聊天会话
     * 路径: PUT /message/sessions/{sessionId}/pin
     * 成功码: 7014, 失败码: 1100
     *
     * @param sessionId 会话ID
     * @return 置顶结果
     */
    @PutMapping("/sessions/{sessionId}/pin")
    @RequiresLogin
    public Result<Boolean> pinChatSession(@PathVariable String sessionId) {
        logger.info("置顶聊天会话请求: {}", sessionId);
        return messageService.pinChatSession(sessionId);
    }

    /**
     * 删除聊天会话
     * 路径: DELETE /message/sessions/{sessionId}
     * 成功码: 7001, 失败码: 1100
     *
     * @param sessionId 会话ID
     * @return 删除结果
     */
    @DeleteMapping("/sessions/{sessionId}")
    @RequiresLogin
    public Result<Boolean> deleteChatSession(@PathVariable String sessionId) {
        logger.info("删除聊天会话请求: {}", sessionId);
        return messageService.deleteChatSession(sessionId);
    }

    // ==================== 用户主页 ====================

    /**
     * 获取用户发布的帖子列表
     * 路径: GET /message/user/posts
     * 成功码: 200, 失败码: 1102
     *
     * @param params 查询参数 {page, size, sortBy}
     * @return 帖子列表
     */
    @GetMapping("/user/posts")
    @RequiresLogin
    public Result<Object> getUserPosts(@RequestParam Map<String, Object> params) {
        logger.info("获取用户帖子列表请求, params: {}", params);
        return messageService.getUserPosts(params);
    }

    /**
     * 获取用户评价记录
     * 路径: GET /message/user/reviews
     * 成功码: 200, 失败码: 1102
     *
     * @param params 查询参数 {page, size}
     * @return 评价记录列表
     */
    @GetMapping("/user/reviews")
    @RequiresLogin
    public Result<Object> getUserReviews(@RequestParam Map<String, Object> params) {
        logger.info("获取用户评价记录请求, params: {}", params);
        return messageService.getUserReviews(params);
    }

    /**
     * 获取用户动态
     * 路径: GET /message/user/{userId}/dynamic
     * 成功码: 200, 失败码: 7120
     *
     * @param userId 用户ID
     * @return 用户动态
     */
    @GetMapping("/user/{userId}/dynamic")
    public Result<Object> getUserDynamic(@PathVariable String userId) {
        logger.info("获取用户动态请求: {}", userId);
        return messageService.getUserDynamic(userId);
    }

    /**
     * 获取用户统计数据
     * 路径: GET /message/user/{userId}/statistics
     * 成功码: 200, 失败码: 7120
     *
     * @param userId 用户ID
     * @return 用户统计数据
     */
    @GetMapping("/user/{userId}/statistics")
    public Result<Object> getUserStatistics(@PathVariable String userId) {
        logger.info("获取用户统计数据请求: {}", userId);
        return messageService.getUserStatistics(userId);
    }

    /**
     * 获取用户主页信息
     * 路径: GET /message/user/{userId}
     * 成功码: 200, 失败码: 7120, 7121
     * 注意：此路径应放在最后，避免与更具体的路径冲突
     *
     * @param userId 用户ID
     * @return 用户主页信息
     */
    @GetMapping("/user/{userId}")
    public Result<Object> getUserProfile(@PathVariable String userId) {
        logger.info("获取用户主页信息请求: {}", userId);
        return messageService.getUserProfile(userId);
    }
}
