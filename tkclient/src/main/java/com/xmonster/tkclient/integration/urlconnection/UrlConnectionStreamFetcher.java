package com.xmonster.tkclient.integration.urlconnection;

import android.util.Log;

import com.xmonster.tkclient.model.DataFetcher;
import com.xmonster.tkclient.model.TKClientUrl;
import com.xmonster.tkclient.utils.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by sun on 29/09/2016.
 */

public class UrlConnectionStreamFetcher implements DataFetcher<InputStream> {

    private static final String TAG = "UrlConnectionFetcher";
    private final TKClientUrl tkUrl;
    private final ThreadPoolExecutor threadPool;
    private InputStream stream;

    public UrlConnectionStreamFetcher(ThreadPoolExecutor threadPool, TKClientUrl tkUrl) {
        this.tkUrl = tkUrl;
        this.threadPool = threadPool;
    }

    @Override
    public void loadData(final DataCallback<? super InputStream> callback) {
        ConnectionWorker worker = new ConnectionWorker(tkUrl, new DataCallback<InputStream>() {
            @Override
            public void onDataReady(InputStream data) {
                stream = data;
                callback.onDataReady(data);
            }

            @Override
            public void onLoadFailed(Exception e) {
                callback.onLoadFailed(e);
            }
        });
        this.threadPool.execute(worker);
    }

    @Override
    public void cleanup() {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cancel() {
        // NOT IMPLEMENT
    }

    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    private static class ConnectionWorker implements Runnable {

        private final DataCallback<? super InputStream> callback;
        private final TKClientUrl url;

        ConnectionWorker(TKClientUrl url, DataCallback<? super InputStream> callback) {
            this.callback = Preconditions.checkNotNull(callback);
            this.url = Preconditions.checkNotNull(url);
        }

        @Override
        public void run() {
            try {
                HttpURLConnection conn = (HttpURLConnection) url.toURL().openConnection();
                conn.setRequestMethod(url.getMethod());
                conn.setDoOutput(true);
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setInstanceFollowRedirects(false);
                for (Map.Entry<String, String> entry : url.getHeaders().entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
                conn.setUseCaches(false);
                conn.connect();
                Log.d(TAG, "response code " + conn.getResponseCode() + " msg: " + conn.getResponseMessage());
                InputStream inputStream = conn.getInputStream();
                this.callback.onDataReady(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
                this.callback.onLoadFailed(e);
            }
        }
    }
}
