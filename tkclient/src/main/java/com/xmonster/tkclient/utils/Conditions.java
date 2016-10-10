package com.xmonster.tkclient.utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by sun on 11/10/2016.
 */

public class Conditions {

    private static final String FILE_NAME = "CONDITIONS_MAP";
    private static final Pattern INT_PATTERN = Pattern.compile("-?[0-9]+");

    private final Map<String, String> properties;

    public Conditions (Context context) {
        properties = read(context);
    }

    public Boolean check(String rules) {
        return true;
    }

    public Conditions set(String key, String value) {
        properties.put(key, value);
        return this;
    }

    public Conditions clean() {
        properties.clear();
        return this;
    }

    public void save(Context context) throws IOException {
        File file = new File(context.getFilesDir(), FILE_NAME);
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
        outputStream.writeObject(properties);
        outputStream.flush();
        outputStream.close();
    }

    private HashMap<String, String> read(Context context) {
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            if (file.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                return (HashMap<String, String>) ois.readObject();
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        return new HashMap<>();
    }

    private static Boolean isInt(String string) {
        return INT_PATTERN.matcher(string).matches();
    }
}
