package com.sim.tinyhttpclient.internal;

/**
 * Created by sun on 9/18/16.
 */

public enum RequestAction {

    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    PATCH("PATCH");

    private String action;

    private RequestAction(String canonicalStr) {
        this.action = canonicalStr;
    }

    public String getAction() {
        return this.action;
    }
}
