package com.xmonster.tkclient.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sun on 9/18/16.
 */

public class FileUtils {
    public static File readStream(InputStream inputStream, String filePath) throws IOException {
        File file = new File(filePath);
        FileOutputStream fileOutput = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int bufferLength;
        while ((bufferLength = inputStream.read(buffer)) > 0) {
            fileOutput.write(buffer, 0, bufferLength);
        }
        fileOutput.close();
        return new File(filePath);
    }
}
