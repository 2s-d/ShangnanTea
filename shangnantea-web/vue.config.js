const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8082,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: {
          '^/api': '/api'
        }
      }
    },
    setupMiddlewares: (middlewares, devServer) => {
      return middlewares;
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