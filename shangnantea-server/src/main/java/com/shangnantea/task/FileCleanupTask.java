package com.shangnantea.task;

import com.shangnantea.utils.FileUploadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 文件清理定时任务
 * 
 * 功能:
 * 1. 清理临时文件
 * 2. 清理孤儿文件(数据库中没有引用的文件)
 * 3. 清理过期文件
 * 
 * 执行时间: 每天凌晨3点执行
 */
@Component
public class FileCleanupTask {
    
    private static final Logger logger = LoggerFactory.getLogger(FileCleanupTask.class);
    
    /**
     * 文件保留天数(超过此天数且未被引用的文件将被删除)
     */
    private static final int RETENTION_DAYS = 7;
    
    /**
     * 定时清理任务
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOrphanFiles() {
        logger.info("开始执行文件清理任务...");
        
        try {
            // 获取文件存储根目录
            String projectRoot = System.getProperty("user.dir");
            String filesPath = projectRoot + File.separator + "files";
            
            File filesDir = new File(filesPath);
            if (!filesDir.exists() || !filesDir.isDirectory()) {
                logger.warn("文件存储目录不存在: {}", filesPath);
                return;
            }
            
            // 统计信息
            int totalFiles = 0;
            int deletedFiles = 0;
            long freedSpace = 0;
            
            // 遍历所有文件
            List<File> filesToCheck = new ArrayList<>();
            collectFiles(filesDir, filesToCheck);
            totalFiles = filesToCheck.size();
            
            logger.info("共扫描到 {} 个文件", totalFiles);
            
            // 检查并删除过期文件
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(RETENTION_DAYS);
            
            for (File file : filesToCheck) {
                try {
                    // 获取文件最后修改时间
                    long lastModified = file.lastModified();
                    LocalDateTime fileTime = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(lastModified),
                        ZoneId.systemDefault()
                    );
                    
                    // 如果文件超过保留期限
                    if (fileTime.isBefore(cutoffTime)) {
                        // TODO: 这里应该检查数据库中是否还有引用
                        // 为了安全起见,暂时只记录日志,不实际删除
                        logger.debug("发现过期文件: {} (最后修改: {})", 
                            file.getAbsolutePath(), fileTime);
                        
                        // 如果确认要删除,取消下面的注释
                        /*
                        long fileSize = file.length();
                        if (file.delete()) {
                            deletedFiles++;
                            freedSpace += fileSize;
                            logger.info("已删除过期文件: {}", file.getAbsolutePath());
                        }
                        */
                    }
                } catch (Exception e) {
                    logger.error("处理文件失败: {}", file.getAbsolutePath(), e);
                }
            }
            
            logger.info("文件清理任务完成. 扫描: {}, 删除: {}, 释放空间: {} MB", 
                totalFiles, deletedFiles, freedSpace / (1024 * 1024));
            
        } catch (Exception e) {
            logger.error("文件清理任务执行失败", e);
        }
    }
    
    /**
     * 递归收集所有文件
     */
    private void collectFiles(File dir, List<File> fileList) {
        if (dir == null || !dir.exists()) {
            return;
        }
        
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                collectFiles(file, fileList);
            } else if (file.isFile()) {
                fileList.add(file);
            }
        }
    }
    
    /**
     * 清理空目录
     * 每周日凌晨4点执行
     */
    @Scheduled(cron = "0 0 4 ? * SUN")
    public void cleanupEmptyDirectories() {
        logger.info("开始清理空目录...");
        
        try {
            String projectRoot = System.getProperty("user.dir");
            String filesPath = projectRoot + File.separator + "files";
            
            File filesDir = new File(filesPath);
            if (!filesDir.exists()) {
                return;
            }
            
            int deletedDirs = deleteEmptyDirectories(filesDir);
            logger.info("清理空目录完成, 共删除 {} 个空目录", deletedDirs);
            
        } catch (Exception e) {
            logger.error("清理空目录失败", e);
        }
    }
    
    /**
     * 递归删除空目录
     */
    private int deleteEmptyDirectories(File dir) {
        int count = 0;
        
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return count;
        }
        
        File[] files = dir.listFiles();
        if (files == null) {
            return count;
        }
        
        // 先递归处理子目录
        for (File file : files) {
            if (file.isDirectory()) {
                count += deleteEmptyDirectories(file);
            }
        }
        
        // 再次检查当前目录是否为空
        files = dir.listFiles();
        if (files != null && files.length == 0) {
            // 不删除根目录
            if (!dir.getAbsolutePath().endsWith("files")) {
                if (dir.delete()) {
                    count++;
                    logger.debug("已删除空目录: {}", dir.getAbsolutePath());
                }
            }
        }
        
        return count;
    }
}
