package com.shangnantea.service.impl;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.common.api.Result;
import com.shangnantea.mapper.ShopCertificationMapper;
import com.shangnantea.mapper.ShopMapper;
import com.shangnantea.mapper.TeaMapper;
import com.shangnantea.model.entity.shop.Shop;
import com.shangnantea.model.entity.shop.ShopCertification;
import com.shangnantea.model.entity.tea.Tea;
import com.shangnantea.security.context.UserContext;
import com.shangnantea.service.ShopService;
import com.shangnantea.utils.FileUploadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 店铺服务实现类
 */
@Service
public class ShopServiceImpl implements ShopService {

    private static final Logger logger = LoggerFactory.getLogger(ShopServiceImpl.class);

    @Autowired
    private ShopMapper shopMapper;
    
    @Autowired
    private ShopCertificationMapper certificationMapper;
    
    @Autowired
    private TeaMapper teaMapper;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Override
    public Shop getShopById(String id) {
        // TODO: 实现获取店铺详情的逻辑
        return shopMapper.selectById(id);
    }
    
    @Override
    public PageResult<Shop> listShops(PageParam pageParam) {
        // TODO: 实现分页查询店铺的逻辑
        return new PageResult<>();
    }
    
    @Override
    public Shop getShopByUserId(String userId) {
        // TODO: 实现获取商家店铺的逻辑
        return null; // 待实现
    }
    
    @Override
    @Transactional
    public Shop createShop(Shop shop) {
        // TODO: 实现创建店铺的逻辑
        Date now = new Date();
        shop.setId(UUID.randomUUID().toString().replace("-", ""));
        shop.setCreateTime(now);
        shop.setUpdateTime(now);
        shopMapper.insert(shop);
        return shop;
    }
    
    @Override
    public boolean updateShop(Shop shop) {
        // TODO: 实现更新店铺信息的逻辑
        shop.setUpdateTime(new Date());
        return shopMapper.updateById(shop) > 0;
    }
    
    @Override
    public PageResult<Tea> listShopTeas(String shopId, PageParam pageParam) {
        // TODO: 实现获取店铺茶叶的逻辑
        return new PageResult<>();
    }
    
    @Override
    @Transactional
    public ShopCertification createCertification(ShopCertification certification) {
        // TODO: 实现创建商家认证申请的逻辑
        Date now = new Date();
        certification.setCreateTime(now);
        certification.setUpdateTime(now);
        certification.setStatus(0); // 待审核
        certificationMapper.insert(certification);
        return certification;
    }
    
    @Override
    public ShopCertification getCertificationById(Integer id) {
        // TODO: 实现获取认证申请的逻辑
        return certificationMapper.selectById(id);
    }
    
    @Override
    public ShopCertification getCertificationByUserId(String userId) {
        // TODO: 实现获取用户认证申请的逻辑
        return null; // 待实现
    }
    
    @Override
    public PageResult<ShopCertification> listCertifications(Integer status, PageParam pageParam) {
        // TODO: 实现查询认证申请列表的逻辑
        return new PageResult<>();
    }
    
    @Override
    @Transactional
    public boolean processCertification(Integer id, Integer status, String adminId, String rejectReason) {
        // TODO: 实现处理认证申请的逻辑
        ShopCertification certification = certificationMapper.selectById(id);
        if (certification == null) {
            return false;
        }
        
        certification.setStatus(status);
        certification.setAdminId(adminId);
        certification.setRejectReason(rejectReason);
        certification.setUpdateTime(new Date());
        
        return certificationMapper.updateById(certification) > 0;
    }
    
    @Override
    @Transactional
    public boolean confirmNotification(Integer certificationId) {
        // TODO: 实现确认通知的逻辑
        ShopCertification certification = certificationMapper.selectById(certificationId);
        if (certification == null || certification.getStatus() != 1) {
            return false;
        }
        
        certification.setNotificationConfirmed(1);
        certification.setUpdateTime(new Date());
        
        return certificationMapper.updateById(certification) > 0;
    }
    
    @Override
    public Result<Map<String, Object>> uploadCertificationImage(MultipartFile image) {
        try {
            logger.info("上传商家认证图片请求, 文件名: {}", image.getOriginalFilename());
            
            // 1. 调用工具类上传（硬编码type为"certifications"）
            String relativePath = FileUploadUtils.uploadImage(image, "certifications");
            
            // 2. 生成访问URL
            String accessUrl = FileUploadUtils.generateAccessUrl(relativePath, baseUrl);
            
            // 3. 直接返回，不存数据库（场景2：先返回URL，稍后存储）
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("url", accessUrl);
            responseData.put("path", relativePath);
            
            logger.info("商家认证图片上传成功: path: {}", relativePath);
            return Result.success(2024, responseData); // 认证图片上传成功
            
        } catch (Exception e) {
            logger.error("商家认证图片上传失败: 系统异常", e);
            return Result.failure(2146); // 认证图片上传失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> uploadShopLogo(String shopId, MultipartFile image) {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("店铺Logo上传失败: 用户未登录");
                return Result.failure(4113); // Logo上传失败
            }
            
            // 2. 获取店铺信息并验证权限
            Shop shop = getShopById(shopId);
            if (shop == null) {
                logger.warn("店铺Logo上传失败: 店铺不存在, shopId: {}", shopId);
                return Result.failure(4114); // Logo上传失败
            }
            
            // 验证用户是否为店铺所有者
            if (!userId.equals(shop.getUserId())) {
                logger.warn("店铺Logo上传失败: 无权限操作, userId: {}, shopId: {}", userId, shopId);
                return Result.failure(4114); // Logo上传失败
            }
            
            logger.info("上传店铺Logo请求, 文件名: {}, shopId: {}", image.getOriginalFilename(), shopId);
            
            // 3. 调用工具类上传（硬编码type为"logos"）
            String relativePath = FileUploadUtils.uploadImage(image, "logos");
            
            // 4. 生成访问URL
            String accessUrl = FileUploadUtils.generateAccessUrl(relativePath, baseUrl);
            
            // 5. 更新店铺Logo字段
            shop.setLogo(relativePath);
            shop.setUpdateTime(new Date());
            int result = shopMapper.updateById(shop);
            if (result <= 0) {
                logger.error("店铺Logo上传失败: 数据库更新失败, shopId: {}", shopId);
                return Result.failure(4115); // Logo上传失败
            }
            
            // 6. 返回结果
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("url", accessUrl);
            responseData.put("path", relativePath);
            
            logger.info("店铺Logo上传成功: shopId: {}, path: {}", shopId, relativePath);
            return Result.success(4007, responseData); // Logo上传成功
            
        } catch (Exception e) {
            logger.error("店铺Logo上传失败: 系统异常", e);
            return Result.failure(4115); // Logo上传失败
        }
    }
    
    @Override
    public Result<Map<String, Object>> uploadShopBanner(MultipartFile image) {
        try {
            logger.info("上传店铺轮播图请求, 文件名: {}", image.getOriginalFilename());
            
            // 1. 调用工具类上传（硬编码type为"shop-banners"）
            String relativePath = FileUploadUtils.uploadImage(image, "shop-banners");
            
            // 2. 生成访问URL
            String accessUrl = FileUploadUtils.generateAccessUrl(relativePath, baseUrl);
            
            // 3. 直接返回，不存数据库（场景2：先返回URL，稍后存储）
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("url", accessUrl);
            responseData.put("path", relativePath);
            
            logger.info("店铺轮播图上传成功: path: {}", relativePath);
            return Result.success(4008, responseData); // Banner上传成功
            
        } catch (Exception e) {
            logger.error("店铺轮播图上传失败: 系统异常", e);
            return Result.failure(4117); // Banner上传失败
        }
    }
} 