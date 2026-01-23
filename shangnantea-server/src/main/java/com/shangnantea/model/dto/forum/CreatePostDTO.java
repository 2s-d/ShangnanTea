package com.shangnantea.model.dto.forum;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建帖子DTO
 */
@Data
public class CreatePostDTO {
    
    /**
     * 所属版块ID
     */
    @NotNull(message = "版块ID不能为空")
    private Integer topicId;
    
    /**
     * 帖子标题
     */
    @NotBlank(message = "帖子标题不能为空")
    @Size(max = 100, message = "帖子标题长度不能超过100个字符")
    private String title;
    
    /**
     * 帖子内容
     */
    @NotBlank(message = "帖子内容不能为空")
    @Size(max = 10000, message = "帖子内容长度不能超过10000个字符")
    private String content;
    
    /**
     * 帖子摘要
     */
    @Size(max = 500, message = "帖子摘要长度不能超过500个字符")
    private String summary;
    
    /**
     * 封面图片
     */
    private String coverImage;
    
    /**
     * 图片JSON数组
     */
    private String images;
}
