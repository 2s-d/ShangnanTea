package com.shangnantea.model.vo.message;

import java.io.Serializable;
import java.util.Date;

/**
 * 通知VO
 */
public class NotificationVO implements Serializable {
    
    /**
     * 通知ID
     */
    private Long id;
    
    /**
     * 通知类型（post_reply, system_announcement, external_link 等）
     */
    private String type;
    
    /**
     * 通知标题
     */
    private String title;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 目标ID（帖子ID、订单ID、茶叶ID、店铺ID、会话ID等）
     */
    private String targetId;
    
    /**
     * 目标类型（post, order, tea, shop, chat_session 等）
     */
    private String targetType;
    
    /**
     * 额外数据(JSON字符串，根据不同情景自定义字段)
     */
    private String extraData;
    
    /**
     * 是否已读（0:未读, 1:已读）
     */
    private Integer isRead;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    public NotificationVO() {
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getTargetId() {
        return targetId;
    }
    
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
    
    public String getTargetType() {
        return targetType;
    }
    
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }
    
    public String getExtraData() {
        return extraData;
    }
    
    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }
    
    public Integer getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
