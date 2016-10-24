package com.xmonster.tkclient.model.request;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sun on 24/10/2016.
 */

public class SuccessReport extends BaseReport {
    public SuccessReport(String appKey, String appVersion, String patchVersion) {
        super(appKey, appVersion, patchVersion);
    }


}
