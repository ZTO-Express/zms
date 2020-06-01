/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const path = require('path')
// 编译测时速
// const SpeedMeasurePlugin = require('speed-measure-webpack-plugin')
// const smp = new SpeedMeasurePlugin()

function resolve(dir) {
  return path.join(__dirname, dir)
}
module.exports = {
  // 打包的路径方式-绝对路径
  publicPath: '/',
  // 输出目录
  outputDir: '../resources/static',
  // 静态资源目录
  assetsDir: '',
  // 是否在构建生产包时生成 sourceMap 文件
  productionSourceMap: false,
  // 配置 webpack-dev-server 行为
  devServer: {
    // 端口
    port: 8088,
    // 启动时自动默认浏览器打开
    open: true,
    overlay: {
      // 显示warnings
      warnings: true,
      // 显示error
      errors: true
    },
    // 代理配置
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 后台地址
        changeOrigin: true
      }
    }
  },
  // 简单配置
  configureWebpack: {
    resolve: {
      // 别名
      alias: {
        vue$: 'vue/dist/vue.esm.js',
        '@': resolve('src')
      }
    }
  },
  // 简单配置+编译测时速
  // configureWebpack: smp.wrap({
  //   resolve: {
  //     // 别名
  //     alias: {
  //       '@': resolve('src')
  //     }
  //   }
  // }),
  // 链式高级配置
  chainWebpack: config => {
    console.log('process.env.NODE_ENV', process.env.NODE_ENV)
    // 开发环境(npm run serve) eval-source-map
    config.when(process.env.NODE_ENV === 'development', config => config.devtool('cheap-module-eval-source-map'))
    // 生产环境(npm run build)
    // config.when(process.env.NODE_ENV === "production", config =>
    //   config.devtool("source-map")
    // );

    // 分析
    config.when(process.env.use_analyzer === 'true', config =>
      config.plugin('webpack-bundle-analyzer').use(require('webpack-bundle-analyzer').BundleAnalyzerPlugin)
    )
    // 分包策略
    config.when(process.env.NODE_ENV === 'production', config => {
      config.optimization.splitChunks({
        chunks: 'all',
        cacheGroups: {
          libs: {
            name: 'chunk-libs', //基础类库，比如 vue+vue-router+vuex+axios+全局被共用的，体积不大的第三方库
            test: /[\\/]node_modules[\\/]/,
            priority: 10,
            chunks: 'initial' // only package third parties that are initially dependent
          },
          elementUI: {
            name: 'chunk-elementUI', //  按需提取、加载较大的第三方库。模仿chunk-elementUI 创建
            priority: 20, // the weight needs to be larger than libs and app or it will be packaged into libs or app
            test: /[\\/]node_modules[\\/]_?element-ui(.*)/ // in order to adapt to cnpm
          },
          commons: {
            name: 'chunk-commons', //提取给views视图层复用较多的公共组件
            test: resolve('src/modules/components'), // can customize your rules
            minChunks: 3, //  minimum common number
            priority: 5,
            reuseExistingChunk: true
          }
        }
      })
      config.optimization.runtimeChunk('single')
    })
    // 代码压缩默认 true
    // config.optimization.minimize(false)

    //gizp压缩
    // config.when(process.env.NODE_ENV === "production", config =>
    //   config
    //     .plugin("compression-webpack-plugin")
    //     .use(require("compression-webpack-plugin"), [
    //       {
    //         filename: "[path].gz[query]",
    //         algorithm: "gzip",
    //         test: new RegExp("\\.(" + ["js", "css"].join("|") + ")$"),
    //         threshold: 10240,
    //         minRatio: 0.8
    //       }
    //     ])
    // );
  }
}
