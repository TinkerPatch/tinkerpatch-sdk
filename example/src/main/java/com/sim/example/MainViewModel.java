package com.sim.example;

import android.content.Context;
import android.databinding.ObservableField;
import android.util.Log;

import com.xmonster.tkclient.*;
import com.xmonster.tkclient.BuildConfig;
import com.xmonster.tkclient.model.DataFetcher;

import java.io.File;

public class MainViewModel {

    private static final String TAG = "MainViewModel";
    public ObservableField<String> response;
    private static final String APP_KEY = "c9dd59c417cdc023";

    public MainViewModel(Context context) {
        this.response = new ObservableField<>("Hello World!");
        TinkerClient.init(context, APP_KEY, "1", true);
    }

    public void sync() {
        this.response.set("sync");
        Log.d(TAG, "sync");
        TinkerClient.get().sync(new DataFetcher.DataCallback<String>() {
            @Override
            public void onDataReady(String data) {
                response.set(data);
            }

            @Override
            public void onLoadFailed(Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void fetchUpdate() {
        this.response.set("fetchUpdate");
        Log.d(TAG, "fetchUpdate");
        TinkerClient.get().download("patchVersion", "./test", new DataFetcher.DataCallback<File>() {
            @Override
            public void onDataReady(File data) {
                if (data != null) {
                    Log.d(TAG, "save to " + data.getAbsolutePath());
                    response.set(data.getAbsolutePath());
                }
            }

            @Override
            public void onLoadFailed(Exception e) {
                e.printStackTrace();
            }
        });
    }
}