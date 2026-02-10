package com.shangnantea.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 通用结果封装类（精简为 code + data，message 为空时不返回）
 *
 * @param <T> 数据类型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {
    /**
     * 状态码
     */
    private int code;
    
    /**
     * 消息（默认不返回，保留字段便于内部使用）
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
     * 成功结果（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), null, null);
    }
    
    /**
     * 成功结果（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), null, data);
    }
    
    /**
     * 成功结果（自定义状态码 + 数据）
     */
    public static <T> Result<T> success(int code, T data) {
        return new Result<>(code, null, data);
    }
    
    /**
     * 成功结果（自定义状态码，无数据）
     */
    public static <T> Result<T> success(int code) {
        return new Result<>(code, null, null);
    }
    
    /**
     * 失败结果（使用通用失败码）
     */
    public static <T> Result<T> failure() {
        return new Result<>(ResultCode.FAILURE.getCode(), null, null);
    }
    
    /**
     * 失败结果（自定义状态码）
     */
    public static <T> Result<T> failure(int code) {
        return new Result<>(code, null, null);
    }
    
    /**
     * 失败结果（自定义状态码与数据）
     */
    public static <T> Result<T> failure(int code, T data) {
        return new Result<>(code, null, data);
    }
    
    /**
     * 失败结果（使用枚举状态码）
     */
    public static <T> Result<T> failure(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), null, null);
    }
    
    /**
     * 失败结果（使用枚举状态码 + 数据）
     */
    public static <T> Result<T> failure(ResultCode resultCode, T data) {
        return new Result<>(resultCode.getCode(), null, data);
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