package com.shangnantea.mapper;

import com.shangnantea.model.entity.tea.Tea;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 茶叶Mapper接口
 */
public interface TeaMapper extends BaseMapper<Tea, Long> {
    
    /**
     * 分页查询茶叶列表（支持多条件筛选）
     *
     * @param params 查询参数Map
     * @return 茶叶列表
     */
    List<Tea> selectTeasWithPage(Map<String, Object> params);
    
    /**
     * 统计符合条件的茶叶总数
     *
     * @param params 查询参数Map
     * @return 总数
     */
    long countTeas(Map<String, Object> params);

    /**
     * 分页查询店铺茶叶列表
     *
     * @param shopId 店铺ID
     * @param offset 分页偏移量
     * @param pageSize 每页记录数
     * @return 茶叶列表
     */
    List<Tea> selectByShopId(@Param("shopId") String shopId,
                             @Param("offset") Integer offset,
                             @Param("pageSize") Integer pageSize);

    /**
     * 统计店铺茶叶总数
     *
     * @param shopId 店铺ID
     * @return 茶叶总数
     */
    Long countByShopId(@Param("shopId") String shopId);
    
    /**
     * 逻辑删除茶叶（软删除）
     *
     * @param id 茶叶ID
     * @return 影响行数
     */
    int delete(String id);

    /**
     * 逻辑删除茶叶（设置is_deleted=1）
     * 兼容：为店铺模块提供Long类型ID的删除接口，不影响原有delete(String)调用。
     *
     * @param id 茶叶ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 统计指定分类下的茶叶数量
     *
     * @param categoryId 分类ID
     * @return 茶叶数量
     */
    int countByCategory(Integer categoryId);
    
    /**
     * 更新茶叶状态（上架/下架）
     *
     * @param id 茶叶ID
     * @param status 状态（1-上架，0-下架）
     * @return 影响行数
     */
    int updateStatus(String id, Integer status);
    
    /**
     * 批量更新茶叶状态
     *
     * @param teaIds 茶叶ID列表
     * @param status 状态（1-上架，0-下架）
     * @return 影响行数
     */
    int batchUpdateStatus(List<String> teaIds, Integer status);
    
    /**
     * 更新库存和销量（扣减库存，增加销量）
     *
     * @param id 茶叶ID
     * @param quantity 数量
     * @return 影响行数（0表示库存不足或商品不存在）
     */
    int updateStockAndSales(@Param("id") String id, @Param("quantity") Integer quantity);
    
    /**
     * 恢复库存和销量（增加库存，减少销量）
     * 用于取消订单或退款
     *
     * @param id 茶叶ID
     * @param quantity 数量
     * @return 影响行数
     */
    int restoreStockAndSales(@Param("id") String id, @Param("quantity") Integer quantity);
}
