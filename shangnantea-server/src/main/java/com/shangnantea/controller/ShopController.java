package com.shangnantea.controller;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.common.api.Result;
import com.shangnantea.model.entity.shop.Shop;
import com.shangnantea.model.entity.shop.ShopCertification;
import com.shangnantea.model.entity.tea.Tea;
import com.shangnantea.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 店铺控制器
 */
@RestController
@RequestMapping("/api/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;
    
    /**
     * 获取店铺详情
     *
     * @param id 店铺ID
     * @return 结果
     */
    @GetMapping("/{id}")
    public Result<Shop> getShopById(@PathVariable String id) {
        Shop result = shopService.getShopById(id);
        return Result.success(result);
    }
    
    /**
     * 获取店铺列表
     *
     * @param page 页码
     * @param size 每页数量
     * @return 结果
     */
    @GetMapping("/list")
    public Result<PageResult<Shop>> listShops(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<Shop> result = shopService.listShops(pageParam);
        return Result.success(result);
    }
    
    /**
     * 获取当前商家的店铺
     *
     * @return 结果
     */
    @GetMapping("/mine")
    public Result<Shop> getMyShop() {
        Shop result = shopService.getMyShop();
        return Result.success(result);
    }

    /**
     * 兼容前端路径：/shop/my
     *
     * @return 店铺信息
     */
    @GetMapping("/my")
    public Result<Shop> getMyShopCompat() {
        Shop result = shopService.getMyShop();
        return Result.success(result);
    }
    
    /**
     * 创建/更新店铺信息(商家)
     *
     * @param shop 店铺信息
     * @return 结果
     */
    @PostMapping
    public Result<Shop> saveShop(@RequestBody Shop shop) {
        Shop result = shopService.saveShop(shop);
        return Result.success(result);
    }
    
    /**
     * 获取店铺茶叶
     *
     * @param shopId 店铺ID
     * @param page 页码
     * @param size 每页数量
     * @return 结果
     */
    @GetMapping("/{shopId}/teas")
    public Result<PageResult<Tea>> listShopTeas(
            @PathVariable String shopId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<Tea> result = shopService.listShopTeas(shopId, pageParam);
        return Result.success(result);
    }
    
    /**
     * 提交商家认证申请
     *
     * @param certification 认证信息
     * @return 结果
     */
    @PostMapping("/certification")
    public Result<ShopCertification> applyCertification(@RequestBody ShopCertification certification) {
        ShopCertification result = shopService.applyCertification(certification);
        return Result.success(result);
    }
    
    /**
     * 获取认证申请状态
     *
     * @return 结果
     */
    @GetMapping("/certification/status")
    public Result<ShopCertification> getCertificationStatus() {
        ShopCertification result = shopService.getCertificationStatus();
        return Result.success(result);
    }
    
    /**
     * 确认认证通知
     *
     * @param certificationId 认证ID
     * @return 结果
     */
    @PostMapping("/certification/confirm/{certificationId}")
    public Result<Boolean> confirmNotification(@PathVariable Integer certificationId) {
        Boolean result = shopService.confirmNotification(certificationId);
        return Result.success(result);
    }
    
    /**
     * 查询认证申请列表(管理员)
     *
     * @param status 状态
     * @param page 页码
     * @param size 每页数量
     * @return 结果
     */
    @GetMapping("/certifications")
    public Result<PageResult<ShopCertification>> listCertifications(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<ShopCertification> result = shopService.listCertifications(status, pageParam);
        return Result.success(result);
    }
    
    /**
     * 处理认证申请(管理员)
     *
     * @param id 认证ID
     * @param status 状态
     * @param rejectReason 拒绝原因
     * @return 结果
     */
    @PutMapping("/certification/{id}")
    public Result<Boolean> processCertification(
            @PathVariable Integer id,
            @RequestParam Integer status,
            @RequestParam(required = false) String rejectReason) {
        Boolean result = shopService.processCertification(id, status, rejectReason);
        return Result.success(result);
    }
    
    /**
     * 获取店铺评价列表
     *
     * @param shopId 店铺ID
     * @param page 页码
     * @param size 每页数量
     * @return 评价列表
     */
    @GetMapping("/{shopId}/reviews")
    public Result<PageResult<Map<String, Object>>> getShopReviews(
            @PathVariable String shopId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<Map<String, Object>> result = shopService.getShopReviews(shopId, pageParam);
        return Result.success(result);
    }
    
    /**
     * 提交店铺评价
     *
     * @param shopId 店铺ID
     * @param body 评价信息
     * @return 提交结果
     */
    @PostMapping("/{shopId}/reviews")
    public Result<Map<String, Object>> submitShopReview(
            @PathVariable String shopId,
            @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> result = shopService.submitShopReview(shopId, body);
        return Result.success(result);
    }
    
    /**
     * 搜索店铺
     *
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页数量
     * @return 结果
     */
    @GetMapping("/search")
    public Result<PageResult<Shop>> searchShops(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<Shop> result = shopService.searchShops(keyword, pageParam);
        return Result.success(result);
    }
    
    /**
     * 更新店铺信息
     *
     * @param id 店铺ID
     * @param shop 店铺信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateShop(@PathVariable String id, @RequestBody Shop shop) {
        shop.setId(id);
        Boolean result = shopService.updateShop(shop);
        return Result.success(result);
    }
    
    /**
     * 删除店铺（管理员）
     *
     * @param id 店铺ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteShop(@PathVariable String id) {
        Boolean result = shopService.deleteShop(id);
        return Result.success(result);
    }
    
    /**
     * 获取店铺列表（管理员）
     *
     * @param keyword 关键词
     * @param status 状态
     * @param page 页码
     * @param size 每页数量
     * @return 店铺列表
     */
    @GetMapping("/admin/list")
    public Result<PageResult<Shop>> listAdminShops(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<Shop> result = shopService.listAdminShops(keyword, status, pageParam);
        return Result.success(result);
    }
    
    /**
     * 审核店铺（管理员）
     *
     * @param id 店铺ID
     * @param body 审核信息
     * @return 审核结果
     */
    @PutMapping("/admin/{id}/audit")
    public Result<Boolean> auditShop(@PathVariable String id, @RequestBody(required = false) Map<String, Object> body) {
        Boolean result = shopService.auditShop(id, body);
        return Result.success(result);
    }
}
