package com.xmonster.tkclient.model;


public interface RequestLoader<T, R> {
    DataFetcher<R> buildLoadData(T model);
}
