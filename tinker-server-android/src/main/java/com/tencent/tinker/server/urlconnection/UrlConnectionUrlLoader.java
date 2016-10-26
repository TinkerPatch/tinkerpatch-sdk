package com.tencent.tinker.server.urlconnection;
import com.tencent.tinker.server.model.DataFetcher;
import com.tencent.tinker.server.model.TinkerClientUrl;

import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UrlConnectionUrlLoader {

    private final Executor executor;

    public UrlConnectionUrlLoader() {
        executor = Executors.newSingleThreadExecutor();
    }

    public DataFetcher<InputStream> buildLoadData(TinkerClientUrl url) {
        return new UrlConnectionStreamFetcher(executor, url);
    }
}
