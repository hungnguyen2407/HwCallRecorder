package com.android.server.telecom.common;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class NumberMarkInfo implements Parcelable {
    public static final Creator<NumberMarkInfo> CREATOR = new Creator<NumberMarkInfo>() {
        public NumberMarkInfo createFromParcel(Parcel in) {
            return new NumberMarkInfo(in);
        }

        public NumberMarkInfo[] newArray(int size) {
            return new NumberMarkInfo[size];
        }
    };
    public String attribute;
    public String classify;
    public String classifyName;
    public String description;
    public String errorMsg;
    public boolean isCloudMark;
    public boolean isVerified;
    public boolean isVip;
    public int markedCount;
    public String name;
    public String number;
    public String supplier;
    public String vipMsg;

    public NumberMarkInfo() {
        this.number = "";
        this.attribute = "";
        this.name = "";
        this.classify = "";
        this.classifyName = "";
        this.isVip = false;
        this.vipMsg = "";
        this.isVerified = false;
        this.isCloudMark = false;
        this.markedCount = 0;
        this.supplier = "";
        this.description = "";
        this.errorMsg = "";
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.number);
        dest.writeString(this.attribute);
        dest.writeString(this.name);
        dest.writeString(this.classify);
        dest.writeString(this.classifyName);
        dest.writeByte(this.isVip ? (byte) 1 : 0);
        dest.writeString(this.vipMsg);
        dest.writeByte(this.isVerified ? (byte) 1 : 0);
        dest.writeByte(this.isCloudMark ? (byte) 1 : 0);
        dest.writeInt(this.markedCount);
        dest.writeString(this.supplier);
        dest.writeString(this.description);
        dest.writeString(this.errorMsg);
    }

    private NumberMarkInfo(Parcel in) {
        this.number = in.readString();
        this.attribute = in.readString();
        this.name = in.readString();
        this.classify = in.readString();
        this.classifyName = in.readString();
        boolean z = false;
        this.isVip = in.readByte() == 1;
        this.vipMsg = in.readString();
        this.isVerified = in.readByte() == 1;
        if (in.readByte() == 1) {
            z = true;
        }
        this.isCloudMark = z;
        this.markedCount = in.readInt();
        this.supplier = in.readString();
        this.description = in.readString();
        this.errorMsg = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("number: ");
        buf.append(MoreStrings.toSafeString(this.number));
        buf.append("attribute: ");
        buf.append(this.attribute);
        buf.append("name: ");
        buf.append(MoreStrings.toSafeString(this.name));
        buf.append("classify: ");
        buf.append(this.classify);
        buf.append("classifyName: ");
        buf.append(MoreStrings.toSafeString(this.classifyName));
        buf.append("isVip: ");
        buf.append(this.isVip);
        buf.append("vipMsg: ");
        buf.append(this.vipMsg);
        buf.append("isVerified: ");
        buf.append(this.isVerified);
        buf.append("isCloudMark: ");
        buf.append(this.isCloudMark);
        buf.append("markedCount: ");
        buf.append(this.markedCount);
        buf.append("supplier: ");
        buf.append(this.supplier);
        buf.append("description: ");
        buf.append(MoreStrings.toSafeString(this.description));
        buf.append("errorMsg: ");
        buf.append(this.errorMsg);
        return buf.toString();
    }
}
