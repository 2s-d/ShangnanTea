package com.shangnantea.utils;

import com.shangnantea.mapper.ShopMapper;
import com.shangnantea.mapper.TeaMapper;
import com.shangnantea.mapper.UserMapper;
import com.shangnantea.mapper.UserNotificationMapper;
import com.shangnantea.model.entity.message.UserNotification;
import com.shangnantea.model.entity.shop.Shop;
import com.shangnantea.model.entity.tea.Tea;
import com.shangnantea.model.entity.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 通知工具类：统一处理通知创建逻辑
 */
@Component
public class NotificationUtils {

    private static final Logger logger = LoggerFactory.getLogger(NotificationUtils.class);

    private static UserNotificationMapper notificationMapper;
    private static UserMapper userMapper;
    private static ShopMapper shopMapper;
    private static TeaMapper teaMapper;

    @Autowired
    public void setNotificationMapper(UserNotificationMapper notificationMapper) {
        NotificationUtils.notificationMapper = notificationMapper;
    }

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        NotificationUtils.userMapper = userMapper;
    }

    @Autowired
    public void setShopMapper(ShopMapper shopMapper) {
        NotificationUtils.shopMapper = shopMapper;
    }

    @Autowired
    public void setTeaMapper(TeaMapper teaMapper) {
        NotificationUtils.teaMapper = teaMapper;
    }

    /**
     * 创建帖子回复通知
     * @param receiverId 接收者ID（帖子作者）
     * @param senderId 发送者ID（回复者）
     * @param postId 帖子ID
     * @param replyId 回复ID
     * @param replyContent 回复内容（用于extraData）
     */
    public static void createPostReplyNotification(String receiverId, String senderId, 
                                                   Long postId, Long replyId, String replyContent) {
        try {
            // 避免给自己发通知
            if (receiverId.equals(senderId)) {
                return;
            }

            UserNotification notification = new UserNotification();
            notification.setUserId(receiverId);
            notification.setSenderId(senderId);
            notification.setType("post_reply");
            notification.setTitle("您的帖子收到新的回复");
            
            // 获取发送者用户名
            String senderName = getSenderName(senderId);
            notification.setContent(senderName + "回复了您的帖子");
            
            notification.setTargetId(String.valueOf(postId));
            notification.setTargetType("post");
            
            // extraData包含回复ID和内容摘要
            String extraData = String.format("{\"replyId\":%d,\"excerpt\":\"%s\"}", 
                replyId, truncateContent(replyContent, 50));
            notification.setExtraData(extraData);
            
            notification.setIsRead(0);
            notification.setCreateTime(new Date());
            
            notificationMapper.insert(notification);
            logger.info("创建帖子回复通知成功: receiverId={}, senderId={}, postId={}", 
                receiverId, senderId, postId);
        } catch (Exception e) {
            logger.error("创建帖子回复通知失败: receiverId={}, senderId={}, postId={}", 
                receiverId, senderId, postId, e);
        }
    }

    /**
     * 创建点赞通知
     * @param receiverId 接收者ID（被点赞内容的作者）
     * @param senderId 发送者ID（点赞者）
     * @param targetType 目标类型（post/reply/article）
     * @param targetId 目标ID
     */
    public static void createLikeNotification(String receiverId, String senderId, 
                                             String targetType, String targetId) {
        try {
            // 避免给自己发通知
            if (receiverId.equals(senderId)) {
                return;
            }

            UserNotification notification = new UserNotification();
            notification.setUserId(receiverId);
            notification.setSenderId(senderId);
            notification.setType("post_reply"); // 点赞通知也归类为post_reply类型（可跳转）
            notification.setTitle("您的" + getTargetTypeName(targetType) + "被点赞");
            
            String senderName = getSenderName(senderId);
            notification.setContent(senderName + "点赞了您的" + getTargetTypeName(targetType));
            
            notification.setTargetId(targetId);
            notification.setTargetType(targetType);
            notification.setExtraData("{\"likeSource\":\"" + targetType + "\"}");
            
            notification.setIsRead(0);
            notification.setCreateTime(new Date());
            
            notificationMapper.insert(notification);
            logger.info("创建点赞通知成功: receiverId={}, senderId={}, targetType={}, targetId={}", 
                receiverId, senderId, targetType, targetId);
        } catch (Exception e) {
            logger.error("创建点赞通知失败: receiverId={}, senderId={}, targetType={}, targetId={}", 
                receiverId, senderId, targetType, targetId, e);
        }
    }

    /**
     * 创建系统公告通知
     * @param userId 接收者ID
     * @param title 通知标题
     * @param content 通知内容
     */
    public static void createSystemNotification(String userId, String title, String content) {
        try {
            UserNotification notification = new UserNotification();
            notification.setUserId(userId);
            notification.setSenderId(null); // 系统通知没有发送者
            notification.setType("system_announcement");
            notification.setTitle(title);
            notification.setContent(content);
            notification.setTargetId(null);
            notification.setTargetType(null);
            notification.setExtraData(null);
            notification.setIsRead(0);
            notification.setCreateTime(new Date());
            
            notificationMapper.insert(notification);
            logger.info("创建系统通知成功: userId={}, title={}", userId, title);
        } catch (Exception e) {
            logger.error("创建系统通知失败: userId={}, title={}", userId, title, e);
        }
    }

    /**
     * 创建外部链接通知
     * @param userId 接收者ID
     * @param title 通知标题
     * @param content 通知内容
     * @param externalUrl 外部链接地址
     */
    public static void createExternalLinkNotification(String userId, String title, 
                                                      String content, String externalUrl) {
        try {
            UserNotification notification = new UserNotification();
            notification.setUserId(userId);
            notification.setSenderId(null);
            notification.setType("external_link");
            notification.setTitle(title);
            notification.setContent(content);
            notification.setTargetId(null);
            notification.setTargetType(null);
            
            String extraData = String.format("{\"externalUrl\":\"%s\"}", externalUrl);
            notification.setExtraData(extraData);
            
            notification.setIsRead(0);
            notification.setCreateTime(new Date());
            
            notificationMapper.insert(notification);
            logger.info("创建外部链接通知成功: userId={}, title={}", userId, title);
        } catch (Exception e) {
            logger.error("创建外部链接通知失败: userId={}, title={}", userId, title, e);
        }
    }

    /**
     * 创建私信通知
     * @param receiverId 接收者ID
     * @param senderId 发送者ID
     * @param sessionId 会话ID
     * @param messageContent 消息内容
     */
    public static void createMessageNotification(String receiverId, String senderId, 
                                                 String sessionId, String messageContent) {
        try {
            UserNotification notification = new UserNotification();
            notification.setUserId(receiverId);
            notification.setSenderId(senderId);
            notification.setType("post_reply"); // 私信通知归类为可跳转类型
            notification.setTitle("您有新的私聊消息");
            
            String senderName = getSenderName(senderId);
            notification.setContent(senderName + "向您发送了一条消息");
            
            notification.setTargetId(sessionId);
            notification.setTargetType("chat_session");
            
            String extraData = String.format("{\"unreadCount\":1,\"excerpt\":\"%s\"}", 
                truncateContent(messageContent, 30));
            notification.setExtraData(extraData);
            
            notification.setIsRead(0);
            notification.setCreateTime(new Date());
            
            notificationMapper.insert(notification);
            logger.info("创建私信通知成功: receiverId={}, senderId={}, sessionId={}", 
                receiverId, senderId, sessionId);
        } catch (Exception e) {
            logger.error("创建私信通知失败: receiverId={}, senderId={}, sessionId={}", 
                receiverId, senderId, sessionId, e);
        }
    }

    /**
     * 获取发送者用户名（从数据库查询）
     */
    private static String getSenderName(String userId) {
        try {
            if (userId == null || userMapper == null) {
                return "用户";
            }
            User user = userMapper.selectById(userId);
            if (user != null && user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
                return user.getNickname();
            }
            if (user != null && user.getUsername() != null) {
                return user.getUsername();
            }
            return "用户";
        } catch (Exception e) {
            logger.warn("获取用户名失败，使用默认值: userId={}", userId, e);
            return "用户";
        }
    }

    /**
     * 获取目标类型中文名称
     */
    private static String getTargetTypeName(String targetType) {
        switch (targetType) {
            case "post": return "帖子";
            case "reply": return "回复";
            case "article": return "文章";
            default: return "内容";
        }
    }

    /**
     * 截断内容（用于extraData中的excerpt）
     */
    private static String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content.replace("\"", "\\\"").replace("\n", " ");
        }
        return content.substring(0, maxLength).replace("\"", "\\\"").replace("\n", " ") + "...";
    }

    /**
     * 创建订单创建通知（给商家）
     * @param orderId 订单ID
     * @param buyerId 买家ID
     * @param shopId 店铺ID
     * @param teaName 茶叶名称
     */
    public static void createOrderCreatedNotification(String orderId, String buyerId, String shopId, String teaName) {
        try {
            // 获取店铺商家ID
            String shopOwnerId = getShopOwnerId(shopId);
            if (shopOwnerId == null || shopOwnerId.equals(buyerId)) {
                return; // 避免给自己发通知
            }

            String buyerName = getSenderName(buyerId);
            UserNotification notification = new UserNotification();
            notification.setUserId(shopOwnerId);
            notification.setSenderId(buyerId);
            notification.setType("post_reply");
            notification.setTitle("您有新的订单");
            notification.setContent(buyerName + "在您的店铺创建了新订单" + orderId + "，商品：" + teaName);
            notification.setTargetId(orderId);
            notification.setTargetType("order");
            notification.setExtraData("{\"orderId\":\"" + orderId + "\",\"teaName\":\"" + teaName + "\"}");
            notification.setIsRead(0);
            notification.setCreateTime(new Date());
            
            notificationMapper.insert(notification);
            logger.info("创建订单创建通知成功: orderId={}, shopOwnerId={}, buyerId={}", orderId, shopOwnerId, buyerId);
        } catch (Exception e) {
            logger.error("创建订单创建通知失败: orderId={}, shopId={}", orderId, shopId, e);
        }
    }

    /**
     * 创建订单支付成功通知（给商家）
     * @param orderId 订单ID
     * @param buyerId 买家ID
     * @param shopId 店铺ID
     */
    public static void createOrderPaidNotification(String orderId, String buyerId, String shopId) {
        try {
            String shopOwnerId = getShopOwnerId(shopId);
            if (shopOwnerId == null || shopOwnerId.equals(buyerId)) {
                return;
            }

            UserNotification notification = new UserNotification();
            notification.setUserId(shopOwnerId);
            notification.setSenderId(buyerId);
            notification.setType("post_reply");
            notification.setTitle("订单已支付");
            notification.setContent("订单" + orderId + "已支付成功，请尽快发货");
            notification.setTargetId(orderId);
            notification.setTargetType("order");
            notification.setExtraData("{\"orderId\":\"" + orderId + "\"}");
            notification.setIsRead(0);
            notification.setCreateTime(new Date());
            
            notificationMapper.insert(notification);
            logger.info("创建订单支付通知成功: orderId={}, shopOwnerId={}", orderId, shopOwnerId);
        } catch (Exception e) {
            logger.error("创建订单支付通知失败: orderId={}, shopId={}", orderId, shopId, e);
        }
    }

    /**
     * 创建订单发货通知（给买家）
     * @param orderId 订单ID
     * @param buyerId 买家ID
     * @param logisticsCompany 物流公司
     * @param logisticsNumber 物流单号
     */
    public static void createOrderShippedNotification(String orderId, String buyerId, String logisticsCompany, String logisticsNumber) {
        try {
            UserNotification notification = new UserNotification();
            notification.setUserId(buyerId);
            notification.setSenderId(null); // 系统通知
            notification.setType("post_reply");
            notification.setTitle("订单已发货");
            notification.setContent("您的订单" + orderId + "已发货，物流单号：" + logisticsNumber);
            notification.setTargetId(orderId);
            notification.setTargetType("order");
            notification.setExtraData("{\"orderId\":\"" + orderId + "\",\"logisticsCompany\":\"" + logisticsCompany + "\",\"logisticsNumber\":\"" + logisticsNumber + "\"}");
            notification.setIsRead(0);
            notification.setCreateTime(new Date());
            
            notificationMapper.insert(notification);
            logger.info("创建订单发货通知成功: orderId={}, buyerId={}", orderId, buyerId);
        } catch (Exception e) {
            logger.error("创建订单发货通知失败: orderId={}, buyerId={}", orderId, buyerId, e);
        }
    }

    /**
     * 创建订单取消通知（给商家）
     * @param orderId 订单ID
     * @param buyerId 买家ID
     * @param shopId 店铺ID
     */
    public static void createOrderCancelledNotification(String orderId, String buyerId, String shopId) {
        try {
            String shopOwnerId = getShopOwnerId(shopId);
            if (shopOwnerId == null || shopOwnerId.equals(buyerId)) {
                return;
            }

            String buyerName = getSenderName(buyerId);
            UserNotification notification = new UserNotification();
            notification.setUserId(shopOwnerId);
            notification.setSenderId(buyerId);
            notification.setType("post_reply");
            notification.setTitle("订单已取消");
            notification.setContent(buyerName + "取消了订单" + orderId);
            notification.setTargetId(orderId);
            notification.setTargetType("order");
            notification.setExtraData("{\"orderId\":\"" + orderId + "\"}");
            notification.setIsRead(0);
            notification.setCreateTime(new Date());
            
            notificationMapper.insert(notification);
            logger.info("创建订单取消通知成功: orderId={}, shopOwnerId={}", orderId, shopOwnerId);
        } catch (Exception e) {
            logger.error("创建订单取消通知失败: orderId={}, shopId={}", orderId, shopId, e);
        }
    }

    /**
     * 创建退款申请通知（给商家）
     * @param orderId 订单ID
     * @param buyerId 买家ID
     * @param shopId 店铺ID
     * @param reason 退款原因
     */
    public static void createRefundAppliedNotification(String orderId, String buyerId, String shopId, String reason) {
        try {
            String shopOwnerId = getShopOwnerId(shopId);
            if (shopOwnerId == null || shopOwnerId.equals(buyerId)) {
                return;
            }

            UserNotification notification = new UserNotification();
            notification.setUserId(shopOwnerId);
            notification.setSenderId(buyerId);
            notification.setType("post_reply");
            notification.setTitle("有新的退款申请");
            notification.setContent("订单" + orderId + "有新的退款申请，请及时处理");
            notification.setTargetId(orderId);
            notification.setTargetType("order");
            notification.setExtraData("{\"orderId\":\"" + orderId + "\",\"reason\":\"" + truncateContent(reason, 50) + "\"}");
            notification.setIsRead(0);
            notification.setCreateTime(new Date());
            
            notificationMapper.insert(notification);
            logger.info("创建退款申请通知成功: orderId={}, shopOwnerId={}", orderId, shopOwnerId);
        } catch (Exception e) {
            logger.error("创建退款申请通知失败: orderId={}, shopId={}", orderId, shopId, e);
        }
    }

    /**
     * 创建退款处理结果通知（给买家）
     * @param orderId 订单ID
     * @param buyerId 买家ID
     * @param approved 是否同意退款
     * @param rejectReason 拒绝原因（如果拒绝）
     */
    public static void createRefundProcessedNotification(String orderId, String buyerId, boolean approved, String rejectReason) {
        try {
            UserNotification notification = new UserNotification();
            notification.setUserId(buyerId);
            notification.setSenderId(null); // 系统通知
            notification.setType("post_reply");
            notification.setTitle("退款申请处理结果");
            if (approved) {
                notification.setContent("您的退款申请已同意，订单号：" + orderId);
            } else {
                notification.setContent("您的退款申请已拒绝，订单号：" + orderId + "，原因：" + (rejectReason != null ? truncateContent(rejectReason, 30) : "无"));
            }
            notification.setTargetId(orderId);
            notification.setTargetType("order");
            notification.setExtraData("{\"orderId\":\"" + orderId + "\",\"approved\":" + approved + ",\"rejectReason\":\"" + (rejectReason != null ? truncateContent(rejectReason, 50) : "") + "\"}");
            notification.setIsRead(0);
            notification.setCreateTime(new Date());
            
            notificationMapper.insert(notification);
            logger.info("创建退款处理结果通知成功: orderId={}, buyerId={}, approved={}", orderId, buyerId, approved);
        } catch (Exception e) {
            logger.error("创建退款处理结果通知失败: orderId={}, buyerId={}", orderId, buyerId, e);
        }
    }

    /**
     * 创建评价通知（给商家）
     * @param teaId 茶叶ID
     * @param reviewerId 评价者ID
     * @param orderId 订单ID
     * @param rating 评分
     */
    public static void createReviewNotification(String teaId, String reviewerId, String orderId, Integer rating) {
        try {
            // 获取茶叶所属店铺的商家ID
            Tea tea = teaMapper != null ? teaMapper.selectById(teaId) : null;
            if (tea == null || tea.getShopId() == null) {
                logger.warn("创建评价通知失败: 茶叶不存在或店铺ID为空, teaId={}", teaId);
                return;
            }

            String shopOwnerId = getShopOwnerId(tea.getShopId());
            if (shopOwnerId == null || shopOwnerId.equals(reviewerId)) {
                return; // 避免给自己发通知
            }

            String reviewerName = getSenderName(reviewerId);
            String teaName = tea.getName() != null ? tea.getName() : "茶叶";
            UserNotification notification = new UserNotification();
            notification.setUserId(shopOwnerId);
            notification.setSenderId(reviewerId);
            notification.setType("post_reply");
            notification.setTitle("您的茶叶收到新评价");
            notification.setContent(reviewerName + "对您的茶叶" + teaName + "进行了评价（" + rating + "星）");
            notification.setTargetId(teaId);
            notification.setTargetType("tea");
            notification.setExtraData("{\"teaId\":\"" + teaId + "\",\"orderId\":\"" + orderId + "\",\"rating\":" + rating + "}");
            notification.setIsRead(0);
            notification.setCreateTime(new Date());
            
            notificationMapper.insert(notification);
            logger.info("创建评价通知成功: teaId={}, shopOwnerId={}, reviewerId={}", teaId, shopOwnerId, reviewerId);
        } catch (Exception e) {
            logger.error("创建评价通知失败: teaId={}, reviewerId={}", teaId, reviewerId, e);
        }
    }

    /**
     * 创建关注通知（给被关注者）
     * @param followerId 关注者ID
     * @param targetType 目标类型（user/shop）
     * @param targetId 目标ID（用户ID或店铺ID）
     */
    public static void createFollowNotification(String followerId, String targetType, String targetId) {
        try {
            String receiverId = null;
            String targetName = null;
            String jumpPath = null;

            if ("user".equals(targetType)) {
                // 关注用户，通知被关注的用户
                receiverId = targetId;
                User user = userMapper != null ? userMapper.selectById(targetId) : null;
                targetName = user != null && user.getNickname() != null ? user.getNickname() : "用户";
                jumpPath = "/user/profile/" + followerId;
            } else if ("shop".equals(targetType)) {
                // 关注店铺，通知店铺商家
                Shop shop = shopMapper != null ? shopMapper.selectById(targetId) : null;
                if (shop == null || shop.getOwnerId() == null) {
                    logger.warn("创建关注通知失败: 店铺不存在或商家ID为空, shopId={}", targetId);
                    return;
                }
                receiverId = shop.getOwnerId();
                targetName = shop.getShopName() != null ? shop.getShopName() : "店铺";
                jumpPath = "/shop/detail/" + targetId;
            } else {
                return; // 不支持的类型
            }

            if (receiverId == null || receiverId.equals(followerId)) {
                return; // 避免给自己发通知
            }

            String followerName = getSenderName(followerId);
            UserNotification notification = new UserNotification();
            notification.setUserId(receiverId);
            notification.setSenderId(followerId);
            notification.setType("post_reply");
            notification.setTitle("您被关注了");
            notification.setContent(followerName + "关注了您");
            notification.setTargetId(targetId);
            notification.setTargetType(targetType);
            notification.setExtraData("{\"targetType\":\"" + targetType + "\",\"targetId\":\"" + targetId + "\",\"targetName\":\"" + targetName + "\"}");
            notification.setIsRead(0);
            notification.setCreateTime(new Date());
            
            notificationMapper.insert(notification);
            logger.info("创建关注通知成功: followerId={}, targetType={}, targetId={}, receiverId={}", 
                followerId, targetType, targetId, receiverId);
        } catch (Exception e) {
            logger.error("创建关注通知失败: followerId={}, targetType={}, targetId={}", 
                followerId, targetType, targetId, e);
        }
    }

    /**
     * 创建收藏通知（给商家，仅tea和shop类型）
     * @param collectorId 收藏者ID
     * @param itemType 收藏类型（tea/shop）
     * @param itemId 收藏项ID（茶叶ID或店铺ID）
     */
    public static void createFavoriteNotification(String collectorId, String itemType, String itemId) {
        try {
            // 只对tea和shop类型发送通知
            if (!"tea".equals(itemType) && !"shop".equals(itemType)) {
                return;
            }

            String receiverId = null;
            String itemName = null;
            String jumpPath = null;

            if ("tea".equals(itemType)) {
                // 收藏茶叶，通知茶叶所属店铺的商家
                Tea tea = teaMapper != null ? teaMapper.selectById(itemId) : null;
                if (tea == null || tea.getShopId() == null) {
                    logger.warn("创建收藏通知失败: 茶叶不存在或店铺ID为空, teaId={}", itemId);
                    return;
                }
                receiverId = getShopOwnerId(tea.getShopId());
                itemName = tea.getName() != null ? tea.getName() : "茶叶";
                jumpPath = "/tea/detail/" + itemId;
            } else if ("shop".equals(itemType)) {
                // 收藏店铺，通知店铺商家
                Shop shop = shopMapper != null ? shopMapper.selectById(itemId) : null;
                if (shop == null || shop.getOwnerId() == null) {
                    logger.warn("创建收藏通知失败: 店铺不存在或商家ID为空, shopId={}", itemId);
                    return;
                }
                receiverId = shop.getOwnerId();
                itemName = shop.getShopName() != null ? shop.getShopName() : "店铺";
                jumpPath = "/shop/detail/" + itemId;
            }

            if (receiverId == null || receiverId.equals(collectorId)) {
                return; // 避免给自己发通知
            }

            String collectorName = getSenderName(collectorId);
            UserNotification notification = new UserNotification();
            notification.setUserId(receiverId);
            notification.setSenderId(collectorId);
            notification.setType("post_reply");
            notification.setTitle("您的" + ("tea".equals(itemType) ? "茶叶" : "店铺") + "被收藏");
            notification.setContent(collectorName + "收藏了您的" + itemName);
            notification.setTargetId(itemId);
            notification.setTargetType(itemType);
            notification.setExtraData("{\"itemType\":\"" + itemType + "\",\"itemId\":\"" + itemId + "\",\"itemName\":\"" + itemName + "\"}");
            notification.setIsRead(0);
            notification.setCreateTime(new Date());
            
            notificationMapper.insert(notification);
            logger.info("创建收藏通知成功: collectorId={}, itemType={}, itemId={}, receiverId={}", 
                collectorId, itemType, itemId, receiverId);
        } catch (Exception e) {
            logger.error("创建收藏通知失败: collectorId={}, itemType={}, itemId={}", 
                collectorId, itemType, itemId, e);
        }
    }

    /**
     * 获取店铺商家ID
     */
    private static String getShopOwnerId(String shopId) {
        try {
            if (shopId == null || shopMapper == null) {
                return null;
            }
            Shop shop = shopMapper.selectById(shopId);
            return shop != null ? shop.getOwnerId() : null;
        } catch (Exception e) {
            logger.warn("获取店铺商家ID失败: shopId={}", shopId, e);
            return null;
        }
    }

    /**
     * 创建商家认证审核结果通知（纯文本系统公告）
     *
     * @param userId  认证申请人用户ID
     * @param approved 是否通过
     * @param reason  拒绝原因（可选）
     */
    public static void createCertificationResultNotification(String userId, boolean approved, String reason) {
        try {
            if (userId == null) {
                return;
            }
            UserNotification notification = new UserNotification();
            notification.setUserId(userId);
            notification.setSenderId(null); // 系统通知
            notification.setType("system_announcement");
            notification.setTitle("商家认证审核结果");
            if (approved) {
                notification.setContent("您的商家认证申请已通过，欢迎入驻商南茶文化平台。");
            } else {
                String msg = (reason != null && !reason.trim().isEmpty())
                        ? reason
                        : "请根据提示完善资料后重新提交。";
                notification.setContent("您的商家认证申请未通过，原因：" + truncateContent(msg, 80));
            }
            notification.setTargetId(null);
            notification.setTargetType(null);
            notification.setExtraData(null);
            notification.setIsRead(0);
            notification.setCreateTime(new Date());

            notificationMapper.insert(notification);
            logger.info("创建商家认证审核结果通知成功: userId={}, approved={}", userId, approved);
        } catch (Exception e) {
            logger.error("创建商家认证审核结果通知失败: userId={}, approved={}", userId, approved, e);
        }
    }

    /**
     * 创建帖子审核结果通知（纯文本系统公告，可跳转到帖子详情）
     *
     * @param userId  发帖人用户ID
     * @param postId  帖子ID
     * @param title   帖子标题
     * @param approved 是否通过
     * @param approveReason 审核通过原因（可选，仅当approved=true时使用）
     * @param selectedReason 选择的拒绝原因（仅当approved=false时使用）
     * @param customReason 管理员自定义拒绝原因（仅当approved=false时使用）
     */
    public static void createPostAuditResultNotification(String userId, Long postId, String title,
                                                         boolean approved, String approveReason,
                                                         String selectedReason, String customReason) {
        try {
            if (userId == null || postId == null) {
                return;
            }
            UserNotification notification = new UserNotification();
            notification.setUserId(userId);
            notification.setSenderId(null); // 系统通知
            notification.setType("post_audit"); // 帖子审核结果，可在前端按类型跳转到帖子详情
            notification.setTitle("帖子审核结果");

            String safeTitle = (title != null && !title.trim().isEmpty())
                    ? truncateContent(title.trim(), 50)
                    : "你的帖子";

            if (approved) {
                // 审核通过：如果有原因则显示，没有则显示默认消息
                String content = "您发布的《" + safeTitle + "》已通过审核。";
                if (approveReason != null && !approveReason.trim().isEmpty()) {
                    content += "审核意见：" + truncateContent(approveReason.trim(), 100);
                }
                notification.setContent(content);
            } else {
                // 审核拒绝：必须包含选择的拒绝原因和管理员自定义原因
                StringBuilder reasonMsg = new StringBuilder();
                if (selectedReason != null && !selectedReason.trim().isEmpty()) {
                    reasonMsg.append("拒绝原因：").append(truncateContent(selectedReason.trim(), 50));
                }
                if (customReason != null && !customReason.trim().isEmpty()) {
                    if (reasonMsg.length() > 0) {
                        reasonMsg.append("；");
                    }
                    reasonMsg.append("审核意见：").append(truncateContent(customReason.trim(), 100));
                }
                if (reasonMsg.length() == 0) {
                    reasonMsg.append("请根据提示修改内容后重新提交审核。");
                }
                notification.setContent("您发布的《" + safeTitle + "》未通过审核。" + reasonMsg.toString());
            }

            notification.setTargetId(String.valueOf(postId));
            notification.setTargetType("post");
            notification.setExtraData(null);
            notification.setIsRead(0);
            notification.setCreateTime(new Date());

            notificationMapper.insert(notification);
            logger.info("创建帖子审核结果通知成功: userId={}, postId={}, approved={}", userId, postId, approved);
        } catch (Exception e) {
            logger.error("创建帖子审核结果通知失败: userId={}, postId={}, approved={}", userId, postId, approved, e);
        }
    }
}
