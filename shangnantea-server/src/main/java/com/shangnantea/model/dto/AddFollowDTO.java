package com.shangnantea.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 添加关注数据传输对象
 */
@Data
public class AddFollowDTO {
    /**
     * 关注目标ID
     */
    @NotBlank(message = "关注目标ID不能为空")
    private String targetId;
    
    /**
     * 关注类型（shop-店铺, user-用户）
     */
    @NotBlank(message = "关注类型不能为空")
    @Size(max = 20, message = "关注类型长度不能超过20个字符")
    private String targetType;
    
    /**
     * 目标名称（可选）
     */
    @Size(max = 100, message = "目标名称长度不能超过100个字符")
    private String targetName;
    
    /**
     * 目标头像（可选）
     */
    @Size(max = 255, message = "目标头像路径长度不能超过255个字符")
    private String targetAvatar;
}
