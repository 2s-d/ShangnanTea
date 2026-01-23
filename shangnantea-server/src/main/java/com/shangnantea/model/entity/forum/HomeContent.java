package com.shangnantea.model.entity.forum;

import lombok.Data;

import java.util.Date;

/**
 * 首页内容配置实体类
 */
@Data
public class HomeContent {
    /**
     * 内容ID
     */
    private Integer id;
    
    /**
     * 区域标识(banner,recommend,feature等)
     */
    private String section;
    
    /**
     * 区域标题
     */
    private String title;
    
    /**
     * 区域副标题
     */
    private String subTitle;
    
    /**
     * 内容JSON格式
     */
    private String content;
    
    /**
     * 链接地址
     */
    private String linkUrl;
    
    /**
     * 内容类型(image,article等)
     */
    private String type;
    
    /**
     * 排序值
     */
    private Integer sortOrder;
    
    /**
     * 状态(1显示,0隐藏)
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