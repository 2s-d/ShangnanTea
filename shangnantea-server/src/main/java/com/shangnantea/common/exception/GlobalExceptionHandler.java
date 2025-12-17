package com.shangnantea.common.exception;

import com.shangnantea.common.api.Result;
import com.shangnantea.common.api.ResultCode;
import com.shangnantea.exception.BusinessException;
import com.shangnantea.exception.ResourceNotFoundException;
import com.shangnantea.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<String> handleValidationException(Exception e) {
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
        return Result.failure(ResultCode.VALIDATE_FAILED, message == null ? "参数校验失败" : message);
    }
    
    /**
     * 处理权限异常
     */
    @ExceptionHandler(UnauthorizedException.class)
    public Result<String> handleUnauthorizedException(UnauthorizedException e) {
        LOGGER.warn("权限异常: {}", e.getMessage());
        return Result.failure(e.getResultCode(), e.getMessage());
    }
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException(BusinessException e) {
        LOGGER.warn("业务异常: {}", e.getMessage());
        return Result.failure(e.getResultCode(), e.getMessage());
    }
    
    /**
     * 处理资源不存在异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public Result<String> handleResourceNotFoundException(ResourceNotFoundException e) {
        LOGGER.warn("资源不存在: {}", e.getMessage());
        return Result.failure(ResultCode.NOT_FOUND, e.getMessage());
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        LOGGER.error("系统异常: ", e);
        return Result.failure("系统错误，请联系管理员");
    }
} 