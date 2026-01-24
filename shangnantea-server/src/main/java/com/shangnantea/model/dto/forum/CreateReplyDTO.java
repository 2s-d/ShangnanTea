package com.shangnantea.model.dto.forum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建回复DTO
 */
@Data
public class CreateReplyDTO {
    /**
     * 回复内容
     */
    @NotBlank(message = "回复内容不能为空")
    @Size(min = 1, max = 500, message = "回复内容长度必须在1-500字符之间")
    private String content;
    
    /**
     * 父回复ID（回复评论时使用）
     */
    private String parentId;
}
