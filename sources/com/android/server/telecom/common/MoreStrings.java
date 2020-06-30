package com.android.server.telecom.common;

public class MoreStrings {
    public static String toSafeString(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        int valueLength = value.length();
        for (int i = 0; i < valueLength; i++) {
            char c = value.charAt(i);
            if (c == '-' || c == '@' || c == '.') {
                builder.append(c);
            } else {
                builder.append('x');
            }
        }
        return builder.toString();
    }
}
