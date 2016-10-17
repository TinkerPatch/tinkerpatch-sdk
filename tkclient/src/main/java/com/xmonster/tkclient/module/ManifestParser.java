package com.xmonster.tkclient.module;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Parses {@link com.xmonster.tkclient.module.TKClientModule} references out of the AndroidManifest file.
 */
public final class ManifestParser {
    private static final String TAG = "ManifestParser";
    private static final String MODULE_VALUE = "TKClientModule";

    private final Context context;

    public ManifestParser(Context context) {
        this.context = context;
    }

    private static TKClientModule parseModule(String className) {
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to find TKClientModule implementation", e);
        }

        Object module;
        try {
            module = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Unable to instantiate implementation for " + clazz, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to instantiate implementation for " + clazz, e);
        }

        if (!(module instanceof TKClientModule)) {
            throw new RuntimeException("Expected instanceof TKCLientModule, but found: " + module);
        }
        return (TKClientModule) module;
    }

    public TKClientModule parse() {
        Log.d(TAG, "Loading TKClient module");
        TKClientModule module = null;
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                context.getPackageName(),
                PackageManager.GET_META_DATA
            );
            if (appInfo.metaData == null) {
                return null;
            }
            for (String key : appInfo.metaData.keySet()) {
                if (MODULE_VALUE.equals(appInfo.metaData.get(key))) {
                    module = parseModule(key);
                    Log.d(TAG, "Loaded TinkerClient module: " + key);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Unable to find metadata to parse TKClientModules", e);
        }
        Log.d(TAG, "Finished loading TinkerClient modules");
        return module;
    }
}
