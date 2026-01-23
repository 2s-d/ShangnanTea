package com.shangnantea.service;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.common.api.Result;
import com.shangnantea.model.entity.order.Order;
import com.shangnantea.model.entity.order.ShoppingCart;
import com.shangnantea.model.vo.order.CartResponseVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService {
    
    /**
     * 获取订单详情
     *
     * @param id 订单ID
     * @return 订单信息
     */
    Order getOrderById(String id);
    
    /**
     * 创建订单
     *
     * @param order 订单信息
     * @return 订单信息
     */
    Order createOrder(Order order);
    
    /**
     * 更新订单状态
     *
     * @param id 订单ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateOrderStatus(String id, Integer status);
    
    /**
     * 取消订单
     *
     * @param id 订单ID
     * @param cancelReason 取消原因
     * @return 是否成功
     */
    boolean cancelOrder(String id, String cancelReason);
    
    /**
     * 发货
     *
     * @param id 订单ID
     * @param logisticsCompany 物流公司
     * @param logisticsNumber 物流单号
     * @return 是否成功
     */
    boolean shipOrder(String id, String logisticsCompany, String logisticsNumber);
    
    /**
     * 完成订单
     *
     * @param id 订单ID
     * @return 是否成功
     */
    boolean completeOrder(String id);
    
    /**
     * 查询用户订单
     *
     * @param userId 用户ID
     * @param status 状态
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<Order> listUserOrders(String userId, Integer status, PageParam pageParam);
    
    /**
     * 查询商家订单
     *
     * @param shopId 店铺ID
     * @param status 状态
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<Order> listShopOrders(String shopId, Integer status, PageParam pageParam);
    
    /**
     * 查询所有订单(管理员)
     *
     * @param status 状态
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<Order> listAllOrders(Integer status, PageParam pageParam);
    
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
     * 查询用户购物车
     *
     * @param userId 用户ID
     * @return 购物车列表
     */
    List<ShoppingCart> listUserCarts(String userId);
    
    /**
     * 清空用户购物车
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean clearUserCart(String userId);
    
    /**
     * 上传评价图片
     *
     * @param image 图片文件
     * @return 上传结果
     */
    Result<Map<String, Object>> uploadReviewImage(MultipartFile image);
} 