package com.android.phone.recorder;

import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.voicerecorder.BaseVoiceRecorder;
import android.media.voicerecorder.BaseVoiceRecorder.OnVoiceRecorderListener;
import android.media.voicerecorder.CallRecorderFileObserver;
import android.media.voicerecorder.VoiceRecorderFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings.Secure;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Toast;
import com.android.phone.recorder.autorecord.AutoRecordDbHelper;
import com.android.phone.recorder.autorecord.NumberCompareUtils;
import com.huawei.android.app.admin.DeviceRestrictionManager;
import com.huawei.android.os.PowerManagerEx;
import com.huawei.internal.telephony.CallEx;
import com.huawei.internal.telephony.CallManagerEx;
import com.huawei.internal.telephony.ConnectionEx;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VoiceRecorderManager implements OnVoiceRecorderListener {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = VoiceRecorderManager.class.getSimpleName();
    /* access modifiers changed from: private */
    public static Context mContext;
    private int mActiveSub = -1;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String access$700 = VoiceRecorderManager.LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("onReceive() intent = ");
            sb.append(intent);
            sb.append(", context = ");
            sb.append(context);
            Log.d(access$700, sb.toString());
            if (intent != null && context != null) {
                String action = intent.getAction();
                String access$7002 = VoiceRecorderManager.LOG_TAG;
                StringBuilder sb2 = new StringBuilder();
                sb2.append("onReceive() action = ");
                sb2.append(action);
                Log.d(access$7002, sb2.toString());
                if ("com.android.phone.recorder.autorecord.VoiceRecorderManager.deleteNotification".equals(action)) {
                    ((NotificationManager) VoiceRecorderManager.mContext.getSystemService("notification")).cancel(VoiceRecorderManager.LOG_TAG, 0);
                    String type = intent.getStringExtra("delete");
                    String access$7003 = VoiceRecorderManager.LOG_TAG;
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append("onReceive() type = ");
                    sb3.append(type);
                    Log.d(access$7003, sb3.toString());
                    if ("push_type_live".equals(type)) {
                        VoiceRecorderManager.this.value = 0;
                    }
                } else if ("com.android.soundrecorder.RecordListActivity.returnCallRecord".equals(action)) {
                    VoiceRecorderManager.this.value = 0;
                    ((NotificationManager) VoiceRecorderManager.mContext.getSystemService("notification")).cancel(VoiceRecorderManager.LOG_TAG, 0);
                }
            }
        }
    };
    private CallRecorderFileObserver mCallRecorderFileObserver;
    /* access modifiers changed from: private */
    public boolean mEjectSdError = false;
    private final ArrayList<VoiceRecorderListener> mListeners = new ArrayList<>();
    private String mName;
    private String mNumber;
    /* access modifiers changed from: private */
    public final Handler mRecordHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    VoiceRecorderManager.this.onRecordTimeChange(DateUtils.formatElapsedTime(VoiceRecorderManager.this.mRecordTime));
                    VoiceRecorderManager.this.mRecordTime = 1 + VoiceRecorderManager.this.mRecordTime;
                    VoiceRecorderManager.this.mRecordHandler.sendEmptyMessageDelayed(1, 1000);
                    return;
                case 2:
                    if (VoiceRecorderManager.this.mRecorder != null && VoiceRecorderManager.this.mRecorder.isRecording()) {
                        VoiceRecorderManager.this.voiceEnable = false;
                        VoiceRecorderManager.this.mRecorder.stop(10);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    /* access modifiers changed from: private */
    public long mRecordTime = 0;
    /* access modifiers changed from: private */
    public BaseVoiceRecorder mRecorder;
    private String mResultPath;
    private SdReceiver mSdReceiver = new SdReceiver();
    private int mToastThemeID;
    /* access modifiers changed from: private */
    public int value = 0;
    /* access modifiers changed from: private */
    public boolean voiceEnable = true;

    private static class SaveRunnable implements Runnable {
        FileInfo mFileInfo;

        public SaveRunnable(FileInfo fileInfo) {
            this.mFileInfo = fileInfo;
        }

        public void run() {
            AutoRecordDbHelper helper = AutoRecordDbHelper.getInstance(VoiceRecorderManager.mContext);
            helper.deleteRecord(this.mFileInfo.getMFilePath());
            helper.saveRecord(this.mFileInfo);
        }
    }

    class SdReceiver extends BroadcastReceiver {
        SdReceiver() {
        }

        public void onReceive(Context c, Intent intent) {
            if ("android.intent.action.MEDIA_EJECT".equals(intent.getAction()) && VoiceRecorderManager.this.mRecorder != null && VoiceRecorderManager.this.mRecorder.isRecording() && Utils.sIsExternalStorge) {
                VoiceRecorderManager.this.mRecorder.stop(12);
                VoiceRecorderManager.this.mEjectSdError = true;
            }
        }
    }

    public interface VoiceRecorderListener {
        void onRecordStateChange(boolean z);

        void onRecordTimeChange(String str);

        void onRecorderStart();
    }

    public VoiceRecorderManager(Context context) {
        initContext(context);
        initVoiceRecord();
    }

    private static void initContext(Context context) {
        mContext = context;
    }

    private void initVoiceRecord() {
        this.mRecorder = VoiceRecorderFactory.createVoiceRecorder(mContext);
        this.mRecorder.registerForNotifications();
        this.mRecorder.setOnVoiceRecorderListener(this);
        IntentFilter f = new IntentFilter();
        f.addAction("android.intent.action.MEDIA_EJECT");
        f.addAction("android.intent.action.MEDIA_MOUNTED");
        f.addDataScheme("file");
        mContext.registerReceiver(this.mSdReceiver, f);
        IntentFilter notiFilter = new IntentFilter();
        notiFilter.addAction("com.android.phone.recorder.autorecord.VoiceRecorderManager.deleteNotification");
        notiFilter.addAction("com.android.soundrecorder.RecordListActivity.returnCallRecord");
        mContext.registerReceiver(this.mBroadcastReceiver, notiFilter);
        this.mToastThemeID = mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Toast", null, null);
    }

    public void voiceRecord(String name, String number) {
        if (this.mRecorder.isRecording()) {
            this.mRecorder.stop(10);
            onRecordStateChange(false);
        } else if (!getCallsToRecord(mContext).isEmpty()) {
            try {
                this.mName = name;
                this.mNumber = number;
                this.mResultPath = setupPathFileName();
                if (this.mResultPath != null) {
                    if (this.mCallRecorderFileObserver == null) {
                        this.mCallRecorderFileObserver = new CallRecorderFileObserver(this.mResultPath);
                    }
                    this.mCallRecorderFileObserver.setHandler(this.mRecordHandler);
                    this.mRecorder.start(this.mResultPath);
                    this.mCallRecorderFileObserver.startWatching();
                    Context context = mContext;
                    StringBuilder sb = new StringBuilder();
                    sb.append("[voiceRecord] ");
                    sb.append(mContext != null ? mContext.getResources().getString(R.string.record_call) : "");
                    Utils.logForMIIT(context, sb.toString());
                }
                onRecordStateChange(this.mResultPath != null);
            } catch (RuntimeException e) {
                Log.e(LOG_TAG, e.toString());
                ContextThemeWrapper toastThemeCon = new ContextThemeWrapper(mContext, this.mToastThemeID);
                String tostText = mContext.getString(R.string.unknown_error);
                Toast.makeText(toastThemeCon, mContext.getString(R.string.unknown_error), 0).show();
                onRecordStateChange(false);
                RecordRadar.reportRecordFail(tostText);
            }
        }
    }

    public static LinkedList<String> getCallsToRecord(Context context) {
        LinkedList<String> callsToRecord = new LinkedList<>();
        List<CallEx> foregroundCalls = CallManagerEx.getForegroundCalls();
        int callSize = foregroundCalls.size();
        for (int i = 0; i < callSize; i++) {
            CallEx c = (CallEx) foregroundCalls.get(i);
            if (c != null && c.isActive() && ((!Utils.isDsda() && !Utils.isDsds()) || c.getPhone().getSubId() == Utils.getActiveSub(context))) {
                for (ConnectionEx conn : c.getConnections()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(conn.getAddress());
                    sb.append("&");
                    sb.append(conn.getCreateTime());
                    callsToRecord.add(sb.toString());
                }
            }
        }
        if (callsToRecord.isEmpty() != 0) {
            Log.e(LOG_TAG, "No recording call!");
        } else {
            String str = LOG_TAG;
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Recording call size: ");
            sb2.append(callsToRecord.size());
            Log.i(str, sb2.toString());
        }
        return callsToRecord;
    }

    public void onVoiceRecorderEvent(int event) {
        ContextThemeWrapper toastThemeCon = new ContextThemeWrapper(mContext, this.mToastThemeID);
        boolean shouldInteruptThread = true;
        if (event == 5) {
            handleEioEvent(toastThemeCon);
        } else if (event != 28) {
            switch (event) {
                case 257:
                    handleCreateFileErrorEvent(toastThemeCon);
                    break;
                case 258:
                    handleOpenDevErrorEvent(toastThemeCon);
                    break;
                case 259:
                    Log.e(LOG_TAG, "onVoiceRecorderEvent: start error ...");
                    wakeUpScreen();
                    if (!new DeviceRestrictionManager().isMicrophoneDisabled(null)) {
                        Toast.makeText(toastThemeCon, mContext.getString(R.string.unknown_error), 0).show();
                        break;
                    }
                    break;
                case 260:
                    handleReadErrorEvent(toastThemeCon);
                    break;
                default:
                    switch (event) {
                        case 289:
                            Log.i(LOG_TAG, "onVoiceRecorderEvent: start ok...");
                            try {
                                this.mRecordHandler.sendEmptyMessage(1);
                                onRecorderStart();
                                shouldInteruptThread = false;
                                break;
                            } catch (IllegalThreadStateException e) {
                                Log.i(LOG_TAG, "onVoiceRecorderEvent->IllegalThreadStateException");
                                break;
                            }
                        case 290:
                            wakeUpScreen();
                            if (this.mRecorder != null) {
                                handleStopOkEvent(toastThemeCon);
                                break;
                            }
                            break;
                        default:
                            handleDefault(toastThemeCon);
                            break;
                    }
            }
        } else {
            handleEnospcEvent(toastThemeCon);
        }
        notifyRecordStateChange(shouldInteruptThread);
    }

    private void notifyRecordStateChange(boolean shouldInteruptThread) {
        if (shouldInteruptThread && this.mRecorder != null) {
            onRecordStateChange(this.mRecorder.isRecording());
        }
    }

    private void handleDefault(ContextThemeWrapper toastThemeCon) {
        wakeUpScreen();
        String text = mContext.getString(R.string.unknown_error);
        Toast.makeText(toastThemeCon, text, 0).show();
        RecordRadar.reportRecordFail(text);
    }

    private void handleOpenDevErrorEvent(ContextThemeWrapper toastThemeCon) {
        wakeUpScreen();
        Toast.makeText(toastThemeCon, mContext.getString(R.string.open_dev_error), 0).show();
    }

    private void handleCreateFileErrorEvent(ContextThemeWrapper toastThemeCon) {
        wakeUpScreen();
        Toast.makeText(toastThemeCon, mContext.getString(R.string.create_file_error), 0).show();
    }

    private void handleReadErrorEvent(ContextThemeWrapper toastThemeCon) {
        wakeUpScreen();
        if (this.mRecorder.getRecordFilePath() != null) {
            mContext.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(new File(this.mRecorder.getRecordFilePath()))));
        }
        Toast.makeText(toastThemeCon, getToastStringPath(R.string.record_done_notify, this.mRecorder.getRecordFilePath()), 1).show();
    }

    private void handleEioEvent(ContextThemeWrapper toastThemeCon) {
        wakeUpScreen();
        Toast.makeText(toastThemeCon, getToastStringPath(R.string.eject_memory_card_error, this.mRecorder.getRecordFilePath()), 0).show();
    }

    private void handleEnospcEvent(ContextThemeWrapper toastThemeCon) {
        ((Vibrator) mContext.getSystemService("vibrator")).vibrate(500);
        wakeUpScreen();
        String text = getToastStringPath(R.string.insufficient_memory_card_storage_in_recording, this.mRecorder.getRecordFilePath());
        Toast.makeText(toastThemeCon, text, 0).show();
        RecordRadar.reportRecordFail(text);
    }

    private void handleStopOkEvent(ContextThemeWrapper toastThemeCon) {
        if (this.mEjectSdError) {
            String text = getToastStringPath(R.string.eject_memory_card_error, this.mRecorder.getRecordFilePath());
            Toast.makeText(toastThemeCon, text, 0).show();
            this.mEjectSdError = false;
            RecordRadar.reportRecordFail(text);
        } else if (!this.voiceEnable) {
            String text2 = mContext.getString(R.string.create_file_error);
            Toast.makeText(toastThemeCon, text2, 0).show();
            this.voiceEnable = true;
            RecordRadar.reportRecordFail(text2);
        } else {
            File file = new File(this.mRecorder.getRecordFilePath());
            addToMediaDB(file);
            mContext.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(file)));
            recordNotification(this.mRecorder.getRecordFilePath());
        }
    }

    private void addToMediaDB(File file) {
        if (file != null) {
            String absolutePath = file.getAbsolutePath();
            String name = file.getName();
            StringBuilder sb = new StringBuilder();
            sb.append(file.length());
            sb.append("");
            FileInfo fileInfo = new FileInfo(absolutePath, name, sb.toString(), file.lastModified());
            new Thread(new SaveRunnable(fileInfo)).start();
        }
    }

    private String getToastStringPath(int stringId, String path) {
        if (path == null) {
            return mContext.getString(R.string.file_path_error);
        }
        return mContext.getString(stringId, new Object[]{path});
    }

    public String setupPathFileName() {
        return Utils.generateAbsoluteFilePath(mContext, this.mName, this.mNumber);
    }

    public void addVoiceRecorderListener(VoiceRecorderListener listener) {
        String str = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("addVoiceRecorderListener: ");
        sb.append(listener);
        Log.d(str, sb.toString());
        if (!this.mListeners.contains(listener)) {
            this.mListeners.add(listener);
            listener.onRecordTimeChange(null);
            listener.onRecordStateChange(this.mRecorder.isRecording());
        }
    }

    public void removeVoiceRecorderListener(VoiceRecorderListener listener) {
        String str = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("removeVoiceRecorderListener: ");
        sb.append(listener);
        Log.d(str, sb.toString());
        if (this.mListeners.contains(listener)) {
            this.mListeners.remove(listener);
        }
    }

    /* access modifiers changed from: private */
    public void onRecordTimeChange(String time) {
        int listenersSize = this.mListeners.size();
        for (int i = 0; i < listenersSize; i++) {
            ((VoiceRecorderListener) this.mListeners.get(i)).onRecordTimeChange(time);
        }
    }

    private void onRecordStateChange(boolean recording) {
        String str = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("onRecordStateChange recording: ");
        sb.append(recording);
        Log.d(str, sb.toString());
        if (!recording && this.mRecordHandler != null) {
            this.mRecordHandler.removeMessages(1);
            this.mRecordTime = 0;
            if (this.mCallRecorderFileObserver != null) {
                this.mCallRecorderFileObserver.stopWatching();
                this.mCallRecorderFileObserver = null;
            }
        }
        int listenersSize = this.mListeners.size();
        for (int i = 0; i < listenersSize; i++) {
            ((VoiceRecorderListener) this.mListeners.get(i)).onRecordStateChange(recording);
        }
    }

    private void onRecorderStart() {
        Log.d(LOG_TAG, "onRecorderStart");
        int listenersSize = this.mListeners.size();
        for (int i = 0; i < listenersSize; i++) {
            ((VoiceRecorderListener) this.mListeners.get(i)).onRecorderStart();
        }
    }

    private void wakeUpScreen() {
        PowerManager powerManager = (PowerManager) mContext.getSystemService("power");
        if (powerManager != null) {
            PowerManagerEx.wakeUp(powerManager, SystemClock.uptimeMillis(), "wakeUp");
        }
    }

    public void recordNotification(String filePath) {
        this.value++;
        File file = new File(filePath);
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        intent.addFlags(268468224);
        bundle.putString("filePath", null);
        bundle.putString("playUri", Uri.fromFile(file).toString());
        bundle.putString("fileName", file.getName());
        bundle.putBoolean("isCallfolder", true);
        bundle.putBoolean("needReturnNotification", true);
        intent.putExtras(bundle);
        intent.setClassName("com.android.soundrecorder", "com.android.soundrecorder.RecordListActivity");
        PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, 0);
        Builder builder = new Builder(mContext);
        builder.setSmallIcon(R.drawable.ic_phone_sys_call_recoder);
        builder.setAutoCancel(true);
        builder.setDeleteIntent(PendingIntent.getBroadcast(mContext, 0, new Intent("com.android.phone.recorder.autorecord.VoiceRecorderManager.deleteNotification").putExtra("delete", "push_type_live"), 0));
        builder.setTicker(file.getName());
        builder.setContentTitle(String.format(mContext.getResources().getQuantityText(R.plurals.record_success_notification_title, this.value).toString(), new Object[]{Integer.valueOf(this.value)}));
        String contentText = mContext.getString(R.string.record_success_notification_content);
        builder.setContentText(contentText);
        builder.setStyle(new BigTextStyle().bigText(contentText));
        builder.setWhen(System.currentTimeMillis());
        builder.setContentIntent(pi);
        if (mContext.getApplicationInfo().targetSdkVersion >= 26) {
            builder.setChannelId("CallRecordDefault");
        }
        ((NotificationManager) mContext.getSystemService("notification")).notify(LOG_TAG, 0, builder.getNotification());
        if (this.value == 1000) {
            this.value = 0;
        }
    }

    public boolean isRecording() {
        if (this.mRecorder != null) {
            return this.mRecorder.isRecording();
        }
        return false;
    }

    public void setActiveSubscription(int subId) {
        if (this.mActiveSub != subId) {
            int oldSub = this.mActiveSub;
            this.mActiveSub = subId;
            if (isRecording() && oldSub != -1) {
                this.mRecorder.stop(10);
            }
        }
    }

    public void unRegisterSdcardReceiver() {
        if (mContext != null) {
            if (this.mSdReceiver != null) {
                mContext.unregisterReceiver(this.mSdReceiver);
            }
            if (this.mBroadcastReceiver != null) {
                mContext.unregisterReceiver(this.mBroadcastReceiver);
            }
        }
    }

    public boolean isAutoRecordNumber(String number, boolean isStrangerNumber) {
        String str = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("isAutoRecordNumber() , isStrangerNumber = ");
        sb.append(isStrangerNumber);
        Log.d(str, sb.toString());
        ContentResolver cr = mContext.getContentResolver();
        boolean result = false;
        if (Secure.getInt(cr, "enable_record_auto_key", 0) == 0) {
            Log.d(LOG_TAG, "isAutoRecordNumber() autokey false");
            result = false;
        } else if (1 == Secure.getInt(cr, "enable_all_numbers_key", 1)) {
            Log.d(LOG_TAG, "isAutoRecordNumber() all number");
            result = true;
        } else if (1 == Secure.getInt(cr, "enable_custom_list_key", 0)) {
            Log.d(LOG_TAG, "isAutoRecordNumber() custom list");
            if (1 == Secure.getInt(cr, "enable_unknown_numbers_key", 0)) {
                boolean result2 = isStrangerNumber;
                Log.d(LOG_TAG, "isAutoRecordNumber() strange number");
                if (result2) {
                    return result2;
                }
            }
            result = NumberCompareUtils.isAutoRecordNumber(cr, number);
        }
        String str2 = LOG_TAG;
        StringBuilder sb2 = new StringBuilder();
        sb2.append("isAutoRecordNumber() result = ");
        sb2.append(result);
        Log.d(str2, sb2.toString());
        return result;
    }

    public String getAutoRecordNumberName(String number) {
        Log.d(LOG_TAG, "getAutoRecordNumberName()");
        return NumberCompareUtils.getAutoRecordNumberName(mContext.getContentResolver(), number);
    }
}
