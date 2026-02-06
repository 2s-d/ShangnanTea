package com.shangnantea.controller;

import com.shangnantea.common.api.Result;
import com.shangnantea.model.dto.order.AddToCartDTO;
import com.shangnantea.model.dto.order.UpdateCartItemDTO;
import com.shangnantea.model.vo.order.CartItemVO;
import com.shangnantea.security.annotation.RequiresLogin;
import com.shangnantea.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 订单控制器
 * 参考：前端 order.js 和 code-message-mapping.md
 */
@RestController
@RequestMapping({"/order", "/api/order"})
@Validated
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    // ==================== 购物车相关 ====================

    /**
     * 获取购物车列表
     * 路径: GET /order/cart
     * 成功码: 200, 失败码: 5100
     *
     * @return 购物车商品列表
     */
    @GetMapping("/cart")
    @RequiresLogin
    public Result<?> getCartItems() {
        logger.info("获取购物车列表请求");
        return orderService.getCartItems();
    }

    /**
     * 添加商品到购物车
     * 路径: POST /order/cart/add
     * 成功码: 5000, 失败码: 5101, 5102, 5103, 5104
     *
     * @param data 购物车商品数据 {teaId, quantity, specificationId}
     * @return 添加结果
     */
    @PostMapping("/cart/add")
    @RequiresLogin
    public Result<CartItemVO> addToCart(@RequestBody @Validated AddToCartDTO data) {
        logger.info("添加商品到购物车请求: teaId={}, quantity={}, specificationId={}", 
                    data.getTeaId(), data.getQuantity(), data.getSpecificationId());
        return orderService.addToCart(data.getTeaId(), data.getQuantity(), data.getSpecificationId());
    }

    /**
     * 更新购物车商品
     * 路径: PUT /order/cart/update
     * 成功码: 5001, 5002, 失败码: 5105, 5106, 5107
     *
     * @param data 更新数据 {id, quantity, specificationId}
     * @return 更新结果
     */
    @PutMapping("/cart/update")
    @RequiresLogin
    public Result<CartItemVO> updateCartItem(@RequestBody @Validated UpdateCartItemDTO data) {
        logger.info("更新购物车商品请求: id={}, quantity={}, specificationId={}", 
                    data.getId(), data.getQuantity(), data.getSpecificationId());
        return orderService.updateCart(data.getId(), data.getQuantity(), data.getSpecificationId());
    }

    /**
     * 移除购物车商品
     * 路径: DELETE /order/cart/remove
     * 成功码: 5003, 失败码: 5108
     *
     * @param id 购物车项ID
     * @return 移除结果
     */
    @DeleteMapping("/cart/remove")
    @RequiresLogin
    public Result<Boolean> removeFromCart(@RequestParam Integer id) {
        logger.info("移除购物车商品请求: {}", id);
        return orderService.removeFromCart(id);
    }

    /**
     * 清空购物车
     * 路径: DELETE /order/cart/clear
     * 成功码: 5004, 失败码: 5109
     *
     * @return 清空结果
     */
    @DeleteMapping("/cart/clear")
    @RequiresLogin
    public Result<Boolean> clearCart() {
        logger.info("清空购物车请求");
        return orderService.clearCart();
    }

    // ==================== 订单基础操作 ====================

    /**
     * 创建订单
     * 路径: POST /order/create
     * 成功码: 5005, 失败码: 5110, 5111, 5112
     *
     * @param data 订单数据
     * @return 创建结果
     */
    @PostMapping("/create")
    @RequiresLogin
    public Result<Object> createOrder(@RequestBody Map<String, Object> data) {
        logger.info("创建订单请求");
        return orderService.createOrder(data);
    }

    /**
     * 获取订单列表
     * 路径: GET /order/list
     * 成功码: 200, 失败码: 5113
     *
     * @param params 查询参数 {status, page, pageSize}
     * @return 订单列表
     */
    @GetMapping("/list")
    @RequiresLogin
    public Result<Map<String, Object>> getOrders(@RequestParam Map<String, Object> params) {
        logger.info("获取订单列表请求, params: {}", params);
        return orderService.getOrders(params);
    }

    /**
     * 支付订单
     * 路径: POST /order/pay
     * 成功码: 5006, 5007, 失败码: 5117, 5118, 5119, 5120
     *
     * @param data 支付数据 {orderId, paymentMethod}
     * @return 支付结果
     */
    @PostMapping("/pay")
    @RequiresLogin
    public Result<Object> payOrder(@RequestBody Map<String, Object> data) {
        logger.info("支付订单请求");
        return orderService.payOrder(data);
    }

    /**
     * 取消订单
     * 路径: POST /order/cancel
     * 成功码: 5008, 失败码: 5121, 5122, 5123
     *
     * @param data 取消数据 {id}
     * @return 取消结果
     */
    @PostMapping("/cancel")
    @RequiresLogin
    public Result<Boolean> cancelOrder(@RequestBody Map<String, Object> data) {
        logger.info("取消订单请求");
        return orderService.cancelOrder(data);
    }

    /**
     * 确认收货
     * 路径: POST /order/confirm
     * 成功码: 5009, 失败码: 5124, 5125, 5126
     *
     * @param data 确认数据 {id}
     * @return 确认结果
     */
    @PostMapping("/confirm")
    @RequiresLogin
    public Result<Boolean> confirmOrder(@RequestBody Map<String, Object> data) {
        logger.info("确认收货请求");
        return orderService.confirmOrder(data);
    }

    /**
     * 评价订单
     * 路径: POST /order/review
     * 成功码: 5010, 失败码: 5127
     *
     * @param data 评价数据
     * @return 评价结果
     */
    @PostMapping("/review")
    @RequiresLogin
    public Result<Boolean> reviewOrder(@RequestBody Map<String, Object> data) {
        logger.info("评价订单请求");
        return orderService.reviewOrder(data);
    }

    /**
     * 申请退款（兼容旧路径）
     * 路径: POST /order/refund
     * 成功码: 5011, 失败码: 5128, 5129, 5130
     *
     * @param data 退款数据 {orderId, reason}
     * @return 申请结果
     */
    @PostMapping("/refund")
    @RequiresLogin
    public Result<Boolean> refundOrder(@RequestBody Map<String, Object> data) {
        logger.info("申请退款请求");
        return orderService.refundOrder(data);
    }

    /**
     * 批量发货
     * 路径: POST /order/batch-ship
     * 成功码: 5015, 失败码: 5138, 5139
     *
     * @param data 批量发货数据 {orderIds, logisticsCompany, logisticsNumber}
     * @return 发货结果
     */
    @PostMapping("/batch-ship")
    @RequiresLogin
    public Result<Boolean> batchShipOrders(@RequestBody Map<String, Object> data) {
        logger.info("批量发货请求");
        return orderService.batchShipOrders(data);
    }

    /**
     * 获取订单统计数据
     * 路径: GET /order/statistics
     * 成功码: 200, 失败码: 5142
     *
     * @param params 查询参数 {startDate, endDate, shopId}
     * @return 订单统计数据
     */
    @GetMapping("/statistics")
    @RequiresLogin
    public Result<Object> getOrderStatistics(@RequestParam Map<String, Object> params) {
        logger.info("获取订单统计数据请求, params: {}", params);
        return orderService.getOrderStatistics(params);
    }

    /**
     * 导出订单数据
     * 路径: GET /order/export
     * 成功码: 200, 失败码: 5143
     *
     * @param params 导出参数 {format, startDate, endDate, status, shopId}
     * @return 文件流
     */
    @GetMapping("/export")
    @RequiresLogin
    public Result<Object> exportOrders(@RequestParam Map<String, Object> params) {
        logger.info("导出订单数据请求, params: {}", params);
        return orderService.exportOrders(params);
    }

    // ==================== 订单详情相关（路径参数） ====================

    /**
     * 获取退款详情
     * 路径: GET /order/{id}/refund
     * 成功码: 200, 失败码: 5133, 5134
     *
     * @param id 订单ID
     * @return 退款详情
     */
    @GetMapping("/{id}/refund")
    @RequiresLogin
    public Result<Object> getRefundDetail(@PathVariable String id) {
        logger.info("获取退款详情请求: {}", id);
        return orderService.getRefundDetail(id);
    }

    /**
     * 审批退款
     * 路径: POST /order/{id}/refund/process
     * 成功码: 5012, 5013, 失败码: 5131, 5132
     *
     * @param id 订单ID
     * @param data 审批数据 {approve, reason}
     * @return 审批结果
     */
    @PostMapping("/{id}/refund/process")
    @RequiresLogin
    public Result<Boolean> processRefund(@PathVariable String id, @RequestBody Map<String, Object> data) {
        logger.info("审批退款请求: {}", id);
        return orderService.processRefund(id, data);
    }

    /**
     * 发货（单个订单）
     * 路径: POST /order/{id}/ship
     * 成功码: 5014, 失败码: 5135, 5136, 5137
     *
     * @param id 订单ID
     * @param logisticsCompany 物流公司
     * @param logisticsNumber 物流单号
     * @return 发货结果
     */
    @PostMapping("/{id}/ship")
    @RequiresLogin
    public Result<Boolean> shipOrder(@PathVariable String id,
                                     @RequestParam String logisticsCompany,
                                     @RequestParam String logisticsNumber) {
        logger.info("发货请求: {}, 物流公司: {}, 物流单号: {}", id, logisticsCompany, logisticsNumber);
        return orderService.shipOrder(id, logisticsCompany, logisticsNumber);
    }

    /**
     * 获取订单物流信息
     * 路径: GET /order/{id}/logistics
     * 成功码: 200, 失败码: 5140, 5141
     *
     * @param id 订单ID
     * @return 物流信息
     */
    @GetMapping("/{id}/logistics")
    @RequiresLogin
    public Result<Object> getOrderLogistics(@PathVariable String id) {
        logger.info("获取订单物流信息请求: {}", id);
        return orderService.getOrderLogistics(id);
    }

    /**
     * 获取订单详情
     * 路径: GET /order/{id}
     * 成功码: 200, 失败码: 5114, 5115, 5116
     * 注意：此路径应放在最后，避免与更具体的路径冲突
     *
     * @param id 订单ID
     * @return 订单详情
     */
    @GetMapping("/{id}")
    @RequiresLogin
    public Result<Object> getOrderDetail(@PathVariable String id) {
        logger.info("获取订单详情请求: {}", id);
        return orderService.getOrderDetail(id);
    }

    /**
     * 上传订单评价图片
     * 路径: POST /order/review/image
     * 成功码: 5016, 失败码: 5144, 5145, 5146
     *
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping("/review/image")
    @RequiresLogin
    public Result<Map<String, Object>> uploadReviewImage(@RequestParam("file") MultipartFile file) {
        logger.info("上传订单评价图片请求, 文件名: {}", file.getOriginalFilename());
        return orderService.uploadReviewImage(file);
    }
    
    /**
     * 支付宝异步回调接口
     * 路径: POST /order/alipay/notify
     * 注意：此接口由支付宝服务器调用，不需要登录验证
     *
     * @param params 支付宝回调参数
     * @return "success" 或 "failure"
     */
    @PostMapping("/alipay/notify")
    public String alipayNotify(@RequestParam Map<String, String> params) {
        logger.info("收到支付宝异步回调");
        return orderService.handleAlipayNotify(params);
    }
}
