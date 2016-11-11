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

package com.tencent.tinker.server.model.response;

import org.json.JSONException;
import org.json.JSONObject;


public final class SyncResponse {

    private static final String KEY_VERSION    = "v";
    private static final String KEY_GRAY       = "g";
    private static final String KEY_CONDITIONS = "c";
    private static final String KEY_PAUSE      = "p";
    private static final String KEY_ROLLBACK   = "e";

    public final String  version;
    public final Integer grayValue;
    public final String  conditions;
    public final Boolean isPaused;
    public final Boolean isRollback;

    private SyncResponse(String version, Integer grayValue, String conditions, Boolean pause, Boolean rollback) {
        this.version = version;
        this.conditions = conditions;
        this.isPaused = pause;
        this.isRollback = rollback;
        if (grayValue == 0) {
            this.grayValue = null;
        } else {
            this.grayValue = grayValue;
        }
    }

    public static SyncResponse fromJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            String version = jsonObject.optString(KEY_VERSION);
            String conditions = jsonObject.optString(KEY_CONDITIONS);
            Integer grayValue = jsonObject.optInt(KEY_GRAY);
            Integer pauseFlag = jsonObject.optInt(KEY_PAUSE);
            Integer rollbackFlag = jsonObject.optInt(KEY_ROLLBACK);

            return new SyncResponse(version, grayValue, conditions, pauseFlag == 1, rollbackFlag == 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "version:" + version + "\ngrayValue:" + grayValue + "\nconditions:" + conditions
            + "\npause:" + isPaused + "\nrollback:" + isRollback;
    }
}
