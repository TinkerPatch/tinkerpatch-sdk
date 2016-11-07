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

package com.tencent.tinker.server.utils;

/**
 * Created by zhangshaowen on 16/11/4.
 */

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.tencent.tinker.lib.util.TinkerLog;

import java.util.HashMap;


public final class Debugger {

    private static final String TAG             = "Tinker.Debugger";

    public static final Uri    CONTENT_URI  = Uri.parse("content://com.tinker.debug.debugprovider/config");

    public static final String KEY = "key";
    public static final String TYPE = "type";
    public static final String VALUE = "value";

    public static final int TYPE_INT = 1;
    public static final int TYPE_LONG = 2;
    public static final int TYPE_STRING = 3;
    public static final int TYPE_BOOLEAN = 4;
    public static final int TYPE_FLOAT = 5;
    public static final int TYPE_DOUBLE = 6;


    private final String[] columns = {BaseColumns._ID, KEY, TYPE, VALUE};

    private final HashMap<String, Object> values = new HashMap<>();


    private static Debugger sDebugger;

    public static Debugger getInstance(Context context) {
        if (sDebugger == null) {
            sDebugger = new Debugger(context);
        }
        return sDebugger;
    }

    private Debugger(final Context context) {
        final ContentResolver cr = context.getContentResolver();
        Cursor cu;
        try {
            cu = cr.query(CONTENT_URI, columns, null, null, null);
        } catch (Exception e) {
            TinkerLog.e(TAG, "Get contentProvider error", e);
            cu = null;
        }

        if (cu == null) {
            TinkerLog.w(TAG, "debugger not attached cu == null");
            return;
        }

        if (cu.getCount() <= 0) {
            TinkerLog.w(TAG, "debugger not attached cu size == 0");
            cu.close();
            return;
        }
        TinkerLog.w(TAG, "debugger attached");

        final int keyIdx = cu.getColumnIndex("key");
        final int typeIdx = cu.getColumnIndex("type");
        final int valueIdx = cu.getColumnIndex("value");

        while (cu.moveToNext()) {
            final Object obj = Resolver.resolveObj(cu.getInt(typeIdx), cu.getString(valueIdx));
            values.put(cu.getString(keyIdx), obj);
        }
        cu.close();
    }

    public boolean isDebug() {
        Boolean debug = getBoolean(".com.tinker.debugtool.debug");
        if (debug == null) {
            return false;
        }
        return debug;
    }



    public String getString(final String key) {
        final Object obj = values.get(key);
        if (obj instanceof String) {
            TinkerLog.d(TAG, "getString(): key=" + key + ", value=" + obj.toString());
            return (String) obj;
        }

        return null;
    }

    public Integer getInteger(final String key) {
        final Object obj = values.get(key);
        if (obj instanceof Integer) {
            TinkerLog.d(TAG, "getInteger(): key=" + key + ", value=" + obj.toString());
            return (Integer) obj;
        }

        return null;
    }

    public Long getLong(final String key) {
        final Object obj = values.get(key);
        if (obj instanceof Long) {
            TinkerLog.d(TAG, "getLong(): key=" + key + ", value=" + obj.toString());
            return (Long) obj;
        }

        return null;
    }

    public Boolean getBoolean(final String key) {
        final Object obj = values.get(key);
        if (obj == null) {
            return false;
        }

        if (obj instanceof Boolean) {
            TinkerLog.d(TAG, "getBoolean(): key=" + key + ", value=" + obj.toString());
            return (Boolean) obj;
        }
        return false;
    }

    public static final class Resolver {
        private static final String TAG = "Tinker.Debugger.Resolver";

        private Resolver() {

        }

        public static Object resolveObj(int type, String value) {

            try {
                switch (type) {
                    case TYPE_INT:
                        return Integer.valueOf(value);

                    case TYPE_LONG:
                        return Long.valueOf(value);

                    case TYPE_STRING:
                        return value;

                    case TYPE_BOOLEAN:
                        return Boolean.valueOf(value);

                    case TYPE_FLOAT:
                        return Float.valueOf(value);

                    case TYPE_DOUBLE:
                        return Double.valueOf(value);

                    default:
                        TinkerLog.e(TAG, "unknown type");
                        break;
                }

            } catch (Exception e) {
                TinkerLog.printErrStackTrace(TAG, e, "");
            }
            return null;
        }
    }
}
