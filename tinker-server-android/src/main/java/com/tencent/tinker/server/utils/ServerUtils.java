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
import android.content.pm.ApplicationInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sun on 9/18/16.
 */

public final class ServerUtils {
    public static final String CHARSET             = "UTF-8";
    public static final int    BUFFER_SIZE         = 4096;
    public static final String TINKER_SERVER_DIR   = "tinker_server";
    public static final String TINKER_VERSION_FILE = "version.info";


    private ServerUtils() {
        // A Utils Class
    }

    public static File readStreamToFile(InputStream inputStream, String filePath) throws IOException {
        if (inputStream == null) {
            return null;
        }

        File file = new File(filePath);
        File parent = file.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException(String.format("Can't create folder %s", parent.getAbsolutePath()));
        }
        FileOutputStream fileOutput = new FileOutputStream(file);
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bufferLength;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
            }
        } finally {
            try {
                fileOutput.close();
            } catch (IOException ignored) {
                // ignored
            }
        }
        return file;
    }

    public static Integer stringToInteger(String string) {
        if (string == null) {
            return null;
        }
        return Integer.parseInt(string);
    }

    public static String readStreamToString(InputStream inputStream, String charset) {
        if (inputStream == null) {
            return null;
        }
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int bufferLength;

        String result;
        try {
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                bo.write(buffer, 0, bufferLength);
            }
            result = bo.toString(charset);
        } catch (Throwable e) {
            result = null;
        }
        return result;
    }

    public static File getServerDirectory(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        if (applicationInfo == null) {
            // Looks like running on a test Context, so just return without patching.
            return null;
        }
        return new File(applicationInfo.dataDir, TINKER_SERVER_DIR);
    }

    public static File getServerFile(Context context, String appVersion, String currentVersion) {
        return new File(getServerDirectory(context), appVersion + "_" + currentVersion + ".apk");
    }
}
