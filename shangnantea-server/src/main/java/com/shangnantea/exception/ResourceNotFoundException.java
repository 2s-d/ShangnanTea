package com.shangnantea.exception;

import com.shangnantea.common.api.ResultCode;

/**
 * 资源未找到异常
 * 用于处理请求的资源不存在的情况，例如查询不存在的用户、商品等
 */
public class ResourceNotFoundException extends RuntimeException {
    
    private ResultCode resultCode;
    
    /**
     * 构造方法
     *
     * @param message 错误信息
     */
    public ResourceNotFoundException(String message) {
        super(message);
        this.resultCode = ResultCode.NOT_FOUND;
    }
    
    /**
     * 构造方法
     *
     * @param resourceName 资源名称
     * @param fieldName    字段名
     * @param fieldValue   字段值
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s不存在，%s: %s", resourceName, fieldName, fieldValue));
        this.resultCode = ResultCode.NOT_FOUND;
    }
    
    public ResourceNotFoundException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }
    
    public ResourceNotFoundException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
    
    /**
     * 获取结果码
     *
     * @return 结果码
     */
    public ResultCode getResultCode() {
        return resultCode;
    }
} 