package com.shangnantea.model.entity.message;

import lombok.Data;

import java.util.Date;

/**
 * 聊天消息实体类
 */
@Data
public class ChatMessage {
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 发送者ID
     */
    private String senderId;
    
    /**
     * 接收者ID
     */
    private String receiverId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 内容类型(text文本,image图片)
     */
    private String contentType;
    
    /**
     * 是否已读
     */
    private Integer isRead;
    
    /**
     * 阅读时间
     */
    private Date readTime;
    
    /**
     * 创建时间
     */
    private Date createTime;
} 