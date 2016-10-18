package com.xmonster.tkclient.model.response;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sun on 10/10/2016.
 */

public final class SyncResponse {

    public final String version;
    public final Integer grayValue;
    public final String conditions;
    public final Boolean isPaused;

    private static final String KEY_VERSION = "v";
    private static final String KEY_GRAY = "g";
    private static final String KEY_CONDITIONS = "c";
    private static final String KEY_PAUSE = "p";

    private SyncResponse(String version, Integer grayValue, String conditions, Boolean pause) {
        this.version = version;
        this.conditions = conditions;
        this.isPaused = pause;
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
            String conditions = jsonObject.optString(KEY_CONDITIONS);
            Integer grayValue = jsonObject.optInt(KEY_GRAY);
            Integer pauseFlag = jsonObject.optInt(KEY_PAUSE);
            return new SyncResponse(version, grayValue, conditions, pauseFlag == 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "version:" + version + " grayValue:" + grayValue + " conditions:" + conditions
            + " pause:" + isPaused;
    }
}
