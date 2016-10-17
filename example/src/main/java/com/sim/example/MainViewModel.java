package com.sim.example;

import android.content.Context;
import android.databinding.ObservableField;
import android.util.Log;

import com.xmonster.tkclient.TinkerClient;
import com.xmonster.tkclient.model.DataFetcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MainViewModel {

    private static final String TAG = "MainViewModel";
    private static final String APP_KEY = "c9dd59c417cdc023";
    private final Context context;
    public ObservableField<String> response;

    public MainViewModel(Context context) {
        this.response = new ObservableField<>("Hello World!");
        TinkerClient.init(context, APP_KEY, "1", false);
        this.context = context;
    }

    public void sync() {
        this.response.set("sync");
        Log.d(TAG, "sync");
        TinkerClient.get().sync(context, new DataFetcher.DataCallback<String>() {
            @Override
            public void onDataReady(String data) {
                response.set(data);
            }

            @Override
            public void onLoadFailed(Exception e) {
                e.printStackTrace();
            }
        });
        TinkerClient.get().params("test", "1").save(context);
    }

    public void fetchUpdate() {
        this.response.set("fetchUpdate");
        Log.d(TAG, "fetchUpdate");
        final String patchVersion = "1";
        File patchFile = new File(context.getFilesDir(), "this/is/a/test/folder/patch_" + patchVersion);
        TinkerClient.get().download(
            context,
            patchVersion,
            patchFile.getAbsolutePath(),
            new DataFetcher.DataCallback<File>() {
                @Override
                public void onDataReady(File data) {
                    if (data != null) {
                        Log.d(TAG, "save to " + data.getAbsolutePath());
                        response.set(data.getAbsolutePath() + readFromFile(data));
                    }
                }

                @Override
                public void onLoadFailed(Exception e) {
                    e.printStackTrace();
                }
            }
        );
    }

    public void update() {
        this.response.set("update whole flow");
        Log.d(TAG, "update");
        final String patchVersion = "1";
        File patchFile = new File(context.getFilesDir(), "this/is/a/test/folder/patch_" + patchVersion);
        TinkerClient.get().update(
            context,
            patchVersion,
            patchFile.getAbsolutePath(),
            new DataFetcher.DataCallback<File>() {
                @Override
                public void onDataReady(File data) {
                    if (data != null) {
                        Log.d(TAG, "save to " + data.getAbsolutePath());
                        response.set(data.getAbsolutePath() + readFromFile(data));
                    }
                }

                @Override
                public void onLoadFailed(Exception e) {
                    e.printStackTrace();
                }
            }
        );
    }


    public String readFromFile(File file) {
        //Read text from file
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException ignore) {
            //You'll need to add proper error handling here
        }
        return text.toString();
    }
}
