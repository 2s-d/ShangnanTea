package com.shangnantea.mapper;

import com.shangnantea.model.entity.tea.TeaReview;

import java.util.List;
import java.util.Map;

/**
 * 茶叶评价Mapper接口
 */
public interface TeaReviewMapper extends BaseMapper<TeaReview, Long> {
    
    /**
     * 根据茶叶ID分页查询评价列表
     *
     * @param params 查询参数（teaId, offset, limit等）
     * @return 评价列表
     */
    List<TeaReview> selectByTeaIdWithPage(Map<String, Object> params);
    
    /**
     * 统计茶叶评价数量
     *
     * @param teaId 茶叶ID
     * @return 评价数量
     */
    int countByTeaId(String teaId);
    
    /**
     * 查询茶叶评分统计
     *
     * @param teaId 茶叶ID
     * @return 统计数据Map
     */
    Map<String, Object> selectRatingStats(String teaId);
    
    /**
     * 根据订单ID查询评价
     *
     * @param orderId 订单ID
     * @return 评价对象
     */
    TeaReview selectByOrderId(String orderId);
    
    /**
     * 商家回复评价
     *
     * @param params 回复参数（id, reply）
     * @return 影响行数
     */
    int reply(Map<String, Object> params);
    
    /**
     * 增加评价点赞数
     *
     * @param reviewId 评价ID
     * @return 影响行数
     */
    int incrementLikeCount(Long reviewId);
}