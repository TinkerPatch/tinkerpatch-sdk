package com.xmonster.tkclient.integration.okhttp3;


import android.util.Log;

import com.xmonster.tkclient.model.DataFetcher;
import com.xmonster.tkclient.model.TKClientUrl;
import com.xmonster.tkclient.utils.ContentLengthInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Fetches an {@link InputStream} using the okhttp3 library.
 */
public class OkHttp3StreamFetcher implements DataFetcher<InputStream> {
    private static final String TAG = "OkHttp3Fetcher";
    private final Call.Factory client;
    private ResponseBody responseBody;
    private volatile Call call;
    private InputStream stream;
    private TKClientUrl tkUrl;

    public OkHttp3StreamFetcher(Call.Factory client, TKClientUrl tkUrl) {
        this.client = client;
        this.tkUrl = tkUrl;
    }

    @Override
    public void loadData(final DataCallback<? super InputStream> callback) {
        Request.Builder requestBuilder = new Request.Builder().url(tkUrl.toStringUrl());
        for (Map.Entry<String, String> headerEntry : tkUrl.getHeaders().entrySet()) {
            String key = headerEntry.getKey();
            requestBuilder.addHeader(key, headerEntry.getValue());
        }
        Request request = requestBuilder.build();

        call = client.newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "OkHttp3 failed to obtain result", e);
                callback.onLoadFailed(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                responseBody = response.body();
                if (response.isSuccessful()) {
                    long contentLength = response.body().contentLength();
                    Log.d(TAG, "OkHttp3 got success response: " + response.code() + ", " + response.message());
                    stream = ContentLengthInputStream.obtain(responseBody.byteStream(), contentLength);
                } else {
                    Log.d(TAG, "OkHttp3 got error response: " + response.code() + ", " + response.message());
                }
                callback.onDataReady(stream);
            }
        });
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
        if (responseBody != null) {
            responseBody.close();
        }
    }

    @Override
    public void cancel() {
        Call local = call;
        if (local != null) {
            local.cancel();
        }
    }

    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }
}
