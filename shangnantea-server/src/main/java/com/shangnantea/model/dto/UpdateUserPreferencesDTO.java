package com.shangnantea.model.dto;

import lombok.Data;

/**
 * 更新用户偏好设置数据传输对象
 */
@Data
public class UpdateUserPreferencesDTO {
    /**
     * 语言设置（zh_CN, en_US）
     */
    private String language;
    
    /**
     * 主题设置（light, dark）
     */
    private String theme;
    
    /**
     * 是否接收系统通知
     */
    private Boolean systemNotification;
    
    /**
     * 是否接收订单通知
     */
    private Boolean orderNotification;
    
    /**
     * 是否接收评论通知
     */
    private Boolean commentNotification;
    
    /**
     * 是否接收点赞通知
     */
    private Boolean likeNotification;
}
