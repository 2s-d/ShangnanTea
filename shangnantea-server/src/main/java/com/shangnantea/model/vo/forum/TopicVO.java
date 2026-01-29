package com.shangnantea.model.vo.forum;

import lombok.Data;

import java.util.Date;

/**
 * 版块列表VO
 */
@Data
public class TopicVO {
    /**
     * 版块ID
     */
    private Integer id;
    
    /**
     * 版块名称
     */
    private String name;
    
    /**
     * 版块描述
     */
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
     * 帖子数量
     */
    private Integer postCount;
    
    /**
     * 创建时间
     */
    private Date createTime;
}
