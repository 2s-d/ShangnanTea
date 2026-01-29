package com.shangnantea.model.vo.forum;

import lombok.Data;

import java.util.Date;

/**
 * 版块详情VO
 */
@Data
public class TopicDetailVO {
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
     * 版主用户ID
     */
    private String userId;
    
    /**
     * 版主名称
     */
    private String moderatorName;
    
    /**
     * 排序值
     */
    private Integer sortOrder;
    
    /**
     * 帖子总数
     */
    private Integer postCount;
    
    /**
     * 今日帖子数
     */
    private Integer todayPostCount;
    
    /**
     * 状态(1启用,0禁用)
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}
