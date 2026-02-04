package com.shangnantea.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.shangnantea.config.AlipayConfig;
import com.shangnantea.service.AlipayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 支付宝支付服务实现
 */
@Service
@Slf4j
public class AlipayServiceImpl implements AlipayService {
    
    @Autowired
    private AlipayConfig alipayConfig;
    
    /**
     * 获取支付宝客户端
     */
    private AlipayClient getAlipayClient() {
        return new DefaultAlipayClient(
            alipayConfig.getGatewayUrl(),
            alipayConfig.getAppId(),
            alipayConfig.getPrivateKey(),
            alipayConfig.getFormat(),
            alipayConfig.getCharset(),
            alipayConfig.getPublicKey(),
            alipayConfig.getSignType()
        );
    }
    
    @Override
    public String createPaymentForm(String orderId, String subject, String totalAmount) throws Exception {
        try {
            // 创建支付宝客户端
            AlipayClient alipayClient = getAlipayClient();
            
            // 创建支付请求
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            
            // 设置回调地址
            request.setReturnUrl(alipayConfig.getReturnUrl() + "?orderId=" + orderId);
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
            
            // 设置请求参数
            String bizContent = String.format(
                "{\"out_trade_no\":\"%s\",\"product_code\":\"FAST_INSTANT_TRADE_PAY\",\"total_amount\":\"%s\",\"subject\":\"%s\"}",
                orderId,
                totalAmount,
                subject
            );
            request.setBizContent(bizContent);
            
            // 调用SDK生成表单
            String form = alipayClient.pageExecute(request).getBody();
            
            log.info("生成支付表单成功，订单ID: {}", orderId);
            return form;
            
        } catch (AlipayApiException e) {
            log.error("生成支付表单失败，订单ID: {}, 错误: {}", orderId, e.getMessage());
            throw new Exception("生成支付表单失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean verifyNotify(Map<String, String> params) throws Exception {
        try {
            // 调用SDK验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                params,
                alipayConfig.getPublicKey(),
                alipayConfig.getCharset(),
                alipayConfig.getSignType()
            );
            
            if (signVerified) {
                log.info("支付宝回调签名验证成功");
            } else {
                log.warn("支付宝回调签名验证失败");
            }
            
            return signVerified;
            
        } catch (AlipayApiException e) {
            log.error("验证支付宝回调签名异常: {}", e.getMessage());
            throw new Exception("验证签名失败: " + e.getMessage());
        }
    }
}
