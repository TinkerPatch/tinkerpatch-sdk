package com.xmonster.tkclient.integration.okhttp;

import com.squareup.okhttp.OkHttpClient;
import com.xmonster.tkclient.model.DataFetcher;
import com.xmonster.tkclient.model.RequestLoader;
import com.xmonster.tkclient.model.RequestLoaderFactory;
import com.xmonster.tkclient.model.TKClientUrl;

import java.io.InputStream;

/**
 * A simple model loader for fetching media over http/https using OkHttp.
 */
public class OkHttpUrlLoader implements RequestLoader<TKClientUrl, InputStream> {

    private final OkHttpClient client;

    public OkHttpUrlLoader(OkHttpClient client) {
        this.client = client;
    }

    @Override
    public DataFetcher<InputStream> buildLoadData(TKClientUrl tkUrl) {
        return new OkHttpStreamFetcher(client, tkUrl);
    }

    /**
     * The default factory for {@link OkHttpUrlLoader}s.
     */
    public static class Factory implements RequestLoaderFactory<TKClientUrl, InputStream> {
        private static volatile OkHttpClient internalClient;
        private OkHttpClient client;

        /**
         * Constructor for a new Factory that runs requests using a static singleton client.
         */
        public Factory() {
            this(getInternalClient());
        }

        /**
         * Constructor for a new Factory that runs requests using given client.
         */
        public Factory(OkHttpClient client) {
            this.client = client;
        }

        private static OkHttpClient getInternalClient() {
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
            return new OkHttpUrlLoader(client);
        }
    }
}