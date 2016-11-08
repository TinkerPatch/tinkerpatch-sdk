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

package com.tinker.debug;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * Created by shwenzhang on 16/11/4.
 */

public class Utils {
    private static final String TAG       = "Tinker.Utils";

    public static final  String AUTHORITY = "com.tinker.debug.debugprovider";

    public static final Uri    CONTENT_URI  = Uri.parse("content://" + AUTHORITY + "/config");
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tencent.mm.debug";

    public static final String KEY = "key";
    public static final String TYPE = "type";
    public static final String VALUE = "value";

    public static final int TYPE_INT = 1;
    public static final int TYPE_LONG = 2;
    public static final int TYPE_STRING = 3;
    public static final int TYPE_BOOLEAN = 4;
    public static final int TYPE_FLOAT = 5;
    public static final int TYPE_DOUBLE = 6;


    public static void setValue(final ContentResolver resolver, final String key, final Object value, final int Type) {
        final ContentValues values = new ContentValues();
        values.put(Utils.KEY, key);
        values.put(Utils.TYPE, Type);
        values.put(Utils.VALUE, "" + (value == null ? "" : value.toString()));

        final Cursor cu = resolver.query(Utils.CONTENT_URI, null, Utils.KEY + " = ?", new String[] { key }, null);
        if (cu == null) {
            Log.e(TAG, "failed set value: key=" + key + ", value=" + value);
            return;
        }

        if (cu.getCount() <= 0) {
            resolver.insert(Utils.CONTENT_URI, values);
            Log.d(TAG, "insert value: key=" + key + ", value=" + value);

        } else {
            resolver.update(Utils.CONTENT_URI, values, Utils.KEY + " = ?", new String[] { key });
            Log.d(TAG, "update value: key=" + key + ", value=" + value);
        }
        cu.close();
    }
}
