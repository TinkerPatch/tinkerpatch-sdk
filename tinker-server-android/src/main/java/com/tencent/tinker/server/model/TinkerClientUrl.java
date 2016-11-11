/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2016 Shengjie Sim Sun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.tencent.tinker.server.model;

import android.net.Uri;
import android.text.TextUtils;

import com.tencent.tinker.server.utils.Preconditions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class TinkerClientUrl {

    private static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";

    private final Headers headers;
    private final String  stringUrl;
    private final String  body;
    private final String  method;

    private String safeStringUrl;
    private URL    safeUrl;

    public TinkerClientUrl(String stringUrl, Headers headers, String body, String method) {
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
        private String                  url;
        private HashMap<String, String> params;
        private String                  body;
        private String                  method;
        private Headers                 headers;

        public TinkerClientUrl.Builder url(String url) {
            this.url = url;
            return this;
        }

        public TinkerClientUrl.Builder param(String key, Object value) {
            if (params == null) {
                this.params = new HashMap<>();
            }
            this.params.put(key, String.valueOf(value));
            return this;
        }

        public TinkerClientUrl.Builder body(String body) {
            this.body = body;
            return this;
        }

        public TinkerClientUrl.Builder method(String method) {
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

        public TinkerClientUrl.Builder headers(Headers headers) {
            this.headers = headers;
            return this;
        }

        private void makeDefault() {
            Uri.Builder urlBuilder = Uri.parse(this.url).buildUpon();
            if (TextUtils.isEmpty(this.method)) {
                this.method = "GET";
            }
            if (this.headers == null) {
                this.headers = Headers.DEFAULT;
            }
            if (this.params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    urlBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
                }
            }
            this.url = urlBuilder.build().toString();
        }

        public TinkerClientUrl build() {
            makeDefault();
            return new TinkerClientUrl(this.url, this.headers, this.body, this.method);
        }
    }
}
