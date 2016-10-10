package com.xmonster.tkclient.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.UUID;

/**
 * solution from
 * <a href="http://android-developers.blogspot.com/2011/03/identifying-app-installations.html">identifying-app-installations</a>
 */
public class Installation {
    private static String ID = null;
    private static Integer gValue = null;
    private static final String INSTALLATION = "INSTALLATION";
    private static final String  GRAY_VALUE = "GRAY_VALUE";

    public synchronized static String id(Context context) {
        if (ID == null) {
            ID = fileValue(context, INSTALLATION, UUID.randomUUID().toString());
        }
        return ID;
    }

    public synchronized static Integer grayValue(Context context) {
        if (gValue == null) {
            int g = randInt(1, 10);
            gValue = Integer.valueOf(fileValue(context, GRAY_VALUE, String.valueOf(g)));
        }
        return gValue;
    }

    private synchronized static String fileValue(Context context, String fileName, String value) {
        File file = new File(context.getFilesDir(), fileName);
        try {
            if (!file.exists()) {
                writeInstallationFile(file, value);
            }
            return readInstallationFile(file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation, String value) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        out.write(value.getBytes());
        out.close();
    }

    private static int randInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }
}