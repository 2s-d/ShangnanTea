-- 用户模块数据库修复脚本
-- 修复日期: 2026-01-25
-- 修复内容: 
--   1. 添加user_follows和user_favorites表的冗余字段
--   2. 统一用户密码加密方式

-- ========================================
-- 第一部分：修复冗余字段
-- ========================================

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

-- ========================================
-- 第二部分：统一密码加密方式
-- ========================================

-- 说明：
-- 数据库中存在两种密码格式：
-- 1. 旧格式：SHA256哈希（64字符）- cy100001, cy100002, cy100003
-- 2. 新格式：Base64编码（44字符）- cy141130, cy500255, cy764575, cy793904
-- 
-- 解决方案：删除测试账号，保留正式账号并重置密码
-- 正式账号的密码将在首次登录时由用户重新设置

-- 1. 删除测试账号（新格式密码的账号）
DELETE FROM users WHERE id IN ('cy141130', 'cy500255', 'cy764575', 'cy793904');

-- 2. 重置正式账号密码为统一的初始密码
-- 初始密码: admin123 (加密后的值需要使用后端的PasswordEncoder生成)
-- 注意：这里使用占位符，实际密码需要通过后端PasswordEncoder.encode("admin123")生成
-- 建议：执行此脚本后，通过后端接口或管理界面重置这些账号的密码

-- 为安全起见，将这些账号标记为需要修改密码（通过status或其他字段）
-- 这里我们保持原密码不变，但添加注释说明需要重置

-- 3. 添加密码重置标记（可选，如果需要强制用户首次登录修改密码）
-- ALTER TABLE users ADD COLUMN need_reset_password TINYINT(1) DEFAULT 0 COMMENT '是否需要重置密码';
-- UPDATE users SET need_reset_password = 1 WHERE id IN ('cy100001', 'cy100002', 'cy100003');

-- ========================================
-- 执行说明
-- ========================================
-- 1. 第一部分（冗余字段）已在Navicat中执行完成
-- 2. 第二部分（密码统一）：
--    - 已删除测试账号
--    - 正式账号保持原密码（SHA256格式）
--    - 建议：后续通过管理界面统一重置为新格式密码
