package com.shangnantea.service.impl;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.common.api.Result;
import com.shangnantea.mapper.OrderMapper;
import com.shangnantea.mapper.ShopMapper;
import com.shangnantea.mapper.ShoppingCartMapper;
import com.shangnantea.mapper.TeaMapper;
import com.shangnantea.mapper.TeaReviewMapper;
import com.shangnantea.mapper.TeaSpecificationMapper;
import com.shangnantea.mapper.UserAddressMapper;
import com.shangnantea.model.entity.order.Order;
import com.shangnantea.model.entity.order.ShoppingCart;
import com.shangnantea.model.entity.shop.Shop;
import com.shangnantea.model.entity.tea.Tea;
import com.shangnantea.model.entity.tea.TeaReview;
import com.shangnantea.model.entity.tea.TeaSpecification;
import com.shangnantea.model.entity.user.UserAddress;
import com.shangnantea.model.vo.order.CartItemVO;
import com.shangnantea.model.vo.order.CartResponseVO;
import com.shangnantea.model.vo.order.LogisticsVO;
import com.shangnantea.model.vo.order.OrderDetailVO;
import com.shangnantea.model.vo.order.OrderVO;
import com.shangnantea.model.vo.order.RefundDetailVO;
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
    
    @Autowired
    private TeaReviewMapper teaReviewMapper;
    
    @Autowired
    private ShopMapper shopMapper;
    
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
    
    /**
     * 验证商家是否拥有指定店铺
     * @param userId 用户ID
     * @param shopId 店铺ID
     * @return true-拥有该店铺，false-不拥有
     */
    private boolean isShopOwner(String userId, String shopId) {
        if (userId == null || userId.isEmpty() || shopId == null || shopId.isEmpty()) {
            return false;
        }
        
        try {
            // 查询该用户对应的店铺
            Shop shop = shopMapper.selectByOwnerId(userId);
            if (shop == null) {
                return false;
            }
            
            // 验证店铺ID是否匹配
            return shopId.equals(shop.getId());
        } catch (Exception e) {
            logger.error("验证商家权限失败: userId={}, shopId={}, error={}", userId, shopId, e.getMessage());
            return false;
        }
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
    @Transactional
    public Result<Boolean> confirmOrder(Map<String, Object> data) {
        logger.info("确认收货请求: data={}", data);
        
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("确认收货失败: 用户未登录");
                return Result.failure(5124);
            }
            
            // 2. 解析请求参数
            Object idObj = data.get("id");
            if (idObj == null) {
                logger.warn("确认收货失败: 订单ID为空");
                return Result.failure(5124);
            }
            
            String orderId = idObj.toString();
            
            // 3. 查询订单
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                logger.warn("确认收货失败: 订单不存在: orderId={}", orderId);
                return Result.failure(5125); // 订单不存在
            }
            
            // 4. 验证订单是否属于当前用户
            if (!userId.equals(order.getUserId())) {
                logger.warn("确认收货失败: 无权限: orderId={}, userId={}, orderUserId={}", 
                           orderId, userId, order.getUserId());
                return Result.failure(5126); // 您没有权限操作此订单
            }
            
            // 5. 验证订单状态是否为待收货（只有待收货状态可以确认收货）
            if (order.getStatus() == null || order.getStatus() != Order.STATUS_PENDING_RECEIPT) {
                logger.warn("确认收货失败: 订单状态不是待收货: orderId={}, status={}", orderId, order.getStatus());
                return Result.failure(5124);
            }
            
            // 6. 更新订单状态为已完成
            order.setStatus(Order.STATUS_COMPLETED); // 已完成
            order.setCompletionTime(new Date());
            order.setUpdateTime(new Date());
            
            int rows = orderMapper.updateById(order);
            if (rows > 0) {
                logger.info("确认收货成功: orderId={}, userId={}", orderId, userId);
                return Result.success(5009, true); // 确认收货成功
            } else {
                logger.warn("确认收货失败: 更新订单失败: orderId={}", orderId);
                return Result.failure(5124);
            }
            
        } catch (Exception e) {
            logger.error("确认收货失败: 系统异常", e);
            return Result.failure(5124);
        }
    }
    
    @Override
    @Transactional
    public Result<Boolean> reviewOrder(Map<String, Object> data) {
        logger.info("评价订单请求: data={}", data);
        
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("评价订单失败: 用户未登录");
                return Result.failure(5127);
            }
            
            // 2. 解析请求参数
            String orderId = (String) data.get("orderId");
            Object ratingObj = data.get("rating");
            String content = (String) data.get("content");
            @SuppressWarnings("unchecked")
            List<String> images = (List<String>) data.get("images"); // 评价图片列表
            Object isAnonymousObj = data.get("isAnonymous");
            
            if (orderId == null || orderId.isEmpty()) {
                logger.warn("评价订单失败: 订单ID为空");
                return Result.failure(5127);
            }
            
            if (ratingObj == null) {
                logger.warn("评价订单失败: 评分为空");
                return Result.failure(5127);
            }
            
            Integer rating = null;
            try {
                rating = Integer.parseInt(ratingObj.toString());
            } catch (NumberFormatException e) {
                logger.warn("评价订单失败: 评分格式错误: {}", ratingObj);
                return Result.failure(5127);
            }
            
            // 验证评分范围
            if (rating < 1 || rating > 5) {
                logger.warn("评价订单失败: 评分超出范围: rating={}", rating);
                return Result.failure(5127);
            }
            
            // 解析是否匿名
            Integer isAnonymous = 0; // 默认不匿名
            if (isAnonymousObj != null) {
                try {
                    isAnonymous = Integer.parseInt(isAnonymousObj.toString());
                } catch (NumberFormatException e) {
                    logger.warn("是否匿名参数格式错误: {}, 使用默认值0", isAnonymousObj);
                }
            }
            
            // 3. 查询订单
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                logger.warn("评价订单失败: 订单不存在: orderId={}", orderId);
                return Result.failure(5127);
            }
            
            // 4. 验证订单是否属于当前用户
            if (!userId.equals(order.getUserId())) {
                logger.warn("评价订单失败: 无权限: orderId={}, userId={}, orderUserId={}", 
                           orderId, userId, order.getUserId());
                return Result.failure(5127);
            }
            
            // 5. 验证订单状态是否为已完成（只有已完成的订单可以评价）
            if (order.getStatus() == null || order.getStatus() != Order.STATUS_COMPLETED) {
                logger.warn("评价订单失败: 订单状态不是已完成: orderId={}, status={}", orderId, order.getStatus());
                return Result.failure(5127);
            }
            
            // 6. 验证是否已评价
            if (order.getBuyerRate() != null && order.getBuyerRate() == 1) {
                logger.warn("评价订单失败: 订单已评价: orderId={}", orderId);
                return Result.failure(5127);
            }
            
            // 7. 创建茶叶评价记录
            TeaReview review = new TeaReview();
            review.setTeaId(order.getTeaId());
            review.setUserId(userId);
            review.setOrderId(orderId);
            review.setContent(content);
            review.setRating(rating);
            review.setIsAnonymous(isAnonymous);
            review.setLikeCount(0);
            
            // 处理评价图片（将图片列表转为JSON字符串）
            if (images != null && !images.isEmpty()) {
                try {
                    String imagesJson = com.alibaba.fastjson2.JSON.toJSONString(images);
                    review.setImages(imagesJson);
                } catch (Exception e) {
                    logger.warn("评价图片JSON序列化失败: {}", e.getMessage());
                }
            }
            
            review.setCreateTime(new Date());
            review.setUpdateTime(new Date());
            
            // 插入评价记录到tea_review表
            teaReviewMapper.insert(review);
            logger.info("茶叶评价已创建: reviewId={}, teaId={}, orderId={}, rating={}", 
                       review.getId(), review.getTeaId(), orderId, rating);
            
            // 8. 更新订单评价状态
            order.setBuyerRate(1); // 已评价
            order.setUpdateTime(new Date());
            
            int rows = orderMapper.updateById(order);
            if (rows > 0) {
                logger.info("评价订单成功: orderId={}, userId={}, rating={}", orderId, userId, rating);
                return Result.success(5010, true); // 评价提交成功，感谢您的反馈
            } else {
                logger.warn("评价订单失败: 更新订单失败: orderId={}", orderId);
                return Result.failure(5127);
            }
            
        } catch (Exception e) {
            logger.error("评价订单失败: 系统异常", e);
            return Result.failure(5127);
        }
    }
    
    @Override
    @Transactional
    public Result<Boolean> refundOrder(Map<String, Object> data) {
        logger.info("申请退款请求: data={}", data);
        
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("申请退款失败: 用户未登录");
                return Result.failure(5128);
            }
            
            // 2. 解析请求参数
            String orderId = (String) data.get("orderId");
            String reason = (String) data.get("reason");
            
            if (orderId == null || orderId.isEmpty()) {
                logger.warn("申请退款失败: 订单ID为空");
                return Result.failure(5128);
            }
            
            if (reason == null || reason.trim().isEmpty()) {
                logger.warn("申请退款失败: 退款原因为空");
                return Result.failure(5128);
            }
            
            // 3. 查询订单
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                logger.warn("申请退款失败: 订单不存在: orderId={}", orderId);
                return Result.failure(5129); // 订单不存在
            }
            
            // 4. 验证订单是否属于当前用户
            if (!userId.equals(order.getUserId())) {
                logger.warn("申请退款失败: 无权限: orderId={}, userId={}, orderUserId={}", 
                           orderId, userId, order.getUserId());
                return Result.failure(5130); // 您没有权限操作此订单
            }
            
            // 5. 验证订单状态（只有已支付、待发货、待收货、已完成的订单可以申请退款）
            if (order.getStatus() == null || 
                (order.getStatus() != Order.STATUS_PENDING_SHIPMENT && 
                 order.getStatus() != Order.STATUS_PENDING_RECEIPT &&
                 order.getStatus() != Order.STATUS_COMPLETED)) {
                logger.warn("申请退款失败: 订单状态不允许退款: orderId={}, status={}", orderId, order.getStatus());
                return Result.failure(5128);
            }
            
            // 6. 验证是否已有退款申请
            if (order.getRefundStatus() != null && order.getRefundStatus() > 0) {
                logger.warn("申请退款失败: 订单已有退款申请: orderId={}, refundStatus={}", orderId, order.getRefundStatus());
                return Result.failure(5128);
            }
            
            // 7. 更新订单退款信息
            order.setRefundStatus(1); // 1:申请中
            order.setRefundReason(reason);
            order.setRefundApplyTime(new Date());
            order.setUpdateTime(new Date());
            
            int rows = orderMapper.updateById(order);
            if (rows > 0) {
                logger.info("申请退款成功: orderId={}, userId={}, reason={}", orderId, userId, reason);
                return Result.success(5011, true); // 退款申请已提交，等待商家审核
            } else {
                logger.warn("申请退款失败: 更新订单失败: orderId={}", orderId);
                return Result.failure(5128);
            }
            
        } catch (Exception e) {
            logger.error("申请退款失败: 系统异常", e);
            return Result.failure(5128);
        }
    }
    
    @Override
    @Transactional
    public Result<Boolean> processRefund(String id, Map<String, Object> data) {
        logger.info("处理退款请求: orderId={}, data={}", id, data);
        
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("处理退款失败: 用户未登录");
                return Result.failure(5131);
            }
            
            // 2. 解析请求参数
            Object approveObj = data.get("approve");
            String rejectReason = (String) data.get("reason");
            
            if (approveObj == null) {
                logger.warn("处理退款失败: 审批结果为空");
                return Result.failure(5131);
            }
            
            Boolean approve = null;
            if (approveObj instanceof Boolean) {
                approve = (Boolean) approveObj;
            } else {
                try {
                    approve = Boolean.parseBoolean(approveObj.toString());
                } catch (Exception e) {
                    logger.warn("处理退款失败: 审批结果格式错误: {}", approveObj);
                    return Result.failure(5131);
                }
            }
            
            // 3. 查询订单
            Order order = orderMapper.selectById(id);
            if (order == null) {
                logger.warn("处理退款失败: 订单不存在: orderId={}", id);
                return Result.failure(5131);
            }
            
            // 4. 验证权限：只有管理员或订单所属店铺的商家可以处理退款
            if (!UserContext.isAdmin()) {
                // 如果不是管理员，必须是商家
                if (!UserContext.isShop()) {
                    logger.warn("处理退款失败: 权限不足, userId={}", userId);
                    return Result.failure(5132); // 权限不足
                }
                
                // 验证是否是该订单所属店铺的商家
                if (!isShopOwner(userId, order.getShopId())) {
                    logger.warn("处理退款失败: 不是该订单所属店铺的商家, userId={}, shopId={}", userId, order.getShopId());
                    return Result.failure(5132); // 权限不足
                }
            }
            
            // 5. 验证订单退款状态（只能处理申请中的退款）
            if (order.getRefundStatus() == null || order.getRefundStatus() != 1) {
                logger.warn("处理退款失败: 订单退款状态不是申请中: orderId={}, refundStatus={}", id, order.getRefundStatus());
                return Result.failure(5131);
            }
            
            // 6. 根据审批结果更新订单
            if (approve) {
                // 同意退款
                order.setRefundStatus(2); // 2:已同意
                order.setStatus(Order.STATUS_REFUNDED); // 订单状态改为已退款
                order.setRefundProcessTime(new Date());
                order.setUpdateTime(new Date());
                
                int rows = orderMapper.updateById(order);
                if (rows > 0) {
                    // TODO: 这里应该执行实际的退款操作（调用支付接口）
                    logger.info("同意退款成功: orderId={}, userId={}", id, userId);
                    return Result.success(5012, true); // 已同意退款申请
                } else {
                    logger.warn("同意退款失败: 更新订单失败: orderId={}", id);
                    return Result.failure(5131);
                }
            } else {
                // 拒绝退款
                order.setRefundStatus(3); // 3:已拒绝
                order.setRefundRejectReason(rejectReason);
                order.setRefundProcessTime(new Date());
                order.setUpdateTime(new Date());
                
                int rows = orderMapper.updateById(order);
                if (rows > 0) {
                    logger.info("拒绝退款成功: orderId={}, userId={}, reason={}", id, userId, rejectReason);
                    return Result.success(5013, true); // 已拒绝退款申请
                } else {
                    logger.warn("拒绝退款失败: 更新订单失败: orderId={}", id);
                    return Result.failure(5131);
                }
            }
            
        } catch (Exception e) {
            logger.error("处理退款失败: 系统异常", e);
            return Result.failure(5131);
        }
    }
    
    @Override
    public Result<Object> getRefundDetail(String id) {
        logger.info("获取退款详情请求: orderId={}", id);
        
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取退款详情失败: 用户未登录");
                return Result.failure(5133);
            }
            
            // 2. 查询订单
            Order order = orderMapper.selectById(id);
            if (order == null) {
                logger.warn("获取退款详情失败: 订单不存在: orderId={}", id);
                return Result.failure(5134); // 订单不存在
            }
            
            // 3. 验证用户权限（订单所有者或商家/管理员可以查看）
            // 简化处理：只验证是否是订单所有者
            if (!userId.equals(order.getUserId())) {
                logger.warn("获取退款详情失败: 无权限: orderId={}, userId={}, orderUserId={}", 
                           id, userId, order.getUserId());
                return Result.failure(5133);
            }
            
            // 4. 构建退款详情VO
            RefundDetailVO vo = new RefundDetailVO();
            vo.setOrderId(order.getId());
            vo.setRefundStatus(order.getRefundStatus());
            vo.setRefundReason(order.getRefundReason());
            vo.setRefundRejectReason(order.getRefundRejectReason());
            vo.setRefundApplyTime(order.getRefundApplyTime());
            vo.setRefundProcessTime(order.getRefundProcessTime());
            vo.setTotalAmount(order.getTotalAmount());
            vo.setOrderStatus(order.getStatus());
            
            logger.info("获取退款详情成功: orderId={}, userId={}, refundStatus={}", id, userId, order.getRefundStatus());
            return Result.success(200, vo);
            
        } catch (Exception e) {
            logger.error("获取退款详情失败: 系统异常", e);
            return Result.failure(5133);
        }
    }
    
    @Override
    @Transactional
    public Result<Boolean> shipOrder(String id, String logisticsCompany, String logisticsNumber) {
        logger.info("发货请求: orderId={}, logisticsCompany={}, logisticsNumber={}", id, logisticsCompany, logisticsNumber);
        
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("发货失败: 用户未登录");
                return Result.failure(5135);
            }
            
            // 2. 查询订单
            Order order = orderMapper.selectById(id);
            if (order == null) {
                logger.warn("发货失败: 订单不存在: orderId={}", id);
                return Result.failure(5136); // 订单不存在
            }
            
            // 3. 验证订单状态（只有待发货状态可以发货）
            if (order.getStatus() == null || order.getStatus() != Order.STATUS_PENDING_SHIPMENT) {
                logger.warn("发货失败: 订单状态不是待发货: orderId={}, status={}", id, order.getStatus());
                return Result.failure(5135);
            }
            
            // 4. 验证权限：只有管理员或订单所属店铺的商家可以发货
            if (!UserContext.isAdmin()) {
                // 如果不是管理员，必须是商家
                if (!UserContext.isShop()) {
                    logger.warn("发货失败: 权限不足, userId={}", userId);
                    return Result.failure(5137); // 您没有权限操作此订单
                }
                
                // 验证是否是该订单所属店铺的商家
                if (!isShopOwner(userId, order.getShopId())) {
                    logger.warn("发货失败: 不是该订单所属店铺的商家, userId={}, shopId={}", userId, order.getShopId());
                    return Result.failure(5137); // 您没有权限操作此订单
                }
            }
            
            // 5. 更新订单状态为待收货
            order.setStatus(Order.STATUS_PENDING_RECEIPT); // 待收货
            order.setLogisticsCompany(logisticsCompany);
            order.setLogisticsNumber(logisticsNumber);
            order.setShippingTime(new Date());
            order.setUpdateTime(new Date());
            
            int rows = orderMapper.updateById(order);
            if (rows > 0) {
                logger.info("发货成功: orderId={}, userId={}, logisticsCompany={}, logisticsNumber={}", 
                           id, userId, logisticsCompany, logisticsNumber);
                return Result.success(5014, true); // 订单已发货
            } else {
                logger.warn("发货失败: 更新订单失败: orderId={}", id);
                return Result.failure(5135);
            }
            
        } catch (Exception e) {
            logger.error("发货失败: 系统异常", e);
            return Result.failure(5135);
        }
    }
    
    @Override
    @Transactional
    public Result<Boolean> batchShipOrders(Map<String, Object> data) {
        logger.info("批量发货请求: data={}", data);
        
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("批量发货失败: 用户未登录");
                return Result.failure(5138);
            }
            
            // 2. 解析请求参数
            @SuppressWarnings("unchecked")
            List<String> orderIds = (List<String>) data.get("orderIds");
            String logisticsCompany = (String) data.get("logisticsCompany");
            String logisticsNumber = (String) data.get("logisticsNumber");
            
            if (orderIds == null || orderIds.isEmpty()) {
                logger.warn("批量发货失败: 订单ID列表为空");
                return Result.failure(5138);
            }
            
            if (logisticsCompany == null || logisticsCompany.trim().isEmpty()) {
                logger.warn("批量发货失败: 物流公司为空");
                return Result.failure(5138);
            }
            
            if (logisticsNumber == null || logisticsNumber.trim().isEmpty()) {
                logger.warn("批量发货失败: 物流单号为空");
                return Result.failure(5138);
            }
            
            // 3. 验证权限（需要是商家或管理员）
            if (!UserContext.isAdmin() && !UserContext.isShop()) {
                logger.warn("批量发货失败: 用户不是商家或管理员: userId={}, role={}", 
                           userId, UserContext.getCurrentUserRole());
                return Result.failure(5139); // 您没有权限操作此订单
            }
            
            // 4. 如果是商家，需要验证所有订单都属于该商家的店铺
            String merchantShopId = null;
            if (!UserContext.isAdmin() && UserContext.isShop()) {
                // 查询商家对应的店铺ID
                Shop shop = shopMapper.selectByOwnerId(userId);
                if (shop == null) {
                    logger.warn("批量发货失败: 商家没有关联的店铺: userId={}", userId);
                    return Result.failure(5139); // 您没有权限操作此订单
                }
                merchantShopId = shop.getId();
            }
            
            // 5. 循环处理每个订单
            int successCount = 0;
            int failCount = 0;
            
            for (String orderId : orderIds) {
                try {
                    // 查询订单
                    Order order = orderMapper.selectById(orderId);
                    if (order == null) {
                        logger.warn("批量发货: 订单不存在: orderId={}", orderId);
                        failCount++;
                        continue;
                    }
                    
                    // 如果是商家，验证订单是否属于该商家的店铺
                    if (merchantShopId != null && !merchantShopId.equals(order.getShopId())) {
                        logger.warn("批量发货: 订单不属于该商家的店铺: orderId={}, orderShopId={}, merchantShopId={}", 
                                   orderId, order.getShopId(), merchantShopId);
                        failCount++;
                        continue;
                    }
                    
                    // 验证订单状态
                    if (order.getStatus() == null || order.getStatus() != Order.STATUS_PENDING_SHIPMENT) {
                        logger.warn("批量发货: 订单状态不是待发货: orderId={}, status={}", orderId, order.getStatus());
                        failCount++;
                        continue;
                    }
                    
                    // 更新订单
                    order.setStatus(Order.STATUS_PENDING_RECEIPT); // 待收货
                    order.setLogisticsCompany(logisticsCompany);
                    order.setLogisticsNumber(logisticsNumber);
                    order.setShippingTime(new Date());
                    order.setUpdateTime(new Date());
                    
                    int rows = orderMapper.updateById(order);
                    if (rows > 0) {
                        successCount++;
                        logger.info("批量发货成功: orderId={}", orderId);
                    } else {
                        failCount++;
                        logger.warn("批量发货失败: 更新订单失败: orderId={}", orderId);
                    }
                    
                } catch (Exception e) {
                    failCount++;
                    logger.error("批量发货失败: orderId={}, error={}", orderId, e.getMessage());
                }
            }
            
            logger.info("批量发货完成: userId={}, total={}, success={}, fail={}", 
                       userId, orderIds.size(), successCount, failCount);
            
            if (successCount > 0) {
                return Result.success(5015, true); // 批量发货操作已完成
            } else {
                return Result.failure(5138);
            }
            
        } catch (Exception e) {
            logger.error("批量发货失败: 系统异常", e);
            return Result.failure(5138);
        }
    }
    
    @Override
    public Result<Object> getOrderLogistics(String id) {
        logger.info("获取物流信息请求: orderId={}", id);
        
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取物流信息失败: 用户未登录");
                return Result.failure(5140);
            }
            
            // 2. 查询订单
            Order order = orderMapper.selectById(id);
            if (order == null) {
                logger.warn("获取物流信息失败: 订单不存在: orderId={}", id);
                return Result.failure(5141); // 订单不存在
            }
            
            // 3. 验证用户权限（订单所有者或商家/管理员可以查看）
            if (!userId.equals(order.getUserId())) {
                // 如果不是订单的买家，检查是否是商家或管理员
                if (UserContext.isAdmin()) {
                    // 管理员可以查看所有订单的物流信息
                } else if (UserContext.isShop()) {
                    // 商家只能查看自己店铺订单的物流信息
                    if (!isShopOwner(userId, order.getShopId())) {
                        logger.warn("获取物流信息失败: 不是该订单所属店铺的商家: orderId={}, userId={}, shopId={}", 
                                   id, userId, order.getShopId());
                        return Result.failure(5141); // 权限不足
                    }
                } else {
                    // 既不是买家，也不是商家或管理员
                    logger.warn("获取物流信息失败: 无权限: orderId={}, userId={}, orderUserId={}", 
                               id, userId, order.getUserId());
                    return Result.failure(5140);
                }
            }
            
            // 4. 构建物流信息VO
            LogisticsVO vo = new LogisticsVO();
            vo.setLogisticsCompany(order.getLogisticsCompany());
            vo.setLogisticsNumber(order.getLogisticsNumber());
            vo.setShippingTime(order.getShippingTime());
            vo.setTraces(null); // 物流轨迹暂时返回null，后续可对接第三方物流API
            
            logger.info("获取物流信息成功: orderId={}, userId={}, logisticsCompany={}, logisticsNumber={}", 
                       id, userId, order.getLogisticsCompany(), order.getLogisticsNumber());
            return Result.success(200, vo);
            
        } catch (Exception e) {
            logger.error("获取物流信息失败: 系统异常", e);
            return Result.failure(5140);
        }
    }
    
    @Override
    public Result<Object> getOrderStatistics(Map<String, Object> params) {
        try {
            // 获取当前用户ID
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                logger.warn("获取订单统计失败: 用户未登录");
                return Result.failure(5142);
            }
            
            // 获取参数
            String startDate = (String) params.get("startDate");
            String endDate = (String) params.get("endDate");
            String shopId = (String) params.get("shopId");
            
            // 根据用户角色确定查询范围
            String userId = null;
            if (UserContext.isUser()) {
                // 普通用户只能查看自己的订单统计
                userId = currentUserId;
            } else if (UserContext.isShop()) {
                // 商家查看自己店铺的订单统计
                // 查询商家对应的店铺ID
                Shop shop = shopMapper.selectByOwnerId(currentUserId);
                if (shop == null) {
                    logger.warn("获取订单统计失败: 商家没有关联的店铺: userId={}", currentUserId);
                    return Result.failure(5142);
                }
                shopId = shop.getId();
            } else if (UserContext.isAdmin()) {
                // 管理员可以查看所有订单统计，或指定店铺的统计
                // shopId参数由前端传入
            }
            
            // 统计订单总数
            Integer totalOrders = orderMapper.countOrders(userId, shopId, startDate, endDate);
            
            // 统计订单总金额
            java.math.BigDecimal totalAmount = orderMapper.sumOrderAmount(userId, shopId, startDate, endDate);
            
            // 统计各状态订单数量
            List<Map<String, Object>> statusList = orderMapper.countOrdersByStatus(userId, shopId, startDate, endDate);
            Map<String, Integer> statusDistribution = new java.util.HashMap<>();
            // 初始化所有状态为0
            for (int i = 0; i <= 5; i++) {
                statusDistribution.put(String.valueOf(i), 0);
            }
            // 填充实际数据
            for (Map<String, Object> item : statusList) {
                String status = (String) item.get("status");
                Integer count = ((Number) item.get("count")).intValue();
                statusDistribution.put(status, count);
            }
            
            // 查询订单趋势数据
            List<Map<String, Object>> trendDataList = orderMapper.selectOrderTrend(userId, shopId, startDate, endDate);
            List<OrderStatisticsVO.TrendData> trend = new java.util.ArrayList<>();
            for (Map<String, Object> item : trendDataList) {
                OrderStatisticsVO.TrendData trendData = new OrderStatisticsVO.TrendData();
                trendData.setDate(item.get("date").toString());
                trendData.setOrders(((Number) item.get("orders")).intValue());
                trendData.setAmount((java.math.BigDecimal) item.get("amount"));
                trend.add(trendData);
            }
            
            // 构建返回结果
            OrderStatisticsVO vo = new OrderStatisticsVO();
            vo.setTotalOrders(totalOrders);
            vo.setTotalAmount(totalAmount);
            vo.setStatusDistribution(statusDistribution);
            vo.setTrend(trend);
            
            logger.info("获取订单统计成功: userId={}, shopId={}, totalOrders={}", userId, shopId, totalOrders);
            return Result.success(vo);
            
        } catch (Exception e) {
            logger.error("获取订单统计失败: 系统异常", e);
            return Result.failure(5142);
        }
    }
    
    @Override
    public Result<Object> exportOrders(Map<String, Object> params) {
        try {
            // 获取当前用户ID
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                logger.warn("导出订单失败: 用户未登录");
                return Result.failure(5143);
            }
            
            // 验证权限：只有管理员和商家可以导出订单
            if (!UserContext.isAdmin() && !UserContext.isShop()) {
                logger.warn("导出订单失败: 权限不足, userId={}", currentUserId);
                return Result.failure(5143);
            }
            
            // 获取参数
            String startDate = (String) params.get("startDate");
            String endDate = (String) params.get("endDate");
            Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
            String shopId = (String) params.get("shopId");
            String format = (String) params.get("format"); // excel 或 csv
            
            // 根据用户角色确定查询范围
            String userId = null;
            if (UserContext.isShop()) {
                // 商家只能导出自己店铺的订单
                // 查询商家对应的店铺ID
                Shop shop = shopMapper.selectByOwnerId(currentUserId);
                if (shop == null) {
                    logger.warn("导出订单失败: 商家没有关联的店铺: userId={}", currentUserId);
                    return Result.failure(5143);
                }
                shopId = shop.getId();
            } else if (UserContext.isAdmin()) {
                // 管理员可以导出所有订单，或指定店铺的订单
                // shopId参数由前端传入
            }
            
            // 查询订单列表
            List<Order> orders = orderMapper.selectOrdersForExport(userId, shopId, status, startDate, endDate);
            
            if (orders == null || orders.isEmpty()) {
                logger.info("导出订单: 没有符合条件的订单数据");
                return Result.success(new java.util.ArrayList<>());
            }
            
            // TODO: 实现Excel/CSV文件生成
            // 当前版本返回订单数据的JSON格式
            // 如需生成实际的Excel文件，需要添加Apache POI依赖
            
            logger.info("导出订单成功: 共{}条订单, userId={}, shopId={}", orders.size(), userId, shopId);
            return Result.success(orders);
            
        } catch (Exception e) {
            logger.error("导出订单失败: 系统异常", e);
            return Result.failure(5143);
        }
    }
} 