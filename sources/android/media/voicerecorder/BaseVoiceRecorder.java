package android.media.voicerecorder;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.android.phone.recorder.R;
import com.android.phone.recorder.RecordRadar;
import com.android.phone.recorder.Utils;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.internal.telephony.CallEx;
import com.huawei.internal.telephony.CallManagerEx;
import com.huawei.internal.telephony.ConnectionEx;
import com.huawei.internal.telephony.PhoneEx;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

public abstract class BaseVoiceRecorder {
    protected Context mContext;
    protected EventHandler mEventHandler;
    protected Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            PhoneEx phone;
            if (BaseVoiceRecorder.this.isRecording()) {
                StringBuilder sb = new StringBuilder();
                sb.append("msg: ");
                sb.append(msg.what);
                Log.d("BaseVoiceRecorder", sb.toString());
                switch (msg.what) {
                    case 101:
                        int activeSub = Utils.getActiveSub(BaseVoiceRecorder.this.mContext);
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("activeSub: ");
                        sb2.append(activeSub);
                        Log.d("BaseVoiceRecorder", sb2.toString());
                        if ((Utils.isDsda() || Utils.isDsds()) && -1 != activeSub) {
                            phone = CallManagerEx.getFgPhone(activeSub);
                        } else {
                            phone = CallManagerEx.getFgPhone();
                        }
                        if (BaseVoiceRecorder.this.mPhoneBgRecording != null || (BaseVoiceRecorder.this.mPhoneFgRecording != null && (1 == phone.getPhoneType() || 2 == phone.getPhoneType() || 5 == phone.getPhoneType() || 3 == phone.getPhoneType()))) {
                            BaseVoiceRecorder.this.phoneStateChange();
                            break;
                        }
                        break;
                    case 102:
                        AsyncResultEx r = AsyncResultEx.from(msg.obj);
                        if (r != null) {
                            BaseVoiceRecorder.this.recordingCallDisconnect(ConnectionEx.from(r.getResult()));
                            break;
                        }
                        break;
                }
            }
        }
    };
    protected Boolean mIsCanRecording = Boolean.valueOf(true);
    protected OnVoiceRecorderListener mOnVoiceRecorderListener;
    protected RecordingCall mPhoneBgRecording;
    protected RecordingCall mPhoneFgRecording;
    protected String mRecordFilePath;
    protected Handler mRecordHandler = new Handler() {
        public void handleMessage(Message msg) {
            Message message = Message.obtain();
            StringBuilder sb = new StringBuilder();
            sb.append("msg: ");
            sb.append(msg.what);
            Log.d("BaseVoiceRecorder", sb.toString());
            switch (msg.what) {
                case 13:
                    BaseVoiceRecorder.this.handleFgRecordingCallChange();
                    return;
                case 14:
                    BaseVoiceRecorder.this.changeBgRecordingCall();
                    return;
                case 15:
                    if (BaseVoiceRecorder.this.mPhoneFgRecording == null || 3 != BaseVoiceRecorder.this.mPhoneFgRecording.getRecordingCallState()) {
                        Log.e("BaseVoiceRecorder", "mPhoneFgRecording is null or mPhoneFgRecordingState is not RECORDING");
                        return;
                    }
                    BaseVoiceRecorder.this.mRecordingCall = BaseVoiceRecorder.this.mPhoneFgRecording;
                    BaseVoiceRecorder.this.changeFgRecordingCall();
                    message.what = 16;
                    BaseVoiceRecorder.this.mRecordHandler.sendMessageDelayed(message, 300);
                    return;
                case 16:
                    BaseVoiceRecorder.this.handleRecordingCallStart(message);
                    return;
                default:
                    return;
            }
        }
    };
    protected RecordingCall mRecordingCall = null;
    protected int mState = 1;

    protected class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (!(i == 5 || i == 28)) {
                switch (i) {
                    case 257:
                    case 258:
                    case 259:
                    case 260:
                        break;
                    default:
                        switch (i) {
                            case 289:
                                BaseVoiceRecorder.this.mIsCanRecording = Boolean.valueOf(false);
                                break;
                            case 290:
                                BaseVoiceRecorder.this.mIsCanRecording = Boolean.valueOf(true);
                                BaseVoiceRecorder.this.release();
                                break;
                        }
                }
            }
            BaseVoiceRecorder.this.mState = 3;
            BaseVoiceRecorder.this.release();
            if (BaseVoiceRecorder.this.mOnVoiceRecorderListener != null) {
                BaseVoiceRecorder.this.mOnVoiceRecorderListener.onVoiceRecorderEvent(msg.what);
            }
        }
    }

    public interface OnVoiceRecorderListener {
        void onVoiceRecorderEvent(int i);

        String setupPathFileName();
    }

    public abstract void pause();

    public abstract void release();

    public abstract void start(String str);

    public abstract void stop(int i);

    public BaseVoiceRecorder(Context context) {
        this.mContext = context;
    }

    public String getRecordFilePath() {
        return this.mRecordFilePath;
    }

    public boolean isRecording() {
        return this.mState == 2;
    }

    public void registerForNotifications() {
        CallManagerEx.registerForPreciseCallStateChanged(this.mHandler, 101, null);
        CallManagerEx.registerForDisconnect(this.mHandler, 102, null);
    }

    public void setOnVoiceRecorderListener(OnVoiceRecorderListener listener) {
        this.mOnVoiceRecorderListener = listener;
    }

    /* access modifiers changed from: protected */
    public void phoneStateChange() {
        if (this.mPhoneBgRecording == null && this.mPhoneFgRecording == null) {
            Log.e("BaseVoiceRecorder", "The mPhoneBgRecording  and mPhoneFgRecording is null.");
            return;
        }
        if (this.mPhoneFgRecording != null && this.mPhoneBgRecording != null && 3 == this.mPhoneFgRecording.getRecordingCallState() && 4 == this.mPhoneBgRecording.getRecordingCallState()) {
            Message msg = Message.obtain();
            msg.what = 15;
            this.mRecordHandler.sendMessage(msg);
        } else if (this.mPhoneFgRecording != null && 3 == this.mPhoneFgRecording.getRecordingCallState()) {
            Message msg2 = Message.obtain();
            msg2.what = 13;
            this.mRecordHandler.sendMessage(msg2);
        } else if (this.mPhoneBgRecording == null || 4 != this.mPhoneBgRecording.getRecordingCallState()) {
            Log.e("BaseVoiceRecorder", "phoneStateChange file");
        } else {
            Message msg3 = Message.obtain();
            msg3.what = 14;
            this.mRecordHandler.sendMessage(msg3);
        }
    }

    /* access modifiers changed from: protected */
    public void recordingCallDisconnect(ConnectionEx c) {
        boolean hasStop = false;
        if (this.mPhoneFgRecording == null && this.mPhoneBgRecording == null) {
            Log.e("BaseVoiceRecorder", "recordingCallDisconnect return[mPhoneFgRecording is null and mPhoneBgRecording is null]");
        } else {
            if (this.mPhoneFgRecording != null) {
                LinkedList recordingCall = this.mPhoneFgRecording.getRecordingCall();
                StringBuilder sb = new StringBuilder();
                sb.append(c.getAddress());
                sb.append("&");
                sb.append(c.getCreateTime());
                if (recordingCall.contains(sb.toString())) {
                    if (1 == this.mPhoneFgRecording.getRecordingCall().size()) {
                        stop(10);
                        hasStop = true;
                    } else {
                        RecordingCall recordingCall2 = this.mPhoneFgRecording;
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(c.getAddress());
                        sb2.append("&");
                        sb2.append(c.getCreateTime());
                        recordingCall2.delRecording(sb2.toString());
                    }
                }
            }
            if (this.mPhoneBgRecording != null) {
                LinkedList recordingCall3 = this.mPhoneBgRecording.getRecordingCall();
                StringBuilder sb3 = new StringBuilder();
                sb3.append(c.getAddress());
                sb3.append("&");
                sb3.append(c.getCreateTime());
                if (recordingCall3.contains(sb3.toString())) {
                    if (1 == this.mPhoneBgRecording.getRecordingCall().size()) {
                        stop(11);
                    } else {
                        RecordingCall recordingCall4 = this.mPhoneBgRecording;
                        StringBuilder sb4 = new StringBuilder();
                        sb4.append(c.getAddress());
                        sb4.append("&");
                        sb4.append(c.getCreateTime());
                        recordingCall4.delRecording(sb4.toString());
                    }
                }
            }
        }
        StringBuilder sb5 = new StringBuilder();
        sb5.append("isRecording=");
        sb5.append(isRecording());
        sb5.append(", hasStop=");
        sb5.append(hasStop);
        sb5.append(", hasActiveFgCall=");
        sb5.append(CallManagerEx.hasActiveFgCall());
        Log.i("BaseVoiceRecorder", sb5.toString());
        if (isRecording() && !hasStop && !CallManagerEx.hasActiveFgCall()) {
            Log.e("BaseVoiceRecorder", "has no ActiveFgCall, stop recording.");
            stop(10);
            RecordRadar.reportRecordNoStop();
        }
    }

    /* access modifiers changed from: protected */
    public void changeFgRecordingCall() {
        if (Utils.isDsda() || Utils.isDsds()) {
            changeFgRecordingForMultiCard();
        } else {
            changeFgRecordingForSingleCard();
        }
    }

    private void changeFgRecordingForMultiCard() {
        int activeSub = Utils.getActiveSub(this.mContext);
        if (!Utils.isValidSub(activeSub)) {
            this.mPhoneFgRecording = null;
            this.mRecordingCall = null;
            return;
        }
        CallEx fgCall = CallManagerEx.getActiveFgCall(activeSub);
        if (fgCall == null || !fgCall.isMultiparty()) {
            if (this.mPhoneFgRecording.equalRecordingCall(Utils.getBackgroundCalls(activeSub)).booleanValue() || this.mPhoneFgRecording.equalRecordingCall(Utils.getAltSubActiveFgCalls(activeSub)).booleanValue()) {
                Log.d("BaseVoiceRecorder", "puase");
                pause();
                this.mPhoneFgRecording = null;
            }
        } else if (!this.mPhoneFgRecording.equalRecordingCall(Utils.getFgCalls(activeSub)).booleanValue()) {
            Log.d("BaseVoiceRecorder", "stop fgRecord call");
            stop(10);
            this.mPhoneFgRecording = null;
            this.mRecordingCall = null;
        }
    }

    private void changeFgRecordingForSingleCard() {
        if (CallManagerEx.getActiveFgCall().isMultiparty()) {
            if (!this.mPhoneFgRecording.equalSingleRecordingCall(CallManagerEx.getActiveFgCall()).booleanValue()) {
                stop(10);
                this.mPhoneFgRecording = null;
                this.mRecordingCall = null;
            }
        } else if (this.mPhoneFgRecording.equalRecordingCall(CallManagerEx.getBackgroundCalls()).booleanValue()) {
            pause();
            this.mPhoneFgRecording = null;
        }
    }

    /* access modifiers changed from: protected */
    public void changeBgRecordingCall() {
        if (this.mPhoneBgRecording != null && 4 == this.mPhoneBgRecording.getRecordingCallState()) {
            if (Utils.isDsda() || Utils.isDsds()) {
                int activeSub = Utils.getActiveSub(this.mContext);
                StringBuilder sb = new StringBuilder();
                sb.append("changeBgRecordingCall - activeSub: ");
                sb.append(activeSub);
                Log.d("BaseVoiceRecorder", sb.toString());
                if (!Utils.isValidSub(activeSub)) {
                    this.mPhoneBgRecording = null;
                    return;
                }
                CallEx fgCall = CallManagerEx.getActiveFgCall(activeSub);
                if (fgCall != null && fgCall.isMultiparty()) {
                    this.mPhoneBgRecording = null;
                } else if (this.mPhoneBgRecording.equalRecordingCall(Utils.getFgCalls(activeSub)).booleanValue()) {
                    try {
                        start(setupPathFileName());
                    } catch (IllegalStateException e) {
                        Toast.makeText(this.mContext, this.mContext.getString(R.string.unknown_error), 0).show();
                    }
                    this.mPhoneBgRecording = null;
                }
            } else if (CallManagerEx.getActiveFgCall().isMultiparty()) {
                this.mPhoneBgRecording = null;
            } else if (this.mPhoneBgRecording.equalRecordingCall(CallManagerEx.getForegroundCalls()).booleanValue()) {
                try {
                    start(setupPathFileName());
                } catch (IllegalStateException e2) {
                    Toast.makeText(this.mContext, this.mContext.getString(R.string.unknown_error), 0).show();
                }
                this.mPhoneBgRecording = null;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void startRecordingCall() {
        this.mPhoneFgRecording = new RecordingCall(this.mContext);
        this.mPhoneFgRecording.setRecordingCallState(3);
    }

    /* access modifiers changed from: protected */
    public String setupPathFileName() {
        if (this.mOnVoiceRecorderListener != null) {
            return this.mOnVoiceRecorderListener.setupPathFileName();
        }
        Log.e("BaseVoiceRecorder", "could not find a fragment for an active subscription. Would give default file path");
        String path = Utils.getStoragePath(this.mContext);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        StringBuilder sb = new StringBuilder();
        sb.append(dateFormat);
        sb.append("");
        return Utils.getFormattedFile(path, sb.toString()).getAbsolutePath();
    }

    /* access modifiers changed from: private */
    public void handleRecordingCallStart(Message message) {
        if (true == this.mIsCanRecording.booleanValue()) {
            changeBgRecordingCall();
            if (Utils.isDsda() || Utils.isDsds()) {
                int activeSub = Utils.getActiveSub(this.mContext);
                if (!Utils.isValidSub(activeSub)) {
                    this.mPhoneBgRecording = null;
                } else if (CallManagerEx.getActiveFgCall(activeSub) != null && !CallManagerEx.getActiveFgCall(activeSub).isMultiparty()) {
                    this.mPhoneBgRecording = this.mRecordingCall;
                }
            } else if (!CallManagerEx.getActiveFgCall().isMultiparty()) {
                this.mPhoneBgRecording = this.mRecordingCall;
            }
        } else {
            message.what = 16;
            this.mRecordHandler.sendMessageDelayed(message, 300);
        }
    }

    /* access modifiers changed from: private */
    public void handleFgRecordingCallChange() {
        if (this.mPhoneFgRecording == null || 3 != this.mPhoneFgRecording.getRecordingCallState()) {
            Log.e("BaseVoiceRecorder", "mPhoneFgRecording is null or mPhoneFgRecordingState is not RECORDING");
        } else {
            this.mRecordingCall = this.mPhoneFgRecording;
            changeFgRecordingCall();
            if (Utils.isDsda() || Utils.isDsds()) {
                int activeSub = Utils.getActiveSub(this.mContext);
                StringBuilder sb = new StringBuilder();
                sb.append("activeSub: ");
                sb.append(activeSub);
                Log.d("BaseVoiceRecorder", sb.toString());
                if (!Utils.isValidSub(activeSub)) {
                    this.mPhoneBgRecording = null;
                } else if (CallManagerEx.getActiveFgCall(activeSub) != null && !CallManagerEx.getActiveFgCall(activeSub).isMultiparty() && this.mPhoneFgRecording == null) {
                    this.mPhoneBgRecording = this.mRecordingCall;
                }
            } else if (CallManagerEx.getActiveFgCall() != null && !CallManagerEx.getActiveFgCall().isMultiparty() && this.mPhoneFgRecording == null) {
                this.mPhoneBgRecording = this.mRecordingCall;
            }
        }
    }
}
