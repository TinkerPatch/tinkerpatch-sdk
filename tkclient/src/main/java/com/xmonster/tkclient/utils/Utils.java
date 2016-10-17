package com.xmonster.tkclient.utils;

import android.content.Context;

import com.xmonster.tkclient.Config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sun on 9/18/16.
 */

public final class Utils {

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
            byte[] buffer = new byte[Config.BUFFER_SIZE];
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

    public static String readStreamToString(InputStream inputStream, String charset) throws IOException {
        if (inputStream == null) {
            return null;
        }

        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buffer = new byte[Config.BUFFER_SIZE];
        int bufferLength;

        while ((bufferLength = inputStream.read(buffer)) > 0) {
            bo.write(buffer, 0, bufferLength);
        }
        return bo.toString(charset);
    }

    /**
     * 在SDK启动时检测灰度值有没有生成，若没有，从1-10随机选择一个数，保存起来作为这个设备的灰度值。
     * 若是灰度下发，请求返回的json会有g字段，值是1-10，例如{v:5, g:2}。
     * 这里g字段的值大于设备的灰度值就命中灰度，否则不命中
     */
    public static boolean isInGrayGroup(Integer grayValue, Context context) {
        return grayValue == null || Installation.grayValue(context) >= grayValue;
    }
}
