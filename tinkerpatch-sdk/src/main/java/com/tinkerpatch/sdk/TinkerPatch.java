/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2016 Shengjie Sim Sun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.tinkerpatch.sdk;


import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.app.ApplicationLike;
import com.tinkerpatch.sdk.server.callback.ConfigRequestCallback;
import com.tinkerpatch.sdk.server.callback.RollbackCallBack;
import com.tinkerpatch.sdk.tinker.callback.ResultCallBack;

public abstract class TinkerPatch {

    /**
     * 设置Tinker相关Log的真正实现,用于自定义日志输出
     * @param imp
     */
    public static void setLogIml(TinkerLog.TinkerLogImp imp) {
        // nothing
    }

    /**
     * 用默认的构造参数初始化TinkerPatch的SDK
     * @param applicationLike
     * @return
     */
    public static TinkerPatch init(ApplicationLike applicationLike) {
        return null;
    }

    /**
     * 自定义参数初始化TinkerPatch的SDK
     * @param tinkerPatch
     * @return
     */
    public static TinkerPatch init(TinkerPatch tinkerPatch) {
        return null;
    }

    /**
     * 获得TinkerPatch的实例
     * @return
     */
    public static TinkerPatch with() {
        return null;
    }

    /**
     * 获得ApplicationLike的实例
     * @return
     */
    public abstract ApplicationLike getApplcationLike();

    /**
     * 反射补丁的Library path, 自动加载library
     * 是否自动反射Library路径,无须手动加载补丁中的So文件
     * 注意,调用在反射接口之后才能生效,你也可以使用Tinker的方式加载Library
     * @return
     */
    public TinkerPatch reflectPatchLibrary() {
        return null;
    }

    /**
     * 向后台获得动态配置,默认的访问间隔为3个小时
     * 若参数为true,即每次调用都会真正的访问后台配置
     *
     * @param configRequestCallback
     * @param immediately           是否立刻请求,忽略时间间隔限制
     */
    public TinkerPatch fetchDynamicConfig(final ConfigRequestCallback configRequestCallback,
                                          final boolean immediately) {
        return null;
    }

    /**
     * 获得当前的补丁版本,
     * 在TinkerPatch sdk 1.1.4 版本添加
     *
     * @return 当前补丁版本号。（此版本号由后台管理，且单调递增）
     */
    public Integer getPatchVersion() {
        return 0;
    }

    /**
     * 向后台获取是否有补丁包更新,默认的访问间隔为3个小时
     * 若参数为true,即每次调用都会真正的访问后台配置
     *
     * @param immediately 是否立刻检查,忽略时间间隔限制
     */
    public TinkerPatch fetchPatchUpdate(final boolean immediately) {
        return null;
    }

    /**
     * 设置当前渠道号,对于某些渠道我们可能会想屏蔽补丁功能
     * 设置渠道后,我们就可以使用后台的条件控制渠道更新
     *
     * @param channel
     * @return
     */
    public TinkerPatch setAppChannel(String channel) {
        return null;
    }

    /**
     * 屏蔽部分渠道的补丁功能
     *
     * @param channel
     * @return
     */
    public TinkerPatch addIgnoreAppChannel(String channel) {
        return null;
    }

    /**
     * 设置tinkerpatch平台的条件下发参数
     * 默认内置的条件有[wifi, sdk, brand, model, cpu, cpu]
     * 若调用了setAppChannel, 能增加[channel]条件
     *
     * @param key
     * @param value
     */
    public TinkerPatch setPatchCondition(String key, String value) {
        return null;
    }

    /**
     * 设置访问后台动态配置的时间间隔,默认为3个小时
     * @param hours
     * @return
     */
    public TinkerPatch setFetchDynamicConfigIntervalByHours(int hours) {
        return null;
    }

    /**
     * 设置访问后台补丁包更新配置的时间间隔,默认为3个小时
     *
     * @param hours
     * @return
     */
    public TinkerPatch setFetchPatchIntervalByHours(int hours) {
        return null;
    }

    /**
     * 设置补丁合成成功后,是否通过锁屏重启程序,这样可以加快补丁的生效时间
     * 默认为false, 即等待应用自身重新启动时加载
     *
     * @param restartOnScreenOff
     * @return
     */
    public TinkerPatch setPatchRestartOnSrceenOff(boolean restartOnScreenOff) {
        return null;
    }

    /**
     * 我们可以通过ResultCallBack设置对合成后的回调
     * 例如我们也可以不锁屏,而是在这里通过弹框咨询用户等方式
     *
     * @param resultCallBack
     * @return
     */
    public TinkerPatch setPatchResultCallback(ResultCallBack resultCallBack) {
        return null;
    }

    /**
     * 设置收到后台回退要求时,是否在锁屏时清除补丁
     * 默认为false,即等待应用下一次重新启动时才会去清除补丁
     *
     * @param rollbackOnScreenOff
     * @return
     */
    public TinkerPatch setPatchRollbackOnScreenOff(boolean rollbackOnScreenOff) {
        return null;
    }

    /**
     * 我们可以通过RollbackCallBack设置对回退时的回调
     *
     * @param rollbackCallBack
     * @return
     */
    public TinkerPatch setPatchRollBackCallback(RollbackCallBack rollbackCallBack) {
        return null;
    }
    /**
     * 清除补丁
     * @return
     */
    public TinkerPatch cleanPatch() {
        return null;
    }
}
