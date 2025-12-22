-- =====================================================
-- 数据库字段完整性修复脚本 (修订版)
-- 生成日期: 2025-12-21
-- 说明: 根据前端API需求分析，补充缺失的表和字段
-- =====================================================
-- 
-- 【重要说明】
-- 数据库现有情况:
-- 1. forum_posts, forum_replies, tea_articles 表已有 like_count 字段 ✓
-- 2. shops 表已有 banner 字段（单个URL）和 announcement 字段（单个文本）
--    但前端需要多Banner和多公告管理功能，所以需要新建独立表
-- 3. tea_reviews 表没有 like_count 字段，需要添加
-- 4. shops 表没有 follow_count 字段，需要添加
-- 5. orders 表没有退款相关字段，需要添加
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 新增表: shop_banners (店铺多Banner管理)
-- 说明: shops表的banner字段只能存一个URL，
--       但前端需要多Banner轮播、排序、增删功能
-- =====================================================
DROP TABLE IF EXISTS `shop_banners`;
CREATE TABLE `shop_banners` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `shop_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '店铺ID',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Banner图片URL',
  `link_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '点击跳转链接',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Banner标题',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序值',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态:1显示,0隐藏',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_id`(`shop_id`) USING BTREE,
  INDEX `idx_sort`(`sort_order`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '店铺Banner表(多Banner管理)' ROW_FORMAT = Dynamic;

-- =====================================================
-- 2. 新增表: shop_announcements (店铺多公告管理)
-- 说明: shops表的announcement字段只能存一条公告，
--       但前端需要多公告、置顶、增删功能
-- =====================================================
DROP TABLE IF EXISTS `shop_announcements`;
CREATE TABLE `shop_announcements` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `shop_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '店铺ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告内容',
  `is_top` tinyint(1) DEFAULT 0 COMMENT '是否置顶',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态:1显示,0隐藏',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_id`(`shop_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '店铺公告表(多公告管理)' ROW_FORMAT = Dynamic;

-- =====================================================
-- 3. 修改表: orders (添加退款相关字段)
-- 说明: 
--   - 现有 status=5 已表示"已退款"最终状态
--   - 但项目设计了完整退款流程：申请→审核→通过/拒绝
--   - 前端有 processRefund(approve, reason) 接口
--   - 后端返回 reject_reason 字段
--   - 所以需要额外字段记录退款申请状态和原因
-- =====================================================
ALTER TABLE `orders`
  ADD COLUMN `refund_status` tinyint(4) DEFAULT 0 COMMENT '退款状态:0无退款申请,1申请中,2已同意,3已拒绝' AFTER `buyer_rate`,
  ADD COLUMN `refund_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户退款原因' AFTER `refund_status`,
  ADD COLUMN `refund_reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商家/管理员拒绝原因' AFTER `refund_reason`,
  ADD COLUMN `refund_apply_time` datetime DEFAULT NULL COMMENT '退款申请时间' AFTER `refund_reject_reason`,
  ADD COLUMN `refund_process_time` datetime DEFAULT NULL COMMENT '退款处理时间' AFTER `refund_apply_time`;

-- 为退款状态添加索引
ALTER TABLE `orders` ADD INDEX `idx_refund_status`(`refund_status`) USING BTREE;

-- =====================================================
-- 4. 修改表: tea_reviews (添加点赞数字段)
-- 说明: forum_posts等表有like_count，但tea_reviews没有
-- =====================================================
ALTER TABLE `tea_reviews`
  ADD COLUMN `like_count` int(11) DEFAULT 0 COMMENT '点赞数' AFTER `is_anonymous`;

-- =====================================================
-- 5. 修改表: shops (添加关注数字段)
-- 说明: 前端需要显示店铺粉丝数
-- =====================================================
ALTER TABLE `shops`
  ADD COLUMN `follow_count` int(11) DEFAULT 0 COMMENT '关注数/粉丝数' AFTER `sales_count`;

-- =====================================================
-- 6. 修改表: user_favorites (添加目标名称和图片字段)
-- 说明: 收藏列表展示时需要显示名称和图片，避免每次JOIN查询
-- =====================================================
ALTER TABLE `user_favorites`
  ADD COLUMN `target_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收藏目标名称' AFTER `item_id`,
  ADD COLUMN `target_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收藏目标图片' AFTER `target_name`;

-- =====================================================
-- 7. 修改表: user_follows (添加目标名称和头像字段)
-- 说明: 关注列表展示时需要显示名称和头像，避免每次JOIN查询
-- =====================================================
ALTER TABLE `user_follows`
  ADD COLUMN `target_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关注目标名称' AFTER `follow_id`,
  ADD COLUMN `target_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关注目标头像' AFTER `target_name`;

-- =====================================================
-- 8. 触发器: 关注店铺时更新店铺关注数
-- =====================================================
DROP TRIGGER IF EXISTS `after_follow_insert`;
delimiter ;;
CREATE TRIGGER `after_follow_insert` AFTER INSERT ON `user_follows` FOR EACH ROW 
BEGIN
    IF NEW.follow_type = 'shop' THEN
        UPDATE shops SET follow_count = follow_count + 1 
        WHERE id = NEW.follow_id;
    END IF;
END
;;
delimiter ;

DROP TRIGGER IF EXISTS `after_follow_delete`;
delimiter ;;
CREATE TRIGGER `after_follow_delete` AFTER DELETE ON `user_follows` FOR EACH ROW 
BEGIN
    IF OLD.follow_type = 'shop' THEN
        UPDATE shops SET follow_count = follow_count - 1 
        WHERE id = OLD.follow_id AND follow_count > 0;
    END IF;
END
;;
delimiter ;

-- =====================================================
-- 9. 触发器: 点赞茶叶评价时更新评价点赞数
-- =====================================================
DROP TRIGGER IF EXISTS `after_review_like_insert`;
delimiter ;;
CREATE TRIGGER `after_review_like_insert` AFTER INSERT ON `user_likes` FOR EACH ROW 
BEGIN
    IF NEW.target_type = 'review' THEN
        UPDATE tea_reviews SET like_count = like_count + 1 
        WHERE id = NEW.target_id;
    END IF;
END
;;
delimiter ;

DROP TRIGGER IF EXISTS `after_review_like_delete`;
delimiter ;;
CREATE TRIGGER `after_review_like_delete` AFTER DELETE ON `user_likes` FOR EACH ROW 
BEGIN
    IF OLD.target_type = 'review' THEN
        UPDATE tea_reviews SET like_count = like_count - 1 
        WHERE id = OLD.target_id AND like_count > 0;
    END IF;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 执行说明:
-- 1. 备份数据库后再执行此脚本
-- 2. 如果某些字段已存在，对应的ALTER语句会报错，可以忽略
-- 3. 建议在测试环境先验证后再应用到生产环境
-- 
-- 变更汇总:
-- [新增表] shop_banners - 店铺多Banner管理
-- [新增表] shop_announcements - 店铺多公告管理
-- [新增字段] orders.refund_status/refund_reason/refund_reject_reason/refund_apply_time/refund_process_time
-- [新增字段] tea_reviews.like_count
-- [新增字段] shops.follow_count
-- [新增字段] user_favorites.target_name/target_image
-- [新增字段] user_follows.target_name/target_avatar
-- [新增触发器] after_follow_insert/after_follow_delete
-- [新增触发器] after_review_like_insert/after_review_like_delete
-- =====================================================
