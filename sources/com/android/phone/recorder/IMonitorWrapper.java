package com.android.phone.recorder;

import com.huawei.android.util.IMonitorEx;
import com.huawei.android.util.IMonitorEx.EventStreamEx;

public class IMonitorWrapper {

    public static class EventStreamWrapper {
        private EventStreamEx mEventStream;

        public EventStreamWrapper(EventStreamEx eStream) {
            this.mEventStream = eStream;
        }

        /* access modifiers changed from: private */
        public EventStreamEx getEventStream() {
            return this.mEventStream;
        }

        public EventStreamWrapper setParam(short paramID, String value) {
            this.mEventStream.setParam(this.mEventStream, paramID, value);
            return this;
        }
    }

    public static EventStreamWrapper openEventStream(int eventID) {
        return new EventStreamWrapper(IMonitorEx.openEventStream(eventID));
    }

    public static void closeEventStream(EventStreamWrapper eStream) {
        IMonitorEx.closeEventStream(eStream.getEventStream());
    }

    public static boolean sendEvent(EventStreamWrapper eStream) {
        return IMonitorEx.sendEvent(eStream.getEventStream());
    }
}
