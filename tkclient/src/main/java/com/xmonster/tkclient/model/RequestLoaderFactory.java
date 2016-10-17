package com.xmonster.tkclient.model;

public interface RequestLoaderFactory<T, Y> {

    /**
     * Build a concrete ModelLoader for this model type.
     *
     * @return A new {@link RequestLoader}
     */
    RequestLoader<T, Y> build();
}
