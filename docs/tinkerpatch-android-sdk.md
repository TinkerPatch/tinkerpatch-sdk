# SDK 接入

[ ![Download](https://api.bintray.com/packages/simsun/maven/tinkerpatch-android-sdk/images/download.svg) ](https://bintray.com/simsun/maven/tinkerpatch-android-sdk/_latestVersion)

这里只是针对 TinkerPatch SDK的使用说明，对于 Tinker 的基本用法，可参考[Tinker 接入指南](https://github.com/Tencent/tinker/wiki/Tinker-%E6%8E%A5%E5%85%A5%E6%8C%87%E5%8D%97)。 Tinker SDK 在 Github 为大家提供了大量的范例，大家可点击前往 [[TinkerPatch Samples]](https://github.com/TinkerPatch).

## 第一步 添加 gradle 插件依赖

gradle 远程仓库依赖 jcenter, 例如 TinkerPatch Sample 中的[build.gradle](https://github.com/TinkerPatch/tinkerpatch-sample/blob/master/build.gradle).

```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        // TinkerPatch 插件
        classpath "com.tinkerpatch.sdk:tinkerpatch-gradle-plugin:1.1.4"
    }
}
```

**注意，在这里SDK使用了fat打包的模式，我们不能再引入任何 Tinker 的相关依赖，否则会造成版本冲突。当前SDK是基于 tinker 1.7.7 内核开发的。**

## 第二步 集成 TinkerPatch SDK

添加TinkerPatch SDK 库的 denpendencies 依赖, 可参考 Sample 中的 [app/build.gradle](https://github.com/TinkerPatch/tinkerpatch-sample/blob/master/app/build.gradle):

```
dependencies {
    // 若使用annotation需要单独引用,对于tinker的其他库都无需再引用
    provided("com.tencent.tinker:tinker-android-anno:1.7.7")
    compile("com.tinkerpatch.sdk:tinkerpatch-android-sdk:1.1.4")
}
```
**注意,若使用 annotation 自动生成 Application， 需要单独引入 Tinker的 tinker-android-anno 库。除此之外，我们无需再单独引入 tinker 的其他库。**

为了简单方便，我们将 TinkerPatch 相关的配置都放于 [tinkerpatch.gradle](https://github.com/TinkerPatch/tinkerpatch-sample/blob/master/app/tinkerpatch.gradle) 中, 我们需要将其引入：

```
apply from: 'tinkerpatch.gradle'
```

## 第三步 配置 tinkerpatchSupport 参数
打开引入的 tinkerpatch.gradle 文件，它的具体参数如下：

```
tinkerpatchSupport {
    /** 可以在debug的时候关闭 tinkerPatch **/
    tinkerEnable = true
    reflectApplication = true
    appKey = "${yourAppKey}"
    autoBackupApkPath = "${bakPath}"

    baseApkInfos {
        item {
            variantName = "debug"
            appVersion = "1.0.0"
            baseApkFile = "${pathPrefix}/${name}.apk"
            baseProguardMappingFile = "${pathPrefix}/${name}-mapping.txt"
            baseResourceRFile = "${pathPrefix}/${name}-R.txt"
        }
        item {
            variantName = "release"
            appVersion = "1.0.0"
            baseApkFile = "${pathPrefix}/${name}.apk"
            baseProguardMappingFile = "${pathPrefix}/${name}-mapping.txt"
            baseResourceRFile = "${pathPrefix}/${name}-R.txt"
        }
    }
}
```

**这里缺失了部分变量的声明，完整例子可以参照上文中给的链接**

它的具体含义如下：

| 参数               | 默认值      | 描述       |
| ----------------- | ---------  | ---------  |
| tinkerEnable       | true  | 是否开启 tinkerpatchSupport 插件功能。 |
| appKey            | ""  | 在 TinkerPatch 平台 申请的 appkey, 例如 sample 中的 'f828475486f91936' |
| appVersion     | "" | 在 TinkerPatch 平台 输入的版本号, 例如 sample 中的 '1.0.0'。 **注意，我们使用 appVersion 作为 TinkerId, 我们需要保证每个发布出去的基础安装包的 appVersion 都不一样。**|
| `reflectApplication`     | false | 是否反射 Application 实现一键接入；**一般来说，接入 Tinker 我们需要改造我们的 Application, 若这里为 true， 即我们无需对应用做任何改造即可接入**。|
| autoBackupApkPath  |"" |将每次编译产生的 apk/mapping.txt/R.txt 归档存储的位置|
| baseApkFile       | "" | `基准包的文件路径, 对应 tinker 插件中的 oldApk 参数`;编译补丁包时，必需指定基准版本的 apk，默认值为空，则表示不是进行补丁包的编译。   |     
| baseProguardMappingFile       | "" | `基准包的 Proguard mapping.txt 文件路径, 对应 tinker 插件 applyMapping 参数`；在编译新的 apk 时候，我们希望通过保持基准 apk 的 proguard 混淆方式，从而减少补丁包的大小。这是强烈推荐的，编译补丁包时，我们推荐输入基准 apk 生成的 mapping.txt 文件。   |  
| baseResourceRFile       | "" |  `基准包的资源 R.txt 文件路径, 对应 tinker 插件 applyResourceMapping 参数`；在编译新的apk时候，我们希望通基准 apk 的 R.txt 文件来保持 Resource Id 的分配，这样不仅可以减少补丁包的大小，同时也避免由于 Resource Id 改变导致 remote view 异常。   |  


**我们可以平行的在`baseApkInfos`中定义多组item，用来达到适配不同的flavor编译。**

**一般来说，我们无需修改引用 android 的编译配置，也不用修改 tinker 插件原来的配置**。针对特殊需求，具体的参数含义可参考 Tinker 文档:[Tinker 接入指南](https://github.com/Tencent/tinker/wiki/Tinker-%E6%8E%A5%E5%85%A5%E6%8C%87%E5%8D%97).

## 第四步 初始化 TinkerPatch SDK
最后在我们的代码中，只需简单的初始化 TinkerPatch 的 SDK 即可，我们无需考虑 Tinker 是如何下载/合成/应用补丁包， 也无需引入各种各样 Tinker 的相关类。

### 1. reflectApplication = true 的情况
若我们使用 reflectApplication 模式，我们无需为接入 Tinker 而改造我们的 Application 类。初始化 SDK 可参考 tinkerpatch-easy-sample 中的 [SampleApplication 类](https://github.com/TinkerPatch/tinkerpatch-easy-sample/blob/master/app/src/main/java/com/tinkerpatch/easy_sample/SampleApplication.java).

```
public class SampleApplication extends Application {

    ...

    @Override
    public void onCreate() {
        super.onCreate();
        // 我们可以从这里获得Tinker加载过程的信息
        tinkerApplicationLike = TinkerPatchApplicationLike.getTinkerPatchApplicationLike();

        // 初始化TinkerPatch SDK, 更多配置可参照API章节中的,初始化SDK
        TinkerPatch.init(tinkerApplicationLike)
            .reflectPatchLibrary()
            .setPatchRollbackOnScreenOff(true)
            .setPatchRestartOnSrceenOff(true);

        // 每隔3个小时去访问后台时候有更新,通过handler实现轮训的效果
        new FetchPatchHandler().fetchPatchWithInterval(3);
    }

    ...

```

我们将 Tinker 加载补丁过程的结果存放在 TinkerPatchApplicationLike 中。

### 2. reflectApplication = false 的情况
若我们已经完成了应用的 Application 改造，即将 Application 的逻辑移动到 ApplicationLike类中。我们可以参考 tinkerpatch-sample 中的 [SampleApplicationLike 类](https://github.com/TinkerPatch/tinkerpatch-sample/blob/master/app/src/main/java/tinker/sample/android/app/SampleApplicationLike.java).

```
public class SampleApplicationLike extends DefaultApplicationLike {

    ...

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化TinkerPatch SDK, 更多配置可参照API章节中的,初始化 SDK
        TinkerPatch.init(this)
            .reflectPatchLibrary()
            .setPatchRollbackOnScreenOff(true)
            .setPatchRestartOnSrceenOff(true);

        // 每隔3个小时去访问后台时候有更新,通过handler实现轮训的效果
        new FetchPatchHandler().fetchPatchWithInterval(3);
    }

    ...

}
```

## 第五步 使用步骤
TinkerPatch 的使用步骤非常简单，一般来说可以参考以下几个步骤：

1. 运行 `assembleRelease` task 构建基准包，即将要发布的版本；
2. 通过 `autoBackupApkPath` 保存编译的产物 apk/mapping.txt/R.txt 文件；
3. 若想发布补丁包， 只需将步骤2保存下来的文件分别填到 `baseApkFile`/`baseProguardMappingFile`/`baseResourceRFile` 参数中；
4. 运行 `tinkerPatchRelease` task 构建补丁包，补丁包将位于 `build/outputs/tinkerPatch` 中；
5. 1.1.0版本开始TinkerPatch支持了多Flavor多AppVersion的配置，可以按自己的需求添加相应item即可。
