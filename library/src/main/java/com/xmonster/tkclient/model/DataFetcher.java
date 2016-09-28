package com.xmonster.tkclient.model;

/**
 * @param <T> The type of data to be loaded (InputStream, byte[], File etc).
 */
public interface DataFetcher<T> {

    /**
     * Synchronously fetch data from which a resource can be decoded.
     * <p>
     * <p> This will always be called on
     * background thread so it is safe to perform long running tasks here. Any third party libraries
     * called must be thread safe since this method will be called from a thread in a {@link
     * java.util.concurrent.ExecutorService} that may have more than one background thread. </p>
     * <p>
     * <p> This method will only be called when the corresponding resource is not in the cache. </p>
     * <p>
     * <p> Note - this method will be run on a background thread so blocking I/O is safe. </p>
     *
     * @see #cleanup() where the data retuned will be cleaned up
     */
    void loadData(DataCallback<? super T> callback);

    /**
     * Cleanup or recycle any resources used by this data fetcher. This method will be called in a
     * finally block after the data provided by
     * {@link #loadData(com.xmonster.tkclient.model.DataFetcher.DataCallback)}
     * <p> Note - this method will be run on a background thread so blocking I/O is safe. </p>
     */
    void cleanup();

    void cancel();

    /**
     * Returns the class of the data this fetcher will attempt to obtain.
     */
    Class<T> getDataClass();

    /**
     * Callback that should be called when data has been loaded and is available, or when the load
     * fails.
     *
     * @param <T> The type of data that will be loaded.
     */
    interface DataCallback<T> {
        /**
         * Called with the loaded data if the load succeeded, or with {@code null} if the load failed.
         */
        void onDataReady(T data);

        /**
         * Called when the load fails.
         *
         * @param e a non-null {@link Exception} indicating why the load failed.
         */
        void onLoadFailed(Exception e);
    }
}