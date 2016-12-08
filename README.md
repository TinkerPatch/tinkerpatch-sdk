# TinkerPatch SDK

[![Build Status](https://travis-ci.org/TinkerPatch/tinkerpatch-sdk.svg?branch=master)](https://travis-ci.org/TinkerPatch/tinkerpatch-sdk)
[ ![Download](https://api.bintray.com/packages/simsun/maven/tinker-server-android/images/download.svg) ](https://bintray.com/simsun/maven/tinker-server-android/_latestVersion)

## SDK接入

这里只是针对 TinkerPatch SDK的使用说明，对于 Tinker 的基本用法，可参考[ Tinker接入指南](https://github.com/Tencent/tinker/wiki/Tinker-%E6%8E%A5%E5%85%A5%E6%8C%87%E5%8D%97)。

### 第一步 添加gradle依赖

gradle远程仓库依赖jcenter,例如 Tinker server sample中的[build.gradle](https://github.com/simpleton/tinker_server_client/blob/master/tinker-server-sample/build.gradle).

```java
repositories {
    jcenter()
}
```

再添加sdk库的dependencies依赖:

```java
dependencies {
    compile("com.tencent.tinker:tinker-server-android:0.3.2")
}
```

### 第二步 配置AndroidManifest文件

在AndroidManifest中声明SDK需要的权限：

```java
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 第三步 配置AppKey与AppVersion
在 TinkerPatch 平台中得到的 AppKey 以及 AppVersion记住，我们可以简单的将他们写入 [buildConfig](https://github.com/simpleton/tinker_server_client/blob/master/tinker-server-sample/build.gradle#L86) 中:

```java
buildConfigField "String", "APP_KEY", "\"f938475486f91936\""
buildConfigField "String", "APP_VERSION",  "\"3.0.0\""
```

### 第四步 初始化 TinkerPatch SDK

我们提供默认的默认的实现，位于`TinkerManager`和`TinkerServerManager`.
首先初始化 TinkerPatch 的 SDK，例如 Sample 中 [SampleApplicationLike类](https://github.com/simpleton/tinker_server_client/blob/master/tinker-server-sample/src/main/java/tinker/sample/android/app/SampleApplicationLike.java#L93)：

```java
//初始化Tinker
TinkerManager.installTinker(this);
//初始化TinkerPatch SDK
TinkerServerManager.installTinkerServer(
  getApplication(), Tinker.with(getApplication()), 3,
  BuildConfig.APP_KEY, BuildConfig.APP_VERSION, "default"
);
//开始检查是否有补丁，这里配置的是每隔访问3小时服务器是否有更新。
TinkerServerManager.checkTinkerUpdate(false);
```

SDK 需要Tinker已经初始化，`3`表示客户端每隔三个小时才会访问服务器一次，具体的 API 将在后面详细说明。
appKey和appVersion为第三部填写的配置，可以通过`BuildConfig.APP_KEY`和`BuildConfig.APP_VERSION`得到。
由于GooglePlay渠道的限制，不能使用原生代码下发的机制更新app，我们会过滤channel中含有`google`的关键字，停止动态更新功能。

如果要使用SDK提供的默认的Service实现，需要在`AndroidManifest.xml`中声明：

```java
<service
  android:name="com.tencent.tinker.app.service.TinkerServerResultService"
  android:exported="false"
/>
```

你也可以根据自己的需求实现Manager, [API相关文档](http://tinkerpatch.com/Docs/api)
所有与TinkerPatch后台交互的 API 都位于 TinkerServerClient.java中。

[更多文档](http://tinkerpatch.com/Docs/intro)
