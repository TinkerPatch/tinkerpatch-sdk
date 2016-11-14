# TinkerPatch SDK

[![Build Status](https://travis-ci.org/simpleton/tinker_server_client.svg?branch=master)](https://travis-ci.org/simpleton/tinker_server_client)
[ ![Download](https://api.bintray.com/packages/simsun/maven/tinker-server-android/images/download.svg) ](https://bintray.com/simsun/maven/tinker-server-android/_latestVersion)

## SDK接入

这里只是针对 TinkerPatch SDK的使用说明，对于 Tinker 的基本用法，可参考[ Tinker接入指南](https://github.com/Tencent/tinker/wiki/Tinker-%E6%8E%A5%E5%85%A5%E6%8C%87%E5%8D%97)。

[更多文档](http://tinkerpatch.com/Docs/intro)

### 第一步 添加gradle依赖

gradle远程仓库依赖jcenter,例如 Tinker server sample中的[build.gradle](https://github.com/simpleton/tinker_server_client/blob/master/tinker-server-sample/build.gradle).

```
repositories {
    jcenter()
}
```

再添加sdk库的dependencies依赖:

```
dependencies {
    compile("com.tencent.tinker:tinker-server-android:0.2.0")
}
```

### 第二步 配置AndroidManifest文件

在AndroidManifest中声明SDK需要的权限：

```
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 第三步 配置AppKey与AppVersion
在 TinkerPatch 平台中得到的 AppKey 以及 AppVersion记住，我们可以简单的将他们写入 [buildConfig](https://github.com/simpleton/tinker_server_client/blob/master/tinker-server-sample/build.gradle#L86) 中:

```
buildConfigField "String", "APP_KEY", "\"f938475486f91936\""
buildConfigField "String", "APP_VERSION",  "\"3.0.0\""
```

### 第四步 初始化 TinkerPatch SDK
首先初始化 TinkerPatch 的 SDK，例如 Sample 中 [SampleApplicationLike类](https://github.com/simpleton/tinker_server_client/blob/master/tinker-server-sample/src/main/java/tinker/sample/android/app/SampleApplicationLike.java#L88)：

```
TinkerServerManager.installTinkerServer(getApplication(), Tinker.with(getApplication()), 3);
```
SDK 需要Tinker已经初始化，`3`表示客户端每隔三个小时才会访问服务器一次，具体的 API 将在后面详细说明。

此外为了可以监控补丁的合成与加载情况，我们需要在以下几个类中增加补丁后台的上报代码：

上报是否被PatchListener拦截；在我们的 PatchListener 实现类添加相关上报，例如 [SamplePatchListener类](https://github.com/simpleton/tinker_server_client/blob/master/tinker-server-sample/src/main/java/tinker/sample/android/reporter/SamplePatchListener.java#L60)：

```
TinkerServerManager.reportTinkerPatchListenerFail(returnCode, patchMd5);
```

上报加载是否成功；在我们的 TinkerResultService 实现类添加相关上报，例如 [SampleResultService类](https://github.com/simpleton/tinker_server_client/blob/master/tinker-server-sample/src/main/java/tinker/sample/android/service/SampleResultService.java#L55)：

```
TinkerServerManager.reportTinkerPatchFail(result);
```

上报合成是否成功；在我们的 LoadReporter 实现类添加相关上报，例如 [SampleLoadReporter类](https://github.com/simpleton/tinker_server_client/blob/master/tinker-server-sample/src/main/java/tinker/sample/android/reporter/SampleLoadReporter.java#L41)：


```
TinkerServerManager.reportTinkerLoadFail();
```
