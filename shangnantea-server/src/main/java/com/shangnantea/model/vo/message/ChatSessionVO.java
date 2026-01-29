package com.shangnantea.model.vo.message;

import java.io.Serializable;
import java.util.Date;

/**
 * 聊天会话VO
 */
public class ChatSessionVO implements Serializable {
    
    /**
     * 会话ID
     */
    private String id;
    
    /**
     * 对方用户ID
     */
    private String targetUserId;
    
    /**
     * 对方用户名
     */
    private String targetUserName;
    
    /**
     * 对方头像
     */
    private String targetAvatar;
    
    /**
     * 最后一条消息内容
     */
    private String lastMessage;
    
    /**
     * 最后消息时间
     */
    private Date lastMessageTime;
    
    /**
     * 未读消息数
     */
    private Integer unreadCount;
    
    /**
     * 会话类型
     */
    private String sessionType;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    public ChatSessionVO() {
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTargetUserId() {
        return targetUserId;
    }
    
    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }
    
    public String getTargetUserName() {
        return targetUserName;
    }
    
    public void setTargetUserName(String targetUserName) {
        this.targetUserName = targetUserName;
    }
    
    public String getTargetAvatar() {
        return targetAvatar;
    }
    
    public void setTargetAvatar(String targetAvatar) {
        this.targetAvatar = targetAvatar;
    }
    
    public String getLastMessage() {
        return lastMessage;
    }
    
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
    
    public Date getLastMessageTime() {
        return lastMessageTime;
    }
    
    public void setLastMessageTime(Date lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
    
    public Integer getUnreadCount() {
        return unreadCount;
    }
    
    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }
    
    public String getSessionType() {
        return sessionType;
    }
    
    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
