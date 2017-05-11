/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Shengjie Sim Sun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.tencent.tinker.server.urlconnection;


import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;
import com.tencent.tinker.server.model.DataFetcher;
import com.tencent.tinker.server.model.TinkerClientUrl;
import com.tencent.tinker.server.utils.Preconditions;
import com.tencent.tinker.server.utils.ServerUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.Executor;


public class UrlConnectionStreamFetcher implements DataFetcher<InputStream> {

    private static final String TAG = "Tinker.UrlConnectionFetcher";
    private final TinkerClientUrl tkUrl;
    private Executor        executor;

    public UrlConnectionStreamFetcher(Executor executor, TinkerClientUrl tkUrl) {
        this.tkUrl = tkUrl;
        this.executor = executor;
    }

    @Override
    public void loadData(final DataCallback<? super InputStream> callback) {
        ConnectionWorker worker = new ConnectionWorker(tkUrl, new DataCallback<InputStream>() {
            @Override
            public void onDataReady(InputStream data) {
                callback.onDataReady(data);
            }

            @Override
            public void onLoadFailed(Exception e) {
                callback.onLoadFailed(e);
            }
        });
        if (executor != null) {
            executor.execute(worker);
        } else {
            TinkerLog.e(TAG, "Executor is null");
        }
    }

    @Override
    public void cleanup() {
        this.executor = null;
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
        private final TinkerClientUrl                   url;

        ConnectionWorker(TinkerClientUrl url, DataCallback<? super InputStream> callback) {
            this.callback = Preconditions.checkNotNull(callback);
            this.url = Preconditions.checkNotNull(url);
        }

        @Override
        public void run() {
            InputStream inputStream = null;
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
                        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), ServerUtils.CHARSET);
                        writer.write(url.getBody());
                        writer.flush();
                        writer.close();
                        break;
                    default:
                        throw new RuntimeException("Unsupported request method" + url.getMethod());
                }
                conn.connect();
                TinkerLog.d(TAG, "response code " + conn.getResponseCode() + " msg: " + conn.getResponseMessage());
                inputStream = conn.getInputStream();
                this.callback.onDataReady(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
                this.callback.onLoadFailed(e);
            } finally {
                SharePatchFileUtil.closeQuietly(inputStream);
            }
        }
    }
}
