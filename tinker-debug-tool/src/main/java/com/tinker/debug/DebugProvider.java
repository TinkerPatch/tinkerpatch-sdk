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

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by shwenzhang on 16/11/4.
 */

public class DebugProvider extends ContentProvider {
    private static final String TAG       = "Tinker.DebugProvider";



    private static final int    DATABASE_VERSION   = 1;
    private static final String DATABASE_NAME      = "debugtool.db";
    private static final String DEBUG_CONFIG_TABLE = "config";
    private static final int    DEBUG_CONFIG_CODE  = 1;

    private static final UriMatcher              sUriMatcher;
    private static       HashMap<String, String> projectionMap;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Utils.AUTHORITY, DEBUG_CONFIG_TABLE, DEBUG_CONFIG_CODE);

        projectionMap = new HashMap<>();
        projectionMap.put(BaseColumns._ID, BaseColumns._ID);
        projectionMap.put(Utils.KEY, Utils.KEY);
        projectionMap.put(Utils.TYPE, Utils.TYPE);
        projectionMap.put(Utils.VALUE, Utils.VALUE);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DEBUG_CONFIG_TABLE + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + Utils.KEY + " TEXT," + Utils.TYPE
                + " INTEGER," + Utils.VALUE + " TEXT);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DEBUG_CONFIG_TABLE);
            onCreate(db);
        }
    }

    private DatabaseHelper dbHelper;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case DEBUG_CONFIG_CODE:
                count = db.delete(DEBUG_CONFIG_TABLE, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case DEBUG_CONFIG_CODE:
                return Utils.CONTENT_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (sUriMatcher.match(uri) != DEBUG_CONFIG_CODE) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(DEBUG_CONFIG_TABLE, Utils.KEY, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(Utils.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case DEBUG_CONFIG_CODE:
                qb.setTables(DEBUG_CONFIG_TABLE);
                qb.setProjectionMap(projectionMap);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case DEBUG_CONFIG_CODE:
                count = db.update(DEBUG_CONFIG_TABLE, values, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
