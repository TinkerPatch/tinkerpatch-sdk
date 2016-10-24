package com.xmonster.tkclient.integration.urlconnection;

import android.util.Log;

import com.xmonster.tkclient.Config;
import com.xmonster.tkclient.model.DataFetcher;
import com.xmonster.tkclient.model.TKClientUrl;
import com.xmonster.tkclient.utils.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.Executor;


public class UrlConnectionStreamFetcher implements DataFetcher<InputStream> {

    private static final String TAG = "UrlConnectionFetcher";
    private final TKClientUrl tkUrl;
    private final Executor executor;
    InputStream stream;

    public UrlConnectionStreamFetcher(Executor executor, TKClientUrl tkUrl) {
        this.tkUrl = tkUrl;
        this.executor = executor;
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
        executor.execute(worker);
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
                conn.setUseCaches(false);
                for (Map.Entry<String, String> entry : url.getHeaders().entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
                switch (url.getMethod()) {
                    case "GET":
                        break;
                    case "POST":
                        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), Config.CHARSET);
                        writer.write(url.getBody());
                        writer.flush();
                        writer.close();
                        break;
                    default:
                        throw new RuntimeException("Unsupported request method" + url.getMethod());
                }
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
