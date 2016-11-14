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

package tinker.sample.android.patchserver;

import android.content.Context;
import android.content.SharedPreferences;

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.lib.tinker.TinkerLoadResult;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;
import com.tencent.tinker.server.TinkerServerClient;
import com.tencent.tinker.server.client.DefaultPatchRequestCallback;
import com.tencent.tinker.server.utils.ServerUtils;

import java.io.File;

import tinker.sample.android.util.Utils;


public class SamplePatchRequestCallback extends DefaultPatchRequestCallback {
    private static final String TAG = "Tinker.SampleRequestCallback";

    public static final String TINKER_RETRY_PATCH     = "tinker_retry_patch";
    public static final int    TINKER_MAX_RETRY_COUNT = 3;

    @Override
    public boolean beforePatchRequest() {
        boolean result = super.beforePatchRequest();
        if (result) {
            TinkerServerClient client = TinkerServerClient.get();
            Tinker tinker = client.getTinker();
            Context context = client.getContext();

            if (!tinker.isMainProcess()) {
                TinkerLog.e(TAG, "beforePatchRequest, only request on the main process");
                return false;
            }
            if (Utils.isGooglePlay()) {
                TinkerLog.e(TAG, "beforePatchRequest, google play channel, return false");
                return false;
            }
            // main process must be the newly version
            // check whether it is pending work
            String currentPatchMd5 = client.getCurrentPatchMd5();
            TinkerLoadResult tinkerLoadResult = tinker.getTinkerLoadResultIfPresent();

            if (tinkerLoadResult.currentVersion == null || !currentPatchMd5.equals(tinkerLoadResult.currentVersion)) {
                Integer version = client.getCurrentPatchVersion();
                if (version > 0) {
                    File patchFile = ServerUtils.getServerFile(context, client.getAppVersion(), String.valueOf(version));
                    if (patchFile.exists() && patchFile.isFile()) {

                        SharedPreferences sp = context.getSharedPreferences(
                            TinkerServerClient.SHARE_SERVER_PREFERENCE_CONFIG, Context.MODE_PRIVATE
                        );
                        int current = sp.getInt(TINKER_RETRY_PATCH, 0);
                        if (current >= TINKER_MAX_RETRY_COUNT) {
                            SharePatchFileUtil.safeDeleteFile(patchFile);
                            sp.edit().putInt(TINKER_RETRY_PATCH, 0).commit();
                            TinkerLog.w(TAG, "beforePatchRequest, retry patch install have more than %d count, " +
                                "version: %d, patch:%s", current, version, patchFile.getPath());
                        } else {
                            TinkerLog.w(TAG, "beforePatchRequest, have pending patch to install, " +
                                "version: %d, patch:%s", version, patchFile.getPath());

                            sp.edit().putInt(TINKER_RETRY_PATCH, ++current).commit();
                            TinkerInstaller.onReceiveUpgradePatch(context, patchFile.getAbsolutePath());
                            return false;

                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void onPatchRollback() {
        TinkerLog.w(TAG, "onPatchRollback");
        TinkerServerClient client = TinkerServerClient.get();

        if (Utils.isBackground()) {
            TinkerLog.i(TAG, "onPatchRollback, it is in background, just clean patch and kill all process");
            rollbackPatchDirectly();
        } else {
            //we can wait process at background, such as onAppBackground
            //or we can restart when the screen off
            TinkerLog.i(TAG, "tinker wait screen to clean patch and kill all process");
            new Utils.ScreenState(client.getContext(), new Utils.IOnScreenOff() {
                @Override
                public void onScreenOff() {
                    rollbackPatchDirectly();
                }
            });
        }
    }

    @Override
    public void onPatchDownloadFail(Exception e, Integer newVersion, Integer currentVersion) {
        super.onPatchDownloadFail(e, newVersion, currentVersion);
    }

    @Override
    public void onPatchSyncFail(Exception e) {
        super.onPatchSyncFail(e);
    }

    @Override
    public boolean onPatchUpgrade(File file, Integer newVersion, Integer currentVersion) {
        boolean result = super.onPatchUpgrade(file, newVersion, currentVersion);
        if (result) {
            TinkerServerClient client = TinkerServerClient.get();
            Context context = client.getContext();
            SharedPreferences sp = context.getSharedPreferences(
                TinkerServerClient.SHARE_SERVER_PREFERENCE_CONFIG, Context.MODE_PRIVATE
            );
            sp.edit().putInt(TINKER_RETRY_PATCH, 0).commit();
        }
        return result;
    }
}
