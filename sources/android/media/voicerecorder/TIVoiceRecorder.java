package android.media.voicerecorder;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.android.phone.recorder.R;
import com.android.phone.recorder.RecordRadar;
import com.android.phone.recorder.SdcardVolumeService;
import com.android.phone.recorder.SdcardVolumeService.LocalBinder;
import com.huawei.android.media.AudioManagerEx;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class TIVoiceRecorder extends BaseVoiceRecorder {
    private AudioManager mAudioManager = null;
    private boolean mIsBind = false;
    MediaRecorder mRecorder = null;
    Handler mRecorderHandler = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 10001:
                    TIVoiceRecorder.this.startRecording(message.obj.toString());
                    return;
                case 10002:
                    Log.i("TIVoiceRecorder", "RECORDER_STOP");
                    if (hasMessages(10001)) {
                        removeMessages(10001);
                        TIVoiceRecorder.this.mRetryCnt = 0;
                    }
                    if (TIVoiceRecorder.this.mState == 2) {
                        TIVoiceRecorder.this.stopRecording(message.arg1);
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("state is wrong -> ");
                    sb.append(TIVoiceRecorder.this.mState);
                    Log.e("TIVoiceRecorder", sb.toString());
                    return;
                default:
                    return;
            }
        }
    };
    /* access modifiers changed from: private */
    public int mRetryCnt = 0;
    private String mWhiteList = null;
    Service nSdcardService;
    private ServiceConnection nSdcardServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            TIVoiceRecorder.this.nSdcardService = ((LocalBinder) service).gainSdcardVolumeService();
            ((SdcardVolumeService) TIVoiceRecorder.this.nSdcardService).initData(TIVoiceRecorder.this, TIVoiceRecorder.this.mRecordFilePath);
        }

        public void onServiceDisconnected(ComponentName name) {
            TIVoiceRecorder.this.nSdcardService = null;
        }
    };
    Handler nSdcardVolumeHandler = new Handler() {
        public void handleMessage(Message message) {
            if (message.what == -1) {
                TIVoiceRecorder.this.dealEvent(28);
            }
        }
    };

    public TIVoiceRecorder(Context context) {
        super(context);
        Looper myLooper = Looper.myLooper();
        Looper looper = myLooper;
        if (myLooper != null) {
            this.mEventHandler = new EventHandler(looper);
        } else {
            Looper mainLooper = Looper.getMainLooper();
            Looper looper2 = mainLooper;
            if (mainLooper != null) {
                this.mEventHandler = new EventHandler(looper2);
            } else {
                this.mEventHandler = null;
            }
        }
        this.mRecorder = new MediaRecorder();
        this.mWhiteList = this.mContext.getResources().getString(R.string.filter_lowpriority_pkglist);
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
    }

    public void start(final String path) {
        if (!TextUtils.isEmpty(path)) {
            new Thread(new Runnable() {
                public void run() {
                    Message msg = Message.obtain();
                    msg.what = 10001;
                    msg.obj = path;
                    TIVoiceRecorder.this.mRecorderHandler.sendMessageDelayed(msg, 200);
                }
            }).start();
        } else {
            Log.e("TIVoiceRecorder", "TIVoiceRecorder: can not start with empty path.");
        }
    }

    private int getActiveRecordPid() {
        if (this.mAudioManager == null) {
            return -1;
        }
        String activeRecordPid = this.mAudioManager.getParameters("active_record_pid");
        if (!TextUtils.isEmpty(activeRecordPid)) {
            return Integer.parseInt(activeRecordPid);
        }
        return -1;
    }

    private String getPkgForFocusOcupied() {
        int pid = getActiveRecordPid();
        if (pid == -1) {
            return null;
        }
        String pkg = getPackageNameByPid(pid);
        if (TextUtils.isEmpty(pkg)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("pid = ");
        sb.append(pid);
        sb.append("pkg = ");
        sb.append(pkg);
        Log.i("TIVoiceRecorder", sb.toString());
        return pkg;
    }

    private String getPackageNameByPid(int pid) {
        if (pid <= 0) {
            return null;
        }
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        if (activityManager == null) {
            return null;
        }
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return null;
        }
        String packageName = null;
        Iterator it = appProcesses.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            RunningAppProcessInfo appProcess = (RunningAppProcessInfo) it.next();
            if (appProcess.pid == pid) {
                packageName = appProcess.processName;
                break;
            }
        }
        int indexProcessFlag = -1;
        if (packageName != null) {
            indexProcessFlag = packageName.indexOf(58);
        }
        return indexProcessFlag > 0 ? packageName.substring(0, indexProcessFlag) : packageName;
    }

    private boolean isLowPrioPkgActive(String pkg) {
        if (TextUtils.isEmpty(pkg)) {
            return true;
        }
        if (TextUtils.isEmpty(this.mWhiteList)) {
            return false;
        }
        return this.mWhiteList.contains(pkg);
    }

    /* access modifiers changed from: private */
    public void startRecording(String path) {
        boolean isActiveRecordPid;
        if (path == null) {
            dealEvent(28);
            return;
        }
        this.mRecordFilePath = path;
        if (this.mRecorder == null) {
            this.mRecorder = new MediaRecorder();
        }
        if (!TextUtils.isEmpty(this.mWhiteList)) {
            isActiveRecordPid = !isLowPrioPkgActive(getPkgForFocusOcupied());
        } else {
            isActiveRecordPid = getActiveRecordPid() != -1;
        }
        if (AudioManagerEx.isSourceActive(1) || AudioManagerEx.isSourceActive(4) || AudioManagerEx.isSourceActive(7) || isActiveRecordPid) {
            StringBuilder sb = new StringBuilder();
            sb.append("record source is busy, retry start record cnt ");
            sb.append(this.mRetryCnt);
            Log.i("TIVoiceRecorder", sb.toString());
            if (this.mRetryCnt < 5) {
                Message msg = Message.obtain();
                msg.what = 10001;
                msg.obj = path;
                this.mRecorderHandler.sendMessageDelayed(msg, 100);
                this.mRetryCnt++;
            } else {
                this.mRetryCnt = 0;
                dealEvent(258);
                RecordRadar.reportRecordFail("open device error");
            }
            this.mRecorder = null;
            return;
        }
        this.mRetryCnt = 0;
        try {
            setAudioSource();
        } catch (Exception e) {
            Log.e("TIVoiceRecorder", e.toString());
            RecordRadar.reportRecordFail("setAudioSource exception.");
        }
        try {
            setOutputFormat();
        } catch (Exception e2) {
            Log.e("TIVoiceRecorder", e2.toString());
            RecordRadar.reportRecordFail("setOutputFormat exception.");
        }
        try {
            this.mRecorder.setAudioEncoder(2);
        } catch (Exception e3) {
            Log.e("TIVoiceRecorder", e3.toString());
            RecordRadar.reportRecordFail("setAudioEncoder exception.");
        }
        try {
            this.mRecorder.setOutputFile(path);
        } catch (Exception e4) {
            Log.e("TIVoiceRecorder", e4.toString());
            RecordRadar.reportRecordFail("setOutputFile exception.");
        }
        try {
            this.mRecorder.prepare();
        } catch (IOException e5) {
            Log.i("TIVoiceRecorder", "startRecording()->prepare exception");
            dealEvent(5);
            this.mRecorder = null;
            RecordRadar.reportRecordFail("prepare IOException.");
            return;
        } catch (IllegalStateException e6) {
            Log.e("TIVoiceRecorder", e6.toString());
            RecordRadar.reportRecordFail("prepare IllegalStateException.");
        }
        try {
            this.mRecorder.start();
            startCheckingService();
            setState(2);
            startRecordingCall();
            dealEvent(289);
        } catch (RuntimeException exception) {
            Log.e("TIVoiceRecorder", exception.toString());
            dealEvent(259);
            this.mRecorder = null;
            RecordRadar.reportRecordFail("Record start RuntimeException.");
        }
    }

    public void stop(final int callState) {
        new Thread(new Runnable() {
            public void run() {
                Message msg = Message.obtain();
                msg.what = 10002;
                msg.arg1 = callState;
                TIVoiceRecorder.this.mRecorderHandler.sendMessage(msg);
            }
        }).start();
    }

    /* access modifiers changed from: private */
    public void stopRecording(int callState) {
        StringBuilder sb = new StringBuilder();
        sb.append("stopRecording: ");
        sb.append(callState);
        Log.i("TIVoiceRecorder", sb.toString());
        setState(1);
        if (this.mRecorder == null) {
            Log.e("TIVoiceRecorder", "mRecorder is null");
            return;
        }
        try {
            this.mRecorder.stop();
        } catch (Exception e) {
            Log.e("TIVoiceRecorder", "stopRecording->stopRecording exception");
        }
        this.mRecorder.reset();
        this.mRecorder.release();
        this.mRecorder = null;
        switch (callState) {
            case 10:
                this.mPhoneFgRecording = null;
                break;
            case 11:
                this.mPhoneBgRecording = null;
                break;
            case 12:
                this.mPhoneBgRecording = null;
                this.mPhoneFgRecording = null;
                break;
            default:
                StringBuilder sb2 = new StringBuilder();
                sb2.append("stop RecordingCall error,stop all RecordingCall [callState]=");
                sb2.append(callState);
                Log.e("TIVoiceRecorder", sb2.toString());
                this.mPhoneFgRecording = null;
                this.mPhoneBgRecording = null;
                break;
        }
        stopCheckingService();
        dealEvent(290);
    }

    public void pause() {
        this.mPhoneFgRecording.pause();
        stop(0);
    }

    public void release() {
        stopCheckingService();
        if (this.mRecorder != null) {
            this.mRecorder.reset();
            this.mRecorder.release();
            this.mRecorder = null;
        }
    }

    private void setState(int state) {
        if (state != this.mState) {
            this.mState = state;
        }
    }

    public void dealEvent(int eventType) {
        Message msg = Message.obtain();
        msg.what = eventType;
        this.mEventHandler.sendMessage(msg);
    }

    public Handler gainVolumeHandler() {
        return this.nSdcardVolumeHandler;
    }

    private void startCheckingService() {
        Intent intent = new Intent();
        intent.setClassName(SdcardVolumeService.class.getPackage().getName(), SdcardVolumeService.class.getName());
        this.mIsBind = this.mContext.bindService(intent, this.nSdcardServiceConnection, 1);
    }

    private void stopCheckingService() {
        if (this.nSdcardService != null) {
            ((SdcardVolumeService) this.nSdcardService).cancelTimer();
            this.nSdcardService.stopSelf();
            if (this.mIsBind) {
                try {
                    this.mContext.unbindService(this.nSdcardServiceConnection);
                } catch (IllegalArgumentException e) {
                    Log.i("TIVoiceRecorder", "service not registered when unbind sdcardservice");
                }
                this.mIsBind = false;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setAudioSource() {
        this.mRecorder.setAudioSource(4);
    }

    /* access modifiers changed from: protected */
    public void setOutputFormat() {
        this.mRecorder.setOutputFormat(4);
    }
}
