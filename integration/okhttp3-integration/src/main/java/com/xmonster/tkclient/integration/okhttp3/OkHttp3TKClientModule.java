package com.xmonster.tkclient.integration.okhttp3;

import android.content.Context;

import com.xmonster.tkclient.Registry;
import com.xmonster.tkclient.model.TKClientUrl;
import com.xmonster.tkclient.module.TKClientModule;

import java.io.InputStream;

/**
 * A {@link com.xmonster.tkclient.module.TKClientModule} implementation to replace default
 * {@link java.net.HttpURLConnection} based {@link com.xmonster.tkclient.model.RequestLoader}
 * with an OkHttp3 based {@link com.xmonster.tkclient.model.RequestLoader}.
 * <p>
 * <p> If you're using gradle, you can include this module simply by depending on the aar, the
 * module will be merged in by manifest merger. For other build systems or for more more
 * information, see {@link com.xmonster.tkclient.module.TKClientModule}. </p>
 */
public class OkHttp3TKClientModule implements TKClientModule {

    @Override
    public void register(Context context, Registry registry) {
        registry.register(TKClientUrl.class, InputStream.class, new OkHttp3UrlLoader.Factory());
    }
}
