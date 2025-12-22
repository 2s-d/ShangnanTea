-- =====================================================
-- 商南茶数据库迁移脚本 (Navicat执行版)
-- 执行前请先备份数据库！
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 新增表: shop_banners (店铺多Banner管理)
CREATE TABLE IF NOT EXISTS `shop_banners` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `shop_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '店铺ID',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Banner图片URL',
  `link_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '点击跳转链接',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Banner标题',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序值',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态:1显示,0隐藏',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_shop_id`(`shop_id`),
  INDEX `idx_sort`(`sort_order`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '店铺Banner表';

-- 2. 新增表: shop_announcements (店铺多公告管理)
CREATE TABLE IF NOT EXISTS `shop_announcements` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `shop_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '店铺ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告内容',
  `is_top` tinyint(1) DEFAULT 0 COMMENT '是否置顶',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态:1显示,0隐藏',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_shop_id`(`shop_id`),
  INDEX `idx_create_time`(`create_time`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '店铺公告表';

-- 3. orders表添加退款字段
ALTER TABLE `orders`
  ADD COLUMN IF NOT EXISTS `refund_status` tinyint(4) DEFAULT 0 COMMENT '退款状态:0无退款申请,1申请中,2已同意,3已拒绝' AFTER `buyer_rate`,
  ADD COLUMN IF NOT EXISTS `refund_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户退款原因' AFTER `refund_status`,
  ADD COLUMN IF NOT EXISTS `refund_reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商家/管理员拒绝原因' AFTER `refund_reason`,
  ADD COLUMN IF NOT EXISTS `refund_apply_time` datetime DEFAULT NULL COMMENT '退款申请时间' AFTER `refund_reject_reason`,
  ADD COLUMN IF NOT EXISTS `refund_process_time` datetime DEFAULT NULL COMMENT '退款处理时间' AFTER `refund_apply_time`;

-- 4. tea_reviews表添加点赞数
ALTER TABLE `tea_reviews`
  ADD COLUMN IF NOT EXISTS `like_count` int(11) DEFAULT 0 COMMENT '点赞数' AFTER `is_anonymous`;

-- 5. shops表添加关注数
ALTER TABLE `shops`
  ADD COLUMN IF NOT EXISTS `follow_count` int(11) DEFAULT 0 COMMENT '关注数/粉丝数' AFTER `sales_count`;

-- 6. user_favorites表添加冗余字段(可选，优化列表展示)
ALTER TABLE `user_favorites`
  ADD COLUMN IF NOT EXISTS `target_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收藏目标名称' AFTER `item_id`,
  ADD COLUMN IF NOT EXISTS `target_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收藏目标图片' AFTER `target_name`;

-- 7. user_follows表添加冗余字段(可选，优化列表展示)
ALTER TABLE `user_follows`
  ADD COLUMN IF NOT EXISTS `target_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关注目标名称' AFTER `follow_id`,
  ADD COLUMN IF NOT EXISTS `target_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关注目标头像' AFTER `target_name`;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 执行完成！
-- 如果某些字段已存在会报警告，可以忽略
-- =====================================================
