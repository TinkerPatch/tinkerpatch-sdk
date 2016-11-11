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
import android.os.Looper;
import android.os.MessageQueue;

import com.tencent.tinker.lib.service.PatchResult;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;
import com.tencent.tinker.server.TinkerServerClient;
import com.tencent.tinker.server.client.DefaultPatchRequestCallback;
import com.tencent.tinker.server.utils.Debugger;
import com.tencent.tinker.server.utils.ServerUtils;

import java.io.File;

import tinker.sample.android.BuildConfig;
import tinker.sample.android.util.Utils;

/**
 * Created by zhangshaowen on 16/11/3.
 */

public class TinkerServerManager {
    private static final String TAG = "Tinker.ServerManager";

    public static final String CONDITION_CHANNEL = "channel";

    public static TinkerServerClient sTinkerServerClient;

    public static void installTinkerServer(Context context, Tinker tinker, int hours) {
        boolean debug = Debugger.getInstance(context).isDebug();
        TinkerLog.w(TAG, "installTinkerServer, debug value:" + debug);
        sTinkerServerClient = TinkerServerClient.init(context, tinker, BuildConfig.APP_KEY, BuildConfig.APP_VERSION,
            debug, new SamplePatchRequestCallback());
        // add channel condition
        sTinkerServerClient.updateTinkerCondition(CONDITION_CHANNEL, Utils.getChannel());
        sTinkerServerClient.setCheckIntervalByHours(hours);
    }

    public static void checkTinkerUpdate() {
        if (sTinkerServerClient == null) {
            TinkerLog.e(TAG, "checkTinkerUpdate, sTinkerServerClient == null");
            return;
        }
        Tinker tinker = sTinkerServerClient.getTinker();
        //only check at the main process
        if (tinker.isMainProcess()) {
            Looper.getMainLooper().myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
                @Override public boolean queueIdle() {
                    sTinkerServerClient.checkTinkerUpdate();
                    return false;
                }
            });
        }
    }

    public static void receiveTinkerCheckPush() {
        if (sTinkerServerClient == null) {
            TinkerLog.e(TAG, "receiveTinkerCheckPush, sTinkerServerClient == null");
            return;
        }
        Tinker tinker = sTinkerServerClient.getTinker();
        //only check at the main process
        if (tinker.isMainProcess()) {
            sTinkerServerClient.checkTinkerUpdateImmediately();
        }
    }

    public static void reportTinkerPatchFail(PatchResult patchResult) {
        if (sTinkerServerClient == null) {
            TinkerLog.e(TAG, "reportTinkerPatchFail, sTinkerServerClient == null");
            return;
        }
        if (patchResult == null) {
            TinkerLog.e(TAG, "reportTinkerPatchFail, patchResult == null");
            return;
        }
        File patchFile = new File(patchResult.rawPatchFilePath);
        if (patchFile.exists()) {
            File parentFile = patchFile.getParentFile();
            if (parentFile.getName().equals(ServerUtils.TINKER_SERVER_DIR)) {
                //delete server file whether it is success
                SharePatchFileUtil.safeDeleteFile(parentFile);
            }
        }

        SharePatchFileUtil.safeDeleteFile(new File(patchResult.rawPatchFilePath));

        if (patchResult.isSuccess) {
            TinkerLog.i(TAG, "reportTinkerPatchFail, patch success, just return");
            return;
        }
        String patchMd5 = (patchResult.patchVersion != null)
            ? patchResult.patchVersion : SharePatchFileUtil.getMD5(new File(patchResult.rawPatchFilePath));

        if (!patchMd5.equals(sTinkerServerClient.getCurrentPatchMd5())) {
            TinkerLog.e(TAG, "reportTinkerPatchFail, md5 not equal, " +
                "patchMd5:%s, currentPatchMd5:%s", patchMd5, sTinkerServerClient.getCurrentPatchMd5());
            return;
        }
        sTinkerServerClient.reportPatchFail(sTinkerServerClient.getCurrentPatchVersion(), DefaultPatchRequestCallback.ERROR_PATCH_FAIL);
    }

    public static void reportTinkerPatchListenerFail(String patchMd5) {
        if (sTinkerServerClient == null) {
            TinkerLog.e(TAG, "reportTinkerPatchListenerFail, sTinkerServerClient == null");
            return;
        }
        if (patchMd5 == null) {
            TinkerLog.e(TAG, "reportTinkerPatchListenerFail, patchMd5 == null");
            return;
        }
        if (!patchMd5.equals(sTinkerServerClient.getCurrentPatchMd5())) {
            TinkerLog.e(TAG, "reportTinkerPatchListenerFail, md5 not equal, " +
                "patchMd5:%s, currentPatchMd5:%s", patchMd5, sTinkerServerClient.getCurrentPatchMd5());
            return;
        }
        sTinkerServerClient.reportPatchFail(sTinkerServerClient.getCurrentPatchVersion(), DefaultPatchRequestCallback.ERROR_LISTENER_CHECK_FAIL);
    }


    public static void reportTinkerLoadFail() {
        if (sTinkerServerClient == null) {
            TinkerLog.e(TAG, "reportTinkerPatchFail, sTinkerServerClient == null");
            return;
        }
        sTinkerServerClient.reportPatchFail(sTinkerServerClient.getCurrentPatchVersion(), DefaultPatchRequestCallback.ERROR_LOAD_FAIL);
    }
}
