package com.xmonster.tkclient.integration.urlconnection;

import com.xmonster.tkclient.model.DataFetcher;
import com.xmonster.tkclient.model.RequestLoader;
import com.xmonster.tkclient.model.TKClientUrl;

import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UrlConnectionUrlLoader implements RequestLoader<TKClientUrl, InputStream> {

    private final Executor executor;

    public UrlConnectionUrlLoader() {
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public DataFetcher<InputStream> buildLoadData(TKClientUrl url) {
        return new UrlConnectionStreamFetcher(executor, url);
    }
}
