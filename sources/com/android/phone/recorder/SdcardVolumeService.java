package com.android.phone.recorder;

import android.app.Service;
import android.content.Intent;
import android.media.voicerecorder.BaseVoiceRecorder;
import android.media.voicerecorder.TIVoiceRecorder;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;

public class SdcardVolumeService extends Service {
    final IBinder nBinder = new LocalBinder();
    String nStoragePath;
    Timer nTimer;
    BaseVoiceRecorder nVoiceRecorder;

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        public SdcardVolumeService gainSdcardVolumeService() {
            return SdcardVolumeService.this;
        }
    }

    public void onCreate() {
        super.onCreate();
        this.nTimer = new Timer();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return 2;
    }

    public void onDestroy() {
        Log.v("SdcardVolumeService", "SdcardVolumeService#onDestroy");
    }

    public IBinder onBind(Intent intent) {
        Log.d("SdcardVolumeService", "Sdcard service bind");
        return this.nBinder;
    }

    private void checkSdcardVolume() {
        if (this.nStoragePath != null) {
            if (this.nTimer == null) {
                this.nTimer = new Timer();
            }
            this.nTimer.schedule(new TimerTask() {
                public void run() {
                    boolean hasSpace = false;
                    String storagePath = SdcardVolumeService.this.nStoragePath.substring(0, SdcardVolumeService.this.nStoragePath.lastIndexOf("/"));
                    StatFs fs = new StatFs(storagePath.substring(0, storagePath.lastIndexOf("/")));
                    if (((long) fs.getAvailableBlocks()) * ((long) fs.getBlockSize()) > 2097152) {
                        hasSpace = true;
                    }
                    if (!hasSpace) {
                        Message message = Message.obtain();
                        message.what = -1;
                        ((TIVoiceRecorder) SdcardVolumeService.this.nVoiceRecorder).gainVolumeHandler().sendMessage(message);
                    }
                }
            }, 0, 2000);
        }
    }

    public void cancelTimer() {
        if (this.nTimer != null) {
            this.nTimer.cancel();
            this.nTimer.purge();
            this.nTimer = null;
        }
    }

    public void initData(BaseVoiceRecorder recorder, String storagePath) {
        this.nVoiceRecorder = recorder;
        this.nStoragePath = storagePath;
        checkSdcardVolume();
    }
}
