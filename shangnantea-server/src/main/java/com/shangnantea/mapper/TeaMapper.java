package com.shangnantea.mapper;

import com.shangnantea.model.entity.tea.Tea;

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
     * 逻辑删除茶叶（软删除）
     *
     * @param id 茶叶ID
     * @return 影响行数
     */
    int delete(String id);
    
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
}
