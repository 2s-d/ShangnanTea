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
        Tea tea = teaService.getTeaById(id);
        return Result.success(tea);
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
        PageResult<Tea> pageResult = teaService.listTeas(pageParam);
        return Result.success(pageResult);
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
        PageResult<Tea> pageResult = teaService.listTeasByCategory(categoryId, pageParam);
        return Result.success(pageResult);
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
        PageResult<Tea> pageResult = teaService.searchTeas(keyword, pageParam);
        return Result.success(pageResult);
    }
    
    /**
     * 获取茶叶分类
     *
     * @return 结果
     */
    @GetMapping("/categories")
    public Result<List<TeaCategory>> listCategories() {
        List<TeaCategory> categories = teaService.listCategories();
        return Result.success(categories);
    }
    
    /**
     * 获取茶叶规格
     *
     * @param teaId 茶叶ID
     * @return 结果
     */
    @GetMapping("/{teaId}/specifications")
    public Result<List<TeaSpecification>> listSpecifications(@PathVariable Long teaId) {
        List<TeaSpecification> specifications = teaService.listSpecifications(teaId);
        return Result.success(specifications);
    }
    
    /**
     * 获取茶叶图片
     *
     * @param teaId 茶叶ID
     * @return 结果
     */
    @GetMapping("/{teaId}/images")
    public Result<List<TeaImage>> listImages(@PathVariable Long teaId) {
        List<TeaImage> images = teaService.listImages(teaId);
        return Result.success(images);
    }
    
    /**
     * 添加茶叶(管理员)
     *
     * @param tea 茶叶信息
     * @return 结果
     */
    @PostMapping
    public Result<Tea> addTea(@RequestBody Tea tea) {
        Tea saved = teaService.addTea(tea);
        return Result.success(saved);
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
        boolean success = teaService.updateTea(tea);
        return Result.success(success);
    }
    
    /**
     * 删除茶叶(管理员)
     *
     * @param id 茶叶ID
     * @return 结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteTea(@PathVariable Long id) {
        boolean success = teaService.deleteTea(id);
        return Result.success(success);
    }
} 