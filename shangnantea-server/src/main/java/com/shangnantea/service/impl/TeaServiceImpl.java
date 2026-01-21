package com.shangnantea.service.impl;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.common.api.Result;
import com.shangnantea.mapper.TeaCategoryMapper;
import com.shangnantea.mapper.TeaImageMapper;
import com.shangnantea.mapper.TeaMapper;
import com.shangnantea.mapper.TeaSpecificationMapper;
import com.shangnantea.model.entity.tea.Tea;
import com.shangnantea.model.entity.tea.TeaCategory;
import com.shangnantea.model.entity.tea.TeaImage;
import com.shangnantea.model.entity.tea.TeaSpecification;
import com.shangnantea.service.TeaService;
import com.shangnantea.utils.FileUploadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Override
    public Tea getTeaById(Long id) {
        // TODO: 实现获取茶叶详情的逻辑
        return teaMapper.selectById(id);
    }
    
    @Override
    public PageResult<Tea> listTeas(PageParam pageParam) {
        // TODO: 实现分页查询茶叶的逻辑
        return new PageResult<>();
    }
    
    @Override
    public PageResult<Tea> listTeasByCategory(Integer categoryId, PageParam pageParam) {
        // TODO: 实现按分类查询茶叶的逻辑
        return new PageResult<>();
    }
    
    @Override
    public Tea addTea(Tea tea) {
        // TODO: 实现添加茶叶的逻辑
        Date now = new Date();
        tea.setCreateTime(now);
        tea.setUpdateTime(now);
        teaMapper.insert(tea);
        return tea;
    }
    
    @Override
    public boolean updateTea(Tea tea) {
        // TODO: 实现更新茶叶信息的逻辑
        tea.setUpdateTime(new Date());
        return teaMapper.updateById(tea) > 0;
    }
    
    @Override
    public boolean deleteTea(Long id) {
        // TODO: 实现删除茶叶的逻辑
        return teaMapper.deleteById(id) > 0;
    }
    
    @Override
    public List<TeaCategory> listCategories() {
        // TODO: 实现获取茶叶分类的逻辑
        return teaCategoryMapper.selectAll();
    }
    
    @Override
    public List<TeaSpecification> listSpecifications(Long teaId) {
        // TODO: 实现获取茶叶规格的逻辑
        return null; // 待实现
    }
    
    @Override
    public List<TeaImage> listImages(Long teaId) {
        // TODO: 实现获取茶叶图片的逻辑
        return null; // 待实现
    }
    
    @Override
    public PageResult<Tea> searchTeas(String keyword, PageParam pageParam) {
        // TODO: 实现搜索茶叶的逻辑
        return new PageResult<>();
    }
    
    @Override
    public Result<Object> uploadTeaImages(String teaId, MultipartFile[] files) {
        try {
            logger.info("上传茶叶图片请求, teaId: {}, 文件数量: {}", teaId, files.length);
            
            // 1. 验证茶叶是否存在
            Tea tea = getTeaById(Long.valueOf(teaId));
            if (tea == null) {
                logger.warn("茶叶图片上传失败: 茶叶不存在, teaId: {}", teaId);
                return Result.failure(3121); // 茶叶图片上传失败
            }
            
            // 2. 批量上传图片并存储到tea_images表
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
                teaImage.setTeaId(Long.valueOf(teaId));
                teaImage.setImageUrl(relativePath); // 存储相对路径
                teaImage.setCreateTime(new Date());
                teaImage.setUpdateTime(new Date());
                
                // 保存到数据库
                teaImageMapper.insert(teaImage);
                
                logger.info("茶叶图片上传成功: teaId: {}, path: {}", teaId, relativePath);
            }
            
            return Result.success(3014, "茶叶图片上传成功"); // 茶叶图片上传成功
            
        } catch (Exception e) {
            logger.error("茶叶图片上传失败: 系统异常", e);
            return Result.failure(3120); // 茶叶图片上传失败
        }
    }
} 