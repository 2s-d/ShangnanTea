package com.shangnantea.interceptor;

import com.shangnantea.common.api.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 响应日志拦截器
 * 记录所有接口返回的状态码
 */
@ControllerAdvice
public class ResponseLoggingAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger logger = LoggerFactory.getLogger(ResponseLoggingAdvice.class);

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 只处理Result类型的返回值
        return Result.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, 
                                 MethodParameter returnType, 
                                 MediaType selectedContentType,
                                 Class<? extends HttpMessageConverter<?>> selectedConverterType, 
                                 ServerHttpRequest request,
                                 ServerHttpResponse response) {
        if (body instanceof Result) {
            Result<?> result = (Result<?>) body;
            String uri = "";
            String method = "";
            
            if (request instanceof ServletServerHttpRequest) {
                ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                uri = servletRequest.getServletRequest().getRequestURI();
                method = servletRequest.getServletRequest().getMethod();
            }
            
            // 记录返回的状态码
            logger.info("API响应 - {} {} - 返回状态码: {}", method, uri, result.getCode());
        }
        
        return body;
    }
}
