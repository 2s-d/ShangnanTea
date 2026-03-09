package com.shangnantea.model.vo.user;

import lombok.Data;

/**
 * 用户偏好设置视图对象。
 */
@Data
public class UserPreferencesVO {

    /**
     * 主题模式。
     */
    private String themeMode;

    /**
     * 字体大小。
     */
    private Integer fontSize;

    /**
     * 字体选择。
     */
    private String fontFamily;

    /**
     * 是否启用界面动画效果。
     */
    private Boolean enableAnimation;

    /**
     * 是否允许他人查看个人主页。
     */
    private Boolean profileVisible;
}
