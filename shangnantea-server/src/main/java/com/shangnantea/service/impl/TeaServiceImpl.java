package com.shangnantea.service.impl;

import com.shangnantea.common.api.Result;
import com.shangnantea.mapper.TeaImageMapper;
import com.shangnantea.mapper.TeaMapper;
import com.shangnantea.model.entity.tea.Tea;
import com.shangnantea.model.entity.tea.TeaImage;
import com.shangnantea.service.TeaService;
import com.shangnantea.utils.FileUploadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
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
    private TeaImageMapper teaImageMapper;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Override
    public Result<Object> uploadTeaImages(String teaId, MultipartFile[] files) {
        try {
            logger.info("上传茶叶图片请求, teaId: {}, 文件数量: {}", teaId, files.length);
            
            // 1. 验证茶叶是否存在
            Tea tea = teaMapper.selectById(Long.valueOf(teaId));
            if (tea == null) {
                logger.warn("茶叶图片上传失败: 茶叶不存在, teaId: {}", teaId);
                return Result.failure(3121);
            }
            
            // 2. 批量上传图片并存储到tea_images表
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }
                
                // 调用工具类上传
                String relativePath = FileUploadUtils.uploadImage(file, "teas");
                
                // 生成访问URL
                String accessUrl = FileUploadUtils.generateAccessUrl(relativePath, baseUrl);
                
                // 创建茶叶图片记录
                TeaImage teaImage = new TeaImage();
                teaImage.setTeaId(Long.valueOf(teaId));
                teaImage.setImageUrl(relativePath);
                teaImage.setCreateTime(new Date());
                teaImage.setUpdateTime(new Date());
                
                // 保存到数据库
                teaImageMapper.insert(teaImage);
                
                logger.info("茶叶图片上传成功: teaId: {}, path: {}", teaId, relativePath);
            }
            
            return Result.success(3014, "茶叶图片上传成功");
            
        } catch (Exception e) {
            logger.error("茶叶图片上传失败: 系统异常", e);
            return Result.failure(3120);
        }
    }
} 