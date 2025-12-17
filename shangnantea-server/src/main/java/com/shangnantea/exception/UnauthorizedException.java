package com.shangnantea.exception;

import com.shangnantea.common.api.ResultCode;

/**
 * 未授权异常，用于处理未登录或无权限访问的情况
 */
public class UnauthorizedException extends RuntimeException {
    
    private final ResultCode resultCode;
    
    /**
     * 构造方法
     *
     * @param resultCode 结果码
     * @param message    错误信息
     */
    public UnauthorizedException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
    
    /**
     * 构造方法
     *
     * @param resultCode 结果码
     */
    public UnauthorizedException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }
    
    /**
     * 构造方法
     *
     * @param message 错误信息
     */
    public UnauthorizedException(String message) {
        super(message);
        this.resultCode = ResultCode.UNAUTHORIZED;
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