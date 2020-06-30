package com.android.phone.recorder.autorecord;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Files;
import android.text.TextUtils;
import android.util.Log;
import com.android.phone.recorder.FileChangedMonitorService;
import com.android.phone.recorder.FileInfo;
import com.android.phone.recorder.FileOperator;
import com.android.phone.recorder.Utils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoRecordDbHelper extends SQLiteOpenHelper {
    private static final Uri EXTERNAL_FILE_URI = Files.getContentUri("external");
    public static final Uri FILES_URI = Uri.parse("content://com.android.phone.autorecord/files");
    private static final Object mLock = new Object();
    private static final AtomicBoolean sInUpdate = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public static AutoRecordDbHelper sInstance = null;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public boolean mIsDbUpgrated = false;
    private SQLiteStatement mNormalRecordDelete;
    private SQLiteStatement mNormalRecordInsert;
    /* access modifiers changed from: private */
    public int mPendingRequestsCount = 0;
    /* access modifiers changed from: private */
    public Handler mSyncHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 101 && !AutoRecordDbHelper.this.mIsDbUpgrated) {
                AutoRecordDbHelper.this.startRecordUpdateThread();
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mUpdatingFinished = true;

    private class RecordListUpdateAsyncTask extends AsyncTask {
        private RecordListUpdateAsyncTask() {
        }

        /* access modifiers changed from: protected */
        public Object doInBackground(Object[] objects) {
            Log.v("AutoRecordDbHelper", "  doInBackground ");
            synchronized (AutoRecordDbHelper.sInstance) {
                AutoRecordDbHelper.this.mUpdatingFinished = false;
            }
            ArrayList<FileInfo> recordList = FileOperator.getRecordFileList(AutoRecordDbHelper.this.mContext, Utils.RECORD_FOLDER);
            AutoRecordDbHelper.this.updateRecordListDatabase(AutoRecordDbHelper.this.getWritableDatabase(), recordList);
            return null;
        }

        /* access modifiers changed from: protected */
        public void onCancelled() {
            Log.v("AutoRecordDbHelper", "Updating  record list  Cancelled");
            synchronized (AutoRecordDbHelper.sInstance) {
                AutoRecordDbHelper.this.mUpdatingFinished = true;
            }
            super.onCancelled();
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Object o) {
            int pendingRequestCount;
            Log.v("AutoRecordDbHelper", "Updating   record list  Finished");
            synchronized (AutoRecordDbHelper.sInstance) {
                AutoRecordDbHelper.this.mUpdatingFinished = true;
                pendingRequestCount = AutoRecordDbHelper.this.mPendingRequestsCount;
            }
            if (pendingRequestCount > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(pendingRequestCount);
                sb.append(" pending requests waiting ...");
                Log.d("AutoRecordDbHelper", sb.toString());
                AutoRecordDbHelper.this.startRecordUpdateThread();
            }
            super.onPostExecute(o);
        }
    }

    public static AutoRecordDbHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AutoRecordDbHelper(context);
        }
        return sInstance;
    }

    private AutoRecordDbHelper(Context context) {
        super(context, "autorecord.db", null, 3);
        this.mContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
        updateDatabase(this.mContext, db, 0, 3);
    }

    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        updateDatabase(this.mContext, db, oldV, newV);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion != 3) {
            StringBuilder sb = new StringBuilder();
            sb.append("Illegal downgrade request. Got ");
            sb.append(newVersion);
            sb.append(", expected ");
            sb.append(3);
            Log.e("AutoRecordDbHelper", sb.toString());
            throw new IllegalArgumentException();
        } else if (oldVersion > newVersion) {
            createCallRecordTable(db);
            createCallRecordFileTable(db);
            deleteCallRecordUriFromMediaDB(this.mContext);
            this.mContext.startService(new Intent(this.mContext, FileChangedMonitorService.class));
        } else {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Illegal downgrade request: can't downgrade from ");
            sb2.append(oldVersion);
            sb2.append(" to ");
            sb2.append(newVersion);
            sb2.append(".");
            Log.e("AutoRecordDbHelper", sb2.toString());
            throw new IllegalArgumentException();
        }
    }

    private void updateDatabase(Context context, SQLiteDatabase db, int fromVersion, int toVersion) {
        if (toVersion != 3) {
            StringBuilder sb = new StringBuilder();
            sb.append("Illegal update request. Got ");
            sb.append(toVersion);
            sb.append(", expected ");
            sb.append(3);
            Log.e("AutoRecordDbHelper", sb.toString());
            throw new IllegalArgumentException();
        } else if (fromVersion <= toVersion) {
            if (fromVersion == 1) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Upgrading database from version ");
                sb2.append(fromVersion);
                sb2.append(" to ");
                sb2.append(toVersion);
                sb2.append(", which will destroy all old data");
                Log.i("AutoRecordDbHelper", sb2.toString());
                createCallRecordTable(db);
            }
            if (fromVersion < 1) {
                createCallRecordFileTable(db);
                deleteCallRecordUriFromMediaDB(context);
                createCallRecordTable(db);
            }
            if (fromVersion < 3) {
                new Thread(new Runnable() {
                    public void run() {
                        FileOperator.moveFileDir(AutoRecordDbHelper.this.mContext);
                        AutoRecordDbHelper.this.mSyncHandler.sendEmptyMessageDelayed(101, 4000);
                    }
                }).start();
            }
        } else {
            StringBuilder sb3 = new StringBuilder();
            sb3.append("Illegal update request: can't downgrade from ");
            sb3.append(fromVersion);
            sb3.append(" to ");
            sb3.append(toVersion);
            sb3.append(". Did you forget to wipe data?");
            Log.e("AutoRecordDbHelper", sb3.toString());
            throw new IllegalArgumentException();
        }
    }

    private static void createCallRecordFileTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS call_record_file (_id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT NOT NULL UNIQUE ON CONFLICT REPLACE,_data TEXT,date_added LONG, file_size LONG, duration LONG );");
        Log.i("AutoRecordDbHelper", "call record files table created");
    }

    public void startRecordUpdateThread() {
        synchronized (sInstance) {
            if (this.mUpdatingFinished) {
                this.mPendingRequestsCount = 0;
                new RecordListUpdateAsyncTask().execute(new Object[0]);
            } else {
                this.mPendingRequestsCount++;
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00b7, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00b8, code lost:
        r13 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x010a, code lost:
        r13 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x011b, code lost:
        r13 = r23;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0109 A[ExcHandler: SQLiteReadOnlyDatabaseException (e android.database.sqlite.SQLiteReadOnlyDatabaseException), Splitter:B:11:0x0035] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x011a A[ExcHandler: IllegalStateException (e java.lang.IllegalStateException), Splitter:B:11:0x0035] */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0140 A[Catch:{ all -> 0x01a3, all -> 0x01b7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x0191 A[Catch:{ all -> 0x01a3, all -> 0x01b7 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateRecordListDatabase(android.database.sqlite.SQLiteDatabase r22, java.util.ArrayList<com.android.phone.recorder.FileInfo> r23) {
        /*
            r21 = this;
            r1 = r21
            r2 = r22
            java.lang.Object r3 = mLock
            monitor-enter(r3)
            java.lang.String r0 = "AutoRecordDbHelper"
            java.lang.String r4 = "Starting to update database"
            android.util.Log.v(r0, r4)     // Catch:{ all -> 0x01b1 }
            r0 = 0
            java.lang.String r4 = r1.buildQueryString(r0)     // Catch:{ all -> 0x01b1 }
            r5 = 0
            java.lang.String[] r6 = new java.lang.String[r5]     // Catch:{ all -> 0x01b1 }
            android.database.Cursor r4 = r2.rawQuery(r4, r6)     // Catch:{ all -> 0x01b1 }
            if (r4 != 0) goto L_0x0025
            java.lang.String r0 = "AutoRecordDbHelper"
            java.lang.String r5 = " query received null for cursor"
            android.util.Log.d(r0, r5)     // Catch:{ all -> 0x01b1 }
            monitor-exit(r3)     // Catch:{ all -> 0x01b1 }
            return
        L_0x0025:
            java.util.concurrent.atomic.AtomicBoolean r6 = sInUpdate     // Catch:{ all -> 0x01b1 }
            r7 = 1
            r6.getAndSet(r7)     // Catch:{ all -> 0x01b1 }
            java.util.ArrayList r6 = new java.util.ArrayList     // Catch:{ all -> 0x01b1 }
            r6.<init>()     // Catch:{ all -> 0x01b1 }
            r22.beginTransaction()     // Catch:{ all -> 0x01b1 }
            java.lang.String r8 = "AutoRecordDbHelper"
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
            r9.<init>()     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
            java.lang.String r10 = "fileList size: "
            r9.append(r10)     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
            int r10 = r23.size()     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
            r9.append(r10)     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
            java.lang.String r9 = r9.toString()     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
            android.util.Log.d(r8, r9)     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
            int r8 = r23.size()     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
            if (r8 != 0) goto L_0x005e
            java.lang.String r7 = "call_record_file"
            java.lang.String[] r8 = new java.lang.String[r5]     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
            r2.delete(r7, r0, r8)     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
            r13 = r23
            goto L_0x00f8
        L_0x005e:
            java.util.ArrayList r8 = new java.util.ArrayList     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
            r8.<init>()     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
        L_0x0063:
            boolean r9 = r4.moveToNext()     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
            if (r9 == 0) goto L_0x00bc
            int r9 = r4.getInt(r5)     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
            java.lang.String r12 = r4.getString(r7)     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
            r10 = 2
            java.lang.String r11 = r4.getString(r10)     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
            r10 = 3
            long r14 = r4.getLong(r10)     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
            r10 = 4
            long r16 = r4.getLong(r10)     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
            r18 = r16
            com.android.phone.recorder.FileInfo r16 = new com.android.phone.recorder.FileInfo     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
            r10.<init>()     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x0103 }
            r0 = r18
            r10.append(r0)     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x00b7 }
            java.lang.String r13 = ""
            r10.append(r13)     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x00b7 }
            java.lang.String r13 = r10.toString()     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x00b7 }
            r10 = r16
            r10.<init>(r11, r12, r13, r14)     // Catch:{ IllegalStateException -> 0x011a, SQLiteReadOnlyDatabaseException -> 0x0109, all -> 0x00b7 }
            r10 = r16
            r13 = r23
            boolean r16 = r13.contains(r10)     // Catch:{ IllegalStateException -> 0x0101, SQLiteReadOnlyDatabaseException -> 0x00ff }
            if (r16 != 0) goto L_0x00ae
            java.lang.String r5 = java.lang.String.valueOf(r9)     // Catch:{ IllegalStateException -> 0x0101, SQLiteReadOnlyDatabaseException -> 0x00ff }
            r8.add(r5)     // Catch:{ IllegalStateException -> 0x0101, SQLiteReadOnlyDatabaseException -> 0x00ff }
            goto L_0x00b1
        L_0x00ae:
            r6.add(r10)     // Catch:{ IllegalStateException -> 0x0101, SQLiteReadOnlyDatabaseException -> 0x00ff }
        L_0x00b1:
            r0 = 0
            r1 = r21
            r5 = 0
            goto L_0x0063
        L_0x00b7:
            r0 = move-exception
            r13 = r23
            goto L_0x01a8
        L_0x00bc:
            r13 = r23
            int r0 = r8.size()     // Catch:{ IllegalStateException -> 0x0101, SQLiteReadOnlyDatabaseException -> 0x00ff }
            if (r0 <= 0) goto L_0x00f8
            int r0 = r8.size()     // Catch:{ IllegalStateException -> 0x0101, SQLiteReadOnlyDatabaseException -> 0x00ff }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ IllegalStateException -> 0x0101, SQLiteReadOnlyDatabaseException -> 0x00ff }
            r1.<init>()     // Catch:{ IllegalStateException -> 0x0101, SQLiteReadOnlyDatabaseException -> 0x00ff }
            r5 = 0
        L_0x00ce:
            if (r5 >= r0) goto L_0x00e3
            if (r5 <= 0) goto L_0x00d7
            java.lang.String r9 = ","
            r1.append(r9)     // Catch:{ IllegalStateException -> 0x0101, SQLiteReadOnlyDatabaseException -> 0x00ff }
        L_0x00d7:
            java.lang.Object r9 = r8.get(r5)     // Catch:{ IllegalStateException -> 0x0101, SQLiteReadOnlyDatabaseException -> 0x00ff }
            java.lang.String r9 = (java.lang.String) r9     // Catch:{ IllegalStateException -> 0x0101, SQLiteReadOnlyDatabaseException -> 0x00ff }
            r1.append(r9)     // Catch:{ IllegalStateException -> 0x0101, SQLiteReadOnlyDatabaseException -> 0x00ff }
            int r5 = r5 + 1
            goto L_0x00ce
        L_0x00e3:
            java.lang.String r5 = "_id IN (%s)"
            java.lang.Object[] r7 = new java.lang.Object[r7]     // Catch:{ IllegalStateException -> 0x0101, SQLiteReadOnlyDatabaseException -> 0x00ff }
            java.lang.String r9 = r1.toString()     // Catch:{ IllegalStateException -> 0x0101, SQLiteReadOnlyDatabaseException -> 0x00ff }
            r10 = 0
            r7[r10] = r9     // Catch:{ IllegalStateException -> 0x0101, SQLiteReadOnlyDatabaseException -> 0x00ff }
            java.lang.String r5 = java.lang.String.format(r5, r7)     // Catch:{ IllegalStateException -> 0x0101, SQLiteReadOnlyDatabaseException -> 0x00ff }
            java.lang.String r7 = "call_record_file"
            r9 = 0
            r2.delete(r7, r5, r9)     // Catch:{ IllegalStateException -> 0x0101, SQLiteReadOnlyDatabaseException -> 0x00ff }
        L_0x00f8:
            r22.setTransactionSuccessful()     // Catch:{ IllegalStateException -> 0x0101, SQLiteReadOnlyDatabaseException -> 0x00ff }
            r4.close()     // Catch:{ all -> 0x01a3 }
            goto L_0x0116
        L_0x00ff:
            r0 = move-exception
            goto L_0x010c
        L_0x0101:
            r0 = move-exception
            goto L_0x011d
        L_0x0103:
            r0 = move-exception
            r13 = r23
            r10 = r1
            goto L_0x01aa
        L_0x0109:
            r0 = move-exception
            r13 = r23
        L_0x010c:
            java.lang.String r1 = "AutoRecordDbHelper"
            java.lang.String r5 = "updateRecordListDatabase throw Exception"
            android.util.Log.e(r1, r5)     // Catch:{ all -> 0x01a7 }
            r4.close()     // Catch:{ all -> 0x01a3 }
        L_0x0116:
            r22.endTransaction()     // Catch:{ all -> 0x01a3 }
            goto L_0x0128
        L_0x011a:
            r0 = move-exception
            r13 = r23
        L_0x011d:
            java.lang.String r1 = "AutoRecordDbHelper"
            java.lang.String r5 = "updateRecordListDatabase throw IllegalStateException"
            android.util.Log.e(r1, r5)     // Catch:{ all -> 0x01a7 }
            r4.close()     // Catch:{ all -> 0x01a3 }
            goto L_0x0116
        L_0x0128:
            java.lang.String r0 = "AutoRecordDbHelper"
            java.lang.String r1 = "compute the added file duration "
            android.util.Log.v(r0, r1)     // Catch:{ all -> 0x01a3 }
            java.util.ArrayList r0 = new java.util.ArrayList     // Catch:{ all -> 0x01a3 }
            r0.<init>()     // Catch:{ all -> 0x01a3 }
            r1 = 0
            java.util.Iterator r5 = r23.iterator()     // Catch:{ all -> 0x01a3 }
        L_0x013a:
            boolean r7 = r5.hasNext()     // Catch:{ all -> 0x01a3 }
            if (r7 == 0) goto L_0x0189
            java.lang.Object r7 = r5.next()     // Catch:{ all -> 0x01a3 }
            com.android.phone.recorder.FileInfo r7 = (com.android.phone.recorder.FileInfo) r7     // Catch:{ all -> 0x01a3 }
            boolean r8 = r6.contains(r7)     // Catch:{ all -> 0x01a3 }
            if (r8 != 0) goto L_0x0186
            java.lang.String r8 = r7.getMFilePath()     // Catch:{ all -> 0x01a3 }
            int r8 = getFileDuration(r8)     // Catch:{ all -> 0x01a3 }
            long r8 = (long) r8     // Catch:{ all -> 0x01a3 }
            java.lang.String r10 = "AutoRecordDbHelper"
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ all -> 0x01a3 }
            r11.<init>()     // Catch:{ all -> 0x01a3 }
            java.lang.String r12 = "duration:"
            r11.append(r12)     // Catch:{ all -> 0x01a3 }
            r11.append(r8)     // Catch:{ all -> 0x01a3 }
            java.lang.String r11 = r11.toString()     // Catch:{ all -> 0x01a3 }
            android.util.Log.d(r10, r11)     // Catch:{ all -> 0x01a3 }
            r10 = 0
            int r10 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1))
            if (r10 <= 0) goto L_0x0179
            r7.setmDuration(r8)     // Catch:{ all -> 0x01a3 }
            r0.add(r7)     // Catch:{ all -> 0x01a3 }
            int r1 = r1 + 1
        L_0x0179:
            int r10 = r1 % 100
            if (r10 != 0) goto L_0x0186
            r10 = r21
            r10.flushFileList(r2, r0)     // Catch:{ all -> 0x01b7 }
            r0.clear()     // Catch:{ all -> 0x01b7 }
            goto L_0x0188
        L_0x0186:
            r10 = r21
        L_0x0188:
            goto L_0x013a
        L_0x0189:
            r10 = r21
            int r5 = r0.size()     // Catch:{ all -> 0x01b7 }
            if (r5 <= 0) goto L_0x0194
            r10.flushFileList(r2, r0)     // Catch:{ all -> 0x01b7 }
        L_0x0194:
            java.util.concurrent.atomic.AtomicBoolean r5 = sInUpdate     // Catch:{ all -> 0x01b7 }
            r7 = 0
            r5.getAndSet(r7)     // Catch:{ all -> 0x01b7 }
            java.lang.String r5 = "AutoRecordDbHelper"
            java.lang.String r7 = "ending to update database"
            android.util.Log.v(r5, r7)     // Catch:{ all -> 0x01b7 }
            monitor-exit(r3)     // Catch:{ all -> 0x01b7 }
            return
        L_0x01a3:
            r0 = move-exception
            r10 = r21
            goto L_0x01b5
        L_0x01a7:
            r0 = move-exception
        L_0x01a8:
            r10 = r21
        L_0x01aa:
            r4.close()     // Catch:{ all -> 0x01b7 }
            r22.endTransaction()     // Catch:{ all -> 0x01b7 }
            throw r0     // Catch:{ all -> 0x01b7 }
        L_0x01b1:
            r0 = move-exception
            r13 = r23
            r10 = r1
        L_0x01b5:
            monitor-exit(r3)     // Catch:{ all -> 0x01b7 }
            throw r0
        L_0x01b7:
            r0 = move-exception
            goto L_0x01b5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.phone.recorder.autorecord.AutoRecordDbHelper.updateRecordListDatabase(android.database.sqlite.SQLiteDatabase, java.util.ArrayList):void");
    }

    /* access modifiers changed from: 0000 */
    public void flushFileList(SQLiteDatabase db, ArrayList<FileInfo> fileList) {
        if (fileList != null && fileList.size() != 0) {
            db.beginTransaction();
            String str = "INSERT INTO call_record_file (title, _data, date_added, file_size, duration)  VALUES (?, ?, ? ,?,?)";
            try {
                SQLiteStatement insert = db.compileStatement("INSERT INTO call_record_file (title, _data, date_added, file_size, duration)  VALUES (?, ?, ? ,?,?)");
                int fileListSize = fileList.size();
                for (int i = 0; i < fileListSize; i++) {
                    FileInfo info = (FileInfo) fileList.get(i);
                    insert.bindString(1, info.getMFileName());
                    insert.bindString(2, info.getMFilePath());
                    insert.bindLong(3, info.getMCreateTime());
                    insert.bindLong(4, Long.parseLong(info.getMFileSize()));
                    insert.bindLong(5, info.getmDuration());
                    StringBuilder sb = new StringBuilder();
                    sb.append("flushFileList() info.getmDuration():");
                    sb.append(info.getmDuration());
                    Log.d("AutoRecordDbHelper", sb.toString());
                    insert.executeInsert();
                    insert.clearBindings();
                }
                this.mIsDbUpgrated = true;
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    public static int getFileDuration(String filePath) {
        MediaPlayer mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(filePath);
            mPlayer.prepare();
            return mPlayer.getDuration();
        } catch (IllegalStateException e) {
            String str = "AutoRecordDbHelper";
            StringBuilder sb = new StringBuilder();
            sb.append("getFileDuration : IllegalArgumentException = ");
            sb.append(e.getMessage());
            Log.d(str, sb.toString());
            return 0;
        } catch (IOException e2) {
            String str2 = "AutoRecordDbHelper";
            StringBuilder sb2 = new StringBuilder();
            sb2.append("getFileDuration : SecurityException = ");
            sb2.append(e2.getMessage());
            Log.d(str2, sb2.toString());
            return 0;
        } catch (IllegalArgumentException e3) {
            String str3 = "AutoRecordDbHelper";
            StringBuilder sb3 = new StringBuilder();
            sb3.append("getFileDuration : IllegalStateException = ");
            sb3.append(e3.getMessage());
            Log.d(str3, sb3.toString());
            return 0;
        } catch (SecurityException e4) {
            String str4 = "AutoRecordDbHelper";
            StringBuilder sb4 = new StringBuilder();
            sb4.append("getFileDuration : IOException = ");
            sb4.append(e4.getMessage());
            Log.d(str4, sb4.toString());
            return 0;
        } finally {
            mPlayer.reset();
            mPlayer.release();
        }
    }

    public String buildQueryString(String whereClause) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(" _id, title, _data, date_added, file_size, duration");
        sb.append(" FROM ");
        sb.append("call_record_file");
        if (!TextUtils.isEmpty(whereClause)) {
            sb.append(" WHERE ");
            sb.append(whereClause);
        }
        return sb.toString();
    }

    public void deleteRecord(String path) {
        if (this.mNormalRecordDelete == null) {
            this.mNormalRecordDelete = getWritableDatabase().compileStatement("DELETE FROM call_record_file WHERE _data=?");
        }
        this.mNormalRecordDelete.bindString(1, path);
        this.mNormalRecordDelete.execute();
    }

    public void saveRecord(FileInfo info) {
        if (info != null) {
            if (this.mNormalRecordInsert == null) {
                this.mNormalRecordInsert = getWritableDatabase().compileStatement("INSERT INTO call_record_file (title, _data, date_added, file_size, duration)  VALUES (?, ?, ? ,?,?)");
            }
            try {
                this.mNormalRecordInsert.bindString(1, info.getMFileName());
                this.mNormalRecordInsert.bindString(2, info.getMFilePath());
                this.mNormalRecordInsert.bindLong(3, info.getMCreateTime());
                this.mNormalRecordInsert.bindLong(4, Long.parseLong(info.getMFileSize()));
                this.mNormalRecordInsert.bindLong(5, (long) getFileDuration(info.getMFilePath()));
                this.mNormalRecordInsert.executeInsert();
            } catch (SQLException e) {
                Log.e("AutoRecordDbHelper", "SQLException saveRecord filed");
            }
        }
    }

    private static void deleteCallRecordUriFromMediaDB(Context context) {
        ContentResolver mediaProvider = context.getContentResolver();
        if (mediaProvider != null) {
            deleteFromMediaDB(context, mediaProvider, Utils.getInternalStorage());
            deleteFromMediaDB(context, mediaProvider, Utils.getExternalStorage());
        }
    }

    private static void deleteFromMediaDB(Context context, ContentResolver mediaProvider, String storage) {
        StringBuilder sb = new StringBuilder();
        sb.append(storage);
        sb.append("/");
        sb.append(Utils.RECORD_FOLDER);
        Utils.makeNomedia(new File(sb.toString()));
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("((");
        whereClause.append("_data");
        whereClause.append(" LIKE '");
        whereClause.append(Utils.getInternalStorage());
        whereClause.append("/");
        whereClause.append(Utils.RECORD_FOLDER);
        whereClause.append("/");
        whereClause.append("%' ");
        whereClause.append(" AND ");
        whereClause.append("_data");
        whereClause.append(" NOT LIKE '");
        whereClause.append(Utils.getInternalStorage());
        whereClause.append("/");
        whereClause.append(Utils.RECORD_FOLDER);
        whereClause.append("/");
        whereClause.append("%/%' ");
        whereClause.append(") OR (");
        whereClause.append("_data");
        whereClause.append(" LIKE '");
        whereClause.append(Utils.getExternalStorage());
        whereClause.append("/");
        whereClause.append(Utils.RECORD_FOLDER);
        whereClause.append("/");
        whereClause.append("%' ");
        whereClause.append(" AND ");
        whereClause.append("_data");
        whereClause.append(" NOT LIKE '");
        whereClause.append(Utils.getExternalStorage());
        whereClause.append("/");
        whereClause.append(Utils.RECORD_FOLDER);
        whereClause.append("/");
        whereClause.append("%/%' ");
        whereClause.append("))");
        whereClause.append(" AND ");
        whereClause.append("mime_type");
        whereClause.append(" = '");
        whereClause.append("audio/amr");
        whereClause.append("' ");
        try {
            mediaProvider.delete(EXTERNAL_FILE_URI, whereClause.toString(), null);
        } catch (SQLException e) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("SQLException in deleteFromMediaDB() Exception:");
            sb2.append(e);
            Log.d("AutoRecordDbHelper", sb2.toString());
        } catch (UnsupportedOperationException e2) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append("UnsupportedOperationException in deleteFromMediaDB() Exception:");
            sb3.append(e2);
            Log.d("AutoRecordDbHelper", sb3.toString());
        }
    }

    private static void createCallRecordTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS customize (_id INTEGER PRIMARY KEY,name TEXT,number TEXT NOT NULL UNIQUE ON CONFLICT REPLACE);");
        Log.i("AutoRecordDbHelper", "CallRecord table created");
    }
}
