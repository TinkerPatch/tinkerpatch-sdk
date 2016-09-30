package com.xmonster.tkclient.integration.urlconnection;

import com.xmonster.tkclient.model.DataFetcher;
import com.xmonster.tkclient.model.RequestLoader;
import com.xmonster.tkclient.model.TKClientUrl;

import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by sun on 29/09/2016.
 */

public class UrlConnectionUrlLoader implements RequestLoader<TKClientUrl, InputStream> {

    private static final int KEEP_ALIVE_TIME = 60;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private final ThreadPoolExecutor threadPool;

    public UrlConnectionUrlLoader() {
        BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<>();
        threadPool = new ThreadPoolExecutor(
            1,       // Initial pool size
            2,       // Max pool size
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT,
            workQueue
        );
    }

    @Override
    public DataFetcher<InputStream> buildLoadData(TKClientUrl url) {
        return new UrlConnectionStreamFetcher(threadPool, url);
    }
}
