package com.shangnantea.service;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.common.api.Result;
import com.shangnantea.model.entity.tea.Tea;
import com.shangnantea.model.entity.tea.TeaCategory;
import com.shangnantea.model.entity.tea.TeaImage;
import com.shangnantea.model.entity.tea.TeaSpecification;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 茶叶服务接口
 */
public interface TeaService {
    
    /**
     * 获取茶叶列表
     * 路径: GET /tea/list
     * 成功码: 3000, 失败码: 3100
     *
     * @param params 查询参数（Map格式，包含page, pageSize, categoryId, keyword, shopId, sortBy, sortOrder等）
     * @return 茶叶列表分页结果
     */
    Result<Object> getTeas(Map<String, Object> params);
    
    /**
     * 添加茶叶
     * 路径: POST /tea/list
     * 成功码: 3001, 失败码: 3101
     *
     * @param teaData 茶叶数据（Map格式）
     * @return 添加结果，包含新创建的茶叶信息
     */
    Result<Object> addTea(Map<String, Object> teaData);
    
    /**
     * 获取茶叶详情
     * 路径: GET /tea/{id}
     * 成功码: 200, 失败码: 3102
     * 改造说明：返回的茶叶详情中包含 isFavorited 字段（当前用户是否已收藏该茶叶）
     *
     * @param id 茶叶ID
     * @return 茶叶详情信息（包含 isFavorited 字段）
     */
    Result<Object> getTeaDetail(String id);
    
    /**
     * 更新茶叶信息
     * 路径: PUT /tea/{id}
     * 成功码: 3002, 失败码: 3103
     *
     * @param id 茶叶ID
     * @param teaData 茶叶数据（Map格式）
     * @return 更新后的茶叶信息
     */
    Result<Object> updateTea(String id, Map<String, Object> teaData);
    
    /**
     * 删除茶叶（商家/管理员）
     * 路径: DELETE /tea/{id}
     * 成功码: 3003, 失败码: 3104
     *
     * @param id 茶叶ID
     * @return 删除结果
     */
    Result<Object> deleteTea(String id);
    
    /**
     * 获取推荐茶叶
     * 路径: GET /tea/recommend
     * 成功码: 200, 失败码: 3105
     *
     * @param params 查询参数（Map格式，包含type, teaId, count等）
     * @return 推荐茶叶列表
     */
    Result<Object> getRecommendTeas(Map<String, Object> params);
    
    /**
     * 获取茶叶分类列表
     * 路径: GET /tea/categories
     * 成功码: 200, 失败码: 3106
     *
     * @return 分类列表
     */
    Result<Object> getTeaCategories();
    
    /**
     * 创建茶叶分类（管理员）
     * 路径: POST /tea/categories
     * 成功码: 3004, 失败码: 3107
     *
     * @param categoryData 分类数据（Map格式）
     * @return 创建结果
     */
    Result<Object> createCategory(Map<String, Object> categoryData);
    
    /**
     * 更新茶叶分类（管理员）
     * 路径: PUT /tea/categories/{id}
     * 成功码: 3005, 失败码: 3108
     *
     * @param id 分类ID
     * @param categoryData 分类数据（Map格式）
     * @return 更新结果
     */
    Result<Object> updateCategory(Integer id, Map<String, Object> categoryData);
    
    /**
     * 删除茶叶分类（管理员）
     * 路径: DELETE /tea/categories/{id}
     * 成功码: 3006, 失败码: 3109
     *
     * @param id 分类ID
     * @return 删除结果
     */
    Result<Boolean> deleteCategory(Integer id);
    
    /**
     * 获取茶叶评价列表
     * 路径: GET /tea/{teaId}/reviews
     * 成功码: 200, 失败码: 3110
     * 改造说明：返回的评价列表中，每个评价对象包含 isLiked 字段（当前用户是否已点赞该评价）
     *
     * @param teaId 茶叶ID
     * @param params 查询参数（Map格式，包含page, pageSize等）
     * @return 评价列表（每个评价对象包含 isLiked 字段）
     */
    Result<Object> getTeaReviews(String teaId, Map<String, Object> params);
    
    /**
     * 获取茶叶评价统计
     * 路径: GET /tea/{teaId}/reviews/stats
     * 成功码: 200, 失败码: 3111
     *
     * @param teaId 茶叶ID
     * @return 评价统计数据
     */
    Result<Object> getReviewStats(String teaId);
    
    /**
     * 商家回复评价
     * 路径: POST /tea/reviews/{reviewId}/reply
     * 成功码: 3008, 失败码: 3113
     *
     * @param reviewId 评价ID
     * @param replyData 回复数据（Map格式）
     * @return 回复结果
     */
    Result<Boolean> replyReview(String reviewId, Map<String, Object> replyData);
    
    // ⚠️ 已删除：评价点赞相关接口（likeReview）
    // 说明：评价点赞功能已统一使用用户模块的通用接口（UserService 中的 addLike/removeLike）
    // 评价列表接口（getTeaReviews）已包含每个评价的 isLiked 字段，无需单独调用点赞接口
    
    /**
     * 获取茶叶规格列表
     * 路径: GET /tea/{teaId}/specifications
     * 成功码: 200, 失败码: 3115
     *
     * @param teaId 茶叶ID
     * @return 规格列表
     */
    Result<Object> getTeaSpecifications(String teaId);
    
    /**
     * 添加茶叶规格
     * 路径: POST /tea/{teaId}/specifications
     * 成功码: 3010, 失败码: 3116
     *
     * @param teaId 茶叶ID
     * @param specData 规格数据（Map格式）
     * @return 添加结果
     */
    Result<Object> addSpecification(String teaId, Map<String, Object> specData);
    
    /**
     * 更新茶叶规格
     * 路径: PUT /tea/specifications/{specId}
     * 成功码: 3011, 失败码: 3117
     *
     * @param specId 规格ID
     * @param specData 规格数据（Map格式）
     * @return 更新结果
     */
    Result<Boolean> updateSpecification(String specId, Map<String, Object> specData);
    
    /**
     * 删除茶叶规格
     * 路径: DELETE /tea/specifications/{specId}
     * 成功码: 3012, 失败码: 3118
     *
     * @param specId 规格ID
     * @return 删除结果
     */
    Result<Boolean> deleteSpecification(String specId);
    
    /**
     * 设置默认规格
     * 路径: PUT /tea/specifications/{specId}/default
     * 成功码: 3013, 失败码: 3119
     *
     * @param specId 规格ID
     * @return 设置结果
     */
    Result<Boolean> setDefaultSpecification(String specId);
    
    /**
     * 上传茶叶图片
     *
     * @param teaId 茶叶ID
     * @param files 图片文件数组
     * @return 上传结果
     */
    Result<Object> uploadTeaImages(String teaId, MultipartFile[] files);
    
    /**
     * 删除茶叶图片
     * 路径: DELETE /tea/images/{imageId}
     * 成功码: 3015, 失败码: 3123
     *
     * @param imageId 图片ID
     * @return 删除结果
     */
    Result<Boolean> deleteTeaImage(String imageId);
    
    /**
     * 设置主图
     * 路径: PUT /tea/images/{imageId}/main
     * 成功码: 3016, 失败码: 3124
     *
     * @param imageId 图片ID
     * @return 设置结果
     */
    Result<Boolean> setMainImage(String imageId);
    
    /**
     * 更新图片顺序
     * 路径: PUT /tea/images/order
     * 成功码: 3017, 失败码: 3125
     *
     * @param orderData 包含teaId和orders数组的数据
     * @return 更新结果
     */
    Result<Boolean> updateImageOrder(Map<String, Object> orderData);
    
    /**
     * 切换茶叶状态（上架/下架）
     * 路径: PUT /tea/{teaId}/status
     * 成功码: 3018(上架), 3019(下架), 失败码: 3126, 3127
     *
     * @param teaId 茶叶ID
     * @param statusData 状态数据（Map格式）
     * @return 切换结果
     */
    Result<Boolean> toggleTeaStatus(String teaId, Map<String, Object> statusData);
    
    /**
     * 批量切换茶叶状态（上架/下架）
     * 路径: PUT /tea/batch-status
     * 成功码: 3020(批量上架), 3021(批量下架), 失败码: 3128, 3129
     *
     * @param batchData 批量数据（Map格式）
     * @return 批量切换结果
     */
    Result<Boolean> batchToggleTeaStatus(Map<String, Object> batchData);
} 