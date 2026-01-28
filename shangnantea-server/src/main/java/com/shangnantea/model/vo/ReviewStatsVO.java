package com.shangnantea.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 评价统计VO类
 */
@Data
public class ReviewStatsVO {
    /**
     * 平均评分
     */
    private BigDecimal averageRating;
    
    /**
     * 总评价数
     */
    private Integer totalCount;
    
    /**
     * 评分分布
     * key: 评分(1-5)
     * value: 该评分的数量
     */
    private Map<String, Integer> ratingDistribution;
}
