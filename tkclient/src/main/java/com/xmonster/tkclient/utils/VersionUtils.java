package com.xmonster.tkclient.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import static com.xmonster.tkclient.TinkerClient.TAG;

/**
 * Created by sun on 18/10/2016.
 */

public final class VersionUtils {

    private static final String CURRENT_VERSION = "current_version";
    private static final String PATCH_FILE_PREF = "patch_path_";
    private static final String SP_FILE_NAME    = "tkclient_sp_version";

    private VersionUtils() {
        // A Util Class
    }

    public static boolean update(Context context, Integer version, String path) {
        SharedPreferences sp = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        Integer current = sp.getInt(CURRENT_VERSION, 0);
        if (version > current) {
            return sp.edit().putInt(CURRENT_VERSION, version)
                .putString(PATCH_FILE_PREF + version, path)
                .commit();
        } else {
            Log.w(TAG, "update failed, target version is not latest. current version is:" + version);
            return false;
        }
    }

    public static Integer getCurrentVersion(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getInt(CURRENT_VERSION, 0);
    }

    public static String getPatchFilePath(Context context, Integer version) {
        SharedPreferences sp = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(PATCH_FILE_PREF + version, "");
    }
}
