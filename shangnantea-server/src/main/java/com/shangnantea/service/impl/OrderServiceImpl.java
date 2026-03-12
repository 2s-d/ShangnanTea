package com.shangnantea.service.impl;

import com.shangnantea.common.api.Result;
import com.shangnantea.mapper.OrderMapper;
import com.shangnantea.mapper.OrderAddressMapper;
import com.shangnantea.mapper.PaymentMapper;
import com.shangnantea.mapper.ShopMapper;
import com.shangnantea.mapper.ShoppingCartMapper;
import com.shangnantea.mapper.TeaMapper;
import com.shangnantea.mapper.TeaReviewMapper;
import com.shangnantea.mapper.TeaSpecificationMapper;
import com.shangnantea.mapper.UserAddressMapper;
import com.shangnantea.model.entity.order.Order;
import com.shangnantea.model.entity.order.Payment;
import com.shangnantea.model.entity.order.OrderAddress;
import com.shangnantea.model.entity.order.ShoppingCart;
import com.shangnantea.model.entity.shop.Shop;
import com.shangnantea.model.entity.tea.Tea;
import com.shangnantea.model.entity.tea.TeaReview;
import com.shangnantea.model.entity.tea.TeaSpecification;
import com.shangnantea.model.entity.user.User;
import com.shangnantea.model.entity.user.UserAddress;
import com.shangnantea.model.vo.order.CartItemVO;
import com.shangnantea.model.vo.order.CartResponseVO;
import com.shangnantea.model.vo.order.LogisticsVO;
import com.shangnantea.model.vo.order.OrderDetailVO;
import com.shangnantea.model.vo.order.OrderVO;
import com.shangnantea.model.vo.order.RefundDetailVO;
import com.shangnantea.model.vo.order.OrderStatisticsVO;
import com.shangnantea.security.context.UserContext;
import com.shangnantea.service.AlipayService;
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
import java.text.SimpleDateFormat;
import java.util.Random;

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
    private OrderAddressMapper orderAddressMapper;
    
    @Autowired
    private TeaReviewMapper teaReviewMapper;
    
    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private PaymentMapper paymentMapper;
    
    @Autowired
    private AlipayService alipayService;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
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
            Boolean fromCart = (Boolean) data.get("fromCart"); // 是否从购物车创建
            
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
            
            // 3. 查询并校验地址簿地址
            UserAddress baseAddress = userAddressMapper.selectById(addressId);
            if (baseAddress == null || !userId.equals(baseAddress.getUserId())) {
                logger.warn("创建订单失败: 收货地址不存在或不属于当前用户: addressId={}, userId={}", addressId, userId);
                return Result.failure(5110);
            }
            
            // 4. 验证每个商品并计算总价（用于生成支付单）
            BigDecimal totalPrice = BigDecimal.ZERO;
            List<Order> orderList = new ArrayList<>();
            List<String> cartItemIdsToRemove = new ArrayList<>(); // 需要删除的购物车项ID
            
            for (Map<String, Object> item : items) {
                String teaId = (String) item.get("teaId");
                Object specIdObj = item.get("specId");
                Object quantityObj = item.get("quantity");
                Object cartItemIdObj = item.get("cartItemId"); // 购物车项ID（如果从购物车创建）
                
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
                
                // 如果是从购物车创建，记录购物车项ID
                if (fromCart != null && fromCart && cartItemIdObj != null) {
                    cartItemIdsToRemove.add(cartItemIdObj.toString());
                }
                
                // 验证商品是否存在
                Tea tea = null;
                try {
                    tea = teaMapper.selectById(teaId);
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
                        TeaSpecification spec = teaSpecificationMapper.selectById(specId);
                        
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
                order.setId(generateOrderId());
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
            
            // 5. 生成支付单（支付批次），支持多订单一次支付
            String paymentId = generatePaymentId();
            Date now = new Date();

            Payment payment = new Payment();
            payment.setId(paymentId);
            payment.setUserId(userId);
            payment.setTotalAmount(totalPrice);
            payment.setStatus(Payment.STATUS_PENDING); // 待支付
            payment.setPaymentMethod(null); // 支付方式在实际发起支付时再填
            payment.setOrderCount(orderList.size());
            payment.setChannelTradeNo(null);
            payment.setRemark(null);
            payment.setCreateTime(now);
            payment.setSuccessTime(null);
            payment.setUpdateTime(now);

            paymentMapper.insert(payment);
            logger.info("支付单已创建: paymentId={}, userId={}, totalAmount={}, orderCount={}",
                    paymentId, userId, totalPrice, orderList.size());

            // 6. 插入订单地址快照和订单数据
            for (Order order : orderList) {
                // 为当前订单创建地址快照
                OrderAddress orderAddress = new OrderAddress();
                orderAddress.setOrderId(order.getId());
                orderAddress.setUserId(userId);
                orderAddress.setReceiverName(baseAddress.getReceiverName());
                orderAddress.setReceiverPhone(baseAddress.getReceiverPhone());
                orderAddress.setProvince(baseAddress.getProvince());
                orderAddress.setCity(baseAddress.getCity());
                orderAddress.setDistrict(baseAddress.getDistrict());
                orderAddress.setDetailAddress(baseAddress.getDetailAddress());
                orderAddress.setCreateTime(now);
                orderAddress.setUpdateTime(now);
                orderAddressMapper.insert(orderAddress);

                order.setPaymentId(paymentId);
                // 订单地址ID指向订单地址快照
                order.setAddressId(orderAddress.getId());
                orderMapper.insert(order);
                logger.info("订单已创建: orderId={}, paymentId={}, teaId={}, quantity={}, totalAmount={}",
                        order.getId(), paymentId, order.getTeaId(), order.getQuantity(), order.getTotalAmount());
                
                // 创建订单创建通知（给商家）
                com.shangnantea.utils.NotificationUtils.createOrderCreatedNotification(
                    order.getId(), userId, order.getShopId(), order.getTeaName()
                );
            }
            
            // 7. 如果是从购物车创建，删除对应的购物车项
            if (fromCart != null && fromCart && !cartItemIdsToRemove.isEmpty()) {
                for (String cartItemId : cartItemIdsToRemove) {
                    try {
                        Integer id = Integer.parseInt(cartItemId);
                        ShoppingCart cart = cartMapper.selectById(id);
                        // 验证购物车项属于当前用户
                        if (cart != null && userId.equals(cart.getUserId())) {
                            cartMapper.deleteById(id);
                            logger.info("已删除购物车项: cartItemId={}, userId={}", id, userId);
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("购物车项ID格式错误: {}", cartItemId);
                    }
                }
            }
            
            // 7. 构建返回数据（兼容旧字段，同时返回支付单号和所有订单ID）
            Map<String, Object> responseData = new HashMap<>();
            // 保留旧字段：第一个订单ID
            responseData.put("orderId", orderList.get(0).getId());
            // 新增：支付单号和全部订单ID列表
            List<String> orderIds = new ArrayList<>();
            for (Order order : orderList) {
                orderIds.add(order.getId());
            }
            responseData.put("orderIds", orderIds);
            responseData.put("paymentId", paymentId);
            responseData.put("totalPrice", totalPrice);
            responseData.put("status", Order.STATUS_PENDING_PAYMENT);
            
            logger.info("创建订单成功: userId={}, orderCount={}, totalPrice={}, fromCart={}, removedCartItems={}", 
                       userId, orderList.size(), totalPrice, fromCart, cartItemIdsToRemove.size());
            return Result.success(5005, responseData);
            
        } catch (Exception e) {
            logger.error("创建订单失败: 系统异常", e);
            return Result.failure(5110);
        }
    }
    
    @Override
    @Transactional
    public Result<CartItemVO> addToCart(String teaId, Integer quantity, String specificationId) {
        logger.info("添加购物车请求: teaId={}, quantity={}, specificationId={}", teaId, quantity, specificationId);
        
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            User currentUser = UserContext.getCurrentUser();
            logger.debug("当前用户上下文: userId={}, user={}, role={}", 
                        userId, currentUser != null ? currentUser.getUsername() : "null",
                        currentUser != null ? currentUser.getRole() : "null");
            
            if (userId == null) {
                logger.error("添加购物车失败: 用户未登录 - UserContext.getCurrentUserId()返回null");
                logger.error("可能原因: 1) JWT拦截器未正确设置用户上下文 2) role验证失败 3) token无效");
                return Result.failure(5101);
            }
            
            // 2. 验证商品是否存在
            Tea tea = null;
            try {
                tea = teaMapper.selectById(teaId);
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
                    TeaSpecification spec = teaSpecificationMapper.selectById(specId);
                    
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
                CartItemVO itemVO = buildCartItemVO(existingCart, tea, specName, price, stock);
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
                CartItemVO itemVO = buildCartItemVO(cart, tea, specName, price, stock);
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
    private CartItemVO buildCartItemVO(ShoppingCart cart, Tea tea, String specName, BigDecimal price, Integer stock) {
        CartItemVO itemVO = new CartItemVO();
        itemVO.setId(String.valueOf(cart.getId()));
        itemVO.setTeaId(cart.getTeaId());
        itemVO.setTeaName(tea.getName());
        // 处理茶叶图片URL
        String teaImage = tea.getMainImage();
        if (teaImage != null && !teaImage.trim().isEmpty()) {
            if (teaImage.startsWith("http://") || teaImage.startsWith("https://")) {
                itemVO.setTeaImage(teaImage);
            } else {
                itemVO.setTeaImage(FileUploadUtils.generateAccessUrl(teaImage, baseUrl));
            }
        } else {
            itemVO.setTeaImage(null);
        }
        itemVO.setSpecId(cart.getSpecId() != null ? String.valueOf(cart.getSpecId()) : null);
        itemVO.setSpecName(specName);
        itemVO.setPrice(price);
        itemVO.setQuantity(cart.getQuantity());
        itemVO.setSelected(cart.getSelected() != null && cart.getSelected() == 1);
        itemVO.setShopId(tea.getShopId());
        itemVO.setStock(stock);
        
        // 查询店铺名称
        if (tea.getShopId() != null) {
            try {
                Shop shop = shopMapper.selectById(tea.getShopId());
                if (shop != null) {
                    itemVO.setShopName(shop.getShopName());
                }
            } catch (Exception e) {
                logger.warn("查询店铺信息失败: shopId={}, error={}", tea.getShopId(), e.getMessage());
            }
        }
        
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
                tea = teaMapper.selectById(cart.getTeaId());
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
                    TeaSpecification spec = teaSpecificationMapper.selectById(newSpecId);
                    
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
                
                // 检查新规格是否已在购物车中存在（排除当前正在更新的购物车项）
                ShoppingCart existingCartWithNewSpec = cartMapper.selectByUserIdAndTeaIdAndSpecId(userId, cart.getTeaId(), newSpecId);
                if (existingCartWithNewSpec != null && !existingCartWithNewSpec.getId().equals(cart.getId())) {
                    // 使用订单模块新增失败码 5147：此规格已存在（参见 code-message-mapping.md）
                    logger.warn("更新购物车失败: 此规格已存在于购物车中: cartId={}, newSpecId={}, existingCartId={}", 
                               id, newSpecId, existingCartWithNewSpec.getId());
                    return Result.failure(5147);
                }
                
                // 如果新规格就是当前规格，直接返回成功（无需更新）
                if (cart.getSpecId() != null && cart.getSpecId().equals(newSpecId)) {
                    logger.info("购物车规格未变更: cartId={}, specId={}", id, newSpecId);
                    CartItemVO itemVO = buildCartItemVO(cart, tea, newSpecName, newPrice, stock);
                    return Result.success(5002, itemVO); // 规格已更新（实际未变更）
                }
                
                // 更新规格
                cart.setSpecId(newSpecId);
                cart.setUpdateTime(new Date());
                cartMapper.updateById(cart);
                
                logger.info("购物车规格已更新: cartId={}, newSpecId={}", id, newSpecId);
                
                // 构建返回VO
                CartItemVO itemVO = buildCartItemVO(cart, tea, newSpecName, newPrice, stock);
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
                    TeaSpecification spec = teaSpecificationMapper.selectById(specId);
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
                if (quantity == null || stock < quantity.intValue()) {
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
                CartItemVO itemVO = buildCartItemVO(cart, tea, specName, price, stock);
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
                // 返回 code=5003，data=null
                return Result.success(5003);
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
                // 查询商品信息
                Tea tea = null;
                if (cart.getTeaId() != null) {
                    try {
                        tea = teaMapper.selectById(cart.getTeaId());
                    } catch (NumberFormatException e) {
                        logger.warn("商品ID格式错误: {}", cart.getTeaId());
                        continue; // 跳过无效商品
                    }
                }
                
                if (tea == null) {
                    logger.warn("商品不存在，跳过: teaId={}", cart.getTeaId());
                    continue; // 跳过不存在的商品
                }
                
                // 查询规格信息和库存
                BigDecimal price = null;
                String specName = null;
                Integer stock = 0;
                
                if (cart.getSpecId() != null) {
                    try {
                        TeaSpecification spec = teaSpecificationMapper.selectById(cart.getSpecId());
                        if (spec != null) {
                            specName = spec.getSpecName();
                            price = spec.getPrice();
                            stock = spec.getStock() != null ? spec.getStock() : 0;
                        } else {
                            logger.warn("规格不存在，使用商品默认价格: specId={}, teaId={}", cart.getSpecId(), cart.getTeaId());
                            // 规格不存在，使用商品默认价格和库存
                            price = tea.getPrice();
                            stock = tea.getStock() != null ? tea.getStock() : 0;
                        }
                    } catch (Exception e) {
                        logger.warn("查询规格信息失败: specId={}, error={}", cart.getSpecId(), e.getMessage());
                        // 查询失败，使用商品默认价格和库存
                        price = tea.getPrice();
                        stock = tea.getStock() != null ? tea.getStock() : 0;
                    }
                } else {
                    // 没有规格，使用商品默认价格和库存
                    price = tea.getPrice();
                    stock = tea.getStock() != null ? tea.getStock() : 0;
                }
                
                // 构建CartItemVO
                CartItemVO itemVO = new CartItemVO();
                itemVO.setId(String.valueOf(cart.getId()));
                itemVO.setTeaId(cart.getTeaId());
                itemVO.setTeaName(tea.getName());
                // 处理茶叶图片URL
                String teaImage = tea.getMainImage();
                if (teaImage != null && !teaImage.trim().isEmpty()) {
                    if (teaImage.startsWith("http://") || teaImage.startsWith("https://")) {
                        itemVO.setTeaImage(teaImage);
                    } else {
                        itemVO.setTeaImage(FileUploadUtils.generateAccessUrl(teaImage, baseUrl));
                    }
                } else {
                    itemVO.setTeaImage(null);
                }
                itemVO.setSpecId(cart.getSpecId() != null ? String.valueOf(cart.getSpecId()) : null);
                itemVO.setSpecName(specName);
                itemVO.setPrice(price);
                itemVO.setQuantity(cart.getQuantity());
                itemVO.setSelected(cart.getSelected() != null && cart.getSelected() == 1);
                itemVO.setShopId(tea.getShopId());
                itemVO.setStock(stock);
                
                // 查询店铺名称
                if (tea.getShopId() != null) {
                    try {
                        Shop shop = shopMapper.selectById(tea.getShopId());
                        if (shop != null) {
                            itemVO.setShopName(shop.getShopName());
                        }
                    } catch (Exception e) {
                        logger.warn("查询店铺信息失败: shopId={}, error={}", tea.getShopId(), e.getMessage());
                    }
                }
                
                // 计算总价（仅计算选中的商品）
                if (itemVO.getSelected() && price != null && cart.getQuantity() != null) {
                    BigDecimal itemTotal = price.multiply(new BigDecimal(cart.getQuantity()));
                    totalPrice = totalPrice.add(itemTotal);
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
                // 返回 code=5004，data=null
                return Result.success(5004);
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
                // 返回 code=5004，data=null
                return Result.success(5004);
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
            // 管理员：查询平台直售订单（shop_id='PLATFORM'）
            // 商户：查询自己店铺的订单（shop_id=自己的店铺ID）
            // 普通用户：查询自己作为买家的订单
            List<Order> orderList;
            boolean isAdmin = UserContext.isAdmin();
            boolean isShop = UserContext.isShop();
            
            if (isAdmin) {
                // 管理员只查询平台直售订单（shop_id='PLATFORM'）
                String platformShopId = "PLATFORM";
                orderList = orderMapper.selectByShopIdAndStatus(platformShopId, status);
            } else if (isShop) {
                // 商户查询自己店铺的订单
                Shop shop = shopMapper.selectByOwnerId(userId);
                if (shop == null) {
                    logger.warn("获取订单列表失败: 商家没有关联的店铺: userId={}", userId);
                    // 返回空列表，而不是错误，因为商户可能还没有创建店铺
                    orderList = new ArrayList<>();
                } else {
                    orderList = orderMapper.selectByShopIdAndStatus(shop.getId(), status);
                }
            } else {
                // 普通用户只查询自己作为买家的订单
                orderList = orderMapper.selectByUserIdAndStatus(userId, status);
            }
            
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
                // 处理茶叶图片URL
                String teaImage = order.getTeaImage();
                if (teaImage != null && !teaImage.trim().isEmpty()) {
                    if (teaImage.startsWith("http://") || teaImage.startsWith("https://")) {
                        vo.setTeaImage(teaImage);
                    } else {
                        vo.setTeaImage(FileUploadUtils.generateAccessUrl(teaImage, baseUrl));
                    }
                } else {
                    vo.setTeaImage(null);
                }
                vo.setSpecName(order.getSpecName());
                vo.setQuantity(order.getQuantity());
                vo.setPrice(order.getPrice());
                vo.setShopId(order.getShopId());
                vo.setIsReviewed(order.getBuyerRate());
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
            
            // 3. 权限验证
            //    - 普通用户：只能查看自己的订单
            //    - 商家：只能查看自己店铺的订单
            //    - 管理员：可以查看所有订单
            if (!userId.equals(order.getUserId())) {
                boolean isAdmin = UserContext.isAdmin();
                boolean isShop = UserContext.isShop();

                if (isAdmin) {
                    // 管理员：放行（后续通过前端控制只在管理端入口使用）
                } else if (isShop) {
                    // 商家：必须是该订单所属店铺的商家
                    if (!isShopOwner(userId, order.getShopId())) {
                        logger.warn("获取订单详情失败: 商家无权限: orderId={}, userId={}, shopId={}",
                                   id, userId, order.getShopId());
                        return Result.failure(5115); // 您没有权限操作此订单
                    }
                } else {
                    logger.warn("获取订单详情失败: 普通用户无权限: orderId={}, userId={}, orderUserId={}",
                               id, userId, order.getUserId());
                    return Result.failure(5115); // 您没有权限操作此订单
                }
            }
            
            // 4. 查询订单地址快照信息
            Map<String, Object> addressInfo = new HashMap<>();
            if (order.getAddressId() != null) {
                OrderAddress address = orderAddressMapper.selectById(order.getAddressId());
                if (address != null) {
                    addressInfo.put("receiverName", address.getReceiverName());
                    addressInfo.put("receiverPhone", address.getReceiverPhone());
                    addressInfo.put("province", address.getProvince());
                    addressInfo.put("city", address.getCity());
                    addressInfo.put("district", address.getDistrict());
                    addressInfo.put("detailAddress", address.getDetailAddress());
                }
            }
            
            // 5. 查询支付单信息（如果存在）
            Integer paymentStatus = null;
            if (order.getPaymentId() != null && !order.getPaymentId().trim().isEmpty()) {
                Payment payment = paymentMapper.selectById(order.getPaymentId());
                if (payment != null) {
                    paymentStatus = payment.getStatus();
                }
            }
            
            // 6. 转换为VO
            OrderDetailVO vo = new OrderDetailVO();
            vo.setId(order.getId());
            vo.setUserId(order.getUserId());
            vo.setStatus(order.getStatus());
            vo.setTotalPrice(order.getTotalAmount());
            vo.setTeaId(order.getTeaId());
            vo.setTeaName(order.getTeaName());
            // 处理茶叶图片URL
            String teaImage = order.getTeaImage();
            if (teaImage != null && !teaImage.trim().isEmpty()) {
                if (teaImage.startsWith("http://") || teaImage.startsWith("https://")) {
                    vo.setTeaImage(teaImage);
                } else {
                    vo.setTeaImage(FileUploadUtils.generateAccessUrl(teaImage, baseUrl));
                }
            } else {
                vo.setTeaImage(null);
            }
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
            vo.setShopId(order.getShopId());
            vo.setIsReviewed(order.getBuyerRate());
            
            // 退款相关字段
            vo.setRefundStatus(order.getRefundStatus());
            vo.setRefundReason(order.getRefundReason());
            vo.setRefundRejectReason(order.getRefundRejectReason());
            vo.setRefundApplyTime(order.getRefundApplyTime());
            vo.setRefundProcessTime(order.getRefundProcessTime());
            
            // 支付相关字段
            vo.setPaymentId(order.getPaymentId());
            vo.setPaymentStatus(paymentStatus);
            
            // 取消相关字段
            vo.setCancelTime(order.getCancelTime());
            vo.setCancelReason(order.getCancelReason());
            
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
            
            // 2. 解析请求参数（兼容两种方式：paymentId 优先，其次 orderId）
            String paymentId = (String) data.get("paymentId");
            String orderId = (String) data.get("orderId");
            String paymentMethod = (String) data.get("paymentMethod");
            
            if ((paymentId == null || paymentId.isEmpty()) &&
                (orderId == null || orderId.isEmpty())) {
                logger.warn("支付订单失败: paymentId 与 orderId 同时为空");
                return Result.failure(5117);
            }
            
            if (paymentMethod == null || paymentMethod.isEmpty()) {
                logger.warn("支付订单失败: 支付方式为空");
                return Result.failure(5117);
            }
            
            // 3. 验证支付方式是否支持
            if (!"alipay".equals(paymentMethod) && !"wechat".equals(paymentMethod)) {
                logger.warn("支付订单失败: 不支持的支付方式: paymentMethod={}", paymentMethod);
                return Result.failure(5118); // 不支持的支付方式
            }
            
            // 4. 查找支付单（优先使用 paymentId，其次根据 orderId 反查）
            Payment payment = null;
            if (paymentId != null && !paymentId.isEmpty()) {
                payment = paymentMapper.selectById(paymentId);
                if (payment == null) {
                    logger.warn("支付订单失败: 支付单不存在: paymentId={}", paymentId);
                    return Result.failure(5117);
                }
                // 校验支付单所属用户
                if (!userId.equals(payment.getUserId())) {
                    logger.warn("支付订单失败: 支付单不属于当前用户: paymentId={}, userId={}, paymentUserId={}",
                            paymentId, userId, payment.getUserId());
                    return Result.failure(5117);
                }
            } else {
                // 仅提供 orderId 的老调用路径：根据订单反查 paymentId
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                logger.warn("支付订单失败: 订单不存在: orderId={}", orderId);
                return Result.failure(5117);
            }
            if (!userId.equals(order.getUserId())) {
                logger.warn("支付订单失败: 无权限: orderId={}, userId={}, orderUserId={}", 
                           orderId, userId, order.getUserId());
                return Result.failure(5117);
            }
                if (order.getPaymentId() == null || order.getPaymentId().isEmpty()) {
                    logger.warn("支付订单失败: 订单缺少支付单号: orderId={}", orderId);
                    return Result.failure(5117);
                }
                paymentId = order.getPaymentId();
                payment = paymentMapper.selectById(paymentId);
                if (payment == null) {
                    logger.warn("支付订单失败: 通过订单找到的支付单不存在: orderId={}, paymentId={}", orderId, paymentId);
                    return Result.failure(5117);
                }
            }

            // 5. 校验支付单状态
            if (payment.getStatus() == null || payment.getStatus() != Payment.STATUS_PENDING) {
                logger.warn("支付订单失败: 支付单状态不是待支付: paymentId={}, status={}", paymentId, payment.getStatus());
                if (payment.getStatus() != null && payment.getStatus() == Payment.STATUS_SUCCESS) {
                    // 支付单已支付，认为订单已支付
                    return Result.success(5006, null);
                }
                return Result.failure(5117);
            }

            // 6. 校验支付单关联的订单列表（至少一单，且都为当前用户，且待付款，总金额一致）
            List<Order> orderList = orderMapper.selectByPaymentId(paymentId);
            if (orderList == null || orderList.isEmpty()) {
                logger.warn("支付订单失败: 支付单未关联任何订单: paymentId={}", paymentId);
                return Result.failure(5117);
            }

            BigDecimal sumAmount = BigDecimal.ZERO;
            for (Order o : orderList) {
                if (!userId.equals(o.getUserId())) {
                    logger.warn("支付订单失败: 存在不属于当前用户的订单: orderId={}, userId={}, orderUserId={}",
                            o.getId(), userId, o.getUserId());
                    return Result.failure(5117);
                }
                if (o.getStatus() == null || o.getStatus() != Order.STATUS_PENDING_PAYMENT) {
                    logger.warn("支付订单失败: 存在非待付款订单: orderId={}, status={}", o.getId(), o.getStatus());
                    if (o.getStatus() != null && o.getStatus() > Order.STATUS_PENDING_PAYMENT) {
                        return Result.success(5006, null);
                    }
                    return Result.failure(5117);
                }
                if (o.getTotalAmount() != null) {
                    sumAmount = sumAmount.add(o.getTotalAmount());
                }
            }

            if (payment.getTotalAmount() == null || payment.getTotalAmount().compareTo(sumAmount) != 0) {
                logger.warn("支付订单失败: 支付单金额与订单汇总不一致: paymentId={}, paymentAmount={}, sumAmount={}",
                        paymentId, payment.getTotalAmount(), sumAmount);
                return Result.failure(5117);
            }
            
            // 7. 调用支付宝生成支付表单
            if ("alipay".equals(paymentMethod)) {
                try {
                    // 获取支付信息（多订单一次支付）
                    String subject = "商南茶城订单支付-" + paymentId;
                    String totalAmount = payment.getTotalAmount().toString();
                    
                    // 调用支付宝服务生成支付表单
                    String paymentForm = alipayService.createPaymentForm(paymentId, subject, totalAmount);
                    
                    // 返回支付表单HTML
                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("formHtml", paymentForm);
                    responseData.put("paymentId", paymentId);
                    // 为兼容旧前端，仍返回第一个订单ID
                    responseData.put("orderId", orderList.get(0).getId());
                    
                    logger.info("生成支付表单成功: paymentId={}, orderCount={}, totalAmount={}",
                            paymentId, orderList.size(), payment.getTotalAmount());
                    return Result.success(5007, responseData); // 支付表单生成成功，正在跳转...
                    
                } catch (Exception e) {
                    logger.error("生成支付表单失败: orderId={}, error={}", orderId, e.getMessage());
                    return Result.failure(5119); // 生成支付表单失败
                }
            } else if ("wechat".equals(paymentMethod)) {
                // TODO: 微信支付暂未实现
                logger.warn("微信支付暂未实现");
                return Result.failure(5118); // 不支持的支付方式
            }
            
            return Result.failure(5117);
            
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

            // 5.1 待付款取消：按“支付单批次”整体取消，避免多订单支付单出现“部分取消导致金额不一致/无法再次支付”
            if (order.getStatus() == Order.STATUS_PENDING_PAYMENT) {
                if (cancelReason == null || cancelReason.trim().isEmpty()) {
                    // 手动取消要求必须填原因（前端会强制输入，这里再兜底一次）
                    cancelReason = "用户手动取消";
                }

                String paymentId = order.getPaymentId();
                if (paymentId != null && !paymentId.isEmpty()) {
                    Payment payment = paymentMapper.selectById(paymentId);
                    // 关闭支付单（仅在待支付时关闭）
                    if (payment != null && payment.getStatus() != null && payment.getStatus() == Payment.STATUS_PENDING) {
                        payment.setStatus(Payment.STATUS_CLOSED);
                        payment.setRemark(cancelReason);
                        payment.setUpdateTime(new Date());
                        paymentMapper.updateById(payment);
                    }

                    // 取消该支付单下所有待付款订单
                    List<Order> batchOrders = orderMapper.selectByPaymentId(paymentId);
                    Date now = new Date();
                    int cancelledCount = 0;
                    if (batchOrders != null && !batchOrders.isEmpty()) {
                        for (Order o : batchOrders) {
                            if (o == null) continue;
                            // 仅取消待付款订单，其他状态不动
                            if (o.getStatus() != null && o.getStatus() == Order.STATUS_PENDING_PAYMENT) {
                                o.setStatus(Order.STATUS_CANCELLED);
                                o.setCancelReason(cancelReason);
                                o.setCancelTime(now);
                                o.setUpdateTime(now);
                                orderMapper.updateById(o);
                                cancelledCount++;
                            }
                        }
                    }

                    logger.info("待付款订单按支付单批次取消成功: paymentId={}, cancelledOrders={}, userId={}, reason={}",
                            paymentId, cancelledCount, userId, cancelReason);
                    return Result.success(5008);
                }
                // 极端情况：缺少paymentId，则继续走单笔取消（保持兼容）
            }
            
            // 6. 如果订单已支付（待发货状态），需要恢复库存
            boolean needRestoreStock = (order.getStatus() == Order.STATUS_PENDING_SHIPMENT);
            if (needRestoreStock) {
                if (order.getSpecId() != null) {
                    // 有规格，恢复规格库存
                    int rows = teaSpecificationMapper.restoreStock(order.getSpecId(), order.getQuantity());
                    logger.info("已恢复规格库存: orderId={}, specId={}, quantity={}, rows={}", 
                               orderId, order.getSpecId(), order.getQuantity(), rows);
                } else {
                    // 无规格，恢复茶叶库存和销量
                    int rows = teaMapper.restoreStockAndSales(order.getTeaId(), order.getQuantity());
                    logger.info("已恢复库存和销量: orderId={}, teaId={}, quantity={}, rows={}", 
                               orderId, order.getTeaId(), order.getQuantity(), rows);
                }
            }
            
            // 7. 更新订单状态为已取消
            order.setStatus(Order.STATUS_CANCELLED); // 已取消
            order.setCancelReason(cancelReason);
            order.setCancelTime(new Date());
            order.setUpdateTime(new Date());
            
            int rows = orderMapper.updateById(order);
            if (rows > 0) {
                logger.info("取消订单成功: orderId={}, userId={}, reason={}", orderId, userId, cancelReason);
                
                // 创建订单取消通知（给商家）
                com.shangnantea.utils.NotificationUtils.createOrderCancelledNotification(
                    order.getId(), userId, order.getShopId()
                );
                
                // 返回 code=5008，data=null（订单已取消）
                return Result.success(5008);
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
                // 返回 code=5009，data=null（确认收货成功）
                return Result.success(5009);
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
            // likeCount已从数据库删除，使用动态计算
            
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
                
                // 创建评价通知（给商家）
                com.shangnantea.utils.NotificationUtils.createReviewNotification(
                    order.getTeaId(), userId, orderId, rating
                );
                
                // 返回 code=5010，data=null（评价提交成功）
                return Result.success(5010);
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
            
            // 7. 更新订单退款信息（记录退款前状态，并将订单状态置为退款中）
            order.setRefundBeforeStatus(order.getStatus());
            order.setStatus(Order.STATUS_REFUNDING);
            order.setRefundStatus(1); // 1:申请中
            order.setRefundReason(reason);
            order.setRefundApplyTime(new Date());
            order.setUpdateTime(new Date());
            
            int rows = orderMapper.updateById(order);
            if (rows > 0) {
                logger.info("申请退款成功: orderId={}, userId={}, reason={}", orderId, userId, reason);
                
                // 创建退款申请通知（给商家）
                com.shangnantea.utils.NotificationUtils.createRefundAppliedNotification(
                    order.getId(), userId, order.getShopId(), reason
                );
                
                // 返回 code=5011，data=null（退款申请已提交）
                return Result.success(5011);
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
    public Result<Boolean> updateOrderAddress(Map<String, Object> data) {
        logger.info("修改订单收货地址请求: data={}", data);
        
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("修改订单地址失败: 用户未登录");
                return Result.failure(5147);
            }
            
            // 2. 解析请求参数
            String orderId = (String) data.get("orderId");
            Object addressIdObj = data.get("addressId");
            
            if (orderId == null || orderId.isEmpty()) {
                logger.warn("修改订单地址失败: 订单ID为空");
                return Result.failure(5147);
            }
            if (addressIdObj == null) {
                logger.warn("修改订单地址失败: 地址簿ID为空");
                return Result.failure(5147);
            }
            
            Integer addressId;
            try {
                addressId = Integer.parseInt(addressIdObj.toString());
            } catch (NumberFormatException e) {
                logger.warn("修改订单地址失败: 地址簿ID格式错误: {}", addressIdObj);
                return Result.failure(5147);
            }
            
            // 3. 查询订单
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                logger.warn("修改订单地址失败: 订单不存在: orderId={}", orderId);
                return Result.failure(5147);
            }
            
            // 4. 验证订单是否属于当前用户
            if (!userId.equals(order.getUserId())) {
                logger.warn("修改订单地址失败: 无权限: orderId={}, userId={}, orderUserId={}",
                        orderId, userId, order.getUserId());
                return Result.failure(5147);
            }
            
            // 5. 仅在待付款和待发货状态允许修改地址
            if (order.getStatus() == null ||
                    (order.getStatus() != Order.STATUS_PENDING_PAYMENT &&
                     order.getStatus() != Order.STATUS_PENDING_SHIPMENT)) {
                logger.warn("修改订单地址失败: 订单状态不允许修改地址: orderId={}, status={}", orderId, order.getStatus());
                return Result.failure(5147);
            }
            
            // 6. 查询并校验地址簿地址
            UserAddress baseAddress = userAddressMapper.selectById(addressId);
            if (baseAddress == null || !userId.equals(baseAddress.getUserId())) {
                logger.warn("修改订单地址失败: 收货地址不存在或不属于当前用户: addressId={}, userId={}", addressId, userId);
                return Result.failure(5147);
            }
            
            Date now = new Date();
            
            // 7. 查询订单地址快照，如不存在则创建一条新的
            OrderAddress orderAddress = null;
            if (order.getAddressId() != null) {
                orderAddress = orderAddressMapper.selectById(order.getAddressId());
            }
            
            if (orderAddress == null) {
                orderAddress = new OrderAddress();
                orderAddress.setOrderId(order.getId());
                orderAddress.setUserId(userId);
                orderAddress.setCreateTime(now);
            }
            
            // 复制地址簿信息到订单地址快照
            orderAddress.setReceiverName(baseAddress.getReceiverName());
            orderAddress.setReceiverPhone(baseAddress.getReceiverPhone());
            orderAddress.setProvince(baseAddress.getProvince());
            orderAddress.setCity(baseAddress.getCity());
            orderAddress.setDistrict(baseAddress.getDistrict());
            orderAddress.setDetailAddress(baseAddress.getDetailAddress());
            orderAddress.setUpdateTime(now);
            
            if (orderAddress.getId() == null) {
                orderAddressMapper.insert(orderAddress);
                // 同步更新订单的 addressId
                order.setAddressId(orderAddress.getId());
                order.setUpdateTime(now);
                orderMapper.updateById(order);
            } else {
                orderAddressMapper.updateById(orderAddress);
            }
            
            logger.info("修改订单地址成功: orderId={}, userId={}, newAddressId={}", orderId, userId, addressId);
            return Result.success(5017);
            
        } catch (Exception e) {
            logger.error("修改订单地址失败: 系统异常", e);
            return Result.failure(5147);
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
                // 同意退款 - 恢复库存和销量
                if (order.getSpecId() != null) {
                    // 有规格，恢复规格库存
                    int rows = teaSpecificationMapper.restoreStock(order.getSpecId(), order.getQuantity());
                    logger.info("退款成功，已恢复规格库存: orderId={}, specId={}, quantity={}, rows={}", 
                               id, order.getSpecId(), order.getQuantity(), rows);
                } else {
                    // 无规格，恢复茶叶库存和销量
                    int rows = teaMapper.restoreStockAndSales(order.getTeaId(), order.getQuantity());
                    logger.info("退款成功，已恢复库存和销量: orderId={}, teaId={}, quantity={}, rows={}", 
                               id, order.getTeaId(), order.getQuantity(), rows);
                }
                
                order.setRefundStatus(2); // 2:已同意
                order.setStatus(Order.STATUS_REFUNDED); // 订单状态改为已退款
                order.setRefundProcessTime(new Date());
                order.setUpdateTime(new Date());
                
                int rows = orderMapper.updateById(order);
                if (rows > 0) {
                    // TODO: 这里应该执行实际的退款操作（调用支付接口）
                    logger.info("同意退款成功: orderId={}, userId={}", id, userId);
                    
                    // 创建退款处理结果通知（给买家）
                    com.shangnantea.utils.NotificationUtils.createRefundProcessedNotification(
                        order.getId(), order.getUserId(), true, null
                    );
                    
                    // 返回 code=5012，data=null（已同意退款申请）
                    return Result.success(5012);
                } else {
                    logger.warn("同意退款失败: 更新订单失败: orderId={}", id);
                    return Result.failure(5131);
                }
            } else {
                // 拒绝退款：恢复到退款前的订单状态（如果有记录），并更新退款状态
                order.setRefundStatus(3); // 3:已拒绝
                if (order.getRefundBeforeStatus() != null) {
                    order.setStatus(order.getRefundBeforeStatus());
                }
                order.setRefundRejectReason(rejectReason);
                order.setRefundProcessTime(new Date());
                order.setUpdateTime(new Date());
                
                int rows = orderMapper.updateById(order);
                if (rows > 0) {
                    logger.info("拒绝退款成功: orderId={}, userId={}, reason={}", id, userId, rejectReason);
                    
                    // 创建退款处理结果通知（给买家）
                    com.shangnantea.utils.NotificationUtils.createRefundProcessedNotification(
                        order.getId(), order.getUserId(), false, rejectReason
                    );
                    
                    // 返回 code=5013，data=null（已拒绝退款申请）
                    return Result.success(5013);
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
            if (!userId.equals(order.getUserId())) {
                // 如果不是订单的买家，检查是否是商家或管理员
                if (UserContext.isAdmin()) {
                    // 管理员可以查看所有订单的退款详情
                } else if (UserContext.isShop()) {
                    // 商家只能查看自己店铺订单的退款详情
                    if (!isShopOwner(userId, order.getShopId())) {
                        logger.warn("获取退款详情失败: 不是该订单所属店铺的商家: orderId={}, userId={}, shopId={}", 
                                   id, userId, order.getShopId());
                        return Result.failure(5133); // 权限不足
                    }
                } else {
                    // 既不是买家，也不是商家或管理员
                    logger.warn("获取退款详情失败: 无权限: orderId={}, userId={}, orderUserId={}", 
                               id, userId, order.getUserId());
                    return Result.failure(5133);
                }
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
                
                // 创建订单发货通知（给买家）
                com.shangnantea.utils.NotificationUtils.createOrderShippedNotification(
                    order.getId(), order.getUserId(), logisticsCompany, logisticsNumber
                );
                
                // 返回 code=5014，data=null（订单已发货）
                return Result.success(5014);
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
                // 返回 code=5015，data=null（批量发货完成）
                return Result.success(5015);
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
                // 管理员只查看平台直售订单统计（shop_id='PLATFORM'）
                shopId = "PLATFORM";
            }
            
            // 统计订单总数
            Integer totalOrders = orderMapper.countOrders(userId, shopId, startDate, endDate);
            
            // 统计订单总金额
            java.math.BigDecimal totalAmount = orderMapper.sumOrderAmount(userId, shopId, startDate, endDate);
            
            // 统计各状态订单数量（Mapper 返回多行，每行一个状态）
            java.util.List<java.util.Map<String, Object>> statusList = orderMapper.countOrdersByStatus(userId, shopId, startDate, endDate);
            Map<String, Integer> statusDistribution = new java.util.HashMap<>();
            if (statusList != null) {
                for (java.util.Map<String, Object> row : statusList) {
                    if (row == null) {
                        continue;
                    }
                    Object statusKey = row.get("status");
                    Object countVal = row.get("count");
                    if (statusKey == null || countVal == null) {
                        continue;
                    }
                    String statusStr = statusKey.toString();
                    int count = ((Number) countVal).intValue();
                    statusDistribution.put(statusStr, count);
            }
            }
            // 确保所有状态都有值（初始化为0）
            for (int i = 0; i <= 5; i++) {
                statusDistribution.putIfAbsent(String.valueOf(i), 0);
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
            // String format = (String) params.get("format"); // excel 或 csv（当前未使用，预留扩展）
            
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
                // 管理员只能导出平台直售订单（shop_id='PLATFORM'）
                shopId = "PLATFORM";
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
    
    @Override
    @Transactional
    public String handleAlipayNotify(Map<String, String> params) {
        logger.info("收到支付宝异步回调: params={}", params);
        
        try {
            // 1. 验证签名
            boolean signVerified = alipayService.verifyNotify(params);
            if (!signVerified) {
                logger.warn("支付宝回调签名验证失败");
                return "failure";
            }
            
            // 2. 获取回调参数
            String tradeStatus = params.get("trade_status");
            String outTradeNo = params.get("out_trade_no"); // 这里期望为支付单号 paymentId
            String tradeNo = params.get("trade_no"); // 支付宝交易号
            String totalAmount = params.get("total_amount"); // 支付金额
            
            logger.info("支付宝回调参数: outTradeNo={}, tradeNo={}, tradeStatus={}, totalAmount={}", 
                       outTradeNo, tradeNo, tradeStatus, totalAmount);
            
            // 3. 先按支付单号处理（推荐路径：out_trade_no = paymentId）
            Payment payment = paymentMapper.selectById(outTradeNo);
            if (payment != null) {
                // 验证支付单状态
                if (payment.getStatus() != null && payment.getStatus() != Payment.STATUS_PENDING) {
                    logger.warn("支付宝回调: 支付单状态不是待支付: paymentId={}, status={}", outTradeNo, payment.getStatus());
                    // 已处理过，返回 success 避免重复
                    return "success";
                }

                // 验证金额
                BigDecimal callbackAmount = new BigDecimal(totalAmount);
                if (payment.getTotalAmount() == null || payment.getTotalAmount().compareTo(callbackAmount) != 0) {
                    logger.warn("支付宝回调: 支付单金额不匹配: paymentId={}, paymentAmount={}, callbackAmount={}",
                            outTradeNo, payment.getTotalAmount(), callbackAmount);
                    return "failure";
                }

                // 判断交易状态
                if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                    logger.warn("支付宝回调: 交易状态不是成功: tradeStatus={}", tradeStatus);
                    return "failure";
                }

                // 查询该支付单下所有订单
                List<Order> orderList = orderMapper.selectByPaymentId(outTradeNo);
                if (orderList == null || orderList.isEmpty()) {
                    logger.warn("支付宝回调: 支付单未关联任何订单: paymentId={}", outTradeNo);
                    return "failure";
                }

                // 逐单扣减库存 & 更新订单状态
                Date now = new Date();
                for (Order order : orderList) {
                    // 只处理待付款订单，其余视为已处理过
                    if (order.getStatus() == null || order.getStatus() != Order.STATUS_PENDING_PAYMENT) {
                        logger.info("支付宝回调: 跳过非待付款订单: orderId={}, status={}", order.getId(), order.getStatus());
                        continue;
                    }

                    // 扣库存
                    if (order.getSpecId() != null) {
                        int rows = teaSpecificationMapper.updateStock(order.getSpecId(), order.getQuantity());
                        if (rows == 0) {
                            logger.warn("支付宝回调: 库存不足或规格不存在: specId={}, quantity={}",
                                    order.getSpecId(), order.getQuantity());
                            return "failure";
                        }
                        logger.info("已扣减规格库存: specId={}, quantity={}", order.getSpecId(), order.getQuantity());
                    } else {
                        int rows = teaMapper.updateStockAndSales(order.getTeaId(), order.getQuantity());
                        if (rows == 0) {
                            logger.warn("支付宝回调: 库存不足或商品不存在: teaId={}, quantity={}",
                                    order.getTeaId(), order.getQuantity());
                            return "failure";
                        }
                        logger.info("已扣减库存并增加销量: teaId={}, quantity={}", order.getTeaId(), order.getQuantity());
                    }

                    // 更新订单状态为待发货
                    order.setStatus(Order.STATUS_PENDING_SHIPMENT);
                    order.setPaymentMethod("alipay");
                    order.setPaymentTime(now);
                    order.setUpdateTime(now);

                    int rows = orderMapper.updateById(order);
                    if (rows > 0) {
                        // 创建订单支付成功通知（给商家）
                        com.shangnantea.utils.NotificationUtils.createOrderPaidNotification(
                                order.getId(), order.getUserId(), order.getShopId()
                        );
                    } else {
                        logger.warn("支付宝回调: 更新订单失败: orderId={}", order.getId());
                        return "failure";
                    }
                }

                // 更新支付单状态
                payment.setStatus(Payment.STATUS_SUCCESS);
                payment.setPaymentMethod("alipay");
                payment.setChannelTradeNo(tradeNo);
                payment.setSuccessTime(now);
                payment.setUpdateTime(now);
                paymentMapper.updateById(payment);

                logger.info("支付宝回调处理成功（按支付单）: paymentId={}, tradeNo={}", outTradeNo, tradeNo);
                return "success";
            }

            // 4. 兼容旧逻辑：out_trade_no 为单个订单ID 的情况
            String orderId = outTradeNo;
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                logger.warn("支付宝回调: 既不是支付单也不是订单: outTradeNo={}", outTradeNo);
                return "failure";
            }
            
            // 验证订单状态
            if (order.getStatus() != Order.STATUS_PENDING_PAYMENT) {
                logger.warn("支付宝回调(兼容旧逻辑): 订单状态不是待付款: orderId={}, status={}", orderId, order.getStatus());
                return "success";
            }
            
            // 验证金额
            BigDecimal callbackAmount = new BigDecimal(totalAmount);
            if (order.getTotalAmount().compareTo(callbackAmount) != 0) {
                logger.warn("支付宝回调(兼容旧逻辑): 金额不匹配: orderId={}, orderAmount={}, callbackAmount={}", 
                           orderId, order.getTotalAmount(), callbackAmount);
                return "failure";
            }
            
            // 判断交易状态
            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                logger.warn("支付宝回调(兼容旧逻辑): 交易状态不是成功: tradeStatus={}", tradeStatus);
                return "failure";
            }

            // 扣减库存（兼容旧逻辑）
                if (order.getSpecId() != null) {
                    int rows = teaSpecificationMapper.updateStock(order.getSpecId(), order.getQuantity());
                    if (rows == 0) {
                    logger.warn("支付宝回调(兼容旧逻辑): 库存不足或规格不存在: specId={}, quantity={}", 
                                   order.getSpecId(), order.getQuantity());
                        return "failure";
                    }
                    logger.info("已扣减规格库存: specId={}, quantity={}", order.getSpecId(), order.getQuantity());
                } else {
                    int rows = teaMapper.updateStockAndSales(order.getTeaId(), order.getQuantity());
                    if (rows == 0) {
                    logger.warn("支付宝回调(兼容旧逻辑): 库存不足或商品不存在: teaId={}, quantity={}", 
                                   order.getTeaId(), order.getQuantity());
                        return "failure";
                    }
                    logger.info("已扣减库存并增加销量: teaId={}, quantity={}", order.getTeaId(), order.getQuantity());
                }
                
                // 更新订单状态为待发货
            Date now = new Date();
            order.setStatus(Order.STATUS_PENDING_SHIPMENT);
                order.setPaymentMethod("alipay");
            order.setPaymentTime(now);
            order.setUpdateTime(now);
                
                int rows = orderMapper.updateById(order);
                if (rows > 0) {
                logger.info("支付宝回调处理成功(兼容旧逻辑): orderId={}, tradeNo={}", orderId, tradeNo);
                    
                    com.shangnantea.utils.NotificationUtils.createOrderPaidNotification(
                        order.getId(), order.getUserId(), order.getShopId()
                    );
                    
                    return "success";
                } else {
                logger.warn("支付宝回调(兼容旧逻辑): 更新订单失败: orderId={}", orderId);
                return "failure";
            }
            
        } catch (Exception e) {
            logger.error("处理支付宝回调失败", e);
            return "failure";
        }
    }
    
    /**
     * 生成订单ID：OD + 日期(YYYYMMDD) + 3位序号
     * @return 订单ID
     */
    private String generateOrderId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        Random random = new Random();
        StringBuilder sb = new StringBuilder("OD");
        sb.append(dateStr);
        // 生成3位序号
        for (int i = 0; i < 3; i++) {
            sb.append(random.nextInt(10));
        }
        String orderId = sb.toString();
        // 检查ID是否已存在，存在则重新生成
        if (orderMapper.selectById(orderId) != null) {
            return generateOrderId(); // 递归调用直到生成唯一ID
        }
        return orderId;
    }

    /**
     * 生成支付单ID：PM + 日期(YYYYMMDD) + 3位序号
     * @return 支付单ID
     */
    private String generatePaymentId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        Random random = new Random();
        StringBuilder sb = new StringBuilder("PM");
        sb.append(dateStr);
        // 生成3位序号
        for (int i = 0; i < 3; i++) {
            sb.append(random.nextInt(10));
        }
        String paymentId = sb.toString();
        // 检查ID是否已存在，存在则重新生成
        if (paymentMapper.selectById(paymentId) != null) {
            return generatePaymentId(); // 递归调用直到生成唯一ID
        }
        return paymentId;
    }
}
