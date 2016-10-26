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

public final class Utils {
    public static final String CHARSET                   = "UTF-8";
    public static final int    BUFFER_SIZE               = 4096;
    public static final String TINKER_SERVER_DIR         = "tinker_server";


    private Utils() {
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
    public static String readStreamToString(InputStream inputStream, String charset) throws IOException {
        if (inputStream == null) {
            return null;
        }

        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int bufferLength;

        while ((bufferLength = inputStream.read(buffer)) > 0) {
            bo.write(buffer, 0, bufferLength);
        }
        return bo.toString(charset);
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
