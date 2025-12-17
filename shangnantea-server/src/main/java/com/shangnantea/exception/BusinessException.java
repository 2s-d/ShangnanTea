package com.shangnantea.exception;

import com.shangnantea.common.api.ResultCode;

/**
 * 业务异常，用于处理业务逻辑中的异常情况
 */
public class BusinessException extends RuntimeException {

    private final ResultCode resultCode;
    
    /**
     * 构造方法
     *
     * @param resultCode 结果码
     * @param message    错误信息
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
    
    /**
     * 构造方法
     *
     * @param resultCode 结果码
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }
    
    /**
     * 构造方法
     *
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(message);
        this.resultCode = ResultCode.FAILURE;
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