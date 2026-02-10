# Requirements Document

## Introduction

将 shangnantea-web 项目从 Vue CLI 构建工具迁移到 Vite，以获得更快的开发服务器启动速度、热模块替换（HMR）性能和构建速度。该迁移需要保持所有现有功能正常工作，包括路由、状态管理（Pinia）、API 调用、样式处理等。

## Glossary

- **Vue_CLI**: Vue.js 官方的基于 Webpack 的构建工具
- **Vite**: 下一代前端构建工具，使用原生 ES 模块
- **Build_System**: 构建系统，负责将源代码转换为生产环境代码
- **Dev_Server**: 开发服务器，用于本地开发时提供热重载功能
- **HMR**: Hot Module Replacement，热模块替换
- **Environment_Variables**: 环境变量，用于配置不同环境的参数

## Requirements

### Requirement 1: 构建配置迁移

**User Story:** 作为开发者，我想将项目从 Vue CLI 迁移到 Vite，以便获得更快的开发体验。

#### Acceptance Criteria

1. WHEN 项目配置文件被更新 THEN THE Build_System SHALL 使用 Vite 配置文件（vite.config.js）替代 Vue CLI 配置文件（vue.config.js）
2. WHEN 依赖包被更新 THEN THE Build_System SHALL 移除 Vue CLI 相关依赖并添加 Vite 相关依赖
3. WHEN 构建脚本被执行 THEN THE Build_System SHALL 使用 Vite 命令替代 Vue CLI 命令
4. WHEN 项目启动 THEN THE Dev_Server SHALL 在 3 秒内完成启动（相比 Vue CLI 的 10-30 秒）

### Requirement 2: 环境变量处理

**User Story:** 作为开发者，我想正确处理环境变量，以便在不同环境下使用正确的配置。

#### Acceptance Criteria

1. WHEN 环境变量文件存在 THEN THE Build_System SHALL 将 VUE_APP_ 前缀的变量转换为 VITE_ 前缀
2. WHEN 代码中访问环境变量 THEN THE Build_System SHALL 使用 import.meta.env 替代 process.env
3. WHEN 环境变量被读取 THEN THE Build_System SHALL 正确加载 .env.local 和 .env.production 文件
4. WHEN 构建时 THEN THE Build_System SHALL 正确注入所有必需的环境变量

### Requirement 3: 模块导入路径处理

**User Story:** 作为开发者，我想确保所有模块导入路径正确工作，以便项目能够正常运行。

#### Acceptance Criteria

1. WHEN 使用路径别名 THEN THE Build_System SHALL 正确解析 @ 别名指向 src 目录
2. WHEN 导入静态资源 THEN THE Build_System SHALL 正确处理图片、字体等静态文件的导入
3. WHEN 使用绝对路径导入 THEN THE Build_System SHALL 正确解析所有模块路径
4. WHEN 导入 node_modules 中的包 THEN THE Build_System SHALL 正确处理第三方依赖

### Requirement 4: HTML 入口文件处理

**User Story:** 作为开发者，我想正确处理 HTML 入口文件，以便应用能够正常加载。

#### Acceptance Criteria

1. WHEN HTML 文件被处理 THEN THE Build_System SHALL 将 public/index.html 移动到项目根目录的 index.html
2. WHEN HTML 中引用资源 THEN THE Build_System SHALL 使用 <%= BASE_URL %> 语法替换为 / 或相对路径
3. WHEN HTML 中加载脚本 THEN THE Build_System SHALL 添加 type="module" 到主入口脚本标签
4. WHEN 构建完成 THEN THE Build_System SHALL 正确注入所有必需的脚本和样式

### Requirement 5: 插件和加载器迁移

**User Story:** 作为开发者，我想迁移所有必需的插件和加载器，以便保持现有功能。

#### Acceptance Criteria

1. WHEN 处理 Vue 文件 THEN THE Build_System SHALL 使用 @vitejs/plugin-vue 插件
2. WHEN 处理 SCSS 文件 THEN THE Build_System SHALL 正确配置 SCSS 预处理器
3. WHEN 需要兼容性处理 THEN THE Build_System SHALL 使用 @vitejs/plugin-legacy 处理旧浏览器兼容
4. WHERE 使用了特殊的 Webpack 加载器 THEN THE Build_System SHALL 找到对应的 Vite 插件或配置替代方案

### Requirement 6: 开发服务器配置

**User Story:** 作为开发者，我想配置开发服务器，以便本地开发时能够正常工作。

#### Acceptance Criteria

1. WHEN 开发服务器启动 THEN THE Dev_Server SHALL 监听正确的端口（默认 5173 或配置的端口）
2. WHEN 需要代理 API 请求 THEN THE Dev_Server SHALL 正确配置代理规则转发到后端服务器
3. WHEN 文件变化 THEN THE Dev_Server SHALL 在 100ms 内触发 HMR 更新
4. WHEN 访问开发服务器 THEN THE Dev_Server SHALL 正确处理 CORS 和其他安全配置

### Requirement 7: 生产构建配置

**User Story:** 作为开发者，我想配置生产构建，以便生成优化的生产代码。

#### Acceptance Criteria

1. WHEN 执行生产构建 THEN THE Build_System SHALL 生成优化的、压缩的代码到 dist 目录
2. WHEN 构建完成 THEN THE Build_System SHALL 生成正确的 source map 文件（如果配置）
3. WHEN 处理静态资源 THEN THE Build_System SHALL 正确处理资源的哈希命名和路径
4. WHEN 代码分割 THEN THE Build_System SHALL 合理分割代码块以优化加载性能

### Requirement 8: 测试和验证

**User Story:** 作为开发者，我想验证迁移后的项目，以便确保所有功能正常工作。

#### Acceptance Criteria

1. WHEN 开发服务器启动 THEN THE Dev_Server SHALL 能够正常访问所有页面
2. WHEN 执行生产构建 THEN THE Build_System SHALL 成功完成构建且无错误
3. WHEN 运行生产构建的代码 THEN THE Build_System SHALL 所有功能与迁移前保持一致
4. WHEN 测试关键功能 THEN THE Build_System SHALL 路由、状态管理、API 调用、样式渲染等核心功能正常工作

### Requirement 9: 向后兼容性处理

**User Story:** 作为开发者，我想确保项目在旧浏览器中也能正常工作，以便支持更广泛的用户群体。

#### Acceptance Criteria

1. WHERE 需要支持旧浏览器 THEN THE Build_System SHALL 使用 @vitejs/plugin-legacy 插件生成兼容代码
2. WHEN 使用现代 JavaScript 特性 THEN THE Build_System SHALL 正确转译为 ES5 代码（如果配置）
3. WHEN 加载 polyfills THEN THE Build_System SHALL 按需加载必要的 polyfills
4. WHEN 在旧浏览器中运行 THEN THE Build_System SHALL 应用能够正常启动和运行

### Requirement 10: 清理和优化

**User Story:** 作为开发者，我想清理不再需要的文件和配置，以便保持项目整洁。

#### Acceptance Criteria

1. WHEN 迁移完成 THEN THE Build_System SHALL 删除 vue.config.js 和其他 Vue CLI 配置文件
2. WHEN 依赖更新完成 THEN THE Build_System SHALL 从 package.json 中移除所有 Vue CLI 相关依赖
3. WHEN 清理完成 THEN THE Build_System SHALL 删除不再使用的 Webpack 相关配置和文件
4. WHEN 项目结构调整完成 THEN THE Build_System SHALL 更新 .gitignore 文件以适应 Vite 的输出目录
