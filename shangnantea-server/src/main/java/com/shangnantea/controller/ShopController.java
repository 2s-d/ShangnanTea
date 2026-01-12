package com.shangnantea.controller;

import com.shangnantea.common.api.Result;
import com.shangnantea.security.annotation.RequiresLogin;
import com.shangnantea.security.annotation.RequiresRoles;
import com.shangnantea.service.ShopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 店铺控制器
 * 参考：前端 shop.js 和 code-message-mapping.md
 * 接口数量：28个（26个店铺接口 + 2个公共上传接口）
 */
@RestController
@RequestMapping({"/shop", "/api/shop", "/upload", "/api/upload"})
@Validated
public class ShopController {

    private static final Logger logger = LoggerFactory.getLogger(ShopController.class);

    @Autowired
    private ShopService shopService;

    // ==================== 店铺查询 ====================

    /**
     * 获取店铺列表
     * 路径: GET /shop/list
     * 成功码: 200, 失败码: 5101
     *
     * @param params 查询参数（page, size, keyword, rating, salesCount, region, sortBy, sortOrder等）
     * @return 店铺列表
     */
    @GetMapping("/list")
    public Result<Object> getShops(@RequestParam Map<String, Object> params) {
        logger.info("获取店铺列表请求, params: {}", params);
        return shopService.getShops(params);
    }

    /**
     * 获取当前商家的店铺信息
     * 路径: GET /shop/my
     * 成功码: 200, 失败码: 5100
     *
     * @return 我的店铺信息
     */
    @GetMapping("/my")
    @RequiresLogin
    public Result<Object> getMyShop() {
        logger.info("获取我的店铺信息请求");
        return shopService.getMyShop();
    }

    /**
     * 获取店铺茶叶列表
     * 路径: GET /shop/{shopId}/teas
     * 成功码: 200, 失败码: 5101
     *
     * @param shopId 店铺ID
     * @param params 查询参数（page, size等）
     * @return 店铺茶叶列表
     */
    @GetMapping("/{shopId}/teas")
    public Result<Object> getShopTeas(@PathVariable String shopId, @RequestParam Map<String, Object> params) {
        logger.info("获取店铺茶叶列表请求: {}, params: {}", shopId, params);
        return shopService.getShopTeas(shopId, params);
    }

    /**
     * 获取店铺统计数据
     * 路径: GET /shop/{shopId}/statistics
     * 成功码: 200, 失败码: 1102
     *
     * @param shopId 店铺ID
     * @param params 查询参数（startDate, endDate）
     * @return 店铺统计数据
     */
    @GetMapping("/{shopId}/statistics")
    @RequiresLogin
    public Result<Object> getShopStatistics(@PathVariable String shopId, @RequestParam Map<String, Object> params) {
        logger.info("获取店铺统计数据请求: {}, params: {}", shopId, params);
        return shopService.getShopStatistics(shopId, params);
    }

    /**
     * 获取店铺Banner列表
     * 路径: GET /shop/{shopId}/banners
     * 成功码: 200, 失败码: 1102
     *
     * @param shopId 店铺ID
     * @return Banner列表
     */
    @GetMapping("/{shopId}/banners")
    public Result<Object> getShopBanners(@PathVariable String shopId) {
        logger.info("获取店铺Banner列表请求: {}", shopId);
        return shopService.getShopBanners(shopId);
    }

    /**
     * 获取店铺公告列表
     * 路径: GET /shop/{shopId}/announcements
     * 成功码: 200, 失败码: 1102
     *
     * @param shopId 店铺ID
     * @return 公告列表
     */
    @GetMapping("/{shopId}/announcements")
    public Result<Object> getShopAnnouncements(@PathVariable String shopId) {
        logger.info("获取店铺公告列表请求: {}", shopId);
        return shopService.getShopAnnouncements(shopId);
    }

    /**
     * 获取店铺关注状态
     * 路径: GET /shop/{shopId}/follow-status
     * 成功码: 200, 失败码: 1102
     *
     * @param shopId 店铺ID
     * @return 关注状态
     */
    @GetMapping("/{shopId}/follow-status")
    @RequiresLogin
    public Result<Object> checkFollowStatus(@PathVariable String shopId) {
        logger.info("获取店铺关注状态请求: {}", shopId);
        return shopService.checkFollowStatus(shopId);
    }

    /**
     * 获取店铺评价列表
     * 路径: GET /shop/{shopId}/reviews
     * 成功码: 200, 失败码: 5103
     *
     * @param shopId 店铺ID
     * @param params 查询参数（page, size）
     * @return 评价列表
     */
    @GetMapping("/{shopId}/reviews")
    public Result<Object> getShopReviews(@PathVariable String shopId, @RequestParam Map<String, Object> params) {
        logger.info("获取店铺评价列表请求: {}, params: {}", shopId, params);
        return shopService.getShopReviews(shopId, params);
    }

    // ==================== 店铺管理（商家） ====================

    /**
     * 创建店铺（商家认证通过后自动创建）
     * 路径: POST /shop/list
     * 成功码: 1000, 失败码: 1100
     *
     * @param shopData 店铺数据（可选）
     * @return 创建结果
     */
    @PostMapping("/list")
    @RequiresLogin
    public Result<Object> createShop(@RequestBody Map<String, Object> shopData) {
        logger.info("创建店铺请求");
        return shopService.createShop(shopData);
    }

    /**
     * 更新店铺信息（商家）
     * 路径: PUT /shop/{id}
     * 成功码: 1004, 失败码: 1100
     *
     * @param id 店铺ID
     * @param shopData 店铺数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @RequiresLogin
    public Result<Boolean> updateShop(@PathVariable String id, @RequestBody Map<String, Object> shopData) {
        logger.info("更新店铺信息请求: {}", id);
        return shopService.updateShop(id, shopData);
    }

    /**
     * 上传店铺Logo
     * 路径: POST /shop/{shopId}/logo
     * 成功码: 5030, 失败码: 5130, 1103, 1104
     *
     * @param shopId 店铺ID
     * @param file Logo文件
     * @return 上传结果
     */
    @PostMapping("/{shopId}/logo")
    @RequiresLogin
    public Result<Map<String, Object>> uploadShopLogo(@PathVariable String shopId, 
                                                       @RequestParam("file") MultipartFile file) {
        logger.info("上传店铺Logo请求: {}, 文件名: {}", shopId, file.getOriginalFilename());
        return shopService.uploadShopLogo(shopId, file);
    }

    // ==================== 店铺茶叶管理（商家） ====================

    /**
     * 添加茶叶到店铺
     * 路径: POST /shop/{shopId}/teas
     * 成功码: 3026, 失败码: 3125
     *
     * @param shopId 店铺ID
     * @param teaData 茶叶数据
     * @return 添加结果
     */
    @PostMapping("/{shopId}/teas")
    @RequiresLogin
    public Result<Object> addShopTea(@PathVariable String shopId, @RequestBody Map<String, Object> teaData) {
        logger.info("添加店铺茶叶请求: {}", shopId);
        return shopService.addShopTea(shopId, teaData);
    }

    /**
     * 更新店铺茶叶
     * 路径: PUT /shop/teas/{teaId}
     * 成功码: 3025, 失败码: 3125
     *
     * @param teaId 茶叶ID
     * @param teaData 茶叶数据
     * @return 更新结果
     */
    @PutMapping("/teas/{teaId}")
    @RequiresLogin
    public Result<Boolean> updateShopTea(@PathVariable String teaId, @RequestBody Map<String, Object> teaData) {
        logger.info("更新店铺茶叶请求: {}", teaId);
        return shopService.updateShopTea(teaId, teaData);
    }

    /**
     * 删除店铺茶叶
     * 路径: DELETE /shop/teas/{teaId}
     * 成功码: 3024, 失败码: 3124
     *
     * @param teaId 茶叶ID
     * @return 删除结果
     */
    @DeleteMapping("/teas/{teaId}")
    @RequiresLogin
    public Result<Boolean> deleteShopTea(@PathVariable String teaId) {
        logger.info("删除店铺茶叶请求: {}", teaId);
        return shopService.deleteShopTea(teaId);
    }

    /**
     * 茶叶上下架
     * 路径: PUT /shop/teas/{teaId}/status
     * 成功码: 3020(上架), 3021(下架), 失败码: 3120, 3121
     *
     * @param teaId 茶叶ID
     * @param statusData 状态数据 {status}
     * @return 更新结果
     */
    @PutMapping("/teas/{teaId}/status")
    @RequiresLogin
    public Result<Boolean> toggleShopTeaStatus(@PathVariable String teaId, @RequestBody Map<String, Object> statusData) {
        logger.info("茶叶上下架请求: {}, status: {}", teaId, statusData.get("status"));
        return shopService.toggleShopTeaStatus(teaId, statusData);
    }

    // ==================== Banner管理（商家） ====================

    /**
     * 上传/创建店铺Banner
     * 路径: POST /shop/{shopId}/banners
     * 成功码: 5010, 失败码: 5111
     *
     * @param shopId 店铺ID
     * @param bannerData Banner数据（包含image、link_url、title等，FormData格式）
     * @return 创建后的Banner
     */
    @PostMapping("/{shopId}/banners")
    @RequiresLogin
    public Result<Object> uploadBanner(@PathVariable String shopId, 
                                      @RequestParam Map<String, Object> bannerData) {
        logger.info("上传店铺Banner请求: {}", shopId);
        return shopService.uploadBanner(shopId, bannerData);
    }

    /**
     * 更新店铺Banner
     * 路径: PUT /shop/banners/{bannerId}
     * 成功码: 5011, 失败码: 5111
     *
     * @param bannerId Banner ID
     * @param bannerData Banner数据
     * @return 更新后的Banner
     */
    @PutMapping("/banners/{bannerId}")
    @RequiresLogin
    public Result<Object> updateBanner(@PathVariable String bannerId, @RequestBody Map<String, Object> bannerData) {
        logger.info("更新店铺Banner请求: {}", bannerId);
        return shopService.updateBanner(bannerId, bannerData);
    }

    /**
     * 删除店铺Banner
     * 路径: DELETE /shop/banners/{bannerId}
     * 成功码: 5012, 失败码: 5112
     *
     * @param bannerId Banner ID
     * @return 删除结果
     */
    @DeleteMapping("/banners/{bannerId}")
    @RequiresLogin
    public Result<Boolean> deleteBanner(@PathVariable String bannerId) {
        logger.info("删除店铺Banner请求: {}", bannerId);
        return shopService.deleteBanner(bannerId);
    }

    /**
     * 更新Banner顺序
     * 路径: PUT /shop/banners/order
     * 成功码: 5013, 失败码: 5113
     *
     * @param orderData 排序数据 {orders: [{id, order}, ...]}
     * @return 更新结果
     */
    @PutMapping("/banners/order")
    @RequiresLogin
    public Result<Boolean> updateBannerOrder(@RequestBody Map<String, Object> orderData) {
        logger.info("更新Banner顺序请求");
        return shopService.updateBannerOrder(orderData);
    }

    // ==================== 公告管理（商家） ====================

    /**
     * 创建店铺公告
     * 路径: POST /shop/{shopId}/announcements
     * 成功码: 5020, 失败码: 5121
     *
     * @param shopId 店铺ID
     * @param announcementData 公告数据（title、content、is_top等）
     * @return 创建后的公告
     */
    @PostMapping("/{shopId}/announcements")
    @RequiresLogin
    public Result<Object> createAnnouncement(@PathVariable String shopId, @RequestBody Map<String, Object> announcementData) {
        logger.info("创建店铺公告请求: {}", shopId);
        return shopService.createAnnouncement(shopId, announcementData);
    }

    /**
     * 更新店铺公告
     * 路径: PUT /shop/announcements/{announcementId}
     * 成功码: 5021, 失败码: 5121
     *
     * @param announcementId 公告ID
     * @param announcementData 公告数据
     * @return 更新后的公告
     */
    @PutMapping("/announcements/{announcementId}")
    @RequiresLogin
    public Result<Object> updateAnnouncement(@PathVariable String announcementId, @RequestBody Map<String, Object> announcementData) {
        logger.info("更新店铺公告请求: {}", announcementId);
        return shopService.updateAnnouncement(announcementId, announcementData);
    }

    /**
     * 删除店铺公告
     * 路径: DELETE /shop/announcements/{announcementId}
     * 成功码: 5022, 失败码: 5122
     *
     * @param announcementId 公告ID
     * @return 删除结果
     */
    @DeleteMapping("/announcements/{announcementId}")
    @RequiresLogin
    public Result<Boolean> deleteAnnouncement(@PathVariable String announcementId) {
        logger.info("删除店铺公告请求: {}", announcementId);
        return shopService.deleteAnnouncement(announcementId);
    }

    // ==================== 店铺关注与评价 ====================

    /**
     * 关注店铺
     * 路径: POST /shop/{shopId}/follow
     * 成功码: 5000, 失败码: 5102
     *
     * @param shopId 店铺ID
     * @return 关注结果
     */
    @PostMapping("/{shopId}/follow")
    @RequiresLogin
    public Result<Boolean> followShop(@PathVariable String shopId) {
        logger.info("关注店铺请求: {}", shopId);
        return shopService.followShop(shopId);
    }

    /**
     * 取消关注店铺
     * 路径: DELETE /shop/{shopId}/follow
     * 成功码: 5001, 失败码: 5102
     *
     * @param shopId 店铺ID
     * @return 取消结果
     */
    @DeleteMapping("/{shopId}/follow")
    @RequiresLogin
    public Result<Boolean> unfollowShop(@PathVariable String shopId) {
        logger.info("取消关注店铺请求: {}", shopId);
        return shopService.unfollowShop(shopId);
    }

    /**
     * 提交店铺评价
     * 路径: POST /shop/{shopId}/reviews
     * 成功码: 5002, 失败码: 5104
     *
     * @param shopId 店铺ID
     * @param reviewData 评价数据 {rating, content, images}
     * @return 提交结果
     */
    @PostMapping("/{shopId}/reviews")
    @RequiresLogin
    public Result<Boolean> submitShopReview(@PathVariable String shopId, @RequestBody Map<String, Object> reviewData) {
        logger.info("提交店铺评价请求: {}", shopId);
        return shopService.submitShopReview(shopId, reviewData);
    }

    /**
     * 获取店铺详情
     * 路径: GET /shop/{id}
     * 成功码: 200, 失败码: 5100, 5105
     * 注意：此路径应放在最后，避免与更具体的路径冲突
     *
     * @param id 店铺ID
     * @return 店铺详情
     */
    @GetMapping("/{id}")
    public Result<Object> getShopDetail(@PathVariable String id) {
        logger.info("获取店铺详情请求: {}", id);
        return shopService.getShopDetail(id);
    }

    // ==================== 公共上传接口（临时放在店铺模块） ====================

    /**
     * 上传文件（公共接口）
     * 路径: POST /upload
     * 成功码: 1001, 失败码: 1101, 1103, 1104
     *
     * @param file 文件
     * @param type 文件类型（avatar, product, shop, common等）
     * @return 上传结果，包含文件URL
     */
    @PostMapping("/upload")
    @RequiresLogin
    public Result<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file,
                                                   @RequestParam(value = "type", defaultValue = "common") String type) {
        logger.info("上传文件请求, type: {}, 文件名: {}", type, file.getOriginalFilename());
        return shopService.uploadFile(file, type);
    }

    /**
     * 上传图片（公共接口）
     * 路径: POST /upload/image
     * 成功码: 1001, 失败码: 1101, 1103, 1104
     *
     * @param file 图片文件
     * @param type 图片类型
     * @return 上传结果，包含文件URL
     */
    @PostMapping("/upload/image")
    @RequiresLogin
    public Result<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file,
                                                    @RequestParam(value = "type", defaultValue = "common") String type) {
        logger.info("上传图片请求, type: {}, 文件名: {}", type, file.getOriginalFilename());
        return shopService.uploadImage(file, type);
    }
}
