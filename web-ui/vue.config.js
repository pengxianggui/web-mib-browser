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
        target: 'http://localhost:8888',
        changeOrigin: true,
        pathRewrite: {
          '^/api': ''
        }
      },
      '/socket': {
        target: 'ws://localhost:8888',
        ws: true,
        changeOrigin: true,
        pathRewrite: {
          '^/socket': '/socket'
        }
      },
      '/webssh': {
        target: 'ws://localhost:8888',
        ws: true,
        changeOrigin: true,
        pathRewrite: {
          '^/webssh': '/webssh'
        }
      }
    }
  }
})
