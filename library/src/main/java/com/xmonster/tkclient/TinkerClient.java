package com.xmonster.tkclient;

import android.content.Context;
import android.text.TextUtils;

import com.xmonster.tkclient.model.DataFetcher;
import com.xmonster.tkclient.model.RequestLoader;
import com.xmonster.tkclient.model.TKClientUrl;
import com.xmonster.tkclient.module.ManifestParser;
import com.xmonster.tkclient.module.TKClientModule;
import com.xmonster.tkclient.utils.Preconditions;
import com.xmonster.tkclient.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class TinkerClient implements TKClientFactory {

    private final String appVersion;
    private final String appKey;
    private final String host;
    private final Registry registry;
    private RequestLoader<TKClientUrl, InputStream> loader;
    private static volatile TinkerClient client;

    public static TinkerClient with(Context context, String appKey, String appVersion) {
        if (client == null) {
            synchronized (TinkerClient.class) {
                if (client == null) {
                    client = new TinkerClient.Builder()
                            .appKey(appKey)
                            .appVersion(appVersion)
                            .build();

                    Context applicationContext = context.getApplicationContext();
                    TKClientModule module = new ManifestParser(applicationContext).parse();
                    if (module != null) {
                        module.register(applicationContext, client.registry);
                    } else {
                        throw new RuntimeException("No Http Module Loaded");
                    }
                    client.loader = client.registry.build(TKClientUrl.class, InputStream.class);
                }
            }
        }
        return client;
    }

    @Override
    public void sync(final DataFetcher.DataCallback<String> callback) {
        final String url = TextUtils.join("/", Arrays.asList(this.host, this.appKey, this.appVersion));
        TKClientUrl tkClientUrl = new TKClientUrl.Builder()
                .url(url)
                .param("d", "deviceId")
                .param("v", System.currentTimeMillis())
                .build();

        final DataFetcher<InputStream> dataFetcher = loader.buildLoadData(tkClientUrl);
        dataFetcher.loadData(new DataFetcher.DataCallback<InputStream>() {
            @Override
            public void onDataReady(InputStream data) {
                if (callback == null) return;
                try {
                    callback.onDataReady(Utils.readStreamToString(data));
                } catch (IOException e) {
                    callback.onLoadFailed(e);
                } finally {
                    dataFetcher.cleanup();
                }
            }

            @Override
            public void onLoadFailed(Exception e) {
                if (callback == null) return;
                try {
                    callback.onLoadFailed(e);
                } finally {
                    dataFetcher.cleanup();
                }
            }
        });
    }

    @Override
    public void download(String patchVersion, final String filePath, final DataFetcher.DataCallback<? super File> callback) {
        patchVersion = Preconditions.checkNotEmpty(patchVersion);
        final String url = TextUtils.join(
                "/",
                Arrays.asList(this.host, this.appKey, this.appVersion, "file"+patchVersion)
        );
        TKClientUrl tkClientUrl = new TKClientUrl.Builder()
                .url(url)
                .param("d", "deviceId")
                .param("v", System.currentTimeMillis())
                .build();
        final DataFetcher<InputStream> dataFetcher = loader.buildLoadData(tkClientUrl);
        dataFetcher.loadData(new DataFetcher.DataCallback<InputStream>() {
            @Override
            public void onDataReady(InputStream data) {
                if (callback == null) return;
                try {
                    callback.onDataReady(Utils.readStreamToFile(data, filePath));
                } catch (IOException e) {
                    callback.onLoadFailed(e);
                } finally {
                    dataFetcher.cleanup();
                }
            }

            @Override
            public void onLoadFailed(Exception e) {
                if (callback == null) return;
                try {
                    callback.onLoadFailed(e);
                } finally {
                    dataFetcher.cleanup();
                }
            }
        });
    }

    private TinkerClient(final String appVersion, final String appKey, final String host) {
        this.appVersion = appVersion;
        this.appKey = appKey;
        this.host = host;
        this.registry = new Registry();
    }

    public static class Builder {
        private static final String HOST_URL = "http://q.tinkerpatch.com";
        private String appVersion;
        private String appKey;
        private String host;

        public TinkerClient.Builder host(String host) {
            this.host = host;
            return this;
        }

        public TinkerClient.Builder appKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        public TinkerClient.Builder appVersion(String appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        void makeDefault() {
            if (TextUtils.isEmpty(host)) {
                this.host = HOST_URL;
            }
        }

        public TinkerClient build() {
            makeDefault();
            return new TinkerClient(this.appVersion, this.appKey, this.host);
        }
    }
}
