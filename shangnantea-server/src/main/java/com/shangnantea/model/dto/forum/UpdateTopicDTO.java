package com.shangnantea.model.dto.forum;

import lombok.Data;

import jakarta.validation.constraints.Size;

/**
 * 更新版块DTO
 */
@Data
public class UpdateTopicDTO {
    
    /**
     * 版块名称
     */
    @Size(max = 50, message = "版块名称长度不能超过50个字符")
    private String name;
    
    /**
     * 版块描述
     */
    @Size(max = 500, message = "版块描述长度不能超过500个字符")
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
     * 排序值
     */
    private Integer sortOrder;
    
    /**
     * 状态(1启用,0禁用)
     */
    private Integer status;
}
