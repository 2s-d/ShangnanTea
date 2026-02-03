package com.shangnantea.service;

import com.shangnantea.common.api.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 店铺服务接口
 * 包含26个店铺模块接口的定义
 */
public interface ShopService {
    
    // ==================== 接口1-7：基础店铺管理 ====================
    
    /**
     * 接口1：获取店铺列表
     * 支持分页、搜索、筛选和排序
     *
     * @param params 查询参数（page, size, keyword, rating, sortBy, sortOrder等）
     * @return 店铺列表分页结果
     */
    Result<Object> getShops(Map<String, Object> params);
    
    /**
     * 接口2：创建店铺（商家）
     * 验证商家认证状态，验证店铺名称唯一性
     *
     * @param shopData 店铺数据
     * @return 创建结果
     */
    Result<Object> createShop(Map<String, Object> shopData);
    
    /**
     * 接口3：获取店铺详情
     * 根据店铺ID获取店铺详细信息
     * 改造说明：返回的店铺详情中包含 isFollowed 字段（当前用户是否已关注该店铺）
     *
     * @param id 店铺ID
     * @return 店铺详情（包含 isFollowed 字段）
     */
    Result<Object> getShopDetail(String id);
    
    /**
     * 接口4：更新店铺信息（商家）
     * 验证店铺所有权，更新店铺信息
     *
     * @param id 店铺ID
     * @param shopData 店铺数据
     * @return 更新结果
     */
    Result<Boolean> updateShop(String id, Map<String, Object> shopData);
    
    /**
     * 接口5：获取我的店铺信息
     * 获取当前登录商家的店铺信息
     *
     * @return 店铺信息
     */
    Result<Object> getMyShop();
    
    /**
     * 接口6：获取店铺统计数据
     * 获取指定店铺的统计信息
     *
     * @param shopId 店铺ID
     * @param params 查询参数
     * @return 店铺统计数据
     */
    Result<Object> getShopStatistics(String shopId, Map<String, Object> params);
    
    /**
     * 接口7：上传店铺Logo
     *
     * @param shopId 店铺ID
     * @param image 图片文件
     * @return 上传结果
     */
    Result<Map<String, Object>> uploadShopLogo(String shopId, MultipartFile image);
    
    // ==================== 接口8-14：茶叶管理 ====================
    
    /**
     * 接口8：获取店铺茶叶列表
     * 获取指定店铺的茶叶商品列表（游客可见）
     *
     * @param shopId 店铺ID
     * @param params 查询参数（page, size等）
     * @return 茶叶列表
     */
    Result<Object> getShopTeas(String shopId, Map<String, Object> params);
    
    /**
     * 接口9：添加店铺茶叶
     * 商家为店铺添加新的茶叶商品
     *
     * @param shopId 店铺ID
     * @param teaData 茶叶数据
     * @return 添加结果
     */
    Result<Object> addShopTea(String shopId, Map<String, Object> teaData);
    
    /**
     * 接口10：更新店铺茶叶
     * 商家更新店铺中的茶叶信息
     *
     * @param teaId 茶叶ID
     * @param teaData 茶叶数据
     * @return 更新结果
     */
    Result<Boolean> updateShopTea(String teaId, Map<String, Object> teaData);
    
    /**
     * 接口11：删除店铺茶叶
     * 商家删除店铺中的茶叶（逻辑删除）
     *
     * @param teaId 茶叶ID
     * @return 删除结果
     */
    Result<Object> deleteShopTea(String teaId);
    
    /**
     * 接口12-13：茶叶上下架
     * 商家更新店铺茶叶的上下架状态
     *
     * @param teaId 茶叶ID
     * @param status 状态（0=下架, 1=上架）
     * @return 操作结果
     */
    Result<Object> toggleShopTeaStatus(String teaId, Integer status);
    
    /**
     * 接口14：上传店铺Banner
     * 商家上传店铺Banner图片
     *
     * @param shopId 店铺ID
     * @param file Banner图片文件
     * @param title Banner标题
     * @param linkUrl 跳转链接
     * @return 上传结果
     */
    Result<Object> uploadBanner(String shopId, MultipartFile file, String title, String linkUrl);
    
    // ==================== 接口15-21：Banner和公告管理 ====================
    
    /**
     * 接口15：获取店铺Banner列表
     * 获取指定店铺的轮播Banner列表
     *
     * @param shopId 店铺ID
     * @return Banner列表
     */
    Result<Object> getShopBanners(String shopId);
    
    /**
     * 接口16：更新店铺Banner
     * 商家更新Banner信息
     *
     * @param bannerId Banner ID
     * @param bannerData Banner数据
     * @return 更新结果
     */
    Result<Object> updateBanner(String bannerId, Map<String, Object> bannerData);
    
    /**
     * 接口17：删除店铺Banner
     * 商家删除Banner
     *
     * @param bannerId Banner ID
     * @return 删除结果
     */
    Result<Boolean> deleteBanner(String bannerId);
    
    /**
     * 接口18：更新Banner顺序
     * 商家批量更新Banner的显示顺序
     *
     * @param orderData 排序数据
     * @return 更新结果
     */
    Result<Boolean> updateBannerOrder(Map<String, Object> orderData);
    
    /**
     * 接口19：获取店铺公告列表
     * 获取指定店铺的公告列表
     *
     * @param shopId 店铺ID
     * @return 公告列表
     */
    Result<Object> getShopAnnouncements(String shopId);
    
    /**
     * 接口20：创建店铺公告
     * 商家创建店铺公告
     *
     * @param shopId 店铺ID
     * @param announcementData 公告数据
     * @return 创建结果
     */
    Result<Object> createAnnouncement(String shopId, Map<String, Object> announcementData);
    
    /**
     * 接口21：更新店铺公告
     * 商家更新店铺公告
     *
     * @param announcementId 公告ID
     * @param announcementData 公告数据
     * @return 更新结果
     */
    Result<Object> updateAnnouncement(String announcementId, Map<String, Object> announcementData);
    
    /**
     * 接口22：删除店铺公告
     * 商家删除店铺公告
     *
     * @param announcementId 公告ID
     * @return 删除结果
     */
    Result<Boolean> deleteAnnouncement(String announcementId);
    
    // ==================== 接口23-26：评价 ====================
    // ⚠️ 已删除：店铺关注相关接口（followShop, unfollowShop, checkFollowStatus）
    // 说明：店铺关注功能已统一使用用户模块的通用接口（UserService 中的 addFollow/removeFollow）
    // 店铺详情接口（getShopDetail）已包含 isFollowed 字段，无需单独检查关注状态
    
    /**
     * 接口26：获取店铺评价信息
     * 获取店铺的评分统计信息
     *
     * @param shopId 店铺ID
     * @param params 查询参数
     * @return 评分信息
     */
    Result<Object> getShopReviews(String shopId, Map<String, Object> params);
    
    /**
     * 接口27：提交店铺评价
     * 用户提交对店铺的评分
     *
     * @param shopId 店铺ID
     * @param reviewData 评价数据
     * @return 提交结果
     */
    Result<Boolean> submitShopReview(String shopId, Map<String, Object> reviewData);
} 