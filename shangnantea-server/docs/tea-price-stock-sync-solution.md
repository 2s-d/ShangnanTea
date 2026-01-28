# 茶叶价格和库存自动同步方案

## 问题描述

数据库设计中存在数据冗余和逻辑冲突：

- **teas表**有 `price` 和 `stock` 字段
- **tea_specifications表**也有 `price` 和 `stock` 字段
- 两者独立维护，容易导致数据不一致

## 解决方案

### 设计原则

1. **规格表为主**：`tea_specifications` 表是真实数据源
2. **茶叶表为辅**：`teas` 表的 `price` 和 `stock` 作为冗余字段，方便查询
3. **自动同步**：通过数据库触发器自动保持一致性

### 同步规则

#### 价格同步规则
```
teas.price = 默认规格的价格（is_default = 1）
如果没有默认规格，则使用第一个规格的价格
```

#### 库存同步规则
```
teas.stock = SUM(所有规格的库存)
```

## 实现方式

### 1. 数据库触发器

创建3个触发器，在规格表发生变化时自动更新茶叶表：

- **trg_tea_spec_after_insert**：插入规格后触发
- **trg_tea_spec_after_update**：更新规格后触发
- **trg_tea_spec_after_delete**：删除规格后触发

### 2. 触发器逻辑

```sql
-- 每次规格表变化时：
1. 查询该茶叶的默认规格价格
2. 计算该茶叶所有规格的库存总和
3. 更新茶叶表的price和stock字段
4. 更新茶叶表的update_time字段
```

## 部署步骤

### 步骤1：备份数据库
```bash
mysqldump -u root -p teasystem > backup_before_trigger.sql
```

### 步骤2：执行触发器脚本
```bash
mysql -u root -p teasystem < shangnantea-server/docs/database-triggers.sql
```

### 步骤3：验证触发器
```sql
-- 查看已创建的触发器
SHOW TRIGGERS WHERE `Table` = 'tea_specifications';

-- 应该看到3个触发器：
-- trg_tea_spec_after_insert
-- trg_tea_spec_after_update
-- trg_tea_spec_after_delete
```

### 步骤4：验证数据一致性
脚本末尾包含验证查询，会显示每个茶叶的价格和库存是否一致。

## 测试场景

### 测试1：添加规格
```sql
-- 添加新规格
INSERT INTO tea_specifications (tea_id, spec_name, price, stock, is_default, create_time, update_time)
VALUES ('tea1000001', '测试规格', 100.00, 50, 0, NOW(), NOW());

-- 验证：teas表的stock应该增加50
SELECT id, name, price, stock FROM teas WHERE id = 'tea1000001';
```

### 测试2：更新默认规格价格
```sql
-- 更新默认规格的价格
UPDATE tea_specifications 
SET price = 150.00, update_time = NOW()
WHERE tea_id = 'tea1000001' AND is_default = 1;

-- 验证：teas表的price应该变为150.00
SELECT id, name, price, stock FROM teas WHERE id = 'tea1000001';
```

### 测试3：更新规格库存
```sql
-- 更新某个规格的库存
UPDATE tea_specifications 
SET stock = 100, update_time = NOW()
WHERE tea_id = 'tea1000001' AND spec_name = '罐装100g';

-- 验证：teas表的stock应该重新计算
SELECT id, name, price, stock FROM teas WHERE id = 'tea1000001';
```

### 测试4：切换默认规格
```sql
-- 取消当前默认规格
UPDATE tea_specifications 
SET is_default = 0, update_time = NOW()
WHERE tea_id = 'tea1000001' AND is_default = 1;

-- 设置新的默认规格
UPDATE tea_specifications 
SET is_default = 1, update_time = NOW()
WHERE tea_id = 'tea1000001' AND spec_name = '盒装200g';

-- 验证：teas表的price应该变为盒装200g的价格
SELECT id, name, price, stock FROM teas WHERE id = 'tea1000001';
```

### 测试5：删除规格
```sql
-- 删除一个规格
DELETE FROM tea_specifications 
WHERE tea_id = 'tea1000001' AND spec_name = '测试规格';

-- 验证：teas表的stock应该减少
SELECT id, name, price, stock FROM teas WHERE id = 'tea1000001';
```

## 优点

1. ✅ **无需修改应用代码**：现有代码继续工作
2. ✅ **数据自动一致**：触发器保证实时同步
3. ✅ **查询性能好**：茶叶列表查询不需要JOIN规格表
4. ✅ **业务逻辑清晰**：规格表是唯一数据源

## 注意事项

1. **触发器性能**：
   - 触发器在每次规格变化时执行
   - 对于高并发场景，需要监控性能
   - 如有性能问题，可考虑异步更新方案

2. **数据初始化**：
   - 脚本包含初始化SQL，会同步所有现有数据
   - 建议在低峰期执行

3. **应用代码建议**：
   - 添加茶叶时，建议同时创建至少一个默认规格
   - 更新茶叶价格/库存时，建议直接操作规格表
   - 茶叶表的price/stock字段可以标记为只读

## 未来优化方向

如果需要更高的性能，可以考虑：

1. **异步更新**：使用消息队列异步更新茶叶表
2. **缓存层**：在Redis中缓存价格和库存
3. **视图方案**：使用数据库视图代替冗余字段

## 回滚方案

如果需要回滚触发器：

```sql
-- 删除触发器
DROP TRIGGER IF EXISTS trg_tea_spec_after_insert;
DROP TRIGGER IF EXISTS trg_tea_spec_after_update;
DROP TRIGGER IF EXISTS trg_tea_spec_after_delete;

-- 恢复备份
mysql -u root -p teasystem < backup_before_trigger.sql
```
