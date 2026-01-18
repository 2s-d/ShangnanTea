const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8082,
    // 如果使用云端 Mock，不需要代理配置
    // 如果使用本地后端，取消下面的注释并启用代理
    proxy: process.env.VUE_APP_API_BASE_URL && process.env.VUE_APP_API_BASE_URL.startsWith('http') 
      ? {} // 使用云端 Mock 时，不配置代理
      : {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          pathRewrite: {
            '^/api': '/api'
          }
        },
        '/files': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          pathRewrite: {
            '^/files': '/files'
          }
        }
      },
    setupMiddlewares: (middlewares, devServer) => {
      return middlewares
    }
  },
  css: {
    loaderOptions: {
      sass: {
        // 全局样式已在main.js中导入，这里不再使用additionalData
        // additionalData: `@import "@/assets/styles/global.scss";`
      }
    }
  }
}) 