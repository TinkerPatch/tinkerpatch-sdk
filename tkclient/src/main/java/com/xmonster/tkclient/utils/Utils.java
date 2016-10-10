package com.xmonster.tkclient.utils;

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
        // nothing
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
}
