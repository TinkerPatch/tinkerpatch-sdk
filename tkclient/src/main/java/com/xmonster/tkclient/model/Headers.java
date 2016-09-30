package com.xmonster.tkclient.model;

import android.text.TextUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Headers {

    /**
     * A Headers object containing reasonable defaults that should be used when users don't want
     * to provide their own headers.
     */
    public static final Headers DEFAULT = new Headers.Builder().build();

    private final Map<String, String> headers;

    private Headers(Map<String, String> headers) {
        this.headers = new HashMap<>(headers);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    private static class Builder {
        private static final String USER_AGENT_HEADER = "User-Agent";
        private static final String DEFAULT_USER_AGENT = System.getProperty("http.agent");
        private static final String ENCODING_HEADER = "Accept-Encoding";
        private static final String DEFAULT_ENCODING = "identity";
        private static final Map<String, String> DEFAULT_HEADERS;

        static {
            Map<String, String> temp = new HashMap<>(2);
            if (!TextUtils.isEmpty(DEFAULT_USER_AGENT)) {
                temp.put(USER_AGENT_HEADER, DEFAULT_USER_AGENT);
            }
            temp.put(ENCODING_HEADER, DEFAULT_ENCODING);
            DEFAULT_HEADERS = Collections.unmodifiableMap(temp);
        }

        private Map<String, String> headers;

        public Builder setHeader(String key, String value) {
            if (headers == null) {
                headers = new HashMap<>();
            }
            if (!TextUtils.isEmpty(key)) {
                if (value == null && headers.containsKey(key)) {
                    headers.remove(key);
                } else {
                    headers.put(key, value);
                }
            }
            return this;
        }

        public Headers build() {
            if (headers == null || headers.isEmpty()) {
                return new Headers(DEFAULT_HEADERS);
            } else {
                return new Headers(Collections.unmodifiableMap(headers));
            }
        }
    }
}
