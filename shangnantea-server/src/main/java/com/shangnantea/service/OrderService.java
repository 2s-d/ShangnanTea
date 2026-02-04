package com.shangnantea.service;

import com.shangnantea.common.api.Result;
import com.shangnantea.model.vo.order.CartItemVO;
import com.shangnantea.model.vo.order.CartResponseVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService {
    
    /**
     * 创建订单
     *
     * @param data 订单数据
     * @return 创建结果
     */
    Result<Object> createOrder(Map<String, Object> data);
    
    /**
     * 添加购物车
     *
     * @param teaId 茶叶ID
     * @param quantity 数量
     * @param specificationId 规格ID
     * @return 添加结果
     */
    Result<CartItemVO> addToCart(String teaId, Integer quantity, String specificationId);
    
    /**
     * 更新购物车
     *
     * @param id 购物车项ID
     * @param quantity 数量（可选）
     * @param specificationId 规格ID（可选）
     * @return 更新结果
     */
    Result<CartItemVO> updateCart(Integer id, Integer quantity, String specificationId);
    
    /**
     * 删除购物车
     *
     * @param id 购物车ID
     * @return 删除结果
     */
    Result<Boolean> removeFromCart(Integer id);
    
    /**
     * 获取购物车列表
     *
     * @return 购物车响应，包含商品列表和总价
     */
    Result<CartResponseVO> getCartItems();
    
    /**
     * 清空购物车
     *
     * @return 清空结果
     */
    Result<Boolean> clearCart();
    
    /**
     * 上传评价图片
     *
     * @param image 图片文件
     * @return 上传结果
     */
    Result<Map<String, Object>> uploadReviewImage(MultipartFile image);
    
    /**
     * 获取订单列表
     *
     * @param params 查询参数 {status, page, pageSize}
     * @return 订单列表和总数
     */
    Result<Map<String, Object>> getOrders(Map<String, Object> params);
    
    /**
     * 获取订单详情
     *
     * @param id 订单ID
     * @return 订单详情
     */
    Result<Object> getOrderDetail(String id);
    
    /**
     * 支付订单
     *
     * @param data 支付数据 {orderId, paymentMethod}
     * @return 支付结果
     */
    Result<Object> payOrder(Map<String, Object> data);
    
    /**
     * 取消订单
     *
     * @param data 取消数据 {id}
     * @return 取消结果
     */
    Result<Boolean> cancelOrder(Map<String, Object> data);
    
    /**
     * 确认收货
     *
     * @param data 确认数据 {id}
     * @return 确认结果
     */
    Result<Boolean> confirmOrder(Map<String, Object> data);
    
    /**
     * 评价订单
     *
     * @param data 评价数据
     * @return 评价结果
     */
    Result<Boolean> reviewOrder(Map<String, Object> data);
    
    /**
     * 申请退款
     *
     * @param data 退款数据 {orderId, reason}
     * @return 申请结果
     */
    Result<Boolean> refundOrder(Map<String, Object> data);
    
    /**
     * 审批退款
     *
     * @param id 订单ID
     * @param data 审批数据 {approve, reason}
     * @return 审批结果
     */
    Result<Boolean> processRefund(String id, Map<String, Object> data);
    
    /**
     * 获取退款详情
     *
     * @param id 订单ID
     * @return 退款详情
     */
    Result<Object> getRefundDetail(String id);
    
    /**
     * 发货
     *
     * @param id 订单ID
     * @param logisticsCompany 物流公司
     * @param logisticsNumber 物流单号
     * @return 发货结果
     */
    Result<Boolean> shipOrder(String id, String logisticsCompany, String logisticsNumber);
    
    /**
     * 批量发货
     *
     * @param data 批量发货数据 {orderIds, logisticsCompany, logisticsNumber}
     * @return 发货结果
     */
    Result<Boolean> batchShipOrders(Map<String, Object> data);
    
    /**
     * 获取订单物流信息
     *
     * @param id 订单ID
     * @return 物流信息
     */
    Result<Object> getOrderLogistics(String id);
    
    /**
     * 获取订单统计数据
     *
     * @param params 查询参数 {startDate, endDate, shopId}
     * @return 订单统计数据
     */
    Result<Object> getOrderStatistics(Map<String, Object> params);
    
    /**
     * 导出订单数据
     *
     * @param params 导出参数 {format, startDate, endDate, status, shopId}
     * @return 文件流
     */
    Result<Object> exportOrders(Map<String, Object> params);
    
    /**
     * 处理支付宝异步回调
     *
     * @param params 回调参数
     * @return 处理结果（返回"success"或"failure"）
     */
    String handleAlipayNotify(Map<String, String> params);
} 