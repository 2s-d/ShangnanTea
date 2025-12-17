package com.shangnantea.model.entity.message;

import lombok.Data;

import java.util.Date;

/**
 * 用户通知实体类
 */
@Data
public class UserNotification {
    /**
     * 通知ID
     */
    private Long id;
    
    /**
     * 接收用户ID
     */
    private String userId;
    
    /**
     * 发送者用户ID
     */
    private String senderId;
    
    /**
     * 通知类型(system,reply,like,certification等)
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
     * 目标ID
     */
    private String targetId;
    
    /**
     * 目标类型(post,article,shop等)
     */
    private String targetType;
    
    /**
     * 额外数据(JSON格式)
     */
    private String extraData;
    
    /**
     * 是否已读
     */
    private Integer isRead;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 阅读时间
     */
    private Date readTime;
} 