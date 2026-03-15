package com.shangnantea.service.impl;

import com.shangnantea.common.api.Result;
import com.shangnantea.exception.BusinessException;
import com.shangnantea.mapper.OrderMapper;
import com.shangnantea.mapper.ShopAnnouncementMapper;
import com.shangnantea.mapper.ShopBannerMapper;
import com.shangnantea.mapper.ShopCertificationMapper;
import com.shangnantea.mapper.ShopMapper;
import com.shangnantea.mapper.ShopReviewMapper;
import com.shangnantea.mapper.TeaMapper;
import com.shangnantea.mapper.TeaSpecificationMapper;
import com.shangnantea.mapper.TeaImageMapper;
import com.shangnantea.mapper.UserFollowMapper;
import com.shangnantea.model.dto.shop.ShopQueryDTO;
import com.shangnantea.model.entity.order.Order;
import com.shangnantea.model.entity.shop.Shop;
import com.shangnantea.model.entity.shop.ShopAnnouncement;
import com.shangnantea.model.entity.shop.ShopBanner;
import com.shangnantea.model.entity.shop.ShopCertification;
import com.shangnantea.model.entity.shop.ShopReview;
import com.shangnantea.model.entity.tea.Tea;
import com.shangnantea.model.entity.tea.TeaSpecification;
import com.shangnantea.model.entity.tea.TeaImage;
import com.shangnantea.model.entity.user.UserFollow;
import com.shangnantea.model.vo.TeaVO;
import com.shangnantea.model.vo.ReviewVO;
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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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
    private ShopReviewMapper shopReviewMapper;
    
    @Autowired
    private UserFollowMapper userFollowMapper;
    
    @Autowired
    private TeaMapper teaMapper;
    
    @Autowired
    private TeaSpecificationMapper teaSpecificationMapper;
    
    @Autowired
    private TeaImageMapper teaImageMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private StatisticsUtils statisticsUtils;
    
    @Autowired
    private com.shangnantea.mapper.UserMapper userMapper;
    
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
     * 从参数中安全获取去掉首尾空格的字符串，null 或全空白时返回空串
     */
    private String getTrimmed(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString().trim();
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
    public Result<Object> uploadBanner(String shopId, MultipartFile file, String title, String linkUrl, Integer sortOrder) {
        try {
            logger.info("上传店铺Banner请求, shopId: {}, title: {}, sortOrder: {}, 文件名: {}", 
                    shopId, title, sortOrder, file.getOriginalFilename());
            
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
            
            // 6. 计算排序值
            int finalSortOrder = 0;
            try {
                // 如果前端显式传入了排序且大于0，则优先使用
                if (sortOrder != null && sortOrder > 0) {
                    finalSortOrder = sortOrder;
                } else {
                    // 否则按当前最大 sort_order + 1 追加在末尾
                    List<ShopBanner> existingBanners = shopBannerMapper.selectByShopId(shopId);
                    int maxOrder = 0;
                    if (existingBanners != null) {
                        for (ShopBanner b : existingBanners) {
                            if (b != null && b.getSortOrder() != null && b.getSortOrder() > maxOrder) {
                                maxOrder = b.getSortOrder();
                            }
                        }
                    }
                    finalSortOrder = maxOrder + 1;
                }
            } catch (Exception e) {
                logger.warn("计算Banner排序值失败，使用默认0: shopId={}, sortOrderParam={}, error={}", 
                        shopId, sortOrder, e.getMessage());
                finalSortOrder = 0;
            }
            
            // 7. 创建Banner记录
            ShopBanner banner = new ShopBanner();
            banner.setShopId(shopId);
            banner.setImageUrl(relativePath);
            banner.setTitle(title);
            banner.setLinkUrl(linkUrl);
            banner.setSortOrder(finalSortOrder);
            banner.setStatus(1); // 默认显示
            banner.setCreateTime(new Date());
            banner.setUpdateTime(new Date());
            
            // 8. 保存到数据库
            int result = shopBannerMapper.insert(banner);
            if (result <= 0) {
                logger.error("Banner上传失败: 数据库插入失败, shopId: {}", shopId);
                return Result.failure(4117);
            }
            
            // 9. 构造返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("id", banner.getId());
            responseData.put("url", accessUrl);
            responseData.put("path", relativePath);
            
            logger.info("Banner上传成功: shopId: {}, bannerId: {}, sortOrder: {}, path: {}", 
                    shopId, banner.getId(), finalSortOrder, relativePath);
            
            // 10. 返回成功（根据code-message-mapping.md，成功码是4008）
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
            
            // 1. 解析查询参数（兼容空串）
            ShopQueryDTO queryDTO = new ShopQueryDTO();
            if (params != null) {
                String pageStr = getTrimmed(params.get("page"));
                if (!pageStr.isEmpty()) {
                    queryDTO.setPage(Integer.parseInt(pageStr));
                }
                String sizeStr = getTrimmed(params.get("size"));
                if (!sizeStr.isEmpty()) {
                    queryDTO.setSize(Integer.parseInt(sizeStr));
                }
                if (params.get("keyword") != null) {
                    queryDTO.setKeyword(params.get("keyword").toString());
                }
                String ratingStr = getTrimmed(params.get("rating"));
                if (!ratingStr.isEmpty()) {
                    queryDTO.setRating(Double.parseDouble(ratingStr));
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
            
            // 1. 获取用户ID（仅支持审核通过后自动创建：从shopData中获取userId）
            if (shopData == null || !shopData.containsKey("userId")) {
                logger.warn("创建店铺失败: shopData中缺少userId，仅支持审核通过后自动创建");
                return Result.failure(4101);
            }
            String userId = shopData.get("userId").toString();
            logger.info("审核通过后自动创建店铺: userId: {}", userId);
            
            // 2. 验证用户是否有商家认证（必须是已通过认证的申请者）
            // 注意：如果是从processCertification调用的，此时认证状态已更新但事务未提交
            // 使用REQUIRED传播，可以读取到同一事务中的更新
            ShopCertification certification = getCertificationByUserId(userId);
            if (certification == null || certification.getStatus() != 1) {
                logger.warn("创建店铺失败: 用户未通过商家认证, userId: {}, certification: {}", userId, 
                    certification != null ? "status=" + certification.getStatus() : "null");
                return Result.failure(4101);
            }
            
            // 3. 验证用户是否已有店铺
            Shop existingShop = getShopByUserId(userId);
            if (existingShop != null) {
                logger.warn("创建店铺失败: 用户已有店铺, userId: {}, shopId: {}", userId, existingShop.getId());
                return Result.failure(4101);
            }
            
            // 4. 提取并验证店铺名称（shopData已在第379行检查过，此处不再重复检查）
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
            shop.setId(generateShopId());
            shop.setOwnerId(userId);
            shop.setShopName(shopName);
            
            // 设置可选字段（支持从认证信息中读取）
            if (shopData.get("logo") != null) {
                shop.setLogo(shopData.get("logo").toString());
            }
            if (shopData.get("description") != null) {
                shop.setDescription(shopData.get("description").toString());
            }
            if (shopData.get("contactPhone") != null) {
                shop.setContactPhone(shopData.get("contactPhone").toString());
            }
            if (shopData.get("province") != null) {
                shop.setProvince(shopData.get("province").toString());
            }
            if (shopData.get("city") != null) {
                shop.setCity(shopData.get("city").toString());
            }
            if (shopData.get("district") != null) {
                shop.setDistrict(shopData.get("district").toString());
            }
            if (shopData.get("address") != null) {
                shop.setAddress(shopData.get("address").toString());
            }
            if (shopData.get("businessLicense") != null) {
                shop.setBusinessLicense(shopData.get("businessLicense").toString());
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
        // 店铺Logo同样可能存的是相对路径，这里统一补全为可直接访问的URL
        String logo = shop.getLogo();
        if (logo != null && !logo.trim().isEmpty()) {
            if (logo.startsWith("http://") || logo.startsWith("https://")) {
                shopVO.setLogo(logo);
            } else {
                shopVO.setLogo(FileUploadUtils.generateAccessUrl(logo, baseUrl));
            }
        } else {
            shopVO.setLogo(null);
        }
        shopVO.setDescription(shop.getDescription());
        shopVO.setRating(shop.getRating());
        // 评分人数直接来自 shops.rating_count
        shopVO.setRatingCount(shop.getRatingCount());
        shopVO.setSalesCount(shop.getSalesCount());
        // 使用动态计算获取关注数
        shopVO.setFollowCount(statisticsUtils.getFollowCount("shop", shop.getId()));
        shopVO.setOwnerId(shop.getOwnerId());
        shopVO.setCreateTime(shop.getCreateTime());
        
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
        String logo = shop.getLogo();
        if (logo != null && !logo.trim().isEmpty()) {
            if (logo.startsWith("http://") || logo.startsWith("https://")) {
                shopDetailVO.setLogo(logo);
            } else {
                shopDetailVO.setLogo(FileUploadUtils.generateAccessUrl(logo, baseUrl));
            }
        } else {
            shopDetailVO.setLogo(null);
        }
        shopDetailVO.setDescription(shop.getDescription());
        // 注意：评分和评分人数不应在此接口返回，应使用专门的评分接口 GET /shop/{shopId}/reviews
        shopDetailVO.setSalesCount(shop.getSalesCount());
        // 使用动态计算获取关注数
        shopDetailVO.setFollowCount(statisticsUtils.getFollowCount("shop", shop.getId()));
        // 开店时间（创建时间）
        shopDetailVO.setCreateTime(shop.getCreateTime());
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
            // 返回 code=4001，data=null
            return Result.success(4001);
            
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
            
            // 5. 解析日期范围参数
            String startDateParam = null;
            String endDateParam = null;
            if (params != null) {
                if (params.get("startDate") != null) {
                    String startDateStr = params.get("startDate").toString().trim();
                    if (!startDateStr.isEmpty()) {
                        startDateParam = startDateStr;
                    }
                }
                if (params.get("endDate") != null) {
                    String endDateStr = params.get("endDate").toString().trim();
                    if (!endDateStr.isEmpty()) {
                        endDateParam = endDateStr;
                    }
                }
            }
            final String startDate = startDateParam;
            final String endDate = endDateParam;
            
            // 6. 构建统计数据
            ShopStatisticsVO statisticsVO = new ShopStatisticsVO();
            
            // 从店铺基本信息中获取统计数据
            // 使用动态计算获取关注数
            statisticsVO.setFollowCount(statisticsUtils.getFollowCount("shop", shopId));
            statisticsVO.setRatingCount(shop.getRatingCount());
            statisticsVO.setRating(shop.getRating());
            
            // 从订单表查询销售统计数据（统计所有已支付的订单，status >= 1，包括待发货、待收货、已完成）
            // 先查询所有订单，然后过滤掉待付款订单（status=0）和已取消订单（status=4）
            List<Order> allOrders = orderMapper.selectByShopIdAndStatus(shopId, null);
            java.math.BigDecimal totalSales = java.math.BigDecimal.ZERO;
            int orderCount = 0;
            
            if (allOrders != null && !allOrders.isEmpty()) {
                // 过滤掉待付款订单（status=0）和已取消订单（status=4），并应用日期范围过滤
                List<Order> paidOrders = allOrders.stream()
                    .filter(order -> order.getStatus() != null && order.getStatus() >= 1 && order.getStatus() != 4)
                    .filter(order -> {
                        if (startDate != null && order.getCreateTime() != null) {
                            String orderDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(order.getCreateTime());
                            if (orderDate.compareTo(startDate) < 0) {
                                return false;
                            }
                        }
                        if (endDate != null && order.getCreateTime() != null) {
                            String orderDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(order.getCreateTime());
                            if (orderDate.compareTo(endDate) > 0) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .collect(java.util.stream.Collectors.toList());
                
                orderCount = paidOrders.size();
                for (Order order : paidOrders) {
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
            
            // 3. 解析分页参数（兼容空字符串）
            int page = 1;
            int size = 10;
            Integer status = null; // 状态筛选：null表示全部，-1也表示全部，0/1表示具体状态
            String keyword = null;
            Integer categoryId = null;
            
            if (params != null) {
                String pageStr = getTrimmed(params.get("page"));
                if (!pageStr.isEmpty()) {
                    page = Integer.parseInt(pageStr);
                }
                String sizeStr = getTrimmed(params.get("size"));
                if (!sizeStr.isEmpty()) {
                    size = Integer.parseInt(sizeStr);
                }
                // 处理状态筛选参数（参考管理员页面的逻辑：-1表示全部，null也表示全部，0/1表示具体状态）
                if (params.get("status") != null) {
                    String statusStr = params.get("status").toString().trim();
                    if (!statusStr.isEmpty() && !"-1".equals(statusStr)) {
                        status = Integer.parseInt(statusStr);
                    }
                    // 如果 statusStr 是 "-1" 或空字符串，status 保持为 null（表示全部）
                }
                // 处理关键词搜索
                if (params.get("keyword") != null) {
                    keyword = params.get("keyword").toString().trim();
                    if (keyword.isEmpty()) {
                        keyword = null;
                    }
                }
                // 处理分类筛选
                if (params.get("categoryId") != null) {
                    String categoryIdStr = params.get("categoryId").toString().trim();
                    if (!categoryIdStr.isEmpty()) {
                        categoryId = Integer.parseInt(categoryIdStr);
                    }
                }
            }
            
            // 4. 参数验证和默认值设置
            if (page < 1) {
                page = 1;
            }
            if (size < 1) {
                size = 10;
            }
            
            // 5. 构建查询参数Map（参考 TeaServiceImpl.getTeas 的逻辑）
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("shopId", shopId);
            queryMap.put("page", page);
            queryMap.put("pageSize", size);
            queryMap.put("offset", (page - 1) * size);
            // status 处理：-1 或 null 表示全部，其他值表示具体状态
            if (status != null && status != -1) {
                queryMap.put("status", status);
            } else {
                // status 为 null 或 -1 时，不设置 status，让 Mapper 返回全部状态
                queryMap.put("status", -1); // 使用 -1 表示全部状态
            }
            if (keyword != null && !keyword.isEmpty()) {
                queryMap.put("keyword", keyword);
            }
            if (categoryId != null) {
                queryMap.put("categoryId", categoryId);
            }
            // 店铺管理页面：按创建时间倒序排列（最新的在前）
            queryMap.put("sortBy", "createTime");
            queryMap.put("sortOrder", "desc");
            
            // 6. 查询茶叶列表（使用 selectTeasWithPage 支持状态筛选，参考管理员页面）
            List<Tea> teaList = teaMapper.selectTeasWithPage(queryMap);
            
            // 7. 查询总数（使用 countTeas 支持状态筛选）
            Long total = teaMapper.countTeas(queryMap);
            
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
        
        // 统一处理主图：数据库中存的是相对路径，这里转换为前端可直接访问的完整URL
        String mainImage = tea.getMainImage();
        if (mainImage != null && !mainImage.trim().isEmpty()) {
            if (mainImage.startsWith("http://") || mainImage.startsWith("https://")) {
                // 已经是完整URL（兼容历史/外链数据）
                teaVO.setMainImage(mainImage);
            } else {
                // 相对路径 -> 补全为 http://host[:port]/context/files/...
                teaVO.setMainImage(FileUploadUtils.generateAccessUrl(mainImage, baseUrl));
            }
        } else {
            teaVO.setMainImage(null);
        }
        
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
            tea.setId(generateTeaId());
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
            // 6.1 处理主图：根据 images 数组中的 is_main 字段选择主图（参考 TeaServiceImpl.addTea 的逻辑）
            String mainImageUrl = null;
            if (teaData.get("mainImage") != null) {
                mainImageUrl = teaData.get("mainImage").toString();
                tea.setMainImage(mainImageUrl);
            } else {
                // 如果没有直接传 mainImage，从 images 数组中根据 is_main 选择
                Object imagesObj = teaData.get("images");
                if (imagesObj instanceof List) {
                    @SuppressWarnings("rawtypes")
                    List rawImages = (List) imagesObj;
                    for (Object item : rawImages) {
                        if (item == null) {
                            continue;
                        }
                        String url = null;
                        Integer isMain = 0;
                        if (item instanceof String) {
                            url = ((String) item).trim();
                            if (mainImageUrl == null && !url.isEmpty()) {
                                mainImageUrl = url;
                            }
                        } else if (item instanceof java.util.Map) {
                            @SuppressWarnings("rawtypes")
                            java.util.Map map = (java.util.Map) item;
                            Object urlObj = map.get("url");
                            if (urlObj != null) {
                                url = urlObj.toString().trim();
                            }
                            Object isMainObj = map.get("is_main");
                            if (isMainObj != null) {
                                if (isMainObj instanceof Boolean) {
                                    isMain = ((Boolean) isMainObj) ? 1 : 0;
                                } else if (isMainObj instanceof Number) {
                                    isMain = ((Number) isMainObj).intValue();
                                } else if (isMainObj instanceof String) {
                                    isMain = "1".equals(isMainObj.toString()) || "true".equalsIgnoreCase(isMainObj.toString()) ? 1 : 0;
                                }
                            }
                            if (isMain == 1 && url != null && !url.isEmpty()) {
                                mainImageUrl = url;
                                break; // 找到主图就退出
                            }
                            if (mainImageUrl == null && url != null && !url.isEmpty()) {
                                mainImageUrl = url;
                            }
                        }
                    }
                    if (mainImageUrl != null && !mainImageUrl.isEmpty()) {
                        tea.setMainImage(mainImageUrl);
                    }
                }
            }
            
            // 设置默认值
            tea.setSales(0); // 默认销量为0
            if (teaData.get("status") != null) {
                tea.setStatus(Integer.parseInt(teaData.get("status").toString()));
            } else {
            tea.setStatus(1); // 默认上架
            }
            tea.setIsDeleted(0); // 未删除
            
        Date now = new Date();
            tea.setCreateTime(now);
            tea.setUpdateTime(now);
            
            // 7. 插入数据库（主表）
            int result = teaMapper.insert(tea);
            if (result <= 0) {
                logger.error("添加店铺茶叶失败: 数据库插入失败, shopId={}", shopId);
                return Result.failure(4108);
            }
            
            // 8. 同步图片表：如果有传图片URL列表，则全部写入tea_images，并根据mainImage设置主图（参考 TeaServiceImpl.addTea 的逻辑）
            // 注意：规格由前端单独添加，避免与前端逻辑重复（参考管理员页面的做法）
            try {
                List<TeaImage> imageEntities = new ArrayList<>();
                Date imgNow = now;
                String finalMainImageUrl = tea.getMainImage();
                
                Object imagesObjForTable = teaData.get("images");
                if (imagesObjForTable instanceof List && !((List<?>) imagesObjForTable).isEmpty()) {
                    @SuppressWarnings("rawtypes")
                    List rawImages = (List) imagesObjForTable;
                    int order = 1;
                    for (Object item : rawImages) {
                        String url = null;
                        if (item instanceof String) {
                            url = ((String) item).trim();
                        } else if (item instanceof java.util.Map) {
                            @SuppressWarnings("rawtypes")
                            java.util.Map map = (java.util.Map) item;
                            Object urlObj = map.get("url");
                            if (urlObj != null) {
                                url = urlObj.toString().trim();
                            }
                        }
                        // 只接受有效的URL路径
                        if (url == null || url.isEmpty()) {
                            continue;
                        }
                        TeaImage img = new TeaImage();
                        img.setTeaId(tea.getId());
                        img.setUrl(url);
                        img.setSortOrder(order++);
                        // 与主表mainImage对齐：如果等于主图URL，则标记为主图；否则0
                        img.setIsMain((finalMainImageUrl != null && !finalMainImageUrl.isEmpty() && finalMainImageUrl.equals(url)) ? 1 : 0);
                        img.setCreateTime(imgNow);
                        imageEntities.add(img);
                    }
                }
                
                if (!imageEntities.isEmpty()) {
                    for (TeaImage img : imageEntities) {
                        teaImageMapper.insert(img);
                    }
                    // 如果主表还没设置mainImage，但图片表里已经有图片，则用第一张做主图
                    if (tea.getMainImage() == null || tea.getMainImage().trim().isEmpty()) {
                        TeaImage first = imageEntities.get(0);
                        tea.setMainImage(first.getUrl());
                        teaMapper.updateById(tea);
                    }
                }
            } catch (Exception imgEx) {
                logger.error("添加店铺茶叶时写入图片信息失败, teaId: {}", tea.getId(), imgEx);
            }
            
            logger.info("添加店铺茶叶成功: teaId={}, teaName={}, shopId={}", 
                    tea.getId(), tea.getName(), shopId);
            
            // 10. 返回成功（根据code-message-mapping.md，成功码是4002）
            return Result.success(4002, tea.getId());
            
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
            
            // 6.1 处理主图：根据 images 数组中的 is_main 字段选择主图（完全参考 TeaServiceImpl.updateTea 的逻辑）
            String mainImageUrl = null;
            if (teaData.get("mainImage") != null) {
                mainImageUrl = teaData.get("mainImage").toString();
                tea.setMainImage(mainImageUrl);
                updated = true;
            } else {
                // 如果没有直接传 mainImage，从 images 数组中根据 is_main 选择
                Object imagesObj = teaData.get("images");
                if (imagesObj instanceof List) {
                    @SuppressWarnings("rawtypes")
                    List rawImages = (List) imagesObj;
                    for (Object item : rawImages) {
                        if (item == null) {
                            continue;
                        }
                        String url = null;
                        Integer isMain = 0;
                        if (item instanceof String) {
                            url = ((String) item).trim();
                            if (mainImageUrl == null && !url.isEmpty()) {
                                mainImageUrl = url;
                            }
                        } else if (item instanceof java.util.Map) {
                            @SuppressWarnings("rawtypes")
                            java.util.Map map = (java.util.Map) item;
                            Object urlObj = map.get("url");
                            if (urlObj != null) {
                                url = urlObj.toString().trim();
                            }
                            Object isMainObj = map.get("is_main");
                            if (isMainObj != null) {
                                if (isMainObj instanceof Boolean) {
                                    isMain = ((Boolean) isMainObj) ? 1 : 0;
                                } else if (isMainObj instanceof Number) {
                                    isMain = ((Number) isMainObj).intValue();
                                } else if (isMainObj instanceof String) {
                                    isMain = "1".equals(isMainObj.toString()) || "true".equalsIgnoreCase(isMainObj.toString()) ? 1 : 0;
                                }
                            }
                            if (isMain == 1 && url != null && !url.isEmpty()) {
                                mainImageUrl = url;
                                break;
                            }
                            if (mainImageUrl == null && url != null && !url.isEmpty()) {
                                mainImageUrl = url;
                            }
                        }
                    }
                    if (mainImageUrl != null && !mainImageUrl.isEmpty()) {
                        tea.setMainImage(mainImageUrl);
                        updated = true;
                    }
                }
            }
            
            if (teaData.get("status") != null) {
                tea.setStatus(Integer.valueOf(teaData.get("status").toString()));
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
            }
            
            Date now = new Date();
            
            // 8. 处理规格：根据前端传入的规格列表进行增删改（完全参考 TeaServiceImpl.updateTea 的逻辑）
            try {
                Object specificationsObj = teaData.get("specifications");
                List<Map<String, Object>> specsFromFrontend = null;
                if (specificationsObj instanceof List) {
                    specsFromFrontend = (List<Map<String, Object>>) specificationsObj;
                }
                
                // 获取现有规格
                List<TeaSpecification> existingSpecs = teaSpecificationMapper.selectByTeaId(teaId);
                Set<Integer> existingSpecIds = existingSpecs.stream()
                        .map(TeaSpecification::getId)
                        .collect(Collectors.toSet());
                
                List<TeaSpecification> specsToInsert = new ArrayList<>();
                List<TeaSpecification> specsToUpdate = new ArrayList<>();
                Set<Integer> specIdsToKeep = new HashSet<>();
                boolean hasDefault = false;
                
                if (specsFromFrontend != null && !specsFromFrontend.isEmpty()) {
                    for (Map<String, Object> specMap : specsFromFrontend) {
                        Integer specId = specMap.get("id") instanceof Number ? 
                                ((Number) specMap.get("id")).intValue() : null;
                        TeaSpecification spec = new TeaSpecification();
                        spec.setTeaId(teaId);
                        Object specNameObj = specMap.get("specName");
                        spec.setSpecName(specNameObj != null ? specNameObj.toString() : "默认规格");
                        Object priceObj = specMap.get("price");
                        spec.setPrice(priceObj != null ? new BigDecimal(priceObj.toString()) : tea.getPrice());
                        Object stockObj = specMap.get("stock");
                        spec.setStock(stockObj != null ? Integer.valueOf(stockObj.toString()) : tea.getStock());
                        Object isDefaultObj = specMap.get("isDefault");
                        Integer isDefault = 0;
                        if (isDefaultObj != null) {
                            if (isDefaultObj instanceof Boolean) {
                                isDefault = ((Boolean) isDefaultObj) ? 1 : 0;
                            } else if (isDefaultObj instanceof Number) {
                                isDefault = ((Number) isDefaultObj).intValue();
                            } else if (isDefaultObj instanceof String) {
                                isDefault = "1".equals(isDefaultObj.toString()) || "true".equalsIgnoreCase(isDefaultObj.toString()) ? 1 : 0;
                            }
                        }
                        spec.setIsDefault(isDefault);
                        if (isDefault == 1) hasDefault = true;
                        spec.setUpdateTime(now);
                        
                        if (specId != null && existingSpecIds.contains(specId)) {
                            // 更新已有规格
                            spec.setId(specId);
                            specsToUpdate.add(spec);
                            specIdsToKeep.add(specId);
            } else {
                            // 新增规格
                            spec.setCreateTime(now);
                            specsToInsert.add(spec);
                        }
                    }
                    // 如果前端没有指定默认规格，则将第一个规格设为默认
                    if (!hasDefault && !specsFromFrontend.isEmpty()) {
                        if (!specsToInsert.isEmpty()) {
                            specsToInsert.get(0).setIsDefault(1);
                            hasDefault = true;
                        } else if (!specsToUpdate.isEmpty()) {
                            specsToUpdate.get(0).setIsDefault(1);
                            hasDefault = true;
                        }
                    }
                } else {
                    // 如果前端没有提供规格，则添加一个默认规格
                    TeaSpecification defaultSpec = new TeaSpecification();
                    defaultSpec.setTeaId(teaId);
                    defaultSpec.setSpecName("默认规格");
                    defaultSpec.setPrice(tea.getPrice());
                    defaultSpec.setStock(tea.getStock());
                    defaultSpec.setIsDefault(1);
                    defaultSpec.setCreateTime(now);
                    defaultSpec.setUpdateTime(now);
                    specsToInsert.add(defaultSpec);
                    hasDefault = true;
                }
                
                // 如果有规格被设为默认，先清除该茶叶下所有规格的默认状态（确保只有一个默认规格）
                if (hasDefault) {
                    teaSpecificationMapper.clearDefaultByTeaId(String.valueOf(teaId));
                }
                
                // 执行规格更新操作
                if (!specsToInsert.isEmpty()) {
                    for (TeaSpecification spec : specsToInsert) {
                        teaSpecificationMapper.insert(spec);
                    }
                }
                for (TeaSpecification spec : specsToUpdate) {
                    teaSpecificationMapper.update(spec);
                }
                // 删除不再存在的规格
                for (TeaSpecification existingSpec : existingSpecs) {
                    if (!specIdsToKeep.contains(existingSpec.getId())) {
                        teaSpecificationMapper.deleteById(existingSpec.getId());
                    }
                }
            } catch (Exception specEx) {
                logger.error("更新店铺茶叶时处理规格信息失败, teaId: {}", teaId, specEx);
            }
            
            // 9. 处理图片：根据前端传入的图片列表进行增删改（完全参考 TeaServiceImpl.updateTea 的逻辑）
            try {
                Object imagesObjForUpdate = teaData.get("images");
                List<Map<String, Object>> imagesFromFrontend = null;
                if (imagesObjForUpdate instanceof List) {
                    imagesFromFrontend = (List<Map<String, Object>>) imagesObjForUpdate;
                }
                
                // 获取现有图片
                List<TeaImage> existingImages = teaImageMapper.selectByTeaId(teaId);
                Set<Integer> existingImageIds = existingImages.stream()
                        .map(TeaImage::getId)
                        .collect(Collectors.toSet());
                
                List<TeaImage> imagesToInsert = new ArrayList<>();
                List<TeaImage> imagesToUpdate = new ArrayList<>();
                Set<Integer> imageIdsToKeep = new HashSet<>();
                String newMainImageUrl = null;
                
                if (imagesFromFrontend != null && !imagesFromFrontend.isEmpty()) {
                    int order = 1;
                    for (Map<String, Object> imageMap : imagesFromFrontend) {
                        Integer imageId = imageMap.get("id") instanceof Number ? 
                                ((Number) imageMap.get("id")).intValue() : null;
                        Object urlObj = imageMap.get("url");
                        String imageUrl = urlObj != null ? urlObj.toString() : null;
                        if (imageUrl == null || imageUrl.trim().isEmpty()) {
                            continue;
                        }
                        Object isMainObj = imageMap.get("is_main");
                        Integer isMain = 0;
                        if (isMainObj != null) {
                            if (isMainObj instanceof Boolean) {
                                isMain = ((Boolean) isMainObj) ? 1 : 0;
                            } else if (isMainObj instanceof Number) {
                                isMain = ((Number) isMainObj).intValue();
                            } else if (isMainObj instanceof String) {
                                isMain = "1".equals(isMainObj.toString()) || "true".equalsIgnoreCase(isMainObj.toString()) ? 1 : 0;
                            }
                        }
                        
                        TeaImage img = new TeaImage();
                        img.setTeaId(teaId);
                        img.setUrl(imageUrl);
                        img.setSortOrder(order++);
                        img.setIsMain(isMain != null && isMain == 1 ? 1 : 0);
                        
                        if (img.getIsMain() == 1) {
                            newMainImageUrl = imageUrl;
                        }
                        
                        if (imageId != null && existingImageIds.contains(imageId)) {
                            // 更新已有图片
                            img.setId(imageId);
                            imagesToUpdate.add(img);
                            imageIdsToKeep.add(imageId);
                        } else {
                            // 新增图片
                            img.setCreateTime(now);
                            imagesToInsert.add(img);
                        }
                    }
                }
                
                // 执行图片更新操作
                if (!imagesToInsert.isEmpty()) {
                    for (TeaImage img : imagesToInsert) {
                        teaImageMapper.insert(img);
                    }
                }
                for (TeaImage img : imagesToUpdate) {
                    teaImageMapper.updateById(img);
                }
                // 删除不再存在的图片
                for (TeaImage existingImage : existingImages) {
                    if (!imageIdsToKeep.contains(existingImage.getId())) {
                        teaImageMapper.deleteById(existingImage.getId().longValue());
                    }
                }
                
                // 更新主表 main_image 字段
                if (newMainImageUrl != null && !newMainImageUrl.isEmpty()) {
                    tea.setMainImage(newMainImageUrl);
                    teaMapper.updateById(tea);
                } else if (imagesFromFrontend != null && !imagesFromFrontend.isEmpty() && imagesFromFrontend.get(0).containsKey("url")) {
                    // 如果没有明确指定主图，则将第一张图片设为主图
                    tea.setMainImage(imagesFromFrontend.get(0).get("url").toString());
                    teaMapper.updateById(tea);
                }
            } catch (Exception imgEx) {
                logger.error("更新店铺茶叶时处理图片信息失败, teaId: {}", teaId, imgEx);
            }
            
            logger.info("更新店铺茶叶成功: teaId={}, teaName={}", teaId, tea.getName());
            
            // 10. 返回成功（根据code-message-mapping.md，成功码是4003）
            return Result.success(4003);
            
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
        final boolean isUp = Integer.valueOf(1).equals(status);
        try {
            logger.info("茶叶上下架请求: teaId={}, status={}", teaId, status);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("茶叶上下架失败: 用户未登录");
                // 根据status返回不同的失败码
                return Result.failure(isUp ? 4111 : 4112);
            }
            
            // 2. 验证参数
            if (teaId == null || teaId.trim().isEmpty()) {
                logger.warn("茶叶上下架失败: 茶叶ID为空");
                return Result.failure(isUp ? 4111 : 4112);
            }
            
            if (status == null || (status != 0 && status != 1)) {
                logger.warn("茶叶上下架失败: 状态参数无效, status={}", status);
                return Result.failure(isUp ? 4111 : 4112);
            }
            
            // 3. 查询茶叶信息
            Tea tea = teaMapper.selectById(teaId);
            if (tea == null) {
                logger.warn("茶叶上下架失败: 茶叶不存在, teaId={}", teaId);
                return Result.failure(isUp ? 4111 : 4112);
            }
            
            // 4. 查询店铺信息并验证权限
            Shop shop = getShopById(tea.getShopId());
            if (shop == null) {
                logger.warn("茶叶上下架失败: 店铺不存在, shopId={}", tea.getShopId());
                return Result.failure(isUp ? 4111 : 4112);
            }
            
            // 5. 验证用户是否为店铺所有者
            if (!userId.equals(shop.getOwnerId())) {
                logger.warn("茶叶上下架失败: 无权限操作, userId={}, teaId={}, shopId={}, ownerId={}", 
                        userId, teaId, tea.getShopId(), shop.getOwnerId());
                return Result.failure(isUp ? 4111 : 4112);
            }
            
            // 6. 更新茶叶状态
            tea.setStatus(status);
            tea.setUpdateTime(new Date());
            int result = teaMapper.updateById(tea);
            if (result <= 0) {
                logger.error("茶叶上下架失败: 数据库更新失败, teaId={}", teaId);
                return Result.failure(isUp ? 4111 : 4112);
            }
            
            logger.info("茶叶上下架成功: teaId={}, teaName={}, status={}", teaId, tea.getName(), status);
            
            // 7. 返回成功（根据code-message-mapping.md，上架成功码是4005，下架成功码是4006）
            return Result.success(isUp ? 4005 : 4006, null);
            
        } catch (Exception e) {
            logger.error("茶叶上下架失败: 系统异常, teaId={}, status={}", teaId, status, e);
            return Result.failure(isUp ? 4111 : 4112);
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
        String imageUrl = banner.getImageUrl();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                bannerVO.setImageUrl(imageUrl);
            } else {
                bannerVO.setImageUrl(FileUploadUtils.generateAccessUrl(imageUrl, baseUrl));
            }
        } else {
            bannerVO.setImageUrl(null);
        }
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
            // 返回 code=4010，data=null
            return Result.success(4010);
            
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
            // 返回 code=4011，data=null
            return Result.success(4011);
            
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
            // 返回 code=4014，data=null
            return Result.success(4014);
            
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
            
            // 3. 查询当前用户对该店铺的评价（如果已登录）
            Integer userRating = null;
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId != null) {
                ShopReview userReview = shopReviewMapper.selectByShopIdAndUserId(shopId, currentUserId);
                if (userReview != null) {
                    userRating = userReview.getRating();
                }
            }
            
            // 4. 处理分页参数
            int page = 1;
            int size = 10;
            if (params != null) {
                Object pageObj = params.get("page");
                Object sizeObj = params.get("size");
                if (pageObj != null) {
                    try {
                        page = Integer.parseInt(pageObj.toString());
                    } catch (NumberFormatException ignored) {}
                }
                if (sizeObj != null) {
                    try {
                        size = Integer.parseInt(sizeObj.toString());
                    } catch (NumberFormatException ignored) {}
                }
            }
            if (page <= 0) page = 1;
            if (size <= 0) size = 10;
            int offset = (page - 1) * size;
            
            // 5. 查询店铺评价列表和总数
            List<ShopReview> reviewList = shopReviewMapper.selectByShopIdWithPage(shopId, offset, size);
            int total = shopReviewMapper.countByShopId(shopId);
            
            // 6. 将评价列表转换为 ReviewVO（带用户名/头像）
            List<ReviewVO> voList = new ArrayList<>();
            if (reviewList != null && !reviewList.isEmpty()) {
                for (ShopReview review : reviewList) {
                    if (review == null) {
                        continue;
                    }
                    ReviewVO vo = new ReviewVO();
                    vo.setId(review.getId());
                    vo.setUserId(review.getUserId());
                    vo.setContent(review.getContent());
                    vo.setRating(review.getRating());
                    vo.setCreateTime(review.getCreateTime());
                    // 店铺评价暂不支持图片/点赞/回复，统一设置默认值
                    vo.setImages(new ArrayList<>());
                    vo.setLikeCount(0);
                    vo.setIsLiked(false);
                    vo.setReply(null);
                    vo.setReplyTime(null);
                    vo.setIsAnonymous(0);
                    
                    // 补充用户信息
                    try {
                        com.shangnantea.model.entity.user.User user = userMapper.selectById(review.getUserId());
                        if (user != null) {
                            // 前台仅展示昵称；昵称已在业务层保证非空
                            vo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
                            String avatar = user.getAvatar();
                            if (avatar != null && !avatar.trim().isEmpty()) {
                                if (avatar.startsWith("http://") || avatar.startsWith("https://")) {
                                    vo.setAvatar(avatar);
                                } else {
                                    vo.setAvatar(FileUploadUtils.generateAccessUrl(avatar, baseUrl));
                                }
                            } else {
                                vo.setAvatar("");
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("查询店铺评价用户信息失败, reviewId={}, userId={}", review.getId(), review.getUserId(), e);
                    }
                    
                    voList.add(vo);
                }
            }
            
            // 7. 构建返回数据（评分统计依然从 shops 表读取，列表从 shop_reviews 表读取）
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("rating", shop.getRating()); // 店铺总平均评分（从shops表读取）
            responseData.put("ratingCount", shop.getRatingCount()); // 评分人数（从shops表读取）
            responseData.put("userRating", userRating); // 当前用户的评分（从shop_reviews表读取，如果已登录且已评价）
            responseData.put("list", voList);
            responseData.put("total", total);
            responseData.put("pageNum", page);
            responseData.put("pageSize", size);
            
            logger.info("获取店铺评分信息成功: shopId={}, rating={}, ratingCount={}, userRating={}, total={}", 
                    shopId, shop.getRating(), shop.getRatingCount(), userRating, total);
            
            // 5. 返回成功（根据code-message-mapping.md，成功码是200）
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
            
            String content = reviewData.get("content") != null ? reviewData.get("content").toString() : null;
            String orderId = reviewData.get("orderId") != null ? reviewData.get("orderId").toString() : null;
            
            // 6. 检查是否已有评价（覆盖逻辑）
            ShopReview existingReview = shopReviewMapper.selectByShopIdAndUserId(shopId, userId);
            boolean isNewReview = (existingReview == null);
            
            // 7. 插入或更新 shop_reviews 表
            Date now = new Date();
            if (isNewReview) {
                // 新评价：插入
                ShopReview newReview = new ShopReview();
                newReview.setShopId(shopId);
                newReview.setUserId(userId);
                newReview.setOrderId(orderId);
                newReview.setContent(content);
                newReview.setRating(rating);
                newReview.setCreateTime(now);
                newReview.setUpdateTime(now);
                
                int insertResult = shopReviewMapper.insert(newReview);
                if (insertResult <= 0) {
                    logger.error("提交评分失败: 插入评价记录失败, shopId={}, userId={}", shopId, userId);
                    return Result.failure(4132);
                }
            } else {
                // 覆盖评价：更新
                // existingReview 在 else 分支中一定不为 null（因为 isNewReview = (existingReview == null)）
                if (existingReview == null) {
                    logger.error("提交评分失败: 逻辑错误，existingReview 不应为 null");
                    return Result.failure(4132);
                }
                
                existingReview.setRating(rating);
                existingReview.setContent(content);
                if (orderId != null) {
                    existingReview.setOrderId(orderId);
                }
                existingReview.setUpdateTime(now);
                
                int updateResult = shopReviewMapper.updateById(existingReview);
                if (updateResult <= 0) {
                    logger.error("提交评分失败: 更新评价记录失败, reviewId={}", existingReview.getId());
                    return Result.failure(4132);
                }
            }
            
            // 8. 基于评价表重新统计店铺评分（防止手动改库导致统计错误）
            int newCount = shopReviewMapper.countByShopId(shopId);
            BigDecimal newRating;
            if (newCount <= 0) {
                newRating = BigDecimal.ZERO;
                newCount = 0;
            } else {
                Integer sumRating = shopReviewMapper.sumRatingByShopId(shopId);
                BigDecimal totalRating = new BigDecimal(sumRating != null ? sumRating : 0);
                newRating = totalRating.divide(new BigDecimal(newCount), 2, RoundingMode.HALF_UP);
            }
            
            shop.setRating(newRating);
            shop.setRatingCount(newCount);
            shop.setUpdateTime(now);
            
            int result = shopMapper.updateById(shop);
            if (result <= 0) {
                logger.error("提交评分失败: 更新店铺评分统计失败, shopId={}", shopId);
                return Result.failure(4132);
            }
            
            logger.info("提交评分成功: userId={}, shopId={}, rating={}, isNewReview={}, newAvgRating={}, newCount={}", 
                    userId, shopId, rating, isNewReview, newRating, newCount);
            
            // 9. 返回成功（根据code-message-mapping.md，成功码是4017，data 为空）
            return Result.success(4017);
            
        } catch (Exception e) {
            logger.error("提交评分失败: 系统异常, shopId={}", shopId, e);
            return Result.failure(4132);
        }
    }

    /**
     * 上传商家认证图片
     *
     * @param file 图片文件
     * @return 上传结果
     */
    @Override
    public Result<Map<String, Object>> uploadCertificationImage(MultipartFile file) {
        try {
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("上传商家认证图片失败: 用户未登录");
                return Result.failure(4101);
            }
            if (file == null || file.isEmpty()) {
                logger.warn("上传商家认证图片失败: 文件为空, userId={}", userId);
                return Result.failure(4101);
            }

            // 统一走工具类上传（type: shop-certification）
            String relativePath = FileUploadUtils.uploadImage(file, "shop-certification");
            String accessUrl = FileUploadUtils.generateAccessUrl(relativePath, baseUrl);

            Map<String, Object> data = new HashMap<>();
            data.put("url", accessUrl);
            data.put("path", relativePath);

            logger.info("上传商家认证图片成功: userId={}, path={}", userId, relativePath);
            return Result.success(200, data);
        } catch (Exception e) {
            logger.error("上传商家认证图片失败: 系统异常", e);
            return Result.failure(4101);
        }
    }
    
    /**
     * 生成店铺ID：SH + 8位数字
     * @return 店铺ID
     */
    private String generateShopId() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("SH");
        for (int i = 0; i < 8; i++) {
            sb.append(random.nextInt(10));
        }
        String shopId = sb.toString();
        // 检查ID是否已存在，存在则重新生成
        if (shopMapper.selectById(shopId) != null) {
            return generateShopId(); // 递归调用直到生成唯一ID
        }
        return shopId;
    }
    
    /**
     * 生成茶叶ID：TEA + 7位数字
     * 说明：数据库 teas.id 字段长度为10，因此必须是TEA开头+7位数字（例如 TEA0000001）
     * @return 茶叶ID
     */
    private String generateTeaId() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("TEA");
        for (int i = 0; i < 7; i++) {
            sb.append(random.nextInt(10));
        }
        String teaId = sb.toString();
        // 检查ID是否已存在，存在则重新生成
        if (teaMapper.selectById(teaId) != null) {
            return generateTeaId(); // 递归调用直到生成唯一ID
        }
        return teaId;
    }
} 