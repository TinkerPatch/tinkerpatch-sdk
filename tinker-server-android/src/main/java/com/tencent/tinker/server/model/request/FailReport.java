package com.tencent.tinker.server.model.request;

import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by sun on 24/10/2016.
 */
public class FailReport extends BaseReport {
    public final Integer errCode;

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

    @Override
    protected List<Pair<String, String>> toPairList() {
        List<Pair<String, String>> list = super.toPairList();
        list.add(new Pair<>("code", errCode.toString()));
        return list;
    }
}
