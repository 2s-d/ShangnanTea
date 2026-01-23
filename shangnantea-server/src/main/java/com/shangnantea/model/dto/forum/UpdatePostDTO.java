package com.shangnantea.model.dto.forum;

import lombok.Data;

import jakarta.validation.constraints.Size;

/**
 * 更新帖子DTO
 */
@Data
public class UpdatePostDTO {
    
    /**
     * 帖子标题
     */
    @Size(max = 100, message = "帖子标题长度不能超过100个字符")
    private String title;
    
    /**
     * 帖子内容
     */
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
