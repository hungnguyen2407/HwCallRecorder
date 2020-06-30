package com.android.phone.recorder.autorecord;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.SparseArray;
import com.android.phone.recorder.FileInfo;
import com.android.phone.recorder.FileOperator;
import com.android.phone.recorder.Utils;
import com.android.phone.recorder.ZipUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class AutoRecordBackupProvider extends AutoRecordProvider {
    private static SparseArray<RecoveryResult> sResultMap = null;
    private boolean mNeedUnZip = false;

    static class RecoveryResult {
        int mFaildInsertCount = 0;
        int mSuccessInsertCount = 0;

        RecoveryResult() {
        }
    }

    static {
        URI_MATCHER.addURI("com.android.phone.autorecordbackup", "call_record_file", 11);
        URI_MATCHER.addURI("com.android.phone.autorecordbackup", "customize", 1);
    }

    public boolean onCreate() {
        return super.onCreate();
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
                sb.append("Unknown insert uri ");
                sb.append(uri);
                Log.e("AutoRecordBackupProvider", sb.toString());
            } else {
                table = "call_record_file";
            }
            if (table != null) {
                ensureResultMapAndResultValueExits();
                RecoveryResult recoveryResult = (RecoveryResult) sResultMap.get(Binder.getCallingPid());
                long newRecordId = this.database.insert(table, null, values);
                if (newRecordId != -1) {
                    recoveryResult.mSuccessInsertCount++;
                    retUri = Uri.withAppendedPath(retUri, String.valueOf(newRecordId));
                } else {
                    recoveryResult.mFaildInsertCount++;
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("insert failed! uri = ");
                    sb2.append(uri);
                    Log.e("AutoRecordBackupProvider", sb2.toString());
                }
            }
        } else {
            Log.e("AutoRecordBackupProvider", "Get the database null while insert!");
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
            String table = null;
            int matchCode = URI_MATCHER.match(uri);
            if (matchCode == 1) {
                table = "customize";
            } else if (matchCode != 11) {
                StringBuilder sb = new StringBuilder();
                sb.append("Unknown insert uri = ");
                sb.append(uri);
                Log.e("AutoRecordBackupProvider", sb.toString());
            } else {
                table = "call_record_file";
            }
            if (table != null) {
                try {
                    int length = values.length;
                    ensureResultMapAndResultValueExits();
                    RecoveryResult recoveryResult = (RecoveryResult) sResultMap.get(Binder.getCallingPid());
                    this.database.beginTransaction();
                    for (int i = 0; i != length; i++) {
                        if (this.database.insert(table, null, values[i]) != -1) {
                            numInserted++;
                            recoveryResult.mSuccessInsertCount++;
                        } else {
                            recoveryResult.mFaildInsertCount++;
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append("insert failed! uri = ");
                            sb2.append(uri);
                            Log.e("AutoRecordBackupProvider", sb2.toString());
                        }
                    }
                    this.database.setTransactionSuccessful();
                } finally {
                    this.database.endTransaction();
                }
            }
        } else {
            Log.e("AutoRecordBackupProvider", "Get the database null while bulkInsert !");
        }
        return numInserted;
    }

    public Bundle call(String method, String arg, Bundle extras) {
        super.call(method, arg, extras);
        checkPermission();
        if ("backup_query".equals(method)) {
            return backupQuery(arg, extras);
        }
        if ("backup_recover_start".equals(method)) {
            return backupRecoverStart(arg, extras);
        }
        if ("backup_recover_complete".equals(method)) {
            return backupRecoverComplete(arg, extras);
        }
        return null;
    }

    /* JADX INFO: finally extract failed */
    private Bundle backupQuery(String arg, Bundle extras) {
        ArrayList<String> backupList = new ArrayList<>();
        backupList.add("content://com.android.phone.autorecordbackup/call_record_file");
        backupList.add("content://com.android.phone.autorecordbackup/customize");
        ArrayList<String> backupListNeedCount = new ArrayList<>();
        backupListNeedCount.add("content://com.android.phone.autorecordbackup/call_record_file");
        backupListNeedCount.add("content://com.android.phone.autorecordbackup/customize");
        ArrayList<String> copyFilePathList = new ArrayList<>();
        if (this.mAutoRecordDbHelper == null) {
            return null;
        }
        this.database = this.mAutoRecordDbHelper.getReadableDatabase();
        if (this.database != null) {
            try {
                Cursor cursor = this.database.rawQuery(this.mAutoRecordDbHelper.buildQueryString(null), new String[0]);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String dbPath = cursor.getString(2);
                        if (new File(dbPath).exists()) {
                            copyFilePathList.add(dbPath);
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    null.close();
                }
                throw th;
            }
        }
        ArrayList<String> defaultRestoreSDPathList = new ArrayList<>();
        getDefaultRestoreSDPath(defaultRestoreSDPathList);
        Bundle result = new Bundle();
        result.putInt("version", 3);
        result.putStringArrayList("uri_list", backupList);
        result.putStringArrayList("uri_list_need_count", backupListNeedCount);
        result.putStringArrayList("copyfile_path_list", copyFilePathList);
        result.putStringArrayList("default_restore_sd_path_list", defaultRestoreSDPathList);
        return result;
    }

    private void getRestoreSDPath(ArrayList<String> copyFilePathList) {
        String InternalStorage = Utils.getSdCardRootPath(getContext(), false);
        String ExternalStorage = Utils.getSdCardRootPath(getContext(), true);
        if (InternalStorage != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(InternalStorage);
            sb.append(File.separator);
            sb.append(Utils.RECORD_FOLDER);
            copyFilePathList.add(sb.toString());
        }
        if (ExternalStorage != null) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(ExternalStorage);
            sb2.append(File.separator);
            sb2.append(Utils.RECORD_FOLDER);
            copyFilePathList.add(sb2.toString());
        }
    }

    private void getDefaultRestoreSDPath(ArrayList<String> defaultRestoreSDPathList) {
        String InternalStorage = Utils.getSdCardRootPath(getContext(), false);
        String ExternalStorage = Utils.getSdCardRootPath(getContext(), true);
        if (InternalStorage != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(InternalStorage);
            sb.append(File.separator);
            sb.append("record");
            defaultRestoreSDPathList.add(sb.toString());
        }
        if (ExternalStorage != null) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(ExternalStorage);
            sb2.append(File.separator);
            sb2.append("record");
            defaultRestoreSDPathList.add(sb2.toString());
        }
    }

    private Bundle backupRecoverStart(String arg, Bundle extras) {
        if (extras == null) {
            Log.w("AutoRecordBackupProvider", "Caller intent to recover app' data, while the extas send is null");
            return null;
        }
        Bundle result = new Bundle();
        ArrayList<String> recoveryList = new ArrayList<>();
        ArrayList<String> recoveryFileList = new ArrayList<>();
        ArrayList<String> restoreSdPathList = new ArrayList<>();
        boolean isPermitted = extras.getInt("version", 3) <= 3;
        if (isPermitted) {
            recoveryList = extras.getStringArrayList("uri_list");
            recoveryFileList = extras.getStringArrayList("file_uri_list");
            getRestoreSDPath(restoreSdPathList);
        }
        result.putBoolean("permit", isPermitted);
        result.putStringArrayList("uri_list", recoveryList);
        result.putStringArrayList("file_uri_list", recoveryFileList);
        result.putStringArrayList("restore_sd_path", restoreSdPathList);
        return result;
    }

    private Bundle backupRecoverComplete(String arg, Bundle extras) {
        Bundle result = new Bundle();
        File zipFile = new File(getContext().getCacheDir(), "multi.zip");
        if (zipFile.exists()) {
            if (this.mNeedUnZip && Utils.getStoragePath(getContext()) != null) {
                try {
                    ZipUtils.unZipFile(zipFile, Utils.getStoragePath(getContext()));
                } catch (IOException e) {
                    Log.e("AutoRecordBackupProvider", "unzip file error");
                }
            }
            this.mNeedUnZip = false;
            boolean delResult = zipFile.delete();
            StringBuilder sb = new StringBuilder();
            sb.append("zip file delete result: ");
            sb.append(delResult);
            Log.i("AutoRecordBackupProvider", sb.toString());
        }
        int[] resultArrays = new int[2];
        if (sResultMap != null) {
            RecoveryResult recovery = (RecoveryResult) sResultMap.get(Binder.getCallingPid());
            if (recovery != null) {
                resultArrays[0] = recovery.mFaildInsertCount;
                resultArrays[1] = recovery.mSuccessInsertCount;
                sResultMap.delete(Binder.getCallingPid());
            } else {
                Log.d("AutoRecordBackupProvider", "recovery complete while the result is null");
            }
        } else {
            Log.d("AutoRecordBackupProvider", "recovery complete while the result map is null");
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append("the result array : ");
        sb2.append(resultArrays[0]);
        sb2.append(" ");
        sb2.append(resultArrays[1]);
        Log.d("AutoRecordBackupProvider", sb2.toString());
        result.putInt("fail_count", resultArrays[0]);
        result.putInt("success_count", resultArrays[1]);
        return result;
    }

    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        File destFile = null;
        if (URI_MATCHER.match(uri) != 14) {
            Log.e("AutoRecordBackupProvider", "wrong uri!");
        } else {
            ArrayList<FileInfo> recordList = FileOperator.getRecordFileList(getContext(), Utils.RECORD_FOLDER);
            if (recordList.size() == 0) {
                return null;
            }
            destFile = new File(getContext().getCacheDir(), "multi.zip");
            if ("r".equals(mode)) {
                Collection<File> srcFileList = new ArrayList<>();
                int recordListSize = recordList.size();
                for (int i = 0; i < recordListSize; i++) {
                    srcFileList.add(new File(((FileInfo) recordList.get(i)).getMFilePath()));
                }
                try {
                    ZipUtils.zipFiles(srcFileList, destFile);
                } catch (IOException e) {
                    destFile = null;
                    Log.e("AutoRecordBackupProvider", "zip file error");
                }
            } else if ("w".equals(mode)) {
                this.mNeedUnZip = true;
            }
        }
        if (destFile != null) {
            return ParcelFileDescriptor.open(destFile, getFileMode(mode));
        }
        return null;
    }

    private int getFileMode(String mode) {
        int imode = 0;
        if ("w".contains(mode)) {
            imode = 0 | 536870912;
        }
        if ("r".contains(mode)) {
            imode |= 268435456;
        }
        if ("+".contains(mode)) {
            return imode | 33554432;
        }
        return imode;
    }

    private void checkPermission() {
        int uid = Binder.getCallingUid();
        Context context = getContext();
        String rperm = getReadPermission();
        int pid = Binder.getCallingPid();
        if (rperm != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Permission Denial: reading ");
            sb.append(getClass().getName());
            sb.append(" from pid=");
            sb.append(Binder.getCallingPid());
            sb.append(", uid=");
            sb.append(Binder.getCallingUid());
            sb.append(" requires ");
            sb.append(rperm);
            context.enforcePermission(rperm, pid, uid, sb.toString());
        }
    }

    private void ensureResultMapAndResultValueExits() {
        if (sResultMap == null) {
            setResultMap(new SparseArray());
        }
        if (((RecoveryResult) sResultMap.get(Binder.getCallingPid())) == null) {
            sResultMap.put(Binder.getCallingPid(), new RecoveryResult());
        }
    }

    public static void setResultMap(SparseArray<RecoveryResult> result) {
        sResultMap = result;
    }
}
