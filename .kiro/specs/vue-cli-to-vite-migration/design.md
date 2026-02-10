# Design Document: Vue CLI to Vite Migration

## Overview

本设计文档描述了将 shangnantea-web 项目从 Vue CLI 构建系统迁移到 Vite 的技术方案。Vite 是下一代前端构建工具，相比 Vue CLI（基于 Webpack），它提供了：

- **极速的开发服务器启动**：使用原生 ES 模块，无需打包即可启动
- **快速的热模块替换（HMR）**：精确的模块级别更新，响应时间在 100ms 以内
- **优化的生产构建**：使用 Rollup 进行构建，生成更小的包体积
- **开箱即用的 TypeScript 支持**：无需额外配置
- **更简洁的配置**：相比 Webpack 配置更加直观

迁移策略采用渐进式方法，确保在迁移过程中项目始终可运行，所有现有功能保持不变。

## Architecture

### 构建系统架构对比

**当前架构（Vue CLI + Webpack）：**
```
源代码 → Webpack 打包 → Bundle → Dev Server
                ↓
            各种 Loader
            各种 Plugin
                ↓
            优化处理
                ↓
            输出文件
```

**目标架构（Vite）：**
```
开发模式：
源代码 → ES Modules → 浏览器原生加载
            ↓
        按需编译
            ↓
        HMR 更新

生产模式：
源代码 → Rollup 打包 → 优化的 Bundle
            ↓
        Tree Shaking
        Code Splitting
            ↓
        输出文件
```

### 迁移架构

迁移过程分为以下阶段：

1. **准备阶段**：备份现有配置，安装 Vite 依赖
2. **配置迁移阶段**：创建 Vite 配置文件，迁移环境变量
3. **代码调整阶段**：调整 HTML 入口、环境变量引用
4. **测试验证阶段**：验证开发和生产环境
5. **清理阶段**：移除 Vue CLI 相关文件和依赖

## Components and Interfaces

### 核心组件

#### 1. Vite 配置文件（vite.config.js）

负责整个构建系统的配置，包括：
- 插件配置
- 路径别名
- 开发服务器配置
- 生产构建配置
- 代理配置

```javascript
// vite.config.js 结构
export default defineConfig({
  plugins: [],        // Vite 插件
  resolve: {},        // 模块解析配置
  server: {},         // 开发服务器配置
  build: {},          // 生产构建配置
  css: {}            // CSS 处理配置
})
```

#### 2. 环境变量处理器

负责处理环境变量的加载和访问：
- 将 `VUE_APP_*` 前缀转换为 `VITE_*`
- 将 `process.env.*` 访问转换为 `import.meta.env.*`
- 确保环境变量在构建时正确注入

#### 3. HTML 入口处理器

负责处理 HTML 入口文件：
- 移动 `public/index.html` 到根目录
- 添加模块脚本标签
- 处理静态资源引用

#### 4. 静态资源处理器

负责处理静态资源：
- 图片、字体等文件的导入
- Public 目录资源的访问
- 资源的哈希命名和缓存策略

### 接口定义

#### 配置接口

```javascript
// Vite 配置接口
interface ViteConfig {
  plugins: Plugin[]
  resolve: {
    alias: Record<string, string>
    extensions: string[]
  }
  server: {
    port: number
    proxy: Record<string, ProxyOptions>
    cors: boolean
  }
  build: {
    outDir: string
    assetsDir: string
    sourcemap: boolean
    minify: boolean
  }
}
```

#### 环境变量接口

```javascript
// 环境变量访问接口
interface ImportMetaEnv {
  VITE_API_BASE_URL: string
  // 其他环境变量...
}

interface ImportMeta {
  env: ImportMetaEnv
}
```

## Data Models

### 配置数据模型

#### Vite 配置模型

```javascript
{
  // 插件列表
  plugins: [
    vue(),                    // Vue 3 支持
    legacy({                  // 旧浏览器兼容
      targets: ['defaults', 'not IE 11']
    })
  ],
  
  // 路径解析
  resolve: {
    alias: {
      '@': '/src'            // @ 别名指向 src 目录
    },
    extensions: ['.js', '.vue', '.json']
  },
  
  // 开发服务器
  server: {
    port: 8082,              // 保持与 Vue CLI 相同的端口
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/files': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  
  // 生产构建
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'terser',
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus'],
          'vue-vendor': ['vue', 'vue-router', 'pinia']
        }
      }
    }
  },
  
  // CSS 配置
  css: {
    preprocessorOptions: {
      scss: {
        // SCSS 全局配置
      }
    }
  }
}
```

#### 环境变量模型

```javascript
// .env.local
{
  VITE_API_BASE_URL: 'https://m1.apifoxmock.com/m1/7709181-7451992-default'
}

// .env.production
{
  VITE_API_BASE_URL: 'http://96.30.204.197:8080/api'
}
```

#### Package.json 脚本模型

```json
{
  "scripts": {
    "dev": "vite",                          // 开发服务器
    "build": "vite build",                  // 生产构建
    "preview": "vite preview",              // 预览生产构建
    "lint": "eslint . --ext .vue,.js"       // 代码检查
  }
}
```

### 依赖包模型

#### 需要添加的依赖

```json
{
  "devDependencies": {
    "vite": "^5.0.0",
    "@vitejs/plugin-vue": "^5.0.0",
    "@vitejs/plugin-legacy": "^5.0.0",
    "terser": "^5.0.0"
  }
}
```

#### 需要移除的依赖

```json
{
  "devDependencies": {
    "@vue/cli-service": "~5.0.6",
    "@vue/cli-plugin-babel": "~5.0.6",
    "@vue/cli-plugin-eslint": "~5.0.6",
    "@vue/cli-plugin-router": "~5.0.6"
  }
}
```

### 文件结构变化模型

```
迁移前：
shangnantea-web/
├── public/
│   └── index.html          ← HTML 入口
├── src/
├── vue.config.js           ← Vue CLI 配置
├── babel.config.js         ← Babel 配置
└── package.json

迁移后：
shangnantea-web/
├── index.html              ← HTML 入口（移到根目录）
├── public/                 ← 静态资源目录
├── src/
├── vite.config.js          ← Vite 配置
└── package.json
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*


### Property 1: Configuration File Migration
*For any* Vue CLI project with a valid vue.config.js, after migration there should exist a vite.config.js with equivalent functionality and no vue.config.js file.
**Validates: Requirements 1.1, 10.1**

### Property 2: Environment Variable Prefix Consistency
*For any* environment variable file (.env.local, .env.production), all variables should use the VITE_ prefix instead of VUE_APP_ prefix.
**Validates: Requirements 2.1**

### Property 3: Environment Variable Access Pattern
*For any* source file that accesses environment variables, all accesses should use import.meta.env instead of process.env.
**Validates: Requirements 2.2**

### Property 4: Dependency Package Consistency
*For any* migrated project, package.json should contain Vite-related dependencies (vite, @vitejs/plugin-vue) and should not contain Vue CLI dependencies (@vue/cli-service, @vue/cli-plugin-*).
**Validates: Requirements 1.2, 10.2**

### Property 5: HTML Entry File Location
*For any* migrated project, index.html should exist in the project root directory and should not exist in the public directory.
**Validates: Requirements 4.1**

### Property 6: Module Script Type
*For any* migrated project's index.html, the main entry script tag should have type="module" attribute.
**Validates: Requirements 4.3**

### Property 7: Build Output Structure
*For any* successful production build, the dist directory should contain optimized, minified JavaScript and CSS files with hash-based filenames.
**Validates: Requirements 7.1, 7.3**

### Property 8: Path Alias Resolution
*For any* import statement using the @ alias, the module should resolve correctly to the src directory.
**Validates: Requirements 3.1**

### Property 9: Plugin Configuration Completeness
*For any* migrated project, vite.config.js should include @vitejs/plugin-vue for Vue 3 support and appropriate SCSS configuration.
**Validates: Requirements 5.1, 5.2**

### Property 10: Development Server Port Configuration
*For any* migrated project, the development server should listen on the configured port (8082) as specified in vite.config.js.
**Validates: Requirements 6.1**

### Property 11: API Proxy Configuration
*For any* API request to /api or /files paths during development, the request should be correctly proxied to the backend server as configured.
**Validates: Requirements 6.2**

### Property 12: Build Script Compatibility
*For any* migrated project, running the build script should successfully complete without errors and generate production-ready files.
**Validates: Requirements 8.2**

## Error Handling

### Build Errors

**Dependency Resolution Errors:**
- If Vite dependencies fail to install, provide clear error message with installation command
- Verify Node.js version compatibility (Node 14.18+ or 16+ required)
- Check for conflicting dependencies and suggest resolution

**Configuration Errors:**
- If vite.config.js has syntax errors, provide line number and error description
- Validate plugin configurations before build
- Check for missing required plugins

**Module Resolution Errors:**
- If @ alias fails to resolve, verify resolve.alias configuration in vite.config.js
- Check for incorrect import paths and suggest corrections
- Ensure all file extensions are properly configured

### Runtime Errors

**Environment Variable Errors:**
- If environment variables are not accessible, check VITE_ prefix
- Verify .env files are in the correct location
- Ensure import.meta.env is used instead of process.env

**Static Asset Errors:**
- If static assets fail to load, verify public directory structure
- Check asset paths in HTML and components
- Ensure correct base URL configuration

**HMR Errors:**
- If Hot Module Replacement fails, check for circular dependencies
- Verify component structure compatibility with Vite
- Provide fallback to full page reload if HMR fails

### Development Server Errors

**Port Conflicts:**
- If configured port is already in use, suggest alternative port
- Provide option to kill existing process or use different port
- Log clear error message with resolution steps

**Proxy Errors:**
- If API proxy fails, verify backend server is running
- Check proxy configuration syntax in vite.config.js
- Provide detailed error logs for debugging

## Testing Strategy

### Pre-Migration Testing

**Baseline Establishment:**
- Document current Vue CLI build time and bundle size
- Capture screenshots of all major pages
- Record current development server startup time
- List all environment variables in use

**Dependency Audit:**
- Identify all Webpack-specific loaders and plugins
- Check for Vite-compatible alternatives
- Document any custom Webpack configurations

### Migration Testing

**Configuration Validation:**
- Verify vite.config.js syntax and structure
- Test all plugin configurations
- Validate environment variable loading
- Check path alias resolution

**Build Testing:**
- Run development build and verify no errors
- Run production build and verify output structure
- Compare bundle sizes (Vite should be smaller)
- Verify source maps generation (if enabled)

**Functional Testing:**
- Test all routes and navigation
- Verify API calls work correctly
- Check static asset loading (images, fonts, icons)
- Test environment-specific configurations
- Verify SCSS compilation
- Test HMR functionality

### Post-Migration Testing

**Performance Validation:**
- Measure development server startup time (should be < 3 seconds)
- Measure HMR update time (should be < 100ms)
- Compare production bundle sizes
- Verify build time improvements

**Compatibility Testing:**
- Test in all target browsers
- Verify legacy browser support (if @vitejs/plugin-legacy is used)
- Check for console errors or warnings
- Test on different operating systems (Windows, macOS, Linux)

**Integration Testing:**
- Verify backend API integration
- Test authentication flows
- Check file upload functionality
- Verify WebSocket connections (if applicable)

### Regression Testing

**Feature Verification:**
- Test all user-facing features
- Verify admin functionality
- Check form submissions and validations
- Test error handling and edge cases

**Visual Regression:**
- Compare screenshots before and after migration
- Verify CSS and styling consistency
- Check responsive design on different screen sizes
- Test dark mode (if applicable)

### Testing Tools and Commands

```bash
# Development server testing
npm run dev

# Production build testing
npm run build
npm run preview

# Linting
npm run lint

# Check bundle size
npm run build -- --mode production
# Then inspect dist/ directory

# Test specific environment
npm run build -- --mode staging
```

### Success Criteria

Migration is considered successful when:

1. ✅ Development server starts in < 3 seconds
2. ✅ HMR updates in < 100ms
3. ✅ Production build completes without errors
4. ✅ All routes and pages load correctly
5. ✅ API calls function as expected
6. ✅ Static assets load properly
7. ✅ No console errors in browser
8. ✅ Bundle size is equal or smaller than Vue CLI
9. ✅ All tests pass (if test suite exists)
10. ✅ Application functions identically to pre-migration state
