package com.shangnantea.service.impl;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.common.api.Result;
import com.shangnantea.mapper.OrderMapper;
import com.shangnantea.mapper.ShoppingCartMapper;
import com.shangnantea.model.entity.order.Order;
import com.shangnantea.model.entity.order.ShoppingCart;
import com.shangnantea.service.OrderService;
import com.shangnantea.utils.FileUploadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private ShoppingCartMapper cartMapper;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Override
    public Order getOrderById(String id) {
        // TODO: 实现获取订单详情的逻辑
        return orderMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public Order createOrder(Order order) {
        // TODO: 实现创建订单的逻辑
        Date now = new Date();
        order.setId(UUID.randomUUID().toString().replace("-", ""));
        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setStatus(0); // 待支付
        orderMapper.insert(order);
        return order;
    }
    
    @Override
    @Transactional
    public boolean updateOrderStatus(String id, Integer status) {
        // TODO: 实现更新订单状态的逻辑
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return false;
        }
        
        order.setStatus(status);
        order.setUpdateTime(new Date());
        
        return orderMapper.updateById(order) > 0;
    }
    
    @Override
    @Transactional
    public boolean cancelOrder(String id, String cancelReason) {
        // TODO: 实现取消订单的逻辑
        Order order = orderMapper.selectById(id);
        if (order == null || order.getStatus() > 1) {
            return false;
        }
        
        order.setStatus(5); // 已取消
        order.setCancelReason(cancelReason);
        order.setCancelTime(new Date());
        order.setUpdateTime(new Date());
        
        return orderMapper.updateById(order) > 0;
    }
    
    @Override
    @Transactional
    public boolean shipOrder(String id, String logisticsCompany, String logisticsNumber) {
        // TODO: 实现发货的逻辑
        Order order = orderMapper.selectById(id);
        if (order == null || order.getStatus() != 1) {
            return false;
        }
        
        order.setStatus(2); // 已发货
        order.setLogisticsCompany(logisticsCompany);
        order.setLogisticsNumber(logisticsNumber);
        order.setShippingTime(new Date());
        order.setUpdateTime(new Date());
        
        return orderMapper.updateById(order) > 0;
    }
    
    @Override
    @Transactional
    public boolean completeOrder(String id) {
        // TODO: 实现完成订单的逻辑
        Order order = orderMapper.selectById(id);
        if (order == null || order.getStatus() != 2) {
            return false;
        }
        
        order.setStatus(3); // 已完成
        order.setCompletionTime(new Date());
        order.setUpdateTime(new Date());
        
        return orderMapper.updateById(order) > 0;
    }
    
    @Override
    public PageResult<Order> listUserOrders(String userId, Integer status, PageParam pageParam) {
        // TODO: 实现查询用户订单的逻辑
        return new PageResult<>();
    }
    
    @Override
    public PageResult<Order> listShopOrders(String shopId, Integer status, PageParam pageParam) {
        // TODO: 实现查询商家订单的逻辑
        return new PageResult<>();
    }
    
    @Override
    public PageResult<Order> listAllOrders(Integer status, PageParam pageParam) {
        // TODO: 实现查询所有订单的逻辑
        return new PageResult<>();
    }
    
    @Override
    @Transactional
    public ShoppingCart addToCart(ShoppingCart cart) {
        // TODO: 实现添加购物车的逻辑
        Date now = new Date();
        cart.setCreateTime(now);
        cart.setUpdateTime(now);
        cartMapper.insert(cart);
        return cart;
    }
    
    @Override
    @Transactional
    public boolean updateCart(ShoppingCart cart) {
        // TODO: 实现更新购物车的逻辑
        cart.setUpdateTime(new Date());
        return cartMapper.updateById(cart) > 0;
    }
    
    @Override
    @Transactional
    public boolean removeFromCart(Integer id) {
        // TODO: 实现删除购物车的逻辑
        return cartMapper.deleteById(id) > 0;
    }
    
    @Override
    public List<ShoppingCart> listUserCarts(String userId) {
        // TODO: 实现查询用户购物车的逻辑
        return null; // 待实现
    }
    
    @Override
    @Transactional
    public boolean clearUserCart(String userId) {
        // TODO: 实现清空用户购物车的逻辑
        return false; // 待实现
    }
    
    @Override
    public Result<Map<String, Object>> uploadReviewImage(MultipartFile image) {
        try {
            logger.info("上传评价图片请求, 文件名: {}", image.getOriginalFilename());
            
            // 1. 调用工具类上传（硬编码type为"reviews"）
            String relativePath = FileUploadUtils.uploadImage(image, "reviews");
            
            // 2. 生成访问URL
            String accessUrl = FileUploadUtils.generateAccessUrl(relativePath, baseUrl);
            
            // 3. 直接返回，不存数据库（场景2：先返回URL，稍后存储）
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("url", accessUrl);
            responseData.put("path", relativePath);
            
            logger.info("评价图片上传成功: path: {}", relativePath);
            return Result.success(5016, responseData); // 评价图片上传成功
            
        } catch (Exception e) {
            logger.error("评价图片上传失败: 系统异常", e);
            return Result.failure(5144); // 评价图片上传失败
        }
    }
} 