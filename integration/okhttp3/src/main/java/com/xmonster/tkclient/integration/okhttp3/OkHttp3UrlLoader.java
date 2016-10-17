package com.xmonster.tkclient.integration.okhttp3;

import com.xmonster.tkclient.model.DataFetcher;
import com.xmonster.tkclient.model.RequestLoader;
import com.xmonster.tkclient.model.RequestLoaderFactory;
import com.xmonster.tkclient.model.TKClientUrl;

import java.io.InputStream;

import okhttp3.Call;
import okhttp3.OkHttpClient;

/**
 * A simple model loader for fetching media over http/https using OkHttp.
 */
public class OkHttp3UrlLoader implements RequestLoader<TKClientUrl, InputStream> {

    private final Call.Factory client;

    public OkHttp3UrlLoader(Call.Factory client) {
        this.client = client;
    }

    @Override
    public DataFetcher<InputStream> buildLoadData(TKClientUrl tkUrl) {
        return new OkHttp3StreamFetcher(client, tkUrl);
    }

    /**
     * The default factory for {@link OkHttp3UrlLoader}s.
     */
    public static class Factory implements RequestLoaderFactory<TKClientUrl, InputStream> {
        private static volatile Call.Factory internalClient;
        private Call.Factory client;

        /**
         * Constructor for a new Factory that runs requests using a static singleton client.
         */
        public Factory() {
            this(getInternalClient());
        }

        /**
         * Constructor for a new Factory that runs requests using given client.
         *
         * @param client this is typically an instance of {@code OkHttpClient}.
         */
        public Factory(Call.Factory client) {
            this.client = client;
        }

        private static Call.Factory getInternalClient() {
            if (internalClient == null) {
                synchronized (Factory.class) {
                    if (internalClient == null) {
                        internalClient = new OkHttpClient();
                    }
                }
            }
            return internalClient;
        }

        @Override
        public RequestLoader<TKClientUrl, InputStream> build() {
            return new OkHttp3UrlLoader(client);
        }
    }
}
