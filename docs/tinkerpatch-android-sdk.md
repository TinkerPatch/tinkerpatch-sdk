# SDK 接入

[![Build Status](https://travis-ci.org/TinkerPatch/tinkerpatch-sample.svg?branch=master)](https://travis-ci.org/TinkerPatch/tinkerpatch-sample)
[![Download](https://api.bintray.com/packages/simsun/maven/tinkerpatch-android-sdk/images/download.svg) ](https://bintray.com/simsun/maven/tinkerpatch-android-sdk/_latestVersion)
[![Join Slack](https://slack.tinkerpatch.com/badge.svg)](https://slack.tinkerpatch.com)

这里只是针对 TinkerPatch SDK的使用说明，对于 Tinker 的基本用法，可参考[Tinker 接入指南](https://github.com/Tencent/tinker/wiki/Tinker-%E6%8E%A5%E5%85%A5%E6%8C%87%E5%8D%97)。 Tinker SDK 在 Github 为大家提供了大量的范例，大家可点击前往 [[TinkerPatch Samples]](https://github.com/TinkerPatch).

## 第一步 添加 gradle 插件依赖

gradle 远程仓库依赖 jcenter, 例如 TinkerPatch Sample 中的 [build.gradle](https://github.com/TinkerPatch/tinkerpatch-sample/blob/master/build.gradle).

```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        // TinkerPatch 插件
        classpath "com.tinkerpatch.sdk:tinkerpatch-gradle-plugin:1.1.6"
    }
}
```

**注意，在这里 SDK 使用了 fat 打包的模式，我们不能再引入任何 Tinker 的相关依赖，否则会造成版本冲突。当前 SDK 是基于 tinker 1.7.9 内核开发的。**

## 第二步 集成 TinkerPatch SDK

添加 TinkerPatch SDK 库的 denpendencies 依赖, 可参考 Sample 中的 [app/build.gradle](https://github.com/TinkerPatch/tinkerpatch-sample/blob/master/app/build.gradle):

```
dependencies {
    // 若使用annotation需要单独引用,对于tinker的其他库都无需再引用
    provided("com.tencent.tinker:tinker-android-anno:1.7.9")
    compile("com.tinkerpatch.sdk:tinkerpatch-android-sdk:1.1.6")
}
```
**注意,若使用 annotation 自动生成 Application， 需要单独引入 Tinker 的 tinker-android-anno 库。除此之外，我们无需再单独引入 tinker 的其他库。**

为了简单方便，我们将 TinkerPatch 相关的配置都放于 [tinkerpatch.gradle](https://github.com/TinkerPatch/tinkerpatch-sample/blob/master/app/tinkerpatch.gradle) 中, 我们需要将其引入：

```
apply from: 'tinkerpatch.gradle'
```

## 第三步 配置 tinkerpatchSupport 参数
打开引入的 [tinkerpatch.gradle](https://github.com/TinkerPatch/tinkerpatch-sample/blob/master/app/tinkerpatch.gradle) 文件，它的具体参数如下：

```
tinkerpatchSupport {
    tinkerpatchSupport {
    /** 可以在debug的时候关闭 tinkerPatch **/
    tinkerEnable = true

    /** 是否使用一键接入功能  **/
    reflectApplication = true

    autoBackupApkPath = "${bakPath}"

    /** 在tinkerpatch.com得到的appKey **/
    appKey = "yourAppKey"
    /** 注意: 若发布新的全量包, appVersion一定要更新 **/
    appVersion = "1.0.0"

    def pathPrefix = "${bakPath}/${baseInfo}/${variantName}/"
    def name = "${project.name}-${variantName}"

    baseApkFile = "${pathPrefix}/${name}.apk"
    baseProguardMappingFile = "${pathPrefix}/${name}-mapping.txt"
    baseResourceRFile = "${pathPrefix}/${name}-R.txt"
}
```

它的具体含义如下：

| 参数               | 默认值      | 描述       |
| ----------------- | ---------  | ---------  |
| tinkerEnable       | true  | 是否开启 tinkerpatchSupport 插件功能。 |
| appKey            | ""  | 在 TinkerPatch 平台 申请的 appkey, 例如 sample 中的 'f828475486f91936' |
| appVersion     | "" | 在 TinkerPatch 平台 输入的版本号, 例如 sample 中的 '1.0.0'。 **注意，我们使用 appVersion 作为 TinkerId, 我们需要保证每个发布出去的基础安装包的 appVersion 都不一样。**|
| reflectApplication     | false | 是否反射 Application 实现一键接入；**一般来说，接入 Tinker 我们需要改造我们的 Application, 若这里为 true， 即我们无需对应用做任何改造即可接入**。|
| autoBackupApkPath  |"" |将每次编译产生的 apk/mapping.txt/R.txt 归档存储的位置|
| baseApkFile       | "" | `基准包的文件路径, 对应 tinker 插件中的 oldApk 参数`;编译补丁包时，必需指定基准版本的 apk，默认值为空，则表示不是进行补丁包的编译。   |     
| baseProguardMappingFile       | "" | `基准包的 Proguard mapping.txt 文件路径, 对应 tinker 插件 applyMapping 参数`；在编译新的 apk 时候，我们希望通过保持基准 apk 的 proguard 混淆方式，从而减少补丁包的大小。这是强烈推荐的，编译补丁包时，我们推荐输入基准 apk 生成的 mapping.txt 文件。   |  
| baseResourceRFile       | "" |  `基准包的资源 R.txt 文件路径, 对应 tinker 插件 applyResourceMapping 参数`；在编译新的apk时候，我们希望通基准 apk 的 R.txt 文件来保持 Resource Id 的分配，这样不仅可以减少补丁包的大小，同时也避免由于 Resource Id 改变导致 remote view 异常。   |  
| protectedApp | false | 是否开启支持加固，**注意：只有在使用加固时才能开启此开关**|


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
        TinkerPatch.init(this)
            .reflectPatchLibrary()
            .setPatchRollbackOnScreenOff(true)
            .setPatchRestartOnSrceenOff(true)
            .setFetchPatchIntervalByHours(3);

        // 每隔3个小时(通过setFetchPatchIntervalByHours设置)去访问后台时候有更新,通过handler实现轮训的效果
        TinkerPatch.with().fetchPatchUpdateAndPollWithInterval();
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
            .setPatchRestartOnSrceenOff(true)
            .setFetchPatchIntervalByHours(3);

        // 每隔3个小时（通过setFetchPatchIntervalByHours设置）去访问后台时候有更新,通过handler实现轮训的效果
        TinkerPatch.with().fetchPatchUpdateAndPollWithInterval();
    }

    ...

}
```

**注意：初始化的代码建议紧跟 super.onCreate(),并且所有进程都需要初始化，已达到所有进程都可以被 patch 的目的**

**如果你确定只想在主进程中初始化 tinkerPatch，那也请至少在 :patch 进程中初始化，否则会有造成 :patch 进程crash，无法使补丁生效**

## 第五步 使用步骤
TinkerPatch 的使用步骤非常简单，一般来说可以参考以下几个步骤：

1. 运行 `assembleRelease` task 构建基准包（**请在发布前确保更新tinkerpatchSupport中的appVersion**），tinkerPatch会基于你填入的`autoBackupApkPath`自动备份基础包信息到相应的文件夹，包含：apk文件、R.txt文件和mapping.txt文件
（**注：mapping.txt是proguard的产物，如果你没有开启proguard则不会有这个文件**）
2. 若想发布补丁包， 只需将自动保存下来的文件分别填到`tinkerpatchSupport`中的`baseApkFile`、`baseProguardMappingFile`和`baseResourceRFile` 参数中；
3. 运行 `tinkerPatchRelease` task 构建补丁包，补丁包将位于 `build/outputs/tinkerPatch`下。

## 其他

### 1. 对Flavors的支持

在TinkerPatchSupport中添加如下字段, 如果你只是多渠道的需求，建议不要使用Flavor。多flavor必须在后台建立相应的基线工程(如下例子的命名规则为：appVersion_flavorName)，每次生成补丁时也必须对应的生成多个分别上传。

这里增加了`tinkerPatchAllFlavorsDebug` 和 `tinkerPatchAllFlavorsRelease` 用于一次性生成所有flavors的Patch包。

具体可以参照[tinkerpatch-flavors-sample](https://github.com/TinkerPatch/tinkerpatch-flavors-sample)。

**如果只是多渠道的需求，建议不要使用flavor的方式。首先其打包很慢，其次需要维护多个基线包，后期维护成本也很大。Tinker官方推荐 [packer-ng-plugin](https://github.com/mcxiaoke/packer-ng-plugin )或者 [walle](https://github.com/Meituan-Dianping/walle) 来进行多渠道打包，其中walle是支持最新的SchemaV2签名的。**

```
    /** 若有编译多flavors需求,可在flavors中覆盖以下参数
     *  你也可以直接通过tinkerPatchAllFlavorDebug/tinkerPatchAllFlavorRelease, 一次编译所有的flavor补丁包
     *  注意的是:除非你不同的flavor代码是不一样的,不然建议采用zip comment或者文件方式生成渠道信息
     **/
    productFlavors {
        flavor {
            flavorName = "flavor1"
            // 后台需要按照每个flavor的appVersion来建立独立的工程，并单独下发补丁
            appVersion = "${tinkerpatchSupport.appVersion}_${flavorName}"

            pathPrefix = "${bakPath}/${baseInfo}/${flavorName}${buildType}/"
            name = "${project.name}-${flavorName}${buildType}"

            baseApkFile = "${pathPrefix}/${name}.apk"
            baseProguardMappingFile = "${pathPrefix}/${name}-mapping.txt"
            baseResourceRFile = "${pathPrefix}/${name}-R.txt"
        }

        flavor {
            flavorName = "flavor2"
            // 后台需要按照每个flavor的appVersion来建立独立的工程，并单独下发补丁
            appVersion = "${tinkerpatchSupport.appVersion}_${flavorName}"

            pathPrefix = "${bakPath}/${baseInfo}/${flavorName}${buildType}/"
            name = "${project.name}-${flavorName}${buildType}"

            baseApkFile = "${pathPrefix}/${name}.apk"
            baseProguardMappingFile = "${pathPrefix}/${name}-mapping.txt"
            baseResourceRFile = "${pathPrefix}/${name}-R.txt"
        }
    }
```

### 2. 对加固的支持

这里默认大家有同时生成加固渠道与非加固渠道的需求，如果只是单一需要加固，可以直接在配置中开启`protectedApp = true`即可。

可以参考tinkerpatch.gradle文件，具体工程可以参照[tinkerpatch-flavors-sample](https://github.com/TinkerPatch/tinkerpatch-flavors-sample)：
```
    productFlavors {
        flavor {
            flavorName = "protect"
            appVersion = "${tinkerpatchSupport.appVersion}_${flavorName}"

            pathPrefix = "${bakPath}/${baseInfo}/${flavorName}-${variantName}/"
            name = "${project.name}-${flavorName}-${variantName}"

            baseApkFile = "${pathPrefix}/${name}.apk"
            baseProguardMappingFile = "${pathPrefix}/${name}-mapping.txt"
            baseResourceRFile = "${pathPrefix}/${name}-R.txt"

            /** 开启加固开关，上传此flavor的apk到加固网站进行加固 **/
            protectedApp = true
        }

        flavor {
            flavorName = "flavor1"
            appVersion = "${tinkerpatchSupport.appVersion}_${flavorName}"

            pathPrefix = "${bakPath}/${baseInfo}/${flavorName}-${variantName}/"
            name = "${project.name}-${flavorName}-${variantName}"

            baseApkFile = "${pathPrefix}/${name}.apk"
            baseProguardMappingFile = "${pathPrefix}/${name}-mapping.txt"
            baseResourceRFile = "${pathPrefix}/${name}-R.txt"
        }
    }
```

#### 加固步骤：

1. 生成开启`protectedApp = true`的基础包(这里假设此APK名为：`protected.apk`);
2. 上传`protected.apk`到相应的加固网站进行加固，并发布应用市场(请遵循各个加固网站步骤，一般为下载加固包-》重新签名-》发布重签名加固包);
3. 在tinkerPatch后台根据appVersion建立相应的App版本(比如这里2个flavor，就需要建立2个App版本。App版本即为各自flavor中配置的appVersion);
4. 基于各个flavor的基础包（**这里的基础包是第一步中生成的未加固的版本**）生成相应patch包，并上传至相应的App版本中，即完成补丁发布。

**protectedApp=true, 这种模式仅仅可以使用在加固应用中**

#### 支持列表:

| 加固厂商 | 测试      |
| --------| --------- |
| 乐加固   | Tested  |
| 爱加密   | Tested |
| 梆梆加固 | Tested  |
| 360加固 | TODO（对特定场景的Android N支持还存在问题）|
| 其他    | 请自行测试，只要满足下面规则的都可以支持 |

这里是否支持加固，需要加固厂商明确以下两点：

1. 不能提前导入类；
2. 在art平台若要编译oat文件，需要将内联取消。
