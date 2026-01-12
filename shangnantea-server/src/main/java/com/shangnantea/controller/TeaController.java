package com.shangnantea.controller;

import com.shangnantea.common.api.Result;
import com.shangnantea.security.annotation.RequiresLogin;
import com.shangnantea.security.annotation.RequiresRoles;
import com.shangnantea.service.TeaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * 茶叶控制器
 * 参考：前端 tea.js 和 code-message-mapping.md
 */
@RestController
@RequestMapping({"/tea", "/api/tea"})
@Validated
public class TeaController {

    private static final Logger logger = LoggerFactory.getLogger(TeaController.class);

    @Autowired
    private TeaService teaService;

    // ==================== 茶叶查询 ====================

    /**
     * 获取茶叶列表
     * 路径: GET /tea/list
     * 成功码: 3000, 失败码: 3100
     *
     * @param params 查询参数（keyword, categoryId, shopId, page, pageSize等）
     * @return 茶叶列表
     */
    @GetMapping("/list")
    public Result<Object> getTeas(@RequestParam Map<String, Object> params) {
        logger.info("获取茶叶列表请求, params: {}", params);
        return teaService.getTeas(params);
    }

    /**
     * 获取推荐茶叶
     * 路径: GET /tea/recommend
     * 成功码: 200, 失败码: 1102
     *
     * @param params 推荐参数 {type, teaId, count}
     * @return 推荐茶叶列表
     */
    @GetMapping("/recommend")
    public Result<Object> getRecommendTeas(@RequestParam Map<String, Object> params) {
        logger.info("获取推荐茶叶请求, params: {}", params);
        return teaService.getRecommendTeas(params);
    }

    /**
     * 获取茶叶分类
     * 路径: GET /tea/categories
     * 成功码: 200, 失败码: 1102
     *
     * @return 分类列表
     */
    @GetMapping("/categories")
    public Result<Object> getTeaCategories() {
        logger.info("获取茶叶分类请求");
        return teaService.getTeaCategories();
    }

    /**
     * 获取茶叶评价列表
     * 路径: GET /tea/{teaId}/reviews
     * 成功码: 200, 失败码: 1102
     *
     * @param teaId 茶叶ID
     * @param params 查询参数（page, pageSize）
     * @return 评价列表
     */
    @GetMapping("/{teaId}/reviews")
    public Result<Object> getTeaReviews(@PathVariable String teaId, @RequestParam Map<String, Object> params) {
        logger.info("获取茶叶评价列表请求: {}, params: {}", teaId, params);
        return teaService.getTeaReviews(teaId, params);
    }

    /**
     * 获取茶叶评价统计数据
     * 路径: GET /tea/{teaId}/reviews/stats
     * 成功码: 200, 失败码: 1102
     *
     * @param teaId 茶叶ID
     * @return 评价统计数据
     */
    @GetMapping("/{teaId}/reviews/stats")
    public Result<Object> getReviewStats(@PathVariable String teaId) {
        logger.info("获取茶叶评价统计数据请求: {}", teaId);
        return teaService.getReviewStats(teaId);
    }

    /**
     * 获取茶叶规格列表
     * 路径: GET /tea/{teaId}/specifications
     * 成功码: 200, 失败码: 1102
     *
     * @param teaId 茶叶ID
     * @return 规格列表
     */
    @GetMapping("/{teaId}/specifications")
    public Result<Object> getTeaSpecifications(@PathVariable String teaId) {
        logger.info("获取茶叶规格列表请求: {}", teaId);
        return teaService.getTeaSpecifications(teaId);
    }

    /**
     * 获取茶叶详情
     * 路径: GET /tea/{id}
     * 成功码: 3001, 失败码: 3101
     * 注意：此路径应放在最后，避免与更具体的路径冲突
     *
     * @param id 茶叶ID
     * @return 茶叶详情
     */
    @GetMapping("/{id}")
    public Result<Object> getTeaDetail(@PathVariable String id) {
        logger.info("获取茶叶详情请求: {}", id);
        return teaService.getTeaDetail(id);
    }


    // ==================== 分类管理（管理员） ====================

    /**
     * 创建茶叶分类（仅管理员）
     * 路径: POST /tea/categories
     * 成功码: 3030, 失败码: 3130
     *
     * @param categoryData 分类数据 {name, parentId, sortOrder, icon}
     * @return 创建结果
     */
    @PostMapping("/categories")
    @RequiresRoles({1}) // 管理员角色
    public Result<Object> createCategory(@RequestBody Map<String, Object> categoryData) {
        logger.info("创建茶叶分类请求");
        return teaService.createCategory(categoryData);
    }

    /**
     * 更新茶叶分类（仅管理员）
     * 路径: PUT /tea/categories/{id}
     * 成功码: 3031, 失败码: 3130
     *
     * @param id 分类ID
     * @param categoryData 分类数据 {name, parentId, sortOrder, icon}
     * @return 更新结果
     */
    @PutMapping("/categories/{id}")
    @RequiresRoles({1}) // 管理员角色
    public Result<Boolean> updateCategory(@PathVariable Integer id, @RequestBody Map<String, Object> categoryData) {
        logger.info("更新茶叶分类请求: {}", id);
        return teaService.updateCategory(id, categoryData);
    }

    /**
     * 删除茶叶分类（仅管理员）
     * 路径: DELETE /tea/categories/{id}
     * 成功码: 3032, 失败码: 3131
     *
     * @param id 分类ID
     * @return 删除结果
     */
    @DeleteMapping("/categories/{id}")
    @RequiresRoles({1}) // 管理员角色
    public Result<Boolean> deleteCategory(@PathVariable Integer id) {
        logger.info("删除茶叶分类请求: {}", id);
        return teaService.deleteCategory(id);
    }

    // ==================== 茶叶管理（管理员/商家） ====================

    /**
     * 添加茶叶（管理员/商家）
     * 路径: POST /tea/list
     * 成功码: 3026, 失败码: 3125
     *
     * @param teaData 茶叶数据
     * @return 添加结果
     */
    @PostMapping("/list")
    @RequiresLogin
    public Result<Object> addTea(@RequestBody Map<String, Object> teaData) {
        logger.info("添加茶叶请求");
        return teaService.addTea(teaData);
    }

    /**
     * 更新茶叶信息（管理员/商家）
     * 路径: PUT /tea/{id}
     * 成功码: 3025, 失败码: 3125
     *
     * @param id 茶叶ID
     * @param teaData 茶叶数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @RequiresLogin
    public Result<Boolean> updateTea(@PathVariable String id, @RequestBody Map<String, Object> teaData) {
        logger.info("更新茶叶信息请求: {}", id);
        return teaService.updateTea(id, teaData);
    }

    /**
     * 删除茶叶（管理员/商家）
     * 路径: DELETE /tea/{id}
     * 成功码: 3024, 失败码: 3124
     *
     * @param id 茶叶ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @RequiresLogin
    public Result<Boolean> deleteTea(@PathVariable String id) {
        logger.info("删除茶叶请求: {}", id);
        return teaService.deleteTea(id);
    }

    /**
     * 更新茶叶状态（上架/下架）
     * 路径: PUT /tea/{teaId}/status
     * 成功码: 3020(上架), 3021(下架), 失败码: 3120, 3121
     *
     * @param teaId 茶叶ID
     * @param statusData 状态数据 {status}
     * @return 更新结果
     */
    @PutMapping("/{teaId}/status")
    @RequiresLogin
    public Result<Boolean> toggleTeaStatus(@PathVariable String teaId, @RequestBody Map<String, Object> statusData) {
        logger.info("更新茶叶状态请求: {}, status: {}", teaId, statusData.get("status"));
        return teaService.toggleTeaStatus(teaId, statusData);
    }

    /**
     * 批量更新茶叶状态（上架/下架）
     * 路径: PUT /tea/batch-status
     * 成功码: 3022(批量上架), 3023(批量下架), 失败码: 3122, 3123
     *
     * @param batchData 批量数据 {teaIds, status}
     * @return 更新结果
     */
    @PutMapping("/batch-status")
    @RequiresLogin
    public Result<Boolean> batchToggleTeaStatus(@RequestBody Map<String, Object> batchData) {
        logger.info("批量更新茶叶状态请求");
        return teaService.batchToggleTeaStatus(batchData);
    }

    // ==================== 评价系统 ====================

    /**
     * 提交评价
     * 路径: POST /tea/reviews
     * 成功码: 200, 失败码: 1100
     *
     * @param reviewData 评价数据 {teaId, rating, content, images, orderId}
     * @return 提交结果
     */
    @PostMapping("/reviews")
    @RequiresLogin
    public Result<Boolean> submitReview(@RequestBody Map<String, Object> reviewData) {
        logger.info("提交评价请求");
        return teaService.submitReview(reviewData);
    }

    /**
     * 商家回复评价
     * 路径: POST /tea/reviews/{reviewId}/reply
     * 成功码: 3013, 失败码: 3112
     *
     * @param reviewId 评价ID
     * @param replyData 回复数据 {reply}
     * @return 回复结果
     */
    @PostMapping("/reviews/{reviewId}/reply")
    @RequiresLogin
    public Result<Boolean> replyReview(@PathVariable String reviewId, @RequestBody Map<String, Object> replyData) {
        logger.info("商家回复评价请求: {}", reviewId);
        return teaService.replyReview(reviewId, replyData);
    }

    /**
     * 点赞评价
     * 路径: POST /tea/reviews/{reviewId}/like
     * 成功码: 200, 失败码: 3113
     *
     * @param reviewId 评价ID
     * @return 点赞结果
     */
    @PostMapping("/reviews/{reviewId}/like")
    @RequiresLogin
    public Result<Boolean> likeReview(@PathVariable String reviewId) {
        logger.info("点赞评价请求: {}", reviewId);
        return teaService.likeReview(reviewId);
    }

    // ==================== 规格管理 ====================

    /**
     * 添加规格
     * 路径: POST /tea/{teaId}/specifications
     * 成功码: 1002, 失败码: 1100
     *
     * @param teaId 茶叶ID
     * @param specData 规格数据 {spec_name, price, stock, is_default}
     * @return 添加结果
     */
    @PostMapping("/{teaId}/specifications")
    @RequiresLogin
    public Result<Object> addSpecification(@PathVariable String teaId, @RequestBody Map<String, Object> specData) {
        logger.info("添加规格请求: {}", teaId);
        return teaService.addSpecification(teaId, specData);
    }

    /**
     * 更新规格
     * 路径: PUT /tea/specifications/{specId}
     * 成功码: 1004, 失败码: 1100
     *
     * @param specId 规格ID
     * @param specData 规格数据 {spec_name, price, stock, is_default}
     * @return 更新结果
     */
    @PutMapping("/specifications/{specId}")
    @RequiresLogin
    public Result<Boolean> updateSpecification(@PathVariable String specId, @RequestBody Map<String, Object> specData) {
        logger.info("更新规格请求: {}", specId);
        return teaService.updateSpecification(specId, specData);
    }

    /**
     * 删除规格
     * 路径: DELETE /tea/specifications/{specId}
     * 成功码: 1003, 失败码: 1100
     *
     * @param specId 规格ID
     * @return 删除结果
     */
    @DeleteMapping("/specifications/{specId}")
    @RequiresLogin
    public Result<Boolean> deleteSpecification(@PathVariable String specId) {
        logger.info("删除规格请求: {}", specId);
        return teaService.deleteSpecification(specId);
    }

    /**
     * 设置默认规格
     * 路径: PUT /tea/specifications/{specId}/default
     * 成功码: 1004, 失败码: 1100
     *
     * @param specId 规格ID
     * @return 设置结果
     */
    @PutMapping("/specifications/{specId}/default")
    @RequiresLogin
    public Result<Boolean> setDefaultSpecification(@PathVariable String specId) {
        logger.info("设置默认规格请求: {}", specId);
        return teaService.setDefaultSpecification(specId);
    }

    // ==================== 图片管理 ====================

    /**
     * 上传茶叶图片
     * 路径: POST /tea/{teaId}/images
     * 成功码: 1001, 失败码: 1101, 1103, 1104
     *
     * @param teaId 茶叶ID
     * @param files 图片文件（支持多文件上传）
     * @return 上传结果（包含图片列表）
     */
    @PostMapping("/{teaId}/images")
    @RequiresLogin
    public Result<Object> uploadTeaImages(@PathVariable String teaId, 
                                          @RequestParam("files") MultipartFile[] files) {
        logger.info("上传茶叶图片请求: {}, 文件数量: {}", teaId, files.length);
        return teaService.uploadTeaImages(teaId, files);
    }

    /**
     * 删除茶叶图片
     * 路径: DELETE /tea/images/{imageId}
     * 成功码: 1003, 失败码: 1100
     *
     * @param imageId 图片ID
     * @return 删除结果
     */
    @DeleteMapping("/images/{imageId}")
    @RequiresLogin
    public Result<Boolean> deleteTeaImage(@PathVariable String imageId) {
        logger.info("删除茶叶图片请求: {}", imageId);
        return teaService.deleteTeaImage(imageId);
    }

    /**
     * 更新图片顺序
     * 路径: PUT /tea/images/order
     * 成功码: 1004, 失败码: 1100
     *
     * @param orderData 包含teaId和orders数组的数据
     * @return 更新结果
     */
    @PutMapping("/images/order")
    @RequiresLogin
    public Result<Boolean> updateImageOrder(@RequestBody Map<String, Object> orderData) {
        logger.info("更新图片顺序请求");
        return teaService.updateImageOrder(orderData);
    }

    /**
     * 设置主图
     * 路径: PUT /tea/images/{imageId}/main
     * 成功码: 1004, 失败码: 1100
     *
     * @param imageId 图片ID
     * @return 设置结果
     */
    @PutMapping("/images/{imageId}/main")
    @RequiresLogin
    public Result<Boolean> setMainImage(@PathVariable String imageId) {
        logger.info("设置主图请求: {}", imageId);
        return teaService.setMainImage(imageId);
    }
}
