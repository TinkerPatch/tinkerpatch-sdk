package com.tencent.tinker.server.model.request;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sun on 24/10/2016.
 */
public class FailReport extends BaseReport {
    public Integer errCode;

    public FailReport(String appKey, String appVersion, String patchVersion, Integer errCode) {
        super(appKey, appVersion, patchVersion);
        this.errCode = errCode;
    }

    @Override
    protected JSONObject toJsonObject() throws JSONException {
        JSONObject jsonObject = super.toJsonObject();
        jsonObject.put("code", errCode);
        return jsonObject;
    }
}
