package com.android.phone.recorder;

import android.content.Context;
import android.util.Log;
import com.huawei.bd.Reporter;

public class StatisticalHelper {
    private static Context mContext;

    public static void initContext(Context context) {
        mContext = context;
    }

    public static void report(int eventID) {
        Log.d("StatisticalHelper", String.format("report eventID(%s) %s", new Object[]{Integer.valueOf(eventID), Boolean.valueOf(Reporter.c(mContext, eventID))}));
    }
}
