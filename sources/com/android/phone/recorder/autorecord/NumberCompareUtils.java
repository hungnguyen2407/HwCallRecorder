package com.android.phone.recorder.autorecord;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import com.android.phone.recorder.Utils;
import com.huawei.android.os.SystemPropertiesEx;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class NumberCompareUtils {
    private static final Uri CONTENT_URI = Uri.parse("content://com.android.phone.autorecord/numbers");
    private static Method Method_CompareNum = null;
    private static final boolean USE_HW_CALLERINFO = SystemPropertiesEx.getBoolean("ro.config.hw_caller_info", true);
    private static volatile Object mObj = null;

    public static boolean isAutoRecordNumber(ContentResolver cr, String number) {
        StringBuilder sb = new StringBuilder();
        sb.append("isAutoRecordNumber(): number = ");
        sb.append(Utils.toLogSafePhoneNumber(number));
        Log.d("NumberCompareUtils", sb.toString());
        List<String> numbersList = new ArrayList<>();
        ContentResolver contentResolver = cr;
        numbersList.addAll(getNumbersFromCursor(contentResolver.query(CONTENT_URI, new String[]{"number"}, null, null, null)));
        if (numbersList.isEmpty()) {
            Log.d("NumberCompareUtils", "isAutoRecordNumber(): Empty number list");
            return false;
        }
        for (String custNumber : numbersList) {
            if (compareNumber(number, custNumber)) {
                Log.d("NumberCompareUtils", "isAutoRecordNumber(): 2");
                return true;
            }
        }
        Log.d("NumberCompareUtils", "isAutoRecordNumber(): 3");
        return false;
    }

    private static List<String> getNumbersFromCursor(Cursor cursor) {
        List<String> numbersList = new ArrayList<>();
        if (!isNullOrEmptyCursor(cursor)) {
            while (cursor.moveToNext()) {
                numbersList.add(cursor.getString(0));
            }
            cursor.close();
        }
        return numbersList;
    }

    public static String getAutoRecordNumberName(ContentResolver cr, String number) {
        StringBuilder sb = new StringBuilder();
        sb.append("getAutoRecordNumberName() number = ");
        sb.append(Utils.toLogSafePhoneNumber(number));
        Log.d("NumberCompareUtils", sb.toString());
        Cursor cursor = cr.query(CONTENT_URI, new String[]{"name", "number"}, null, null, null);
        if (isNullOrEmptyCursor(cursor)) {
            Log.d("NumberCompareUtils", "getAutoRecordNumberName(): Empty cursor");
            return null;
        }
        String name = null;
        while (true) {
            if (cursor.moveToNext()) {
                if (compareNumber(number, cursor.getString(1))) {
                    name = cursor.getString(0);
                    break;
                }
            } else {
                break;
            }
        }
        cursor.close();
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        return name;
    }

    public static <T> boolean isNullOrEmptyList(List<T> list) {
        if (list == null || list.size() <= 0) {
            return true;
        }
        return false;
    }

    public static boolean isNullOrEmptyCursor(Cursor cursor) {
        if (cursor == null) {
            return true;
        }
        if (cursor.getCount() > 0) {
            return false;
        }
        try {
            cursor.close();
        } catch (Exception e) {
            Log.e("NumberCompareUtils", "", e);
        }
        return true;
    }

    private static boolean compareNumber(String num1, String num2) {
        if (USE_HW_CALLERINFO) {
            return compareNumsHw(num1, num2);
        }
        return PhoneNumberUtils.compare(num1, num2);
    }

    private static boolean compareNumsHw(String num1, String num2) {
        try {
            if (mObj == null) {
                mObj = Class.forName("android.telephony.CallerInfoHW").getConstructor(new Class[0]).newInstance(new Object[0]);
            }
            if (mObj != null) {
                Method_CompareNum = mObj.getClass().getDeclaredMethod("compareNums", new Class[]{String.class, String.class});
                Method_CompareNum.setAccessible(true);
            }
            if (Method_CompareNum != null) {
                return ((Boolean) Method_CompareNum.invoke(mObj, new Object[]{num1, num2})).booleanValue();
            }
        } catch (ClassNotFoundException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("ClassNotFoundException:");
            sb.append(e.toString());
            Log.e("NumberCompareUtils", sb.toString());
        } catch (NoSuchMethodException e2) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("NoSuchMethodException:");
            sb2.append(e2.toString());
            Log.e("NumberCompareUtils", sb2.toString());
        } catch (InstantiationException e3) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append("InstantiationException:");
            sb3.append(e3.toString());
            Log.e("NumberCompareUtils", sb3.toString());
        } catch (IllegalAccessException e4) {
            StringBuilder sb4 = new StringBuilder();
            sb4.append("IllegalAccessException:");
            sb4.append(e4.toString());
            Log.e("NumberCompareUtils", sb4.toString());
        } catch (IllegalArgumentException e5) {
            StringBuilder sb5 = new StringBuilder();
            sb5.append("IllegalArgumentException:");
            sb5.append(e5.toString());
            Log.e("NumberCompareUtils", sb5.toString());
        } catch (InvocationTargetException e6) {
            StringBuilder sb6 = new StringBuilder();
            sb6.append("InvocationTargetException:");
            sb6.append(e6.toString());
            Log.e("NumberCompareUtils", sb6.toString());
        }
        return PhoneNumberUtils.compare(num1, num2);
    }
}
