package com.android.phone.recorder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class NotificationChannelManager {
    private static NotificationChannelManager instance;
    private static BroadcastReceiver mLocaleChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            NotificationChannelManager.createOrUpdateAll(context);
        }
    };

    public static NotificationChannelManager getInstance() {
        if (instance == null) {
            instance = new NotificationChannelManager();
        }
        return instance;
    }

    public void createChannels(Context context) {
        context.registerReceiver(mLocaleChangeReceiver, new IntentFilter("android.intent.action.LOCALE_CHANGED"));
        createOrUpdateAll(context);
    }

    public void unregisterReceiver(Context context) {
        context.unregisterReceiver(mLocaleChangeReceiver);
    }

    /* access modifiers changed from: private */
    public static void createOrUpdateAll(Context context) {
        createOrUpdateChannel(context, "CallRecordDefault");
    }

    private static void createOrUpdateChannel(Context context, String channelId) {
        getNotificationManager(context).createNotificationChannel(createChannel(context, channelId));
    }

    private static NotificationChannel createChannel(Context context, String channelId) {
        if (((channelId.hashCode() == 267317970 && channelId.equals("CallRecordDefault")) ? (char) 0 : 65535) != 0) {
            return null;
        }
        return new NotificationChannel("CallRecordDefault", context.getText(R.string.app_name), 3);
    }

    private static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(NotificationManager.class);
    }
}
