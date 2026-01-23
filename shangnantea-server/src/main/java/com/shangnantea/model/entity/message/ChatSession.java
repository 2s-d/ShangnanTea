package com.shangnantea.model.entity.message;

import lombok.Data;

import java.util.Date;

/**
 * 聊天会话实体类
 */
@Data
public class ChatSession {
    /**
     * 会话ID
     */
    private String id;
    
    /**
     * 发起者用户ID
     */
    private String initiatorId;
    
    /**
     * 接收者用户ID
     */
    private String receiverId;
    
    /**
     * 最后一条消息内容
     */
    private String lastMessage;
    
    /**
     * 最后消息时间
     */
    private Date lastMessageTime;
    
    /**
     * 发起者未读数
     */
    private Integer initiatorUnread;
    
    /**
     * 接收者未读数
     */
    private Integer receiverUnread;
    
    /**
     * 是否置顶(0否,1是)
     */
    private Integer isPinned;
    
    /**
     * 会话类型(private私聊,customer客服)
     */
    private String sessionType;
    
    /**
     * 状态(1正常,0禁用)
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 