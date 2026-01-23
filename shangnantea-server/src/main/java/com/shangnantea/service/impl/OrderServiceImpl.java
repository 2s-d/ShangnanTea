package com.shangnantea.service.impl;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.common.api.Result;
import com.shangnantea.mapper.OrderMapper;
import com.shangnantea.mapper.ShoppingCartMapper;
import com.shangnantea.mapper.TeaMapper;
import com.shangnantea.mapper.TeaSpecificationMapper;
import com.shangnantea.mapper.UserAddressMapper;
import com.shangnantea.model.entity.order.Order;
import com.shangnantea.model.entity.order.ShoppingCart;
import com.shangnantea.model.entity.tea.Tea;
import com.shangnantea.model.entity.tea.TeaSpecification;
import com.shangnantea.model.entity.user.UserAddress;
import com.shangnantea.model.vo.order.CartItemVO;
import com.shangnantea.model.vo.order.CartResponseVO;
import com.shangnantea.model.vo.order.OrderDetailVO;
import com.shangnantea.model.vo.order.OrderVO;
import com.shangnantea.security.context.UserContext;
import com.shangnantea.service.OrderService;
import com.shangnantea.utils.FileUploadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    
    @Autowired
    private TeaMapper teaMapper;
    
    @Autowired
    private TeaSpecificationMapper teaSpecificationMapper;
    
    @Autowired
    private UserAddressMapper userAddressMapper;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Override
    public Order getOrderById(String id) {
        // TODO: 实现获取订单详情的逻辑
        return orderMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public Result<Object> createOrder(Map<String, Object> data) {
        logger.info("创建订单请求: data={}", data);
        
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("创建订单失败: 用户未登录");
                return Result.failure(5110);
            }
            
            // 2. 解析请求参数
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
            Object addressIdObj = data.get("addressId");
            String remark = (String) data.get("remark");
            
            if (items == null || items.isEmpty()) {
                logger.warn("创建订单失败: 订单商品列表为空");
                return Result.failure(5110);
            }
            
            if (addressIdObj == null) {
                logger.warn("创建订单失败: 收货地址ID为空");
                return Result.failure(5110);
            }
            
            Integer addressId = null;
            try {
                addressId = Integer.parseInt(addressIdObj.toString());
            } catch (NumberFormatException e) {
                logger.warn("创建订单失败: 地址ID格式错误: {}", addressIdObj);
                return Result.failure(5110);
            }
            
            // 3. 验证每个商品并计算总价
            BigDecimal totalPrice = BigDecimal.ZERO;
            List<Order> orderList = new ArrayList<>();
            
            for (Map<String, Object> item : items) {
                String teaId = (String) item.get("teaId");
                Object specIdObj = item.get("specId");
                Object quantityObj = item.get("quantity");
                
                if (teaId == null || quantityObj == null) {
                    logger.warn("创建订单失败: 商品信息不完整");
                    return Result.failure(5110);
                }
                
                Integer quantity = null;
                try {
                    quantity = Integer.parseInt(quantityObj.toString());
                } catch (NumberFormatException e) {
                    logger.warn("创建订单失败: 数量格式错误: {}", quantityObj);
                    return Result.failure(5110);
                }
                
                // 验证商品是否存在
                Tea tea = null;
                try {
                    tea = teaMapper.selectById(Long.parseLong(teaId));
                } catch (NumberFormatException e) {
                    logger.warn("创建订单失败: 茶叶ID格式错误: {}", teaId);
                    return Result.failure(5112); // 商品已下架或不可用
                }
                
                if (tea == null) {
                    logger.warn("创建订单失败: 商品不存在: teaId={}", teaId);
                    return Result.failure(5112); // 商品已下架或不可用
                }
                
                // 验证规格和库存
                Integer specId = null;
                BigDecimal price = null;
                String specName = null;
                Integer stock = 0;
                
                if (specIdObj != null && !specIdObj.toString().isEmpty()) {
                    try {
                        specId = Integer.parseInt(specIdObj.toString());
                        TeaSpecification spec = teaSpecificationMapper.selectById(specId.longValue());
                        
                        if (spec == null) {
                            logger.warn("创建订单失败: 规格不存在: specId={}", specId);
                            return Result.failure(5112); // 商品已下架或不可用
                        }
                        
                        price = spec.getPrice();
                        specName = spec.getSpecName();
                        stock = spec.getStock() != null ? spec.getStock() : 0;
                        
                    } catch (NumberFormatException e) {
                        logger.warn("创建订单失败: 规格ID格式错误: {}", specIdObj);
                        return Result.failure(5112);
                    }
                } else {
                    // 没有规格，使用商品默认价格和库存
                    price = tea.getPrice();
                    stock = tea.getStock() != null ? tea.getStock() : 0;
                }
                
                // 验证库存是否充足
                if (stock < quantity) {
                    logger.warn("创建订单失败: 库存不足: teaId={}, stock={}, quantity={}", teaId, stock, quantity);
                    return Result.failure(5111); // 商品库存不足
                }
                
                // 创建订单对象
                Order order = new Order();
                order.setId(UUID.randomUUID().toString().replace("-", ""));
                order.setUserId(userId);
                order.setShopId(tea.getShopId());
                order.setTeaId(teaId);
                order.setTeaName(tea.getName());
                order.setSpecId(specId);
                order.setSpecName(specName);
                order.setTeaImage(tea.getMainImage());
                order.setQuantity(quantity);
                order.setPrice(price);
                order.setTotalAmount(price.multiply(new BigDecimal(quantity)));
                order.setAddressId(addressId);
                order.setStatus(Order.STATUS_PENDING_PAYMENT); // 待付款
                order.setBuyerMessage(remark);
                order.setBuyerRate(0); // 未评价
                order.setRefundStatus(0); // 无退款申请
                order.setCreateTime(new Date());
                order.setUpdateTime(new Date());
                
                orderList.add(order);
                totalPrice = totalPrice.add(order.getTotalAmount());
            }
            
            // 4. 插入订单到数据库
            for (Order order : orderList) {
                orderMapper.insert(order);
                logger.info("订单已创建: orderId={}, teaId={}, quantity={}, totalAmount={}", 
                           order.getId(), order.getTeaId(), order.getQuantity(), order.getTotalAmount());
            }
            
            // 5. 构建返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("orderId", orderList.get(0).getId()); // 返回第一个订单ID
            responseData.put("totalPrice", totalPrice);
            responseData.put("status", Order.STATUS_PENDING_PAYMENT);
            
            logger.info("创建订单成功: userId={}, orderCount={}, totalPrice={}", userId, orderList.size(), totalPrice);
            return Result.success(5005, responseData);
            
        } catch (Exception e) {
            logger.error("创建订单失败: 系统异常", e);
            return Result.failure(5110);
        }
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
    public Result<CartItemVO> addToCart(String teaId, Integer quantity, String specificationId) {
        logger.info("添加购物车请求: teaId={}, quantity={}, specificationId={}", teaId, quantity, specificationId);
        
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("添加购物车失败: 用户未登录");
                return Result.failure(5101);
            }
            
            // 2. 验证商品是否存在
            Tea tea = null;
            try {
                tea = teaMapper.selectById(Long.parseLong(teaId));
            } catch (NumberFormatException e) {
                logger.warn("添加购物车失败: 茶叶ID格式错误: {}", teaId);
                return Result.failure(5104); // 商品已下架或不可用
            }
            
            if (tea == null) {
                logger.warn("添加购物车失败: 商品不存在: teaId={}", teaId);
                return Result.failure(5104); // 商品已下架或不可用
            }
            
            // 3. 验证规格和库存
            Integer specId = null;
            BigDecimal price = null;
            String specName = null;
            Integer stock = 0;
            
            if (specificationId != null && !specificationId.isEmpty()) {
                try {
                    specId = Integer.parseInt(specificationId);
                    TeaSpecification spec = teaSpecificationMapper.selectById(specId.longValue());
                    
                    if (spec == null) {
                        logger.warn("添加购物车失败: 规格不存在: specId={}", specId);
                        return Result.failure(5104); // 商品已下架或不可用
                    }
                    
                    price = spec.getPrice();
                    specName = spec.getSpecName();
                    stock = spec.getStock() != null ? spec.getStock() : 0;
                    
                } catch (NumberFormatException e) {
                    logger.warn("添加购物车失败: 规格ID格式错误: {}", specificationId);
                    return Result.failure(5104);
                }
            } else {
                // 没有规格，使用商品默认价格和库存
                price = tea.getPrice();
                stock = tea.getStock() != null ? tea.getStock() : 0;
            }
            
            // 4. 验证库存是否充足
            if (stock < quantity) {
                logger.warn("添加购物车失败: 库存不足: stock={}, quantity={}", stock, quantity);
                return Result.failure(5102); // 商品库存不足
            }
            
            // 5. 验证购买数量上限（不能超过库存的30%）
            int maxQuantity = (int) Math.floor(stock * 0.3);
            if (maxQuantity < 1) {
                maxQuantity = 1; // 至少允许购买1件
            }
            if (quantity > maxQuantity) {
                logger.warn("添加购物车失败: 超过购买数量上限: quantity={}, maxQuantity={}, stock={}", 
                           quantity, maxQuantity, stock);
                return Result.failure(5103); // 已达到购买数量上限
            }
            
            // 6. 检查购物车中是否已有相同商品
            ShoppingCart existingCart = cartMapper.selectByUserIdAndTeaIdAndSpecId(userId, teaId, specId);
            
            if (existingCart != null) {
                // 已存在，更新数量
                int newQuantity = existingCart.getQuantity() + quantity;
                
                // 再次验证库存
                if (stock < newQuantity) {
                    logger.warn("添加购物车失败: 更新后库存不足: stock={}, newQuantity={}", stock, newQuantity);
                    return Result.failure(5102);
                }
                
                // 再次验证购买数量上限
                if (newQuantity > maxQuantity) {
                    logger.warn("添加购物车失败: 更新后超过购买数量上限: newQuantity={}, maxQuantity={}, stock={}", 
                               newQuantity, maxQuantity, stock);
                    return Result.failure(5103);
                }
                
                existingCart.setQuantity(newQuantity);
                existingCart.setUpdateTime(new Date());
                cartMapper.updateById(existingCart);
                
                logger.info("购物车商品数量已更新: cartId={}, newQuantity={}", existingCart.getId(), newQuantity);
                
                // 构建返回VO
                CartItemVO itemVO = buildCartItemVO(existingCart, tea, specName, price);
                return Result.success(5000, itemVO);
                
            } else {
                // 不存在，新增
                ShoppingCart cart = new ShoppingCart();
                cart.setUserId(userId);
                cart.setTeaId(teaId);
                cart.setSpecId(specId);
                cart.setQuantity(quantity);
                cart.setSelected(1); // 默认选中
                cart.setCreateTime(new Date());
                cart.setUpdateTime(new Date());
                
                cartMapper.insert(cart);
                
                logger.info("商品已加入购物车: cartId={}, teaId={}, quantity={}", cart.getId(), teaId, quantity);
                
                // 构建返回VO
                CartItemVO itemVO = buildCartItemVO(cart, tea, specName, price);
                return Result.success(5000, itemVO);
            }
            
        } catch (Exception e) {
            logger.error("添加购物车失败: 系统异常", e);
            return Result.failure(5101);
        }
    }
    
    /**
     * 构建购物车项VO
     */
    private CartItemVO buildCartItemVO(ShoppingCart cart, Tea tea, String specName, BigDecimal price) {
        CartItemVO itemVO = new CartItemVO();
        itemVO.setId(String.valueOf(cart.getId()));
        itemVO.setTeaId(cart.getTeaId());
        itemVO.setTeaName(tea.getName());
        itemVO.setTeaImage(tea.getMainImage());
        itemVO.setSpecId(cart.getSpecId() != null ? String.valueOf(cart.getSpecId()) : null);
        itemVO.setSpecName(specName);
        itemVO.setPrice(price);
        itemVO.setQuantity(cart.getQuantity());
        itemVO.setSelected(cart.getSelected() != null && cart.getSelected() == 1);
        return itemVO;
    }
    
    @Override
    @Transactional
    public Result<CartItemVO> updateCart(Integer id, Integer quantity, String specificationId) {
        logger.info("更新购物车请求: id={}, quantity={}, specificationId={}", id, quantity, specificationId);
        
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("更新购物车失败: 用户未登录");
                return Result.failure(5105);
            }
            
            // 2. 查询购物车项是否存在
            ShoppingCart cart = cartMapper.selectById(id);
            if (cart == null) {
                logger.warn("更新购物车失败: 购物车项不存在: id={}", id);
                return Result.failure(5105);
            }
            
            // 3. 验证购物车项是否属于当前用户
            if (!userId.equals(cart.getUserId())) {
                logger.warn("更新购物车失败: 购物车项不属于当前用户: cartId={}, userId={}, cartUserId={}", 
                           id, userId, cart.getUserId());
                return Result.failure(5105);
            }
            
            // 4. 查询商品信息
            Tea tea = null;
            try {
                tea = teaMapper.selectById(Long.parseLong(cart.getTeaId()));
            } catch (NumberFormatException e) {
                logger.warn("更新购物车失败: 茶叶ID格式错误: {}", cart.getTeaId());
                return Result.failure(5105);
            }
            
            if (tea == null) {
                logger.warn("更新购物车失败: 商品不存在: teaId={}", cart.getTeaId());
                return Result.failure(5105);
            }
            
            // 5. 判断是更新数量还是更新规格
            boolean isUpdateQuantity = (quantity != null);
            boolean isUpdateSpec = (specificationId != null && !specificationId.isEmpty());
            
            if (!isUpdateQuantity && !isUpdateSpec) {
                logger.warn("更新购物车失败: 未提供更新内容");
                return Result.failure(5105);
            }
            
            // 6. 处理更新规格
            if (isUpdateSpec) {
                Integer newSpecId = null;
                BigDecimal newPrice = null;
                String newSpecName = null;
                Integer stock = 0;
                
                try {
                    newSpecId = Integer.parseInt(specificationId);
                    TeaSpecification spec = teaSpecificationMapper.selectById(newSpecId.longValue());
                    
                    if (spec == null) {
                        logger.warn("更新购物车失败: 规格不存在: specId={}", newSpecId);
                        return Result.failure(5105);
                    }
                    
                    newPrice = spec.getPrice();
                    newSpecName = spec.getSpecName();
                    stock = spec.getStock() != null ? spec.getStock() : 0;
                    
                } catch (NumberFormatException e) {
                    logger.warn("更新购物车失败: 规格ID格式错误: {}", specificationId);
                    return Result.failure(5105);
                }
                
                // 验证库存
                int currentQuantity = cart.getQuantity();
                if (stock < currentQuantity) {
                    logger.warn("更新购物车失败: 新规格库存不足: stock={}, quantity={}", stock, currentQuantity);
                    return Result.failure(5106);
                }
                
                // 验证数量上限
                int maxQuantity = (int) Math.floor(stock * 0.3);
                if (maxQuantity < 1) {
                    maxQuantity = 1;
                }
                if (currentQuantity > maxQuantity) {
                    logger.warn("更新购物车失败: 新规格超过购买数量上限: quantity={}, maxQuantity={}, stock={}", 
                               currentQuantity, maxQuantity, stock);
                    return Result.failure(5107);
                }
                
                // 更新规格
                cart.setSpecId(newSpecId);
                cart.setUpdateTime(new Date());
                cartMapper.updateById(cart);
                
                logger.info("购物车规格已更新: cartId={}, newSpecId={}", id, newSpecId);
                
                // 构建返回VO
                CartItemVO itemVO = buildCartItemVO(cart, tea, newSpecName, newPrice);
                return Result.success(5002, itemVO); // 规格已更新
            }
            
            // 7. 处理更新数量
            if (isUpdateQuantity) {
                // 获取当前规格的库存和价格
                Integer specId = cart.getSpecId();
                BigDecimal price = null;
                String specName = null;
                Integer stock = 0;
                
                if (specId != null) {
                    TeaSpecification spec = teaSpecificationMapper.selectById(specId.longValue());
                    if (spec != null) {
                        price = spec.getPrice();
                        specName = spec.getSpecName();
                        stock = spec.getStock() != null ? spec.getStock() : 0;
                    }
                } else {
                    // 没有规格，使用商品默认价格和库存
                    price = tea.getPrice();
                    stock = tea.getStock() != null ? tea.getStock() : 0;
                }
                
                // 验证库存
                if (stock < quantity) {
                    logger.warn("更新购物车失败: 库存不足: stock={}, quantity={}", stock, quantity);
                    return Result.failure(5106);
                }
                
                // 验证数量上限
                int maxQuantity = (int) Math.floor(stock * 0.3);
                if (maxQuantity < 1) {
                    maxQuantity = 1;
                }
                if (quantity > maxQuantity) {
                    logger.warn("更新购物车失败: 超过购买数量上限: quantity={}, maxQuantity={}, stock={}", 
                               quantity, maxQuantity, stock);
                    return Result.failure(5107);
                }
                
                // 更新数量
                cart.setQuantity(quantity);
                cart.setUpdateTime(new Date());
                cartMapper.updateById(cart);
                
                logger.info("购物车数量已更新: cartId={}, newQuantity={}", id, quantity);
                
                // 构建返回VO
                CartItemVO itemVO = buildCartItemVO(cart, tea, specName, price);
                return Result.success(5001, itemVO); // 商品数量已更新
            }
            
            // 不应该到达这里
            return Result.failure(5105);
            
        } catch (Exception e) {
            logger.error("更新购物车失败: 系统异常", e);
            return Result.failure(5105);
        }
    }
    
    @Override
    @Transactional
    public Result<Boolean> removeFromCart(Integer id) {
        logger.info("移除购物车商品请求: id={}", id);
        
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("移除购物车失败: 用户未登录");
                return Result.failure(5108);
            }
            
            // 2. 查询购物车项是否存在
            ShoppingCart cart = cartMapper.selectById(id);
            if (cart == null) {
                logger.warn("移除购物车失败: 购物车项不存在: id={}", id);
                return Result.failure(5108);
            }
            
            // 3. 验证购物车项是否属于当前用户
            if (!userId.equals(cart.getUserId())) {
                logger.warn("移除购物车失败: 购物车项不属于当前用户: cartId={}, userId={}, cartUserId={}", 
                           id, userId, cart.getUserId());
                return Result.failure(5108);
            }
            
            // 4. 删除购物车项
            int rows = cartMapper.deleteById(id);
            if (rows > 0) {
                logger.info("购物车商品已移除: cartId={}, userId={}", id, userId);
                return Result.success(5003, true);
            } else {
                logger.warn("移除购物车失败: 删除操作未影响任何行: id={}", id);
                return Result.failure(5108);
            }
            
        } catch (Exception e) {
            logger.error("移除购物车失败: 系统异常", e);
            return Result.failure(5108);
        }
    }
    
    @Override
    public Result<CartResponseVO> getCartItems() {
        logger.info("获取购物车列表请求");
        
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取购物车失败: 用户未登录");
                return Result.failure(5100);
            }
            
            // 2. 查询购物车列表
            List<ShoppingCart> cartList = cartMapper.selectByUserId(userId);
            if (cartList == null || cartList.isEmpty()) {
                // 购物车为空，返回空列表
                CartResponseVO response = new CartResponseVO();
                response.setItems(new ArrayList<>());
                response.setTotalPrice(BigDecimal.ZERO);
                logger.info("获取购物车成功: 购物车为空");
                return Result.success(200, response);
            }
            
            // 3. 转换为VO并关联商品信息
            List<CartItemVO> itemList = new ArrayList<>();
            BigDecimal totalPrice = BigDecimal.ZERO;
            
            for (ShoppingCart cart : cartList) {
                CartItemVO itemVO = new CartItemVO();
                itemVO.setId(String.valueOf(cart.getId()));
                itemVO.setTeaId(cart.getTeaId());
                itemVO.setSpecId(cart.getSpecId() != null ? String.valueOf(cart.getSpecId()) : null);
                itemVO.setQuantity(cart.getQuantity());
                itemVO.setSelected(cart.getSelected() != null && cart.getSelected() == 1);
                
                // 查询商品信息
                if (cart.getTeaId() != null) {
                    try {
                        Tea tea = teaMapper.selectById(Long.parseLong(cart.getTeaId()));
                        if (tea != null) {
                            itemVO.setTeaName(tea.getName());
                            itemVO.setTeaImage(tea.getMainImage());
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("商品ID格式错误: {}", cart.getTeaId());
                    }
                }
                
                // 查询规格信息
                if (cart.getSpecId() != null) {
                    try {
                        TeaSpecification spec = teaSpecificationMapper.selectById(cart.getSpecId().longValue());
                        if (spec != null) {
                            itemVO.setSpecName(spec.getSpecName());
                            itemVO.setPrice(spec.getPrice());
                            
                            // 计算总价
                            if (spec.getPrice() != null && cart.getQuantity() != null) {
                                BigDecimal itemTotal = spec.getPrice().multiply(new BigDecimal(cart.getQuantity()));
                                totalPrice = totalPrice.add(itemTotal);
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("查询规格信息失败: specId={}, error={}", cart.getSpecId(), e.getMessage());
                    }
                }
                
                itemList.add(itemVO);
            }
            
            // 4. 构建响应
            CartResponseVO response = new CartResponseVO();
            response.setItems(itemList);
            response.setTotalPrice(totalPrice);
            
            logger.info("获取购物车成功: userId={}, itemCount={}, totalPrice={}", userId, itemList.size(), totalPrice);
            return Result.success(200, response);
            
        } catch (Exception e) {
            logger.error("获取购物车失败: 系统异常", e);
            return Result.failure(5100);
        }
    }
    
    @Override
    public List<ShoppingCart> listUserCarts(String userId) {
        // TODO: 实现查询用户购物车的逻辑
        return null; // 待实现
    }
    
    @Override
    @Transactional
    public Result<Boolean> clearCart() {
        logger.info("清空购物车请求");
        
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("清空购物车失败: 用户未登录");
                return Result.failure(5109);
            }
            
            // 2. 查询用户的所有购物车项
            List<ShoppingCart> cartList = cartMapper.selectByUserId(userId);
            
            if (cartList == null || cartList.isEmpty()) {
                // 购物车已经是空的，直接返回成功
                logger.info("清空购物车成功: 购物车已为空, userId={}", userId);
                return Result.success(5004, true);
            }
            
            // 3. 提取所有购物车项ID
            List<Integer> cartIds = new ArrayList<>();
            for (ShoppingCart cart : cartList) {
                cartIds.add(cart.getId());
            }
            
            // 4. 批量删除购物车项
            int deletedCount = cartMapper.deleteByIds(cartIds);
            
            if (deletedCount > 0) {
                logger.info("清空购物车成功: userId={}, deletedCount={}", userId, deletedCount);
                return Result.success(5004, true);
            } else {
                logger.warn("清空购物车失败: 删除操作未影响任何行, userId={}", userId);
                return Result.failure(5109);
            }
            
        } catch (Exception e) {
            logger.error("清空购物车失败: 系统异常", e);
            return Result.failure(5109);
        }
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
    
    @Override
    public Result<Map<String, Object>> getOrders(Map<String, Object> params) {
        logger.info("获取订单列表请求: params={}", params);
        
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取订单列表失败: 用户未登录");
                return Result.failure(5113);
            }
            
            // 2. 解析查询参数
            Integer status = null;
            if (params.get("status") != null) {
                try {
                    status = Integer.parseInt(params.get("status").toString());
                } catch (NumberFormatException e) {
                    logger.warn("状态参数格式错误: {}", params.get("status"));
                }
            }
            
            int page = 1;
            int pageSize = 10;
            if (params.get("page") != null) {
                try {
                    page = Integer.parseInt(params.get("page").toString());
                } catch (NumberFormatException e) {
                    logger.warn("页码参数格式错误: {}", params.get("page"));
                }
            }
            if (params.get("pageSize") != null) {
                try {
                    pageSize = Integer.parseInt(params.get("pageSize").toString());
                } catch (NumberFormatException e) {
                    logger.warn("每页数量参数格式错误: {}", params.get("pageSize"));
                }
            }
            
            // 3. 查询订单列表
            List<Order> orderList = orderMapper.selectByUserIdAndStatus(userId, status);
            
            if (orderList == null) {
                orderList = new ArrayList<>();
            }
            
            // 4. 计算总数
            int total = orderList.size();
            
            // 5. 分页处理
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, total);
            
            List<Order> pagedOrders = new ArrayList<>();
            if (startIndex < total) {
                pagedOrders = orderList.subList(startIndex, endIndex);
            }
            
            // 6. 转换为VO
            List<OrderVO> orderVOList = new ArrayList<>();
            for (Order order : pagedOrders) {
                OrderVO vo = new OrderVO();
                vo.setId(order.getId());
                vo.setStatus(order.getStatus());
                vo.setTotalPrice(order.getTotalAmount());
                vo.setCreateTime(order.getCreateTime());
                vo.setTeaId(order.getTeaId());
                vo.setTeaName(order.getTeaName());
                vo.setTeaImage(order.getTeaImage());
                vo.setSpecName(order.getSpecName());
                vo.setQuantity(order.getQuantity());
                vo.setPrice(order.getPrice());
                orderVOList.add(vo);
            }
            
            // 7. 构建返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("list", orderVOList);
            responseData.put("total", total);
            
            logger.info("获取订单列表成功: userId={}, status={}, total={}, page={}, pageSize={}", 
                       userId, status, total, page, pageSize);
            return Result.success(200, responseData);
            
        } catch (Exception e) {
            logger.error("获取订单列表失败: 系统异常", e);
            return Result.failure(5113);
        }
    }
    
    @Override
    public Result<Object> getOrderDetail(String id) {
        logger.info("获取订单详情请求: id={}", id);
        
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取订单详情失败: 用户未登录");
                return Result.failure(5116);
            }
            
            // 2. 查询订单
            Order order = orderMapper.selectById(id);
            if (order == null) {
                logger.warn("获取订单详情失败: 订单不存在: id={}", id);
                return Result.failure(5114); // 订单不存在
            }
            
            // 3. 验证订单是否属于当前用户
            if (!userId.equals(order.getUserId())) {
                logger.warn("获取订单详情失败: 无权限: orderId={}, userId={}, orderUserId={}", 
                           id, userId, order.getUserId());
                return Result.failure(5115); // 您没有权限操作此订单
            }
            
            // 4. 查询地址信息
            Map<String, Object> addressInfo = new HashMap<>();
            if (order.getAddressId() != null) {
                UserAddress address = userAddressMapper.selectById(order.getAddressId());
                if (address != null) {
                    addressInfo.put("receiverName", address.getReceiverName());
                    addressInfo.put("receiverPhone", address.getReceiverPhone());
                    addressInfo.put("detailAddress", address.getDetailAddress());
                }
            }
            
            // 5. 转换为VO
            OrderDetailVO vo = new OrderDetailVO();
            vo.setId(order.getId());
            vo.setUserId(order.getUserId());
            vo.setStatus(order.getStatus());
            vo.setTotalPrice(order.getTotalAmount());
            vo.setTeaId(order.getTeaId());
            vo.setTeaName(order.getTeaName());
            vo.setTeaImage(order.getTeaImage());
            vo.setSpecId(order.getSpecId());
            vo.setSpecName(order.getSpecName());
            vo.setQuantity(order.getQuantity());
            vo.setPrice(order.getPrice());
            vo.setAddress(addressInfo);
            vo.setPaymentMethod(order.getPaymentMethod());
            vo.setPaymentTime(order.getPaymentTime());
            vo.setLogisticsCompany(order.getLogisticsCompany());
            vo.setLogisticsNumber(order.getLogisticsNumber());
            vo.setShippingTime(order.getShippingTime());
            vo.setCompletionTime(order.getCompletionTime());
            vo.setBuyerMessage(order.getBuyerMessage());
            vo.setCreateTime(order.getCreateTime());
            vo.setUpdateTime(order.getUpdateTime());
            
            logger.info("获取订单详情成功: orderId={}, userId={}", id, userId);
            return Result.success(200, vo);
            
        } catch (Exception e) {
            logger.error("获取订单详情失败: 系统异常", e);
            return Result.failure(5116);
        }
    }
    
    @Override
    @Transactional
    public Result<Object> payOrder(Map<String, Object> data) {
        logger.info("支付订单请求: data={}", data);
        
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("支付订单失败: 用户未登录");
                return Result.failure(5117);
            }
            
            // 2. 解析请求参数
            String orderId = (String) data.get("orderId");
            String paymentMethod = (String) data.get("paymentMethod");
            
            if (orderId == null || orderId.isEmpty()) {
                logger.warn("支付订单失败: 订单ID为空");
                return Result.failure(5117);
            }
            
            if (paymentMethod == null || paymentMethod.isEmpty()) {
                logger.warn("支付订单失败: 支付方式为空");
                return Result.failure(5117);
            }
            
            // 3. 查询订单
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                logger.warn("支付订单失败: 订单不存在: orderId={}", orderId);
                return Result.failure(5117);
            }
            
            // 4. 验证订单是否属于当前用户
            if (!userId.equals(order.getUserId())) {
                logger.warn("支付订单失败: 无权限: orderId={}, userId={}, orderUserId={}", 
                           orderId, userId, order.getUserId());
                return Result.failure(5117);
            }
            
            // 5. 验证订单状态是否为待付款
            if (order.getStatus() == null || order.getStatus() != Order.STATUS_PENDING_PAYMENT) {
                logger.warn("支付订单失败: 订单状态不是待付款: orderId={}, status={}", orderId, order.getStatus());
                // 如果订单已经支付过了，返回5006
                if (order.getStatus() != null && order.getStatus() > Order.STATUS_PENDING_PAYMENT) {
                    return Result.success(5006, null); // 订单已支付
                }
                return Result.failure(5117);
            }
            
            // 6. 如果支付方式是余额，验证余额是否充足（这里简化处理，实际需要查询用户余额）
            if ("balance".equals(paymentMethod)) {
                // TODO: 实际应该查询用户余额表
                // 这里简化处理，假设余额不足
                logger.warn("支付订单失败: 余额不足: orderId={}, userId={}", orderId, userId);
                return Result.failure(5120); // 余额不足
            }
            
            // 7. 更新订单状态为待发货
            order.setStatus(Order.STATUS_PENDING_SHIPMENT); // 待发货
            order.setPaymentMethod(paymentMethod);
            order.setPaymentTime(new Date());
            order.setUpdateTime(new Date());
            
            int rows = orderMapper.updateById(order);
            if (rows > 0) {
                logger.info("支付订单成功: orderId={}, userId={}, paymentMethod={}", orderId, userId, paymentMethod);
                
                // 8. 构建返回数据
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("orderId", orderId);
                responseData.put("status", order.getStatus());
                responseData.put("paymentTime", order.getPaymentTime());
                
                return Result.success(5007, responseData); // 支付成功
            } else {
                logger.warn("支付订单失败: 更新订单失败: orderId={}", orderId);
                return Result.failure(5117);
            }
            
        } catch (Exception e) {
            logger.error("支付订单失败: 系统异常", e);
            return Result.failure(5117);
        }
    }
    
    @Override
    @Transactional
    public Result<Boolean> cancelOrder(Map<String, Object> data) {
        logger.info("取消订单请求: data={}", data);
        
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("取消订单失败: 用户未登录");
                return Result.failure(5121);
            }
            
            // 2. 解析请求参数
            Object idObj = data.get("id");
            if (idObj == null) {
                logger.warn("取消订单失败: 订单ID为空");
                return Result.failure(5121);
            }
            
            String orderId = idObj.toString();
            String cancelReason = (String) data.get("reason");
            
            // 3. 查询订单
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                logger.warn("取消订单失败: 订单不存在: orderId={}", orderId);
                return Result.failure(5122); // 订单不存在
            }
            
            // 4. 验证订单是否属于当前用户
            if (!userId.equals(order.getUserId())) {
                logger.warn("取消订单失败: 无权限: orderId={}, userId={}, orderUserId={}", 
                           orderId, userId, order.getUserId());
                return Result.failure(5123); // 您没有权限操作此订单
            }
            
            // 5. 验证订单状态是否可以取消（只有待付款和待发货状态可以取消）
            if (order.getStatus() == null || 
                (order.getStatus() != Order.STATUS_PENDING_PAYMENT && 
                 order.getStatus() != Order.STATUS_PENDING_SHIPMENT)) {
                logger.warn("取消订单失败: 订单状态不允许取消: orderId={}, status={}", orderId, order.getStatus());
                return Result.failure(5121);
            }
            
            // 6. 更新订单状态为已取消
            order.setStatus(Order.STATUS_CANCELLED); // 已取消
            order.setCancelReason(cancelReason);
            order.setCancelTime(new Date());
            order.setUpdateTime(new Date());
            
            int rows = orderMapper.updateById(order);
            if (rows > 0) {
                logger.info("取消订单成功: orderId={}, userId={}, reason={}", orderId, userId, cancelReason);
                return Result.success(5008, true); // 订单已取消
            } else {
                logger.warn("取消订单失败: 更新订单失败: orderId={}", orderId);
                return Result.failure(5121);
            }
            
        } catch (Exception e) {
            logger.error("取消订单失败: 系统异常", e);
            return Result.failure(5121);
        }
    }
    
    @Override
    public Result<Boolean> confirmOrder(Map<String, Object> data) {
        // TODO: 实现确认收货逻辑
        return Result.failure(5124);
    }
    
    @Override
    public Result<Boolean> reviewOrder(Map<String, Object> data) {
        // TODO: 实现评价订单逻辑
        return Result.failure(5127);
    }
    
    @Override
    public Result<Boolean> refundOrder(Map<String, Object> data) {
        // TODO: 实现申请退款逻辑
        return Result.failure(5128);
    }
    
    @Override
    public Result<Boolean> processRefund(String id, Map<String, Object> data) {
        // TODO: 实现审批退款逻辑
        return Result.failure(5131);
    }
    
    @Override
    public Result<Object> getRefundDetail(String id) {
        // TODO: 实现获取退款详情逻辑
        return Result.failure(5133);
    }
    
    @Override
    public Result<Boolean> shipOrder(String id, String logisticsCompany, String logisticsNumber) {
        // TODO: 实现发货逻辑
        return Result.failure(5135);
    }
    
    @Override
    public Result<Boolean> batchShipOrders(Map<String, Object> data) {
        // TODO: 实现批量发货逻辑
        return Result.failure(5138);
    }
    
    @Override
    public Result<Object> getOrderLogistics(String id) {
        // TODO: 实现获取物流信息逻辑
        return Result.failure(5140);
    }
    
    @Override
    public Result<Object> getOrderStatistics(Map<String, Object> params) {
        // TODO: 实现获取订单统计逻辑
        return Result.failure(5142);
    }
    
    @Override
    public Result<Object> exportOrders(Map<String, Object> params) {
        // TODO: 实现导出订单数据逻辑
        return Result.failure(5143);
    }
} 