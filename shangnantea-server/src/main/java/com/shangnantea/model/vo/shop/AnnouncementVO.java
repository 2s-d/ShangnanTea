package com.shangnantea.model.vo.shop;

import lombok.Data;

import java.util.Date;

/**
 * 店铺公告 VO
 */
@Data
public class AnnouncementVO {
    /**
     * 公告ID
     */
    private Integer id;
    
    /**
     * 公告标题
     */
    private String title;
    
    /**
     * 公告内容
     */
    private String content;
    
    /**
     * 是否置顶
     */
    private Boolean isTop;
    
    /**
     * 创建时间
     */
    private Date createTime;
}
