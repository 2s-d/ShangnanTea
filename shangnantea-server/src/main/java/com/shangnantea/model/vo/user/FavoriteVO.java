package com.shangnantea.model.vo.user;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 收藏视图对象
 *
 * <p>
 * 说明：为满足“我的主页-收藏”完整展示需求，
 * 此对象在最初基础字段(itemType/itemId/targetName/targetImage/createTime)
 * 之上扩展了针对不同收藏类型的富展示字段。
 * </p>
 */
@Data
public class FavoriteVO {
    /**
     * 收藏ID
     */
    private Integer id;

    /**
     * 收藏项类型：tea / post / tea_article
     */
    private String itemType;

    /**
     * 收藏项ID
     */
    private String itemId;

    /**
     * 目标名称（兼容历史数据）
     */
    private String targetName;

    /**
     * 目标图片（兼容历史数据）
     */
    private String targetImage;

    /**
     * 收藏时间
     */
    private Date createTime;

    // ==================== 茶叶收藏专用字段（itemType = tea） ====================

    /**
     * 茶叶名称
     */
    private String teaName;

    /**
     * 茶叶主图
     */
    private String teaMainImage;

    /**
     * 茶叶价格
     */
    private BigDecimal teaPrice;

    /**
     * 茶叶所属店铺ID
     */
    private String teaShopId;

    /**
     * 茶叶所属店铺名称
     */
    private String teaShopName;

    /**
     * 茶叶所属店铺Logo
     */
    private String teaShopLogo;

    // ==================== 茶文化文章收藏字段（itemType = tea_article） ====================

    /**
     * 文章标题
     */
    private String articleTitle;

    /**
     * 文章摘要
     */
    private String articleSummary;

    /**
     * 文章封面图
     */
    private String articleCoverImage;

    /**
     * 文章发布时间
     */
    private Date articlePublishTime;

    /**
     * 文章阅读量
     */
    private Integer articleViewCount;

    // ==================== 论坛帖子收藏字段（itemType = post） ====================

    /**
     * 帖子标题
     */
    private String postTitle;

    /**
     * 帖子摘要
     */
    private String postSummary;

    /**
     * 帖子封面图片
     */
    private String postCoverImage;

    /**
     * 发帖人用户ID
     */
    private String postUserId;

    /**
     * 发帖人昵称
     */
    private String postNickname;

    /**
     * 发帖人头像
     */
    private String postUserAvatar;

    /**
     * 帖子发布时间
     */
    private Date postPublishTime;

    /**
     * 帖子浏览量
     */
    private Integer postViewCount;

    /**
     * 帖子回复数
     */
    private Integer postReplyCount;
    
    /**
     * 帖子点赞数
     */
    private Integer postLikeCount;
    
    /**
     * 帖子收藏数
     */
    private Integer postFavoriteCount;
}
