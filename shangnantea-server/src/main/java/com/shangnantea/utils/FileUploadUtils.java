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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
     * 默认最大文件大小: 20MB（与Spring Boot配置保持一致）
     * 注意：实际限制由Spring Boot的multipart.max-file-size控制
     * 这里只是业务层面的二次验证，用于提供更友好的错误信息
     */
    private static final long DEFAULT_MAX_SIZE = 20 * 1024 * 1024;
    
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
            
            // 检查文件类型，GIF和WebP动画图片不压缩（保持动画效果）
            String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
            boolean isAnimatedFormat = ".gif".equals(extension) || ".webp".equals(extension);
            
            // 判断是否需要压缩
            // GIF和WebP动画格式不压缩，直接保存原文件以保持动画效果
            if (!isAnimatedFormat && file.getSize() > COMPRESS_THRESHOLD) {
                try {
                    compressAndSaveImage(file, destFile);
                } catch (BusinessException e) {
                    // 如果压缩失败（如无法读取图片），尝试直接保存原文件
                    if (e.getMessage().contains("无法读取图片") || e.getMessage().contains("读取图片失败")) {
                        file.transferTo(destFile);
                    } else {
                        throw e;
                    }
                }
            } else {
                // 小文件或动画格式，直接保存原文件
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
     * 
     * 验证规则：
     * 1. 文件不能为空
     * 2. 文件大小不能超过限制
     * 3. ContentType必须是允许的图片类型
     * 4. 文件扩展名必须是允许的格式
     * 5. 文件名不能包含危险字符（防止路径遍历攻击）
     */
    private static void validateImageFile(MultipartFile file, long maxSize) {
        // 1. 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        // 2. 检查文件大小
        long fileSize = file.getSize();
        if (fileSize <= 0) {
            throw new BusinessException("文件大小为0，请选择有效的图片文件");
        }
        if (fileSize > maxSize) {
            String maxSizeStr = formatFileSize(maxSize);
            String actualSizeStr = formatFileSize(fileSize);
            throw new BusinessException(String.format("文件大小不能超过 %s，当前文件大小为 %s", maxSizeStr, actualSizeStr));
        }
        
        // 3. 检查文件扩展名（先检查扩展名，因为更可靠）
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }
        
        // 防止路径遍历攻击
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new BusinessException("文件名包含非法字符，请使用正常的文件名");
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (extension.isEmpty() || !ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new BusinessException("不支持的文件扩展名，只允许: " + String.join(", ", ALLOWED_IMAGE_EXTENSIONS));
        }
        
        // 4. 检查ContentType（作为辅助验证，某些浏览器可能不准确）
        String contentType = file.getContentType();
        if (contentType != null) {
            String lowerContentType = contentType.toLowerCase();
            // 允许contentType为空（某些情况下浏览器可能不提供），但如果不为空则必须匹配
            boolean contentTypeValid = false;
            for (String allowedType : ALLOWED_IMAGE_TYPES) {
                if (lowerContentType.equals(allowedType) || lowerContentType.startsWith(allowedType + "/")) {
                    contentTypeValid = true;
                    break;
                }
            }
            // 如果contentType存在但不匹配，给出警告但不阻止（因为扩展名已验证）
            // 某些特殊格式的图片可能contentType不标准，但扩展名是正确的
        }
    }
    
    /**
     * 生成文件名(时间戳 + UUID + 扩展名)
     * 格式: 20240118120530_abc123def456.jpg
     */
    private static String generateFilename(MultipartFile file) {
        // 获取时间戳
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        
        // 获取UUID
        String uuid = UUID.randomUUID().toString().replace("-", "");
        
        // 根据MIME类型确定扩展名
        String extension = getExtensionByMimeType(file.getContentType());
        
        return timestamp + "_" + uuid + extension;
    }
    
    /**
     * 根据MIME类型获取扩展名
     */
    private static String getExtensionByMimeType(String mimeType) {
        if (mimeType == null) {
            return ".jpg";
        }
        
        switch (mimeType.toLowerCase()) {
            case "image/jpeg":
            case "image/jpg":
                return ".jpg";
            case "image/png":
                return ".png";
            case "image/gif":
                return ".gif";
            case "image/webp":
                return ".webp";
            default:
                return ".jpg";
        }
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
        if (segment == null || segment.trim().isEmpty()) {
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
        BufferedImage originalImage = null;
        String filename = file.getOriginalFilename();
        long fileSize = file.getSize();
        
        try {
            // 读取原始图片
            originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                // 如果ImageIO无法读取，可能是格式问题
                String errorMsg = String.format("无法读取图片文件: filename=%s, size=%dB, contentType=%s，可能是文件格式不支持或文件已损坏", 
                        filename, fileSize, file.getContentType());
                throw new BusinessException(errorMsg);
            }
        } catch (BusinessException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            // 如果读取失败，记录详细错误信息
            String errorMsg = String.format("读取图片失败: filename=%s, size=%dB, contentType=%s, error=%s，可能是文件格式不支持", 
                    filename, fileSize, file.getContentType(), e.getMessage());
            throw new BusinessException(errorMsg);
        }
        
        // 计算压缩后的尺寸
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        int targetWidth = COMPRESS_WIDTH;
        int targetHeight = (int) (originalHeight * ((double) targetWidth / originalWidth));
        
        // 如果原图宽度小于目标宽度,则不压缩，直接保存原文件
        if (originalWidth <= targetWidth) {
            // MultipartFile.getInputStream()每次调用返回新流，可以直接保存
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
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        } finally {
            g.dispose();
        }
        
        // 保存压缩后的图片
        try {
            String extension = getFileExtension(file.getOriginalFilename());
            if (extension == null || extension.length() <= 1) {
                throw new BusinessException("无法获取文件扩展名");
            }
            String formatName = extension.substring(1).toLowerCase();
            // 确保格式名是ImageIO支持的格式
            if (!formatName.equals("jpg") && !formatName.equals("jpeg") && !formatName.equals("png")) {
                formatName = "jpg"; // 默认使用jpg格式
            }
            
            boolean written = ImageIO.write(compressedImage, formatName, destFile);
            if (!written) {
                throw new BusinessException("图片压缩后保存失败，可能是格式不支持");
            }
        } catch (Exception e) {
            // 如果压缩保存失败，尝试直接保存原文件
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException("压缩图片时出错: " + e.getMessage() + "，尝试直接保存原文件");
        }
    }
    
    /**
     * 删除文件
     * 
     * @param relativePath 相对路径
     * @return 是否删除成功
     */
    public static boolean deleteFile(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
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
        if (path == null || path.trim().isEmpty()) {
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
        if (relativePath == null || relativePath.trim().isEmpty()) {
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
    
    /**
     * 获取文件扩展名
     */
    private static String getFileExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDotIndex);
    }
    
    /**
     * 格式化文件大小
     */
    private static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
