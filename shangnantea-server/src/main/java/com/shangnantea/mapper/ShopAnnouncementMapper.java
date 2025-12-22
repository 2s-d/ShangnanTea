package com.shangnantea.mapper;

import com.shangnantea.model.entity.shop.ShopAnnouncement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 店铺公告 Mapper接口
 */
@Mapper
public interface ShopAnnouncementMapper {
    
    /**
     * 根据ID查询
     */
    ShopAnnouncement selectById(@Param("id") Integer id);
    
    /**
     * 根据店铺ID查询公告列表
     */
    List<ShopAnnouncement> selectByShopId(@Param("shopId") String shopId);
    
    /**
     * 插入记录
     */
    int insert(ShopAnnouncement announcement);
    
    /**
     * 更新记录
     */
    int updateById(ShopAnnouncement announcement);
    
    /**
     * 删除记录
     */
    int deleteById(@Param("id") Integer id);
}
