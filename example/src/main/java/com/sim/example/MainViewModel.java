package com.sim.example;

import android.content.Context;
import android.databinding.ObservableField;
import android.util.Log;

import com.xmonster.tkclient.TinkerClient;
import com.xmonster.tkclient.model.DataFetcher;

import java.io.File;

public class MainViewModel {

    private static final String TAG = "MainViewModel";
    public ObservableField<String> response;
    private TinkerClient tinkerClient;

    public MainViewModel(Context context) {
        this.response = new ObservableField<>("Hello World!");
        tinkerClient = TinkerClient.with(context, "appKey", "appVersion");
    }

    public void sync() {
        this.response.set("sync");
        Log.d(TAG, "sync");
        tinkerClient.sync(new DataFetcher.DataCallback<String>() {
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
        tinkerClient.download("patchVersion", "./test", new DataFetcher.DataCallback<File>() {
            @Override
            public void onDataReady(File data) {
                Log.d(TAG, "save to " + data.getAbsolutePath());
                response.set(data.getAbsolutePath());
            }

            @Override
            public void onLoadFailed(Exception e) {
                e.printStackTrace();
            }
        });
    }
}