package com.android.phone.recorder;

import android.content.Context;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Arrays;

public class FixSpecialNumberUtils {
    private static String areaCodeString = null;
    static final ArrayList<String> mCallChineseAreaFour = new ArrayList<>();
    static final ArrayList<String> mCallChineseAreaThree = new ArrayList<>();
    private static String mFixedPhoneNumber = null;
    private static String resultString = null;
    private static String top2String = null;

    private static boolean parseFixedPhoneNumber() {
        areaCodeString = null;
        if (TextUtils.isEmpty(mFixedPhoneNumber) || mFixedPhoneNumber.length() < 8) {
            return false;
        }
        top2String = mFixedPhoneNumber.substring(0, 2);
        if (top2String.equals("01") || top2String.equals("02")) {
            areaCodeString = mFixedPhoneNumber.substring(0, 3);
            return true;
        }
        areaCodeString = mFixedPhoneNumber.substring(0, 4);
        return true;
    }

    public static String getParseResult(Context ctx, String numberString) {
        resultString = null;
        if (ctx == null) {
            return resultString;
        }
        initArea(ctx, numberString);
        if (!parseFixedPhoneNumber()) {
            return resultString;
        }
        if (areaCodeString.length() == 3 && mCallChineseAreaThree.contains(areaCodeString)) {
            resultString = mFixedPhoneNumber.substring(3);
        } else if (mCallChineseAreaFour.contains(areaCodeString)) {
            resultString = mFixedPhoneNumber.substring(4);
        } else {
            resultString = null;
        }
        return resultString;
    }

    private static void initArea(Context ctx, String numberString) {
        mFixedPhoneNumber = removeDashesAndBlanks(numberString);
        if (mCallChineseAreaThree.size() == 0) {
            String[] threeAreaArray = ctx.getResources().getStringArray(R.array.CALL_AREA_THREE);
            if (threeAreaArray != null) {
                mCallChineseAreaThree.addAll(Arrays.asList(threeAreaArray));
            }
        }
        if (mCallChineseAreaFour.size() == 0) {
            String[] fourAreaArray = ctx.getResources().getStringArray(R.array.CALL_AREA_FOUR);
            if (fourAreaArray != null) {
                mCallChineseAreaFour.addAll(Arrays.asList(fourAreaArray));
            }
        }
    }

    public static String removeDashesAndBlanks(String paramString) {
        if (TextUtils.isEmpty(paramString)) {
            return paramString;
        }
        StringBuilder localStringBuilder = new StringBuilder();
        int paramStringLength = paramString.length();
        for (int i = 0; i < paramStringLength; i++) {
            char c = paramString.charAt(i);
            if (!(c == ' ' || c == '-')) {
                localStringBuilder.append(c);
            }
        }
        return localStringBuilder.toString();
    }
}
