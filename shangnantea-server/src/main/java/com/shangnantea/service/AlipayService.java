package com.shangnantea.service;

import java.util.Map;

/**
 * 支付宝支付服务接口
 */
public interface AlipayService {
    
    /**
     * 生成支付表单HTML
     *
     * @param orderId 订单ID
     * @param subject 订单标题
     * @param totalAmount 订单金额
     * @return 支付表单HTML字符串
     * @throws Exception 生成失败时抛出异常
     */
    String createPaymentForm(String orderId, String subject, String totalAmount) throws Exception;
    
    /**
     * 验证支付宝回调签名
     *
     * @param params 回调参数
     * @return 验证是否通过
     * @throws Exception 验证失败时抛出异常
     */
    boolean verifyNotify(Map<String, String> params) throws Exception;

    /**
     * 主动查询支付宝交易状态
     *
     * @param outTradeNo 商户订单号（本项目中通常为支付单ID）
     * @return 查询结果（至少包含 trade_status、trade_no、total_amount）
     * @throws Exception 查询失败时抛出异常
     */
    Map<String, String> queryTrade(String outTradeNo) throws Exception;
}
