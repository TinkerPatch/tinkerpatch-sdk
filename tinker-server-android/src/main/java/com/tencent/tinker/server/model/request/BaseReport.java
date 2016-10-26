package com.tencent.tinker.server.model.request;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sun on 24/10/2016.
 */
public class BaseReport {
    public final String appKey;
    public final String appVersion;
    public final String patchVersion;
    public final Integer platformType;

    public BaseReport(String appKey, String appVersion, String patchVersion) {
        this.appKey = appKey;
        this.appVersion = appVersion;
        this.patchVersion = patchVersion;
        this.platformType = 1;
    }

    protected JSONObject toJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("k", appKey);
        jsonObject.put("av", appVersion);
        jsonObject.put("pv", patchVersion);
        jsonObject.put("t", platformType);
        return jsonObject;
    }

    public String toJson() {
        try {
            return toJsonObject().toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
}
