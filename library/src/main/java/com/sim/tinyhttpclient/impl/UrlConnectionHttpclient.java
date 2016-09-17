package com.sim.tinyhttpclient.impl;

import android.support.annotation.NonNull;

import com.sim.tinyhttpclient.internal.RequestAction;
import com.sim.tinyhttpclient.util.FileUtils;
import com.sim.tinyhttpclient.util.MapQuery;
import com.sim.tinyhttpclient.util.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Created by sun on 9/18/16.
 */

public class UrlConnectionHttpclient implements IClient {

    private final String charset;
    private final String userAgent;
    private final int readTimeout;
    private final int connectTimeout;
    private final String accept;
    // TODO: currently we didn't support keep alive
    private final boolean isKeepAlive;

    private UrlConnectionHttpclient(
            final String charset,
            final String userAgent,
            final boolean isKeepAlive,
            final int readTimeout,
            final int connectTimeout,
            final String accept
    ) {
        this.charset = charset;
        this.userAgent = userAgent;
        this.isKeepAlive = isKeepAlive;
        this.readTimeout = readTimeout;
        this.connectTimeout = connectTimeout;
        this.accept = accept;
    }

    public String Get(String pathUrl, Map<String, String> params) throws IOException {
        URL url = buildUrl(pathUrl, params);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(RequestAction.GET.getAction());
        return launchConnection(conn);
    }

    public File GetFile(String pathUrl, Map<String, String> params, String filePath) throws IOException {
        URL url = buildUrl(pathUrl, params);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        return launchConnection(conn, filePath);
    }

    public String Post(String pathUrl, Map<String, String> params) throws IOException {
        return Post(pathUrl, params, "");
    }

    public String Post(String pathUrl, Map<String, String> params, String body) throws IOException {
        URL url = buildUrl(pathUrl, params);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.setRequestMethod(RequestAction.POST.getAction());
        conn.setRequestProperty("Content-Type", String.format("application/json; charset=%s", charset));
        writeToRequest(conn, body);
        return launchConnection(conn);
    }

    public String Post(String pathUrl, Map<String, String> params, Map<String, String> urlForm) throws IOException {
        URL url = buildUrl(pathUrl, params);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.setRequestMethod(RequestAction.POST.getAction());
        conn.setRequestProperty("Content-Type", String.format("application/x-www-form-urlencoded; charset=%s", charset));
        writeToRequest(conn, urlForm);
        return launchConnection(conn);
    }

    @NonNull
    private URL buildUrl(String pathUrl, Map<String, String> params) throws UnsupportedEncodingException, MalformedURLException {
        String paramsStr = MapQuery.urlEncode(params, charset);
        String requestUrl = String.format("%s?%s", pathUrl, paramsStr);
        return new URL(requestUrl);
    }

    private void writeToRequest(HttpURLConnection conn, Map<String, String> urlForm) throws IOException {
        String body = MapQuery.urlEncode(urlForm, charset);
        writeToRequest(conn, body);
    }

    private void writeToRequest(HttpURLConnection conn, String body) throws IOException {
        if (StringUtils.isBlank(body)) return;
        OutputStream outputStream = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset));
        writer.write(body);
        writer.flush();
        writer.close();
        outputStream.close();
    }

    private String launchConnection(HttpURLConnection conn) throws IOException {
        try {
            initDefaultConn(conn);
            conn.connect();
            return StringUtils.readStream(conn.getInputStream());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private File launchConnection(HttpURLConnection conn, String filePath) throws IOException {
        try {
            initDefaultConn(conn);
            conn.connect();
            return FileUtils.readStream(conn.getInputStream(), filePath);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void initDefaultConn(HttpURLConnection conn) {
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestProperty("charset", charset);
        conn.setRequestProperty("User-Agent", userAgent);
        conn.setRequestProperty("Accept", accept);
        conn.setUseCaches(false);
        conn.setReadTimeout(readTimeout);
        conn.setConnectTimeout(connectTimeout);
    }

    public static class Builder {
        private String charset = "UTF-8";
        private String userAgent;
        private boolean isKeepAlive = false;
        private int connectTimeout = 15 * 1000;
        private int readTimeout = 15 * 1000;
        private String accept = "application/json";

        public Builder charset(String charset) {
            this.charset = charset;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder keepAlive(boolean isKeepAlive) {
            this.isKeepAlive = isKeepAlive;
            return this;
        }

        public Builder readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder accept(String accept) {
            this.accept = accept;
            return this;
        }

        public UrlConnectionHttpclient build() {
            if (StringUtils.isBlank(userAgent)) {
                userAgent = System.getProperty("http.agent");
                userAgent += ";tinyhttpclient";
            }
            return new UrlConnectionHttpclient(
                    charset,
                    userAgent,
                    isKeepAlive,
                    readTimeout,
                    connectTimeout,
                    accept
            );
        }
    }
}
