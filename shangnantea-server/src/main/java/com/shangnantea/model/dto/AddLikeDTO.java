package com.shangnantea.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 添加点赞数据传输对象
 */
@Data
public class AddLikeDTO {
    /**
     * 点赞目标类型（post-帖子, reply-回复, article-文章）
     */
    @NotBlank(message = "点赞类型不能为空")
    @Size(max = 20, message = "点赞类型长度不能超过20个字符")
    private String targetType;
    
    /**
     * 点赞目标ID
     */
    @NotBlank(message = "点赞目标ID不能为空")
    private String targetId;
}
