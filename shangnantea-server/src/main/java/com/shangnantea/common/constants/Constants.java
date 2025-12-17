package com.shangnantea.common.constants;

/**
 * 系统常量类
 * 定义全局通用的常量
 */
public class Constants {

    /**
     * 系统相关常量
     */
    public static class System {
        /**
         * 项目名称
         */
        public static final String PROJECT_NAME = "商南茶文化平台";
        
        /**
         * 默认分页大小
         */
        public static final int DEFAULT_PAGE_SIZE = 10;
        
        /**
         * 最大分页大小
         */
        public static final int MAX_PAGE_SIZE = 100;
    }
    
    /**
     * 用户相关常量
     */
    public static class User {
        /**
         * 用户默认头像
         */
        public static final String DEFAULT_AVATAR = "/images/default-avatar.png";
        
        /**
         * 管理员角色ID
         */
        public static final int ROLE_ADMIN = 1;
        
        /**
         * 普通用户角色ID
         */
        public static final int ROLE_USER = 2;
        
        /**
         * 商家角色ID
         */
        public static final int ROLE_SHOP = 3;
        
        /**
         * 用户状态-正常
         */
        public static final int STATUS_NORMAL = 1;
        
        /**
         * 用户状态-禁用
         */
        public static final int STATUS_DISABLED = 0;
        
        /**
         * 用户状态-注销
         */
        public static final int STATUS_DELETED = 2;
    }
    
    /**
     * 茶叶相关常量
     */
    public static class Tea {
        /**
         * 茶叶状态-上架
         */
        public static final int STATUS_ON_SALE = 1;
        
        /**
         * 茶叶状态-下架
         */
        public static final int STATUS_OFF_SALE = 0;
        
        /**
         * 默认茶叶图片
         */
        public static final String DEFAULT_IMAGE = "/images/default-tea.png";
    }
    
    /**
     * 订单相关常量
     */
    public static class Order {
        /**
         * 订单状态-待付款
         */
        public static final int STATUS_PENDING_PAYMENT = 0;
        
        /**
         * 订单状态-待发货
         */
        public static final int STATUS_PENDING_SHIPMENT = 1;
        
        /**
         * 订单状态-待收货
         */
        public static final int STATUS_PENDING_RECEIPT = 2;
        
        /**
         * 订单状态-已完成
         */
        public static final int STATUS_COMPLETED = 3;
        
        /**
         * 订单状态-已取消
         */
        public static final int STATUS_CANCELLED = 4;
    }
    
    /**
     * 商家相关常量
     */
    public static class Shop {
        /**
         * 店铺状态-正常
         */
        public static final int STATUS_NORMAL = 1;
        
        /**
         * 店铺状态-关闭
         */
        public static final int STATUS_CLOSED = 0;
        
        /**
         * 商家认证状态-待审核
         */
        public static final int CERT_STATUS_PENDING = 0;
        
        /**
         * 商家认证状态-已通过
         */
        public static final int CERT_STATUS_APPROVED = 1;
        
        /**
         * 商家认证状态-已拒绝
         */
        public static final int CERT_STATUS_REJECTED = 2;
    }
    
    /**
     * 论坛相关常量
     */
    public static class Forum {
        /**
         * 帖子状态-正常
         */
        public static final int POST_STATUS_NORMAL = 1;
        
        /**
         * 帖子状态-待审核
         */
        public static final int POST_STATUS_PENDING = 0;
        
        /**
         * 帖子状态-已删除
         */
        public static final int POST_STATUS_DELETED = 2;
    }
    
    /**
     * 安全相关常量
     */
    public static class Security {
        /**
         * JWT token过期时间（毫秒）：24小时
         */
        public static final long JWT_EXPIRATION = 86400000;
        
        /**
         * JWT 签发者
         */
        public static final String JWT_ISSUER = "shangnantea";
        
        /**
         * JWT 存储在header中的名称
         */
        public static final String JWT_HEADER = "Authorization";
        
        /**
         * JWT token前缀
         */
        public static final String JWT_TOKEN_PREFIX = "Bearer ";
    }
    
    /**
     * 文件存储相关常量
     */
    public static class File {
        /**
         * 允许上传的图片类型
         */
        public static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg", "image/png", "image/gif", "image/webp"
        };
        
        /**
         * 最大图片上传大小（字节）：5MB
         */
        public static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
        
        /**
         * 图片上传目录
         */
        public static final String IMAGE_UPLOAD_DIR = "/uploads/images/";
    }
} 