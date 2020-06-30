package android.media.voicerecorder;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import java.lang.ref.WeakReference;

public class QCVoiceRecorder extends BaseVoiceRecorder {
    private final native void native_release();

    private final native void native_setup(Object obj) throws IllegalStateException;

    public native void native_start(String str) throws IllegalStateException;

    public native void native_stop();

    public QCVoiceRecorder(Context context) {
        super(context);
        Looper myLooper = Looper.myLooper();
        Looper looper = myLooper;
        if (myLooper != null) {
            this.mEventHandler = new EventHandler(looper);
            return;
        }
        Looper mainLooper = Looper.getMainLooper();
        Looper looper2 = mainLooper;
        if (mainLooper != null) {
            this.mEventHandler = new EventHandler(looper2);
        } else {
            this.mEventHandler = null;
        }
    }

    public void start(String path) {
        this.mRecordFilePath = path;
        if (!TextUtils.isEmpty(path)) {
            this.mState = 2;
            startRecordingCall();
            native_setup(new WeakReference(this));
            native_start(path);
            return;
        }
        Log.e("QcVoiceRecorder", "QCVoiceRecorder: can not start with empty path.");
    }

    public void pause() {
        this.mState = 1;
        this.mPhoneFgRecording.pause();
        native_stop();
    }

    public void stop(int callState) {
        switch (callState) {
            case 10:
                this.mPhoneFgRecording = null;
                break;
            case 11:
                this.mPhoneBgRecording = null;
                break;
            case 12:
                this.mPhoneFgRecording = null;
                this.mPhoneBgRecording = null;
                break;
            default:
                StringBuilder sb = new StringBuilder();
                sb.append("stop RecordingCall error,stop all RecordingCall [callState]=");
                sb.append(callState);
                Log.e("QcVoiceRecorder", sb.toString());
                this.mPhoneFgRecording = null;
                this.mPhoneBgRecording = null;
                break;
        }
        this.mState = 1;
        native_stop();
    }

    public void release() {
        native_release();
    }

    static {
        try {
            System.loadLibrary("voicerecorder");
        } catch (UnsatisfiedLinkError e) {
            Log.e("QcVoiceRecorder", "WARNING: Could not load libvoicerecorder.so");
        }
    }
}
