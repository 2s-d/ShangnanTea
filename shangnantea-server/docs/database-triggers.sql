-- ============================================
-- 茶叶价格和库存自动同步触发器
-- ============================================
-- 说明：
-- 1. teas表的price和stock字段作为冗余字段，自动从默认规格同步
-- 2. price = 默认规格的价格
-- 3. stock = 所有规格库存之和
-- ============================================

DELIMITER $$

-- ============================================
-- 触发器1：插入规格后，更新茶叶的价格和库存
-- ============================================
DROP TRIGGER IF EXISTS `trg_tea_spec_after_insert`$$
CREATE TRIGGER `trg_tea_spec_after_insert`
AFTER INSERT ON `tea_specifications`
FOR EACH ROW
BEGIN
    DECLARE default_price DECIMAL(10,2);
    DECLARE total_stock INT;
    
    -- 获取默认规格的价格
    SELECT price INTO default_price
    FROM tea_specifications
    WHERE tea_id = NEW.tea_id AND is_default = 1
    LIMIT 1;
    
    -- 如果没有默认规格，使用新插入的规格价格
    IF default_price IS NULL THEN
        SET default_price = NEW.price;
    END IF;
    
    -- 计算总库存（所有规格库存之和）
    SELECT IFNULL(SUM(stock), 0) INTO total_stock
    FROM tea_specifications
    WHERE tea_id = NEW.tea_id;
    
    -- 更新茶叶表
    UPDATE teas
    SET price = default_price,
        stock = total_stock,
        update_time = NOW()
    WHERE id = NEW.tea_id;
END$$

-- ============================================
-- 触发器2：更新规格后，更新茶叶的价格和库存
-- ============================================
DROP TRIGGER IF EXISTS `trg_tea_spec_after_update`$$
CREATE TRIGGER `trg_tea_spec_after_update`
AFTER UPDATE ON `tea_specifications`
FOR EACH ROW
BEGIN
    DECLARE default_price DECIMAL(10,2);
    DECLARE total_stock INT;
    
    -- 获取默认规格的价格
    SELECT price INTO default_price
    FROM tea_specifications
    WHERE tea_id = NEW.tea_id AND is_default = 1
    LIMIT 1;
    
    -- 如果没有默认规格，使用第一个规格的价格
    IF default_price IS NULL THEN
        SELECT price INTO default_price
        FROM tea_specifications
        WHERE tea_id = NEW.tea_id
        ORDER BY id ASC
        LIMIT 1;
    END IF;
    
    -- 计算总库存（所有规格库存之和）
    SELECT IFNULL(SUM(stock), 0) INTO total_stock
    FROM tea_specifications
    WHERE tea_id = NEW.tea_id;
    
    -- 更新茶叶表
    UPDATE teas
    SET price = default_price,
        stock = total_stock,
        update_time = NOW()
    WHERE id = NEW.tea_id;
END$$

-- ============================================
-- 触发器3：删除规格后，更新茶叶的价格和库存
-- ============================================
DROP TRIGGER IF EXISTS `trg_tea_spec_after_delete`$$
CREATE TRIGGER `trg_tea_spec_after_delete`
AFTER DELETE ON `tea_specifications`
FOR EACH ROW
BEGIN
    DECLARE default_price DECIMAL(10,2);
    DECLARE total_stock INT;
    DECLARE spec_count INT;
    
    -- 检查该茶叶是否还有规格
    SELECT COUNT(*) INTO spec_count
    FROM tea_specifications
    WHERE tea_id = OLD.tea_id;
    
    IF spec_count > 0 THEN
        -- 还有规格，获取默认规格的价格
        SELECT price INTO default_price
        FROM tea_specifications
        WHERE tea_id = OLD.tea_id AND is_default = 1
        LIMIT 1;
        
        -- 如果没有默认规格，使用第一个规格的价格
        IF default_price IS NULL THEN
            SELECT price INTO default_price
            FROM tea_specifications
            WHERE tea_id = OLD.tea_id
            ORDER BY id ASC
            LIMIT 1;
        END IF;
        
        -- 计算总库存
        SELECT IFNULL(SUM(stock), 0) INTO total_stock
        FROM tea_specifications
        WHERE tea_id = OLD.tea_id;
        
        -- 更新茶叶表
        UPDATE teas
        SET price = default_price,
            stock = total_stock,
            update_time = NOW()
        WHERE id = OLD.tea_id;
    ELSE
        -- 没有规格了，设置为0
        UPDATE teas
        SET price = 0,
            stock = 0,
            update_time = NOW()
        WHERE id = OLD.tea_id;
    END IF;
END$$

DELIMITER ;

-- ============================================
-- 初始化脚本：同步现有数据
-- ============================================
-- 说明：为所有现有茶叶更新价格和库存
-- ============================================

UPDATE teas t
LEFT JOIN (
    SELECT 
        tea_id,
        MAX(CASE WHEN is_default = 1 THEN price END) as default_price,
        MIN(price) as min_price,
        SUM(stock) as total_stock
    FROM tea_specifications
    GROUP BY tea_id
) s ON t.id = s.tea_id
SET 
    t.price = IFNULL(s.default_price, s.min_price),
    t.stock = IFNULL(s.total_stock, 0),
    t.update_time = NOW()
WHERE s.tea_id IS NOT NULL;

-- ============================================
-- 验证脚本：检查数据一致性
-- ============================================
SELECT 
    t.id,
    t.name,
    t.price as tea_price,
    t.stock as tea_stock,
    IFNULL(s.default_price, s.min_price) as calculated_price,
    IFNULL(s.total_stock, 0) as calculated_stock,
    CASE 
        WHEN t.price = IFNULL(s.default_price, s.min_price) 
             AND t.stock = IFNULL(s.total_stock, 0) 
        THEN '一致' 
        ELSE '不一致' 
    END as status
FROM teas t
LEFT JOIN (
    SELECT 
        tea_id,
        MAX(CASE WHEN is_default = 1 THEN price END) as default_price,
        MIN(price) as min_price,
        SUM(stock) as total_stock
    FROM tea_specifications
    GROUP BY tea_id
) s ON t.id = s.tea_id
WHERE t.is_deleted = 0;
