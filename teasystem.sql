/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80040
 Source Host           : localhost:3306
 Source Schema         : teasystem.sql

 Target Server Type    : MySQL
 Target Server Version : 80040
 File Encoding         : 65001

 Date: 25/01/2026 08:20:59
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chat_messages
-- ----------------------------
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID',
  `sender_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者ID',
  `receiver_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `content_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'text' COMMENT '内容类型(text文本,image图片)',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_sender_receiver`(`sender_id` ASC, `receiver_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of chat_messages
-- ----------------------------
INSERT INTO `chat_messages` VALUES (1, 'cy100002_cy100003_private', 'cy100002', 'cy100003', '请问这款茶叶如何冲泡最好？', 'text', 0, NULL, '2025-03-26 09:03:26');

-- ----------------------------
-- Table structure for chat_sessions
-- ----------------------------
DROP TABLE IF EXISTS `chat_sessions`;
CREATE TABLE `chat_sessions`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID',
  `initiator_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发起者用户ID',
  `receiver_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
  `last_message` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后一条消息内容',
  `last_message_time` datetime NULL DEFAULT NULL COMMENT '最后消息时间',
  `initiator_unread` int NULL DEFAULT 0 COMMENT '发起者未读数',
  `receiver_unread` int NULL DEFAULT 0 COMMENT '接收者未读数',
  `is_pinned` tinyint(1) NULL DEFAULT 0 COMMENT '是否置顶(0否,1是)',
  `session_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'private' COMMENT '会话类型(private私聊,customer客服)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态:1正常,0禁用',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_users`(`initiator_id` ASC, `receiver_id` ASC, `session_type` ASC) USING BTREE,
  INDEX `idx_initiator`(`initiator_id` ASC) USING BTREE,
  INDEX `idx_receiver`(`receiver_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of chat_sessions
-- ----------------------------
INSERT INTO `chat_sessions` VALUES ('cy100002_cy100003_private', 'cy100002', 'cy100003', '请问这款茶叶如何冲泡最好？', '2025-03-26 09:03:26', 0, 1, 0, 'private', 1, '2025-03-26 09:03:26', '2025-03-26 09:03:26');

-- ----------------------------
-- Table structure for forum_posts
-- ----------------------------
DROP TABLE IF EXISTS `forum_posts`;
CREATE TABLE `forum_posts`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发帖用户ID',
  `topic_id` int NOT NULL COMMENT '所属版块ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帖子标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帖子内容',
  `summary` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '帖子摘要',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图片',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '图片JSON数组，格式:[{\"url\":\"图片链接\",\"desc\":\"图片描述\"}]',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览量',
  `reply_count` int NULL DEFAULT 0 COMMENT '回复数',
  `is_sticky` tinyint(1) NULL DEFAULT 0 COMMENT '是否置顶',
  `is_essence` tinyint(1) NULL DEFAULT 0 COMMENT '是否精华',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态:1正常,0待审核,2已删除',
  `last_reply_time` datetime NULL DEFAULT NULL COMMENT '最后回复时间',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_topic_id`(`topic_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_last_reply_time`(`last_reply_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of forum_posts
-- ----------------------------
INSERT INTO `forum_posts` VALUES (1, 'cy100002', 1, '如何正确冲泡绿茶？', '绿茶的冲泡温度一般控制在80℃左右较为适宜，水温过高会破坏茶叶中的营养物质，使茶汤变苦涩...(此处省略详细内容)', '分享绿茶冲泡的正确温度和时间', '/images/brewing_green_tea.jpg', NULL, 58, 0, 0, 1, 1, NULL, '2025-03-26 09:03:26', '2025-03-26 09:03:26');

-- ----------------------------
-- Table structure for forum_replies
-- ----------------------------
DROP TABLE IF EXISTS `forum_replies`;
CREATE TABLE `forum_replies`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回复用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回复内容',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父回复ID,为空表示一级回复',
  `to_user_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '回复目标用户ID',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态:1正常,0待审核,2已删除',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of forum_replies
-- ----------------------------
INSERT INTO `forum_replies` VALUES (1, 1, 'cy100003', '补充一点，不同品种的绿茶冲泡温度也有差异，比如龙井适合85℃左右，而碧螺春则可以稍低一些，75℃左右更能体现其鲜爽的口感。', NULL, NULL, 1, '2025-03-26 09:03:26', '2025-03-26 09:03:26');

-- ----------------------------
-- Table structure for forum_topics
-- ----------------------------
DROP TABLE IF EXISTS `forum_topics`;
CREATE TABLE `forum_topics`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '版块名称',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '版块描述',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '版块图标',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '版块封面',
  `user_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '版主用户ID(预留)',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序值',
  `post_count` int NULL DEFAULT 0 COMMENT '帖子数量',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态:1启用,0禁用',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sort`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of forum_topics
-- ----------------------------
INSERT INTO `forum_topics` VALUES (1, '茶叶知识', '分享茶叶相关知识、冲泡技巧等', '/icons/knowledge.png', '/covers/tea_knowledge.jpg', 'cy100001', 1, 0, 1, '2025-03-26 09:03:26', '2025-03-26 09:03:26');
INSERT INTO `forum_topics` VALUES (2, '茶友交流', '茶友们的交流互动平台', '/icons/chat.png', '/covers/tea_chat.jpg', 'cy100001', 2, 0, 1, '2025-03-26 09:03:26', '2025-03-26 09:03:26');

-- ----------------------------
-- Table structure for home_contents
-- ----------------------------
DROP TABLE IF EXISTS `home_contents`;
CREATE TABLE `home_contents`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `section` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区域标识(banner,recommend,feature等)',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区域标题',
  `sub_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区域副标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '内容JSON格式,包含图片/链接/ID等',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态:1显示,0隐藏',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_section`(`section` ASC) USING BTREE,
  INDEX `idx_sort`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of home_contents
-- ----------------------------
INSERT INTO `home_contents` VALUES (1, 'banner', '商南茶文化', '千年茶乡，一片叶子的传奇', '[{\"url\":\"/banners/b1.jpg\",\"link\":\"/articles/1\",\"title\":\"探寻茶源\"},{\"url\":\"/banners/b2.jpg\",\"link\":\"/teas/category/1\",\"title\":\"品味名茶\"}]', 1, 1, '2025-03-26 09:03:26', '2025-03-26 09:03:26');
INSERT INTO `home_contents` VALUES (2, 'recommend', '精选好茶', '甄选商南优质茶品', '[{\"id\":\"tea1000001\",\"title\":\"商南翠峰\",\"image\":\"/images/tea1_main.png\",\"price\":128.00}]', 2, 1, '2025-03-26 09:03:26', '2025-03-26 09:03:26');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单编号(系统生成)',
  `user_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `shop_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '店铺ID',
  `tea_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '茶叶ID',
  `tea_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '茶叶名称',
  `spec_id` int NULL DEFAULT NULL COMMENT '规格ID',
  `spec_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '规格名称',
  `tea_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '茶叶图片',
  `quantity` int NOT NULL COMMENT '购买数量',
  `price` decimal(10, 2) NOT NULL COMMENT '单价',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `address_id` int NOT NULL COMMENT '收货地址ID',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态:0待付款,1待发货,2待收货,3已完成,4已取消,5已退款',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式',
  `logistics_company` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '物流公司',
  `logistics_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '物流单号',
  `shipping_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
  `completion_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '取消原因',
  `buyer_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '买家留言',
  `buyer_rate` tinyint(1) NULL DEFAULT 0 COMMENT '买家是否已评价',
  `refund_status` tinyint NULL DEFAULT 0 COMMENT '退款状态:0无退款申请,1申请中,2已同意,3已拒绝',
  `refund_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户退款原因',
  `refund_reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商家/管理员拒绝原因',
  `refund_apply_time` datetime NULL DEFAULT NULL COMMENT '退款申请时间',
  `refund_process_time` datetime NULL DEFAULT NULL COMMENT '退款处理时间',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_shop_id`(`shop_id` ASC) USING BTREE,
  INDEX `idx_tea_id`(`tea_id` ASC) USING BTREE,
  INDEX `idx_address_id`(`address_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES ('OD20250329001', 'cy100002', 'shop100001', 'tea1000001', '商南翠峰', 1, '罐装100g', '/images/tea1_main.png', 2, 128.00, 256.00, 1, 3, '2025-03-23 09:03:26', '支付宝', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, NULL, NULL, '2025-03-21 09:03:26', '2025-03-26 09:03:26');

-- ----------------------------
-- Table structure for shop_announcements
-- ----------------------------
DROP TABLE IF EXISTS `shop_announcements`;
CREATE TABLE `shop_announcements`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `shop_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '店铺ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告内容',
  `is_top` tinyint(1) NULL DEFAULT 0 COMMENT '是否置顶',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态:1显示,0隐藏',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_id`(`shop_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '店铺公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_announcements
-- ----------------------------

-- ----------------------------
-- Table structure for shop_banners
-- ----------------------------
DROP TABLE IF EXISTS `shop_banners`;
CREATE TABLE `shop_banners`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `shop_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '店铺ID',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Banner图片URL',
  `link_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '点击跳转链接',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Banner标题',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态:1显示,0隐藏',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_id`(`shop_id` ASC) USING BTREE,
  INDEX `idx_sort`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '店铺Banner表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_banners
-- ----------------------------

-- ----------------------------
-- Table structure for shop_certifications
-- ----------------------------
DROP TABLE IF EXISTS `shop_certifications`;
CREATE TABLE `shop_certifications`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请用户ID',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '身份证号',
  `id_card_front` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '身份证正面照',
  `id_card_back` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '身份证背面照',
  `business_license` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '营业执照',
  `shop_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请店铺名称',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区县',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址',
  `apply_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '申请理由',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态:0待审核,1已通过,2已拒绝',
  `admin_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核管理员ID',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `notification_id` int NULL DEFAULT NULL COMMENT '关联的系统通知ID',
  `notification_confirmed` tinyint(1) NULL DEFAULT 0 COMMENT '用户是否已确认通知',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of shop_certifications
-- ----------------------------
INSERT INTO `shop_certifications` VALUES (1, 'cy100002', '李四', '610123199001011234', '/uploads/id_front.jpg', '/uploads/id_back.jpg', '/uploads/license.jpg', '李四茶叶店', '13900000004', '陕西省', '商洛市', '商南县', '城关街道茶叶市场15号', '本人从事茶叶经营多年，希望能在平台上开设店铺', 0, NULL, NULL, NULL, 0, '2025-03-26 09:03:26', '2025-03-26 09:03:26');

-- ----------------------------
-- Table structure for shopping_cart
-- ----------------------------
DROP TABLE IF EXISTS `shopping_cart`;
CREATE TABLE `shopping_cart`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `tea_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '茶叶ID',
  `spec_id` int NULL DEFAULT NULL COMMENT '规格ID，为空表示默认规格',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `selected` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否选中',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_tea_spec`(`user_id` ASC, `tea_id` ASC, `spec_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of shopping_cart
-- ----------------------------
INSERT INTO `shopping_cart` VALUES (1, 'cy100002', 'tea1000001', 2, 1, 1, '2025-03-26 09:03:26', '2025-03-26 09:03:26');

-- ----------------------------
-- Table structure for shops
-- ----------------------------
DROP TABLE IF EXISTS `shops`;
CREATE TABLE `shops`  (
  `id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'shop前缀+6位数字',
  `owner_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '店主用户ID',
  `shop_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '店铺名称',
  `logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '店铺logo',
  `banner` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '店铺banner',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '店铺简介',
  `announcement` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '店铺公告',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区县',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '详细地址',
  `business_license` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '营业执照图片',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态:1正常,0关闭',
  `rating` decimal(2, 1) NULL DEFAULT 5.0 COMMENT '平均评分',
  `rating_count` int NULL DEFAULT 0 COMMENT '评分数量',
  `sales_count` int NULL DEFAULT 0 COMMENT '总销量',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_owner_id`(`owner_id` ASC) USING BTREE,
  INDEX `idx_shop_name`(`shop_name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of shops
-- ----------------------------
INSERT INTO `shops` VALUES ('shop100001', 'cy100003', '商南茶叶专营店', '/logo/shop1.png', '/banner/shop1.png', '提供优质商南特产茶叶', NULL, '13800000003', '陕西省', '商洛市', '商南县', '城关街道茶叶市场12号', NULL, 1, 4.8, 24, 168, '2025-03-26 09:03:26', '2025-03-26 09:03:26');

-- ----------------------------
-- Table structure for tea_articles
-- ----------------------------
DROP TABLE IF EXISTS `tea_articles`;
CREATE TABLE `tea_articles`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章标题',
  `subtitle` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '副标题',
  `author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '作者名称',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章内容',
  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文章摘要',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图片',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '图片JSON数组',
  `video_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频链接',
  `category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文章分类',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签,英文逗号分隔',
  `source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文章来源',
  `view_count` int NULL DEFAULT 0 COMMENT '阅读量',
  `is_top` tinyint(1) NULL DEFAULT 0 COMMENT '是否置顶',
  `is_recommend` tinyint(1) NULL DEFAULT 0 COMMENT '是否推荐',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态:1已发布,0草稿,2已删除',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_publish_time`(`publish_time` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tea_articles
-- ----------------------------
INSERT INTO `tea_articles` VALUES (1, '商南茶的历史与文化', '探寻千年茶乡的古韵今香', '茶史专家', '商南茶的历史可以追溯到唐代，当时已有文人墨客记载了商南地区所产茶叶的独特品质...(此处省略详细内容)', '本文详细介绍了商南茶的历史沿革、文化底蕴和特色工艺', '/images/shangnantea_history.jpg', NULL, NULL, '茶文化', '商南茶,历史,文化', NULL, 126, 0, 0, 1, '2025-03-16 09:03:26', '2025-03-14 09:03:26', '2025-03-16 09:03:26');

-- ----------------------------
-- Table structure for tea_categories
-- ----------------------------
DROP TABLE IF EXISTS `tea_categories`;
CREATE TABLE `tea_categories`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `parent_id` int NULL DEFAULT 0 COMMENT '父分类ID',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类图标',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tea_categories
-- ----------------------------
INSERT INTO `tea_categories` VALUES (1, '绿茶', 0, 1, '/icons/green-tea.png', '2025-03-26 09:03:26', '2025-03-26 09:03:26');
INSERT INTO `tea_categories` VALUES (2, '红茶', 0, 2, '/icons/black-tea.png', '2025-03-26 09:03:26', '2025-03-26 09:03:26');
INSERT INTO `tea_categories` VALUES (3, '乌龙茶', 0, 3, '/icons/oolong-tea.png', '2025-03-26 09:03:26', '2025-03-26 09:03:26');

-- ----------------------------
-- Table structure for tea_images
-- ----------------------------
DROP TABLE IF EXISTS `tea_images`;
CREATE TABLE `tea_images`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `tea_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '茶叶ID',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片URL',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `is_main` tinyint(1) NULL DEFAULT 0 COMMENT '是否主图',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tea_id`(`tea_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tea_images
-- ----------------------------
INSERT INTO `tea_images` VALUES (1, 'tea1000001', '/images/tea1_main.png', 0, 1, '2025-03-26 09:03:26');
INSERT INTO `tea_images` VALUES (2, 'tea1000001', '/images/tea1_detail1.png', 1, 0, '2025-03-26 09:03:26');
INSERT INTO `tea_images` VALUES (3, 'tea1000001', '/images/tea1_detail2.png', 2, 0, '2025-03-26 09:03:26');

-- ----------------------------
-- Table structure for tea_reviews
-- ----------------------------
DROP TABLE IF EXISTS `tea_reviews`;
CREATE TABLE `tea_reviews`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `tea_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '茶叶ID',
  `user_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `order_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评价内容',
  `rating` tinyint NOT NULL COMMENT '评分(1-5)',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评价图片,JSON格式存储多个URL',
  `reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '商家回复',
  `reply_time` datetime NULL DEFAULT NULL COMMENT '回复时间',
  `is_anonymous` tinyint(1) NULL DEFAULT 0 COMMENT '是否匿名',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tea_id`(`tea_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tea_reviews
-- ----------------------------
INSERT INTO `tea_reviews` VALUES (1, 'tea1000001', 'cy100002', NULL, '茶叶品质很好，口感清香，回甘持久，非常满意！', 5, NULL, NULL, NULL, 0, '2025-03-26 09:03:26', '2025-03-26 09:03:26');

-- ----------------------------
-- Table structure for tea_specifications
-- ----------------------------
DROP TABLE IF EXISTS `tea_specifications`;
CREATE TABLE `tea_specifications`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `tea_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '茶叶ID',
  `spec_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规格名称(如:罐装50g,袋装900g)',
  `price` decimal(10, 2) NOT NULL COMMENT '规格价格',
  `stock` int NOT NULL DEFAULT 0 COMMENT '规格库存',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否默认规格',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tea_id`(`tea_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tea_specifications
-- ----------------------------
INSERT INTO `tea_specifications` VALUES (1, 'tea1000001', '罐装100g', 128.00, 50, 1, '2025-03-26 09:03:26', '2025-03-26 09:03:26');
INSERT INTO `tea_specifications` VALUES (2, 'tea1000001', '盒装200g', 238.00, 30, 0, '2025-03-26 09:03:26', '2025-03-26 09:03:26');
INSERT INTO `tea_specifications` VALUES (3, 'tea1000001', '礼盒装500g', 568.00, 20, 0, '2025-03-26 09:03:26', '2025-03-26 09:03:26');

-- ----------------------------
-- Table structure for teas
-- ----------------------------
DROP TABLE IF EXISTS `teas`;
CREATE TABLE `teas`  (
  `id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'tea前缀+8位数字',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '茶叶名称',
  `shop_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属商家ID',
  `category_id` int NULL DEFAULT NULL COMMENT '分类ID',
  `price` decimal(10, 2) NOT NULL COMMENT '基础价格',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '茶叶描述',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '详细介绍',
  `origin` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产地',
  `stock` int NOT NULL DEFAULT 0 COMMENT '总库存',
  `sales` int NOT NULL DEFAULT 0 COMMENT '销量',
  `main_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '主图',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态:0下架,1上架',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `is_deleted` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_id`(`shop_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of teas
-- ----------------------------
INSERT INTO `teas` VALUES ('tea1000001', '商南翠峰', 'shop100001', 1, 128.00, '商南特产绿茶，清香回甘', NULL, '陕西省商洛市商南县', 100, 30, '/images/tea1.png', 1, '2025-03-26 09:03:26', '2025-03-26 09:03:26', 0);

-- ----------------------------
-- Table structure for user_addresses
-- ----------------------------
DROP TABLE IF EXISTS `user_addresses`;
CREATE TABLE `user_addresses`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人电话',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区/县',
  `detail_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否默认地址',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_addresses
-- ----------------------------
INSERT INTO `user_addresses` VALUES (1, 'cy100002', '张三', '13900000002', '北京市', '北京市', '海淀区', '中关村科技园23号楼403室', 1, '2025-03-26 09:03:26', '2025-03-26 09:03:26');

-- ----------------------------
-- Table structure for user_favorites
-- ----------------------------
DROP TABLE IF EXISTS `user_favorites`;
CREATE TABLE `user_favorites`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `item_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收藏项类型(tea, post, tea_article)',
  `item_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收藏项ID',
  `target_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收藏目标名称(冗余字段)',
  `target_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收藏目标图片(冗余字段)',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_item`(`user_id` ASC, `item_type` ASC, `item_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_favorites
-- ----------------------------
INSERT INTO `user_favorites` VALUES (1, 'cy100002', 'tea', 'tea1000001', NULL, NULL, '2025-03-26 09:03:26');

-- ----------------------------
-- Table structure for user_follows
-- ----------------------------
DROP TABLE IF EXISTS `user_follows`;
CREATE TABLE `user_follows`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关注人用户ID',
  `follow_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关注类型(shop, user)',
  `follow_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '被关注对象ID',
  `target_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关注目标名称(冗余字段)',
  `target_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关注目标头像(冗余字段)',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_follow`(`user_id` ASC, `follow_type` ASC, `follow_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_follow`(`follow_type` ASC, `follow_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_follows
-- ----------------------------
INSERT INTO `user_follows` VALUES (1, 'cy100002', 'shop', 'shop100001', NULL, NULL, '2025-03-26 09:03:26');

-- ----------------------------
-- Table structure for user_likes
-- ----------------------------
DROP TABLE IF EXISTS `user_likes`;
CREATE TABLE `user_likes`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '点赞目标类型(post,reply,article)',
  `target_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '点赞目标ID',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_likes
-- ----------------------------
INSERT INTO `user_likes` VALUES (1, 'cy100002', 'post', '1', '2025-03-26 09:03:26');

-- ----------------------------
-- Table structure for user_notifications
-- ----------------------------
DROP TABLE IF EXISTS `user_notifications`;
CREATE TABLE `user_notifications`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收用户ID',
  `sender_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者用户ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知类型(system,reply,like,certification等)',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '通知标题',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '通知内容',
  `target_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标ID',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标类型(post,article,shop等)',
  `extra_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '额外数据(JSON格式)',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_notifications
-- ----------------------------
INSERT INTO `user_notifications` VALUES (1, 'cy100003', 'cy100002', 'message', '新消息提醒', '您有一条来自用户的新消息', NULL, NULL, NULL, 0, '2025-03-26 09:03:26', NULL);

-- ----------------------------
-- Table structure for user_settings
-- ----------------------------
DROP TABLE IF EXISTS `user_settings`;
CREATE TABLE `user_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `setting_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设置项键名',
  `setting_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '设置项值',
  `setting_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'string' COMMENT '值类型(string,number,boolean,json)',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_key`(`user_id` ASC, `setting_key` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_settings
-- ----------------------------

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'cy前缀+6位数字',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `role` tinyint NOT NULL COMMENT '1-管理员，2-普通用户，3-商家',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '用户状态:1正常,0禁用,2注销',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记: 0-正常, 1-已删除',
  `gender` tinyint(1) NULL DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `bio` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人简介',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES ('cy100001', 'admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', '管理员', '/avatar/admin.png', '13800000001', 'admin@shangnantea.com', 1, 1, '2025-03-26 09:02:49', '2025-03-26 09:02:49', 0, 0, NULL, NULL);
INSERT INTO `users` VALUES ('cy100002', 'user', '04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb', '普通用户', '/avatar/user.png', '13800000002', 'user@example.com', 2, 1, '2025-03-26 09:02:49', '2025-03-26 09:02:49', 0, 0, NULL, NULL);
INSERT INTO `users` VALUES ('cy100003', 'shop', 'd8022f2060ad6efd297ab73dcc5355c9b214054b0d1776a136a669d26a7d3b14', '商南茶叶店', '/avatar/shop.png', '13800000003', 'shop@example.com', 3, 1, '2025-03-26 09:02:49', '2025-03-26 09:02:49', 0, 0, NULL, NULL);
INSERT INTO `users` VALUES ('cy141130', 'admin199', 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', NULL, NULL, NULL, NULL, 2, 1, '2025-03-27 13:15:16', '2025-03-27 13:15:16', 0, NULL, NULL, NULL);
INSERT INTO `users` VALUES ('cy500255', 'admin1', 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', NULL, NULL, NULL, NULL, 2, 1, '2025-03-27 08:47:40', '2025-03-27 08:47:40', 0, NULL, NULL, NULL);
INSERT INTO `users` VALUES ('cy764575', 'admin12', 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', NULL, NULL, NULL, NULL, 2, 1, '2025-03-27 09:41:36', '2025-03-27 09:41:36', 0, NULL, NULL, NULL);
INSERT INTO `users` VALUES ('cy793904', 'admin166', 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', NULL, NULL, NULL, NULL, 2, 1, '2025-03-27 13:14:46', '2025-03-27 13:14:46', 0, NULL, NULL, NULL);

-- ----------------------------
-- Procedure structure for confirm_shop_certification
-- ----------------------------
DROP PROCEDURE IF EXISTS `confirm_shop_certification`;
delimiter ;;
CREATE PROCEDURE `confirm_shop_certification`(IN cert_id INT)
BEGIN
  DECLARE v_user_id VARCHAR(8);
  DECLARE v_shop_name VARCHAR(100);
  DECLARE v_shop_id VARCHAR(10);
  DECLARE v_business_license VARCHAR(255);
  DECLARE v_contact_phone VARCHAR(20);
  DECLARE v_province VARCHAR(50);
  DECLARE v_city VARCHAR(50);
  DECLARE v_district VARCHAR(50);
  DECLARE v_address VARCHAR(200);
  
  -- 获取认证信息
  SELECT 
    user_id, shop_name, business_license, contact_phone, 
    province, city, district, address
  INTO 
    v_user_id, v_shop_name, v_business_license, v_contact_phone,
    v_province, v_city, v_district, v_address
  FROM shop_certifications 
  WHERE id = cert_id AND status = 1 AND notification_confirmed = 0;
  
  -- 如果找到有效的认证申请
  IF v_user_id IS NOT NULL THEN
    -- 生成店铺ID
    SET v_shop_id = generate_shop_id();
    
    -- 更新用户角色为商家
    UPDATE users SET role = 3 WHERE id = v_user_id;
    
    -- 创建店铺
    INSERT INTO shops (
      id, owner_id, shop_name, business_license, contact_phone,
      province, city, district, address, status, create_time, update_time
    ) VALUES (
      v_shop_id, v_user_id, v_shop_name, v_business_license, v_contact_phone,
      v_province, v_city, v_district, v_address, 1, NOW(), NOW()
    );
    
    -- 标记认证通知已确认
    UPDATE shop_certifications SET 
      notification_confirmed = 1
    WHERE id = cert_id;
    
    -- 返回创建的店铺ID
    SELECT v_shop_id AS shop_id;
  ELSE
    -- 未找到有效认证或已确认过
    SIGNAL SQLSTATE '45000' 
    SET MESSAGE_TEXT = '无效的认证请求或已经确认过';
  END IF;
END
;;
delimiter ;

-- ----------------------------
-- Function structure for generate_session_id
-- ----------------------------
DROP FUNCTION IF EXISTS `generate_session_id`;
delimiter ;;
CREATE FUNCTION `generate_session_id`(user1_id VARCHAR(8), user2_id VARCHAR(8), session_type VARCHAR(20))
 RETURNS varchar(32) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
  DETERMINISTIC
BEGIN
    DECLARE smaller_id VARCHAR(8);
    DECLARE larger_id VARCHAR(8);
    
    -- 确保两个ID的顺序一致
    IF user1_id < user2_id THEN
        SET smaller_id = user1_id;
        SET larger_id = user2_id;
    ELSE
        SET smaller_id = user2_id;
        SET larger_id = user1_id;
    END IF;
    
    -- 返回拼接后的会话ID
    RETURN CONCAT(smaller_id, '_', larger_id, '_', session_type);
END
;;
delimiter ;

-- ----------------------------
-- Function structure for generate_shop_id
-- ----------------------------
DROP FUNCTION IF EXISTS `generate_shop_id`;
delimiter ;;
CREATE FUNCTION `generate_shop_id`()
 RETURNS varchar(10) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
  DETERMINISTIC
BEGIN
  DECLARE new_id varchar(10);
  DECLARE id_exists int;
  
  REPEAT
    -- 生成"shop"前缀+6位随机数字
    SET new_id = CONCAT('shop', LPAD(FLOOR(RAND() * 1000000), 6, '0'));
    
    -- 检查ID是否已存在
    SELECT COUNT(*) INTO id_exists FROM shops WHERE id = new_id;
  UNTIL id_exists = 0 END REPEAT;
  
  RETURN new_id;
END
;;
delimiter ;

-- ----------------------------
-- Function structure for generate_tea_id
-- ----------------------------
DROP FUNCTION IF EXISTS `generate_tea_id`;
delimiter ;;
CREATE FUNCTION `generate_tea_id`()
 RETURNS varchar(10) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
  DETERMINISTIC
BEGIN
    DECLARE new_id VARCHAR(10);
    DECLARE id_exists INT;
    
    -- 生成循环直到找到一个不存在的ID
    REPEAT
        -- 生成"tea"前缀 + 随机7位数字
        SET new_id = CONCAT('tea', 
                           LPAD(FLOOR(RAND() * 10000000), 7, '0'));
        
        -- 检查这个ID是否已存在
        SELECT COUNT(*) INTO id_exists FROM teas WHERE id = new_id;
        
    UNTIL id_exists = 0 END REPEAT;
    
    RETURN new_id;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for insert_tea
-- ----------------------------
DROP PROCEDURE IF EXISTS `insert_tea`;
delimiter ;;
CREATE PROCEDURE `insert_tea`(IN p_name VARCHAR(100),
    IN p_shop_id VARCHAR(10),
    IN p_category_id INT,
    IN p_price DECIMAL(10,2),
    IN p_description TEXT,
    IN p_origin VARCHAR(50),
    IN p_stock INT,
    IN p_main_image VARCHAR(255),
    OUT p_tea_id VARCHAR(10))
BEGIN
    DECLARE new_tea_id VARCHAR(10);
    
    -- 生成唯一ID
    SET new_tea_id = generate_tea_id();
    
    -- 插入茶叶记录
    INSERT INTO teas (
        id, name, shop_id, category_id, price, 
        description, origin, stock, main_image,
        create_time, update_time
    ) VALUES (
        new_tea_id, p_name, p_shop_id, p_category_id, p_price,
        p_description, p_origin, p_stock, p_main_image,
        NOW(), NOW()
    );
    
    -- 返回生成的ID
    SET p_tea_id = new_tea_id;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for insert_user
-- ----------------------------
DROP PROCEDURE IF EXISTS `insert_user`;
delimiter ;;
CREATE PROCEDURE `insert_user`(IN p_username VARCHAR(50),
    IN p_password VARCHAR(100),
    IN p_nickname VARCHAR(50),
    IN p_role TINYINT,
    OUT p_user_id VARCHAR(8))
BEGIN
    DECLARE new_user_id VARCHAR(8);
    
    -- 生成唯一ID
    SET new_user_id = generate_user_id();
    
    -- 插入用户记录
    INSERT INTO users (
        id, username, password, nickname, role, 
        create_time, update_time
    ) VALUES (
        new_user_id, p_username, p_password, p_nickname, p_role, 
        NOW(), NOW()
    );
    
    -- 返回生成的ID
    SET p_user_id = new_user_id;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
