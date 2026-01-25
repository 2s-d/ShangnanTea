-- ============================================================================
-- 数据库冗余字段清理脚本
-- 功能：删除关注、点赞、收藏相关的冗余计数字段
-- 说明：这些计数可以从 user_likes、user_favorites、user_follows 表实时统计
-- 创建日期：2026-01-25
-- ============================================================================

USE teasystem;

-- ============================================================================
-- 第一步：删除触发器（如果存在）
-- ============================================================================

DROP TRIGGER IF EXISTS `after_like_insert`;
DROP TRIGGER IF EXISTS `after_like_delete`;
DROP TRIGGER IF EXISTS `after_favorite_insert`;
DROP TRIGGER IF EXISTS `after_favorite_delete`;
DROP TRIGGER IF EXISTS `after_follow_insert`;
DROP TRIGGER IF EXISTS `after_follow_delete`;

-- ============================================================================
-- 第二步：删除冗余的计数字段
-- ============================================================================

-- 1. forum_posts 表 - 删除点赞数和收藏数
ALTER TABLE `forum_posts` 
    DROP COLUMN IF EXISTS `like_count`,
    DROP COLUMN IF EXISTS `favorite_count`;

-- 2. forum_replies 表 - 删除点赞数
ALTER TABLE `forum_replies` 
    DROP COLUMN IF EXISTS `like_count`;

-- 3. shops 表 - 删除关注数
ALTER TABLE `shops` 
    DROP COLUMN IF EXISTS `follow_count`;

-- 4. tea_articles 表 - 删除点赞数和收藏数
ALTER TABLE `tea_articles` 
    DROP COLUMN IF EXISTS `like_count`,
    DROP COLUMN IF EXISTS `favorite_count`;

-- 5. tea_reviews 表 - 删除点赞数
ALTER TABLE `tea_reviews` 
    DROP COLUMN IF EXISTS `like_count`;

-- ============================================================================
-- 第三步：创建查询视图（可选 - 方便统计）
-- ============================================================================

-- 创建帖子统计视图
CREATE OR REPLACE VIEW `v_forum_posts_stats` AS
SELECT 
    p.id,
    p.title,
    p.user_id,
    p.topic_id,
    p.view_count,
    p.reply_count,
    COUNT(DISTINCT l.id) as like_count,
    COUNT(DISTINCT f.id) as favorite_count,
    p.create_time,
    p.update_time
FROM forum_posts p
LEFT JOIN user_likes l ON l.target_type = 'post' AND l.target_id = CAST(p.id AS CHAR)
LEFT JOIN user_favorites f ON f.item_type = 'post' AND f.item_id = CAST(p.id AS CHAR)
GROUP BY p.id;

-- 创建回复统计视图
CREATE OR REPLACE VIEW `v_forum_replies_stats` AS
SELECT 
    r.id,
    r.post_id,
    r.user_id,
    r.content,
    COUNT(DISTINCT l.id) as like_count,
    r.create_time,
    r.update_time
FROM forum_replies r
LEFT JOIN user_likes l ON l.target_type = 'reply' AND l.target_id = CAST(r.id AS CHAR)
GROUP BY r.id;

-- 创建店铺统计视图
CREATE OR REPLACE VIEW `v_shops_stats` AS
SELECT 
    s.id,
    s.owner_id,
    s.shop_name,
    s.rating,
    s.rating_count,
    s.sales_count,
    COUNT(DISTINCT f.id) as follow_count,
    s.create_time,
    s.update_time
FROM shops s
LEFT JOIN user_follows f ON f.follow_type = 'shop' AND f.follow_id = s.id
GROUP BY s.id;

-- 创建文章统计视图
CREATE OR REPLACE VIEW `v_tea_articles_stats` AS
SELECT 
    a.id,
    a.title,
    a.author,
    a.view_count,
    COUNT(DISTINCT l.id) as like_count,
    COUNT(DISTINCT f.id) as favorite_count,
    a.create_time,
    a.update_time
FROM tea_articles a
LEFT JOIN user_likes l ON l.target_type = 'article' AND l.target_id = CAST(a.id AS CHAR)
LEFT JOIN user_favorites f ON f.item_type = 'tea_article' AND f.item_id = CAST(a.id AS CHAR)
GROUP BY a.id;

-- 创建评价统计视图
CREATE OR REPLACE VIEW `v_tea_reviews_stats` AS
SELECT 
    r.id,
    r.tea_id,
    r.user_id,
    r.content,
    r.rating,
    COUNT(DISTINCT l.id) as like_count,
    r.create_time,
    r.update_time
FROM tea_reviews r
LEFT JOIN user_likes l ON l.target_type = 'review' AND l.target_id = CAST(r.id AS CHAR)
GROUP BY r.id;

-- ============================================================================
-- 第四步：创建索引优化查询性能
-- ============================================================================

-- 为 user_likes 表创建复合索引
CREATE INDEX IF NOT EXISTS `idx_target_type_id` ON `user_likes` (`target_type`, `target_id`);

-- 为 user_favorites 表创建复合索引
CREATE INDEX IF NOT EXISTS `idx_item_type_id` ON `user_favorites` (`item_type`, `item_id`);

-- 为 user_follows 表创建复合索引
CREATE INDEX IF NOT EXISTS `idx_follow_type_id` ON `user_follows` (`follow_type`, `follow_id`);

-- ============================================================================
-- 完成提示
-- ============================================================================

SELECT '✅ 冗余字段清理完成！' as status,
       '已删除 8 个冗余计数字段' as details,
       '已创建 5 个统计视图' as views,
       '已优化索引' as indexes;

-- ============================================================================
-- 使用说明
-- ============================================================================

/*
清理后的查询方式：

1. 查询帖子及统计数据：
   SELECT * FROM v_forum_posts_stats WHERE id = 1;

2. 查询店铺及关注数：
   SELECT * FROM v_shops_stats WHERE id = 'shop100001';

3. 查询文章及点赞收藏数：
   SELECT * FROM v_tea_articles_stats WHERE id = 1;

4. 直接统计某个帖子的点赞数：
   SELECT COUNT(*) as like_count 
   FROM user_likes 
   WHERE target_type = 'post' AND target_id = '1';

5. 直接统计某个店铺的关注数：
   SELECT COUNT(*) as follow_count 
   FROM user_follows 
   WHERE follow_type = 'shop' AND follow_id = 'shop100001';
*/
