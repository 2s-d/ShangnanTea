package com.shangnantea.utils;

import com.shangnantea.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传工具类
 * 
 * 功能:
 * 1. 文件验证(类型、大小)
 * 2. 文件重命名(UUID)
 * 3. 目录管理(自动创建)
 * 4. 图片压缩处理
 * 5. 生成访问URL
 * 
 * 存储路径规则:
 * files/{资源类型}/{业务类型}/{年}/{月}/{日}/{文件名}
 * 例如: files/images/avatars/2024/01/18/abc123.jpg
 */
public class FileUploadUtils {
    
    /**
     * 上传基础路径(相对于项目根目录)
     */
    private static final String BASE_PATH = "files";
    
    /**
     * 允许的图片类型
     */
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    /**
     * 允许的图片扩展名
     */
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
        ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );
    
    /**
     * 默认最大文件大小: 5MB
     */
    private static final long DEFAULT_MAX_SIZE = 5 * 1024 * 1024;
    
    /**
     * 图片压缩阈值: 1MB
     */
    private static final long COMPRESS_THRESHOLD = 1 * 1024 * 1024;
    
    /**
     * 压缩后的目标宽度
     */
    private static final int COMPRESS_WIDTH = 1200;
    
    /**
     * 压缩质量(0.0-1.0)
     */
    private static final float COMPRESS_QUALITY = 0.85f;
    
    /**
     * 上传图片文件
     * 
     * @param file 文件对象
     * @param type 业务类型(avatars/reviews/teas/shops/banners/posts/messages)
     * @return 相对路径(用于存储到数据库)
     */
    public static String uploadImage(MultipartFile file, String type) {
        return uploadImage(file, type, DEFAULT_MAX_SIZE);
    }
    
    /**
     * 上传图片文件(指定最大大小)
     * 
     * @param file    文件对象
     * @param type    业务类型
     * @param maxSize 最大文件大小(字节)
     * @return 相对路径
     */
    public static String uploadImage(MultipartFile file, String type, long maxSize) {
        // 1. 验证文件
        validateImageFile(file, maxSize);
        
        // 2. 生成文件名
        String filename = generateFilename(file);
        
        // 3. 构建存储路径
        String relativePath = buildImagePath(type, filename);
        String fullPath = getFullPath(relativePath);
        
        // 4. 确保目录存在
        ensureDirectoryExists(fullPath);
        
        // 5. 保存文件(如果需要压缩则压缩)
        try {
            File destFile = new File(fullPath);
            
            // 判断是否需要压缩
            if (file.getSize() > COMPRESS_THRESHOLD) {
                compressAndSaveImage(file, destFile);
            } else {
                file.transferTo(destFile);
            }
            
        } catch (IOException e) {
            throw new BusinessException("文件保存失败: " + e.getMessage());
        }
        
        // 6. 返回相对路径
        return relativePath;
    }
    
    /**
     * 验证图片文件
     */
    private static void validateImageFile(MultipartFile file, long maxSize) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        // 检查文件大小
        if (file.getSize() > maxSize) {
            String maxSizeStr = StringUtils.formatFileSize(maxSize);
            throw new BusinessException("文件大小不能超过 " + maxSizeStr);
        }
        
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("不支持的文件类型,只允许上传图片(jpg/png/gif/webp)");
        }
        
        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException("文件名不能为空");
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new BusinessException("不支持的文件扩展名,只允许: " + String.join(", ", ALLOWED_IMAGE_EXTENSIONS));
        }
    }
    
    /**
     * 生成文件名(UUID + 标准化扩展名)
     */
    private static String generateFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        
        // 标准化扩展名:转小写,移除特殊字符
        extension = normalizeExtension(extension);
        
        return StringUtils.generateUUID() + extension;
    }
    
    /**
     * 获取文件扩展名
     */
    private static String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
    
    /**
     * 标准化扩展名
     * 1. 转小写
     * 2. 移除特殊字符
     * 3. 确保以点开头
     */
    private static String normalizeExtension(String extension) {
        if (StringUtils.isEmpty(extension)) {
            return ".jpg"; // 默认扩展名
        }
        
        // 转小写
        extension = extension.toLowerCase().trim();
        
        // 确保以点开头
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }
        
        // 只保留字母和点
        extension = extension.replaceAll("[^a-z.]", "");
        
        // 验证是否为允许的扩展名
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            // 根据MIME类型推断扩展名
            return ".jpg"; // 默认
        }
        
        return extension;
    }
    
    /**
     * 构建图片存储路径
     * 
     * @param type     业务类型
     * @param filename 文件名
     * @return 相对路径(统一使用正斜杠)
     */
    private static String buildImagePath(String type, String filename) {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String day = String.format("%02d", now.getDayOfMonth());
        
        // 标准化type(移除特殊字符,转小写)
        type = normalizePathSegment(type);
        
        // files/images/{type}/{year}/{month}/{day}/{filename}
        // 统一使用正斜杠,便于跨平台和URL访问
        return String.join("/", BASE_PATH, "images", type, year, month, day, filename);
    }
    
    /**
     * 标准化路径片段
     * 只保留字母、数字、下划线、连字符
     */
    private static String normalizePathSegment(String segment) {
        if (StringUtils.isEmpty(segment)) {
            return "default";
        }
        
        // 转小写,移除特殊字符
        segment = segment.toLowerCase().trim();
        segment = segment.replaceAll("[^a-z0-9_-]", "");
        
        // 如果为空,返回默认值
        if (segment.isEmpty()) {
            return "default";
        }
        
        return segment;
    }
    
    /**
     * 获取完整的文件系统路径
     */
    private static String getFullPath(String relativePath) {
        // 获取项目根目录
        String projectRoot = System.getProperty("user.dir");
        return projectRoot + File.separator + relativePath.replace("/", File.separator);
    }
    
    /**
     * 确保目录存在,不存在则创建
     */
    private static void ensureDirectoryExists(String fullPath) {
        try {
            Path path = Paths.get(fullPath);
            Path parentDir = path.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
        } catch (IOException e) {
            throw new BusinessException("创建目录失败: " + e.getMessage());
        }
    }
    
    /**
     * 压缩并保存图片
     */
    private static void compressAndSaveImage(MultipartFile file, File destFile) throws IOException {
        // 读取原始图片
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new BusinessException("无法读取图片文件");
        }
        
        // 计算压缩后的尺寸
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        int targetWidth = COMPRESS_WIDTH;
        int targetHeight = (int) (originalHeight * ((double) targetWidth / originalWidth));
        
        // 如果原图宽度小于目标宽度,则不压缩
        if (originalWidth <= targetWidth) {
            file.transferTo(destFile);
            return;
        }
        
        // 创建压缩后的图片
        BufferedImage compressedImage = new BufferedImage(
            targetWidth, 
            targetHeight, 
            BufferedImage.TYPE_INT_RGB
        );
        
        // 绘制压缩图片
        Graphics2D g = compressedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        
        // 保存压缩后的图片
        String extension = getFileExtension(file.getOriginalFilename()).substring(1);
        ImageIO.write(compressedImage, extension, destFile);
    }
    
    /**
     * 删除文件
     * 
     * @param relativePath 相对路径
     * @return 是否删除成功
     */
    public static boolean deleteFile(String relativePath) {
        if (StringUtils.isEmpty(relativePath)) {
            return false;
        }
        
        // 安全检查:防止路径遍历攻击
        if (!isValidPath(relativePath)) {
            return false;
        }
        
        try {
            String fullPath = getFullPath(relativePath);
            File file = new File(fullPath);
            if (file.exists() && file.isFile()) {
                return file.delete();
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证路径是否安全
     * 防止路径遍历攻击(如: ../../../etc/passwd)
     */
    private static boolean isValidPath(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        
        // 不允许包含..
        if (path.contains("..")) {
            return false;
        }
        
        // 不允许包含反斜杠(统一使用正斜杠)
        if (path.contains("\\")) {
            return false;
        }
        
        // 必须以BASE_PATH开头
        if (!path.startsWith(BASE_PATH + "/")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 生成访问URL
     * 
     * @param relativePath 相对路径
     * @param baseUrl      基础URL(如: http://localhost:8080)
     * @return 完整访问URL
     */
    public static String generateAccessUrl(String relativePath, String baseUrl) {
        if (StringUtils.isEmpty(relativePath)) {
            return null;
        }
        
        // 移除baseUrl末尾的斜杠
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        
        // 确保relativePath以斜杠开头
        if (!relativePath.startsWith("/")) {
            relativePath = "/" + relativePath;
        }
        
        return baseUrl + relativePath;
    }
}
