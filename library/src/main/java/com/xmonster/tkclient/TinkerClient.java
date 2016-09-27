package com.xmonster.tkclient;

/**
 * Created by sun on 27/09/2016.
 */

public class TinkerClient implements TKClientFactory {

    private final String appVersion;
    private final String appKey;
    private final String host;

    private TinkerClient(
            final String appVersion,
            final String appKey,
            final String host
    ) {
        this.appVersion = appVersion;
        this.appKey = appKey;
        this.host = host;
    }

    @Override
    public String sync() {
        return "";
    }

    @Override
    public boolean download(String patchVersion, String downFilePath) {
        return true;
    }

    public static class Builder {
        private String appVersion;
        private String appKey;
        private String host;

        public TinkerClient.Builder host(String host) {
            this.host = host;
            return this;
        }

        public TinkerClient.Builder appKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        public TinkerClient.Builder appVersion(String appVersion) {
            this.appVersion = appVersion;
            return this;
        }
    }
}
