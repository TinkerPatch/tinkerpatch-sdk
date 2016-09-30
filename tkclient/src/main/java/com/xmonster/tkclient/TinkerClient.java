package com.xmonster.tkclient;

import android.content.Context;
import android.text.TextUtils;

import com.xmonster.tkclient.integration.urlconnection.UrlConnectionUrlLoader;
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

public class TinkerClient implements TKClientAPI {

    private static volatile TinkerClient client;
    private final String appVersion;
    private final String appKey;
    private final String host;
    private final boolean debug;
    private final Registry registry;
    private RequestLoader<TKClientUrl, InputStream> loader;

    private TinkerClient(String appVersion, String appKey, String host, Boolean debug) {
        this.appVersion = appVersion;
        this.appKey = appKey;
        this.host = host;
        this.debug = debug;
        this.registry = new Registry();
    }

    /**
     * Singleton get method for Tinker client, you need invoke
     * {@link #init(Context, String, String, Boolean)} before it invoke.
     *
     * @return the instance of {@link TinkerClient}
     */
    public static TinkerClient get() {
        if (client == null) {
            throw new RuntimeException("Please invoke init Tinker Client first");
        }
        return client;
    }

    /**
     * init the Tinker Client, it only effect at first time.
     * you should only invoke once in your app lifecycle
     */
    public static TinkerClient init(Context context,
                                    String appKey,
                                    String appVersion,
                                    Boolean debugMode) {
        if (client == null) {
            synchronized (TinkerClient.class) {
                if (client == null) {
                    client = new TinkerClient.Builder()
                        .appKey(appKey)
                        .appVersion(appVersion)
                        .debug(debugMode)
                        .build();

                    Context applicationContext = context.getApplicationContext();
                    TKClientModule module = new ManifestParser(applicationContext).parse();
                    if (module != null) {
                        module.register(applicationContext, client.registry);
                        client.loader = client.registry.build(TKClientUrl.class, InputStream.class);
                    } else {
                        client.loader = new UrlConnectionUrlLoader();
                    }
                }
            }
        }
        return client;
    }

    @Override
    public void sync(final DataFetcher.DataCallback<String> callback) {
        String url;
        if (client.debug) {
            url = TextUtils.join("/", Arrays.asList(this.host, "dev", this.appKey, this.appVersion));
        } else {
            url = TextUtils.join("/", Arrays.asList(this.host, this.appKey, this.appVersion));
        }

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
            Arrays.asList(this.host, this.appKey, this.appVersion, "file" + patchVersion)
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

    public static class Builder {
        private static final String HOST_URL = "http://q.tinkerpatch.com";
        private String appVersion;
        private String appKey;
        private String host;
        private Boolean debug;

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

        public TinkerClient.Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        void makeDefault() {
            if (TextUtils.isEmpty(host)) {
                this.host = HOST_URL;
            }
            if (TextUtils.isEmpty(this.appKey) || TextUtils.isEmpty(this.appVersion)) {
                throw new RuntimeException("You need setup Appkey and AppVersion");
            }
        }

        public TinkerClient build() {
            makeDefault();
            return new TinkerClient(this.appVersion, this.appKey, this.host, this.debug);
        }
    }
}
