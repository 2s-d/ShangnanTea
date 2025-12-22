# Requirements Document

## Introduction

重构现有的消息提示系统，将 7 个分散的消息文件（commonMessages、userMessages、teaMessages、orderMessages、shopMessages、forumMessages、messageMessages）合并为 2 个文件：
1. `promptMessages.js` - 纯前端提示消息（表单验证、确认框等）
2. `apiMessages.js` - 基于状态码映射的请求消息

## Glossary

- **Prompt_Message**: 纯前端提示消息，用于表单验证、确认框等，不涉及后端交互
- **API_Message**: 与后端请求相关的消息，基于状态码（code）映射显示
- **Code_Map**: 状态码到消息文本的映射表
- **Message_Manager**: 底层消息显示管理器（messageManager.js），负责实际的消息展示

## Requirements

### Requirement 1: 提示消息文件整合

**User Story:** As a 开发者, I want 将所有 PROMPT 类型消息整合到一个文件, so that 前端验证消息集中管理，便于维护。

#### Acceptance Criteria

1. THE System SHALL 创建 `src/utils/promptMessages.js` 文件，包含所有模块的 PROMPT 消息常量
2. THE System SHALL 按模块分组组织 PROMPT 消息常量（COMMON、USER、TEA、ORDER、SHOP、FORUM、MESSAGE）
3. THE System SHALL 导出所有原有的 `xxxPromptMessages` 对象，保持函数名不变
4. WHEN 前端代码导入提示消息函数时, THE System SHALL 只需修改导入路径，无需修改调用代码

### Requirement 2: 状态码映射文档

**User Story:** As a 开发者, I want 创建完整的状态码映射文档, so that 前后端有统一的状态码规范参考。

#### Acceptance Criteria

1. THE System SHALL 创建 `docs/code-message-mapping.md` 文档
2. THE System SHALL 定义状态码分段规则：
   - HTTP 语义码：200、400、401、403、404、500（保留）
   - 通用业务码：1xxx
   - 用户模块码：2xxx
   - 茶叶模块码：3xxx
   - 订单模块码：4xxx
   - 店铺模块码：5xxx
   - 论坛模块码：6xxx
   - 消息模块码：7xxx
3. THE System SHALL 确保每个状态码唯一，不重复
4. THE System SHALL 为每个状态码定义对应的中文消息文本

### Requirement 3: API 消息文件创建

**User Story:** As a 开发者, I want 创建基于状态码映射的 API 消息文件, so that 请求消息可以根据后端返回的 code 自动显示。

#### Acceptance Criteria

1. THE System SHALL 创建 `src/utils/apiMessages.js` 文件
2. THE System SHALL 包含 CODE_MAP 对象，存储所有状态码到消息的映射
3. THE System SHALL 导出 `getMessageByCode(code)` 函数，根据 code 获取消息文本
4. THE System SHALL 导出 `showByCode(code, customMsg?)` 函数，根据 code 自动判断成功/错误并显示
5. THE System SHALL 导出 `isSuccess(code)` 函数，判断是否为成功状态码
