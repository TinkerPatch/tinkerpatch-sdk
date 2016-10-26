package com.tencent.tinker.server;

import android.content.Context;

import com.tencent.tinker.server.model.DataFetcher;

import java.io.File;

/**
 * Created by sun on 27/09/2016.
 */

public interface TinkerClientAPI {

    String REPORT_SUCCESS_URL = "http://stat.tinkerpatch.com/succ.php";
    String REPORT_FAIL_URL = "http://stat.tinkerpatch.com/err.php";
    /**
     * sync http://{Host}/{appKey}/{appVersion}?d={deviceId}&v={timestamp}
     */
    void sync(Context context, DataFetcher.DataCallback<String> callback);

    /**
     * download
     * http://{Host}/{appKey}/{appVersion}/file{patchVersion}?d={deviceId}&v={timestamp}
     */
    void download(
        Context context,
        String patchVersion,
        String filePath,
        DataFetcher.DataCallback<? super File> callback
    );

    /**
     * report success http://stat.tinkerpatch.com/succ.php
     * k:  appKey
     * av: appVersion，当前app版本号
     * pv: patchVersion，应用的补丁版本号
     * t:  平台类型，填数字1
     */
    void reportSuccess(Context context, String patchVersion);

    /**
     * report fail http://stat.tinkerpatch.com/err.php
     * k:  appKey
     * av: appVersion，当前app版本号
     * pv: patchVersion，应用的补丁版本号
     * t:  平台类型，填数字1
     * code: 错误码
     */
    void reportFail(Context context, String patchVersion, Integer errCode);
}
