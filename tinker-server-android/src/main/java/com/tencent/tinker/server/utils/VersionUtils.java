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

import static com.tencent.tinker.server.client.TinkerClientAPI.TAG;

/**
 * Created by sun on 18/10/2016.
 */

public final class VersionUtils {
    private static final String APP_VERSION     = "app";
    private static final String UUID_VALUE      = "uuid";
    private static final String GRAY_VALUE      = "gray";
    private static final String CURRENT_VERSION = "version";
    private static final String CURRENT_MD5     = "md5";


    private final File    versionFile;
    private       String  uuid;
    private       String  appVersion;
    private       Integer grayValue;
    private       Integer patchVersion;
    private       String  patchMd5;


    public VersionUtils(Context context, String appVersion) {
        versionFile = new File(ServerUtils.getServerDirectory(context), ServerUtils.TINKER_VERSION_FILE);
        readVersionProperty();

        if (!versionFile.exists() || uuid == null || appVersion == null || grayValue == null || patchVersion == null) {
            updateVersionProperty(appVersion, 0, "", randInt(1, 10), UUID.randomUUID().toString());
        } else if (!appVersion.equals(this.appVersion)) {
            updateVersionProperty(appVersion, 0, "", grayValue, uuid);
        }
    }

    public boolean isInGrayGroup(Integer gray) {
        boolean result = gray == null || gray >= grayValue;
        TinkerLog.w(TAG, "isInGrayGroup return %b, gray value:%d and my gray value is %d", result, gray, grayValue);
        return result;
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

    public String getPatchMd5() {
        if (patchMd5 == null) {
            return "";
        }
        return patchMd5;
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
            grayValue = ServerUtils.stringToInteger(properties.getProperty(GRAY_VALUE));
            patchVersion = ServerUtils.stringToInteger(properties.getProperty(CURRENT_VERSION));
            patchMd5 = properties.getProperty(CURRENT_MD5);
        } catch (IOException e) {
            TinkerLog.e(TAG, "readVersionProperty exception:" + e);
        } finally {
            SharePatchFileUtil.closeQuietly(inputStream);
        }

    }

    public void updateVersionProperty(String appVersion, int currentVersion,
                                      String patchMd5, int grayValue, String uuid) {
        TinkerLog.i(TAG, "updateVersionProperty file path:"
            + versionFile.getAbsolutePath()
            + " , appVersion: " + appVersion
            + " , patchVersion:" + currentVersion
            + " , patchMd5:" + patchMd5
            + " , grayValue:" + grayValue
            + " , uuid:" + uuid);

        File parentFile = versionFile.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new TinkerRuntimeException("make mkdirs error: " + parentFile.getAbsolutePath());
        }

        Properties newProperties = new Properties();
        newProperties.put(CURRENT_VERSION, String.valueOf(currentVersion));
        newProperties.put(CURRENT_MD5, patchMd5);

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
        this.patchMd5 = patchMd5;
    }
}
