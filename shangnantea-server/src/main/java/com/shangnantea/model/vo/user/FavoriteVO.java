package com.shangnantea.model.vo.user;

import lombok.Data;

import java.util.Date;

/**
 * 收藏视图对象
 */
@Data
public class FavoriteVO {
    /**
     * 收藏ID
     */
    private Integer id;
    
    /**
     * 收藏项类型
     */
    private String itemType;
    
    /**
     * 收藏项ID
     */
    private String itemId;
    
    /**
     * 目标名称
     */
    private String targetName;
    
    /**
     * 目标图片
     */
    private String targetImage;
    
    /**
     * 创建时间
     */
    private Date createTime;
}
