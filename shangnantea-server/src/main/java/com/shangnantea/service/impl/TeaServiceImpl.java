package com.shangnantea.service.impl;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.common.api.Result;
import com.shangnantea.mapper.OrderMapper;
import com.shangnantea.mapper.TeaCategoryMapper;
import com.shangnantea.mapper.TeaImageMapper;
import com.shangnantea.mapper.TeaMapper;
import com.shangnantea.mapper.TeaReviewMapper;
import com.shangnantea.mapper.TeaSpecificationMapper;
import com.shangnantea.mapper.ShopMapper;
import com.shangnantea.mapper.UserFavoriteMapper;
import com.shangnantea.mapper.UserLikeMapper;
import com.shangnantea.mapper.UserMapper;
import com.shangnantea.model.dto.tea.TeaQueryDTO;
import com.shangnantea.model.entity.order.Order;
import com.shangnantea.model.entity.tea.Tea;
import com.shangnantea.model.entity.tea.TeaCategory;
import com.shangnantea.model.entity.tea.TeaImage;
import com.shangnantea.model.entity.tea.TeaReview;
import com.shangnantea.model.entity.tea.TeaSpecification;
import com.shangnantea.model.entity.shop.Shop;
import com.shangnantea.model.entity.user.User;
import com.shangnantea.model.entity.user.UserFavorite;
import com.shangnantea.model.entity.user.UserLike;
import com.shangnantea.model.vo.TeaVO;
import com.shangnantea.model.vo.CategoryVO;
import com.shangnantea.model.vo.message.ReviewVO;
import com.shangnantea.model.vo.ReviewStatsVO;
import com.shangnantea.security.context.UserContext;
import com.shangnantea.service.TeaService;
import com.shangnantea.utils.FileUploadUtils;
import com.shangnantea.utils.StatisticsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
 * 茶叶服务实现类
 */
@Service
public class TeaServiceImpl implements TeaService {

    private static final Logger logger = LoggerFactory.getLogger(TeaServiceImpl.class);

    @Autowired
    private TeaMapper teaMapper;
    
    @Autowired
    private TeaCategoryMapper teaCategoryMapper;
    
    @Autowired
    private TeaSpecificationMapper teaSpecificationMapper;
    
    @Autowired
    private TeaImageMapper teaImageMapper;
    
    @Autowired
    private TeaReviewMapper teaReviewMapper;
    
    @Autowired
    private ShopMapper shopMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private UserFavoriteMapper userFavoriteMapper;
    
    @Autowired
    private UserLikeMapper userLikeMapper;
    
    @Autowired
    private StatisticsUtils statisticsUtils;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    /**
     * 删除茶叶（商家/管理员）
     * 路径: DELETE /tea/{id}
     * 成功码: 3003, 失败码: 3104
     */
    @Override
    public Result<Object> deleteTea(String id) {
        try {
            logger.info("删除茶叶请求, id: {}", id);
            
            // 1. 参数验证
            if (id == null || id.trim().isEmpty()) {
                logger.warn("删除茶叶失败: 茶叶ID不能为空");
                return Result.failure(3104);
            }
            
            // 2. 验证茶叶是否存在
            Tea tea = teaMapper.selectById(id);
            if (tea == null) {
                logger.warn("删除茶叶失败: 茶叶不存在, id: {}", id);
                return Result.failure(3104);
            }
            
            // 3. 验证茶叶是否已删除
            if (tea.getIsDeleted() != null && tea.getIsDeleted() == 1) {
                logger.warn("删除茶叶失败: 茶叶已被删除, id: {}", id);
                return Result.failure(3104);
            }
            
            // 4. 权限验证：验证茶叶是否属于当前用户的店铺
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                logger.warn("删除茶叶失败: 用户未登录");
                return Result.failure(3104);
            }
            
            // 查询店铺信息验证权限
            Shop shop = shopMapper.selectById(tea.getShopId());
            if (shop == null) {
                logger.warn("删除茶叶失败: 店铺不存在, shopId: {}", tea.getShopId());
                return Result.failure(3104);
            }
            if (!currentUserId.equals(shop.getOwnerId())) {
                logger.warn("删除茶叶失败: 无权限删除此茶叶, userId: {}, shopOwnerId: {}", 
                    currentUserId, shop.getOwnerId());
                return Result.failure(3104);
            }
            
            // 5. 执行软删除（调用TeaMapper.xml中的delete方法）
            int deleteCount = teaMapper.delete(id);
            if (deleteCount == 0) {
                logger.warn("删除茶叶失败: 数据库删除失败, id: {}", id);
                return Result.failure(3104);
            }
            
            logger.info("删除茶叶成功, id: {}, name: {}", id, tea.getName());
            return Result.success(3003, null);
            
        } catch (Exception e) {
            logger.error("删除茶叶失败: 系统异常, id: {}", id, e);
            return Result.failure(3104);
        }
    }
    
    /**
     * 获取推荐茶叶
     * 路径: GET /tea/recommend
     * 成功码: 200, 失败码: 3105
     */
    @Override
    public Result<Object> getRecommendTeas(Map<String, Object> params) {
        try {
            logger.info("获取推荐茶叶请求, params: {}", params);
            
            // 1. 提取参数
            String type = params.get("type") != null ? params.get("type").toString() : "random";
            String teaId = params.get("teaId") != null ? params.get("teaId").toString() : null;
            int count = params.get("count") != null ? 
                    Integer.parseInt(params.get("count").toString()) : 6;
            
            // 2. 验证count范围
            if (count <= 0) {
                count = 6;
            }
            if (count > 20) {
                count = 20; // 限制最大数量
            }
            
            List<Tea> recommendTeas = new ArrayList<>();
            
            // 3. 根据推荐类型查询
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("status", 1); // 只查询上架的茶叶
            queryMap.put("offset", 0);
            queryMap.put("pageSize", count);
            
            switch (type) {
                case "similar":
                    // 相似推荐：根据teaId查询同分类的茶叶
                    if (teaId != null && !teaId.trim().isEmpty()) {
                        Tea refTea = teaMapper.selectById(teaId);
                        if (refTea != null) {
                            queryMap.put("categoryId", refTea.getCategoryId());
                            queryMap.put("sortBy", "sales");
                            recommendTeas = teaMapper.selectTeasWithPage(queryMap);
                            // 移除参考茶叶本身
                            recommendTeas.removeIf(tea -> tea.getId().equals(teaId));
                        }
                    }
                    // 如果没有找到相似茶叶，降级为热门推荐
                    if (recommendTeas.isEmpty()) {
                        queryMap.remove("categoryId");
                        queryMap.put("sortBy", "sales");
                        recommendTeas = teaMapper.selectTeasWithPage(queryMap);
                    }
                    break;
                    
                case "popular":
                    // 热门推荐：按销量排序
                    queryMap.put("sortBy", "sales");
                    recommendTeas = teaMapper.selectTeasWithPage(queryMap);
                    break;
                    
                case "random":
                default:
                    // 随机推荐：先查询所有上架茶叶，然后随机选择
                    queryMap.put("pageSize", count * 3); // 查询更多数据用于随机
                    List<Tea> allTeas = teaMapper.selectTeasWithPage(queryMap);
                    
                    // 随机打乱并取前count个
                    if (!allTeas.isEmpty()) {
                        java.util.Collections.shuffle(allTeas);
                        recommendTeas = allTeas.subList(0, Math.min(count, allTeas.size()));
                    }
                    break;
            }
            
            // 4. 转换为简化的VO（只包含基本信息）
            List<Map<String, Object>> result = recommendTeas.stream()
                    .map(tea -> {
                        Map<String, Object> teaMap = new HashMap<>();
                        teaMap.put("id", tea.getId());
                        teaMap.put("name", tea.getName());
                        teaMap.put("price", tea.getPrice());
                        
                        // 处理图片列表
                        List<String> images = new ArrayList<>();
                        if (tea.getMainImage() != null && !tea.getMainImage().isEmpty()) {
                            images.add(tea.getMainImage());
                        }
                        teaMap.put("images", images);
                        
                        return teaMap;
                    })
                    .collect(Collectors.toList());
            
            logger.info("获取推荐茶叶成功, type: {}, count: {}, 实际返回: {}", type, count, result.size());
            return Result.success(200, result);
            
        } catch (NumberFormatException e) {
            logger.error("获取推荐茶叶失败: 参数格式错误", e);
            return Result.failure(3105);
        } catch (Exception e) {
            logger.error("获取推荐茶叶失败: 系统异常", e);
            return Result.failure(3105);
        }
    }
    
    /**
     * 获取茶叶列表
     * 路径: GET /tea/list
     * 成功码: 3000, 失败码: 3100
     *
     * @param params 查询参数（Map格式）
     * @return 茶叶列表分页结果
     */
    @Override
    public Result<Object> getTeas(Map<String, Object> params) {
        try {
            logger.info("获取茶叶列表请求, params: {}", params);
            
            // 1. 将Map参数转换为TeaQueryDTO
            TeaQueryDTO queryDTO = convertMapToQueryDTO(params);
            // 设置默认值
            if (queryDTO.getPage() == null || queryDTO.getPage() < 1) {
                queryDTO.setPage(1);
            }
            if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
                queryDTO.setPageSize(10);
            }
            
            // 2. 构建查询参数Map（用于Mapper）
            Map<String, Object> queryMap = buildQueryMap(queryDTO);
            
            // 3. 查询茶叶列表
            List<Tea> teaList = teaMapper.selectTeasWithPage(queryMap);
            
            // 4. 统计总数
            long total = teaMapper.countTeas(queryMap);
            
            // 5. 转换为VO
            List<TeaVO> teaVOList = teaList.stream()
                    .map(this::convertToTeaVO)
                    .collect(Collectors.toList());
            
            // 6. 构建返回数据（按照openapi格式：包含list, total, page, pageSize）
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("list", teaVOList);
            resultData.put("total", total);
            resultData.put("page", queryDTO.getPage());
            resultData.put("pageSize", queryDTO.getPageSize());
            
            logger.info("获取茶叶列表成功, 总数: {}, 当前页: {}, 每页: {}", total, queryDTO.getPage(), queryDTO.getPageSize());
            return Result.success(3000, resultData);
            
        } catch (Exception e) {
            logger.error("获取茶叶列表失败: 系统异常", e);
            return Result.failure(3100);
        }
    }
    
    /**
     * 添加茶叶
     * 路径: POST /tea/list
     * 成功码: 3001, 失败码: 3101
     *
     * @param teaData 茶叶数据（Map格式）
     * @return 添加结果，包含新创建的茶叶信息
     */
    @Override
    public Result<Object> addTea(Map<String, Object> teaData) {
        try {
            logger.info("添加茶叶请求, teaData: {}", teaData);
            
            // 1. 参数验证
            if (teaData == null || teaData.isEmpty()) {
                logger.warn("添加茶叶失败: 参数为空");
                return Result.failure(3101);
            }
            
            // 2. 提取并验证必填字段
            String name = (String) teaData.get("name");
            Integer categoryId = teaData.get("categoryId") != null ? 
                    Integer.valueOf(teaData.get("categoryId").toString()) : null;
            String shopId = (String) teaData.get("shopId");
            BigDecimal price = teaData.get("price") != null ? 
                    new BigDecimal(teaData.get("price").toString()) : null;
            Integer stock = teaData.get("stock") != null ? 
                    Integer.valueOf(teaData.get("stock").toString()) : null;
            Integer status = teaData.get("status") != null ? 
                    Integer.valueOf(teaData.get("status").toString()) : 1;
            
            // 验证必填字段
            if (name == null || name.trim().isEmpty()) {
                logger.warn("添加茶叶失败: 茶叶名称不能为空");
                return Result.failure(3101);
            }
            if (categoryId == null) {
                logger.warn("添加茶叶失败: 分类ID不能为空");
                return Result.failure(3101);
            }
            if (shopId == null || shopId.trim().isEmpty()) {
                logger.warn("添加茶叶失败: 商家ID不能为空");
                return Result.failure(3101);
            }
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                logger.warn("添加茶叶失败: 价格必须大于0");
                return Result.failure(3101);
            }
            if (stock == null || stock < 0) {
                logger.warn("添加茶叶失败: 库存不能为负数");
                return Result.failure(3101);
            }
            
            // 3. 权限验证：验证shopId是否属于当前用户
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                logger.warn("添加茶叶失败: 用户未登录");
                return Result.failure(3101);
            }
            
            // 查询店铺信息验证权限
            Shop shop = shopMapper.selectById(shopId);
            if (shop == null) {
                logger.warn("添加茶叶失败: 店铺不存在, shopId: {}", shopId);
                return Result.failure(3101);
            }
            if (!currentUserId.equals(shop.getOwnerId())) {
                logger.warn("添加茶叶失败: 无权限为此店铺添加茶叶, userId: {}, shopOwnerId: {}", 
                    currentUserId, shop.getOwnerId());
                return Result.failure(3101);
            }
            
            // 4. 创建Tea实体
            Tea tea = new Tea();
            tea.setId(UUID.randomUUID().toString().replace("-", "")); // 生成32位UUID
            tea.setName(name.trim());
            tea.setCategoryId(categoryId);
            tea.setShopId(shopId);
            tea.setPrice(price);
            tea.setStock(stock);
            tea.setStatus(status);
            tea.setSales(0); // 初始销量为0
            tea.setIsDeleted(0); // 未删除
            
            // 5. 设置可选字段
            if (teaData.get("description") != null) {
                tea.setDescription(teaData.get("description").toString());
            }
            if (teaData.get("detail") != null) {
                tea.setDetail(teaData.get("detail").toString());
            }
            if (teaData.get("origin") != null) {
                tea.setOrigin(teaData.get("origin").toString());
            }
            
            // 6. 处理图片：取第一张作为主图
            if (teaData.get("images") != null && teaData.get("images") instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> images = (List<String>) teaData.get("images");
                if (!images.isEmpty()) {
                    tea.setMainImage(images.get(0));
                }
            }
            
            // 7. 设置时间
            Date now = new Date();
            tea.setCreateTime(now);
            tea.setUpdateTime(now);
            
            // 8. 插入数据库
            int result = teaMapper.insert(tea);
            if (result <= 0) {
                logger.error("添加茶叶失败: 数据库插入失败");
                return Result.failure(3101);
            }
            
            // 9. 转换为VO返回
            TeaVO teaVO = convertToTeaVO(tea);
            
            logger.info("添加茶叶成功, teaId: {}, name: {}", tea.getId(), tea.getName());
            return Result.success(3001, teaVO);
            
        } catch (NumberFormatException e) {
            logger.error("添加茶叶失败: 数字格式错误", e);
            return Result.failure(3101);
        } catch (Exception e) {
            logger.error("添加茶叶失败: 系统异常", e);
            return Result.failure(3101);
        }
    }
    
    /**
     * 获取茶叶详情
     * 路径: GET /tea/{id}
     * 成功码: 200, 失败码: 3102
     *
     * @param id 茶叶ID
     * @return 茶叶详情信息
     */
    @Override
    public Result<Object> getTeaDetail(String id) {
        try {
            logger.info("获取茶叶详情请求, id: {}", id);
            
            // 1. 参数验证
            if (id == null || id.trim().isEmpty()) {
                logger.warn("获取茶叶详情失败: 茶叶ID不能为空");
                return Result.failure(3102);
            }
            
            // 2. 查询茶叶基本信息
            Tea tea = teaMapper.selectById(id);
            if (tea == null) {
                logger.warn("获取茶叶详情失败: 茶叶不存在, id: {}", id);
                return Result.failure(3102);
            }
            
            // 3. 检查是否已删除
            if (tea.getIsDeleted() != null && tea.getIsDeleted() == 1) {
                logger.warn("获取茶叶详情失败: 茶叶已删除, id: {}", id);
                return Result.failure(3102);
            }
            
            // 4. 转换为VO
            TeaVO teaVO = convertToTeaVO(tea);
            
            // 5. 查询规格列表
            List<TeaSpecification> specifications = teaSpecificationMapper.selectByTeaId(id);
            if (specifications != null && !specifications.isEmpty()) {
                List<TeaVO.TeaSpecificationVO> specVOList = specifications.stream()
                        .map(this::convertToSpecificationVO)
                        .collect(Collectors.toList());
                teaVO.setSpecifications(specVOList);
            } else {
                teaVO.setSpecifications(new ArrayList<>());
            }
            
            // 6. 查询图片列表
            List<TeaImage> images = teaImageMapper.selectByTeaId(id);
            if (images != null && !images.isEmpty()) {
                List<TeaVO.TeaImageVO> imageVOList = images.stream()
                        .map(this::convertToImageVO)
                        .collect(Collectors.toList());
                teaVO.setImages(imageVOList);
            } else {
                teaVO.setImages(new ArrayList<>());
            }
            
            // 7. 查询评价统计（从tea_reviews表查询真实数据）
            try {
                Map<String, Object> statsMap = teaReviewMapper.selectRatingStats(id);
                
                // 设置平均评分
                Object avgRatingObj = statsMap.get("avg_rating");
                if (avgRatingObj != null) {
                    teaVO.setRating(new BigDecimal(avgRatingObj.toString()));
                } else {
                    teaVO.setRating(BigDecimal.ZERO);
                }
                
                // 设置总评价数
                Object totalObj = statsMap.get("total");
                if (totalObj != null) {
                    teaVO.setReviewCount(Integer.parseInt(totalObj.toString()));
                } else {
                    teaVO.setReviewCount(0);
                }
                
                logger.debug("查询评价统计成功, teaId: {}, rating: {}, reviewCount: {}", 
                        id, teaVO.getRating(), teaVO.getReviewCount());
            } catch (Exception e) {
                logger.warn("查询评价统计失败, teaId: {}, 使用默认值", id, e);
                teaVO.setRating(BigDecimal.ZERO);
                teaVO.setReviewCount(0);
            }
            
            // 8. 查询当前用户是否收藏了该茶叶
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId != null) {
                try {
                    // 查询收藏记录
                    UserFavorite favorite = userFavoriteMapper.selectByUserIdAndItem(currentUserId, "tea", id);
                    teaVO.setIsFavorite(favorite != null);
                    
                    logger.debug("查询收藏状态成功, teaId: {}, userId: {}, isFavorite: {}", 
                            id, currentUserId, favorite != null);
                } catch (Exception e) {
                    logger.warn("查询收藏状态失败, teaId: {}, userId: {}, 默认设置为未收藏", 
                            id, currentUserId, e);
                    teaVO.setIsFavorite(false);
                }
            } else {
                // 未登录用户默认未收藏
                teaVO.setIsFavorite(false);
            }
            
            logger.info("获取茶叶详情成功, id: {}, name: {}", tea.getId(), tea.getName());
            return Result.success(200, teaVO);
            
        } catch (Exception e) {
            logger.error("获取茶叶详情失败: 系统异常, id: {}", id, e);
            return Result.failure(3102);
        }
    }
    
    /**
     * 更新茶叶信息
     * 路径: PUT /tea/{id}
     * 成功码: 3002, 失败码: 3103
     */
    @Override
    public Result<Object> updateTea(String id, Map<String, Object> teaData) {
        logger.info("开始更新茶叶, id: {}, teaData: {}", id, teaData);
        
        try {
            // 1. 参数验证
            if (id == null || id.trim().isEmpty()) {
                logger.warn("更新茶叶失败: 茶叶ID不能为空");
                return Result.failure(3103);
            }
            if (teaData == null || teaData.isEmpty()) {
                logger.warn("更新茶叶失败: 更新数据不能为空");
                return Result.failure(3103);
            }
            
            // 2. 验证茶叶是否存在
            Tea existingTea = teaMapper.selectById(id);
            if (existingTea == null) {
                logger.warn("更新茶叶失败: 茶叶不存在, id: {}", id);
                return Result.failure(3103);
            }
            
            // 3. 验证茶叶是否已删除
            if (existingTea.getIsDeleted() != null && existingTea.getIsDeleted() == 1) {
                logger.warn("更新茶叶失败: 茶叶已被删除, id: {}", id);
                return Result.failure(3103);
            }
            
            // 4. 权限验证：验证茶叶是否属于当前用户的店铺
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                logger.warn("更新茶叶失败: 用户未登录");
                return Result.failure(3103);
            }
            
            // 查询店铺信息验证权限
            Shop shop = shopMapper.selectById(existingTea.getShopId());
            if (shop == null) {
                logger.warn("更新茶叶失败: 店铺不存在, shopId: {}", existingTea.getShopId());
                return Result.failure(3103);
            }
            if (!currentUserId.equals(shop.getOwnerId())) {
                logger.warn("更新茶叶失败: 无权限更新此茶叶, userId: {}, shopOwnerId: {}", 
                    currentUserId, shop.getOwnerId());
                return Result.failure(3103);
            }
            
            // 5. 提取并验证更新字段
            String name = teaData.get("name") != null ? teaData.get("name").toString() : null;
            Integer categoryId = teaData.get("categoryId") != null ? 
                    Integer.valueOf(teaData.get("categoryId").toString()) : null;
            BigDecimal price = teaData.get("price") != null ? 
                    new BigDecimal(teaData.get("price").toString()) : null;
            Integer stock = teaData.get("stock") != null ? 
                    Integer.valueOf(teaData.get("stock").toString()) : null;
            
            // 验证必填字段
            if (name != null && name.trim().isEmpty()) {
                logger.warn("更新茶叶失败: 茶叶名称不能为空");
                return Result.failure(3103);
            }
            if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
                logger.warn("更新茶叶失败: 价格必须大于0");
                return Result.failure(3103);
            }
            if (stock != null && stock < 0) {
                logger.warn("更新茶叶失败: 库存不能为负数");
                return Result.failure(3103);
            }
            
            // 6. 更新Tea实体
            Tea tea = new Tea();
            tea.setId(id);
            
            if (name != null) {
                tea.setName(name.trim());
            }
            if (categoryId != null) {
                tea.setCategoryId(categoryId);
            }
            if (price != null) {
                tea.setPrice(price);
            }
            if (stock != null) {
                tea.setStock(stock);
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
            if (teaData.get("mainImage") != null) {
                tea.setMainImage(teaData.get("mainImage").toString());
            }
            if (teaData.get("status") != null) {
                tea.setStatus(Integer.valueOf(teaData.get("status").toString()));
            }
            
            // 7. 执行更新
            int updateCount = teaMapper.updateById(tea);
            if (updateCount == 0) {
                logger.warn("更新茶叶失败: 数据库更新失败, id: {}", id);
                return Result.failure(3103);
            }
            
            // 8. 查询更新后的茶叶信息并转换为VO返回
            Tea updatedTea = teaMapper.selectById(id);
            TeaVO teaVO = convertToTeaVO(updatedTea);
            
            // 查询规格列表
            List<TeaSpecification> specifications = teaSpecificationMapper.selectByTeaId(id);
            List<TeaVO.TeaSpecificationVO> specificationVOs = specifications.stream()
                    .map(this::convertToSpecificationVO)
                    .collect(Collectors.toList());
            teaVO.setSpecifications(specificationVOs);
            
            // 查询图片列表
            List<TeaImage> images = teaImageMapper.selectByTeaId(id);
            List<TeaVO.TeaImageVO> imageVOs = images.stream()
                    .map(this::convertToImageVO)
                    .collect(Collectors.toList());
            teaVO.setImages(imageVOs);
            
            logger.info("更新茶叶成功, id: {}, name: {}", id, updatedTea.getName());
            return Result.success(3002, teaVO);
            
        } catch (NumberFormatException e) {
            logger.error("更新茶叶失败: 数据格式错误, id: {}", id, e);
            return Result.failure(3103);
        } catch (Exception e) {
            logger.error("更新茶叶失败: 系统异常, id: {}", id, e);
            return Result.failure(3103);
        }
    }
    
    /**
     * 将Map参数转换为TeaQueryDTO
     */
    private TeaQueryDTO convertMapToQueryDTO(Map<String, Object> params) {
        TeaQueryDTO dto = new TeaQueryDTO();
        
        if (params.get("page") != null) {
            dto.setPage(Integer.valueOf(params.get("page").toString()));
        }
        if (params.get("pageSize") != null) {
            dto.setPageSize(Integer.valueOf(params.get("pageSize").toString()));
        }
        if (params.get("categoryId") != null) {
            dto.setCategoryId(Integer.valueOf(params.get("categoryId").toString()));
        }
        if (params.get("keyword") != null) {
            dto.setKeyword(params.get("keyword").toString());
        }
        if (params.get("shopId") != null) {
            dto.setShopId(params.get("shopId").toString());
        }
        if (params.get("sortBy") != null) {
            dto.setSortBy(params.get("sortBy").toString());
        }
        if (params.get("sortOrder") != null) {
            dto.setSortOrder(params.get("sortOrder").toString());
        }
        if (params.get("priceMin") != null) {
            dto.setPriceMin(new BigDecimal(params.get("priceMin").toString()));
        }
        if (params.get("priceMax") != null) {
            dto.setPriceMax(new BigDecimal(params.get("priceMax").toString()));
        }
        if (params.get("origin") != null) {
            dto.setOrigin(params.get("origin").toString());
        }
        if (params.get("rating") != null) {
            dto.setRating(new BigDecimal(params.get("rating").toString()));
        }
        if (params.get("status") != null) {
            dto.setStatus(Integer.valueOf(params.get("status").toString()));
        }
        
        return dto;
    }
    
    /**
     * 构建查询参数Map（用于Mapper）
     */
    private Map<String, Object> buildQueryMap(TeaQueryDTO queryDTO) {
        Map<String, Object> queryMap = new HashMap<>();
        
        queryMap.put("status", queryDTO.getStatus());
        queryMap.put("keyword", queryDTO.getKeyword());
        queryMap.put("categoryId", queryDTO.getCategoryId());
        queryMap.put("shopId", queryDTO.getShopId());
        queryMap.put("priceMin", queryDTO.getPriceMin());
        queryMap.put("priceMax", queryDTO.getPriceMax());
        queryMap.put("origin", queryDTO.getOrigin());
        queryMap.put("sortBy", queryDTO.getSortBy());
        queryMap.put("sortOrder", queryDTO.getSortOrder());
        queryMap.put("offset", queryDTO.getOffset());
        queryMap.put("pageSize", queryDTO.getPageSize());
        
        return queryMap;
    }
    
    /**
     * 将Tea实体转换为TeaVO
     */
    private TeaVO convertToTeaVO(Tea tea) {
        if (tea == null) {
            return null;
        }
        
        TeaVO vo = new TeaVO();
        BeanUtils.copyProperties(tea, vo);
        
        // 设置主图（如果mainImage为空，尝试从images获取）
        if (vo.getMainImage() == null || vo.getMainImage().isEmpty()) {
            // 这里可以后续从tea_images表查询，暂时使用main_image字段
        }
        
        // 设置图片列表（暂时为空，后续可以从tea_images表查询）
        vo.setImages(new ArrayList<>());
        
        // 设置规格列表（暂时为空，后续可以从tea_specifications表查询）
        vo.setSpecifications(new ArrayList<>());
        
        return vo;
    }
    
    /**
     * 将TeaSpecification实体转换为TeaSpecificationVO
     */
    private TeaVO.TeaSpecificationVO convertToSpecificationVO(TeaSpecification spec) {
        if (spec == null) {
            return null;
        }
        
        TeaVO.TeaSpecificationVO vo = new TeaVO.TeaSpecificationVO();
        vo.setId(spec.getId());
        vo.setSpecName(spec.getSpecName());
        vo.setPrice(spec.getPrice());
        vo.setStock(spec.getStock());
        vo.setIsDefault(spec.getIsDefault());
        
        return vo;
    }
    
    /**
     * 将TeaImage实体转换为TeaImageVO
     */
    private TeaVO.TeaImageVO convertToImageVO(TeaImage image) {
        if (image == null) {
            return null;
        }
        
        TeaVO.TeaImageVO vo = new TeaVO.TeaImageVO();
        vo.setId(image.getId());
        vo.setUrl(image.getUrl());
        vo.setSortOrder(image.getSortOrder());
        vo.setIsMain(image.getIsMain());
        
        return vo;
    }
    
    @Override
    public Result<Object> uploadTeaImages(String teaId, MultipartFile[] files) {
        try {
            logger.info("上传茶叶图片请求, teaId: {}, 文件数量: {}", teaId, files.length);
            
            // 1. 参数验证
            if (teaId == null || teaId.trim().isEmpty()) {
                logger.warn("上传茶叶图片失败: 茶叶ID不能为空");
                return Result.failure(3120);
            }
            if (files == null || files.length == 0) {
                logger.warn("上传茶叶图片失败: 文件列表为空");
                return Result.failure(3120);
            }
            
            // 2. 验证茶叶是否存在
            Tea tea = teaMapper.selectById(teaId);
            if (tea == null) {
                logger.warn("上传茶叶图片失败: 茶叶不存在, teaId: {}", teaId);
                return Result.failure(3121);
            }
            
            // 3. 验证茶叶是否已删除
            if (tea.getIsDeleted() != null && tea.getIsDeleted() == 1) {
                logger.warn("上传茶叶图片失败: 茶叶已删除, teaId: {}", teaId);
                return Result.failure(3121);
            }
            
            // 4. 权限验证：验证茶叶是否属于当前用户的店铺
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                logger.warn("上传茶叶图片失败: 用户未登录");
                return Result.failure(3120);
            }
            
            Shop shop = shopMapper.selectById(tea.getShopId());
            if (shop == null) {
                logger.warn("上传茶叶图片失败: 店铺不存在, shopId: {}", tea.getShopId());
                return Result.failure(3120);
            }
            if (!currentUserId.equals(shop.getOwnerId())) {
                logger.warn("上传茶叶图片失败: 无权限上传图片, userId: {}, shopOwnerId: {}", 
                        currentUserId, shop.getOwnerId());
                return Result.failure(3120);
            }
            
            // 5. 批量上传图片并存储到tea_images表
            List<Map<String, Object>> uploadedImages = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }
                
                // 调用工具类上传（硬编码type为"teas"）
                String relativePath = FileUploadUtils.uploadImage(file, "teas");
                
                // 生成访问URL
                String accessUrl = FileUploadUtils.generateAccessUrl(relativePath, baseUrl);
                
                // 创建茶叶图片记录
                TeaImage teaImage = new TeaImage();
                teaImage.setTeaId(teaId);
                teaImage.setUrl(relativePath); // 存储相对路径
                teaImage.setCreateTime(new Date());
                
                // 保存到数据库
                teaImageMapper.insert(teaImage);
                
                // 添加到返回列表
                Map<String, Object> imageInfo = new HashMap<>();
                imageInfo.put("id", teaImage.getId());
                imageInfo.put("url", relativePath);
                imageInfo.put("accessUrl", accessUrl);
                uploadedImages.add(imageInfo);
                
                logger.info("茶叶图片上传成功: teaId: {}, imageId: {}, path: {}", teaId, teaImage.getId(), relativePath);
            }
            
            // 6. 返回上传的图片列表
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("images", uploadedImages);
            resultData.put("count", uploadedImages.size());
            
            logger.info("批量上传茶叶图片成功, teaId: {}, 成功数量: {}", teaId, uploadedImages.size());
            return Result.success(3014, resultData);
            
        } catch (Exception e) {
            logger.error("上传茶叶图片失败: 系统异常, teaId: {}", teaId, e);
            return Result.failure(3120);
        }
    }
    
    /**
     * 获取茶叶分类列表
     * 路径: GET /tea/categories
     * 成功码: 200, 失败码: 3106
     */
    @Override
    public Result<Object> getTeaCategories() {
        try {
            logger.info("获取茶叶分类列表请求");
            
            // 1. 查询所有分类
            List<TeaCategory> categories = teaCategoryMapper.selectAll();
            
            // 2. 转换为VO
            List<CategoryVO> categoryVOList = categories.stream()
                    .map(this::convertToCategoryVO)
                    .collect(Collectors.toList());
            
            logger.info("获取茶叶分类列表成功, 数量: {}", categoryVOList.size());
            return Result.success(200, categoryVOList);
            
        } catch (Exception e) {
            logger.error("获取茶叶分类列表失败: 系统异常", e);
            return Result.failure(3106);
        }
    }
    
    /**
     * 创建茶叶分类（管理员）
     * 路径: POST /tea/categories
     * 成功码: 3004, 失败码: 3107
     */
    @Override
    public Result<Object> createCategory(Map<String, Object> categoryData) {
        try {
            logger.info("创建茶叶分类请求, categoryData: {}", categoryData);
            
            // 1. 参数验证
            if (categoryData == null || categoryData.isEmpty()) {
                logger.warn("创建茶叶分类失败: 参数为空");
                return Result.failure(3107);
            }
            
            // 2. 提取并验证必填字段
            String name = categoryData.get("name") != null ? categoryData.get("name").toString() : null;
            if (name == null || name.trim().isEmpty()) {
                logger.warn("创建茶叶分类失败: 分类名称不能为空");
                return Result.failure(3107);
            }
            
            // 3. 验证分类名称是否重复
            List<TeaCategory> existingCategories = teaCategoryMapper.selectAll();
            boolean nameExists = existingCategories.stream()
                    .anyMatch(cat -> name.trim().equals(cat.getName()));
            if (nameExists) {
                logger.warn("创建茶叶分类失败: 分类名称已存在, name: {}", name);
                return Result.failure(3107);
            }
            
            // 4. 创建TeaCategory实体
            TeaCategory category = new TeaCategory();
            category.setName(name.trim());
            
            // 5. 设置可选字段
            if (categoryData.get("parentId") != null) {
                category.setParentId(Integer.valueOf(categoryData.get("parentId").toString()));
            } else {
                category.setParentId(0); // 默认为顶级分类
            }
            
            if (categoryData.get("sortOrder") != null) {
                category.setSortOrder(Integer.valueOf(categoryData.get("sortOrder").toString()));
            } else {
                category.setSortOrder(0); // 默认排序为0
            }
            
            if (categoryData.get("icon") != null) {
                category.setIcon(categoryData.get("icon").toString());
            }
            
            // 6. 设置时间
            Date now = new Date();
            category.setCreateTime(now);
            category.setUpdateTime(now);
            
            // 7. 插入数据库
            int result = teaCategoryMapper.insert(category);
            if (result <= 0) {
                logger.error("创建茶叶分类失败: 数据库插入失败");
                return Result.failure(3107);
            }
            
            // 8. 转换为VO返回
            CategoryVO categoryVO = convertToCategoryVO(category);
            
            logger.info("创建茶叶分类成功, id: {}, name: {}", category.getId(), category.getName());
            return Result.success(3004, categoryVO);
            
        } catch (NumberFormatException e) {
            logger.error("创建茶叶分类失败: 数字格式错误", e);
            return Result.failure(3107);
        } catch (Exception e) {
            logger.error("创建茶叶分类失败: 系统异常", e);
            return Result.failure(3107);
        }
    }
    
    /**
     * 更新茶叶分类（管理员）
     * 路径: PUT /tea/categories/{id}
     * 成功码: 3005, 失败码: 3108
     */
    @Override
    public Result<Object> updateCategory(Integer id, Map<String, Object> categoryData) {
        try {
            logger.info("更新茶叶分类请求, id: {}, categoryData: {}", id, categoryData);
            
            // 1. 参数验证
            if (id == null) {
                logger.warn("更新茶叶分类失败: 分类ID不能为空");
                return Result.failure(3108);
            }
            if (categoryData == null || categoryData.isEmpty()) {
                logger.warn("更新茶叶分类失败: 更新数据不能为空");
                return Result.failure(3108);
            }
            
            // 2. 验证分类是否存在
            TeaCategory existingCategory = teaCategoryMapper.selectById(id);
            if (existingCategory == null) {
                logger.warn("更新茶叶分类失败: 分类不存在, id: {}", id);
                return Result.failure(3108);
            }
            
            // 3. 提取更新字段
            String name = categoryData.get("name") != null ? categoryData.get("name").toString() : null;
            
            // 4. 验证分类名称是否重复（排除自身）
            if (name != null && !name.trim().isEmpty()) {
                List<TeaCategory> allCategories = teaCategoryMapper.selectAll();
                boolean nameExists = allCategories.stream()
                        .anyMatch(cat -> !cat.getId().equals(id) && name.trim().equals(cat.getName()));
                if (nameExists) {
                    logger.warn("更新茶叶分类失败: 分类名称已存在, name: {}", name);
                    return Result.failure(3108);
                }
            }
            
            // 5. 更新TeaCategory实体
            TeaCategory category = new TeaCategory();
            category.setId(id);
            
            if (name != null && !name.trim().isEmpty()) {
                category.setName(name.trim());
            }
            if (categoryData.get("parentId") != null) {
                category.setParentId(Integer.valueOf(categoryData.get("parentId").toString()));
            }
            if (categoryData.get("sortOrder") != null) {
                category.setSortOrder(Integer.valueOf(categoryData.get("sortOrder").toString()));
            }
            if (categoryData.get("icon") != null) {
                category.setIcon(categoryData.get("icon").toString());
            }
            
            // 6. 执行更新
            int updateCount = teaCategoryMapper.updateById(category);
            if (updateCount == 0) {
                logger.warn("更新茶叶分类失败: 数据库更新失败, id: {}", id);
                return Result.failure(3108);
            }
            
            // 7. 查询更新后的分类信息并转换为VO返回
            TeaCategory updatedCategory = teaCategoryMapper.selectById(id);
            CategoryVO categoryVO = convertToCategoryVO(updatedCategory);
            
            logger.info("更新茶叶分类成功, id: {}, name: {}", id, updatedCategory.getName());
            return Result.success(3005, categoryVO);
            
        } catch (NumberFormatException e) {
            logger.error("更新茶叶分类失败: 数据格式错误, id: {}", id, e);
            return Result.failure(3108);
        } catch (Exception e) {
            logger.error("更新茶叶分类失败: 系统异常, id: {}", id, e);
            return Result.failure(3108);
        }
    }
    
    /**
     * 将TeaCategory实体转换为CategoryVO
     */
    private CategoryVO convertToCategoryVO(TeaCategory category) {
        if (category == null) {
            return null;
        }
        
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setParentId(category.getParentId());
        vo.setSortOrder(category.getSortOrder());
        vo.setIcon(category.getIcon());
        
        return vo;
    }
    
    /**
     * 删除茶叶分类（管理员）
     * 路径: DELETE /tea/categories/{id}
     * 成功码: 3006, 失败码: 3109
     */
    @Override
    public Result<Boolean> deleteCategory(Integer id) {
        try {
            logger.info("删除茶叶分类请求, id: {}", id);
            
            // 1. 参数验证
            if (id == null) {
                logger.warn("删除茶叶分类失败: 分类ID不能为空");
                return Result.failure(3109);
            }
            
            // 2. 验证分类是否存在
            TeaCategory category = teaCategoryMapper.selectById(id);
            if (category == null) {
                logger.warn("删除茶叶分类失败: 分类不存在, id: {}", id);
                return Result.failure(3109);
            }
            
            // 3. 检查分类下是否有茶叶
            int teaCount = teaMapper.countByCategory(id);
            if (teaCount > 0) {
                logger.warn("删除茶叶分类失败: 分类下还有{}个茶叶，不允许删除, id: {}", teaCount, id);
                return Result.failure(3109);
            }
            
            // 4. 执行删除
            int deleteCount = teaCategoryMapper.deleteById(id);
            if (deleteCount == 0) {
                logger.warn("删除茶叶分类失败: 数据库删除失败, id: {}", id);
                return Result.failure(3109);
            }
            
            logger.info("删除茶叶分类成功, id: {}, name: {}", id, category.getName());
            return Result.success(3006, true);
            
        } catch (Exception e) {
            logger.error("删除茶叶分类失败: 系统异常, id: {}", id, e);
            return Result.failure(3109);
        }
    }
    
    /**
     * 获取茶叶评价列表
     * 路径: GET /tea/{teaId}/reviews
     * 成功码: 200, 失败码: 3110
     */
    @Override
    public Result<Object> getTeaReviews(String teaId, Map<String, Object> params) {
        try {
            logger.info("获取茶叶评价列表请求, teaId: {}, params: {}", teaId, params);
            
            // 1. 参数验证
            if (teaId == null || teaId.trim().isEmpty()) {
                logger.warn("获取茶叶评价列表失败: 茶叶ID不能为空");
                return Result.failure(3110);
            }
            
            // 2. 验证茶叶是否存在
            Tea tea = teaMapper.selectById(teaId);
            if (tea == null) {
                logger.warn("获取茶叶评价列表失败: 茶叶不存在, teaId: {}", teaId);
                return Result.failure(3110);
            }
            
            // 3. 提取分页参数
            int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
            int pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 10;
            
            // 验证分页参数
            if (page < 1) page = 1;
            if (pageSize < 1) pageSize = 10;
            if (pageSize > 100) pageSize = 100; // 限制最大每页数量
            
            int offset = (page - 1) * pageSize;
            
            // 4. 构建查询参数
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("teaId", teaId);
            queryMap.put("offset", offset);
            queryMap.put("limit", pageSize);
            
            // 5. 查询评价列表
            List<TeaReview> reviews = teaReviewMapper.selectByTeaIdWithPage(queryMap);
            
            // 6. 统计总数
            int total = teaReviewMapper.countByTeaId(teaId);
            
            // 7. 批量查询用户信息（优化N+1查询问题）
            Map<String, com.shangnantea.model.entity.user.User> userCache = new HashMap<>();
            for (TeaReview review : reviews) {
                if (!userCache.containsKey(review.getUserId())) {
                    try {
                        com.shangnantea.model.entity.user.User user = userMapper.selectById(review.getUserId());
                        if (user != null) {
                            userCache.put(review.getUserId(), user);
                        }
                    } catch (Exception e) {
                        logger.warn("批量查询用户信息失败, userId: {}", review.getUserId(), e);
                    }
                }
            }
            
            // 8. 批量查询当前用户的点赞记录（优化N+1查询）
            String currentUserId = UserContext.getCurrentUserId();
            Map<String, Boolean> likeMap = new HashMap<>();
            if (currentUserId != null && !reviews.isEmpty()) {
                for (TeaReview review : reviews) {
                    try {
                        UserLike like = userLikeMapper.selectByUserIdAndTarget(
                                currentUserId, "review", String.valueOf(review.getId()));
                        likeMap.put(String.valueOf(review.getId()), like != null);
                    } catch (Exception e) {
                        logger.warn("查询评价点赞状态失败, reviewId: {}, userId: {}, 默认设置为未点赞", 
                                review.getId(), currentUserId, e);
                        likeMap.put(String.valueOf(review.getId()), false);
                    }
                }
            }
            
            // 9. 转换为VO（使用缓存的用户信息和点赞状态）
            final Map<String, Boolean> finalLikeMap = likeMap;
            List<ReviewVO> reviewVOList = reviews.stream()
                    .map(review -> {
                        ReviewVO vo = convertToReviewVO(review, userCache);
                        // 设置点赞状态
                        if (vo != null) {
                            vo.setIsLiked(finalLikeMap.getOrDefault(String.valueOf(review.getId()), false));
                        }
                        return vo;
                    })
                    .collect(Collectors.toList());
            
            // 10. 构建返回数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("list", reviewVOList);
            resultData.put("total", total);
            resultData.put("page", page);
            resultData.put("pageSize", pageSize);
            
            logger.info("获取茶叶评价列表成功, teaId: {}, 总数: {}, 当前页: {}", teaId, total, page);
            return Result.success(200, resultData);
            
        } catch (NumberFormatException e) {
            logger.error("获取茶叶评价列表失败: 参数格式错误, teaId: {}", teaId, e);
            return Result.failure(3110);
        } catch (Exception e) {
            logger.error("获取茶叶评价列表失败: 系统异常, teaId: {}", teaId, e);
            return Result.failure(3110);
        }
    }
    
    /**
     * 获取茶叶评价统计
     * 路径: GET /tea/{teaId}/reviews/stats
     * 成功码: 200, 失败码: 3111
     */
    @Override
    public Result<Object> getReviewStats(String teaId) {
        try {
            logger.info("获取茶叶评价统计请求, teaId: {}", teaId);
            
            // 1. 参数验证
            if (teaId == null || teaId.trim().isEmpty()) {
                logger.warn("获取茶叶评价统计失败: 茶叶ID不能为空");
                return Result.failure(3111);
            }
            
            // 2. 验证茶叶是否存在
            Tea tea = teaMapper.selectById(teaId);
            if (tea == null) {
                logger.warn("获取茶叶评价统计失败: 茶叶不存在, teaId: {}", teaId);
                return Result.failure(3111);
            }
            
            // 3. 查询评价统计数据
            Map<String, Object> statsMap = teaReviewMapper.selectRatingStats(teaId);
            
            // 4. 构建ReviewStatsVO
            ReviewStatsVO statsVO = new ReviewStatsVO();
            
            // 设置平均评分
            Object avgRatingObj = statsMap.get("avg_rating");
            if (avgRatingObj != null) {
                statsVO.setAverageRating(new BigDecimal(avgRatingObj.toString()));
            } else {
                statsVO.setAverageRating(BigDecimal.ZERO);
            }
            
            // 设置总评价数
            Object totalObj = statsMap.get("total");
            if (totalObj != null) {
                statsVO.setTotalCount(Integer.parseInt(totalObj.toString()));
            } else {
                statsVO.setTotalCount(0);
            }
            
            // 设置评分分布
            Map<String, Integer> ratingDistribution = new HashMap<>();
            ratingDistribution.put("5", statsMap.get("five_star") != null ? 
                    Integer.parseInt(statsMap.get("five_star").toString()) : 0);
            ratingDistribution.put("4", statsMap.get("four_star") != null ? 
                    Integer.parseInt(statsMap.get("four_star").toString()) : 0);
            ratingDistribution.put("3", statsMap.get("three_star") != null ? 
                    Integer.parseInt(statsMap.get("three_star").toString()) : 0);
            ratingDistribution.put("2", statsMap.get("two_star") != null ? 
                    Integer.parseInt(statsMap.get("two_star").toString()) : 0);
            ratingDistribution.put("1", statsMap.get("one_star") != null ? 
                    Integer.parseInt(statsMap.get("one_star").toString()) : 0);
            statsVO.setRatingDistribution(ratingDistribution);
            
            logger.info("获取茶叶评价统计成功, teaId: {}, 平均评分: {}, 总数: {}", 
                    teaId, statsVO.getAverageRating(), statsVO.getTotalCount());
            return Result.success(200, statsVO);
            
        } catch (Exception e) {
            logger.error("获取茶叶评价统计失败: 系统异常, teaId: {}", teaId, e);
            return Result.failure(3111);
        }
    }
    
    /**
     * 商家回复评价
     * 路径: POST /tea/reviews/{reviewId}/reply
     * 成功码: 3008, 失败码: 3113
     */
    @Override
    public Result<Boolean> replyReview(String reviewId, Map<String, Object> replyData) {
        try {
            logger.info("商家回复评价请求, reviewId: {}, replyData: {}", reviewId, replyData);
            
            // 1. 参数验证
            if (reviewId == null || reviewId.trim().isEmpty()) {
                logger.warn("商家回复评价失败: 评价ID不能为空");
                return Result.failure(3113);
            }
            if (replyData == null || replyData.isEmpty()) {
                logger.warn("商家回复评价失败: 回复数据为空");
                return Result.failure(3113);
            }
            
            // 2. 提取回复内容
            String reply = replyData.get("reply") != null ? replyData.get("reply").toString() : null;
            if (reply == null || reply.trim().isEmpty()) {
                logger.warn("商家回复评价失败: 回复内容不能为空");
                return Result.failure(3113);
            }
            
            // 3. 验证评价是否存在
            TeaReview review = teaReviewMapper.selectById(Long.valueOf(reviewId));
            if (review == null) {
                logger.warn("商家回复评价失败: 评价不存在, reviewId: {}", reviewId);
                return Result.failure(3113);
            }
            
            // 4. 获取当前用户ID
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                logger.warn("商家回复评价失败: 用户未登录");
                return Result.failure(3113);
            }
            
            // 5. 验证权限：查询茶叶所属店铺，验证当前用户是否为店主
            Tea tea = teaMapper.selectById(review.getTeaId());
            if (tea == null) {
                logger.warn("商家回复评价失败: 茶叶不存在, teaId: {}", review.getTeaId());
                return Result.failure(3113);
            }
            
            Shop shop = shopMapper.selectById(tea.getShopId());
            if (shop == null) {
                logger.warn("商家回复评价失败: 店铺不存在, shopId: {}", tea.getShopId());
                return Result.failure(3113);
            }
            
            if (!currentUserId.equals(shop.getOwnerId())) {
                logger.warn("商家回复评价失败: 无权限回复此评价, userId: {}, shopOwnerId: {}", 
                        currentUserId, shop.getOwnerId());
                return Result.failure(3113);
            }
            
            // 6. 执行回复
            Map<String, Object> replyParams = new HashMap<>();
            replyParams.put("id", Integer.valueOf(reviewId));
            replyParams.put("reply", reply.trim());
            
            int updateCount = teaReviewMapper.reply(replyParams);
            if (updateCount == 0) {
                logger.warn("商家回复评价失败: 数据库更新失败, reviewId: {}", reviewId);
                return Result.failure(3113);
            }
            
            logger.info("商家回复评价成功, reviewId: {}, shopId: {}", reviewId, shop.getId());
            return Result.success(3008, true);
            
        } catch (NumberFormatException e) {
            logger.error("商家回复评价失败: 数字格式错误, reviewId: {}", reviewId, e);
            return Result.failure(3113);
        } catch (Exception e) {
            logger.error("商家回复评价失败: 系统异常, reviewId: {}", reviewId, e);
            return Result.failure(3113);
        }
    }
    
    /**
     * 将TeaReview实体转换为ReviewVO（使用用户缓存，优化性能）
     */
    private ReviewVO convertToReviewVO(TeaReview review, Map<String, com.shangnantea.model.entity.user.User> userCache) {
        if (review == null) {
            return null;
        }
        
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setTeaId(review.getTeaId());
        vo.setUserId(review.getUserId());
        vo.setContent(review.getContent());
        vo.setRating(review.getRating());
        vo.setReply(review.getReply());
        vo.setReplyTime(review.getReplyTime());
        vo.setIsAnonymous(review.getIsAnonymous());
        // 使用动态计算获取点赞数
        vo.setLikeCount(statisticsUtils.getLikeCount("review", String.valueOf(review.getId())));
        vo.setCreateTime(review.getCreateTime());
        
        // 处理图片列表
        if (review.getImages() != null && !review.getImages().isEmpty()) {
            List<String> imageList = java.util.Arrays.asList(review.getImages().split(","));
            vo.setImages(imageList);
        } else {
            vo.setImages(new ArrayList<>());
        }
        
        // 从缓存中获取用户信息
        com.shangnantea.model.entity.user.User user = userCache.get(review.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
            vo.setAvatar(user.getAvatar() != null ? user.getAvatar() : "");
        } else {
            // 用户不存在时的降级处理
            vo.setUsername("用户" + review.getUserId());
            vo.setNickname("用户" + review.getUserId());
            vo.setAvatar("");
        }
        
        return vo;
    }
    
    /**
     * 将TeaReview实体转换为ReviewVO（单个查询，用于其他场景）
     */
    private ReviewVO convertToReviewVO(TeaReview review) {
        if (review == null) {
            return null;
        }
        
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setTeaId(review.getTeaId());
        vo.setUserId(review.getUserId());
        vo.setContent(review.getContent());
        vo.setRating(review.getRating());
        vo.setReply(review.getReply());
        vo.setReplyTime(review.getReplyTime());
        vo.setIsAnonymous(review.getIsAnonymous());
        // 使用动态计算获取点赞数
        vo.setLikeCount(statisticsUtils.getLikeCount("review", String.valueOf(review.getId())));
        vo.setCreateTime(review.getCreateTime());
        
        // 处理图片列表
        if (review.getImages() != null && !review.getImages().isEmpty()) {
            List<String> imageList = java.util.Arrays.asList(review.getImages().split(","));
            vo.setImages(imageList);
        } else {
            vo.setImages(new ArrayList<>());
        }
        
        // 查询用户信息设置username, nickname, avatar
        try {
            com.shangnantea.model.entity.user.User user = userMapper.selectById(review.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
                vo.setAvatar(user.getAvatar() != null ? user.getAvatar() : "");
            } else {
                // 用户不存在时的降级处理
                vo.setUsername("用户" + review.getUserId());
                vo.setNickname("用户" + review.getUserId());
                vo.setAvatar("");
            }
        } catch (Exception e) {
            logger.warn("查询用户信息失败, userId: {}", review.getUserId(), e);
            // 查询失败时的降级处理
            vo.setUsername("用户" + review.getUserId());
            vo.setNickname("用户" + review.getUserId());
            vo.setAvatar("");
        }
        
        return vo;
    }
    
    // ⚠️ 已删除：评价点赞相关方法实现（likeReview）
    // 说明：评价点赞功能已统一使用用户模块的通用接口（UserServiceImpl 中的 addLike/removeLike）
    // 评价列表接口（getTeaReviews）已包含每个评价的 isLiked 字段，无需单独调用点赞接口
    
    /**
     * 获取茶叶规格列表
     * 路径: GET /tea/{teaId}/specifications
     * 成功码: 200, 失败码: 3115
     */
    @Override
    public Result<Object> getTeaSpecifications(String teaId) {
        try {
            logger.info("获取茶叶规格列表请求, teaId: {}", teaId);
            
            // 1. 参数验证
            if (teaId == null || teaId.trim().isEmpty()) {
                logger.warn("获取茶叶规格列表失败: 茶叶ID不能为空");
                return Result.failure(3115);
            }
            
            // 2. 验证茶叶是否存在
            Tea tea = teaMapper.selectById(teaId);
            if (tea == null) {
                logger.warn("获取茶叶规格列表失败: 茶叶不存在, teaId: {}", teaId);
                return Result.failure(3115);
            }
            
            // 3. 查询规格列表
            List<TeaSpecification> specifications = teaSpecificationMapper.selectByTeaId(teaId);
            
            // 4. 转换为VO
            List<TeaVO.TeaSpecificationVO> specVOList = specifications.stream()
                    .map(this::convertToSpecificationVO)
                    .collect(Collectors.toList());
            
            logger.info("获取茶叶规格列表成功, teaId: {}, 规格数量: {}", teaId, specVOList.size());
            return Result.success(200, specVOList);
            
        } catch (Exception e) {
            logger.error("获取茶叶规格列表失败: 系统异常, teaId: {}", teaId, e);
            return Result.failure(3115);
        }
    }
    
    /**
     * 添加茶叶规格
     * 路径: POST /tea/{teaId}/specifications
     * 成功码: 3010, 失败码: 3116
     */
    @Override
    public Result<Object> addSpecification(String teaId, Map<String, Object> specData) {
        try {
            logger.info("添加茶叶规格请求, teaId: {}, specData: {}", teaId, specData);
            
            // 1. 参数验证
            if (teaId == null || teaId.trim().isEmpty()) {
                logger.warn("添加茶叶规格失败: 茶叶ID不能为空");
                return Result.failure(3116);
            }
            if (specData == null || specData.isEmpty()) {
                logger.warn("添加茶叶规格失败: 规格数据为空");
                return Result.failure(3116);
            }
            
            // 2. 验证茶叶是否存在
            Tea tea = teaMapper.selectById(teaId);
            if (tea == null) {
                logger.warn("添加茶叶规格失败: 茶叶不存在, teaId: {}", teaId);
                return Result.failure(3116);
            }
            
            // 3. 权限验证：验证茶叶是否属于当前用户的店铺
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                logger.warn("添加茶叶规格失败: 用户未登录");
                return Result.failure(3116);
            }
            
            Shop shop = shopMapper.selectById(tea.getShopId());
            if (shop == null) {
                logger.warn("添加茶叶规格失败: 店铺不存在, shopId: {}", tea.getShopId());
                return Result.failure(3116);
            }
            if (!currentUserId.equals(shop.getOwnerId())) {
                logger.warn("添加茶叶规格失败: 无权限添加规格, userId: {}, shopOwnerId: {}", 
                        currentUserId, shop.getOwnerId());
                return Result.failure(3116);
            }
            
            // 4. 提取并验证规格字段
            String specName = specData.get("specName") != null ? specData.get("specName").toString() : null;
            BigDecimal price = specData.get("price") != null ? 
                    new BigDecimal(specData.get("price").toString()) : null;
            Integer stock = specData.get("stock") != null ? 
                    Integer.valueOf(specData.get("stock").toString()) : null;
            Integer isDefault = specData.get("isDefault") != null ? 
                    Integer.valueOf(specData.get("isDefault").toString()) : 0;
            
            // 验证必填字段
            if (specName == null || specName.trim().isEmpty()) {
                logger.warn("添加茶叶规格失败: 规格名称不能为空");
                return Result.failure(3116);
            }
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                logger.warn("添加茶叶规格失败: 价格必须大于0");
                return Result.failure(3116);
            }
            if (stock == null || stock < 0) {
                logger.warn("添加茶叶规格失败: 库存不能为负数");
                return Result.failure(3116);
            }
            
            // 5. 如果设置为默认规格，先取消其他默认规格
            if (isDefault == 1) {
                teaSpecificationMapper.clearDefaultByTeaId(teaId);
            }
            
            // 6. 创建规格对象
            TeaSpecification specification = new TeaSpecification();
            specification.setTeaId(teaId);
            specification.setSpecName(specName.trim());
            specification.setPrice(price);
            specification.setStock(stock);
            specification.setIsDefault(isDefault);
            
            Date now = new Date();
            specification.setCreateTime(now);
            specification.setUpdateTime(now);
            
            // 7. 插入数据库
            int result = teaSpecificationMapper.insert(specification);
            if (result <= 0) {
                logger.error("添加茶叶规格失败: 数据库插入失败");
                return Result.failure(3116);
            }
            
            logger.info("添加茶叶规格成功, teaId: {}, specName: {}", teaId, specName);
            return Result.success(3010, null);
            
        } catch (NumberFormatException e) {
            logger.error("添加茶叶规格失败: 数字格式错误, teaId: {}", teaId, e);
            return Result.failure(3116);
        } catch (Exception e) {
            logger.error("添加茶叶规格失败: 系统异常, teaId: {}", teaId, e);
            return Result.failure(3116);
        }
    }
    
    /**
     * 更新茶叶规格
     * 路径: PUT /tea/specifications/{specId}
     * 成功码: 3011, 失败码: 3117
     */
    @Override
    public Result<Boolean> updateSpecification(String specId, Map<String, Object> specData) {
        try {
            logger.info("更新茶叶规格请求, specId: {}, specData: {}", specId, specData);
            
            // 1. 参数验证
            if (specId == null || specId.trim().isEmpty()) {
                logger.warn("更新茶叶规格失败: 规格ID不能为空");
                return Result.failure(3117);
            }
            if (specData == null || specData.isEmpty()) {
                logger.warn("更新茶叶规格失败: 规格数据为空");
                return Result.failure(3117);
            }
            
            // 2. 验证规格是否存在
            TeaSpecification existingSpec = teaSpecificationMapper.selectById(Integer.valueOf(specId));
            if (existingSpec == null) {
                logger.warn("更新茶叶规格失败: 规格不存在, specId: {}", specId);
                return Result.failure(3117);
            }
            
            // 3. 权限验证：验证茶叶是否属于当前用户的店铺
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                logger.warn("更新茶叶规格失败: 用户未登录");
                return Result.failure(3117);
            }
            
            Tea tea = teaMapper.selectById(existingSpec.getTeaId());
            if (tea == null) {
                logger.warn("更新茶叶规格失败: 茶叶不存在, teaId: {}", existingSpec.getTeaId());
                return Result.failure(3117);
            }
            
            Shop shop = shopMapper.selectById(tea.getShopId());
            if (shop == null) {
                logger.warn("更新茶叶规格失败: 店铺不存在, shopId: {}", tea.getShopId());
                return Result.failure(3117);
            }
            if (!currentUserId.equals(shop.getOwnerId())) {
                logger.warn("更新茶叶规格失败: 无权限更新规格, userId: {}, shopOwnerId: {}", 
                        currentUserId, shop.getOwnerId());
                return Result.failure(3117);
            }
            
            // 4. 提取并验证更新字段
            String specName = specData.get("specName") != null ? specData.get("specName").toString() : null;
            BigDecimal price = specData.get("price") != null ? 
                    new BigDecimal(specData.get("price").toString()) : null;
            Integer stock = specData.get("stock") != null ? 
                    Integer.valueOf(specData.get("stock").toString()) : null;
            Integer isDefault = specData.get("isDefault") != null ? 
                    Integer.valueOf(specData.get("isDefault").toString()) : null;
            
            // 验证字段
            if (specName != null && specName.trim().isEmpty()) {
                logger.warn("更新茶叶规格失败: 规格名称不能为空");
                return Result.failure(3117);
            }
            if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
                logger.warn("更新茶叶规格失败: 价格必须大于0");
                return Result.failure(3117);
            }
            if (stock != null && stock < 0) {
                logger.warn("更新茶叶规格失败: 库存不能为负数");
                return Result.failure(3117);
            }
            
            // 5. 如果设置为默认规格，先取消其他默认规格
            if (isDefault != null && isDefault == 1) {
                teaSpecificationMapper.clearDefaultByTeaId(existingSpec.getTeaId());
            }
            
            // 6. 更新规格对象
            TeaSpecification specification = new TeaSpecification();
            specification.setId(Integer.valueOf(specId));
            
            if (specName != null) {
                specification.setSpecName(specName.trim());
            }
            if (price != null) {
                specification.setPrice(price);
            }
            if (stock != null) {
                specification.setStock(stock);
            }
            if (isDefault != null) {
                specification.setIsDefault(isDefault);
            }
            
            specification.setUpdateTime(new Date());
            
            // 7. 执行更新
            int updateCount = teaSpecificationMapper.update(specification);
            if (updateCount == 0) {
                logger.warn("更新茶叶规格失败: 数据库更新失败, specId: {}", specId);
                return Result.failure(3117);
            }
            
            logger.info("更新茶叶规格成功, specId: {}", specId);
            return Result.success(3011, true);
            
        } catch (NumberFormatException e) {
            logger.error("更新茶叶规格失败: 数字格式错误, specId: {}", specId, e);
            return Result.failure(3117);
        } catch (Exception e) {
            logger.error("更新茶叶规格失败: 系统异常, specId: {}", specId, e);
            return Result.failure(3117);
        }
    }
    
    /**
     * 删除茶叶规格
     * 路径: DELETE /tea/specifications/{specId}
     * 成功码: 3012, 失败码: 3118
     */
    @Override
    public Result<Boolean> deleteSpecification(String specId) {
        try {
            logger.info("删除茶叶规格请求, specId: {}", specId);
            
            // 1. 参数验证
            if (specId == null || specId.trim().isEmpty()) {
                logger.warn("删除茶叶规格失败: 规格ID不能为空");
                return Result.failure(3118);
            }
            
            // 2. 验证规格是否存在
            TeaSpecification specification = teaSpecificationMapper.selectById(Integer.valueOf(specId));
            if (specification == null) {
                logger.warn("删除茶叶规格失败: 规格不存在, specId: {}", specId);
                return Result.failure(3118);
            }
            
            // 3. 权限验证：验证茶叶是否属于当前用户的店铺
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                logger.warn("删除茶叶规格失败: 用户未登录");
                return Result.failure(3118);
            }
            
            Tea tea = teaMapper.selectById(specification.getTeaId());
            if (tea == null) {
                logger.warn("删除茶叶规格失败: 茶叶不存在, teaId: {}", specification.getTeaId());
                return Result.failure(3118);
            }
            
            Shop shop = shopMapper.selectById(tea.getShopId());
            if (shop == null) {
                logger.warn("删除茶叶规格失败: 店铺不存在, shopId: {}", tea.getShopId());
                return Result.failure(3118);
            }
            if (!currentUserId.equals(shop.getOwnerId())) {
                logger.warn("删除茶叶规格失败: 无权限删除规格, userId: {}, shopOwnerId: {}", 
                        currentUserId, shop.getOwnerId());
                return Result.failure(3118);
            }
            
            // 4. 检查是否为默认规格
            if (specification.getIsDefault() != null && specification.getIsDefault() == 1) {
                // 查询该茶叶的所有规格
                List<TeaSpecification> allSpecs = teaSpecificationMapper.selectByTeaId(specification.getTeaId());
                if (allSpecs.size() <= 1) {
                    logger.warn("删除茶叶规格失败: 不能删除唯一的规格, specId: {}", specId);
                    return Result.failure(3118);
                }
                // 如果有多个规格，删除默认规格后需要设置另一个为默认
                logger.info("删除默认规格，将自动设置另一个规格为默认");
            }
            
            // 5. 执行删除
            int deleteCount = teaSpecificationMapper.deleteById(Integer.valueOf(specId));
            if (deleteCount == 0) {
                logger.warn("删除茶叶规格失败: 数据库删除失败, specId: {}", specId);
                return Result.failure(3118);
            }
            
            // 6. 如果删除的是默认规格，设置第一个规格为默认
            if (specification.getIsDefault() != null && specification.getIsDefault() == 1) {
                List<TeaSpecification> remainingSpecs = teaSpecificationMapper.selectByTeaId(specification.getTeaId());
                if (!remainingSpecs.isEmpty()) {
                    teaSpecificationMapper.setDefault(remainingSpecs.get(0).getId());
                    logger.info("已将规格 {} 设置为默认规格", remainingSpecs.get(0).getId());
                }
            }
            
            logger.info("删除茶叶规格成功, specId: {}", specId);
            return Result.success(3012, true);
            
        } catch (NumberFormatException e) {
            logger.error("删除茶叶规格失败: 规格ID格式错误, specId: {}", specId, e);
            return Result.failure(3118);
        } catch (Exception e) {
            logger.error("删除茶叶规格失败: 系统异常, specId: {}", specId, e);
            return Result.failure(3118);
        }
    }
    
    /**
     * 设置默认规格
     * 路径: PUT /tea/specifications/{specId}/default
     * 成功码: 3013, 失败码: 3119
     */
    @Override
    public Result<Boolean> setDefaultSpecification(String specId) {
        try {
            logger.info("设置默认规格请求, specId: {}", specId);
            
            // 1. 参数验证
            if (specId == null || specId.trim().isEmpty()) {
                logger.warn("设置默认规格失败: 规格ID不能为空");
                return Result.failure(3119);
            }
            
            // 2. 验证规格是否存在
            TeaSpecification specification = teaSpecificationMapper.selectById(Integer.valueOf(specId));
            if (specification == null) {
                logger.warn("设置默认规格失败: 规格不存在, specId: {}", specId);
                return Result.failure(3119);
            }
            
            // 3. 权限验证：验证茶叶是否属于当前用户的店铺
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                logger.warn("设置默认规格失败: 用户未登录");
                return Result.failure(3119);
            }
            
            Tea tea = teaMapper.selectById(specification.getTeaId());
            if (tea == null) {
                logger.warn("设置默认规格失败: 茶叶不存在, teaId: {}", specification.getTeaId());
                return Result.failure(3119);
            }
            
            Shop shop = shopMapper.selectById(tea.getShopId());
            if (shop == null) {
                logger.warn("设置默认规格失败: 店铺不存在, shopId: {}", tea.getShopId());
                return Result.failure(3119);
            }
            if (!currentUserId.equals(shop.getOwnerId())) {
                logger.warn("设置默认规格失败: 无权限设置默认规格, userId: {}, shopOwnerId: {}", 
                        currentUserId, shop.getOwnerId());
                return Result.failure(3119);
            }
            
            // 4. 先取消该茶叶的所有默认规格
            teaSpecificationMapper.clearDefaultByTeaId(specification.getTeaId());
            
            // 5. 设置当前规格为默认
            int updateCount = teaSpecificationMapper.setDefault(Integer.valueOf(specId));
            if (updateCount == 0) {
                logger.warn("设置默认规格失败: 数据库更新失败, specId: {}", specId);
                return Result.failure(3119);
            }
            
            logger.info("设置默认规格成功, specId: {}, teaId: {}", specId, specification.getTeaId());
            return Result.success(3013, true);
            
        } catch (NumberFormatException e) {
            logger.error("设置默认规格失败: 规格ID格式错误, specId: {}", specId, e);
            return Result.failure(3119);
        } catch (Exception e) {
            logger.error("设置默认规格失败: 系统异常, specId: {}", specId, e);
            return Result.failure(3119);
        }
    }
    
    /**
     * 删除茶叶图片
     * 路径: DELETE /tea/images/{imageId}
     * 成功码: 3015, 失败码: 3123
     */
    @Override
    public Result<Boolean> deleteTeaImage(String imageId) {
        try {
            logger.info("删除茶叶图片请求, imageId: {}", imageId);
            
            // 1. 参数验证
            if (imageId == null || imageId.trim().isEmpty()) {
                logger.warn("删除茶叶图片失败: 图片ID不能为空");
                return Result.failure(3123);
            }
            
            // 2. 验证图片是否存在
            TeaImage image = teaImageMapper.selectById(Long.valueOf(imageId));
            if (image == null) {
                logger.warn("删除茶叶图片失败: 图片不存在, imageId: {}", imageId);
                return Result.failure(3123);
            }
            
            // 3. 权限验证：验证茶叶是否属于当前用户的店铺
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                logger.warn("删除茶叶图片失败: 用户未登录");
                return Result.failure(3123);
            }
            
            Tea tea = teaMapper.selectById(image.getTeaId());
            if (tea == null) {
                logger.warn("删除茶叶图片失败: 茶叶不存在, teaId: {}", image.getTeaId());
                return Result.failure(3123);
            }
            
            Shop shop = shopMapper.selectById(tea.getShopId());
            if (shop == null) {
                logger.warn("删除茶叶图片失败: 店铺不存在, shopId: {}", tea.getShopId());
                return Result.failure(3123);
            }
            if (!currentUserId.equals(shop.getOwnerId())) {
                logger.warn("删除茶叶图片失败: 无权限删除图片, userId: {}, shopOwnerId: {}", 
                        currentUserId, shop.getOwnerId());
                return Result.failure(3123);
            }
            
            // 4. 执行删除
            int deleteCount = teaImageMapper.deleteById(Long.valueOf(imageId));
            if (deleteCount == 0) {
                logger.warn("删除茶叶图片失败: 数据库删除失败, imageId: {}", imageId);
                return Result.failure(3123);
            }
            
            logger.info("删除茶叶图片成功, imageId: {}, teaId: {}", imageId, image.getTeaId());
            return Result.success(3015, true);
            
        } catch (NumberFormatException e) {
            logger.error("删除茶叶图片失败: 图片ID格式错误, imageId: {}", imageId, e);
            return Result.failure(3123);
        } catch (Exception e) {
            logger.error("删除茶叶图片失败: 系统异常, imageId: {}", imageId, e);
            return Result.failure(3123);
        }
    }
    
    /**
     * 设置主图
     * 路径: PUT /tea/images/{imageId}/main
     * 成功码: 3016, 失败码: 3124
     */
    @Override
    public Result<Boolean> setMainImage(String imageId) {
        try {
            logger.info("设置主图请求, imageId: {}", imageId);
            
            // 1. 参数验证
            if (imageId == null || imageId.trim().isEmpty()) {
                logger.warn("设置主图失败: 图片ID不能为空");
                return Result.failure(3124);
            }
            
            // 2. 验证图片是否存在
            TeaImage image = teaImageMapper.selectById(Long.valueOf(imageId));
            if (image == null) {
                logger.warn("设置主图失败: 图片不存在, imageId: {}", imageId);
                return Result.failure(3124);
            }
            
            // 3. 权限验证：验证茶叶是否属于当前用户的店铺
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                logger.warn("设置主图失败: 用户未登录");
                return Result.failure(3124);
            }
            
            Tea tea = teaMapper.selectById(image.getTeaId());
            if (tea == null) {
                logger.warn("设置主图失败: 茶叶不存在, teaId: {}", image.getTeaId());
                return Result.failure(3124);
            }
            
            Shop shop = shopMapper.selectById(tea.getShopId());
            if (shop == null) {
                logger.warn("设置主图失败: 店铺不存在, shopId: {}", tea.getShopId());
                return Result.failure(3124);
            }
            if (!currentUserId.equals(shop.getOwnerId())) {
                logger.warn("设置主图失败: 无权限设置主图, userId: {}, shopOwnerId: {}", 
                        currentUserId, shop.getOwnerId());
                return Result.failure(3124);
            }
            
            // 4. 设置主图（会自动清除该茶叶的其他主图标记）
            teaImageMapper.setMainImage(Integer.valueOf(imageId));
            
            // 5. 同时更新tea表的main_image字段
            Tea updateTea = new Tea();
            updateTea.setId(image.getTeaId());
            updateTea.setMainImage(image.getUrl());
            teaMapper.updateById(updateTea);
            
            logger.info("设置主图成功, imageId: {}, teaId: {}", imageId, image.getTeaId());
            return Result.success(3016, true);
            
        } catch (NumberFormatException e) {
            logger.error("设置主图失败: 图片ID格式错误, imageId: {}", imageId, e);
            return Result.failure(3124);
        } catch (Exception e) {
            logger.error("设置主图失败: 系统异常, imageId: {}", imageId, e);
            return Result.failure(3124);
        }
    }
    
    /**
     * 更新图片顺序
     * 路径: PUT /tea/images/order
     * 成功码: 3017, 失败码: 3125
     */
    @Override
    public Result<Boolean> updateImageOrder(Map<String, Object> orderData) {
        try {
            logger.info("更新图片顺序请求, orderData: {}", orderData);
            
            // 1. 参数验证
            if (orderData == null || orderData.isEmpty()) {
                logger.warn("更新图片顺序失败: 参数为空");
                return Result.failure(3125);
            }
            
            String teaId = orderData.get("teaId") != null ? orderData.get("teaId").toString() : null;
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orders = (List<Map<String, Object>>) orderData.get("orders");
            
            if (teaId == null || teaId.trim().isEmpty()) {
                logger.warn("更新图片顺序失败: 茶叶ID不能为空");
                return Result.failure(3125);
            }
            if (orders == null || orders.isEmpty()) {
                logger.warn("更新图片顺序失败: 排序数据不能为空");
                return Result.failure(3125);
            }
            
            // 2. 验证茶叶是否存在
            Tea tea = teaMapper.selectById(teaId);
            if (tea == null) {
                logger.warn("更新图片顺序失败: 茶叶不存在, teaId: {}", teaId);
                return Result.failure(3125);
            }
            
            // 3. 权限验证：验证茶叶是否属于当前用户的店铺
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                logger.warn("更新图片顺序失败: 用户未登录");
                return Result.failure(3125);
            }
            
            Shop shop = shopMapper.selectById(tea.getShopId());
            if (shop == null) {
                logger.warn("更新图片顺序失败: 店铺不存在, shopId: {}", tea.getShopId());
                return Result.failure(3125);
            }
            if (!currentUserId.equals(shop.getOwnerId())) {
                logger.warn("更新图片顺序失败: 无权限更新图片顺序, userId: {}, shopOwnerId: {}", 
                        currentUserId, shop.getOwnerId());
                return Result.failure(3125);
            }
            
            // 4. 批量更新图片顺序
            for (Map<String, Object> order : orders) {
                Integer imageId = order.get("id") != null ? Integer.valueOf(order.get("id").toString()) : null;
                Integer sortOrder = order.get("sortOrder") != null ? Integer.valueOf(order.get("sortOrder").toString()) : null;
                
                if (imageId != null && sortOrder != null) {
                    teaImageMapper.updateSortOrder(imageId, sortOrder);
                }
            }
            
            logger.info("更新图片顺序成功, teaId: {}, 更新数量: {}", teaId, orders.size());
            return Result.success(3017, true);
            
        } catch (NumberFormatException e) {
            logger.error("更新图片顺序失败: 数据格式错误", e);
            return Result.failure(3125);
        } catch (Exception e) {
            logger.error("更新图片顺序失败: 系统异常", e);
            return Result.failure(3125);
        }
    }
    
    /**
     * 切换茶叶状态（上架/下架）
     * 路径: PUT /tea/{teaId}/status
     * 成功码: 3018(上架), 3019(下架), 失败码: 3126, 3127
     */
    @Override
    public Result<Boolean> toggleTeaStatus(String teaId, Map<String, Object> statusData) {
        try {
            logger.info("切换茶叶状态请求, teaId: {}, statusData: {}", teaId, statusData);
            
            // 1. 参数验证
            if (teaId == null || teaId.trim().isEmpty()) {
                logger.warn("切换茶叶状态失败: 茶叶ID不能为空");
                return Result.failure(3126);
            }
            if (statusData == null || statusData.get("status") == null) {
                logger.warn("切换茶叶状态失败: 状态参数不能为空");
                return Result.failure(3126);
            }
            
            Integer status = Integer.valueOf(statusData.get("status").toString());
            if (status != 0 && status != 1) {
                logger.warn("切换茶叶状态失败: 状态值无效, status: {}", status);
                return Result.failure(3126);
            }
            
            // 2. 验证茶叶是否存在
            Tea tea = teaMapper.selectById(teaId);
            if (tea == null) {
                logger.warn("切换茶叶状态失败: 茶叶不存在, teaId: {}", teaId);
                return Result.failure(status == 1 ? 3126 : 3127);
            }
            
            // 3. 验证茶叶是否已删除
            if (tea.getIsDeleted() != null && tea.getIsDeleted() == 1) {
                logger.warn("切换茶叶状态失败: 茶叶已删除, teaId: {}", teaId);
                return Result.failure(status == 1 ? 3126 : 3127);
            }
            
            // 4. 权限验证：验证茶叶是否属于当前用户的店铺
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                logger.warn("切换茶叶状态失败: 用户未登录");
                return Result.failure(status == 1 ? 3126 : 3127);
            }
            
            Shop shop = shopMapper.selectById(tea.getShopId());
            if (shop == null) {
                logger.warn("切换茶叶状态失败: 店铺不存在, shopId: {}", tea.getShopId());
                return Result.failure(status == 1 ? 3126 : 3127);
            }
            if (!currentUserId.equals(shop.getOwnerId())) {
                logger.warn("切换茶叶状态失败: 无权限切换状态, userId: {}, shopOwnerId: {}", 
                        currentUserId, shop.getOwnerId());
                return Result.failure(status == 1 ? 3126 : 3127);
            }
            
            // 5. 执行状态更新
            int updateCount = teaMapper.updateStatus(teaId, status);
            if (updateCount == 0) {
                logger.warn("切换茶叶状态失败: 数据库更新失败, teaId: {}", teaId);
                return Result.failure(status == 1 ? 3126 : 3127);
            }
            
            // 6. 根据状态返回不同的成功码
            int successCode = status == 1 ? 3018 : 3019;
            logger.info("切换茶叶状态成功, teaId: {}, status: {}, code: {}", teaId, status, successCode);
            return Result.success(successCode, true);
            
        } catch (NumberFormatException e) {
            logger.error("切换茶叶状态失败: 数据格式错误, teaId: {}", teaId, e);
            return Result.failure(3126);
        } catch (Exception e) {
            logger.error("切换茶叶状态失败: 系统异常, teaId: {}", teaId, e);
            return Result.failure(3126);
        }
    }
    
    /**
     * 批量切换茶叶状态（上架/下架）
     * 路径: PUT /tea/batch-status
     * 成功码: 3020(批量上架), 3021(批量下架), 失败码: 3128, 3129
     */
    @Override
    public Result<Boolean> batchToggleTeaStatus(Map<String, Object> batchData) {
        try {
            logger.info("批量切换茶叶状态请求, batchData: {}", batchData);
            
            // 1. 参数验证
            if (batchData == null || batchData.isEmpty()) {
                logger.warn("批量切换茶叶状态失败: 参数为空");
                return Result.failure(3128);
            }
            
            @SuppressWarnings("unchecked")
            List<String> teaIds = (List<String>) batchData.get("teaIds");
            Integer status = batchData.get("status") != null ? Integer.valueOf(batchData.get("status").toString()) : null;
            
            if (teaIds == null || teaIds.isEmpty()) {
                logger.warn("批量切换茶叶状态失败: 茶叶ID列表不能为空");
                return Result.failure(3128);
            }
            if (status == null) {
                logger.warn("批量切换茶叶状态失败: 状态参数不能为空");
                return Result.failure(3128);
            }
            if (status != 0 && status != 1) {
                logger.warn("批量切换茶叶状态失败: 状态值无效, status: {}", status);
                return Result.failure(3128);
            }
            
            // 2. 权限验证：验证所有茶叶是否属于当前用户的店铺
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                logger.warn("批量切换茶叶状态失败: 用户未登录");
                return Result.failure(status == 1 ? 3128 : 3129);
            }
            
            // 验证每个茶叶的权限
            for (String teaId : teaIds) {
                Tea tea = teaMapper.selectById(teaId);
                if (tea == null) {
                    logger.warn("批量切换茶叶状态失败: 茶叶不存在, teaId: {}", teaId);
                    return Result.failure(status == 1 ? 3128 : 3129);
                }
                
                if (tea.getIsDeleted() != null && tea.getIsDeleted() == 1) {
                    logger.warn("批量切换茶叶状态失败: 茶叶已删除, teaId: {}", teaId);
                    return Result.failure(status == 1 ? 3128 : 3129);
                }
                
                Shop shop = shopMapper.selectById(tea.getShopId());
                if (shop == null) {
                    logger.warn("批量切换茶叶状态失败: 店铺不存在, shopId: {}", tea.getShopId());
                    return Result.failure(status == 1 ? 3128 : 3129);
                }
                if (!currentUserId.equals(shop.getOwnerId())) {
                    logger.warn("批量切换茶叶状态失败: 无权限切换状态, userId: {}, shopOwnerId: {}", 
                            currentUserId, shop.getOwnerId());
                    return Result.failure(status == 1 ? 3128 : 3129);
                }
            }
            
            // 3. 执行批量状态更新
            int updateCount = teaMapper.batchUpdateStatus(teaIds, status);
            if (updateCount == 0) {
                logger.warn("批量切换茶叶状态失败: 数据库更新失败");
                return Result.failure(status == 1 ? 3128 : 3129);
            }
            
            // 4. 根据状态返回不同的成功码
            int successCode = status == 1 ? 3020 : 3021;
            logger.info("批量切换茶叶状态成功, 更新数量: {}, status: {}, code: {}", updateCount, status, successCode);
            return Result.success(successCode, true);
            
        } catch (NumberFormatException e) {
            logger.error("批量切换茶叶状态失败: 数据格式错误", e);
            return Result.failure(3128);
        } catch (Exception e) {
            logger.error("批量切换茶叶状态失败: 系统异常", e);
            return Result.failure(3128);
        }
    }
}

