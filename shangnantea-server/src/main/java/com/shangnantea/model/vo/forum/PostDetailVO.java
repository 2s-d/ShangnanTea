package com.shangnantea.model.vo.forum;

import lombok.Data;

import java.util.Date;

/**
 * 帖子详情VO
 */
@Data
public class PostDetailVO {
    
    /**
     * 帖子ID
     */
    private Long id;
    
    /**
     * 发帖用户ID
     */
    private String userId;
    
    /**
     * 发帖用户名
     */
    private String userName;
    
    /**
     * 用户头像
     */
    private String userAvatar;
    
    /**
     * 所属版块ID
     */
    private Integer topicId;
    
    /**
     * 版块名称
     */
    private String topicName;
    
    /**
     * 帖子标题
     */
    private String title;
    
    /**
     * 帖子内容
     */
    private String content;
    
    /**
     * 帖子摘要
     */
    private String summary;
    
    /**
     * 封面图片
     */
    private String coverImage;
    
    /**
     * 图片JSON数组
     */
    private String images;
    
    /**
     * 浏览量
     */
    private Integer viewCount;
    
    /**
     * 回复数
     */
    private Integer replyCount;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 收藏数
     */
    private Integer favoriteCount;
    
    /**
     * 是否置顶
     */
    private Integer isSticky;
    
    /**
     * 是否精华
     */
    private Integer isEssence;
    
    /**
     * 状态(1正常,0待审核,2已删除)
     */
    private Integer status;
    
    /**
     * 最后回复时间
     */
    private Date lastReplyTime;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 当前用户是否已点赞该帖子
     */
    private Boolean isLiked;
    
    /**
     * 当前用户是否已收藏该帖子
     */
    private Boolean isFavorited;
}
