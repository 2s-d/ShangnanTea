-- 用户模块数据库修复脚本
-- 修复日期: 2026-01-25
-- 修复内容: 添加user_follows和user_favorites表的冗余字段

-- 1. 修复user_follows表，添加冗余字段
ALTER TABLE `user_follows` 
ADD COLUMN `target_name` VARCHAR(100) NULL COMMENT '关注目标名称(冗余字段，优化列表展示)' AFTER `follow_id`,
ADD COLUMN `target_avatar` VARCHAR(255) NULL COMMENT '关注目标头像(冗余字段，优化列表展示)' AFTER `target_name`;

-- 2. 修复user_favorites表，添加冗余字段
ALTER TABLE `user_favorites` 
ADD COLUMN `target_name` VARCHAR(100) NULL COMMENT '收藏目标名称(冗余字段，优化列表展示)' AFTER `item_id`,
ADD COLUMN `target_image` VARCHAR(255) NULL COMMENT '收藏目标图片(冗余字段，优化列表展示)' AFTER `target_name`;

-- 3. 更新现有数据的冗余字段（可选，如果有历史数据）
-- 更新user_follows的shop关注
UPDATE user_follows uf
INNER JOIN shops s ON uf.follow_id = s.id
SET uf.target_name = s.shop_name, uf.target_avatar = s.logo
WHERE uf.follow_type = 'shop';

-- 更新user_follows的user关注
UPDATE user_follows uf
INNER JOIN users u ON uf.follow_id = u.id
SET uf.target_name = COALESCE(u.nickname, u.username), uf.target_avatar = u.avatar
WHERE uf.follow_type = 'user';

-- 更新user_favorites的tea收藏
UPDATE user_favorites ufav
INNER JOIN teas t ON ufav.item_id = t.id
SET ufav.target_name = t.name, ufav.target_image = t.main_image
WHERE ufav.item_type = 'tea';

-- 更新user_favorites的post收藏
UPDATE user_favorites ufav
INNER JOIN forum_posts fp ON ufav.item_id = fp.id
SET ufav.target_name = fp.title, ufav.target_image = fp.cover_image
WHERE ufav.item_type = 'post';

-- 更新user_favorites的tea_article收藏
UPDATE user_favorites ufav
INNER JOIN tea_articles ta ON ufav.item_id = ta.id
SET ufav.target_name = ta.title, ufav.target_image = ta.cover_image
WHERE ufav.item_type = 'tea_article';
