package com.xmonster.tkclient.module;

import android.content.Context;

import com.xmonster.tkclient.Registry;

/**
 * Created by sun on 28/09/2016.
 */

public interface TKClientModule {
    /**
     * <p> This method will be called once and only once per implementation. </p>
     *
     * @param context  An Application {@link android.content.Context}.
     * @param registry An {@link com.xmonster.tkclient.Registry} to use to register components.
     */
    void register(Context context, Registry registry);
}
