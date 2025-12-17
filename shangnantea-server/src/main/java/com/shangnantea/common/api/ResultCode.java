package com.shangnantea.common.api;

/**
 * 结果码枚举
 */
public enum ResultCode {
    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),
    
    /**
     * 失败
     */
    FAILURE(400, "操作失败"),
    
    /**
     * 错误请求
     */
    BAD_REQUEST(400, "请求参数错误"),
    
    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未登录或登录已过期"),
    
    /**
     * 拒绝访问
     */
    FORBIDDEN(403, "没有访问权限"),
    
    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),
    
    /**
     * 请求方法不允许
     */
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    
    /**
     * 服务器内部错误
     */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    
    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    
    /**
     * 参数验证错误
     */
    VALIDATE_FAILED(1001, "参数验证失败"),
    
    /**
     * 业务处理错误
     */
    BUSINESS_ERROR(1002, "业务处理失败"),
    
    /**
     * 未知错误
     */
    UNKNOWN(9999, "未知错误");
    
    /**
     * 状态码
     */
    private final int code;
    
    /**
     * 状态信息
     */
    private final String message;
    
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    /**
     * 获取结果码
     *
     * @return 结果码
     */
    public int getCode() {
        return code;
    }
    
    /**
     * 获取消息
     *
     * @return 消息
     */
    public String getMessage() {
        return message;
    }
} 