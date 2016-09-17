package com.sim.tinyhttpclient.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sun on 9/18/16.
 */

public class StringUtils {

    public static boolean isPresent(String str) {
        return str != null && str.length() > 0;
    }

    public static boolean isBlank(String str) {
        return !isPresent(str);
    }

    public static String readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bufferLength;
        while((bufferLength = inputStream.read(buffer)) > 0) {
            bo.write(buffer, 0, bufferLength);
        }
        return bo.toString();
    }
}
