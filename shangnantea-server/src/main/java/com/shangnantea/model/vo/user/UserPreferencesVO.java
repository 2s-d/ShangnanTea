package com.shangnantea.model.vo.user;

import lombok.Data;

/**
 * 用户偏好设置视图对象
 */
@Data
public class UserPreferencesVO {
    /**
     * 语言设置
     */
    private String language;
    
    /**
     * 主题设置
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
