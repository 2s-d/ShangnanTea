package com.shangnantea.common.constants;

/**
 * 帖子状态常量
 */
public class PostStatus {
    
    /**
     * 待审核
     */
    public static final int PENDING = 0;
    
    /**
     * 正常（已发布）
     */
    public static final int NORMAL = 1;
    
    /**
     * 已删除
     */
    public static final int DELETED = 2;
    
    /**
     * 已拒绝（审核不通过）
     */
    public static final int REJECTED = 3;
    
    /**
     * 私有构造函数，防止实例化
     */
    private PostStatus() {
        throw new AssertionError("Cannot instantiate constants class");
    }
    
    /**
     * 判断状态是否有效
     */
    public static boolean isValid(Integer status) {
        return status != null && (status == PENDING || status == NORMAL || 
                status == DELETED || status == REJECTED);
    }
    
    /**
     * 获取状态描述
     */
    public static String getDescription(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case PENDING:
                return "待审核";
            case NORMAL:
                return "正常";
            case DELETED:
                return "已删除";
            case REJECTED:
                return "已拒绝";
            default:
                return "未知";
        }
    }
}
