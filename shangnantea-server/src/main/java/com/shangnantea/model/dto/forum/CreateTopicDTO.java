package com.shangnantea.model.dto.forum;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建版块DTO
 */
@Data
public class CreateTopicDTO {
    
    /**
     * 版块名称
     */
    @NotBlank(message = "版块名称不能为空")
    @Size(max = 50, message = "版块名称长度不能超过50个字符")
    private String name;
    
    /**
     * 版块描述
     */
    @Size(max = 200, message = "版块描述长度不能超过200个字符")
    private String description;
    
    /**
     * 版块图标
     */
    private String icon;
    
    /**
     * 版块封面
     */
    private String cover;
    
    /**
     * 版主用户ID
     */
    private String userId;
    
    /**
     * 排序值
     */
    private Integer sortOrder;
}
