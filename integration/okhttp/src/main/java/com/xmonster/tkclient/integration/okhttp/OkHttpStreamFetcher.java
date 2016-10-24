package com.xmonster.tkclient.integration.okhttp;


import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.xmonster.tkclient.model.DataFetcher;
import com.xmonster.tkclient.model.TKClientUrl;
import com.xmonster.tkclient.utils.ContentLengthInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


/**
 * Fetches an {@link InputStream} using the okhttp library.
 */
public class OkHttpStreamFetcher implements DataFetcher<InputStream> {
    private static final String TAG = "OkHttpFetcher";
    private final OkHttpClient client;
    ResponseBody responseBody;
    InputStream stream;
    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private TKClientUrl tkUrl;

    public OkHttpStreamFetcher(OkHttpClient client, TKClientUrl tkUrl) {
        this.client = client;
        this.tkUrl = tkUrl;
    }

    @Override
    public void loadData(final DataCallback<? super InputStream> callback) {
        Request.Builder requestBuilder = new Request.Builder().url(tkUrl.toStringUrl());
        switch (tkUrl.getMethod()) {
            case "GET":
                requestBuilder = requestBuilder.get();
                break;
            case "POST":
                RequestBody requestBody = RequestBody.create(JSON, tkUrl.getBody());
                requestBuilder = requestBuilder.post(requestBody);
                break;
            default:
                throw new RuntimeException("Unsupported request Method" + tkUrl.getMethod());
        }
        for (Map.Entry<String, String> headerEntry : tkUrl.getHeaders().entrySet()) {
            String key = headerEntry.getKey();
            requestBuilder.addHeader(key, headerEntry.getValue());
        }
        Request request = requestBuilder.build();

        client.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "OkHttp failed to obtain result", e);
                callback.onLoadFailed(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                responseBody = response.body();
                if (response.isSuccessful()) {
                    long contentLength = response.body().contentLength();
                    stream = ContentLengthInputStream.obtain(responseBody.byteStream(), contentLength);
                } else {
                    Log.d(TAG, "OkHttp got error response: " + response.code() + ", " + response.message());
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
        } catch (IOException e) {
            // Ignored
        }
        if (responseBody != null) {
            try {
                responseBody.close();
            } catch (IOException e) {
                // Ignored.
            }
        }
    }

    @Override
    public void cancel() {

    }

    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }
}
