package com.shangnantea.service.impl;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.common.api.Result;
import com.shangnantea.mapper.ShopCertificationMapper;
import com.shangnantea.mapper.ShopMapper;
import com.shangnantea.mapper.TeaMapper;
import com.shangnantea.model.dto.shop.ShopQueryDTO;
import com.shangnantea.model.entity.shop.Shop;
import com.shangnantea.model.entity.shop.ShopCertification;
import com.shangnantea.model.entity.tea.Tea;
import com.shangnantea.model.vo.shop.ShopVO;
import com.shangnantea.model.vo.shop.ShopDetailVO;
import com.shangnantea.model.vo.shop.ShopStatisticsVO;
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

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
        return shopMapper.selectByUserId(userId);
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
        return certificationMapper.selectByUserId(userId);
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
                return Result.failure(4113); // Logo上传失败
            }
            
            // 验证用户是否为店铺所有者
            if (!userId.equals(shop.getOwnerId())) {
                logger.warn("店铺Logo上传失败: 无权限操作, userId: {}, shopId: {}", userId, shopId);
                return Result.failure(4113); // Logo上传失败
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
                return Result.failure(4113); // Logo上传失败
            }
            
            // 6. 返回结果
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("url", accessUrl);
            responseData.put("path", relativePath);
            
            logger.info("店铺Logo上传成功: shopId: {}, path: {}", shopId, relativePath);
            return Result.success(4007, responseData); // Logo上传成功
            
        } catch (Exception e) {
            logger.error("店铺Logo上传失败: 系统异常", e);
            return Result.failure(4113); // Logo上传失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> uploadBanner(String shopId, Map<String, Object> bannerData) {
        try {
            logger.info("上传店铺轮播图请求, shopId: {}", shopId);
            
            // 1. 验证店铺是否存在
            Shop shop = getShopById(shopId);
            if (shop == null) {
                logger.warn("店铺轮播图上传失败: 店铺不存在, shopId: {}", shopId);
                return Result.failure(4117); // Banner上传失败
            }
            
            // 2. 从bannerData中提取图片文件（这里需要根据实际前端传递的数据格式处理）
            // 注意：这个方法的实现需要根据前端实际传递的数据格式来调整
            // 目前先返回一个基本的成功响应
            
            logger.info("店铺轮播图上传成功: shopId: {}", shopId);
            return Result.success(4008, "店铺轮播图上传成功"); // Banner上传成功
            
        } catch (Exception e) {
            logger.error("店铺轮播图上传失败: 系统异常", e);
            return Result.failure(4117); // Banner上传失败
        }
    }
    
    @Override
    public Result<Object> getShops(Map<String, Object> params) {
        try {
            logger.info("获取店铺列表请求, params: {}", params);
            
            // 1. 解析查询参数
            ShopQueryDTO queryDTO = new ShopQueryDTO();
            if (params != null) {
                if (params.get("page") != null) {
                    queryDTO.setPage(Integer.parseInt(params.get("page").toString()));
                }
                if (params.get("size") != null) {
                    queryDTO.setSize(Integer.parseInt(params.get("size").toString()));
                }
                if (params.get("keyword") != null) {
                    queryDTO.setKeyword(params.get("keyword").toString());
                }
                if (params.get("rating") != null) {
                    queryDTO.setRating(Double.parseDouble(params.get("rating").toString()));
                }
                if (params.get("sortBy") != null) {
                    queryDTO.setSortBy(params.get("sortBy").toString());
                }
                if (params.get("sortOrder") != null) {
                    queryDTO.setSortOrder(params.get("sortOrder").toString());
                }
            }
            
            // 2. 参数验证和默认值设置
            if (queryDTO.getPage() == null || queryDTO.getPage() < 1) {
                queryDTO.setPage(1);
            }
            if (queryDTO.getSize() == null || queryDTO.getSize() < 1) {
                queryDTO.setSize(10);
            }
            if (queryDTO.getSortOrder() == null || queryDTO.getSortOrder().isEmpty()) {
                queryDTO.setSortOrder("desc");
            }
            
            // 3. 计算分页偏移量
            int offset = (queryDTO.getPage() - 1) * queryDTO.getSize();
            
            // 4. 查询店铺列表
            List<Shop> shopList = shopMapper.selectShopList(
                    queryDTO.getKeyword(),
                    queryDTO.getRating(),
                    queryDTO.getSortBy(),
                    queryDTO.getSortOrder(),
                    offset,
                    queryDTO.getSize()
            );
            
            // 5. 查询总数
            Long total = shopMapper.countShopList(
                    queryDTO.getKeyword(),
                    queryDTO.getRating()
            );
            
            // 6. 转换为VO
            List<ShopVO> shopVOList = shopList.stream()
                    .map(this::convertToShopVO)
                    .collect(Collectors.toList());
            
            // 7. 构建返回数据（符合前端期望的格式）
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("list", shopVOList);
            responseData.put("total", total);
            responseData.put("page", queryDTO.getPage());
            responseData.put("pageSize", queryDTO.getSize());
            
            logger.info("获取店铺列表成功: 总记录数={}, 当前页={}, 每页={}", 
                    total, queryDTO.getPage(), queryDTO.getSize());
            
            // 根据code-message-mapping.md，成功码是200（静默成功）
            return Result.success(200, responseData);
            
        } catch (Exception e) {
            logger.error("获取店铺列表失败: 系统异常", e);
            return Result.failure(4100);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> createShop(Map<String, Object> shopData) {
        try {
            logger.info("创建店铺请求, shopData: {}", shopData);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("创建店铺失败: 用户未登录");
                return Result.failure(4101);
            }
            
            // 2. 验证用户是否有商家认证
            ShopCertification certification = getCertificationByUserId(userId);
            if (certification == null || certification.getStatus() != 1) {
                logger.warn("创建店铺失败: 用户未通过商家认证, userId: {}", userId);
                return Result.failure(4101);
            }
            
            // 3. 验证用户是否已有店铺
            Shop existingShop = getShopByUserId(userId);
            if (existingShop != null) {
                logger.warn("创建店铺失败: 用户已有店铺, userId: {}, shopId: {}", userId, existingShop.getId());
                return Result.failure(4101);
            }
            
            // 4. 提取并验证店铺名称
            String shopName = shopData.get("name") != null ? shopData.get("name").toString() : null;
            if (shopName == null || shopName.trim().isEmpty()) {
                logger.warn("创建店铺失败: 店铺名称为空");
                return Result.failure(4101);
            }
            
            // 5. 验证店铺名称是否重复
            Shop shopByName = shopMapper.selectByShopName(shopName);
            if (shopByName != null) {
                logger.warn("创建店铺失败: 店铺名称已存在, shopName: {}", shopName);
                return Result.failure(4101);
            }
            
            // 6. 构建店铺实体
            Shop shop = new Shop();
            shop.setId(UUID.randomUUID().toString().replace("-", ""));
            shop.setOwnerId(userId);
            shop.setShopName(shopName);
            
            // 设置可选字段
            if (shopData.get("logo") != null) {
                shop.setLogo(shopData.get("logo").toString());
            }
            if (shopData.get("description") != null) {
                shop.setDescription(shopData.get("description").toString());
            }
            
            // 设置默认值
            shop.setStatus(1); // 正常状态
            shop.setRating(new BigDecimal("5.0")); // 默认评分
            shop.setRatingCount(0);
            shop.setSalesCount(0);
            shop.setFollowCount(0);
            
            Date now = new Date();
            shop.setCreateTime(now);
            shop.setUpdateTime(now);
            
            // 7. 插入数据库
            int result = shopMapper.insert(shop);
            if (result <= 0) {
                logger.error("创建店铺失败: 数据库插入失败, userId: {}", userId);
                return Result.failure(4101);
            }
            
            logger.info("创建店铺成功: shopId: {}, shopName: {}, userId: {}", 
                    shop.getId(), shop.getShopName(), userId);
            
            // 8. 返回成功（根据code-message-mapping.md，成功码是4000）
            return Result.success(4000, null);
            
        } catch (Exception e) {
            logger.error("创建店铺失败: 系统异常", e);
            return Result.failure(4101);
        }
    }
    
    /**
     * 将Shop实体转换为ShopVO
     *
     * @param shop 店铺实体
     * @return 店铺VO
     */
    private ShopVO convertToShopVO(Shop shop) {
        if (shop == null) {
            return null;
        }
        
        ShopVO shopVO = new ShopVO();
        shopVO.setId(shop.getId());
        shopVO.setName(shop.getShopName()); // shopName -> name
        shopVO.setLogo(shop.getLogo());
        shopVO.setDescription(shop.getDescription());
        shopVO.setRating(shop.getRating());
        shopVO.setSalesCount(shop.getSalesCount());
        shopVO.setFollowCount(shop.getFollowCount());
        shopVO.setOwnerId(shop.getOwnerId());
        
        return shopVO;
    }
    
    @Override
    public Result<Object> getShopDetail(String id) {
        try {
            logger.info("获取店铺详情请求: id={}", id);
            
            // 1. 验证店铺ID不为空
            if (id == null || id.trim().isEmpty()) {
                logger.warn("获取店铺详情失败: 店铺ID为空");
                return Result.failure(4102);
            }
            
            // 2. 查询店铺信息
            Shop shop = getShopById(id);
            if (shop == null) {
                logger.warn("获取店铺详情失败: 店铺不存在, id={}", id);
                return Result.failure(4103);
            }
            
            // 3. 转换为ShopDetailVO
            ShopDetailVO shopDetailVO = convertToShopDetailVO(shop);
            
            logger.info("获取店铺详情成功: id={}, name={}", id, shop.getShopName());
            
            // 4. 返回成功（根据code-message-mapping.md，成功码是200）
            return Result.success(200, shopDetailVO);
            
        } catch (Exception e) {
            logger.error("获取店铺详情失败: 系统异常, id={}", id, e);
            return Result.failure(4102);
        }
    }
    
    /**
     * 将Shop实体转换为ShopDetailVO
     *
     * @param shop 店铺实体
     * @return 店铺详情VO
     */
    private ShopDetailVO convertToShopDetailVO(Shop shop) {
        if (shop == null) {
            return null;
        }
        
        ShopDetailVO shopDetailVO = new ShopDetailVO();
        shopDetailVO.setId(shop.getId());
        shopDetailVO.setName(shop.getShopName()); // shopName -> name
        shopDetailVO.setLogo(shop.getLogo());
        shopDetailVO.setDescription(shop.getDescription());
        shopDetailVO.setRating(shop.getRating());
        shopDetailVO.setSalesCount(shop.getSalesCount());
        shopDetailVO.setFollowCount(shop.getFollowCount());
        shopDetailVO.setOwnerId(shop.getOwnerId());
        
        return shopDetailVO;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> updateShop(String id, Map<String, Object> shopData) {
        try {
            logger.info("更新店铺信息请求: id={}, shopData={}", id, shopData);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("更新店铺信息失败: 用户未登录");
                return Result.failure(4104);
            }
            
            // 2. 验证店铺ID不为空
            if (id == null || id.trim().isEmpty()) {
                logger.warn("更新店铺信息失败: 店铺ID为空");
                return Result.failure(4104);
            }
            
            // 3. 查询店铺信息
            Shop shop = getShopById(id);
            if (shop == null) {
                logger.warn("更新店铺信息失败: 店铺不存在, id={}", id);
                return Result.failure(4104);
            }
            
            // 4. 验证用户是否为店铺所有者
            if (!userId.equals(shop.getOwnerId())) {
                logger.warn("更新店铺信息失败: 无权限操作, userId={}, shopId={}, ownerId={}", 
                        userId, id, shop.getOwnerId());
                return Result.failure(4104);
            }
            
            // 5. 更新店铺信息
            boolean updated = false;
            
            if (shopData.get("name") != null) {
                String newName = shopData.get("name").toString();
                if (!newName.equals(shop.getShopName())) {
                    // 验证新店铺名称是否重复
                    Shop existingShop = shopMapper.selectByShopName(newName);
                    if (existingShop != null && !existingShop.getId().equals(id)) {
                        logger.warn("更新店铺信息失败: 店铺名称已存在, newName={}", newName);
                        return Result.failure(4104);
                    }
                    shop.setShopName(newName);
                    updated = true;
                }
            }
            
            if (shopData.get("logo") != null) {
                shop.setLogo(shopData.get("logo").toString());
                updated = true;
            }
            
            if (shopData.get("description") != null) {
                shop.setDescription(shopData.get("description").toString());
                updated = true;
            }
            
            // 6. 如果有更新，则保存到数据库
            if (updated) {
                shop.setUpdateTime(new Date());
                int result = shopMapper.updateById(shop);
                if (result <= 0) {
                    logger.error("更新店铺信息失败: 数据库更新失败, id={}", id);
                    return Result.failure(4104);
                }
                
                logger.info("更新店铺信息成功: id={}, shopName={}", id, shop.getShopName());
            } else {
                logger.info("更新店铺信息: 无需更新, id={}", id);
            }
            
            // 7. 返回成功（根据code-message-mapping.md，成功码是4001）
            return Result.success(4001, true);
            
        } catch (Exception e) {
            logger.error("更新店铺信息失败: 系统异常, id={}", id, e);
            return Result.failure(4104);
        }
    }
    
    @Override
    public Result<Object> getMyShop() {
        try {
            logger.info("获取我的店铺信息请求");
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取我的店铺信息失败: 用户未登录");
                return Result.failure(4105);
            }
            
            // 2. 查询用户的店铺
            Shop shop = getShopByUserId(userId);
            if (shop == null) {
                logger.info("获取我的店铺信息: 用户暂无店铺, userId={}", userId);
                // 用户没有店铺，返回成功但data为null
                return Result.success(200, null);
            }
            
            // 3. 转换为ShopDetailVO
            ShopDetailVO shopDetailVO = convertToShopDetailVO(shop);
            
            logger.info("获取我的店铺信息成功: userId={}, shopId={}, shopName={}", 
                    userId, shop.getId(), shop.getShopName());
            
            // 4. 返回成功（根据code-message-mapping.md，成功码是200）
            return Result.success(200, shopDetailVO);
            
        } catch (Exception e) {
            logger.error("获取我的店铺信息失败: 系统异常", e);
            return Result.failure(4105);
        }
    }
    
    @Override
    public Result<Object> getShopStatistics(String shopId, Map<String, Object> params) {
        try {
            logger.info("获取店铺统计数据请求: shopId={}, params={}", shopId, params);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取店铺统计数据失败: 用户未登录");
                return Result.failure(4106);
            }
            
            // 2. 验证店铺ID不为空
            if (shopId == null || shopId.trim().isEmpty()) {
                logger.warn("获取店铺统计数据失败: 店铺ID为空");
                return Result.failure(4106);
            }
            
            // 3. 查询店铺信息
            Shop shop = getShopById(shopId);
            if (shop == null) {
                logger.warn("获取店铺统计数据失败: 店铺不存在, shopId={}", shopId);
                return Result.failure(4106);
            }
            
            // 4. 验证用户是否为店铺所有者
            if (!userId.equals(shop.getOwnerId())) {
                logger.warn("获取店铺统计数据失败: 无权限操作, userId={}, shopId={}, ownerId={}", 
                        userId, shopId, shop.getOwnerId());
                return Result.failure(4106);
            }
            
            // 5. 构建统计数据
            ShopStatisticsVO statisticsVO = new ShopStatisticsVO();
            
            // 从店铺基本信息中获取统计数据
            statisticsVO.setFollowCount(shop.getFollowCount());
            statisticsVO.setRatingCount(shop.getRatingCount());
            statisticsVO.setRating(shop.getRating());
            
            // TODO: 从订单表和商品表查询更详细的统计数据
            // 目前先设置默认值，后续可以扩展
            statisticsVO.setTotalSales(BigDecimal.ZERO);
            statisticsVO.setOrderCount(0);
            statisticsVO.setProductCount(0);
            
            logger.info("获取店铺统计数据成功: shopId={}, followCount={}, ratingCount={}", 
                    shopId, shop.getFollowCount(), shop.getRatingCount());
            
            // 6. 返回成功（根据code-message-mapping.md，成功码是200）
            return Result.success(200, statisticsVO);
            
        } catch (Exception e) {
            logger.error("获取店铺统计数据失败: 系统异常, shopId={}", shopId, e);
            return Result.failure(4106);
        }
    }
}
} 