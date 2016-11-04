package com.tencent.tinker.server.model.request;

import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static com.tencent.tinker.server.utils.Utils.CHARSET;

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

    protected List<Pair<String, String>> toPairList() {
        List<Pair<String, String>> list = new ArrayList<>();
        list.add(new Pair<>("k", appKey));
        list.add(new Pair<>("av", appVersion));
        list.add(new Pair<>("pv", patchVersion));
        list.add(new Pair<>("t", platformType.toString()));
        return list;
    }

    public String toUrlEncodedForm() {
        try {
            return getQuery(toPairList());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String getQuery(List<Pair<String, String>> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Pair<String, String> pair : params) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }
            result.append(URLEncoder.encode(pair.first, CHARSET));
            result.append("=");
            result.append(URLEncoder.encode(pair.second, CHARSET));
        }
        return result.toString();
    }
}
