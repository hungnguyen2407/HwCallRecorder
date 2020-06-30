package com.android.phone.recorder;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.voicerecorder.WrappedFileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import com.android.phone.recorder.autorecord.AutoRecordDbHelper;
import java.io.File;

public class FileChangedMonitorService extends Service {
    private RecordFileObserver fObserverCall = null;
    private RecordFileObserver mSDcardRFObserverCall = null;
    /* access modifiers changed from: private */
    public Handler mSyncHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 2) {
                AutoRecordDbHelper.getInstance(FileChangedMonitorService.this).startRecordUpdateThread();
            }
        }
    };
    private BroadcastReceiver mUnmountReceiver = null;

    class RecordFileObserver extends WrappedFileObserver {
        private String obSvrPath = null;

        public RecordFileObserver(String obsPath) {
            super(obsPath, 704);
            this.obSvrPath = obsPath;
        }

        public void onEvent(int event, String path) {
            StringBuilder sb = new StringBuilder();
            sb.append("event = ");
            sb.append(event);
            Log.d("FileChangedMonitorService", sb.toString());
            if (event == 512) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append(this.obSvrPath);
                sb2.append("/");
                sb2.append(path);
                FileChangedMonitorService.this.deleteFromDB(sb2.toString());
                FileChangedMonitorService.this.sendSyncMessage(this);
            } else if (event == 128 || event == 64) {
                FileChangedMonitorService.this.sendSyncMessage(this);
            }
        }
    }

    public void onCreate() {
        Log.d("FileChangedMonitorService", "onCreate()");
        this.mSyncHandler.post(new Runnable() {
            public void run() {
                Utils.initStorage(FileChangedMonitorService.this);
                StringBuilder sb = new StringBuilder();
                sb.append(Utils.getExternalStorage());
                sb.append("/");
                sb.append(Utils.RECORD_FOLDER);
                Utils.makeNomedia(new File(sb.toString()));
                StringBuilder sb2 = new StringBuilder();
                sb2.append(Utils.getInternalStorage());
                sb2.append("/");
                sb2.append(Utils.RECORD_FOLDER);
                Utils.makeNomedia(new File(sb2.toString()));
                FileChangedMonitorService.this.registerExternalStorageListener();
                FileChangedMonitorService.this.registerInternalRecordFileObserver();
                FileChangedMonitorService.this.registerExternalRecordFileObserver();
                if (!FileChangedMonitorService.this.mSyncHandler.hasMessages(2)) {
                    FileChangedMonitorService.this.mSyncHandler.sendEmptyMessageDelayed(2, 4000);
                }
            }
        });
    }

    public IBinder onBind(Intent intent) {
        Log.d("FileChangedMonitorService", "onBind()");
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("FileChangedMonitorService", "onStartCommand");
        return 1;
    }

    public void onDestroy() {
        Log.d("FileChangedMonitorService", "onDestroy()");
    }

    /* access modifiers changed from: private */
    public void sendSyncMessage(WrappedFileObserver observer) {
        if ((observer == this.fObserverCall || observer == this.mSDcardRFObserverCall) && !this.mSyncHandler.hasMessages(2)) {
            Log.d("FileChangedMonitorService", "MSG_SYNC_RECORDLIST send ..");
            this.mSyncHandler.sendEmptyMessageDelayed(2, 4000);
        }
    }

    /* access modifiers changed from: private */
    public void registerInternalRecordFileObserver() {
        if (this.fObserverCall == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(Utils.getInternalStorage());
            sb.append("/");
            sb.append(Utils.RECORD_FOLDER);
            this.fObserverCall = new RecordFileObserver(sb.toString());
            this.fObserverCall.startWatching();
            StringBuilder sb2 = new StringBuilder();
            sb2.append("start watching Internal record file:");
            sb2.append(Utils.getInternalStorage());
            sb2.append("/");
            sb2.append(Utils.RECORD_FOLDER);
            Log.i("FileChangedMonitorService", sb2.toString());
        }
    }

    /* access modifiers changed from: private */
    public void registerExternalRecordFileObserver() {
        if (this.mSDcardRFObserverCall == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(Utils.getExternalStorage());
            sb.append("/");
            sb.append(Utils.RECORD_FOLDER);
            this.mSDcardRFObserverCall = new RecordFileObserver(sb.toString());
            this.mSDcardRFObserverCall.startWatching();
            StringBuilder sb2 = new StringBuilder();
            sb2.append("start watching External record file:");
            sb2.append(Utils.getExternalStorage());
            sb2.append("/");
            sb2.append(Utils.RECORD_FOLDER);
            Log.i("FileChangedMonitorService", sb2.toString());
        }
    }

    /* access modifiers changed from: private */
    public void unRegisterExternalRecordFileObserver() {
        if (this.mSDcardRFObserverCall != null) {
            this.mSDcardRFObserverCall.stopWatching();
            this.mSDcardRFObserverCall = null;
        }
    }

    public void registerExternalStorageListener() {
        if (this.mUnmountReceiver == null) {
            this.mUnmountReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if ("android.intent.action.MEDIA_EJECT".equals(action)) {
                        FileChangedMonitorService.this.unRegisterExternalRecordFileObserver();
                    } else if ("android.intent.action.MEDIA_MOUNTED".equals(action)) {
                        Utils.initStorage(FileChangedMonitorService.this);
                        StringBuilder sb = new StringBuilder();
                        sb.append(Utils.getExternalStorage());
                        sb.append("/");
                        sb.append(Utils.RECORD_FOLDER);
                        Utils.makeNomedia(new File(sb.toString()));
                        FileChangedMonitorService.this.registerExternalRecordFileObserver();
                    }
                    if (!FileChangedMonitorService.this.mSyncHandler.hasMessages(2)) {
                        FileChangedMonitorService.this.mSyncHandler.sendEmptyMessageDelayed(2, 4000);
                    }
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction("android.intent.action.MEDIA_EJECT");
            iFilter.addAction("android.intent.action.MEDIA_MOUNTED");
            iFilter.addAction("android.intent.action.MEDIA_SHARED");
            iFilter.addDataScheme("file");
            registerReceiver(this.mUnmountReceiver, iFilter);
        }
    }

    /* access modifiers changed from: private */
    public void deleteFromDB(String data) {
        if (data != null && data.contains(Utils.RECORD_FOLDER)) {
            getContentResolver().delete(AutoRecordDbHelper.FILES_URI, "_data = ?", new String[]{data});
        }
    }
}
