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

package com.tencent.tinker.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.TinkerRuntimeException;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;
import com.tencent.tinker.server.client.ConfigRequestCallback;
import com.tencent.tinker.server.client.DefaultPatchRequestCallback;
import com.tencent.tinker.server.client.PatchRequestCallback;
import com.tencent.tinker.server.client.TinkerClientAPI;
import com.tencent.tinker.server.model.DataFetcher;
import com.tencent.tinker.server.utils.NetStatusUtil;

public class TinkerServerClient {
    public static final  String SHARE_SERVER_PREFERENCE_CONFIG = "tinker_server_config";
    public static final  String TINKER_LAST_CHECK              = "tinker_last_check";
    public static final  String CONDITION_WIFI                 = "wifi";
    public static final  String CONDITION_SDK                  = "sdk";
    public static final  String CONDITION_BRAND                = "brand";
    public static final  String CONDITION_MODEL                = "model";
    public static final  String CONDITION_CPU_ABI              = "cpu";
    public static final  long   DEFAULT_CHECK_INTERVAL         = 1 * 3600 * 1000;
    public static final  long   NEVER_CHECK_UPDATE             = -1;
    private static final String TAG                            = "Tinker.ServerClient";

    private static volatile TinkerServerClient   client;
    private final           Tinker               tinker;
    private final           Context              context;
    private final           PatchRequestCallback patchRequestCallback;
    private final           TinkerClientAPI      clientAPI;
    private long checkInterval = DEFAULT_CHECK_INTERVAL;


    public TinkerServerClient(Context context, Tinker tinker, String appKey,
                              String appVersion, Boolean debug, PatchRequestCallback patchRequestCallback) {
        this.tinker = tinker;
        this.context = context;
        this.clientAPI = TinkerClientAPI.init(context, appKey, appVersion, debug);
        this.patchRequestCallback = patchRequestCallback;
        makeDefaultConditions();
    }

    public static TinkerServerClient get() {
        if (client == null) {
            throw new RuntimeException("Please invoke init Tinker Client first");
        }
        return client;
    }

    public static TinkerServerClient init(Context context, Tinker tinker,
                                          String appKey, String appVersion, Boolean debug) {
        if (client == null) {
            synchronized (TinkerClientAPI.class) {
                if (client == null) {
                    client = new TinkerServerClient(context, tinker, appKey,
                        appVersion, debug, new DefaultPatchRequestCallback());
                }
            }
        }
        return client;
    }

    public static TinkerServerClient init(Context context, Tinker tinker, String appKey,
                                          String appVersion, Boolean debug, PatchRequestCallback patchRequestCallback) {
        if (client == null) {
            synchronized (TinkerClientAPI.class) {
                if (client == null) {
                    client = new TinkerServerClient(context, tinker, appKey, appVersion, debug, patchRequestCallback);
                }
            }
        }
        return client;
    }

    public void updateTinkerCondition(String key, String value) {
        this.clientAPI.params(key, value);
    }

    private void makeDefaultConditions() {
        this.clientAPI.params(CONDITION_WIFI, NetStatusUtil.isWifi(context) ? "1" : "0");
        this.clientAPI.params(CONDITION_SDK, String.valueOf(Build.VERSION.SDK_INT));
        this.clientAPI.params(CONDITION_BRAND, Build.BRAND);
        this.clientAPI.params(CONDITION_MODEL, Build.MODEL);
        this.clientAPI.params(CONDITION_CPU_ABI, Build.CPU_ABI);
    }

    public void checkTinkerUpdate() {
        if (!tinker.isTinkerEnabled() || !ShareTinkerInternals.isTinkerEnableWithSharedPreferences(context)) {
            TinkerLog.e(TAG, "tinker is disable, just return");
            return;
        }

        SharedPreferences sp = context.getSharedPreferences(
            SHARE_SERVER_PREFERENCE_CONFIG, Context.MODE_PRIVATE
        );
        long last = sp.getLong(TINKER_LAST_CHECK, 0);
        if (last == NEVER_CHECK_UPDATE) {
            TinkerLog.i(TAG, "tinker update is disabled, with never check flag!");
            return;
        }
        TinkerClientAPI tinkerClientAPI = TinkerClientAPI.get();
        long interval = System.currentTimeMillis() - last;
        if (tinkerClientAPI.isDebug() || interval >= checkInterval) {
            sp.edit().putLong(TINKER_LAST_CHECK, System.currentTimeMillis()).commit();
            tinkerClientAPI.update(context, patchRequestCallback);
        } else {
            TinkerLog.i(TAG, "tinker sync should wait interval %ss", (checkInterval - interval) / 1000);
        }
    }

    public void checkTinkerUpdateImmediately() {
        if (!tinker.isTinkerEnabled() || !ShareTinkerInternals.isTinkerEnableWithSharedPreferences(context)) {
            TinkerLog.e(TAG, "tinker is disable, just return");
            return;
        }

        SharedPreferences sp = context.getSharedPreferences(
            SHARE_SERVER_PREFERENCE_CONFIG, Context.MODE_PRIVATE
        );
        long last = sp.getLong(TINKER_LAST_CHECK, 0);
        if (last == NEVER_CHECK_UPDATE) {
            TinkerLog.i(TAG, "tinker update is disabled, with never check flag!");
            return;
        }
        TinkerClientAPI tinkerClientAPI = TinkerClientAPI.get();
        sp.edit().putLong(TINKER_LAST_CHECK, System.currentTimeMillis()).commit();
        tinkerClientAPI.update(context, patchRequestCallback);
    }

    /**
     * @param hours between 0 and 24
     */
    public void setCheckIntervalByHours(int hours) {
        if (hours < 0 || hours > 24) {
            throw new TinkerRuntimeException("hours must be between 0 and 24");
        }
        checkInterval = (long) hours * 3600 * 1000;
    }

    public void disableTinkerUpdate() {
        SharedPreferences sp = context.getSharedPreferences(
            SHARE_SERVER_PREFERENCE_CONFIG, Context.MODE_PRIVATE
        );
        sp.edit().putLong(TINKER_LAST_CHECK, NEVER_CHECK_UPDATE).commit();
    }


    public Tinker getTinker() {
        return tinker;
    }

    public Context getContext() {
        return context;
    }

    public String getAppKey() {
        return clientAPI.getAppKey();
    }

    public String getAppVersion() {
        return clientAPI.getAppVersion();
    }

    public Integer getCurrentPatchVersion() {
        return clientAPI.getCurrentPatchVersion();
    }

    public String getCurrentPatchMd5() {
        return clientAPI.getCurrentPatchMd5();
    }

    public boolean isDebug() {
        return clientAPI.isDebug();
    }

    public void reportPatchSuccess(Integer patchVersion) {
        TinkerLog.i(TAG, "tinker server report patch success, patchVersion:%d", patchVersion);
        clientAPI.reportSuccess(patchVersion);
    }

    public void reportPatchFail(Integer patchVersion, int errorCode) {
        TinkerLog.i(TAG, "tinker server report patch fail, patchVersion:%d, errorCode:%d", patchVersion, errorCode);
        clientAPI.reportFail(patchVersion, errorCode);
    }

    public void updateTinkerVersion(Integer newVersion, String patchMd5) {
        clientAPI.updateTinkerVersion(newVersion, patchMd5);
    }

    public void getDynamicConfig(final ConfigRequestCallback callback) {
        clientAPI.getDynamicConfig(new DataFetcher.DataCallback<String>() {
            @Override
            public void onDataReady(String data) {
                if (callback != null) {
                    callback.onSuccess(data);
                }
            }

            @Override
            public void onLoadFailed(Exception e) {
                if (callback != null) {
                    callback.onFail(e);
                }
            }
        });
    }
}
