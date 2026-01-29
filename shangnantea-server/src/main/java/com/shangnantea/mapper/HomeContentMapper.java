package com.shangnantea.mapper;

import com.shangnantea.model.entity.forum.HomeContent;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 首页内容Mapper接口
 */
public interface HomeContentMapper extends BaseMapper<HomeContent, Integer> {
    
    /**
     * 根据section和status查询内容列表
     * @param section 区域标识
     * @param status 状态（1显示，0隐藏）
     * @return 内容列表
     */
    List<HomeContent> selectBySection(@Param("section") String section, @Param("status") Integer status);
    
    /**
     * 批量更新内容
     * @param contents 内容列表
     * @return 更新数量
     */
    int batchUpdate(@Param("contents") List<HomeContent> contents);
    
    /**
     * 根据ID列表查询内容
     * @param ids ID列表
     * @return 内容列表
     */
    List<HomeContent> selectByIds(@Param("ids") List<Integer> ids);
} 