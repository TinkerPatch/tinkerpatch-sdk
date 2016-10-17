package com.xmonster.tkclient;

import android.content.Context;

import com.xmonster.tkclient.model.DataFetcher;

import java.io.File;

/**
 * Created by sun on 27/09/2016.
 */

public interface TKClientAPI {

    /**
     * 每次启动或唤醒调请求 http://{Host}/{appKey}/{appVersion}?d={deviceId}&v={timestamp}
     */
    void sync(Context context, DataFetcher.DataCallback<String> callback);

    /**
     * 若本地未下载过这个补丁版本，则请求
     * http://{Host}/{appKey}/{appVersion}/file{patchVersion}?d={deviceId}&v={timestamp}
     */
    void download(
        Context context,
        String patchVersion,
        String filePath,
        DataFetcher.DataCallback<? super File> callback
    );
}
