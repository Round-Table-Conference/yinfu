银龄心伴Android客户端

本文件夹为银龄心伴Android应用程序的根目录，采用Kotlin开发，使用ViewBinding、Material Design、Coroutines等技术。

主要目录说明：
- app/               - 应用模块，包含源码和资源
- gradle/            - Gradle Wrapper配置
- build.gradle.kts   - 项目构建配置
- settings.gradle.kts - 项目设置

app目录结构：
- src/main/
  - java/com/example/yinfuapp/  - Kotlin源代码
    - data/           - 数据层（API、适配器、本地存储、模型）
    - ui/             - UI层（Activity、Fragment）
  - res/              - 资源文件（布局、图片、样式、字符串）
  - assets/           - 静态资源（视频文件）
  - AndroidManifest.xml - 应用清单配置
