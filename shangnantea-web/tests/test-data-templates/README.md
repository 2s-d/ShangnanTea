# 测试数据模板与经验

## 文档说明

本文档记录清除修改伪代码的经验和写后端测试数据的经验，方便后续测试数据的编写。

**创建日期**: 2024-12-17  
**状态**: 待更新

---

## 一、清除修改伪代码的经验

> **待更新**：清除UI代码中假数据和伪代码的经验将在此记录

---

## 一点五、提交与备份（清除伪代码前的安全动作）

### 目标

- **在大规模清除开始前先做一次可回滚备份**，避免“一次改太多”导致难以定位回归点。
- **分批提交**：按“基础设施开关（路由/导航/入口）→ 页面级清除 → 组件级清除”逐步推进。

### 实操方法（Windows + PowerShell）

- **先只暂存本次要提交的文件**（避免把无关改动一起带上）：

```powershell
cd shangnantea\shangnantea-web
git status
git add docs/tasks/00-testing-standard.md docs/tasks/00-tasks-overview.md `
  src/components/common/layout/NavBar.vue src/main.js src/router/index.js tests
git status
```

- **提交时使用 `--%` 关闭 PowerShell 对 `[]` 的解析**（commit message 需要 `[模块]: ...` 格式）：

```powershell
git --% commit -m "[基础设施]: 清除UI-DEV并建立测试目录结构"
```

### 备注

- 如果发现误暂存：`git restore --staged <file>`
- 如果需要回滚到提交点：`git reset --hard <commit_sha>`

---

## 二、写后端测试数据的经验

> **待更新**：在后端Controller层编写测试数据的经验将在此记录

---

## 三、常见问题与解决方案

> **待更新**：编写测试数据过程中遇到的问题和解决方案将在此记录

---

## 四、清除伪代码过程中的“计划外错误”如何处理（经验模板）

### 典型的计划外错误类型

- **lint/静态检查错误**：例如 `vue/no-unused-components`（导入/注册了组件但模板里没用）。
- **参数/字段不一致**：页面切回 Vuex 后发现 `store.state.xxx` 的字段名与页面预期不一致（如 `teaList` vs `teas`）。
- **异步/加载态冲突**：以前页面自己维护 `loading`，切回 store 后重复维护导致 UI 状态错乱。
- **接口分页参数命名不一致**：`pageSize` / `size` / `limit` 等命名不统一引起后端不识别。

### 处理原则（不扩大改动范围）

- **先让项目能跑**：优先修 lint 和运行时错误，保证“清除之后仍可运行”。
- **就地最小修复**：只改与当前页面/模块直接相关的最小范围，不顺手重构全局。
- **记录到经验**：把“错误现象 → 根因 → 修复方式 → 可脚本化规则”记录下来，后续批量清除会越来越快。

### 示例：`vue/no-unused-components`

- **现象**：终端报 `The "List" component has been registered but not used`
- **处理**：删除未使用的 import 与 components 注册（不要为了消除 warning 去写假模板引用）。

---

## 五、退款功能从“UI伪流程”回归生产版（经验）

### 背景

- UI 开发阶段常用 `setTimeout`、本地字段（如 `_temp_refund_applied`）模拟“申请退款→审核→已退款”流程。
- 回归生产后，**前端必须保留退款入口与交互**，但**不得**再在 UI 层本地篡改订单状态；请求不到/失败应交由后端错误返回。

### 改造要点（推荐做法）

- **UI层**：
  - 保留“申请退款”按钮与弹窗（输入退款原因、校验长度）
  - 点击提交后 `dispatch` 到 Vuex 的退款 action
  - 成功：提示“已提交等待审核”；失败：提示后端返回错误信息
- **Vuex层**：
  - 新增 `applyRefund({ orderId, reason })` action，调用 API（不做本地状态伪更新）
- **API层**：
  - 增加 `refundOrder()` 方法和 `API.ORDER.REFUND` 常量（POST）

### 经验结论

- **前端交互必须完整**：按钮/弹窗/校验/加载态/错误提示都要到位。
- **状态以服务端为准**：订单是否进入退款流程由后端返回决定，前端只展示与刷新。

---

## 六、从“UI-DEV页面”回归生产版的通用改造套路（可脚本化总结）

### 已覆盖的典型类型（本轮实践）

- **全局开关类**：路由守卫、入口初始化、导航栏（移除 dev bypass）
- **列表页类**：茶叶列表、店铺列表（移除本地 mock 列表 + setTimeout）
- **详情页类**：茶叶详情、店铺详情（移除本地 mock 详情 + mock 评论/收藏/加购）
- **操作弹窗类**：订单列表的退款弹窗（保留交互，改为走后端接口）

### 统一原则（写脚本/人工改造都要遵守）

- **组件不造数据**：页面不再 `ref([...mock])`；数据只来自 Vuex state/computed。
- **不在 UI 层模拟成功**：凡是业务动作（退款/回复/收藏/关注/加购/删除）都必须走 Vuex action 调后端；后端失败就提示错误。
- **loading 单一来源**：优先使用 `store.state.xxx.loading`；避免“页面 loading + store loading”双来源导致状态错乱。
- **保留入口、不要删功能**：缺接口时保留按钮/弹窗入口，改为提示“待后端接入”，不要本地 setTimeout 假成功。

### 适合“一键脚本”做的稳定替换点（高确定性）

- **脚本目标边界**：这里的一键脚本指“批量自动化处理高把握的机械替换”，不是“全项目一次性修复”。脚本必须满足：不引入新问题；不确定就不改，而是标注。
- **删除 UI-DEV 标记块**：成对的 `/* UI-DEV-START */ ... /* UI-DEV-END */` 可以整体移除。
- **删除 mock-images 引用**：`/mock-images/*`、`generateMock*()`、`setTimeout(() => {...})` 这类明显的 UI mock 片段。
- **删除未使用 import/components**：如 `vue/no-unused-components`、未使用 icon 导入（可用 eslint --fix 或脚本规则）。
- **替换 import 块**：把“UI-DEV import”替换为生产版 import（增加 `useStore`、`computed/watch` 等）。

### 不建议脚本自动改的点（需要人工判断）

- **字段映射与数据结构兼容**：例如 `images` 是 `string[]` 还是 `{url}[]`，需要人工加兼容层（如 `teaImages`）。
- **分页参数命名对齐**：`pageSize/size/limit` 的对齐要结合后端实现与现有 API。
- **跨模块 action 命名**：必须符合模块职责边界（组件→对应模块 action），否则容易引入隐性 bug。
- **脚本标注策略（建议）**：脚本遇到不确定点时输出 `TODO-SCRIPT:` 注释或生成清单文件（例如 `tests/test-data-templates/script-todos.md`），人工逐项处理并回写规则。

---

## 六点五、安全脚本的“事故复盘 + 规则升级记录”（必须持续更新）

> 目的：**提高文档记录频率**。每次脚本引入/暴露问题，都要记录：现象→根因→回滚→规则升级→下一次验证方式。

### 事故 01：误删 UI-DEV 块内的 import 导致 `no-undef`（已回滚）

- **现象**：
  - 执行 `safe-ui-dev-clean --write` 后，出现大量 eslint error，例如：`ref/computed/onMounted/useRouter/ElMessage/SafeImage is not defined`（`no-undef`）。
- **根因**：
  - 页面把 `import ...` / `components: { ... }` 注册也包在 `/* UI-DEV-START */.../* UI-DEV-END */` 中；
  - 脚本早期版本只把 `const/let/var/function/class/export` 视为“声明风险”，**漏掉了 `import`**；
  - 结果脚本把 import 块当成“安全可删”删掉，直接造成依赖未导入。
- **回滚策略（安全机制）**：
  - **只回滚本次脚本写入的 changedFiles**（不要全量回滚，避免误伤其它已完成的生产化改造）。
- **规则升级点（必须固化到脚本）**：
  - `UI-DEV` 块内出现 `import` 一律视为不安全：**不自动删除，只进 TODO 清单**。
- **下一次验证方式（避免再次偏离目标）**：
  - 使用 `--verify --rollback-on-error`：脚本写入后**只对 changedFiles 跑 eslint**；
  - 若出现 error，自动回滚本次 changedFiles，并把原因记录到运行日志。

### 计划外错误（lint）经验：`no-useless-catch`

- **现象**：eslint 报 `Unnecessary catch clause  no-useless-catch`，典型形式是：
  - `try { ... } catch (e) { throw e } finally { ... }`
- **原因**：这种 `catch` 没有任何处理逻辑，等同于不存在，只会增加噪音。
- **正确修法（安全且不改变行为）**：
  - 删除 `catch`，保留 `finally`（让 loading 之类收尾逻辑仍然执行）。
  - 如果确实需要“补充上下文/埋点/统一提示”，才保留 `catch`，并在其中做“额外工作”，再 `throw`。

### 计划外错误（eslint 解析）经验：重复声明导致 `Parsing error: Identifier ... has already been declared`

- **现象**：eslint 报 `Parsing error: Identifier 'xxx' has already been declared`，webpack 直接编译失败。
- **高频触发场景**：我们用“先插入生产逻辑 + return 阻断旧 mock 逻辑”的策略时，如果旧逻辑里也声明了同名变量（例如 `unreadSession`），即使代码不可达，**解析阶段仍会报重复声明**。
- **正确修法（不改变行为，最安全）**：
  - 把 return 之后的“参考代码”里的变量重命名（例如 `unreadSessionMock`），或直接删除该段参考代码。
  - 推荐保留参考代码时加注释：`NOTE: return 之后仅作参考，避免与生产逻辑变量同名`。

### 计划外错误（eslint）经验：`no-unreachable`（不可达代码导致编译失败）

- **现象**：eslint 报 `Unreachable code  no-unreachable`，webpack 直接编译失败。
- **高频触发场景**：为了“先切换到生产链路”，在函数里插入 `return` 来阻断旧 mock/伪逻辑，但旧逻辑还留在 `return` 后面。
- **正确修法（推荐顺序）**：
  - **直接删除 return 后旧逻辑**（最干净、最不容易再出新问题）。
  - 若必须保留参考实现：把旧逻辑移到**文档**里，而不是留在源码里。
  - 不建议：用 eslint disable 压掉（会掩盖真实问题，降低脚本改造的可信度）。

### 聊天/通知类页面的“伪成功”清理经验（强建议）

- **问题形态**：UI 里直接改本地数组/本地状态来模拟“删除会话/清空记录/举报成功/已读”等。
- **风险**：会制造“假状态”，导致后续测试与真实后端接入时难以排查（测试用例也会被误导）。
- **改造原则（生产版）**：
  - **保留入口**：按钮/弹窗流程保留，避免功能被“删掉”。
  - **不做本地状态变更**：没有后端接口时，确认框点击后只提示 `message.info('待后端接口接入')`，并 `return` 阻断后续本地更新。
  - **必须标注**：用 `TODO-SCRIPT` 标注“需要的后端接口契约”（URL、参数、返回结构），后续有了契约再落地 Vuex→API。

---

## 七、我认为“经验够不够？”以及还建议再补的典型

### 结论

目前的经验对“80% 的 UI-DEV 清除”已经够用（尤其是列表/详情/弹窗操作），**但如果目标是做一个稳定的一键修改脚本**，我建议再补 2 类典型页面，用来覆盖脚本最容易翻车的场景。

### 建议再补的 2 类典型（为了脚本更稳）

- **表单页（强交互/校验/联动）**：
  - 典型：地址新增/编辑、商家入驻申请、个人资料编辑等
  - 风险点：校验规则、级联选择器/上传组件、表单 model 字段映射
- **消息/通知页（状态多、列表多）**：
  - 典型：通知、关注、收藏、消息中心
  - 风险点：列表状态切换、已读/未读、分页与筛选、多处依赖 mock

把这两类也走一遍“生产化改造”，我们就能把一键脚本的规则定得更完整、更不容易误改。

---

**文档版本**: 0.1  
**创建日期**: 2024-12-17  
**最后更新**: 2024-12-17

