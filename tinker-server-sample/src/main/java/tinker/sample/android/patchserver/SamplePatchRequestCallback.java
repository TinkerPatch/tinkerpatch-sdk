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

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.lib.tinker.TinkerLoadResult;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.server.TinkerServerClient;
import com.tencent.tinker.server.client.DefaultPatchRequestCallback;
import com.tencent.tinker.server.utils.ServerUtils;

import java.io.File;

import tinker.sample.android.util.Utils;


public class SamplePatchRequestCallback extends DefaultPatchRequestCallback {
    private static final String TAG = "Tinker.SampleRequestCallback";

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
                        TinkerLog.e(TAG, "beforePatchRequest, have pending patch to install, " +
                            "version: %d, patch:%s", version, patchFile.getPath());

                        TinkerInstaller.onReceiveUpgradePatch(context, patchFile.getAbsolutePath());
                        return false;
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void onPatchRollback() {
        TinkerLog.e(TAG, "onPatchRollback");
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
}
