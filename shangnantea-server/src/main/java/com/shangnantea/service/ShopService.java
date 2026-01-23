package com.shangnantea.service;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.common.api.Result;
import com.shangnantea.model.entity.shop.Shop;
import com.shangnantea.model.entity.shop.ShopCertification;
import com.shangnantea.model.entity.tea.Tea;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 店铺服务接口
 */
public interface ShopService {
    
    /**
     * 获取店铺详情
     *
     * @param id 店铺ID
     * @return 店铺信息
     */
    Shop getShopById(String id);
    
    /**
     * 分页查询店铺
     *
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<Shop> listShops(PageParam pageParam);
    
    /**
     * 获取商家的店铺
     *
     * @param userId 用户ID
     * @return 店铺信息
     */
    Shop getShopByUserId(String userId);
    
    /**
     * 创建店铺
     *
     * @param shop 店铺信息
     * @return 店铺信息
     */
    Shop createShop(Shop shop);
    
    /**
     * 更新店铺信息
     *
     * @param shop 店铺信息
     * @return 是否成功
     */
    boolean updateShop(Shop shop);
    
    /**
     * 获取店铺的茶叶
     *
     * @param shopId 店铺ID
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<Tea> listShopTeas(String shopId, PageParam pageParam);
    
    /**
     * 创建商家认证申请
     *
     * @param certification 认证信息
     * @return 认证信息
     */
    ShopCertification createCertification(ShopCertification certification);
    
    /**
     * 获取认证申请
     *
     * @param id 认证ID
     * @return 认证信息
     */
    ShopCertification getCertificationById(Integer id);
    
    /**
     * 获取用户的认证申请
     *
     * @param userId 用户ID
     * @return 认证信息
     */
    ShopCertification getCertificationByUserId(String userId);
    
    /**
     * 查询认证申请列表
     *
     * @param status 状态
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<ShopCertification> listCertifications(Integer status, PageParam pageParam);
    
    /**
     * 处理认证申请
     *
     * @param id 认证ID
     * @param status 状态
     * @param adminId 管理员ID
     * @param rejectReason 拒绝原因
     * @return 是否成功
     */
    boolean processCertification(Integer id, Integer status, String adminId, String rejectReason);
    
    /**
     * 确认通知
     *
     * @param certificationId 认证ID
     * @return 是否成功
     */
    boolean confirmNotification(Integer certificationId);
    
    /**
     * 上传商家认证图片
     *
     * @param image 图片文件
     * @return 上传结果
     */
    Result<Map<String, Object>> uploadCertificationImage(MultipartFile image);
    
    /**
     * 上传店铺Logo
     *
     * @param shopId 店铺ID
     * @param image 图片文件
     * @return 上传结果
     */
    Result<Map<String, Object>> uploadShopLogo(String shopId, MultipartFile image);
    
    /**
     * 上传店铺Banner
     * 商家上传店铺Banner图片
     *
     * @param shopId 店铺ID
     * @param file Banner图片文件
     * @param title Banner标题
     * @param linkUrl 跳转链接
     * @return 上传结果
     */
    Result<Object> uploadBanner(String shopId, MultipartFile file, String title, String linkUrl);
}
     * 支持分页、搜索、筛选和排序
     *
     * @param params 查询参数（page, size, keyword, rating, sortBy, sortOrder等）
     * @return 店铺列表分页结果
     */
    Result<Object> getShops(Map<String, Object> params);
    
    /**
     * 创建店铺（商家）
     * 验证商家认证状态，验证店铺名称唯一性
     *
     * @param shopData 店铺数据
     * @return 创建结果
     */
    Result<Object> createShop(Map<String, Object> shopData);
    
    /**
     * 获取店铺详情
     * 根据店铺ID获取店铺详细信息
     *
     * @param id 店铺ID
     * @return 店铺详情
     */
    Result<Object> getShopDetail(String id);
    
    /**
     * 更新店铺信息（商家）
     * 验证店铺所有权，更新店铺信息
     *
     * @param id 店铺ID
     * @param shopData 店铺数据
     * @return 更新结果
     */
    Result<Boolean> updateShop(String id, Map<String, Object> shopData);
    
    /**
     * 获取我的店铺信息
     * 获取当前登录商家的店铺信息
     *
     * @return 店铺信息
     */
    Result<Object> getMyShop();
    
    /**
     * 获取店铺统计数据
     * 获取指定店铺的统计信息
     *
     * @param shopId 店铺ID
     * @param params 查询参数
     * @return 店铺统计数据
     */
    Result<Object> getShopStatistics(String shopId, Map<String, Object> params);
    
    /**
     * 获取店铺茶叶列表
     * 获取指定店铺的茶叶商品列表（游客可见）
     *
     * @param shopId 店铺ID
     * @param params 查询参数（page, size等）
     * @return 茶叶列表
     */
    Result<Object> getShopTeas(String shopId, Map<String, Object> params);
    
    /**
     * 添加店铺茶叶
     * 商家为店铺添加新的茶叶商品
     *
     * @param shopId 店铺ID
     * @param teaData 茶叶数据
     * @return 添加结果
     */
    Result<Object> addShopTea(String shopId, Map<String, Object> teaData);
    
    /**
     * 更新店铺茶叶
     * 商家更新店铺中的茶叶信息
     *
     * @param teaId 茶叶ID
     * @param teaData 茶叶数据
     * @return 更新结果
     */
    Result<Boolean> updateShopTea(String teaId, Map<String, Object> teaData);
    
    /**
     * 删除店铺茶叶
     * 商家删除店铺中的茶叶（逻辑删除）
     *
     * @param teaId 茶叶ID
     * @return 删除结果
     */
    Result<Object> deleteShopTea(String teaId);
    
    /**
     * 茶叶上下架
     * 商家更新店铺茶叶的上下架状态
     *
     * @param teaId 茶叶ID
     * @param status 状态（0=下架, 1=上架）
     * @return 操作结果
     */
    Result<Object> toggleShopTeaStatus(String teaId, Integer status);
    
    /**
     * 获取店铺Banner列表
     * 获取指定店铺的轮播Banner列表
     *
     * @param shopId 店铺ID
     * @return Banner列表
     */
    Result<Object> getShopBanners(String shopId);
    
    /**
     * 更新店铺Banner
     * 商家更新Banner信息
     *
     * @param bannerId Banner ID
     * @param bannerData Banner数据
     * @return 更新结果
     */
    Result<Object> updateBanner(String bannerId, Map<String, Object> bannerData);
    
    /**
     * 删除店铺Banner
     * 商家删除Banner
     *
     * @param bannerId Banner ID
     * @return 删除结果
     */
    Result<Boolean> deleteBanner(String bannerId);
    
    /**
     * 更新Banner顺序
     * 商家批量更新Banner的显示顺序
     *
     * @param orderData 排序数据
     * @return 更新结果
     */
    Result<Boolean> updateBannerOrder(Map<String, Object> orderData);
    
    /**
     * 获取店铺公告列表
     * 获取指定店铺的公告列表
     *
     * @param shopId 店铺ID
     * @return 公告列表
     */
    Result<Object> getShopAnnouncements(String shopId);
    
    /**
     * 创建店铺公告
     * 商家创建店铺公告
     *
     * @param shopId 店铺ID
     * @param announcementData 公告数据
     * @return 创建结果
     */
    Result<Object> createAnnouncement(String shopId, Map<String, Object> announcementData);
    
    /**
     * 更新店铺公告
     * 商家更新店铺公告
     *
     * @param announcementId 公告ID
     * @param announcementData 公告数据
     * @return 更新结果
     */
    Result<Object> updateAnnouncement(String announcementId, Map<String, Object> announcementData);
    
    /**
     * 删除店铺公告
     * 商家删除店铺公告
     *
     * @param announcementId 公告ID
     * @return 删除结果
     */
    Result<Boolean> deleteAnnouncement(String announcementId);
    
    /**
     * 关注店铺
     * 用户关注店铺
     *
     * @param shopId 店铺ID
     * @return 关注结果
     */
    Result<Boolean> followShop(String shopId);
    
    /**
     * 取消关注店铺
     * 用户取消关注店铺
     *
     * @param shopId 店铺ID
     * @return 取消结果
     */
    Result<Boolean> unfollowShop(String shopId);
    
    /**
     * 检查关注状态
     * 获取当前用户对店铺的关注状态
     *
     * @param shopId 店铺ID
     * @return 关注状态
     */
    Result<Object> checkFollowStatus(String shopId);
    
    /**
     * 获取店铺评价列表
     * 获取店铺的用户评价列表
     *
     * @param shopId 店铺ID
     * @param params 查询参数（page, pageSize等）
     * @return 评价列表
     */
    Result<Object> getShopReviews(String shopId, Map<String, Object> params);
    
    /**
     * 提交店铺评价
     * 用户提交对店铺的评价
     *
     * @param shopId 店铺ID
     * @param reviewData 评价数据
     * @return 提交结果
     */
    Result<Boolean> submitShopReview(String shopId, Map<String, Object> reviewData);
} 