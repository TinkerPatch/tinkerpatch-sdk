package com.tencent.tinker.server;

import android.content.Context;
import android.content.SharedPreferences;

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.TinkerRuntimeException;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;
import com.tencent.tinker.server.model.DataFetcher;

import java.io.File;

/**
 * Created by zhangshaowen on 16/10/24.
 */
public class TinkerServerClient {
    private static final String TAG = "Tinker.ServerClient";

    private static final String TINKER_LAST_CHECK = "tinker_last_check";

    private static final long DEFAULT_CHECK_INTERVAL = 1 * 3600 * 1000;
    private static final long NEVER_CHECK_UPDATE     = -1;

    private long checkInterval = DEFAULT_CHECK_INTERVAL;

    private final Tinker  tinker;
    private final Context context;

    TinkerClientImpl tinkerClientImp;

    public TinkerServerClient(Context context, Tinker tinker, String appKey, String appVersion, Boolean debug) {
        this.tinker = tinker;
        this.context = context;
        this.tinkerClientImp = TinkerClientImpl.init(context, appKey, appVersion, debug);
    }

    public void checkTinkerUpdate() {
        //check SharePreferences also
        if (!tinker.isTinkerEnabled() || !ShareTinkerInternals.isTinkerEnableWithSharedPreferences(context)) {
            TinkerLog.e(TAG, "tinker is disable, just return");
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(
            ShareConstants.TINKER_SHARE_PREFERENCE_CONFIG, Context.MODE_MULTI_PROCESS
        );
        long last = sp.getLong(TINKER_LAST_CHECK, 0);
        if (last == NEVER_CHECK_UPDATE) {
            TinkerLog.i(TAG, "tinker update is disabled, with never check flag!");
            return;
        }
        long interval = System.currentTimeMillis() - last;
        if (tinkerClientImp.isDebug() || interval >= checkInterval) {
            sp.edit().putLong(TINKER_LAST_CHECK, System.currentTimeMillis()).commit();
            tinkerClientImp.update(context, new DataFetcher.DataCallback<File>() {
                @Override
                public void onDataReady(File data) {
                    TinkerLog.e(TAG, "update success, file path:" + data.getPath());
                    tinkerClientImp.updateVersionFile();
                }

                @Override
                public void onLoadFailed(Exception e) {
                    TinkerLog.e(TAG, "update failed, exception:" + e);
                }
            });
        } else {
            TinkerLog.i(TAG, "tinker sync should wait interval %ss", (checkInterval - interval) / 1000);
        }
    }

    public void setCheckIntervalByHours(int hours) {
        if (hours < 0 || hours > 24) {
            throw new TinkerRuntimeException("hours must be between 0 and 24");
        }
        checkInterval = (long) hours * 3600 * 1000;
    }
}
