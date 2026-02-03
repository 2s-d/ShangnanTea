package com.shangnantea.service.impl;

import com.shangnantea.common.api.Result;
import com.shangnantea.exception.BusinessException;
import com.shangnantea.mapper.OrderMapper;
import com.shangnantea.mapper.ShopAnnouncementMapper;
import com.shangnantea.mapper.ShopBannerMapper;
import com.shangnantea.mapper.ShopCertificationMapper;
import com.shangnantea.mapper.ShopMapper;
import com.shangnantea.mapper.TeaMapper;
import com.shangnantea.mapper.UserFollowMapper;
import com.shangnantea.model.dto.shop.ShopQueryDTO;
import com.shangnantea.model.entity.order.Order;
import com.shangnantea.model.entity.shop.Shop;
import com.shangnantea.model.entity.shop.ShopAnnouncement;
import com.shangnantea.model.entity.shop.ShopBanner;
import com.shangnantea.model.entity.shop.ShopCertification;
import com.shangnantea.model.entity.tea.Tea;
import com.shangnantea.model.entity.user.UserFollow;
import com.shangnantea.model.vo.TeaVO;
import com.shangnantea.model.vo.shop.AnnouncementVO;
import com.shangnantea.model.vo.shop.BannerVO;
import com.shangnantea.model.vo.shop.ShopDetailVO;
import com.shangnantea.model.vo.shop.ShopStatisticsVO;
import com.shangnantea.model.vo.shop.ShopVO;
import com.shangnantea.security.context.UserContext;
import com.shangnantea.service.ShopService;
import com.shangnantea.utils.FileUploadUtils;
import com.shangnantea.utils.StatisticsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 店铺服务实现类
 * 实现26个店铺模块接口
 */
@Service
public class ShopServiceImpl implements ShopService {

    private static final Logger logger = LoggerFactory.getLogger(ShopServiceImpl.class);

    @Autowired
    private ShopMapper shopMapper;
    
    @Autowired
    private ShopCertificationMapper certificationMapper;
    
    @Autowired
    private ShopBannerMapper shopBannerMapper;
    
    @Autowired
    private ShopAnnouncementMapper shopAnnouncementMapper;
    
    @Autowired
    private UserFollowMapper userFollowMapper;
    
    @Autowired
    private TeaMapper teaMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private StatisticsUtils statisticsUtils;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    // ==================== 内部辅助方法 ====================
    
    /**
     * 根据ID获取店铺（内部辅助方法）
     */
    private Shop getShopById(String id) {
        return shopMapper.selectById(id);
    }
    
    /**
     * 根据用户ID获取店铺（内部辅助方法）
     */
    private Shop getShopByUserId(String userId) {
        return shopMapper.selectByUserId(userId);
    }
    
    /**
     * 根据用户ID获取认证信息（内部辅助方法）
     */
    private ShopCertification getCertificationByUserId(String userId) {
        return certificationMapper.selectByUserId(userId);
    }
    
    // ==================== 接口1-7：基础店铺管理 ====================
    
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
    public Result<Object> uploadBanner(String shopId, MultipartFile file, String title, String linkUrl) {
        try {
            logger.info("上传店铺Banner请求, shopId: {}, title: {}, 文件名: {}", shopId, title, file.getOriginalFilename());
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("Banner上传失败: 用户未登录");
                return Result.failure(4117);
            }
            
            // 2. 验证店铺是否存在
            Shop shop = getShopById(shopId);
            if (shop == null) {
                logger.warn("Banner上传失败: 店铺不存在, shopId: {}", shopId);
                return Result.failure(4117);
            }
            
            // 3. 验证用户是否为店铺所有者
            if (!userId.equals(shop.getOwnerId())) {
                logger.warn("Banner上传失败: 无权限操作, userId={}, shopId={}, ownerId={}", 
                        userId, shopId, shop.getOwnerId());
                return Result.failure(4117);
            }
            
            // 4. 调用工具类上传图片（硬编码type为"shop-banners"）
            String relativePath = FileUploadUtils.uploadImage(file, "shop-banners");
            
            // 5. 生成访问URL
            String accessUrl = FileUploadUtils.generateAccessUrl(relativePath, baseUrl);
            
            // 6. 创建Banner记录
            ShopBanner banner = new ShopBanner();
            banner.setShopId(shopId);
            banner.setImageUrl(relativePath);
            banner.setTitle(title);
            banner.setLinkUrl(linkUrl);
            banner.setSortOrder(0); // 默认排序值
            banner.setStatus(1); // 默认显示
            banner.setCreateTime(new Date());
            banner.setUpdateTime(new Date());
            
            // 7. 保存到数据库
            int result = shopBannerMapper.insert(banner);
            if (result <= 0) {
                logger.error("Banner上传失败: 数据库插入失败, shopId: {}", shopId);
                return Result.failure(4117);
            }
            
            // 8. 构造返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("id", banner.getId());
            responseData.put("url", accessUrl);
            responseData.put("path", relativePath);
            
            logger.info("Banner上传成功: shopId: {}, bannerId: {}, path: {}", shopId, banner.getId(), relativePath);
            
            // 9. 返回成功（根据code-message-mapping.md，成功码是4008）
            return Result.success(4008, responseData);
            
        } catch (BusinessException e) {
            logger.error("Banner上传失败: 业务异常", e);
            // 根据异常消息判断返回不同的错误码
            if (e.getMessage() != null && e.getMessage().contains("文件类型")) {
                return Result.failure(4118); // 文件类型错误
            } else if (e.getMessage() != null && e.getMessage().contains("文件大小")) {
                return Result.failure(4119); // 文件过大
            }
            return Result.failure(4117); // 通用失败
        } catch (Exception e) {
            logger.error("Banner上传失败: 系统异常", e);
            return Result.failure(4117);
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
            // followCount已从数据库删除，使用动态计算
            
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
        // 使用动态计算获取关注数
        shopVO.setFollowCount(statisticsUtils.getFollowCount("shop", shop.getId()));
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
            
            // 4. 查询当前用户是否已关注该店铺
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId != null) {
                try {
                    UserFollow follow = userFollowMapper.selectByUserIdAndFollowId(currentUserId, id, "shop");
                    shopDetailVO.setIsFollowed(follow != null);
                } catch (Exception e) {
                    logger.warn("查询店铺关注状态失败, shopId: {}, userId: {}, 默认设置为未关注", id, currentUserId, e);
                    shopDetailVO.setIsFollowed(false);
                }
            } else {
                // 未登录用户默认未关注
                shopDetailVO.setIsFollowed(false);
            }
            
            logger.info("获取店铺详情成功: id={}, name={}", id, shop.getShopName());
            
            // 5. 返回成功（根据code-message-mapping.md，成功码是200）
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
        // 使用动态计算获取关注数
        shopDetailVO.setFollowCount(statisticsUtils.getFollowCount("shop", shop.getId()));
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
            // 使用动态计算获取关注数
            statisticsVO.setFollowCount(statisticsUtils.getFollowCount("shop", shopId));
            statisticsVO.setRatingCount(shop.getRatingCount());
            statisticsVO.setRating(shop.getRating());
            
            // 从订单表查询销售统计数据（只统计已完成的订单）
            List<Order> completedOrders = orderMapper.selectByShopIdAndStatus(shopId, 3); // 状态3=已完成
            BigDecimal totalSales = BigDecimal.ZERO;
            int orderCount = 0;
            if (completedOrders != null && !completedOrders.isEmpty()) {
                orderCount = completedOrders.size();
                for (Order order : completedOrders) {
                    if (order.getTotalAmount() != null) {
                        totalSales = totalSales.add(order.getTotalAmount());
                    }
                }
            }
            statisticsVO.setTotalSales(totalSales);
            statisticsVO.setOrderCount(orderCount);
            
            // 从茶叶表查询商品数量
            Long productCount = teaMapper.countByShopId(shopId);
            statisticsVO.setProductCount(productCount != null ? productCount.intValue() : 0);
            
            logger.info("获取店铺统计数据成功: shopId={}, followCount={}, ratingCount={}", 
                    shopId, statisticsUtils.getFollowCount("shop", shopId), shop.getRatingCount());
            
            // 6. 返回成功（根据code-message-mapping.md，成功码是200）
            return Result.success(200, statisticsVO);
            
        } catch (Exception e) {
            logger.error("获取店铺统计数据失败: 系统异常, shopId={}", shopId, e);
            return Result.failure(4106);
        }
    }
    
    @Override
    public Result<Object> getShopTeas(String shopId, Map<String, Object> params) {
        try {
            logger.info("获取店铺茶叶列表请求: shopId={}, params={}", shopId, params);
            
            // 1. 验证店铺ID不为空
            if (shopId == null || shopId.trim().isEmpty()) {
                logger.warn("获取店铺茶叶列表失败: 店铺ID为空");
                return Result.failure(4107);
            }
            
            // 2. 验证店铺是否存在
            Shop shop = getShopById(shopId);
            if (shop == null) {
                logger.warn("获取店铺茶叶列表失败: 店铺不存在, shopId={}", shopId);
                return Result.failure(4107);
            }
            
            // 3. 解析分页参数
            int page = 1;
            int size = 10;
            if (params != null) {
                if (params.get("page") != null) {
                    page = Integer.parseInt(params.get("page").toString());
                }
                if (params.get("size") != null) {
                    size = Integer.parseInt(params.get("size").toString());
                }
            }
            
            // 4. 参数验证和默认值设置
            if (page < 1) {
                page = 1;
            }
            if (size < 1) {
                size = 10;
            }
            
            // 5. 计算分页偏移量
            int offset = (page - 1) * size;
            
            // 6. 查询茶叶列表（只返回上架的茶叶）
            List<Tea> teaList = teaMapper.selectByShopId(shopId, offset, size);
            
            // 7. 查询总数
            Long total = teaMapper.countByShopId(shopId);
            
            // 8. 转换为VO
            List<TeaVO> teaVOList = teaList.stream()
                    .map(this::convertToTeaVO)
                    .collect(Collectors.toList());
            
            // 9. 构建返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("list", teaVOList);
            responseData.put("total", total);
            responseData.put("page", page);
            responseData.put("pageSize", size);
            
            logger.info("获取店铺茶叶列表成功: shopId={}, 总记录数={}, 当前页={}, 每页={}", 
                    shopId, total, page, size);
            
            // 10. 返回成功（根据code-message-mapping.md，成功码是200）
            return Result.success(200, responseData);
            
        } catch (Exception e) {
            logger.error("获取店铺茶叶列表失败: 系统异常, shopId={}", shopId, e);
            return Result.failure(4107);
        }
    }
    
    /**
     * 将Tea实体转换为TeaVO
     *
     * @param tea 茶叶实体
     * @return 茶叶VO
     */
    private TeaVO convertToTeaVO(Tea tea) {
        if (tea == null) {
            return null;
        }
        
        TeaVO teaVO = new TeaVO();
        teaVO.setId(tea.getId());
        teaVO.setName(tea.getName());
        teaVO.setShopId(tea.getShopId());
        teaVO.setCategoryId(tea.getCategoryId());
        teaVO.setPrice(tea.getPrice());
        teaVO.setDescription(tea.getDescription());
        teaVO.setDetail(tea.getDetail());
        teaVO.setOrigin(tea.getOrigin());
        teaVO.setStock(tea.getStock());
        teaVO.setSales(tea.getSales());
        teaVO.setMainImage(tea.getMainImage());
        teaVO.setStatus(tea.getStatus());
        teaVO.setCreateTime(tea.getCreateTime());
        teaVO.setUpdateTime(tea.getUpdateTime());
        
        return teaVO;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> addShopTea(String shopId, Map<String, Object> teaData) {
        try {
            logger.info("添加店铺茶叶请求: shopId={}, teaData={}", shopId, teaData);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("添加店铺茶叶失败: 用户未登录");
                return Result.failure(4108);
            }
            
            // 2. 验证店铺ID不为空
            if (shopId == null || shopId.trim().isEmpty()) {
                logger.warn("添加店铺茶叶失败: 店铺ID为空");
                return Result.failure(4108);
            }
            
            // 3. 查询店铺信息
            Shop shop = getShopById(shopId);
            if (shop == null) {
                logger.warn("添加店铺茶叶失败: 店铺不存在, shopId={}", shopId);
                return Result.failure(4108);
            }
            
            // 4. 验证用户是否为店铺所有者
            if (!userId.equals(shop.getOwnerId())) {
                logger.warn("添加店铺茶叶失败: 无权限操作, userId={}, shopId={}, ownerId={}", 
                        userId, shopId, shop.getOwnerId());
                return Result.failure(4108);
            }
            
            // 5. 提取并验证茶叶数据
            String name = teaData.get("name") != null ? teaData.get("name").toString() : null;
            if (name == null || name.trim().isEmpty()) {
                logger.warn("添加店铺茶叶失败: 茶叶名称为空");
                return Result.failure(4108);
            }
            
            // 6. 构建茶叶实体
            Tea tea = new Tea();
            tea.setId(UUID.randomUUID().toString().replace("-", ""));
            tea.setShopId(shopId);
            tea.setName(name);
            
            // 设置可选字段
            if (teaData.get("categoryId") != null) {
                tea.setCategoryId(Integer.parseInt(teaData.get("categoryId").toString()));
            }
            if (teaData.get("price") != null) {
                tea.setPrice(new BigDecimal(teaData.get("price").toString()));
            }
            if (teaData.get("description") != null) {
                tea.setDescription(teaData.get("description").toString());
            }
            if (teaData.get("detail") != null) {
                tea.setDetail(teaData.get("detail").toString());
            }
            if (teaData.get("origin") != null) {
                tea.setOrigin(teaData.get("origin").toString());
            }
            if (teaData.get("stock") != null) {
                tea.setStock(Integer.parseInt(teaData.get("stock").toString()));
            } else {
                tea.setStock(0); // 默认库存为0
            }
            if (teaData.get("mainImage") != null) {
                tea.setMainImage(teaData.get("mainImage").toString());
            }
            
            // 设置默认值
            tea.setSales(0); // 默认销量为0
            tea.setStatus(1); // 默认上架
            tea.setIsDeleted(0); // 未删除
            
            Date now = new Date();
            tea.setCreateTime(now);
            tea.setUpdateTime(now);
            
            // 7. 插入数据库
            int result = teaMapper.insert(tea);
            if (result <= 0) {
                logger.error("添加店铺茶叶失败: 数据库插入失败, shopId={}", shopId);
                return Result.failure(4108);
            }
            
            logger.info("添加店铺茶叶成功: teaId={}, teaName={}, shopId={}", 
                    tea.getId(), tea.getName(), shopId);
            
            // 8. 返回成功（根据code-message-mapping.md，成功码是4002）
            return Result.success(4002, null);
            
        } catch (Exception e) {
            logger.error("添加店铺茶叶失败: 系统异常, shopId={}", shopId, e);
            return Result.failure(4108);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> updateShopTea(String teaId, Map<String, Object> teaData) {
        try {
            logger.info("更新店铺茶叶请求: teaId={}, teaData={}", teaId, teaData);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("更新店铺茶叶失败: 用户未登录");
                return Result.failure(4109);
            }
            
            // 2. 验证茶叶ID不为空
            if (teaId == null || teaId.trim().isEmpty()) {
                logger.warn("更新店铺茶叶失败: 茶叶ID为空");
                return Result.failure(4109);
            }
            
            // 3. 查询茶叶信息
            Tea tea = teaMapper.selectById(teaId);
            if (tea == null) {
                logger.warn("更新店铺茶叶失败: 茶叶不存在, teaId={}", teaId);
                return Result.failure(4109);
            }
            
            // 4. 查询店铺信息并验证权限
            Shop shop = getShopById(tea.getShopId());
            if (shop == null) {
                logger.warn("更新店铺茶叶失败: 店铺不存在, shopId={}", tea.getShopId());
                return Result.failure(4109);
            }
            
            // 5. 验证用户是否为店铺所有者
            if (!userId.equals(shop.getOwnerId())) {
                logger.warn("更新店铺茶叶失败: 无权限操作, userId={}, teaId={}, shopId={}, ownerId={}", 
                        userId, teaId, tea.getShopId(), shop.getOwnerId());
                return Result.failure(4109);
            }
            
            // 6. 更新茶叶信息
            boolean updated = false;
            
            if (teaData.get("name") != null) {
                tea.setName(teaData.get("name").toString());
                updated = true;
            }
            
            if (teaData.get("categoryId") != null) {
                tea.setCategoryId(Integer.parseInt(teaData.get("categoryId").toString()));
                updated = true;
            }
            
            if (teaData.get("price") != null) {
                tea.setPrice(new BigDecimal(teaData.get("price").toString()));
                updated = true;
            }
            
            if (teaData.get("description") != null) {
                tea.setDescription(teaData.get("description").toString());
                updated = true;
            }
            
            if (teaData.get("detail") != null) {
                tea.setDetail(teaData.get("detail").toString());
                updated = true;
            }
            
            if (teaData.get("origin") != null) {
                tea.setOrigin(teaData.get("origin").toString());
                updated = true;
            }
            
            if (teaData.get("stock") != null) {
                tea.setStock(Integer.parseInt(teaData.get("stock").toString()));
                updated = true;
            }
            
            if (teaData.get("mainImage") != null) {
                tea.setMainImage(teaData.get("mainImage").toString());
                updated = true;
            }
            
            // 7. 如果有更新，则保存到数据库
            if (updated) {
                tea.setUpdateTime(new Date());
                int result = teaMapper.updateById(tea);
                if (result <= 0) {
                    logger.error("更新店铺茶叶失败: 数据库更新失败, teaId={}", teaId);
                    return Result.failure(4109);
                }
                
                logger.info("更新店铺茶叶成功: teaId={}, teaName={}", teaId, tea.getName());
            } else {
                logger.info("更新店铺茶叶: 无需更新, teaId={}", teaId);
            }
            
            // 8. 返回成功（根据code-message-mapping.md，成功码是4003）
            return Result.success(4003, true);
            
        } catch (Exception e) {
            logger.error("更新店铺茶叶失败: 系统异常, teaId={}", teaId, e);
            return Result.failure(4109);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> deleteShopTea(String teaId) {
        try {
            logger.info("删除店铺茶叶请求: teaId={}", teaId);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("删除店铺茶叶失败: 用户未登录");
                return Result.failure(4110);
            }
            
            // 2. 验证茶叶ID不为空
            if (teaId == null || teaId.trim().isEmpty()) {
                logger.warn("删除店铺茶叶失败: 茶叶ID为空");
                return Result.failure(4110);
            }
            
            // 3. 查询茶叶信息
            Tea tea = teaMapper.selectById(teaId);
            if (tea == null) {
                logger.warn("删除店铺茶叶失败: 茶叶不存在, teaId={}", teaId);
                return Result.failure(4110);
            }
            
            // 4. 查询店铺信息并验证权限
            Shop shop = getShopById(tea.getShopId());
            if (shop == null) {
                logger.warn("删除店铺茶叶失败: 店铺不存在, shopId={}", tea.getShopId());
                return Result.failure(4110);
            }
            
            // 5. 验证用户是否为店铺所有者
            if (!userId.equals(shop.getOwnerId())) {
                logger.warn("删除店铺茶叶失败: 无权限操作, userId={}, teaId={}, shopId={}, ownerId={}", 
                        userId, teaId, tea.getShopId(), shop.getOwnerId());
                return Result.failure(4110);
            }
            
            // 6. 逻辑删除茶叶（使用BaseMapper的deleteById方法，teaId是String类型）
            int result = teaMapper.deleteById(teaId);
            if (result <= 0) {
                logger.error("删除店铺茶叶失败: 数据库删除失败, teaId={}", teaId);
                return Result.failure(4110);
            }
            
            logger.info("删除店铺茶叶成功: teaId={}, teaName={}", teaId, tea.getName());
            
            // 7. 返回成功（根据code-message-mapping.md，成功码是4004）
            return Result.success(4004, null);
            
        } catch (Exception e) {
            logger.error("删除店铺茶叶失败: 系统异常, teaId={}", teaId, e);
            return Result.failure(4110);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> toggleShopTeaStatus(String teaId, Integer status) {
        try {
            logger.info("茶叶上下架请求: teaId={}, status={}", teaId, status);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("茶叶上下架失败: 用户未登录");
                // 根据status返回不同的失败码
                return Result.failure(status == 1 ? 4111 : 4112);
            }
            
            // 2. 验证参数
            if (teaId == null || teaId.trim().isEmpty()) {
                logger.warn("茶叶上下架失败: 茶叶ID为空");
                return Result.failure(status == 1 ? 4111 : 4112);
            }
            
            if (status == null || (status != 0 && status != 1)) {
                logger.warn("茶叶上下架失败: 状态参数无效, status={}", status);
                return Result.failure(status == 1 ? 4111 : 4112);
            }
            
            // 3. 查询茶叶信息
            Tea tea = teaMapper.selectById(teaId);
            if (tea == null) {
                logger.warn("茶叶上下架失败: 茶叶不存在, teaId={}", teaId);
                return Result.failure(status == 1 ? 4111 : 4112);
            }
            
            // 4. 查询店铺信息并验证权限
            Shop shop = getShopById(tea.getShopId());
            if (shop == null) {
                logger.warn("茶叶上下架失败: 店铺不存在, shopId={}", tea.getShopId());
                return Result.failure(status == 1 ? 4111 : 4112);
            }
            
            // 5. 验证用户是否为店铺所有者
            if (!userId.equals(shop.getOwnerId())) {
                logger.warn("茶叶上下架失败: 无权限操作, userId={}, teaId={}, shopId={}, ownerId={}", 
                        userId, teaId, tea.getShopId(), shop.getOwnerId());
                return Result.failure(status == 1 ? 4111 : 4112);
            }
            
            // 6. 更新茶叶状态
            tea.setStatus(status);
            tea.setUpdateTime(new Date());
            int result = teaMapper.updateById(tea);
            if (result <= 0) {
                logger.error("茶叶上下架失败: 数据库更新失败, teaId={}", teaId);
                return Result.failure(status == 1 ? 4111 : 4112);
            }
            
            logger.info("茶叶上下架成功: teaId={}, teaName={}, status={}", teaId, tea.getName(), status);
            
            // 7. 返回成功（根据code-message-mapping.md，上架成功码是4005，下架成功码是4006）
            return Result.success(status == 1 ? 4005 : 4006, null);
            
        } catch (Exception e) {
            logger.error("茶叶上下架失败: 系统异常, teaId={}, status={}", teaId, status, e);
            return Result.failure(status == 1 ? 4111 : 4112);
        }
    }
    
    @Override
    public Result<Object> getShopBanners(String shopId) {
        try {
            logger.info("获取店铺Banner列表请求: shopId={}", shopId);
            
            // 1. 验证店铺ID不为空
            if (shopId == null || shopId.trim().isEmpty()) {
                logger.warn("获取店铺Banner列表失败: 店铺ID为空");
                return Result.failure(4116);
            }
            
            // 2. 验证店铺是否存在
            Shop shop = getShopById(shopId);
            if (shop == null) {
                logger.warn("获取店铺Banner列表失败: 店铺不存在, shopId={}", shopId);
                return Result.failure(4116);
            }
            
            // 3. 查询Banner列表（只返回status=1的）
            List<ShopBanner> bannerList = shopBannerMapper.selectByShopId(shopId);
            
            // 4. 转换为VO
            List<BannerVO> bannerVOList = bannerList.stream()
                    .map(this::convertToBannerVO)
                    .collect(Collectors.toList());
            
            logger.info("获取店铺Banner列表成功: shopId={}, 数量={}", shopId, bannerVOList.size());
            
            // 5. 返回成功（根据code-message-mapping.md，成功码是200）
            return Result.success(200, bannerVOList);
            
        } catch (Exception e) {
            logger.error("获取店铺Banner列表失败: 系统异常, shopId={}", shopId, e);
            return Result.failure(4116);
        }
    }
    
    /**
     * 将ShopBanner实体转换为BannerVO
     *
     * @param banner Banner实体
     * @return BannerVO
     */
    private BannerVO convertToBannerVO(ShopBanner banner) {
        if (banner == null) {
            return null;
        }
        
        BannerVO bannerVO = new BannerVO();
        bannerVO.setId(banner.getId());
        bannerVO.setImageUrl(banner.getImageUrl());
        bannerVO.setTitle(banner.getTitle());
        bannerVO.setLinkUrl(banner.getLinkUrl());
        bannerVO.setSortOrder(banner.getSortOrder());
        
        return bannerVO;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> updateBanner(String bannerId, Map<String, Object> bannerData) {
        try {
            logger.info("更新店铺Banner请求: bannerId={}, bannerData={}", bannerId, bannerData);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("更新Banner失败: 用户未登录");
                return Result.failure(4120);
            }
            
            // 2. 验证Banner ID不为空
            if (bannerId == null || bannerId.trim().isEmpty()) {
                logger.warn("更新Banner失败: Banner ID为空");
                return Result.failure(4120);
            }
            
            // 3. 查询Banner信息
            Integer bannerIdInt = Integer.parseInt(bannerId);
            ShopBanner banner = shopBannerMapper.selectById(bannerIdInt);
            if (banner == null) {
                logger.warn("更新Banner失败: Banner不存在, bannerId={}", bannerId);
                return Result.failure(4120);
            }
            
            // 4. 查询店铺信息并验证权限
            Shop shop = getShopById(banner.getShopId());
            if (shop == null) {
                logger.warn("更新Banner失败: 店铺不存在, shopId={}", banner.getShopId());
                return Result.failure(4120);
            }
            
            // 5. 验证用户是否为店铺所有者
            if (!userId.equals(shop.getOwnerId())) {
                logger.warn("更新Banner失败: 无权限操作, userId={}, bannerId={}, shopId={}, ownerId={}", 
                        userId, bannerId, banner.getShopId(), shop.getOwnerId());
                return Result.failure(4120);
            }
            
            // 6. 更新Banner信息
            boolean updated = false;
            
            if (bannerData.get("title") != null) {
                banner.setTitle(bannerData.get("title").toString());
                updated = true;
            }
            
            if (bannerData.get("linkUrl") != null) {
                banner.setLinkUrl(bannerData.get("linkUrl").toString());
                updated = true;
            }
            
            if (bannerData.get("sortOrder") != null) {
                banner.setSortOrder(Integer.parseInt(bannerData.get("sortOrder").toString()));
                updated = true;
            }
            
            // 7. 如果有更新，则保存到数据库
            if (updated) {
                banner.setUpdateTime(new Date());
                int result = shopBannerMapper.updateById(banner);
                if (result <= 0) {
                    logger.error("更新Banner失败: 数据库更新失败, bannerId={}", bannerId);
                    return Result.failure(4120);
                }
                
                logger.info("更新Banner成功: bannerId={}, title={}", bannerId, banner.getTitle());
            } else {
                logger.info("更新Banner: 无需更新, bannerId={}", bannerId);
            }
            
            // 8. 返回成功（根据code-message-mapping.md，成功码是4009）
            return Result.success(4009, null);
            
        } catch (Exception e) {
            logger.error("更新Banner失败: 系统异常, bannerId={}", bannerId, e);
            return Result.failure(4120);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deleteBanner(String bannerId) {
        try {
            logger.info("删除店铺Banner请求: bannerId={}", bannerId);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("删除Banner失败: 用户未登录");
                return Result.failure(4121);
            }
            
            // 2. 验证Banner ID不为空
            if (bannerId == null || bannerId.trim().isEmpty()) {
                logger.warn("删除Banner失败: Banner ID为空");
                return Result.failure(4121);
            }
            
            // 3. 查询Banner信息
            Integer bannerIdInt = Integer.parseInt(bannerId);
            ShopBanner banner = shopBannerMapper.selectById(bannerIdInt);
            if (banner == null) {
                logger.warn("删除Banner失败: Banner不存在, bannerId={}", bannerId);
                return Result.failure(4121);
            }
            
            // 4. 查询店铺信息并验证权限
            Shop shop = getShopById(banner.getShopId());
            if (shop == null) {
                logger.warn("删除Banner失败: 店铺不存在, shopId={}", banner.getShopId());
                return Result.failure(4121);
            }
            
            // 5. 验证用户是否为店铺所有者
            if (!userId.equals(shop.getOwnerId())) {
                logger.warn("删除Banner失败: 无权限操作, userId={}, bannerId={}, shopId={}, ownerId={}", 
                        userId, bannerId, banner.getShopId(), shop.getOwnerId());
                return Result.failure(4121);
            }
            
            // 6. 删除Banner
            int result = shopBannerMapper.deleteById(bannerIdInt);
            if (result <= 0) {
                logger.error("删除Banner失败: 数据库删除失败, bannerId={}", bannerId);
                return Result.failure(4121);
            }
            
            logger.info("删除Banner成功: bannerId={}, title={}", bannerId, banner.getTitle());
            
            // 7. 返回成功（根据code-message-mapping.md，成功码是4010）
            return Result.success(4010, true);
            
        } catch (Exception e) {
            logger.error("删除Banner失败: 系统异常, bannerId={}", bannerId, e);
            return Result.failure(4121);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> updateBannerOrder(Map<String, Object> orderData) {
        try {
            logger.info("更新Banner顺序请求: orderData={}", orderData);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("更新Banner顺序失败: 用户未登录");
                return Result.failure(4122);
            }
            
            // 2. 提取orders数组
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orders = (List<Map<String, Object>>) orderData.get("orders");
            if (orders == null || orders.isEmpty()) {
                logger.warn("更新Banner顺序失败: orders参数为空");
                return Result.failure(4122);
            }
            
            // 3. 批量更新Banner排序
            for (Map<String, Object> order : orders) {
                String bannerId = order.get("id") != null ? order.get("id").toString() : null;
                Integer sortOrder = order.get("order") != null ? Integer.parseInt(order.get("order").toString()) : null;
                
                if (bannerId == null || sortOrder == null) {
                    continue;
                }
                
                // 查询Banner
                Integer bannerIdInt = Integer.parseInt(bannerId);
                ShopBanner banner = shopBannerMapper.selectById(bannerIdInt);
                if (banner == null) {
                    continue;
                }
                
                // 验证权限
                Shop shop = getShopById(banner.getShopId());
                if (shop == null || !userId.equals(shop.getOwnerId())) {
                    logger.warn("更新Banner顺序失败: 无权限操作, userId={}, bannerId={}", userId, bannerId);
                    return Result.failure(4122);
                }
                
                // 更新排序值
                banner.setSortOrder(sortOrder);
                banner.setUpdateTime(new Date());
                shopBannerMapper.updateById(banner);
            }
            
            logger.info("更新Banner顺序成功: 更新数量={}", orders.size());
            
            // 4. 返回成功（根据code-message-mapping.md，成功码是4011）
            return Result.success(4011, true);
            
        } catch (Exception e) {
            logger.error("更新Banner顺序失败: 系统异常", e);
            return Result.failure(4122);
        }
    }
    
    @Override
    public Result<Object> getShopAnnouncements(String shopId) {
        try {
            logger.info("获取店铺公告列表请求: shopId={}", shopId);
            
            // 1. 验证店铺ID不为空
            if (shopId == null || shopId.trim().isEmpty()) {
                logger.warn("获取店铺公告列表失败: 店铺ID为空");
                return Result.failure(4123);
            }
            
            // 2. 验证店铺是否存在
            Shop shop = getShopById(shopId);
            if (shop == null) {
                logger.warn("获取店铺公告列表失败: 店铺不存在, shopId={}", shopId);
                return Result.failure(4123);
            }
            
            // 3. 查询公告列表（只返回status=1的）
            List<ShopAnnouncement> announcementList = shopAnnouncementMapper.selectByShopId(shopId);
            
            // 4. 转换为VO
            List<AnnouncementVO> announcementVOList = announcementList.stream()
                    .map(this::convertToAnnouncementVO)
                    .collect(Collectors.toList());
            
            logger.info("获取店铺公告列表成功: shopId={}, 数量={}", shopId, announcementVOList.size());
            
            // 5. 返回成功（根据code-message-mapping.md，成功码是200）
            return Result.success(200, announcementVOList);
            
        } catch (Exception e) {
            logger.error("获取店铺公告列表失败: 系统异常, shopId={}", shopId, e);
            return Result.failure(4123);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> createAnnouncement(String shopId, Map<String, Object> announcementData) {
        try {
            logger.info("创建店铺公告请求: shopId={}, announcementData={}", shopId, announcementData);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("创建公告失败: 用户未登录");
                return Result.failure(4124);
            }
            
            // 2. 验证店铺ID不为空
            if (shopId == null || shopId.trim().isEmpty()) {
                logger.warn("创建公告失败: 店铺ID为空");
                return Result.failure(4124);
            }
            
            // 3. 查询店铺信息
            Shop shop = getShopById(shopId);
            if (shop == null) {
                logger.warn("创建公告失败: 店铺不存在, shopId={}", shopId);
                return Result.failure(4124);
            }
            
            // 4. 验证用户是否为店铺所有者
            if (!userId.equals(shop.getOwnerId())) {
                logger.warn("创建公告失败: 无权限操作, userId={}, shopId={}, ownerId={}", 
                        userId, shopId, shop.getOwnerId());
                return Result.failure(4124);
            }
            
            // 5. 提取并验证公告数据
            String title = announcementData.get("title") != null ? announcementData.get("title").toString() : null;
            if (title == null || title.trim().isEmpty()) {
                logger.warn("创建公告失败: 公告标题为空");
                return Result.failure(4124);
            }
            
            String content = announcementData.get("content") != null ? announcementData.get("content").toString() : null;
            if (content == null || content.trim().isEmpty()) {
                logger.warn("创建公告失败: 公告内容为空");
                return Result.failure(4124);
            }
            
            // 6. 构建公告实体
            ShopAnnouncement announcement = new ShopAnnouncement();
            announcement.setShopId(shopId);
            announcement.setTitle(title);
            announcement.setContent(content);
            
            // 设置可选字段
            if (announcementData.get("isTop") != null) {
                Boolean isTop = Boolean.parseBoolean(announcementData.get("isTop").toString());
                announcement.setIsTop(isTop ? 1 : 0);
            } else {
                announcement.setIsTop(0); // 默认不置顶
            }
            
            // 设置默认值
            announcement.setStatus(1); // 默认显示
            
            Date now = new Date();
            announcement.setCreateTime(now);
            announcement.setUpdateTime(now);
            
            // 7. 插入数据库
            int result = shopAnnouncementMapper.insert(announcement);
            if (result <= 0) {
                logger.error("创建公告失败: 数据库插入失败, shopId={}", shopId);
                return Result.failure(4124);
            }
            
            logger.info("创建公告成功: announcementId={}, title={}, shopId={}", 
                    announcement.getId(), announcement.getTitle(), shopId);
            
            // 8. 返回成功（根据code-message-mapping.md，成功码是4012）
            return Result.success(4012, null);
            
        } catch (Exception e) {
            logger.error("创建公告失败: 系统异常, shopId={}", shopId, e);
            return Result.failure(4124);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> updateAnnouncement(String announcementId, Map<String, Object> announcementData) {
        try {
            logger.info("更新店铺公告请求: announcementId={}, announcementData={}", announcementId, announcementData);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("更新公告失败: 用户未登录");
                return Result.failure(4125);
            }
            
            // 2. 验证公告ID不为空
            if (announcementId == null || announcementId.trim().isEmpty()) {
                logger.warn("更新公告失败: 公告ID为空");
                return Result.failure(4125);
            }
            
            // 3. 查询公告信息
            Integer announcementIdInt = Integer.parseInt(announcementId);
            ShopAnnouncement announcement = shopAnnouncementMapper.selectById(announcementIdInt);
            if (announcement == null) {
                logger.warn("更新公告失败: 公告不存在, announcementId={}", announcementId);
                return Result.failure(4125);
            }
            
            // 4. 查询店铺信息并验证权限
            Shop shop = getShopById(announcement.getShopId());
            if (shop == null) {
                logger.warn("更新公告失败: 店铺不存在, shopId={}", announcement.getShopId());
                return Result.failure(4125);
            }
            
            // 5. 验证用户是否为店铺所有者
            if (!userId.equals(shop.getOwnerId())) {
                logger.warn("更新公告失败: 无权限操作, userId={}, announcementId={}, shopId={}, ownerId={}", 
                        userId, announcementId, announcement.getShopId(), shop.getOwnerId());
                return Result.failure(4125);
            }
            
            // 6. 更新公告信息
            boolean updated = false;
            
            if (announcementData.get("title") != null) {
                announcement.setTitle(announcementData.get("title").toString());
                updated = true;
            }
            
            if (announcementData.get("content") != null) {
                announcement.setContent(announcementData.get("content").toString());
                updated = true;
            }
            
            if (announcementData.get("isTop") != null) {
                Boolean isTop = Boolean.parseBoolean(announcementData.get("isTop").toString());
                announcement.setIsTop(isTop ? 1 : 0);
                updated = true;
            }
            
            // 7. 如果有更新，则保存到数据库
            if (updated) {
                announcement.setUpdateTime(new Date());
                int result = shopAnnouncementMapper.updateById(announcement);
                if (result <= 0) {
                    logger.error("更新公告失败: 数据库更新失败, announcementId={}", announcementId);
                    return Result.failure(4125);
                }
                
                logger.info("更新公告成功: announcementId={}, title={}", announcementId, announcement.getTitle());
            } else {
                logger.info("更新公告: 无需更新, announcementId={}", announcementId);
            }
            
            // 8. 返回成功（根据code-message-mapping.md，成功码是4013）
            return Result.success(4013, null);
            
        } catch (Exception e) {
            logger.error("更新公告失败: 系统异常, announcementId={}", announcementId, e);
            return Result.failure(4125);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deleteAnnouncement(String announcementId) {
        try {
            logger.info("删除店铺公告请求: announcementId={}", announcementId);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("删除公告失败: 用户未登录");
                return Result.failure(4126);
            }
            
            // 2. 验证公告ID不为空
            if (announcementId == null || announcementId.trim().isEmpty()) {
                logger.warn("删除公告失败: 公告ID为空");
                return Result.failure(4126);
            }
            
            // 3. 查询公告信息
            Integer announcementIdInt = Integer.parseInt(announcementId);
            ShopAnnouncement announcement = shopAnnouncementMapper.selectById(announcementIdInt);
            if (announcement == null) {
                logger.warn("删除公告失败: 公告不存在, announcementId={}", announcementId);
                return Result.failure(4126);
            }
            
            // 4. 查询店铺信息并验证权限
            Shop shop = getShopById(announcement.getShopId());
            if (shop == null) {
                logger.warn("删除公告失败: 店铺不存在, shopId={}", announcement.getShopId());
                return Result.failure(4126);
            }
            
            // 5. 验证用户是否为店铺所有者
            if (!userId.equals(shop.getOwnerId())) {
                logger.warn("删除公告失败: 无权限操作, userId={}, announcementId={}, shopId={}, ownerId={}", 
                        userId, announcementId, announcement.getShopId(), shop.getOwnerId());
                return Result.failure(4126);
            }
            
            // 6. 删除公告
            int result = shopAnnouncementMapper.deleteById(announcementIdInt);
            if (result <= 0) {
                logger.error("删除公告失败: 数据库删除失败, announcementId={}", announcementId);
                return Result.failure(4126);
            }
            
            logger.info("删除公告成功: announcementId={}, title={}", announcementId, announcement.getTitle());
            
            // 7. 返回成功（根据code-message-mapping.md，成功码是4014）
            return Result.success(4014, true);
            
        } catch (Exception e) {
            logger.error("删除公告失败: 系统异常, announcementId={}", announcementId, e);
            return Result.failure(4126);
        }
    }
    
    /**
     * 将ShopAnnouncement实体转换为AnnouncementVO
     *
     * @param announcement 公告实体
     * @return AnnouncementVO
     */
    private AnnouncementVO convertToAnnouncementVO(ShopAnnouncement announcement) {
        if (announcement == null) {
            return null;
        }
        
        AnnouncementVO announcementVO = new AnnouncementVO();
        announcementVO.setId(announcement.getId());
        announcementVO.setTitle(announcement.getTitle());
        announcementVO.setContent(announcement.getContent());
        announcementVO.setIsTop(announcement.getIsTop() == 1);
        announcementVO.setCreateTime(announcement.getCreateTime());
        
        return announcementVO;
    }
    
    // ⚠️ 已删除：店铺关注相关方法实现（followShop, unfollowShop, checkFollowStatus）
    // 说明：店铺关注功能已统一使用用户模块的通用接口（UserServiceImpl 中的 addFollow/removeFollow）
    // 店铺详情接口（getShopDetail）已包含 isFollowed 字段，无需单独检查关注状态
    
    @Override
    public Result<Object> getShopReviews(String shopId, Map<String, Object> params) {
        try {
            logger.info("获取店铺评分信息请求: shopId={}", shopId);
            
            // 1. 验证店铺ID不为空
            if (shopId == null || shopId.trim().isEmpty()) {
                logger.warn("获取店铺评分信息失败: 店铺ID为空");
                return Result.failure(4130);
            }
            
            // 2. 查询店铺信息
            Shop shop = getShopById(shopId);
            if (shop == null) {
                logger.warn("获取店铺评分信息失败: 店铺不存在, shopId={}", shopId);
                return Result.failure(4130);
            }
            
            // 3. 构建返回数据（只返回评分信息，不返回评价列表）
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("rating", shop.getRating());
            responseData.put("ratingCount", shop.getRatingCount());
            responseData.put("list", new ArrayList<>()); // 空列表
            responseData.put("total", 0);
            
            logger.info("获取店铺评分信息成功: shopId={}, rating={}, ratingCount={}", 
                    shopId, shop.getRating(), shop.getRatingCount());
            
            // 4. 返回成功（根据code-message-mapping.md，成功码是200）
            return Result.success(200, responseData);
            
        } catch (Exception e) {
            logger.error("获取店铺评分信息失败: 系统异常, shopId={}", shopId, e);
            return Result.failure(4130);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> submitShopReview(String shopId, Map<String, Object> reviewData) {
        try {
            logger.info("提交店铺评分请求: shopId={}, reviewData={}", shopId, reviewData);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("提交评分失败: 用户未登录");
                return Result.failure(4132);
            }
            
            // 2. 验证店铺ID不为空
            if (shopId == null || shopId.trim().isEmpty()) {
                logger.warn("提交评分失败: 店铺ID为空");
                return Result.failure(4132);
            }
            
            // 3. 验证店铺是否存在
            Shop shop = getShopById(shopId);
            if (shop == null) {
                logger.warn("提交评分失败: 店铺不存在, shopId={}", shopId);
                return Result.failure(4132);
            }
            
            // 4. 验证用户是否购买过该店铺的茶叶（查询已完成的订单）
            List<Order> userOrders = orderMapper.selectByUserIdAndStatus(userId, 3); // 状态3=已完成
            boolean hasPurchased = false;
            if (userOrders != null && !userOrders.isEmpty()) {
                for (Order order : userOrders) {
                    // 检查订单中的商品是否属于该店铺
                    if (shopId.equals(order.getShopId())) {
                        hasPurchased = true;
                        break;
                    }
                }
            }
            
            if (!hasPurchased) {
                logger.warn("提交评分失败: 用户未购买过该店铺的商品, userId={}, shopId={}", userId, shopId);
                return Result.failure(4131); // 未购买过该店铺商品
            }
            
            // 5. 提取并验证评分数据
            Integer rating = reviewData.get("rating") != null ? Integer.parseInt(reviewData.get("rating").toString()) : null;
            if (rating == null || rating < 1 || rating > 5) {
                logger.warn("提交评分失败: 评分无效, rating={}", rating);
                return Result.failure(4132);
            }
            
            // 6. 更新店铺评分统计
            int currentCount = shop.getRatingCount();
            BigDecimal currentRating = shop.getRating();
            
            // 计算新的平均评分
            BigDecimal newRating = currentRating.multiply(new BigDecimal(currentCount))
                    .add(new BigDecimal(rating))
                    .divide(new BigDecimal(currentCount + 1), 2, BigDecimal.ROUND_HALF_UP);
            
            shop.setRating(newRating);
            shop.setRatingCount(currentCount + 1);
            shop.setUpdateTime(new Date());
            
            int result = shopMapper.updateById(shop);
            if (result <= 0) {
                logger.error("提交评分失败: 数据库更新失败, shopId={}", shopId);
                return Result.failure(4132);
            }
            
            logger.info("提交评分成功: userId={}, shopId={}, rating={}, newAvgRating={}", 
                    userId, shopId, rating, newRating);
            
            // 7. 返回成功（根据code-message-mapping.md，成功码是4017）
            return Result.success(4017, true);
            
        } catch (Exception e) {
            logger.error("提交评分失败: 系统异常, shopId={}", shopId, e);
            return Result.failure(4132);
        }
    }
}