package android.media.voicerecorder;

import android.content.Context;
import com.android.phone.recorder.VoiceRecorderManager;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.internal.telephony.CallEx;
import com.huawei.internal.telephony.ConnectionEx;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RecordingCall {
    private LinkedList<String> mRecordingCall = new LinkedList<>();
    private int recordingCallState;

    public RecordingCall(Context context) {
        this.mRecordingCall = VoiceRecorderManager.getCallsToRecord(context);
    }

    public void pause() {
        this.recordingCallState = 4;
    }

    public void delRecording(String phoneNumber) {
        if (phoneNumber != null) {
            this.mRecordingCall.remove(phoneNumber);
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        Iterator it = this.mRecordingCall.iterator();
        while (it.hasNext()) {
            buf.append((String) it.next());
            buf.append(" ");
        }
        return buf.toString();
    }

    public Boolean equalRecordingCall(List<CallEx> call) {
        if (call.size() == 0) {
            return Boolean.valueOf(false);
        }
        String mRecordingString = toString();
        int validCon = 0;
        for (CallEx c : call) {
            if (c.getConnections().size() > 0) {
                for (ConnectionEx conn : c.getConnections()) {
                    if (conn.getDisconnectTime() == 0) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(conn.getAddress());
                        sb.append("&");
                        sb.append(conn.getCreateTime());
                        if (-1 == mRecordingString.indexOf(sb.toString())) {
                            return Boolean.valueOf(false);
                        }
                        validCon++;
                    }
                }
                continue;
            } else if (!SystemPropertiesEx.getBoolean("ro.config.hw_volte_on", false)) {
                return Boolean.valueOf(false);
            }
        }
        if (validCon == 0) {
            return Boolean.valueOf(false);
        }
        return Boolean.valueOf(true);
    }

    public Boolean equalSingleRecordingCall(CallEx call) {
        String mRecordingString = toString();
        int validCon = 0;
        if (call.getConnections().size() <= 0) {
            return Boolean.valueOf(false);
        }
        for (ConnectionEx conn : call.getConnections()) {
            if (conn.getDisconnectTime() == 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(conn.getAddress());
                sb.append("&");
                sb.append(conn.getCreateTime());
                if (-1 == mRecordingString.indexOf(sb.toString())) {
                    return Boolean.valueOf(false);
                }
                validCon++;
            }
        }
        if (validCon == 0) {
            return Boolean.valueOf(false);
        }
        return Boolean.valueOf(true);
    }

    public LinkedList<String> getRecordingCall() {
        return this.mRecordingCall;
    }

    public int getRecordingCallState() {
        return this.recordingCallState;
    }

    public void setRecordingCallState(int recordingCallState2) {
        this.recordingCallState = recordingCallState2;
    }
}
