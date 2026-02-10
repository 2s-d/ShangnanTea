# Implementation Plan: Vue CLI to Vite Migration

## Overview

本实施计划将 shangnantea-web 项目从 Vue CLI 构建系统迁移到 Vite。迁移采用渐进式方法，确保每一步都可验证，所有现有功能保持不变。整个迁移过程预计 30-60 分钟完成。

## Tasks

- [x] 1. 准备和依赖安装
  - 备份当前 package.json 和 vue.config.js
  - 安装 Vite 相关依赖：vite, @vitejs/plugin-vue, @vitejs/plugin-legacy, terser
  - 安装 sass 依赖（Vite 需要）
  - _Requirements: 1.2_

- [-] 2. 创建 Vite 配置文件
  - [-] 2.1 创建 vite.config.js
    - 配置 @vitejs/plugin-vue 插件
    - 配置路径别名 @ 指向 src 目录
    - 配置开发服务器端口 8082
    - 配置 API 代理规则（/api 和 /files）
    - 配置 SCSS 预处理器
    - 配置生产构建选项（输出目录、代码分割等）
    - _Requirements: 1.1, 3.1, 5.1, 5.2, 6.1, 6.2, 7.1_

- [ ] 3. 迁移环境变量
  - [ ] 3.1 更新 .env.local 文件
    - 将 VUE_APP_API_BASE_URL 改为 VITE_API_BASE_URL
    - _Requirements: 2.1_

  - [ ] 3.2 更新 .env.production 文件
    - 将 VUE_APP_API_BASE_URL 改为 VITE_API_BASE_URL
    - _Requirements: 2.1_

  - [ ] 3.3 更新代码中的环境变量引用
    - 在所有源文件中将 process.env.VUE_APP_ 替换为 import.meta.env.VITE_
    - 重点检查 src/api/ 目录下的文件
    - 检查 src/utils/ 目录下的文件
    - 检查 vue.config.js 中的环境变量使用
    - _Requirements: 2.2_

- [ ] 4. 处理 HTML 入口文件
  - [ ] 4.1 移动和修改 index.html
    - 将 public/index.html 复制到项目根目录 shangnantea-web/index.html
    - 将 <%= BASE_URL %> 替换为 /
    - 添加主入口脚本标签：`<script type="module" src="/src/main.js"></script>`
    - _Requirements: 4.1, 4.2, 4.3_

- [ ] 5. 更新 package.json 脚本
  - [ ] 5.1 更新构建脚本
    - 将 "serve" 改为 "dev": "vite"
    - 将 "build" 改为 "build": "vite build"
    - 添加 "preview": "vite preview"
    - 保留 "lint" 脚本但移除 vue-cli-service 依赖
    - _Requirements: 1.3_

- [ ] 6. Checkpoint - 测试开发环境
  - 运行 `npm run dev` 启动开发服务器
  - 验证服务器在 3 秒内启动
  - 访问 http://localhost:8082 检查首页加载
  - 测试路由导航是否正常
  - 测试 API 调用是否正常（检查代理配置）
  - 测试 HMR 是否工作（修改一个组件文件）
  - 检查浏览器控制台是否有错误
  - _Requirements: 6.1, 6.2, 6.3, 8.1, 8.4_

- [ ] 7. 测试生产构建
  - [ ] 7.1 执行生产构建
    - 运行 `npm run build`
    - 验证构建成功完成且无错误
    - 检查 dist 目录结构
    - 验证静态资源有哈希命名
    - _Requirements: 7.1, 7.3, 8.2_

  - [ ] 7.2 预览生产构建
    - 运行 `npm run preview`
    - 访问预览服务器测试所有功能
    - 验证所有路由正常工作
    - 验证 API 调用正常工作
    - 验证静态资源加载正常
    - _Requirements: 8.3, 8.4_

- [ ] 8. Checkpoint - 功能验证
  - 测试用户认证流程（登录、注册）
  - 测试茶叶浏览和详情页
  - 测试商城功能
  - 测试订单流程
  - 测试论坛功能
  - 测试消息功能
  - 验证 Pinia 状态管理正常工作
  - 验证 SCSS 样式正确渲染
  - _Requirements: 8.4_

- [ ] 9. 清理 Vue CLI 相关文件
  - [ ] 9.1 删除配置文件
    - 删除 vue.config.js
    - 删除 babel.config.js
    - 删除 public/index.html（已移到根目录）
    - _Requirements: 10.1, 10.3_

  - [ ] 9.2 移除 Vue CLI 依赖
    - 从 package.json 移除 @vue/cli-service
    - 从 package.json 移除 @vue/cli-plugin-babel
    - 从 package.json 移除 @vue/cli-plugin-eslint
    - 从 package.json 移除 @vue/cli-plugin-router
    - 从 package.json 移除 babel-eslint
    - 运行 `npm install` 清理依赖
    - _Requirements: 10.2_

  - [ ] 9.3 更新 .gitignore
    - 确保 dist 目录在 .gitignore 中
    - 添加 Vite 相关的临时文件（如果需要）
    - _Requirements: 10.4_

- [ ] 10. 最终验证和文档
  - [ ] 10.1 性能对比
    - 记录开发服务器启动时间（应 < 3 秒）
    - 记录 HMR 更新时间（应 < 100ms）
    - 对比生产构建包大小
    - 记录构建时间

  - [ ] 10.2 完整回归测试
    - 在开发环境测试所有主要功能
    - 在生产构建中测试所有主要功能
    - 在不同浏览器中测试（Chrome, Firefox, Safari）
    - 检查控制台无错误和警告
    - _Requirements: 8.1, 8.2, 8.3, 8.4_

  - [ ] 10.3 提交代码
    - 提交所有更改到 Git
    - 使用清晰的提交信息："迁移构建系统从 Vue CLI 到 Vite"

## Notes

- 迁移过程中保持 Vue CLI 配置文件，直到验证 Vite 完全工作后再删除
- 如果遇到问题，可以随时回退到 Vue CLI（保留了备份）
- 环境变量的修改是关键步骤，需要仔细检查所有引用
- 代理配置需要特别注意，确保 API 调用正常工作
- 建议在非工作时间进行迁移，以便有充足时间测试
