# AgoraPluginRawDataAPI for Android

*Read this in other languages: [English](README.en.md)*

这个示例项目演示了如何使用 Agora 裸数据接口，实现获取音视频数据流的功能。

在这个示例项目中包含了以下功能：

- 获取本地视频采集数据，其他用户视频数据；
- 获取本地音频采集数据，其他用户音频数据；
- 本地视频和远端视频截图功能

## 运行示例程序
- 本程序作为一个Module程序，需要添加到主项目中作为依赖项目运行
- setting.gradle 中添加  include ':AgoraPluginRawDataAPI'
- 主项目 build.gradle 下添加 
	dependencies {
	    compile project(path: ':AgoraPluginRawDataAPI')
	}
- 项目需要配置 NDK 路径

## 代码示例
- 在需要接收回调的类，实现回调方法      class implements MediaDataCallbackUtil.MediaDataObserver
- 初始化 MediaDataCallbackUtil 对象    MediaDataCallbackUtil mediaDataCallbackUtil = MediaDataCallbackUtil.the();
- 设置数据回调                                      MediaPreProcessing.setCallback(mediaDataCallbackUtil);
- 添加回调监听                                      mediaDataCallbackUtil.addObserverListerner(this);
- 不需要时移除回调监听                         mediaDataCallbackUtil.removeObserverListener(this);
- 本地视频截图                                      mediaDataCallbackUtil.saveCaptureVideoShot(path);
- 远端视频截图                                      mediaDataCallbackUtil.saveRenderVideoShot(path);

## 运行环境
- Android Studio 2.0 +
- 真实 Android 设备 (Nexus 5X 或者其它设备)
- 部分模拟器会存在功能缺失或者性能问题，所以推荐使用真机

## 联系我们
- 完整的 API 文档见 [文档中心](https://docs.agora.io/cn/)
- 如果在集成中遇到问题, 你可以到 [开发者社区](https://dev.agora.io/cn/) 提问
- 如果有售前咨询问题, 可以拨打 400 632 6626，或加入官方Q群 12742516 提问
- 如果需要售后技术支持, 你可以在 [Agora Dashboard](https://dashboard.agora.io) 提交工单
- 如果发现了示例代码的 bug, 欢迎提交 [issue](https://github.com/AgoraIO/Wawaji/issues)

## 代码许可
The MIT License (MIT).
