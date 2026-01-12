package com.shangnantea.controller;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.common.api.Result;
import com.shangnantea.model.entity.order.Order;
import com.shangnantea.model.entity.order.ShoppingCart;
import com.shangnantea.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    
    /**
     * 获取订单详情
     *
     * @param id 订单ID
     * @return 结果
     */
    @GetMapping("/{id}")
    public Result<Order> getOrderById(@PathVariable String id) {
        Order result = orderService.getOrderById(id);
        return Result.success(result);
    }
    
    /**
     * 创建订单
     *
     * @param order 订单信息
     * @return 结果
     */
    @PostMapping("/create")
    public Result<Order> createOrder(@RequestBody Order order) {
        Order result = orderService.createOrder(order);
        return Result.success(result);
    }

    /**
     * 兼容前端路径：/order/list
     *
     * @return 分页列表
     */
    @GetMapping("/list")
    public Result<PageResult<Order>> listOrdersCompat(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<Order> result = orderService.listOrdersCompat(status, pageParam);
        return Result.success(result);
    }

    /**
     * 兼容前端路径：/order/pay
     */
    @PostMapping("/pay")
    public Result<Boolean> payOrderCompat(@RequestBody(required = false) Map<String, Object> body) {
        Boolean result = orderService.payOrderCompat(body);
        return Result.success(result);
    }

    /**
     * 兼容前端路径：/order/cancel
     */
    @PostMapping("/cancel")
    public Result<Boolean> cancelOrderCompat(@RequestBody(required = false) Map<String, Object> body) {
        Boolean result = orderService.cancelOrderCompat(body);
        return Result.success(result);
    }

    /**
     * 兼容前端路径：/order/confirm
     */
    @PostMapping("/confirm")
    public Result<Boolean> confirmOrderCompat(@RequestBody(required = false) Map<String, Object> body) {
        Boolean result = orderService.confirmOrderCompat(body);
        return Result.success(result);
    }

    /**
     * 兼容前端路径：/order/review
     */
    @PostMapping("/review")
    public Result<Boolean> reviewOrderCompat(@RequestBody(required = false) Map<String, Object> body) {
        Boolean result = orderService.reviewOrderCompat(body);
        return Result.success(result);
    }

    /**
     * 兼容前端路径：/order/refund
     */
    @PostMapping("/refund")
    public Result<Boolean> refundOrderCompat(@RequestBody(required = false) Map<String, Object> body) {
        Boolean result = orderService.refundOrderCompat(body);
        return Result.success(result);
    }
    
    /**
     * 更新订单状态
     *
     * @param id 订单ID
     * @param status 状态
     * @return 结果
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updateOrderStatus(
            @PathVariable String id,
            @RequestParam Integer status) {
        Boolean result = orderService.updateOrderStatus(id, status);
        return Result.success(result);
    }
    
    /**
     * 取消订单
     *
     * @param id 订单ID
     * @param cancelReason 取消原因
     * @return 结果
     */
    @PostMapping("/{id}/cancel")
    public Result<Boolean> cancelOrder(
            @PathVariable String id,
            @RequestParam String cancelReason) {
        Boolean result = orderService.cancelOrder(id, cancelReason);
        return Result.success(result);
    }
    
    /**
     * 确认收货
     *
     * @param id 订单ID
     * @return 结果
     */
    @PostMapping("/{id}/complete")
    public Result<Boolean> completeOrder(@PathVariable String id) {
        Boolean result = orderService.completeOrder(id);
        return Result.success(result);
    }
    
    /**
     * 发货(商家)
     *
     * @param id 订单ID
     * @param logisticsCompany 物流公司
     * @param logisticsNumber 物流单号
     * @return 结果
     */
    @PostMapping("/{id}/ship")
    public Result<Boolean> shipOrder(
            @PathVariable String id,
            @RequestParam String logisticsCompany,
            @RequestParam String logisticsNumber) {
        Boolean result = orderService.shipOrder(id, logisticsCompany, logisticsNumber);
        return Result.success(result);
    }
    
    /**
     * 查询我的订单
     *
     * @param status 状态
     * @param page 页码
     * @param size 每页数量
     * @return 结果
     */
    @GetMapping("/my")
    public Result<PageResult<Order>> listMyOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<Order> result = orderService.listMyOrders(status, pageParam);
        return Result.success(result);
    }
    
    /**
     * 查询店铺订单(商家)
     *
     * @param status 状态
     * @param page 页码
     * @param size 每页数量
     * @return 结果
     */
    @GetMapping("/shop")
    public Result<PageResult<Order>> listShopOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<Order> result = orderService.listShopOrders(status, pageParam);
        return Result.success(result);
    }
    
    /**
     * 查询所有订单(管理员)
     *
     * @param status 状态
     * @param page 页码
     * @param size 每页数量
     * @return 结果
     */
    @GetMapping("/admin/list")
    public Result<PageResult<Order>> listAllOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<Order> result = orderService.listAllOrders(status, pageParam);
        return Result.success(result);
    }
    
    /**
     * 获取购物车
     *
     * @return 结果
     */
    @GetMapping("/cart")
    public Result<List<ShoppingCart>> getCart() {
        List<ShoppingCart> result = orderService.getCart();
        return Result.success(result);
    }
    
    /**
     * 添加购物车
     *
     * @param cart 购物车信息
     * @return 结果
     */
    @PostMapping("/cart")
    public Result<ShoppingCart> addToCart(@RequestBody ShoppingCart cart) {
        ShoppingCart result = orderService.addToCart(cart);
        return Result.success(result);
    }

    /**
     * 兼容前端路径：/order/cart/add
     */
    @PostMapping("/cart/add")
    public Result<ShoppingCart> addToCartCompat(@RequestBody(required = false) ShoppingCart cart) {
        ShoppingCart result = orderService.addToCartCompat(cart);
        return Result.success(result);
    }

    /**
     * 兼容前端路径：/order/cart/update
     */
    @PutMapping("/cart/update")
    public Result<Boolean> updateCartCompat(@RequestBody(required = false) ShoppingCart cart) {
        Boolean result = orderService.updateCartCompat(cart);
        return Result.success(result);
    }

    /**
     * 兼容前端路径：/order/cart/remove
     */
    @DeleteMapping("/cart/remove")
    public Result<Boolean> removeFromCartCompat(@RequestParam(required = false) Integer id) {
        Boolean result = orderService.removeFromCartCompat(id);
        return Result.success(result);
    }

    /**
     * 更新购物车
     *
     * @param id 购物车ID
     * @param cart 购物车信息
     * @return 结果
     */
    @PutMapping("/cart/{id}")
    public Result<Boolean> updateCart(@PathVariable Integer id, @RequestBody ShoppingCart cart) {
        cart.setId(id);
        Boolean result = orderService.updateCart(cart);
        return Result.success(result);
    }
    
    /**
     * 删除购物车
     *
     * @param id 购物车ID
     * @return 结果
     */
    @DeleteMapping("/cart/{id}")
    public Result<Boolean> removeFromCart(@PathVariable Integer id) {
        Boolean result = orderService.removeFromCart(id);
        return Result.success(result);
    }
    
    /**
     * 清空购物车
     *
     * @return 结果
     */
    @DeleteMapping("/cart/clear")
    public Result<Boolean> clearCart() {
        Boolean result = orderService.clearCart();
        return Result.success(result);
    }
    
    /**
     * 申请退款
     *
     * @param id 订单ID
     * @param body 退款信息
     * @return 申请结果
     */
    @PostMapping("/{id}/refund")
    public Result<Boolean> applyRefund(@PathVariable String id, @RequestBody(required = false) Map<String, Object> body) {
        Boolean result = orderService.applyRefund(id, body);
        return Result.success(result);
    }
    
    /**
     * 处理退款申请（商家/管理员）
     *
     * @param id 订单ID
     * @param body 处理信息
     * @return 处理结果
     */
    @PutMapping("/{id}/refund")
    public Result<Boolean> processRefund(@PathVariable String id, @RequestBody(required = false) Map<String, Object> body) {
        Boolean result = orderService.processRefund(id, body);
        return Result.success(result);
    }
    
    /**
     * 获取退款详情
     *
     * @param id 订单ID
     * @return 退款详情
     */
    @GetMapping("/{id}/refund")
    public Result<Map<String, Object>> getRefundDetail(@PathVariable String id) {
        Map<String, Object> result = orderService.getRefundDetail(id);
        return Result.success(result);
    }
}
