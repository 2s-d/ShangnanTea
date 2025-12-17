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
        Order order = orderService.getOrderById(id);
        return Result.success(order);
    }
    
    /**
     * 创建订单
     *
     * @param order 订单信息
     * @return 结果
     */
    @PostMapping("/create")
    public Result<Order> createOrder(@RequestBody Order order) {
        // TODO: 获取当前登录用户ID
        String userId = "currentUserId";
        order.setUserId(userId);
        
        Order createdOrder = orderService.createOrder(order);
        return Result.success(createdOrder);
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
        boolean success = orderService.updateOrderStatus(id, status);
        return Result.success(success);
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
        boolean success = orderService.cancelOrder(id, cancelReason);
        return Result.success(success);
    }
    
    /**
     * 确认收货
     *
     * @param id 订单ID
     * @return 结果
     */
    @PostMapping("/{id}/complete")
    public Result<Boolean> completeOrder(@PathVariable String id) {
        boolean success = orderService.completeOrder(id);
        return Result.success(success);
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
        boolean success = orderService.shipOrder(id, logisticsCompany, logisticsNumber);
        return Result.success(success);
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
        // TODO: 获取当前登录用户ID
        String userId = "currentUserId";
        
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<Order> pageResult = orderService.listUserOrders(userId, status, pageParam);
        return Result.success(pageResult);
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
        // TODO: 获取当前商家店铺ID
        String shopId = "currentShopId";
        
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<Order> pageResult = orderService.listShopOrders(shopId, status, pageParam);
        return Result.success(pageResult);
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
        PageResult<Order> pageResult = orderService.listAllOrders(status, pageParam);
        return Result.success(pageResult);
    }
    
    /**
     * 获取购物车
     *
     * @return 结果
     */
    @GetMapping("/cart")
    public Result<List<ShoppingCart>> getCart() {
        // TODO: 获取当前登录用户ID
        String userId = "currentUserId";
        List<ShoppingCart> carts = orderService.listUserCarts(userId);
        return Result.success(carts);
    }
    
    /**
     * 添加购物车
     *
     * @param cart 购物车信息
     * @return 结果
     */
    @PostMapping("/cart")
    public Result<ShoppingCart> addToCart(@RequestBody ShoppingCart cart) {
        // TODO: 获取当前登录用户ID
        String userId = "currentUserId";
        cart.setUserId(userId);
        
        ShoppingCart savedCart = orderService.addToCart(cart);
        return Result.success(savedCart);
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
        boolean success = orderService.updateCart(cart);
        return Result.success(success);
    }
    
    /**
     * 删除购物车
     *
     * @param id 购物车ID
     * @return 结果
     */
    @DeleteMapping("/cart/{id}")
    public Result<Boolean> removeFromCart(@PathVariable Integer id) {
        boolean success = orderService.removeFromCart(id);
        return Result.success(success);
    }
    
    /**
     * 清空购物车
     *
     * @return 结果
     */
    @DeleteMapping("/cart/clear")
    public Result<Boolean> clearCart() {
        // TODO: 获取当前登录用户ID
        String userId = "currentUserId";
        boolean success = orderService.clearUserCart(userId);
        return Result.success(success);
    }
} 