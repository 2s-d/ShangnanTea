# Controller层重构提示词

## 任务目标
按照 `UserController.java` 和 `TeaController.java` 的模式，重构指定模块的Controller层文件，使其符合项目最佳实践。

## 参考标准文件
1. **标准模板1**：`shangnantea/shangnantea-server/src/main/java/com/shangnantea/controller/UserController.java`
2. **标准模板2**：`shangnantea/shangnantea-server/src/main/java/com/shangnantea/controller/TeaController.java`
3. **前端API文件**：`shangnantea/shangnantea-web/src/api/{模块名}.js`（如 `shop.js`、`order.js` 等）
4. **状态码映射文档**：`shangnantea/shangnantea-web/docs/code-message-mapping.md`
5. **API常量文件**：`shangnantea/shangnantea-web/src/api/apiConstants.js`

## 核心原则

### 1. Controller层职责
- **只做传递**：Controller层直接返回Service层的结果，不进行任何业务逻辑处理
- **不包装Result**：所有方法直接 `return service.method(...)`，不再调用 `Result.success()`
- **Service层返回Result**：Service层方法必须返回 `Result<T>` 类型

### 2. 代码结构
```java
@RestController
@RequestMapping({"/模块名", "/api/模块名"})
@Validated
public class XxxController {
    private static final Logger logger = LoggerFactory.getLogger(XxxController.class);
    
    @Autowired
    private XxxService xxxService;
    
    /**
     * 方法说明
     * 路径: HTTP方法 /路径
     * 成功码: xxx, 失败码: xxx
     *
     * @param param 参数说明
     * @return 返回值说明
     */
    @GetMapping("/path")
    @RequiresLogin  // 如果需要登录
    public Result<Object> methodName(@RequestParam Map<String, Object> params) {
        logger.info("操作说明请求, params: {}", params);
        return xxxService.methodName(params);  // 直接返回，不包装
    }
}
```

### 3. 权限注解使用
- `@RequiresLogin`：需要登录的接口
- `@RequiresRoles({1})`：仅管理员可访问的接口
- 公开接口：不添加任何权限注解

### 4. 参数类型规范
- **查询参数**：使用 `@RequestParam Map<String, Object> params` 或具体参数
- **路径参数**：使用 `@PathVariable String id`
- **请求体**：使用 `@RequestBody Map<String, Object> data`
- **文件上传**：使用 `@RequestParam("file") MultipartFile file` 或 `MultipartFile[] files`
- **DTO验证**：对于登录、注册等关键接口，使用 `@RequestBody @Valid XxxDTO`

### 5. 路径顺序规则
- **具体路径在前**：如 `/{id}/reviews`、`/{id}/specifications` 等
- **通用路径在后**：如 `/{id}` 放在最后，避免路径冲突
- **固定路径优先**：如 `/list`、`/categories` 等固定路径放在最前面

## 重构步骤

### 第一步：读取参考文件
1. 读取标准模板 `UserController.java` 和 `TeaController.java`
2. 读取前端API文件 `{模块名}.js`
3. 读取状态码映射文档 `code-message-mapping.md`
4. 读取API常量文件 `apiConstants.js`

### 第二步：分析前端API
1. 统计前端API接口数量
2. 列出所有接口的：
   - HTTP方法（GET/POST/PUT/DELETE）
   - 路径（从 `apiConstants.js` 获取）
   - 参数结构
   - 是否需要登录（查看前端代码中的权限检查）
   - 是否需要管理员权限

### 第三步：查找状态码
1. 在 `code-message-mapping.md` 中查找对应模块的状态码
2. 为每个接口标注：
   - 成功码（如 3000、4000 等）
   - 失败码（如 3100、4100 等）

### 第四步：重构Controller
1. **修改类注解**：
   ```java
   @RestController
   @RequestMapping({"/模块名", "/api/模块名"})
   @Validated
   ```

2. **添加Logger**：
   ```java
   private static final Logger logger = LoggerFactory.getLogger(XxxController.class);
   ```

3. **按功能模块分组**：参考UserController和TeaController的分组方式

4. **重写每个方法**：
   - 添加详细的Javadoc注释（包含路径、成功码、失败码）
   - 添加权限注解（如果需要）
   - 修改方法签名，参数类型改为 `Map<String, Object>` 或具体类型
   - 修改返回类型为 `Result<T>`
   - 方法体改为：`return service.method(...);`（直接返回，不包装）

5. **调整路径顺序**：
   - 将具体路径（如 `/{id}/reviews`）放在前面
   - 将通用路径（如 `/{id}`）放在最后

### 第五步：检查清单
完成重构后，检查以下项目：

- [ ] 所有方法都直接返回Service结果，没有调用 `Result.success()`
- [ ] 所有路径与前端API完全一致（参考 `apiConstants.js`）
- [ ] 所有方法都有详细的Javadoc注释（包含路径、成功码、失败码）
- [ ] 权限注解正确（`@RequiresLogin`、`@RequiresRoles({1})`）
- [ ] 路径顺序已优化（具体路径在前，通用路径在后）
- [ ] 参数类型正确（`Map<String, Object>`、`MultipartFile` 等）
- [ ] 文件上传接口参数名与前端一致（通常是 `"file"` 或 `"files"`）
- [ ] 编译错误都是预期的（因为Service层方法还未实现）

## 具体示例

### 示例1：查询接口
```java
/**
 * 获取店铺列表
 * 路径: GET /shop/list
 * 成功码: 5000, 失败码: 5100
 *
 * @param params 查询参数（keyword, rating, salesCount, region, page, pageSize等）
 * @return 店铺列表
 */
@GetMapping("/list")
public Result<Object> getShops(@RequestParam Map<String, Object> params) {
    logger.info("获取店铺列表请求, params: {}", params);
    return shopService.getShops(params);
}
```

### 示例2：需要登录的接口
```java
/**
 * 创建店铺
 * 路径: POST /shop/list
 * 成功码: 5001, 失败码: 5101
 *
 * @param shopData 店铺数据
 * @return 创建结果
 */
@PostMapping("/list")
@RequiresLogin
public Result<Object> createShop(@RequestBody Map<String, Object> shopData) {
    logger.info("创建店铺请求");
    return shopService.createShop(shopData);
}
```

### 示例3：管理员接口
```java
/**
 * 删除店铺（管理员）
 * 路径: DELETE /shop/{id}
 * 成功码: 5020, 失败码: 5120
 *
 * @param id 店铺ID
 * @return 删除结果
 */
@DeleteMapping("/{id}")
@RequiresRoles({1}) // 管理员角色
public Result<Boolean> deleteShop(@PathVariable String id) {
    logger.info("删除店铺请求: {}", id);
    return shopService.deleteShop(id);
}
```

### 示例4：文件上传接口
```java
/**
 * 上传店铺Logo
 * 路径: POST /shop/{shopId}/logo
 * 成功码: 1001, 失败码: 1101, 1103, 1104
 *
 * @param shopId 店铺ID
 * @param file Logo文件
 * @return 上传结果
 */
@PostMapping("/{shopId}/logo")
@RequiresLogin
public Result<Map<String, Object>> uploadLogo(@PathVariable String shopId, 
                                               @RequestParam("file") MultipartFile file) {
    logger.info("上传店铺Logo请求: {}, 文件名: {}", shopId, file.getOriginalFilename());
    return shopService.uploadLogo(shopId, file);
}
```

## 注意事项

1. **不要保留旧的业务逻辑**：删除所有 `Result.success()` 调用和业务判断
2. **不要修改Service层**：只修改Controller层，Service层方法签名会在后续实现
3. **路径必须精确匹配**：参考 `apiConstants.js` 中的路径定义
4. **状态码必须准确**：参考 `code-message-mapping.md`，如果找不到对应的状态码，使用通用状态码（200/1102等）
5. **参数名必须一致**：文件上传的参数名要与前端FormData中的字段名一致

## 需要重构的模块

按优先级顺序：
1. **ShopController** - 店铺模块
2. **OrderController** - 订单模块
3. **ForumController** - 论坛模块
4. **MessageController** - 消息模块

## 完成标准

重构完成后，Controller文件应该：
- ✅ 所有方法直接返回Service结果
- ✅ 路径与前端API完全一致
- ✅ 状态码注释完整
- ✅ 权限注解正确
- ✅ 路径顺序已优化
- ✅ 编译错误都是预期的（Service层未实现）

## 开始重构

请按照以上步骤，重构 **olderController.java** 文件。

文件路径：`shangnantea/shangnantea-server/src/main/java/com/shangnantea/controller/olderController.java`

参考文件：
- 前端API：`shangnantea/shangnantea-web/src/api/shop.js`
- API常量：`shangnantea/shangnantea-web/src/api/apiConstants.js`（older部分）
- 状态码映射：`shangnantea/shangnantea-web/docs/code-message-mapping.md`（订单模块 ）

完成后，请提供：
1. 重构后的完整代码
2. 接口统计（共多少个接口）
3. 检查清单完成情况

