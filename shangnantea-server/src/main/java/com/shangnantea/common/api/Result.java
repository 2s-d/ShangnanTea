package com.shangnantea.common.api;

/**
 * 通用结果封装类
 *
 * @param <T> 数据类型
 */
public class Result<T> {
    /**
     * 状态码
     */
    private int code;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 数据
     */
    private T data;
    
    public Result() {
    }
    
    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    /**
     * 成功结果
     *
     * @param <T> 数据类型
     * @return 结果对象
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }
    
    /**
     * 成功结果（带数据）
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 结果对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }
    
    /**
     * 成功结果（带消息和数据）
     *
     * @param message 消息
     * @param data    数据
     * @param <T>     数据类型
     * @return 结果对象
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }
    
    /**
     * 失败结果
     *
     * @param <T> 数据类型
     * @return 结果对象
     */
    public static <T> Result<T> failure() {
        return new Result<>(ResultCode.FAILURE.getCode(), ResultCode.FAILURE.getMessage(), null);
    }
    
    /**
     * 失败结果（带消息）
     *
     * @param message 消息
     * @param <T>     数据类型
     * @return 结果对象
     */
    public static <T> Result<T> failure(String message) {
        return new Result<>(ResultCode.FAILURE.getCode(), message, null);
    }
    
    /**
     * 失败结果（带状态码和消息）
     *
     * @param resultCode 状态码对象
     * @param <T>        数据类型
     * @return 结果对象
     */
    public static <T> Result<T> failure(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }
    
    /**
     * 失败结果（带状态码和自定义消息）
     *
     * @param resultCode 状态码对象
     * @param message    消息
     * @param <T>        数据类型
     * @return 结果对象
     */
    public static <T> Result<T> failure(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null);
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
} 