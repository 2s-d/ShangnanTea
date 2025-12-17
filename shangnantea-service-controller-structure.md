# 商南茶文化平台服务层架构设计

## 服务层次结构设计理念

为了更好地适应复杂业务场景，采用三层服务架构：

1. **基础服务层**：与实体一一对应，处理单表CRUD操作
2. **业务服务层**：处理跨表业务，实现复杂业务逻辑
3. **门面服务层**：对外提供综合服务，聚合多个业务流程

这种设计使代码结构更清晰，也便于后续扩展和维护。

## 服务类与控制器规划

### 1. 用户模块

#### 基础服务
- `UserService`：用户基本信息管理
- `UserAddressService`：用户地址管理
- `UserFavoriteService`：用户收藏管理
- `UserFollowService`：用户关注管理
- `UserLikeService`：用户点赞管理

#### 业务服务
- `UserAuthService`：用户认证、权限、登录注册
- `UserProfileService`：用户信息聚合服务
- `MerchantApplicationService`：商家申请与认证服务

#### 控制器
- `UserController`：用户管理，包含登录、注册、认证相关功能，用户信息管理，地址管理等
- `UserProfileController`：用户资料管理
- `UserAddressController`：地址管理
- `UserCollectionController`：收藏、关注、点赞管理
- `MerchantApplicationController`：商家入驻申请

### 2. 茶叶模块

#### 基础服务
- `TeaService`：茶叶商品基本管理
- `TeaCategoryService`：茶叶分类管理
- `TeaSpecificationService`：茶叶规格管理
- `TeaImageService`：茶叶图片管理
- `TeaReviewService`：茶叶评价管理

#### 业务服务
- `TeaMarketService`：茶叶市场聚合服务
- `TeaSearchService`：茶叶搜索与推荐
- `TeaAnalyticsService`：茶叶销售分析

#### 控制器
- `TeaController`：茶叶基本操作
- `TeaCategoryController`：分类管理
- `TeaReviewController`：评价管理
- `TeaMarketController`：茶叶展示与搜索
- `TeaAdminController`：管理员茶叶管理

### 3. 商店模块

#### 基础服务
- `ShopService`：店铺基本管理
- `ShopCertificationService`：店铺认证管理

#### 业务服务
- `ShopMarketingService`：店铺营销活动管理
- `ShopStatisticsService`：店铺数据统计分析
- `ShopVerificationService`：店铺审核相关

#### 控制器
- `ShopController`：商家店铺管理
- `ShopCertificationController`：认证管理
- `ShopMarketingController`：营销活动
- `ShopCustomerController`：客户管理
- `ShopAdminController`：管理员店铺管理

### 4. 订单模块

#### 基础服务
- `OrderService`：订单基本管理
- `ShoppingCartService`：购物车管理

#### 业务服务
- `OrderProcessingService`：订单流程处理
- `PaymentService`：支付相关处理
- `OrderStatisticsService`：订单数据统计
- `LogisticsService`：物流相关处理

#### 控制器
- `CartController`：购物车操作
- `OrderController`：订单创建与管理
- `PaymentController`：支付相关
- `OrderAdminController`：管理员订单管理
- `MerchantOrderController`：商家订单管理

### 5. 论坛模块

#### 基础服务
- `ForumTopicService`：论坛主题管理
- `ForumPostService`：论坛帖子管理
- `ForumReplyService`：论坛回复管理
- `TeaArticleService`：茶叶文章管理
- `HomeContentService`：首页内容管理

#### 业务服务
- `ForumManagementService`：论坛综合管理
- `ContentRecommendationService`：内容推荐服务
- `UserInteractionService`：用户互动服务

#### 控制器
- `ForumController`：论坛主题与帖子
- `ForumReplyController`：回复管理
- `ArticleController`：茶叶文章
- `HomeContentController`：首页内容
- `ForumAdminController`：管理员论坛管理

### 6. 消息模块

#### 基础服务
- `ChatMessageService`：聊天消息管理
- `ChatSessionService`：聊天会话管理
- `UserNotificationService`：用户通知管理

#### 业务服务
- `MessageCenterService`：消息中心聚合服务
- `RealTimeChatService`：实时聊天服务
- `NotificationDispatchService`：通知分发服务

#### 控制器
- `ChatController`：聊天相关
- `NotificationController`：通知相关
- `MessageCenterController`：消息中心

### 7. 系统通用

#### 基础服务
- `FileService`：文件上传与管理
- `LogService`：系统日志
- `CacheService`：缓存服务

#### 业务服务  
- `SystemConfigService`：系统配置管理
- `StatisticsService`：系统统计分析

#### 控制器
- `FileController`：文件上传
- `CommonController`：通用接口
- `SystemController`：系统管理

## 跨模块业务处理

### 门面服务示例

1. **首页门面服务**
   ```java
   @Service
   public class HomeFacadeService {
       private final TeaMarketService teaMarketService;
       private final ShopService shopService;
       private final HomeContentService homeContentService;
       private final ForumPostService forumPostService;
       
       // 聚合首页所需的所有数据
       public HomePageData getHomePageData() {
           // 1. 获取首页轮播图
           // 2. 获取推荐茶叶
           // 3. 获取热门店铺
           // 4. 获取精选文章
           // 5. 返回聚合数据
       }
   }
   ```

2. **茶叶详情门面服务**
   ```java
   @Service
   public class TeaDetailFacadeService {
       private final TeaService teaService;
       private final TeaSpecificationService specService;
       private final TeaImageService imageService;
       private final TeaReviewService reviewService;
       private final ShopService shopService;
       
       // 获取完整茶叶详情数据
       public TeaDetailData getTeaDetail(Long teaId) {
           // 1. 获取茶叶基本信息
           // 2. 获取茶叶规格选项
           // 3. 获取茶叶图片
           // 4. 获取茶叶评价
           // 5. 获取店铺信息
           // 6. 返回聚合数据
       }
   }
   ```

### 业务流程服务示例

1. **下单流程服务**
   ```java
   @Service
   public class OrderProcessingService {
       private final ShoppingCartService cartService;
       private final OrderService orderService;
       private final UserAddressService addressService;
       private final PaymentService paymentService;
       private final TeaService teaService;
       
       // 处理完整下单流程
       @Transactional
       public OrderResult createOrder(OrderCreateRequest request) {
           // 1. 验证购物车商品
           // 2. 检查库存
           // 3. 创建订单
           // 4. 扣减库存
           // 5. 清空购物车
           // 6. 创建支付记录
           // 7. 返回订单结果
       }
   }
   ```

2. **商家入驻流程服务**
   ```java
   @Service
   public class MerchantApplicationService {
       private final UserService userService;
       private final ShopCertificationService certificationService;
       private final ShopService shopService;
       private final UserNotificationService notificationService;
       
       // 处理商家入驻申请
       public ApplicationResult applyForMerchant(MerchantApplication application) {
           // 1. 验证用户资格
           // 2. 保存认证信息
           // 3. 更新用户状态
           // 4. 发送通知
           // 5. 返回申请结果
       }
   }
   ```

## 结论

通过这种分层服务架构，可以实现：

1. **模块内高内聚**：基础服务处理单一职责
2. **模块间低耦合**：通过依赖注入和接口调用
3. **业务逻辑清晰**：复杂流程被封装在专门的业务服务中
4. **便于维护扩展**：可以方便地添加新功能而不影响现有代码

这种设计既能处理基于数据表的简单CRUD操作，也能处理跨表的复杂业务逻辑，同时保持代码组织的清晰和可维护性。 
