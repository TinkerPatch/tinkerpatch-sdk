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

package com.tencent.tinker.server.model.request;


import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.server.utils.ServerUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sun on 24/10/2016.
 */
public class BaseReport {
    public static final String TAG = "Tinker.report";

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

    protected HashMap<String, String> toEncodeObject() {
        HashMap<String, String> values = new HashMap<>();
        values.put("k", appKey);
        values.put("av", appVersion);
        values.put("pv", patchVersion);
        values.put("t", String.valueOf(platformType));
        return values;
    }

    public String toEncodeForm() {
        return getPostDataString(toEncodeObject());
    }

    private String getPostDataString(HashMap<String, String> params) {
        StringBuilder result = new StringBuilder();

        try {
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    result.append('&');
                }
                result.append(URLEncoder.encode(entry.getKey(), ServerUtils.CHARSET));
                result.append('=');
                result.append(URLEncoder.encode(entry.getValue(), ServerUtils.CHARSET));
            }
        } catch (Exception e) {
            TinkerLog.e(TAG, "getPostDataString fail" + e.getMessage());
        }

        return result.toString();
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
