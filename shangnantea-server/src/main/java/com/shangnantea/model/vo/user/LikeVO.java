package com.shangnantea.model.vo.user;

import lombok.Data;

import java.util.Date;

/**
 * 点赞视图对象
 */
@Data
public class LikeVO {
    /**
     * 点赞ID
     */
    private Integer id;
    
    /**
     * 点赞目标类型
     */
    private String targetType;
    
    /**
     * 点赞目标ID
     */
    private String targetId;
    
    /**
     * 创建时间
     */
    private Date createTime;
}
