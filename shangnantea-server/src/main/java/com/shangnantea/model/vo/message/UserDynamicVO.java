package com.shangnantea.model.vo.message;

import lombok.Data;

import java.util.Date;

/**
 * 用户动态VO
 */
@Data
public class UserDynamicVO {
    /**
     * 动态ID
     */
    private Long id;
    
    /**
     * 动态类型（post-发帖，comment-评论，like-点赞等）
     */
    private String type;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 内容
     */
    private String content;
    
    /**
     * 创建时间
     */
    private Date createTime;
}
