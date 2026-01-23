package com.shangnantea.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 添加收藏数据传输对象
 */
@Data
public class AddFavoriteDTO {
    /**
     * 收藏项类型（tea-茶叶, post-帖子, tea_article-茶文章）
     */
    @NotBlank(message = "收藏类型不能为空")
    @Size(max = 20, message = "收藏类型长度不能超过20个字符")
    private String itemType;
    
    /**
     * 收藏项ID
     */
    @NotBlank(message = "收藏项ID不能为空")
    private String itemId;
    
    /**
     * 目标名称（可选）
     */
    @Size(max = 100, message = "目标名称长度不能超过100个字符")
    private String targetName;
    
    /**
     * 目标图片（可选）
     */
    @Size(max = 255, message = "目标图片路径长度不能超过255个字符")
    private String targetImage;
}
