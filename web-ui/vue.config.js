const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  outputDir: process.env.VUE_APP_MODE === 'toBackend' ? '../src/main/resources/static' : 'dist',
  devServer: {
    client: {
      overlay: false
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: {
          '^/api': '/api'
        }
      }
    }
  }
})
