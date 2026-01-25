package com.shangnantea.service;

import com.shangnantea.common.api.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 茶叶服务接口
 */
public interface TeaService {
    
    // ==================== 茶叶查询 ====================
    
    /**
     * 获取茶叶列表
     */
    Result<Object> getTeas(Map<String, Object> params);
    
    /**
     * 获取茶叶详情
     */
    Result<Object> getTeaDetail(String id);
    
    /**
     * 获取推荐茶叶
     */
    Result<Object> getRecommendTeas(Map<String, Object> params);
    
    /**
     * 获取茶叶分类
     */
    Result<Object> getTeaCategories();
    
    /**
     * 获取茶叶评价列表
     */
    Result<Object> getTeaReviews(String teaId, Map<String, Object> params);
    
    /**
     * 获取茶叶评价统计
     */
    Result<Object> getReviewStats(String teaId);
    
    /**
     * 获取茶叶规格列表
     */
    Result<Object> getTeaSpecifications(String teaId);
    
    // ==================== 分类管理 ====================
    
    /**
     * 创建茶叶分类
     */
    Result<Object> createCategory(Map<String, Object> categoryData);
    
    /**
     * 更新茶叶分类
     */
    Result<Boolean> updateCategory(Integer id, Map<String, Object> categoryData);
    
    /**
     * 删除茶叶分类
     */
    Result<Boolean> deleteCategory(Integer id);
    
    // ==================== 茶叶管理 ====================
    
    /**
     * 添加茶叶
     */
    Result<Object> addTea(Map<String, Object> teaData);
    
    /**
     * 更新茶叶信息
     */
    Result<Boolean> updateTea(String id, Map<String, Object> teaData);
    
    /**
     * 删除茶叶
     */
    Result<Boolean> deleteTea(String id);
    
    /**
     * 更新茶叶状态
     */
    Result<Boolean> toggleTeaStatus(String teaId, Map<String, Object> statusData);
    
    /**
     * 批量更新茶叶状态
     */
    Result<Boolean> batchToggleTeaStatus(Map<String, Object> batchData);
    
    // ==================== 评价系统 ====================
    
    /**
     * 提交评价
     */
    Result<Boolean> submitReview(Map<String, Object> reviewData);
    
    /**
     * 商家回复评价
     */
    Result<Boolean> replyReview(String reviewId, Map<String, Object> replyData);
    
    /**
     * 点赞评价
     */
    Result<Boolean> likeReview(String reviewId);
    
    // ==================== 规格管理 ====================
    
    /**
     * 添加规格
     */
    Result<Object> addSpecification(String teaId, Map<String, Object> specData);
    
    /**
     * 更新规格
     */
    Result<Boolean> updateSpecification(String specId, Map<String, Object> specData);
    
    /**
     * 删除规格
     */
    Result<Boolean> deleteSpecification(String specId);
    
    /**
     * 设置默认规格
     */
    Result<Boolean> setDefaultSpecification(String specId);
    
    // ==================== 图片管理 ====================
    
    /**
     * 上传茶叶图片
     */
    Result<Object> uploadTeaImages(String teaId, MultipartFile[] files);
    
    /**
     * 删除茶叶图片
     */
    Result<Boolean> deleteTeaImage(String imageId);
    
    /**
     * 更新图片顺序
     */
    Result<Boolean> updateImageOrder(Map<String, Object> orderData);
    
    /**
     * 设置主图
     */
    Result<Boolean> setMainImage(String imageId);
} 