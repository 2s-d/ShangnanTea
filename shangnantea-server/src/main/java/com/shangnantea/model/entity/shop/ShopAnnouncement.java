package com.shangnantea.model.entity.shop;

import lombok.Data;

import java.util.Date;

/**
 * 店铺公告实体类
 */
@Data
public class ShopAnnouncement {
    /**
     * 公告ID
     */
    private Integer id;
    
    /**
     * 店铺ID
     */
    private String shopId;
    
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
    private Integer isTop;
    
    /**
     * 状态:1显示,0隐藏
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
