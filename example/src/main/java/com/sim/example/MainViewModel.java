package com.sim.example;

import android.databinding.ObservableField;
import android.util.Log;

public class MainViewModel {

    private static final String TAG = "MainViewModel";
    public ObservableField<String> response;

    public MainViewModel() {
        this.response = new ObservableField<>("Hello World!");
    }

    public void sync() {
        this.response.set("sync");
        Log.d(TAG, "sync");
    }

    public void fetchUpdate() {
        this.response.set("fetchUpdate");
        Log.d(TAG, "fetchUpdate");
    }
}