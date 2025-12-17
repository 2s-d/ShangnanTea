package com.shangnantea.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 茶叶视图对象，用于返回给前端展示
 */
@Data
public class TeaVO {

    /**
     * 茶叶ID
     */
    private String id;
    
    /**
     * 茶叶名称
     */
    private String name;
    
    /**
     * 所属商家ID
     */
    private String shopId;
    
    /**
     * 所属商家名称
     */
    private String shopName;
    
    /**
     * 分类ID
     */
    private Integer categoryId;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 基础价格
     */
    private BigDecimal price;
    
    /**
     * 茶叶描述
     */
    private String description;
    
    /**
     * 详细介绍，支持富文本
     */
    private String detail;
    
    /**
     * 产地
     */
    private String origin;
    
    /**
     * 总库存
     */
    private Integer stock;
    
    /**
     * 销量
     */
    private Integer sales;
    
    /**
     * 规格列表
     */
    private List<TeaSpecificationVO> specifications;
    
    /**
     * 图片列表
     */
    private List<TeaImageVO> images;
    
    /**
     * 主图URL
     */
    private String mainImage;
    
    /**
     * 状态(0下架,1上架)
     */
    private Integer status;
    
    /**
     * 评分
     */
    private BigDecimal rating;
    
    /**
     * 评价数量
     */
    private Integer reviewCount;
    
    /**
     * 是否已收藏
     */
    private Boolean isFavorite;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 茶叶规格视图对象
     */
    @Data
    public static class TeaSpecificationVO {
        /**
         * 规格ID
         */
        private Integer id;
        
        /**
         * 规格名称
         */
        private String specName;
        
        /**
         * 规格价格
         */
        private BigDecimal price;
        
        /**
         * 规格库存
         */
        private Integer stock;
        
        /**
         * 是否默认规格
         */
        private Integer isDefault;
    }
    
    /**
     * 茶叶图片视图对象
     */
    @Data
    public static class TeaImageVO {
        /**
         * 图片ID
         */
        private Integer id;
        
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
    }
} 