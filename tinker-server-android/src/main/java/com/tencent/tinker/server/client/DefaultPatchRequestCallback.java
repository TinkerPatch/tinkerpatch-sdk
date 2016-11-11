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
import android.content.SharedPreferences;

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.lib.util.TinkerServiceInternals;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;
import com.tencent.tinker.loader.shareutil.ShareSecurityCheck;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;
import com.tencent.tinker.server.TinkerServerClient;
import com.tencent.tinker.server.utils.NetStatusUtil;
import com.tencent.tinker.server.utils.ServerUtils;

import java.io.File;


public class DefaultPatchRequestCallback implements PatchRequestCallback {
    public static final String TINKER_DOWNLOAD_FAIL_TIMES     = "tinker_download_fail";
    public static final int    TINKER_DOWNLOAD_FAIL_MAX_TIMES = 3;
    /**
     * 下载补丁时异常
     */
    public static final int ERROR_DOWNLOAD_FAIL       = -1;
    /**
     * 检测下载补丁的签名时异常
     */
    public static final int ERROR_DOWNLOAD_CHECK_FAIL = -2;
    /**
     * 补丁在patchListener检测时异常
     */
    public static final int ERROR_LISTENER_CHECK_FAIL = -3;
    /**
     * 补丁合成异常
     */
    public static final int ERROR_PATCH_FAIL          = -4;
    /**
     * 补丁加载异常
     */
    public static final int ERROR_LOAD_FAIL           = -5;
    private static final String TAG = "Tinker.RequestCallback";

    @Override
    public boolean beforePatchRequest() {
        TinkerServerClient client = TinkerServerClient.get();

        // check network
        if (!NetStatusUtil.isConnected(client.getContext())) {
            TinkerLog.e(TAG, "not connect to internet");
            return false;
        }
        if (TinkerServiceInternals.isTinkerPatchServiceRunning(client.getContext())) {
            TinkerLog.e(TAG, "tinker service is running");
            return false;
        }
        return true;
    }

    @Override
    public void onPatchUpgrade(File file, Integer newVersion, Integer currentVersion) {
        TinkerLog.e(TAG, "onPatchUpgrade, file:%s, newVersion:%d, currentVersion:%d",
            file.getPath(), newVersion, currentVersion);
        TinkerServerClient client = TinkerServerClient.get();
        Context context = client.getContext();

        ShareSecurityCheck securityCheck = new ShareSecurityCheck(context);
        if (!securityCheck.verifyPatchMetaSignature(file)) {
            TinkerLog.e(TAG, "onPatchUpgrade, signature check fail. file: %s, version:%d", file.getPath(), newVersion);
            //treat it as download fail
            if (increaseDownloadError(context)) {
                //update tinker version also, don't request again
                client.updateTinkerVersion(newVersion, SharePatchFileUtil.getMD5(file));
                client.reportPatchFail(newVersion, ERROR_DOWNLOAD_CHECK_FAIL);
            }
            SharePatchFileUtil.safeDeleteFile(file);
            return;
        }
        tryPatchFile(file, newVersion);
    }

    private void tryPatchFile(File patchFile, Integer newVersion) {
        TinkerServerClient client = TinkerServerClient.get();
        Context context = client.getContext();

        //In order to calculate the user number, just report success here
        client.reportPatchSuccess(newVersion);
        String patchMd5 = SharePatchFileUtil.getMD5(patchFile);
        //update version
        client.updateTinkerVersion(newVersion, patchMd5);
        //delete old patch sever file
        File[] files = ServerUtils.getServerDirectory(context).listFiles();
        if (files != null) {
            String currentName = patchFile.getName();
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.equals(currentName) || fileName.equals(ServerUtils.TINKER_VERSION_FILE)) {
                    continue;
                }
                SharePatchFileUtil.safeDeleteFile(file);
            }
        }
        //try install
        TinkerInstaller.onReceiveUpgradePatch(context, patchFile.getAbsolutePath());
    }

    @Override
    public void onPatchDownloadFail(Exception e, Integer newVersion, Integer currentVersion) {
        TinkerLog.e(TAG, "onPatchDownloadFail e:" + e);
        //check network
        TinkerServerClient client = TinkerServerClient.get();
        //due to network, just return
        if (!NetStatusUtil.isConnected(client.getContext())) {
            TinkerLog.e(TAG, "onPatchDownloadFail, not connect to internet just return");
            return;
        }
        Context context = client.getContext();
        if (increaseDownloadError(context)) {
            client.reportPatchFail(newVersion, ERROR_DOWNLOAD_FAIL);
        }

    }

    @Override
    public void onPatchSyncFail(Exception e) {
        TinkerLog.e(TAG, "onPatchSyncFail error:" + e);
        TinkerLog.printErrStackTrace(TAG, e, "onPatchSyncFail stack:");
    }

    @Override
    public void onPatchRollback() {
        TinkerLog.e(TAG, "onPatchRollback");
        rollbackPatchDirectly();
    }

    public void rollbackPatchDirectly() {
        TinkerServerClient client = TinkerServerClient.get();
        final Context context = client.getContext();
        final Tinker tinker = client.getTinker();
        //restart now
        tinker.cleanPatch();
        ShareTinkerInternals.killAllOtherProcess(context);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void updatePatchConditions() {
        TinkerLog.e(TAG, "updatePatchConditions");
        TinkerServerClient client = TinkerServerClient.get();
        // wifi condition should be updated
        client.updateTinkerCondition(TinkerServerClient.CONDITION_WIFI,
            NetStatusUtil.isWifi(client.getContext()) ? "1" : "0");
    }

    @Deprecated
    public boolean increaseDownloadError(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
            TinkerServerClient.SHARE_SERVER_PREFERENCE_CONFIG, Context.MODE_MULTI_PROCESS
        );
        int currentCount = sp.getInt(TINKER_DOWNLOAD_FAIL_TIMES, 0);
        TinkerLog.e(TAG, "increaseDownloadError, current count:%d", currentCount);

        if (currentCount >= TINKER_DOWNLOAD_FAIL_MAX_TIMES) {
            sp.edit().putInt(TINKER_DOWNLOAD_FAIL_TIMES, 0).commit();
            return true;
        } else {
            sp.edit().putInt(TINKER_DOWNLOAD_FAIL_TIMES, ++currentCount).commit();
        }
        return false;
    }
}
