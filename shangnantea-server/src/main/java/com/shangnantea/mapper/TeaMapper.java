package com.shangnantea.mapper;

import com.shangnantea.model.entity.tea.Tea;
import org.apache.ibatis.annotations.Param;

/**
 * 茶叶Mapper接口
 */
public interface TeaMapper extends BaseMapper<Tea, Long> {
    
    /**
     * 更新库存和销量（扣减库存，增加销量）
     * @param id 茶叶ID
     * @param quantity 数量
     * @return 影响行数（0表示库存不足或商品不存在）
     */
    int updateStockAndSales(@Param("id") String id, @Param("quantity") Integer quantity);
    
    /**
     * 恢复库存和销量（增加库存，减少销量）
     * 用于取消订单或退款
     * @param id 茶叶ID
     * @param quantity 数量
     * @return 影响行数
     */
    int restoreStockAndSales(@Param("id") String id, @Param("quantity") Integer quantity);
} 