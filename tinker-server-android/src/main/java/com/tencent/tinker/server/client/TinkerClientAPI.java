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

package com.tencent.tinker.server.client;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.server.model.DataFetcher;
import com.tencent.tinker.server.model.TinkerClientUrl;
import com.tencent.tinker.server.model.request.FailReport;
import com.tencent.tinker.server.model.request.SuccessReport;
import com.tencent.tinker.server.model.response.SyncResponse;
import com.tencent.tinker.server.urlconnection.UrlConnectionUrlLoader;
import com.tencent.tinker.server.utils.Conditions;
import com.tencent.tinker.server.utils.Preconditions;
import com.tencent.tinker.server.utils.ServerUtils;
import com.tencent.tinker.server.utils.VersionUtils;

import java.io.File;
import java.io.InputStream;


public class TinkerClientAPI {
    public static final String TAG = "Tinker.ClientImpl";

    private static final String REPORT_SUCCESS_URL = "http://stat.tinkerpatch.com/succ.php";
    private static final String REPORT_FAIL_URL    = "http://stat.tinkerpatch.com/err.php";


    private static volatile TinkerClientAPI clientAPI;
    final Conditions   conditions;
    final VersionUtils versionUtils;
    private final String                 appVersion;
    private final String                 appKey;
    private final String                 host;
    private final boolean                debug;
    private final UrlConnectionUrlLoader loader;

    TinkerClientAPI(
        String appKey, String appVersion, String host,
        Boolean debug, Conditions conditions, UrlConnectionUrlLoader loader, VersionUtils versionUtils) {
        this.appVersion = appVersion;
        this.appKey = appKey;
        this.host = host;
        this.debug = debug;
        this.conditions = conditions;
        this.loader = loader;
        this.versionUtils = versionUtils;
    }

    /**
     * Singleton get method for Tinker clientAPI, you need invoke
     * {@link #init(Context, String, String, Boolean)} before it invoke.
     *
     * @return the instance of {@link TinkerClientAPI}
     */
    public static TinkerClientAPI get() {
        if (clientAPI == null) {
            throw new RuntimeException("Please invoke init Tinker Client first");
        }
        return clientAPI;
    }

    /**
     * init the Tinker Client, it only effect at first time.
     * you should only invoke once in your app lifecycle
     *
     * @param context    {@link Context} context
     * @param appKey     your appKey get from <a href=tinkerpatch.com>tinkerpatch.com<a/>
     * @param appVersion your app version, this is "App版本号" in tinkerpatch.com
     * @param debug      use debug config, which is "开发预览" in tinkerpatch.com
     * @return {@link #TinkerClientAPI} tinker patch clientAPI
     */
    public static TinkerClientAPI init(Context context, String appKey, String appVersion, Boolean debug) {
        if (clientAPI == null) {
            synchronized (TinkerClientAPI.class) {
                if (clientAPI == null) {
                    clientAPI = new Builder()
                        .appKey(appKey)
                        .appVersion(appVersion)
                        .debug(debug)
                        .conditions(context)
                        .versionUtils(new VersionUtils(context, appVersion))
                        .build();
                }
            }
        }
        return clientAPI;
    }

    public Conditions params(String key, String value) {
        return this.conditions.set(key, value);
    }

    public void updateTinkerVersion(Integer newVersion, String md5) {
        versionUtils.updateVersionProperty(
            getAppVersion(), newVersion, md5, versionUtils.grayValue(), versionUtils.id()
        );
    }

    public void update(final Context context, final PatchRequestCallback callback) {
        if (callback == null) {
            throw new RuntimeException("callback can't be null");
        }
        if (!callback.beforePatchRequest()) {
            return;
        }
        sync(new DataFetcher.DataCallback<String>() {
            @Override
            public void onDataReady(String data) {
                final SyncResponse response = SyncResponse.fromJson(data);
                if (response == null) {
                    callback.onPatchSyncFail(new RuntimeException("Can't sync with version: response == null"));
                } else {
                    TinkerLog.i(TAG, "sync response in update:" + response);

                    if (response.isRollback) {
                        callback.onPatchRollback();
                        return;
                    }
                    if (response.isPaused) {
                        TinkerLog.i(TAG, "Needn't update, sync response is: " + response.toString()
                            + "\ngray: " + versionUtils.grayValue());
                        return;
                    }
                    if (!TextUtils.isEmpty(response.conditions)) {
                        callback.updatePatchConditions();
                    }
                    final Integer newVersion = Integer.parseInt(response.version);
                    if (versionUtils.isUpdate(newVersion, getAppVersion())
                        && versionUtils.isInGrayGroup(response.grayValue)
                        && conditions.check(response.conditions)) {

                        DataFetcher.DataCallback<File> downloadCallback = new DataFetcher.DataCallback<File>() {
                            @Override
                            public void onDataReady(File data) {
                                callback.onPatchUpgrade(data, newVersion, getCurrentPatchVersion());
                            }

                            @Override
                            public void onLoadFailed(Exception e) {
                                callback.onPatchDownloadFail(e, newVersion, getCurrentPatchVersion());
                            }
                        };
                        String patchPath = ServerUtils.getServerFile(
                            context, getAppVersion(), response.version
                        ).getAbsolutePath();
                        download(response.version, patchPath, downloadCallback);
                    } else {
                        TinkerLog.i(TAG, "Needn't update, sync response is: " + response.toString()
                            + "\ngray: " + versionUtils.grayValue());
                    }
                }
            }

            @Override
            public void onLoadFailed(Exception e) {
                callback.onPatchSyncFail(e);
            }
        });
    }

    /**
     * sync http://{Host}/{appKey}/{appVersion}?d={deviceId}&v={timestamp}
     *
     * @param callback the request callback
     */
    public void sync(final DataFetcher.DataCallback<String> callback) {
        Uri.Builder urlBuilder = Uri.parse(this.host).buildUpon();
        if (clientAPI.debug) {
            urlBuilder.appendPath("dev");
        }
        final String url = urlBuilder.appendPath(this.appKey)
            .appendPath(this.appVersion)
            .appendQueryParameter("d", versionUtils.id())
            .appendQueryParameter("v", String.valueOf(System.currentTimeMillis()))
            .build().toString();

        TinkerClientUrl tkClientUrl = new TinkerClientUrl.Builder().url(url).build();

        final DataFetcher<InputStream> dataFetcher = loader.buildLoadData(tkClientUrl);
        dataFetcher.loadData(new DataFetcher.DataCallback<InputStream>() {
            @Override
            public void onDataReady(InputStream data) {
                if (callback == null) {
                    return;
                }
                try {
                    String response = ServerUtils.readStreamToString(data, ServerUtils.CHARSET);
                    TinkerLog.e(TAG, "sync respond111:" + response);

                    SyncResponse.fromJson(response);
                    callback.onDataReady(response);
                } catch (Exception e) {
                    callback.onLoadFailed(e);
                } finally {
                    dataFetcher.cleanup();
                }
            }

            @Override
            public void onLoadFailed(Exception e) {
                if (callback == null) {
                    return;
                }
                try {
                    callback.onLoadFailed(e);
                } finally {
                    dataFetcher.cleanup();
                }
            }
        });
    }

    /**
     * download
     * http://{Host}/{appKey}/{appVersion}/file{patchVersion}?d={deviceId}&v={timestamp}
     *
     * @param patchVersion patchVersion
     * @param filePath the target patch file path
     * @param callback the request callback
     */
    public void download(final String patchVersion,
                  final String filePath,
                  final DataFetcher.DataCallback<? super File> callback) {

        Preconditions.checkNotEmpty(patchVersion);
        final String url = Uri.parse(this.host)
            .buildUpon()
            .appendPath(this.appKey)
            .appendPath(this.appVersion)
            .appendPath(String.format("file%s", patchVersion))
            .appendQueryParameter("d", versionUtils.id())
            .appendQueryParameter("v", String.valueOf(System.currentTimeMillis()))
            .build().toString();

        TinkerClientUrl tkClientUrl = new TinkerClientUrl.Builder().url(url).build();

        final DataFetcher<InputStream> dataFetcher = loader.buildLoadData(tkClientUrl);
        dataFetcher.loadData(new DataFetcher.DataCallback<InputStream>() {
            @Override
            public void onDataReady(InputStream data) {
                if (callback == null) {
                    return;
                }
                try {
                    callback.onDataReady(ServerUtils.readStreamToFile(data, filePath));
                } catch (Exception e) {
                    callback.onLoadFailed(e);
                } finally {
                    dataFetcher.cleanup();
                }
            }

            @Override
            public void onLoadFailed(Exception e) {
                if (callback == null) {
                    return;
                }

                try {
                    callback.onLoadFailed(e);
                } finally {
                    dataFetcher.cleanup();
                }
            }
        });
    }

    /**
     * report success http://stat.tinkerpatch.com/succ.php
     * k:  appKey
     * av: appVersion，当前app版本号
     * pv: patchVersion，应用的补丁版本号
     * t:  平台类型，填数字1
     *
     * @param patchVersion patchVersion
     */
    public void reportSuccess(Integer patchVersion) {
        Uri.Builder urlBuilder = Uri.parse(REPORT_SUCCESS_URL).buildUpon();
        final String url = urlBuilder.build().toString();
        SuccessReport report = new SuccessReport(this.appKey, this.appVersion, String.valueOf(patchVersion));
        TinkerClientUrl tkClientUrl = new TinkerClientUrl.Builder()
            .url(url)
            .body(report.toEncodeForm())
            .method("POST").build();

        final DataFetcher<InputStream> dataFetcher = loader.buildLoadData(tkClientUrl);
        dataFetcher.loadData(new DataFetcher.DataCallback<InputStream>() {
            @Override
            public void onDataReady(InputStream data) {
                TinkerLog.d(TAG, "reportSuccess successfully:"
                    + ServerUtils.readStreamToString(data, ServerUtils.CHARSET));
            }

            @Override
            public void onLoadFailed(Exception e) {
                TinkerLog.e(TAG, "reportSuccess error", e);
            }
        });
    }

    /**
     * report fail http://stat.tinkerpatch.com/err.php
     * k:  appKey
     * av: appVersion，当前app版本号
     * pv: patchVersion，应用的补丁版本号
     * t:  平台类型，填数字1
     * code: 错误码
     *
     * @param patchVersion patchVersion
     * @param errCode      errCode
     */
    public void reportFail(Integer patchVersion, Integer errCode) {
        Uri.Builder urlBuilder = Uri.parse(REPORT_FAIL_URL).buildUpon();
        final String url = urlBuilder.build().toString();
        FailReport report = new FailReport(this.appKey, this.appVersion, String.valueOf(patchVersion), errCode);
        TinkerClientUrl tkClientUrl = new TinkerClientUrl.Builder()
            .url(url)
            .body(report.toEncodeForm())
            .method("POST").build();

        final DataFetcher<InputStream> dataFetcher = loader.buildLoadData(tkClientUrl);
        dataFetcher.loadData(new DataFetcher.DataCallback<InputStream>() {
            @Override
            public void onDataReady(InputStream data) {
                TinkerLog.d(TAG, "reportFail successfully:"
                    + ServerUtils.readStreamToString(data, ServerUtils.CHARSET));
            }

            @Override
            public void onLoadFailed(Exception e) {
                TinkerLog.e(TAG, "reportSuccess error", e);
            }
        });
    }

    /**
     * 请求动态配置文件。
     * 请求http://{Host}/c/{appKey}?d={deviceId}&v={timestamp}
     */
    public void getDynamicConfig(final DataFetcher.DataCallback<String> callback) {
        final String url = Uri.parse(this.host)
            .buildUpon()
            .appendPath("c")
            .appendPath(this.appKey)
            .appendQueryParameter("d", versionUtils.id())
            .appendQueryParameter("v", String.valueOf(System.currentTimeMillis()))
            .build().toString();

        TinkerClientUrl tkClientUrl = new TinkerClientUrl.Builder().url(url).build();

        final DataFetcher<InputStream> dataFetcher = loader.buildLoadData(tkClientUrl);
        dataFetcher.loadData(new DataFetcher.DataCallback<InputStream>() {
            @Override
            public void onDataReady(InputStream data) {
                if (callback == null) {
                    TinkerLog.e(TAG, "[succ] getDynamicConfig's callback is null!");
                    return;
                }
                try {
                    String response = ServerUtils.readStreamToString(data, ServerUtils.CHARSET);
                    callback.onDataReady(response);
                } catch (Exception e) {
                    callback.onLoadFailed(e);
                } finally {
                    dataFetcher.cleanup();
                }
            }

            @Override
            public void onLoadFailed(Exception e) {
                if (callback == null) {
                    TinkerLog.e(TAG, "[fail] getDynamicConfig's callback is null!");
                    return;
                }
                try {
                    callback.onLoadFailed(e);
                } finally {
                    dataFetcher.cleanup();
                }
            }
        });
    }

    /**
     * Get current patch version
     *
     * @return patch version
     */
    public Integer getCurrentPatchVersion() {
        return versionUtils.getPatchVersion();
    }

    public String getCurrentPatchMd5() {
        return versionUtils.getPatchMd5();
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getHost() {
        return host;
    }

    public boolean isDebug() {
        return debug;
    }

    public Conditions getConditions() {
        return conditions;
    }


    static class Builder {
        private static final String HOST_URL = "http://q.tinkerpatch.com";
        private String                 appVersion;
        private String                 appKey;
        private String                 host;
        private Boolean                debug;
        private Conditions             conditions;
        private UrlConnectionUrlLoader loader;
        private VersionUtils           versionUtils;

        TinkerClientAPI.Builder host(String host) {
            this.host = host;
            return this;
        }

        TinkerClientAPI.Builder appKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        TinkerClientAPI.Builder appVersion(String appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        TinkerClientAPI.Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        TinkerClientAPI.Builder conditions(Context context) {
            this.conditions = new Conditions();
            return this;
        }

        TinkerClientAPI.Builder urlLoader(UrlConnectionUrlLoader loader) {
            this.loader = loader;
            return this;
        }

        TinkerClientAPI.Builder versionUtils(VersionUtils versionUtils) {
            this.versionUtils = versionUtils;
            return this;
        }

        void makeDefault() {
            if (TextUtils.isEmpty(host)) {
                this.host = HOST_URL;
            }
            if (this.loader == null) {
                this.loader = new UrlConnectionUrlLoader();
            }
            if (TextUtils.isEmpty(this.appKey) || TextUtils.isEmpty(this.appVersion)) {
                throw new RuntimeException("You need setup Appkey and AppVersion");
            }
            if (this.conditions == null) {
                throw new RuntimeException("You need init conditions property");
            }
        }

        public TinkerClientAPI build() {
            makeDefault();
            return new TinkerClientAPI(
                this.appKey, this.appVersion, this.host, this.debug,
                this.conditions, this.loader, this.versionUtils
            );
        }
    }
}
