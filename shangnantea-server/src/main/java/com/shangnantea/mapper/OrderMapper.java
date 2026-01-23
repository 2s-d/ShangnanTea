package com.shangnantea.mapper;

import com.shangnantea.model.entity.order.Order;
import java.util.List;

/**
 * 订单Mapper接口
 */
public interface OrderMapper extends BaseMapper<Order, String> {
    /**
     * 根据状态查询订单
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> selectByStatus(Integer status);
    
    /**
     * 根据用户ID和状态查询订单
     * @param userId 用户ID
     * @param status 订单状态，可为null表示查询所有状态
     * @return 订单列表
     */
    List<Order> selectByUserIdAndStatus(String userId, Integer status);
    
    /**
     * 根据店铺ID和状态查询订单
     * @param shopId 店铺ID
     * @param status 订单状态，可为null表示查询所有状态
     * @return 订单列表
     */
    List<Order> selectByShopIdAndStatus(String shopId, Integer status);
    
    /**
     * 查询待付款订单 (状态码0)
     * @return 待付款订单列表
     */
    List<Order> selectPendingPaymentOrders();
    
    /**
     * 查询待发货订单 (状态码1)
     * @return 待发货订单列表
     */
    List<Order> selectPendingShipmentOrders();
    
    /**
     * 查询待收货订单 (状态码2)
     * @return 待收货订单列表
     */
    List<Order> selectPendingReceiptOrders();
    
    /**
     * 查询已完成订单 (状态码3)
     * @return 已完成订单列表
     */
    List<Order> selectCompletedOrders();
    
    /**
     * 查询已取消订单 (状态码4)
     * @return 已取消订单列表
     */
    List<Order> selectCancelledOrders();
    
    /**
     * 查询已退款订单 (状态码5)
     * @return 已退款订单列表
     */
    List<Order> selectRefundedOrders();
    
    /**
     * 统计订单总数
     * @param userId 用户ID（可选）
     * @param shopId 店铺ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 订单总数
     */
    Integer countOrders(String userId, String shopId, String startDate, String endDate);
    
    /**
     * 统计订单总金额
     * @param userId 用户ID（可选）
     * @param shopId 店铺ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 订单总金额
     */
    java.math.BigDecimal sumOrderAmount(String userId, String shopId, String startDate, String endDate);
    
    /**
     * 统计各状态订单数量
     * @param userId 用户ID（可选）
     * @param shopId 店铺ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return Map<状态, 数量>
     */
    java.util.Map<String, Integer> countOrdersByStatus(String userId, String shopId, String startDate, String endDate);
    
    /**
     * 查询订单趋势数据
     * @param userId 用户ID（可选）
     * @param shopId 店铺ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 趋势数据列表
     */
    java.util.List<java.util.Map<String, Object>> selectOrderTrend(String userId, String shopId, String startDate, String endDate);
    
    /**
     * 根据条件查询订单列表（用于导出）
     * @param userId 用户ID（可选）
     * @param shopId 店铺ID（可选）
     * @param status 订单状态（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 订单列表
     */
    List<Order> selectOrdersForExport(String userId, String shopId, Integer status, String startDate, String endDate);
} 