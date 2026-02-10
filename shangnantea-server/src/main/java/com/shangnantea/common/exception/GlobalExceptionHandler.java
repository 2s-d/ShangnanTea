package com.shangnantea.common.exception;

import com.shangnantea.common.api.Result;
import com.shangnantea.common.api.ResultCode;
import com.shangnantea.exception.BusinessException;
import com.shangnantea.exception.ResourceNotFoundException;
import com.shangnantea.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理验证异常
     * 
     * 说明：
     * - 这里必须按「接口路径 + 请求方法」把校验失败映射到业务状态码
     * - 具体业务码参见：shangnantea-web/docs/tasks/code-message-mapping.md
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<String> handleValidationException(Exception e, HttpServletRequest request) {
        BindingResult bindingResult;
        if (e instanceof MethodArgumentNotValidException) {
            bindingResult = ((MethodArgumentNotValidException) e).getBindingResult();
        } else {
            bindingResult = ((BindException) e).getBindingResult();
        }
        
        String message = null;
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                message = fieldError.getDefaultMessage();
            }
        }
        
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // 默认使用通用的参数校验失败码（1001），再按接口覆盖
        int code = ResultCode.VALIDATE_FAILED.getCode();
        
        // 统一前缀（本项目 context-path 为 /api）
        // 用户模块 - 管理员相关接口
        if ("/api/user/admin/users".equals(uri) && "POST".equalsIgnoreCase(method)) {
            // 接口：createAdmin - /user/admin/users
            // 失败码：2134（创建管理员失败）
            code = 2134;
        } else if (uri != null && uri.startsWith("/api/user/admin/users/") 
                && uri.endsWith("/status") 
                && "PUT".equalsIgnoreCase(method)) {
            // 接口：toggleUserStatus - /user/admin/users/{userId}/status
            // 失败码：2140（切换用户状态失败）
            code = 2140;
        } else if (uri != null && uri.startsWith("/api/user/admin/users/") 
                && "PUT".equalsIgnoreCase(method)) {
            // 接口：updateUser - /user/admin/users/{userId}
            // 失败码：2136（更新用户失败）
            code = 2136;
        } else if (uri != null && uri.startsWith("/api/user/admin/certifications/") 
                && "PUT".equalsIgnoreCase(method)) {
            // 接口：processCertification - /user/admin/certifications/{id}
            // 失败码：2144（审核认证失败）
            code = 2144;
        } else if ("/api/user/verification-code/send".equals(uri) 
                && "POST".equalsIgnoreCase(method)) {
            // 接口：sendVerificationCode - /user/verification-code/send
            // 参数格式不对时，视为发送失败（2149）
            code = 2149;
        } else if ("/api/user/register".equals(uri) 
                && "POST".equalsIgnoreCase(method)) {
            // 接口：register - /user/register
            // 任意参数校验失败都视为注册失败（2102）
            code = 2102;
        }
        
        LOGGER.warn("参数校验失败: {}, uri={}, method={}", message, uri, method);
        // 仅返回 code，message 走前端映射
        return Result.failure(code);
    }
    
    /**
     * 处理权限异常
     */
    @ExceptionHandler(UnauthorizedException.class)
    public Result<String> handleUnauthorizedException(UnauthorizedException e, HttpServletRequest request) {
        LOGGER.warn("权限异常: {}, uri={}, method={}", 
                e.getMessage(), request.getRequestURI(), request.getMethod());
        return Result.failure(e.getResultCode());
    }
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException(BusinessException e, HttpServletRequest request) {
        LOGGER.warn("业务异常: {}, uri={}, method={}", 
                e.getMessage(), request.getRequestURI(), request.getMethod());
        return Result.failure(e.getResultCode());
    }
    
    /**
     * 处理资源不存在异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public Result<String> handleResourceNotFoundException(ResourceNotFoundException e, HttpServletRequest request) {
        LOGGER.warn("资源不存在: {}, uri={}, method={}", 
                e.getMessage(), request.getRequestURI(), request.getMethod());
        return Result.failure(ResultCode.NOT_FOUND);
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e, HttpServletRequest request) {
        LOGGER.error("系统异常, uri={}, method={}", 
                request.getRequestURI(), request.getMethod(), e);
        return Result.failure(ResultCode.INTERNAL_SERVER_ERROR);
    }
} 