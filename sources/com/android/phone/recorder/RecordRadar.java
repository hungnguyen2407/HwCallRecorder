package com.android.phone.recorder;

import com.android.phone.recorder.IMonitorWrapper.EventStreamWrapper;

public class RecordRadar {
    public static void reportRecordFail(String reason) {
        EventStreamWrapper eStreamWrapper = IMonitorWrapper.openEventStream(907003012);
        if (eStreamWrapper != null) {
            eStreamWrapper.setParam(0, "Call record fail.");
            eStreamWrapper.setParam(1, reason);
            IMonitorWrapper.sendEvent(eStreamWrapper);
            IMonitorWrapper.closeEventStream(eStreamWrapper);
        }
    }

    public static void reportRecordNoStop() {
        EventStreamWrapper eStreamWrapper = IMonitorWrapper.openEventStream(907003013);
        if (eStreamWrapper != null) {
            eStreamWrapper.setParam(0, "Call record should be stop.");
            IMonitorWrapper.sendEvent(eStreamWrapper);
            IMonitorWrapper.closeEventStream(eStreamWrapper);
        }
    }
}
