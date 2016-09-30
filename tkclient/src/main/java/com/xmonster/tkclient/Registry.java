package com.xmonster.tkclient;

import com.xmonster.tkclient.model.RequestLoader;
import com.xmonster.tkclient.model.RequestLoaderFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by sun on 28/09/2016.
 */

public class Registry {

    private final Set<Entry<?, ?>> loaderFactorySet;

    public Registry() {
        this.loaderFactorySet = new HashSet<>(1);
    }

    public <T, U> Registry register(Class<T> reqClass, Class<U> respClass, RequestLoaderFactory<T, U> factory) {
        Entry<T, U> entry = new Entry<>(reqClass, respClass, factory);
        this.loaderFactorySet.add(entry);
        return this;
    }

    public synchronized <T, U> RequestLoader<T, U> build(Class<T> reqClass, Class<U> respClass) {
        for (Entry<?, ?> entry : loaderFactorySet) {
            if (entry.handles(reqClass, respClass)) {
                return (RequestLoader<T, U>) entry.build();
            }
        }
        return null;
    }

    private static class Entry<Req, Resp> {
        private final Class<Req> reqClass;
        private final Class<Resp> respClass;
        private final RequestLoaderFactory<Req, Resp> factory;

        public Entry(Class<Req> reqClass, Class<Resp> respClass, RequestLoaderFactory<Req, Resp> factory) {
            this.reqClass = reqClass;
            this.respClass = respClass;
            this.factory = factory;
        }

        public boolean handles(Class<?> modelClass, Class<?> dataClass) {
            return handles(modelClass) && this.respClass.isAssignableFrom(dataClass);
        }

        boolean handles(Class<?> modelClass) {
            return this.reqClass.isAssignableFrom(modelClass);
        }

        public RequestLoader<Req, Resp> build() {
            return this.factory.build();
        }
    }
}
