package com.shangnantea.model.vo.message;

import lombok.Data;

import java.util.Date;

/**
 * 帖子VO
 */
@Data
public class PostVO {
    /**
     * 帖子ID
     */
    private Long id;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 浏览数
     */
    private Integer viewCount;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 创建时间
     */
    private Date createTime;
}
