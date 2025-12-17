package com.shangnantea.model.entity.tea;

import lombok.Data;

import java.util.Date;

/**
 * 茶叶图片实体类
 */
@Data
public class TeaImage {
    /**
     * 图片ID
     */
    private Integer id;
    
    /**
     * 茶叶ID
     */
    private String teaId;
    
    /**
     * 图片URL
     */
    private String url;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 是否主图
     */
    private Integer isMain;
    
    /**
     * 创建时间
     */
    private Date createTime;
} 