package com.xmonster.tkclient;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.xmonster.tkclient.integration.urlconnection.UrlConnectionUrlLoader;
import com.xmonster.tkclient.model.DataFetcher;
import com.xmonster.tkclient.model.RequestLoader;
import com.xmonster.tkclient.model.TKClientUrl;
import com.xmonster.tkclient.model.response.SyncResponse;
import com.xmonster.tkclient.module.ManifestParser;
import com.xmonster.tkclient.module.TKClientModule;
import com.xmonster.tkclient.utils.Conditions;
import com.xmonster.tkclient.utils.Installation;
import com.xmonster.tkclient.utils.Preconditions;
import com.xmonster.tkclient.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TinkerClient implements TKClientAPI {

    private static volatile TinkerClient client;
    private final String appVersion;
    private final String appKey;
    private final String host;
    private final boolean debug;
    private final Registry registry;
    private RequestLoader<TKClientUrl, InputStream> loader;
    final Conditions conditions;

    TinkerClient(String appVersion, String appKey, String host, Boolean debug, Conditions conditions) {
        this.appVersion = appVersion;
        this.appKey = appKey;
        this.host = host;
        this.debug = debug;
        this.registry = new Registry();
        this.conditions = conditions;
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
                    client = new Builder()
                        .appKey(appKey)
                        .appVersion(appVersion)
                        .debug(debugMode)
                        .conditions(context)
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

    public TinkerClient params(String key, String value) {
        this.conditions.set(key, value);
        return this;
    }

    public void save(Context context) {
        try {
            this.conditions.save(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(
        final Context context,
        final String patchVersion,
        final String filePath,
        final DataFetcher.DataCallback<? super File> callback) {

        if (callback == null) {
            throw new RuntimeException("callback can't be null");
        }

        sync(context, new DataFetcher.DataCallback<String>() {
            @Override
            public void onDataReady(String data) {
                SyncResponse response = SyncResponse.fromJson(data);
                if (response == null) {
                    callback.onLoadFailed(new RuntimeException("Can't sync with version: response == null"));
                } else {

                    DataFetcher.DataCallback downloadCallback = new DataFetcher.DataCallback<File>() {
                        @Override
                        public void onDataReady(File data) {
                            callback.onDataReady(data);
                        }

                        @Override
                        public void onLoadFailed(Exception e) {
                            callback.onLoadFailed(e);
                        }
                    };
                    /**
                     * 在SDK启动时检测灰度值有没有生成，若没有，从1-10随机选择一个数，保存起来作为这个设备的灰度值。
                     * 若是灰度下发，请求返回的json会有g字段，值是1-10，例如{v:5, g:2}。
                     * 这里g字段的值大于设备的灰度值就命中灰度，否则不命中
                     */
                    if ((response.grayValue == null || Installation.grayValue(context) >= response.grayValue)
                        && conditions.check("")) {
                        download(context, patchVersion, filePath, downloadCallback);
                    }
                }
            }

            @Override
            public void onLoadFailed(Exception e) {
                callback.onLoadFailed(e);
            }
        });
    }

    @Override
    public void sync(final Context context, final DataFetcher.DataCallback<String> callback) {
        Uri.Builder urlBuilder = Uri.parse(this.host).buildUpon();
        if (client.debug) {
            urlBuilder.appendPath("dev");
        }
        final String url = urlBuilder.appendPath(this.appKey)
            .appendPath(this.appVersion)
            .appendQueryParameter("d", Installation.id(context))
            .appendQueryParameter("v", String.valueOf(System.currentTimeMillis()))
            .build().toString();

        TKClientUrl tkClientUrl = new TKClientUrl.Builder().url(url).build();

        final DataFetcher<InputStream> dataFetcher = loader.buildLoadData(tkClientUrl);
        dataFetcher.loadData(new DataFetcher.DataCallback<InputStream>() {
            @Override
            public void onDataReady(InputStream data) {
                if (callback == null) {
                    return;
                }
                try {
                    String response = Utils.readStreamToString(data, Config.CHARSET);
                    SyncResponse.fromJson(response);
                    callback.onDataReady(response);
                } catch (IOException e) {
                    callback.onLoadFailed(e);
                } finally {
                    dataFetcher.cleanup();
                }
            }

            @Override
            public void onLoadFailed(Exception e) {
                if (callback == null) {
                    return;
                }
                try {
                    callback.onLoadFailed(e);
                } finally {
                    dataFetcher.cleanup();
                }
            }
        });
    }

    @Override
    public void download(final Context context,
                         final String patchVersion,
                         final String filePath,
                         final DataFetcher.DataCallback<? super File> callback) {

        Preconditions.checkNotEmpty(patchVersion);
        final String url = Uri.parse(this.host)
            .buildUpon()
            .appendPath(this.appKey)
            .appendPath(this.appVersion)
            .appendPath(String.format("file%s", patchVersion))
            .appendQueryParameter("d", Installation.id(context))
            .appendQueryParameter("v", String.valueOf(System.currentTimeMillis()))
            .build().toString();

        TKClientUrl tkClientUrl = new TKClientUrl.Builder().url(url).build();

        final DataFetcher<InputStream> dataFetcher = loader.buildLoadData(tkClientUrl);
        dataFetcher.loadData(new DataFetcher.DataCallback<InputStream>() {
            @Override
            public void onDataReady(InputStream data) {
                if (callback == null) {
                    return;
                }
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
                if (callback == null) {
                    return;
                }

                try {
                    callback.onLoadFailed(e);
                } finally {
                    dataFetcher.cleanup();
                }
            }
        });
    }

    static class Builder {
        private static final String HOST_URL = "http://q.tinkerpatch.com";
        private String appVersion;
        private String appKey;
        private String host;
        private Boolean debug;
        private Conditions conditions;

        TinkerClient.Builder host(String host) {
            this.host = host;
            return this;
        }

        TinkerClient.Builder appKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        TinkerClient.Builder appVersion(String appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        TinkerClient.Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        TinkerClient.Builder conditions(Context context) {
            this.conditions = new Conditions(context);
            return this;
        }

        void makeDefault() {
            if (TextUtils.isEmpty(host)) {
                this.host = HOST_URL;
            }
            if (TextUtils.isEmpty(this.appKey) || TextUtils.isEmpty(this.appVersion)) {
                throw new RuntimeException("You need setup Appkey and AppVersion");
            }
            if (this.conditions == null) {
                throw new RuntimeException("You need init conditions property");
            }
        }

        public TinkerClient build() {
            makeDefault();
            return new TinkerClient(this.appVersion, this.appKey, this.host, this.debug, this.conditions);
        }
    }
}
