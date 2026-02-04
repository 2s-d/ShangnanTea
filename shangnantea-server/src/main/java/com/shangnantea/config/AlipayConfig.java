package com.shangnantea.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝配置类
 */
@Configuration
@ConfigurationProperties(prefix = "alipay")
@Data
public class AlipayConfig {
    
    /**
     * 应用ID
     */
    private String appId;
    
    /**
     * 商户私钥
     */
    private String privateKey;
    
    /**
     * 支付宝公钥
     */
    private String publicKey;
    
    /**
     * 支付宝网关地址
     */
    private String gatewayUrl;
    
    /**
     * 签名类型
     */
    private String signType;
    
    /**
     * 字符编码
     */
    private String charset;
    
    /**
     * 返回格式
     */
    private String format;
    
    /**
     * 同步回调地址
     */
    private String returnUrl;
    
    /**
     * 异步回调地址
     */
    private String notifyUrl;
}
