package com.android.phone.recorder;

import java.io.Serializable;
import java.util.Comparator;

public class FileInfo {
    /* access modifiers changed from: private */
    public long mCreateTime = 0;
    private long mDuration = 0;
    private String mFileName = null;
    private String mFilePath = null;
    private String mFileSize = null;

    public static class ItemComparator implements Serializable, Comparator<FileInfo> {
        private static final long serialVersionUID = 1;

        public int compare(FileInfo lhs, FileInfo rhs) {
            return (int) (rhs.mCreateTime - lhs.mCreateTime);
        }
    }

    public FileInfo() {
    }

    public FileInfo(String filePath, String fileName, String fileSize, long createTime) {
        this.mFilePath = filePath;
        this.mFileName = fileName;
        this.mFileSize = fileSize;
        this.mCreateTime = createTime;
    }

    public String getMFilePath() {
        return this.mFilePath;
    }

    public String getMFileName() {
        return this.mFileName;
    }

    public String getMFileSize() {
        return this.mFileSize;
    }

    public long getMCreateTime() {
        return this.mCreateTime;
    }

    public long getmDuration() {
        return this.mDuration;
    }

    public void setmDuration(long mDuration2) {
        this.mDuration = mDuration2;
    }

    public int hashCode() {
        return super.hashCode();
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof FileInfo)) {
            return false;
        }
        FileInfo fb = (FileInfo) o;
        if (!fb.getMFilePath().equals(this.mFilePath) || fb.getMCreateTime() != this.mCreateTime) {
            z = false;
        }
        return z;
    }
}
