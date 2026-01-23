package com.shangnantea.model.vo.shop;

import java.math.BigDecimal;

/**
 * 店铺统计信息VO
 */
public class ShopStatisticsVO {
    
    /**
     * 总销售额
     */
    private BigDecimal totalSales;
    
    /**
     * 订单数量
     */
    private Integer orderCount;
    
    /**
     * 商品数量
     */
    private Integer productCount;
    
    /**
     * 关注数量
     */
    private Integer followCount;
    
    /**
     * 评价数量
     */
    private Integer ratingCount;
    
    /**
     * 平均评分
     */
    private BigDecimal rating;

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public Integer getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Integer orderCount) {
        this.orderCount = orderCount;
    }

    public Integer getProductCount() {
        return productCount;
    }

    public void setProductCount(Integer productCount) {
        this.productCount = productCount;
    }

    public Integer getFollowCount() {
        return followCount;
    }

    public void setFollowCount(Integer followCount) {
        this.followCount = followCount;
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }
}
