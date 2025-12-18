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
        Shop shop = shopService.getShopById(id);
        return Result.success(shop);
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
        PageResult<Shop> pageResult = shopService.listShops(pageParam);
        return Result.success(pageResult);
    }
    
    /**
     * 获取当前商家的店铺
     *
     * @return 结果
     */
    @GetMapping("/mine")
    public Result<Shop> getMyShop() {
        // TODO: 获取当前登录用户ID
        String userId = "currentUserId";
        Shop shop = shopService.getShopByUserId(userId);
        return Result.success(shop);
    }

    /**
     * 兼容前端路径：/shop/my
     *
     * @return 店铺信息
     */
    @GetMapping("/my")
    public Result<Shop> getMyShopCompat() {
        return getMyShop();
    }
    
    /**
     * 创建/更新店铺信息(商家)
     *
     * @param shop 店铺信息
     * @return 结果
     */
    @PostMapping
    public Result<Shop> saveShop(@RequestBody Shop shop) {
        // TODO: 获取当前登录用户ID
        String userId = "currentUserId";
        shop.setOwnerId(userId);
        
        Shop existingShop = shopService.getShopByUserId(userId);
        if (existingShop != null) {
            shop.setId(existingShop.getId());
            shopService.updateShop(shop);
            return Result.success(shop);
        } else {
            Shop createdShop = shopService.createShop(shop);
            return Result.success(createdShop);
        }
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
        PageResult<Tea> pageResult = shopService.listShopTeas(shopId, pageParam);
        return Result.success(pageResult);
    }
    
    /**
     * 提交商家认证申请
     *
     * @param certification 认证信息
     * @return 结果
     */
    @PostMapping("/certification")
    public Result<ShopCertification> applyCertification(@RequestBody ShopCertification certification) {
        // TODO: 获取当前登录用户ID
        String userId = "currentUserId";
        certification.setUserId(userId);
        
        ShopCertification result = shopService.createCertification(certification);
        return Result.success(result);
    }
    
    /**
     * 获取认证申请状态
     *
     * @return 结果
     */
    @GetMapping("/certification/status")
    public Result<ShopCertification> getCertificationStatus() {
        // TODO: 获取当前登录用户ID
        String userId = "currentUserId";
        ShopCertification certification = shopService.getCertificationByUserId(userId);
        return Result.success(certification);
    }
    
    /**
     * 确认认证通知
     *
     * @param certificationId 认证ID
     * @return 结果
     */
    @PostMapping("/certification/confirm/{certificationId}")
    public Result<Boolean> confirmNotification(@PathVariable Integer certificationId) {
        boolean success = shopService.confirmNotification(certificationId);
        return Result.success(success);
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
        PageResult<ShopCertification> pageResult = shopService.listCertifications(status, pageParam);
        return Result.success(pageResult);
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
        // TODO: 获取当前管理员ID
        String adminId = "currentAdminId";
        boolean success = shopService.processCertification(id, status, adminId, rejectReason);
        return Result.success(success);
    }
} 