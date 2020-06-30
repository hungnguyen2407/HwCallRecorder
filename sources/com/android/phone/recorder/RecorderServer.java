package com.android.phone.recorder;

import android.app.AppOpsManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.android.phone.recorder.IRecordService.Stub;
import com.android.phone.recorder.VoiceRecorderManager.VoiceRecorderListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class RecorderServer extends Service implements VoiceRecorderListener {
    /* access modifiers changed from: private */
    public final ArrayList<IRecordServiceAdapter> adapters = new ArrayList<>();
    private final Stub mBinder = new Stub() {
        public void voiceRecord(String name, String number) {
            if (RecorderServer.this.hasPermission("voiceRecord")) {
                Message msg = Message.obtain(RecorderServer.this.mHandler, 1);
                Bundle bundle = new Bundle();
                bundle.putString("name", name);
                bundle.putString("number", number);
                msg.setData(bundle);
                msg.sendToTarget();
            }
        }

        public void addRecordServiceAdapter(IRecordServiceAdapter adapter) {
            if (RecorderServer.this.hasPermission("addRecordServiceAdapter") && adapter != null) {
                RecorderServer.this.mHandler.obtainMessage(2, adapter).sendToTarget();
            }
        }

        public boolean isRecording() {
            if (!RecorderServer.this.hasPermission("isRecording")) {
                return false;
            }
            return RecorderServer.this.recorderManager.isRecording();
        }

        public void setActiveSubscription(int subId) {
            if (RecorderServer.this.hasPermission("setActiveSubscription")) {
                Message msg = Message.obtain(RecorderServer.this.mHandler, 3);
                Bundle bundle = new Bundle();
                bundle.putInt("sub", subId);
                msg.setData(bundle);
                msg.sendToTarget();
            }
        }

        public boolean isAutoRecordNumber(String number, boolean isStrangerNumber) {
            if (!RecorderServer.this.hasPermission("isAutoRecordNumber") || number == null) {
                return false;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("isStrangerNumber = ");
            sb.append(isStrangerNumber);
            Log.d("RecorderServer", sb.toString());
            return RecorderServer.this.recorderManager.isAutoRecordNumber(number, isStrangerNumber);
        }

        public String getAutoRecordNumberName(String number) {
            if (!RecorderServer.this.hasPermission("getAutoRecordNumberName") || number == null) {
                return null;
            }
            return RecorderServer.this.recorderManager.getAutoRecordNumberName(number);
        }
    };
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            StringBuilder sb = new StringBuilder();
            sb.append("msg: ");
            sb.append(msg.what);
            Log.d("RecorderServer", sb.toString());
            switch (msg.what) {
                case 1:
                    Bundle data = msg.getData();
                    RecorderServer.this.recorderManager.voiceRecord(data.getString("name"), data.getString("number"));
                    return;
                case 2:
                    if (!RecorderServer.this.adapters.contains(msg.obj)) {
                        Log.d("RecorderServer", "MSG_ADD_ADAPTER add adapter");
                        RecorderServer.this.adapters.add((IRecordServiceAdapter) msg.obj);
                        return;
                    }
                    return;
                case 3:
                    RecorderServer.this.recorderManager.setActiveSubscription(msg.getData().getInt("sub"));
                    return;
                default:
                    return;
            }
        }
    };
    /* access modifiers changed from: private */
    public VoiceRecorderManager recorderManager;

    /* access modifiers changed from: private */
    public boolean hasPermission(String tag) {
        if (isWhiteList()) {
            Log.d("RecorderServer", String.format("Call method %s succeed for white list uid", new Object[]{tag}));
            return true;
        } else if (Binder.getCallingUid() == getApplicationInfo().uid) {
            return true;
        } else {
            Log.d("RecorderServer", String.format("Call method %s fail for unallow uid", new Object[]{tag}));
            return false;
        }
    }

    public IBinder onBind(Intent arg0) {
        Log.d("RecorderServer", "voiceRecord service onBind");
        return this.mBinder;
    }

    public void onCreate() {
        Log.d("RecorderServer", "onCreate begin");
        super.onCreate();
        StatisticalHelper.initContext(this);
        setMode(this, 15, getApplicationInfo().uid, 0);
        this.recorderManager = new VoiceRecorderManager(this);
        this.recorderManager.addVoiceRecorderListener(this);
        if (getApplicationInfo().targetSdkVersion >= 26) {
            NotificationChannelManager.getInstance().createChannels(this);
        }
        Log.d("RecorderServer", "onCreate end");
    }

    private boolean setMode(Context context, int code, int uid, int mode) {
        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService("appops");
        try {
            appOpsManager.getClass().getMethod("setMode", new Class[]{Integer.TYPE, Integer.TYPE, String.class, Integer.TYPE}).invoke(appOpsManager, new Object[]{Integer.valueOf(code), Integer.valueOf(uid), getPackageName(), Integer.valueOf(mode)});
            return true;
        } catch (NoSuchMethodException e) {
            Log.e("RecorderServer", "NoSuchMethodException");
            return false;
        } catch (InvocationTargetException e2) {
            Log.e("RecorderServer", "InvocationTargetException");
            return false;
        } catch (IllegalAccessException e3) {
            Log.e("RecorderServer", "IllegalAccessException");
            return false;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d("RecorderServer", "onDestroy");
        this.recorderManager.removeVoiceRecorderListener(this);
        this.recorderManager.unRegisterSdcardReceiver();
        if (getApplicationInfo().targetSdkVersion >= 26) {
            NotificationChannelManager.getInstance().unregisterReceiver(this);
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return 0;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void onRecorderStart() {
        Log.d("RecorderServer", "onRecorderStart");
        int adaptersSize = this.adapters.size();
        for (int i = 0; i < adaptersSize; i++) {
            IRecordServiceAdapter element = (IRecordServiceAdapter) this.adapters.get(i);
            String str = "RecorderServer";
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("onRecorderStart element: ");
                sb.append(element);
                Log.d(str, sb.toString());
                element.onRecordStart();
            } catch (RemoteException e) {
                Log.e("RecorderServer", "calling onRecordStart RemoteException");
            }
        }
    }

    public void onRecordTimeChange(String time) {
        int adaptersSize = this.adapters.size();
        for (int i = 0; i < adaptersSize; i++) {
            try {
                ((IRecordServiceAdapter) this.adapters.get(i)).onRecordTimeChange(time);
            } catch (RemoteException e) {
                Log.e("RecorderServer", "calling onRecordTimeChange RemoteException");
            }
        }
    }

    public void onRecordStateChange(boolean recording) {
        StringBuilder sb = new StringBuilder();
        sb.append("onRecordStateChange: ");
        sb.append(recording);
        Log.d("RecorderServer", sb.toString());
        int adaptersSize = this.adapters.size();
        for (int i = 0; i < adaptersSize; i++) {
            IRecordServiceAdapter element = (IRecordServiceAdapter) this.adapters.get(i);
            String str = "RecorderServer";
            try {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("onRecordStateChange element: ");
                sb2.append(element);
                Log.d(str, sb2.toString());
                element.onRecordStateChange(recording);
            } catch (RemoteException e) {
                Log.e("RecorderServer", "calling onRecordStateChange RemoteException");
            }
        }
    }

    private boolean isWhiteList() {
        try {
            if (Binder.getCallingUid() == getPackageManager().getApplicationInfo("com.android.huawei.smartkey", 1).uid) {
                return true;
            }
            return false;
        } catch (NameNotFoundException e) {
            Log.e("RecorderServer", "isWhiteList NameNotFoundException.");
            return false;
        }
    }
}
