package com.xmonster.tkclient.integration.urlconnection;

import com.xmonster.tkclient.model.DataFetcher;
import com.xmonster.tkclient.model.RequestLoader;
import com.xmonster.tkclient.model.TKClientUrl;

import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UrlConnectionUrlLoader implements RequestLoader<TKClientUrl, InputStream> {

    private static final int KEEP_ALIVE_TIME = 60;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private final Executor executor;

    public UrlConnectionUrlLoader() {
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public DataFetcher<InputStream> buildLoadData(TKClientUrl url) {
        return new UrlConnectionStreamFetcher(executor, url);
    }
}
