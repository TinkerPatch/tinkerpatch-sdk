package com.xmonster.tkclient;

import com.xmonster.tkclient.model.RequestLoaderFactory;

/**
 * Created by sun on 28/09/2016.
 */

public class Registry {
    private RequestLoaderFactory loaderFactory;

    public Registry() {
        this.loaderFactory = null;
    }

    public void setLoaderFactory(RequestLoaderFactory factory) {
        this.loaderFactory = factory;
    }
}
