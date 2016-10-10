package com.xmonster.tkclient.model.response;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sun on 10/10/2016.
 */

public class SyncResponse {
    public final String version;
    public final Integer grayValue;

    private static final String KEY_VERSION = "v";
    private static final String KEY_GRAY = "g";

    private SyncResponse(String version, Integer grayValue) {
        this.version = version;
        if (grayValue == 0) {
            this.grayValue = null;
        } else {
            this.grayValue = grayValue;
        }

    }

    public static SyncResponse fromJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            String version = jsonObject.getString(KEY_VERSION);
            Integer grayValue = jsonObject.optInt(KEY_GRAY);
            return new SyncResponse(version, grayValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
