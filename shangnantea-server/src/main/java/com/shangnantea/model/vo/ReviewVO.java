package com.shangnantea.model.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 评价VO类
 */
@Data
public class ReviewVO {
    /**
     * 评价ID
     */
    private Integer id;
    
    /**
     * 茶叶ID
     */
    private String teaId;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户头像
     */
    private String avatar;
    
    /**
     * 评价内容
     */
    private String content;
    
    /**
     * 评分(1-5)
     */
    private Integer rating;
    
    /**
     * 评价图片列表
     */
    private List<String> images;
    
    /**
     * 商家回复
     */
    private String reply;
    
    /**
     * 回复时间
     */
    private Date replyTime;
    
    /**
     * 是否匿名
     */
    private Integer isAnonymous;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 当前用户是否已点赞该评价
     */
    private Boolean isLiked;
}
