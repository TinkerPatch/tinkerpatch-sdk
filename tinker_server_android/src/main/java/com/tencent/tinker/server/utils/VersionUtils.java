package com.tencent.tinker.server.utils;

import android.content.Context;

import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.TinkerRuntimeException;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import static com.tencent.tinker.server.TinkerClientImpl.TAG;

/**
 * Created by sun on 18/10/2016.
 */

public final class VersionUtils {
    private static final String CURRENT_VERSION = "version";
    private static final String APP_VERSION     = "app";
    private static final String UUID_VALUE      = "uuid";
    private static final String GRAY_VALUE      = "gray";
    private static final String VERSION_FILE    = "version";

    private final File versionFile;
    private String  uuid;
    private String  appVersion;
    private Integer grayValue;
    private Integer patchVersion;


    public VersionUtils(Context context, String appVersion) {
        versionFile = new File(Utils.getServerDirectory(context), VERSION_FILE);
        readVersionProperty();

        if (!versionFile.exists() || uuid == null || appVersion == null || grayValue == null || patchVersion == null) {
            updateVersionProperty(appVersion, 0, randInt(1, 10), UUID.randomUUID().toString());
        } else if (!appVersion.equals(this.appVersion)) {
            updateVersionProperty(appVersion, 0, grayValue, uuid);
        }
    }

    public boolean isInGrayGroup(Integer gray) {
        return gray == null || grayValue >= gray;
    }

    public boolean isUpdate(Integer version, String currentAppVersion) {
        if (!currentAppVersion.equals(appVersion)) {
            TinkerLog.w(TAG, "update return true, appVersion from %s to %s", appVersion, currentAppVersion);
            return true;
        }
        Integer current = getPatchVersion();
        if (version > current) {
            TinkerLog.w(TAG, "update return true, patchVersion from %s to %s", current, version);

            return true;
        } else {
            TinkerLog.w(TAG, "update return false, target version is not latest. current version is:" + version);
            return false;
        }
    }

    public Integer getPatchVersion() {
        if (patchVersion == null) {
            return 0;
        }
        return patchVersion;
    }

    public String id() {
        return uuid;
    }

    public Integer grayValue() {
        return grayValue;
    }

    private int randInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    private void readVersionProperty() {
        if (versionFile == null || !versionFile.exists() || versionFile.length() == 0) {
            return;
        }

        Properties properties = new Properties();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(versionFile);
            properties.load(inputStream);
            uuid = properties.getProperty(UUID_VALUE);
            appVersion = properties.getProperty(APP_VERSION);
            grayValue = Utils.stringToInteger(properties.getProperty(GRAY_VALUE));
            patchVersion = Utils.stringToInteger(properties.getProperty(CURRENT_VERSION));
        } catch (IOException e) {
            TinkerLog.e(TAG, "readVersionProperty exception:" + e);
        } finally {
            SharePatchFileUtil.closeQuietly(inputStream);
        }

    }

    public void updateVersionProperty(String appVersion, int currentVersion, int grayValue, String uuid) {
        TinkerLog.i(TAG, "updateVersionProperty file path:"
            + versionFile.getAbsolutePath()
            + " , appVersion: " + appVersion
            + " , patchVersion:" + currentVersion
            + " , grayValue:" + grayValue
            + " , uuid:" + uuid);

        File parentFile = versionFile.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new TinkerRuntimeException("make mkdirs error: " + parentFile.getAbsolutePath());
        }

        Properties newProperties = new Properties();
        newProperties.put(CURRENT_VERSION, String.valueOf(currentVersion));
        newProperties.put(GRAY_VALUE, String.valueOf(grayValue));
        newProperties.put(APP_VERSION, appVersion);
        newProperties.put(UUID_VALUE, uuid);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(versionFile, false);
            String comment = "from old version:" + getPatchVersion() + " to new version:" + currentVersion;
            newProperties.store(outputStream, comment);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SharePatchFileUtil.closeQuietly(outputStream);
        }
        //update value
        this.appVersion = appVersion;
        this.patchVersion = currentVersion;
        this.grayValue = grayValue;
        this.uuid = uuid;
    }
}
