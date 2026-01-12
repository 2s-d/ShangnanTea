package com.shangnantea.controller;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.common.api.Result;
import com.shangnantea.model.entity.tea.Tea;
import com.shangnantea.model.entity.tea.TeaCategory;
import com.shangnantea.model.entity.tea.TeaImage;
import com.shangnantea.model.entity.tea.TeaSpecification;
import com.shangnantea.service.TeaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 茶叶控制器
 */
@RestController
@RequestMapping("/api/tea")
public class TeaController {

    @Autowired
    private TeaService teaService;
    
    /**
     * 获取茶叶详情
     *
     * @param id 茶叶ID
     * @return 结果
     */
    @GetMapping("/{id}")
    public Result<Tea> getTeaById(@PathVariable Long id) {
        Tea result = teaService.getTeaById(id);
        return Result.success(result);
    }
    
    /**
     * 获取茶叶列表
     *
     * @param page 页码
     * @param size 每页数量
     * @return 结果
     */
    @GetMapping("/list")
    public Result<PageResult<Tea>> listTeas(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<Tea> result = teaService.listTeas(pageParam);
        return Result.success(result);
    }
    
    /**
     * 按分类获取茶叶
     *
     * @param categoryId 分类ID
     * @param page 页码
     * @param size 每页数量
     * @return 结果
     */
    @GetMapping("/category/{categoryId}")
    public Result<PageResult<Tea>> listTeasByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<Tea> result = teaService.listTeasByCategory(categoryId, pageParam);
        return Result.success(result);
    }
    
    /**
     * 搜索茶叶
     *
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页数量
     * @return 结果
     */
    @GetMapping("/search")
    public Result<PageResult<Tea>> searchTeas(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<Tea> result = teaService.searchTeas(keyword, pageParam);
        return Result.success(result);
    }
    
    /**
     * 获取茶叶分类
     *
     * @return 结果
     */
    @GetMapping("/categories")
    public Result<List<TeaCategory>> listCategories() {
        List<TeaCategory> result = teaService.listCategories();
        return Result.success(result);
    }
    
    /**
     * 获取茶叶规格
     *
     * @param teaId 茶叶ID
     * @return 结果
     */
    @GetMapping("/{teaId}/specifications")
    public Result<List<TeaSpecification>> listSpecifications(@PathVariable Long teaId) {
        List<TeaSpecification> result = teaService.listSpecifications(teaId);
        return Result.success(result);
    }
    
    /**
     * 获取茶叶图片
     *
     * @param teaId 茶叶ID
     * @return 结果
     */
    @GetMapping("/{teaId}/images")
    public Result<List<TeaImage>> listImages(@PathVariable Long teaId) {
        List<TeaImage> result = teaService.listImages(teaId);
        return Result.success(result);
    }
    
    /**
     * 添加茶叶(管理员)
     *
     * @param tea 茶叶信息
     * @return 结果
     */
    @PostMapping
    public Result<Tea> addTea(@RequestBody Tea tea) {
        Tea result = teaService.addTea(tea);
        return Result.success(result);
    }
    
    /**
     * 更新茶叶(管理员)
     *
     * @param id 茶叶ID
     * @param tea 茶叶信息
     * @return 结果
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateTea(@PathVariable Long id, @RequestBody Tea tea) {
        tea.setId(id.toString());
        Boolean result = teaService.updateTea(tea);
        return Result.success(result);
    }
    
    /**
     * 删除茶叶(管理员)
     *
     * @param id 茶叶ID
     * @return 结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteTea(@PathVariable Long id) {
        Boolean result = teaService.deleteTea(id);
        return Result.success(result);
    }
    
    /**
     * 获取茶叶分类列表（管理员）
     *
     * @return 分类列表
     */
    @GetMapping("/admin/categories")
    public Result<List<TeaCategory>> listAdminCategories() {
        List<TeaCategory> result = teaService.listAdminCategories();
        return Result.success(result);
    }
    
    /**
     * 创建茶叶分类（管理员）
     *
     * @param category 分类信息
     * @return 创建结果
     */
    @PostMapping("/admin/categories")
    public Result<TeaCategory> createCategory(@RequestBody TeaCategory category) {
        TeaCategory result = teaService.createCategory(category);
        return Result.success(result);
    }
    
    /**
     * 更新茶叶分类（管理员）
     *
     * @param id 分类ID
     * @param category 分类信息
     * @return 更新结果
     */
    @PutMapping("/admin/categories/{id}")
    public Result<Boolean> updateCategory(@PathVariable Integer id, @RequestBody TeaCategory category) {
        category.setId(id);
        Boolean result = teaService.updateCategory(category);
        return Result.success(result);
    }
    
    /**
     * 删除茶叶分类（管理员）
     *
     * @param id 分类ID
     * @return 删除结果
     */
    @DeleteMapping("/admin/categories/{id}")
    public Result<Boolean> deleteCategory(@PathVariable Integer id) {
        Boolean result = teaService.deleteCategory(id);
        return Result.success(result);
    }
    
    /**
     * 获取茶叶列表（管理员）
     *
     * @param keyword 关键词
     * @param categoryId 分类ID
     * @param shopId 店铺ID
     * @param page 页码
     * @param size 每页数量
     * @return 茶叶列表
     */
    @GetMapping("/admin/list")
    public Result<PageResult<Tea>> listAdminTeas(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String shopId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<Tea> result = teaService.listAdminTeas(keyword, categoryId, shopId, pageParam);
        return Result.success(result);
    }
    
    /**
     * 批量删除茶叶（管理员）
     *
     * @param ids 茶叶ID列表
     * @return 删除结果
     */
    @DeleteMapping("/admin/batch")
    public Result<Boolean> batchDeleteTeas(@RequestBody List<Long> ids) {
        Boolean result = teaService.batchDeleteTeas(ids);
        return Result.success(result);
    }
    
    /**
     * 上传茶叶图片
     *
     * @param teaId 茶叶ID
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping("/{teaId}/images")
    public Result<TeaImage> uploadImage(@PathVariable Long teaId, 
                                         @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        TeaImage result = teaService.uploadImage(teaId, file);
        return Result.success(result);
    }
    
    /**
     * 删除茶叶图片
     *
     * @param teaId 茶叶ID
     * @param imageId 图片ID
     * @return 删除结果
     */
    @DeleteMapping("/{teaId}/images/{imageId}")
    public Result<Boolean> deleteImage(@PathVariable Long teaId, @PathVariable Long imageId) {
        Boolean result = teaService.deleteImage(teaId, imageId);
        return Result.success(result);
    }
    
    /**
     * 添加茶叶规格
     *
     * @param teaId 茶叶ID
     * @param specification 规格信息
     * @return 添加结果
     */
    @PostMapping("/{teaId}/specifications")
    public Result<TeaSpecification> addSpecification(@PathVariable Long teaId, @RequestBody TeaSpecification specification) {
        TeaSpecification result = teaService.addSpecification(teaId, specification);
        return Result.success(result);
    }
    
    /**
     * 更新茶叶规格
     *
     * @param teaId 茶叶ID
     * @param specificationId 规格ID
     * @param specification 规格信息
     * @return 更新结果
     */
    @PutMapping("/{teaId}/specifications/{specificationId}")
    public Result<Boolean> updateSpecification(@PathVariable Long teaId, 
                                                @PathVariable Long specificationId,
                                                @RequestBody TeaSpecification specification) {
        specification.setId(specificationId);
        Boolean result = teaService.updateSpecification(teaId, specification);
        return Result.success(result);
    }
    
    /**
     * 删除茶叶规格
     *
     * @param teaId 茶叶ID
     * @param specificationId 规格ID
     * @return 删除结果
     */
    @DeleteMapping("/{teaId}/specifications/{specificationId}")
    public Result<Boolean> deleteSpecification(@PathVariable Long teaId, @PathVariable Long specificationId) {
        Boolean result = teaService.deleteSpecification(teaId, specificationId);
        return Result.success(result);
    }
}
