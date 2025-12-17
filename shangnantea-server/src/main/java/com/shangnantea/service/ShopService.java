package com.shangnantea.service;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.model.entity.shop.Shop;
import com.shangnantea.model.entity.shop.ShopCertification;
import com.shangnantea.model.entity.tea.Tea;

import java.util.List;

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
} 