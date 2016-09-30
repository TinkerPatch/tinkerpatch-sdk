package com.xmonster.tkclient.model;

import android.net.Uri;
import android.text.TextUtils;

import com.xmonster.tkclient.utils.Preconditions;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sun on 28/09/2016.
 */

public class TKClientUrl {

    private static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
    private static final String CHARSET = "UTF-8";

    private final Headers headers;
    private final String stringUrl;
    private final String body;
    private final String method;

    private String safeStringUrl;
    private URL safeUrl;

    public TKClientUrl(String stringUrl, Headers headers, String body, String method) {
        this.stringUrl = Preconditions.checkNotEmpty(stringUrl);
        this.method = Preconditions.checkNotEmpty(method);
        this.headers = headers;
        this.body = body;
    }

    public URL toURL() throws MalformedURLException {
        return getSafeUrl();
    }

    public String toStringUrl() {
        return getSafeStringUrl();
    }

    public Map<String, String> getHeaders() {
        return headers.getHeaders();
    }

    public String getMethod() {
        return method;
    }

    public String getBody() {
        return body;
    }

    // See http://stackoverflow.com/questions/3286067/url-encoding-in-android. Although the answer
    // using URI would work, using it would require both decoding and encoding each string which is
    // more complicated, slower and generates more objects than the solution below. See also issue
    // #133.
    private URL getSafeUrl() throws MalformedURLException {
        if (safeUrl == null) {
            safeUrl = new URL(getSafeStringUrl());
        }
        return safeUrl;
    }

    private String getSafeStringUrl() {
        if (TextUtils.isEmpty(safeStringUrl)) {
            safeStringUrl = Uri.encode(stringUrl, ALLOWED_URI_CHARS);
        }
        return safeStringUrl;
    }

    public static class Builder {
        private String url;
        private HashMap<String, String> params;
        private String body;
        private String method;
        private Headers headers;

        static String urlEncode(String s, String charset) throws UnsupportedEncodingException {
            return URLEncoder.encode(s, charset);
        }

        static String urlEncode(Map<?, ?> map, String charset) {
            if (map == null) {
                return "";
            }

            StringBuilder sb = new StringBuilder();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (sb.length() > 0) {
                    sb.append('&');
                }
                try {
                    sb.append(String.format("%s=%s",
                        urlEncode(entry.getKey().toString(), charset),
                        urlEncode(entry.getValue().toString(), charset)
                    ));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }

        public TKClientUrl.Builder url(String url) {
            this.url = url;
            return this;
        }

        public TKClientUrl.Builder param(String key, Object value) {
            if (params == null) {
                this.params = new HashMap<>();
            }
            this.params.put(key, String.valueOf(value));
            return this;
        }

        public TKClientUrl.Builder body(String body) {
            this.body = body;
            return this;
        }

        public TKClientUrl.Builder method(String method) {
            switch (method) {
                case "GET":
                case "POST":
                    this.method = method;
                    break;
                default:
                    throw new RuntimeException("Didn't Supported Method, Please pass the correct method");
            }
            return this;
        }

        public TKClientUrl.Builder headers(Headers headers) {
            this.headers = headers;
            return this;
        }

        private void makeDefault() {
            if (TextUtils.isEmpty(this.method)) {
                this.method = "GET";
            }
            if (this.headers == null) {
                this.headers = Headers.DEFAULT;
            }
            if (this.params != null) {
                this.url = String.format("%s?%s", url, urlEncode(this.params, CHARSET));
            }
        }

        public TKClientUrl build() {
            makeDefault();
            return new TKClientUrl(this.url, this.headers, this.body, this.method);
        }
    }
}
