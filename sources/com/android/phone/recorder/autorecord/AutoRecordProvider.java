package com.android.phone.recorder.autorecord;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class AutoRecordProvider extends ContentProvider {
    protected static final UriMatcher URI_MATCHER = new UriMatcher(-1);
    protected SQLiteDatabase database;
    protected AutoRecordDbHelper mAutoRecordDbHelper;

    static {
        URI_MATCHER.addURI("com.android.phone.autorecord", "files", 11);
        URI_MATCHER.addURI("com.android.phone.autorecord", "files/#", 12);
        URI_MATCHER.addURI("com.android.phone.autorecord", "numbers", 1);
        URI_MATCHER.addURI("com.android.phone.autorecord", "numbers/#", 2);
    }

    public boolean onCreate() {
        this.mAutoRecordDbHelper = AutoRecordDbHelper.getInstance(getContext());
        return true;
    }

    public String getType(Uri arg0) {
        return null;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sort) {
        String str;
        Uri uri2 = uri;
        if (this.mAutoRecordDbHelper == null) {
            return null;
        }
        this.database = this.mAutoRecordDbHelper.getReadableDatabase();
        if (this.database != null) {
            int matchCode = URI_MATCHER.match(uri2);
            if (matchCode != 1) {
                switch (matchCode) {
                    case 11:
                        return this.database.query("call_record_file", projection, selection, selectionArgs, null, null, sort);
                    case 12:
                        String rowID = (String) uri.getPathSegments().get(1);
                        StringBuilder sb = new StringBuilder();
                        sb.append("_id=");
                        sb.append(rowID);
                        if (!TextUtils.isEmpty(selection)) {
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append(" AND (");
                            sb2.append(selection);
                            sb2.append(')');
                            str = sb2.toString();
                        } else {
                            String str2 = selection;
                            str = "";
                        }
                        sb.append(str);
                        return this.database.query("call_record_file", projection, sb.toString(), selectionArgs, null, null, sort);
                    default:
                        StringBuilder sb3 = new StringBuilder();
                        sb3.append("Unknown query uri = ");
                        sb3.append(uri2);
                        Log.e("AutoRecordProvider", sb3.toString());
                        String str3 = selection;
                        break;
                }
            } else {
                return this.database.query("customize", projection, selection, selectionArgs, null, null, sort);
            }
        } else {
            String str4 = selection;
            Log.e("AutoRecordProvider", "Get the database null while query !");
        }
        return null;
    }

    public int delete(Uri uri, String userWhere, String[] whereArgs) {
        int count = 0;
        if (this.mAutoRecordDbHelper == null) {
            return 0;
        }
        this.database = this.mAutoRecordDbHelper.getWritableDatabase();
        if (this.database != null) {
            switch (URI_MATCHER.match(uri)) {
                case 1:
                    count = this.database.delete("customize", userWhere, whereArgs);
                    break;
                case 2:
                    StringBuilder sb = new StringBuilder();
                    sb.append("_id = ");
                    sb.append(uri.getLastPathSegment());
                    count = this.database.delete("customize", sb.toString(), whereArgs);
                    break;
                case 11:
                    count = this.database.delete("call_record_file", userWhere, whereArgs);
                    break;
                case 12:
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("_id = ");
                    sb2.append(uri.getLastPathSegment());
                    count = this.database.delete("call_record_file", sb2.toString(), whereArgs);
                    break;
                default:
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append("Unknown delete uri = ");
                    sb3.append(uri);
                    Log.e("AutoRecordProvider", sb3.toString());
                    break;
            }
        } else {
            Log.e("AutoRecordProvider", "Get the database null while delete !");
        }
        return count;
    }

    public Uri insert(Uri uri, ContentValues values) {
        if (this.mAutoRecordDbHelper == null || values == null) {
            return null;
        }
        Uri retUri = uri;
        this.database = this.mAutoRecordDbHelper.getWritableDatabase();
        if (this.database != null) {
            int matchCode = URI_MATCHER.match(uri);
            String table = null;
            if (matchCode == 1) {
                table = "customize";
            } else if (matchCode != 11) {
                StringBuilder sb = new StringBuilder();
                sb.append("Unknown insert uri = ");
                sb.append(uri);
                Log.e("AutoRecordProvider", sb.toString());
            } else {
                table = "call_record_file";
            }
            if (table != null) {
                long newRecordId = this.database.insert(table, null, values);
                if (newRecordId != -1) {
                    retUri = Uri.withAppendedPath(retUri, String.valueOf(newRecordId));
                } else {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("insert failed! uri = ");
                    sb2.append(uri);
                    Log.e("AutoRecordProvider", sb2.toString());
                }
            }
        } else {
            Log.e("AutoRecordProvider", "Get the database null while insert!");
        }
        return retUri;
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        int numInserted = 0;
        if (this.mAutoRecordDbHelper == null) {
            return 0;
        }
        this.database = this.mAutoRecordDbHelper.getWritableDatabase();
        if (this.database != null) {
            int matchCode = URI_MATCHER.match(uri);
            String table = null;
            if (matchCode == 1) {
                table = "customize";
            } else if (matchCode != 11) {
                StringBuilder sb = new StringBuilder();
                sb.append("Unknown insert uri ");
                sb.append(uri);
                Log.e("AutoRecordProvider", sb.toString());
            } else {
                table = "call_record_file";
            }
            if (table != null) {
                try {
                    int length = values.length;
                    this.database.beginTransaction();
                    for (int i = 0; i != length; i++) {
                        this.database.insert(table, null, values[i]);
                    }
                    this.database.setTransactionSuccessful();
                    numInserted = length;
                } finally {
                    this.database.endTransaction();
                }
            }
        } else {
            Log.e("AutoRecordProvider", "Get the database null while bulkInsert!");
        }
        return numInserted;
    }

    public int update(Uri uri, ContentValues initialValues, String userWhere, String[] whereArgs) {
        int count = 0;
        if (this.mAutoRecordDbHelper == null) {
            return 0;
        }
        this.database = this.mAutoRecordDbHelper.getWritableDatabase();
        if (this.database != null) {
            switch (URI_MATCHER.match(uri)) {
                case 1:
                    count = this.database.update("customize", initialValues, userWhere, whereArgs);
                    break;
                case 2:
                    StringBuilder sb = new StringBuilder();
                    sb.append("_id = ");
                    sb.append(uri.getLastPathSegment());
                    count = this.database.update("customize", initialValues, sb.toString(), whereArgs);
                    break;
                case 11:
                    count = this.database.update("call_record_file", initialValues, userWhere, whereArgs);
                    break;
                case 12:
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("_id = ");
                    sb2.append(uri.getLastPathSegment());
                    count = this.database.update("call_record_file", initialValues, sb2.toString(), whereArgs);
                    break;
                default:
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append("Unknown update uri = ");
                    sb3.append(uri);
                    Log.e("AutoRecordProvider", sb3.toString());
                    break;
            }
        } else {
            Log.e("AutoRecordProvider", "Get the database null while insert !");
        }
        return count;
    }
}
