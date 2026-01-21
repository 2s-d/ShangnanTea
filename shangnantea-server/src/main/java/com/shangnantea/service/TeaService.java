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
     * 获取茶叶详情
     *
     * @param id 茶叶ID
     * @return 茶叶信息
     */
    Tea getTeaById(Long id);
    
    /**
     * 分页查询茶叶
     *
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<Tea> listTeas(PageParam pageParam);
    
    /**
     * 按分类查询茶叶
     *
     * @param categoryId 分类ID
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<Tea> listTeasByCategory(Integer categoryId, PageParam pageParam);
    
    /**
     * 添加茶叶
     *
     * @param tea 茶叶信息
     * @return 茶叶信息
     */
    Tea addTea(Tea tea);
    
    /**
     * 更新茶叶信息
     *
     * @param tea 茶叶信息
     * @return 是否成功
     */
    boolean updateTea(Tea tea);
    
    /**
     * 删除茶叶
     *
     * @param id 茶叶ID
     * @return 是否成功
     */
    boolean deleteTea(Long id);
    
    /**
     * 获取茶叶分类
     *
     * @return 分类列表
     */
    List<TeaCategory> listCategories();
    
    /**
     * 获取茶叶规格
     *
     * @param teaId 茶叶ID
     * @return 规格列表
     */
    List<TeaSpecification> listSpecifications(Long teaId);
    
    /**
     * 获取茶叶图片
     *
     * @param teaId 茶叶ID
     * @return 图片列表
     */
    List<TeaImage> listImages(Long teaId);
    
    /**
     * 搜索茶叶
     *
     * @param keyword 关键词
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<Tea> searchTeas(String keyword, PageParam pageParam);
    
    /**
     * 上传茶叶图片
     *
     * @param teaId 茶叶ID
     * @param files 图片文件数组
     * @return 上传结果
     */
    Result<Object> uploadTeaImages(String teaId, MultipartFile[] files);
} 