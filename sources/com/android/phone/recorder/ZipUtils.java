package com.android.phone.recorder;

import android.util.Log;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    public static void zipFiles(Collection<File> resFileList, File zipFile) throws IOException {
        zipFiles(resFileList, zipFile, null);
    }

    /* JADX WARNING: Incorrect condition in loop: B:4:0x001f */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void zipFiles(java.util.Collection<java.io.File> r7, java.io.File r8, java.lang.String r9) throws java.io.IOException {
        /*
            long r0 = java.lang.System.currentTimeMillis()
            r2 = 0
            java.util.zip.ZipOutputStream r3 = new java.util.zip.ZipOutputStream     // Catch:{ all -> 0x0060 }
            java.io.BufferedOutputStream r4 = new java.io.BufferedOutputStream     // Catch:{ all -> 0x0060 }
            java.io.FileOutputStream r5 = new java.io.FileOutputStream     // Catch:{ all -> 0x0060 }
            r5.<init>(r8)     // Catch:{ all -> 0x0060 }
            r6 = 1048576(0x100000, float:1.469368E-39)
            r4.<init>(r5, r6)     // Catch:{ all -> 0x0060 }
            r3.<init>(r4)     // Catch:{ all -> 0x0060 }
            r2 = r3
            java.util.Iterator r3 = r7.iterator()     // Catch:{ all -> 0x0060 }
        L_0x001b:
            boolean r4 = r3.hasNext()     // Catch:{ all -> 0x0060 }
            if (r4 == 0) goto L_0x002d
            java.lang.Object r4 = r3.next()     // Catch:{ all -> 0x0060 }
            java.io.File r4 = (java.io.File) r4     // Catch:{ all -> 0x0060 }
            java.lang.String r5 = ""
            zipFile(r4, r2, r5)     // Catch:{ all -> 0x0060 }
            goto L_0x001b
        L_0x002d:
            boolean r3 = android.text.TextUtils.isEmpty(r9)     // Catch:{ all -> 0x0060 }
            if (r3 != 0) goto L_0x0036
            r2.setComment(r9)     // Catch:{ all -> 0x0060 }
        L_0x0036:
            r2.close()     // Catch:{ IOException -> 0x003b }
        L_0x003a:
            goto L_0x0044
        L_0x003b:
            r3 = move-exception
            java.lang.String r4 = "ZipUtils"
            java.lang.String r5 = "zipout close failed"
            android.util.Log.e(r4, r5)
            goto L_0x003a
        L_0x0044:
            java.lang.String r3 = "ZipUtils"
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "Zip attachments files usage "
            r4.append(r5)
            long r5 = java.lang.System.currentTimeMillis()
            long r5 = r5 - r0
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            android.util.Log.i(r3, r4)
            return
        L_0x0060:
            r3 = move-exception
            if (r2 == 0) goto L_0x006f
            r2.close()     // Catch:{ IOException -> 0x0067 }
            goto L_0x006f
        L_0x0067:
            r4 = move-exception
            java.lang.String r5 = "ZipUtils"
            java.lang.String r6 = "zipout close failed"
            android.util.Log.e(r5, r6)
        L_0x006f:
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.phone.recorder.ZipUtils.zipFiles(java.util.Collection, java.io.File, java.lang.String):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:119:?, code lost:
        r7.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x01c7, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x01c8, code lost:
        r1 = r0;
        android.util.Log.d("ZipUtils", "zf close failed!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:124:?, code lost:
        r8.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x01d6, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x01d7, code lost:
        r1 = r0;
        android.util.Log.d("ZipUtils", "out close failed!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:129:?, code lost:
        r9.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:137:?, code lost:
        r7.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x01f5, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:139:0x01f6, code lost:
        r1 = r0;
        android.util.Log.d("ZipUtils", "zf close failed!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:?, code lost:
        r8.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:143:0x0204, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:144:0x0205, code lost:
        r1 = r0;
        android.util.Log.d("ZipUtils", "out close failed!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:147:?, code lost:
        r9.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:155:?, code lost:
        r7.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:156:0x0223, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:157:0x0224, code lost:
        r1 = r0;
        android.util.Log.d("ZipUtils", "zf close failed!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:160:?, code lost:
        r8.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:161:0x0232, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:0x0233, code lost:
        r1 = r0;
        android.util.Log.d("ZipUtils", "out close failed!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:?, code lost:
        r9.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:173:?, code lost:
        r7.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:174:0x0252, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:175:0x0253, code lost:
        r1 = r0;
        android.util.Log.d("ZipUtils", "zf close failed!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:178:?, code lost:
        r8.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:179:0x0261, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:180:0x0262, code lost:
        r1 = r0;
        android.util.Log.d("ZipUtils", "out close failed!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:183:?, code lost:
        r9.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00fe, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00ff, code lost:
        r1 = r0;
        r4 = r19;
        r5 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0107, code lost:
        r4 = r19;
        r5 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x010e, code lost:
        r4 = r19;
        r5 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0115, code lost:
        r4 = r19;
        r5 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0130, code lost:
        r4 = r19;
        r5 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:?, code lost:
        r7.close();
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x0196 A[SYNTHETIC, Splitter:B:100:0x0196] */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x01a7 A[ExcHandler: Exception (e java.lang.Exception), PHI: r7 r8 r9 
      PHI: (r7v11 'zf' java.util.zip.ZipFile) = (r7v0 'zf' java.util.zip.ZipFile), (r7v12 'zf' java.util.zip.ZipFile), (r7v12 'zf' java.util.zip.ZipFile), (r7v12 'zf' java.util.zip.ZipFile) binds: [B:10:0x002a, B:83:0x0167, B:87:0x0173, B:84:?] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r8v11 'out' java.io.OutputStream) = (r8v0 'out' java.io.OutputStream), (r8v14 'out' java.io.OutputStream), (r8v14 'out' java.io.OutputStream), (r8v14 'out' java.io.OutputStream) binds: [B:10:0x002a, B:83:0x0167, B:87:0x0173, B:84:?] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r9v11 'in' java.io.InputStream) = (r9v0 'in' java.io.InputStream), (r9v13 'in' java.io.InputStream), (r9v13 'in' java.io.InputStream), (r9v13 'in' java.io.InputStream) binds: [B:10:0x002a, B:83:0x0167, B:87:0x0173, B:84:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:10:0x002a] */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x01a9 A[ExcHandler: RuntimeException (e java.lang.RuntimeException), PHI: r7 r8 r9 
      PHI: (r7v10 'zf' java.util.zip.ZipFile) = (r7v0 'zf' java.util.zip.ZipFile), (r7v12 'zf' java.util.zip.ZipFile), (r7v12 'zf' java.util.zip.ZipFile), (r7v12 'zf' java.util.zip.ZipFile) binds: [B:10:0x002a, B:83:0x0167, B:87:0x0173, B:84:?] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r8v10 'out' java.io.OutputStream) = (r8v0 'out' java.io.OutputStream), (r8v14 'out' java.io.OutputStream), (r8v14 'out' java.io.OutputStream), (r8v14 'out' java.io.OutputStream) binds: [B:10:0x002a, B:83:0x0167, B:87:0x0173, B:84:?] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r9v10 'in' java.io.InputStream) = (r9v0 'in' java.io.InputStream), (r9v13 'in' java.io.InputStream), (r9v13 'in' java.io.InputStream), (r9v13 'in' java.io.InputStream) binds: [B:10:0x002a, B:83:0x0167, B:87:0x0173, B:84:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:10:0x002a] */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x01ae A[ExcHandler: FileNotFoundException (e java.io.FileNotFoundException), PHI: r7 r8 r9 
      PHI: (r7v8 'zf' java.util.zip.ZipFile) = (r7v0 'zf' java.util.zip.ZipFile), (r7v12 'zf' java.util.zip.ZipFile), (r7v12 'zf' java.util.zip.ZipFile), (r7v12 'zf' java.util.zip.ZipFile) binds: [B:10:0x002a, B:83:0x0167, B:87:0x0173, B:84:?] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r8v8 'out' java.io.OutputStream) = (r8v0 'out' java.io.OutputStream), (r8v14 'out' java.io.OutputStream), (r8v14 'out' java.io.OutputStream), (r8v14 'out' java.io.OutputStream) binds: [B:10:0x002a, B:83:0x0167, B:87:0x0173, B:84:?] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r9v8 'in' java.io.InputStream) = (r9v0 'in' java.io.InputStream), (r9v13 'in' java.io.InputStream), (r9v13 'in' java.io.InputStream), (r9v13 'in' java.io.InputStream) binds: [B:10:0x002a, B:83:0x0167, B:87:0x0173, B:84:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:10:0x002a] */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x01c3 A[SYNTHETIC, Splitter:B:118:0x01c3] */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x01d2 A[SYNTHETIC, Splitter:B:123:0x01d2] */
    /* JADX WARNING: Removed duplicated region for block: B:128:0x01e1 A[SYNTHETIC, Splitter:B:128:0x01e1] */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x01f1 A[SYNTHETIC, Splitter:B:136:0x01f1] */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x0200 A[SYNTHETIC, Splitter:B:141:0x0200] */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x020f A[SYNTHETIC, Splitter:B:146:0x020f] */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x021f A[SYNTHETIC, Splitter:B:154:0x021f] */
    /* JADX WARNING: Removed duplicated region for block: B:159:0x022e A[SYNTHETIC, Splitter:B:159:0x022e] */
    /* JADX WARNING: Removed duplicated region for block: B:164:0x023d A[SYNTHETIC, Splitter:B:164:0x023d] */
    /* JADX WARNING: Removed duplicated region for block: B:172:0x024e A[SYNTHETIC, Splitter:B:172:0x024e] */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x025d A[SYNTHETIC, Splitter:B:177:0x025d] */
    /* JADX WARNING: Removed duplicated region for block: B:182:0x026c A[SYNTHETIC, Splitter:B:182:0x026c] */
    /* JADX WARNING: Removed duplicated region for block: B:186:0x0274 A[SYNTHETIC, Splitter:B:186:0x0274] */
    /* JADX WARNING: Removed duplicated region for block: B:191:0x0283 A[SYNTHETIC, Splitter:B:191:0x0283] */
    /* JADX WARNING: Removed duplicated region for block: B:196:0x0292 A[SYNTHETIC, Splitter:B:196:0x0292] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00fe A[ExcHandler: all (r0v59 'th' java.lang.Throwable A[CUSTOM_DECLARE]), Splitter:B:51:0x00fa] */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x0106 A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:51:0x00fa] */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x010d A[ExcHandler: RuntimeException (e java.lang.RuntimeException), Splitter:B:51:0x00fa] */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0114 A[ExcHandler: FileNotFoundException (e java.io.FileNotFoundException), Splitter:B:51:0x00fa] */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x0167 A[SYNTHETIC, Splitter:B:83:0x0167] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void unZipFile(java.io.File r22, java.lang.String r23) throws java.util.zip.ZipException, java.io.IOException {
        /*
            r1 = r23
            long r2 = java.lang.System.currentTimeMillis()
            r4 = 0
            r5 = 0
            java.io.File r0 = new java.io.File
            r0.<init>(r1)
            r6 = r0
            boolean r0 = r6.exists()
            if (r0 != 0) goto L_0x0022
            boolean r0 = r6.mkdirs()
            if (r0 != 0) goto L_0x0022
            java.lang.String r0 = "ZipUtils"
            java.lang.String r7 = "mkdir failed"
            android.util.Log.e(r0, r7)
            return
        L_0x0022:
            r7 = 0
            r8 = 0
            r0 = 0
            r9 = r0
            java.util.zip.ZipFile r0 = new java.util.zip.ZipFile     // Catch:{ FileNotFoundException -> 0x0242, IOException -> 0x0213, RuntimeException -> 0x01e5, Exception -> 0x01b7, all -> 0x01b1 }
            r10 = r22
            r0.<init>(r10)     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            r7 = r0
            java.util.Enumeration r0 = r7.entries()     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
        L_0x0032:
            r11 = r0
            boolean r0 = r11.hasMoreElements()     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            if (r0 == 0) goto L_0x0177
            java.lang.Object r0 = r11.nextElement()     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            java.util.zip.ZipEntry r0 = (java.util.zip.ZipEntry) r0     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            r12 = r0
            if (r9 == 0) goto L_0x0045
            r9.close()     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
        L_0x0045:
            java.io.InputStream r0 = r7.getInputStream(r12)     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            r9 = r0
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            r0.<init>()     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            r0.append(r1)     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            java.lang.String r13 = java.io.File.separator     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            r0.append(r13)     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            java.lang.String r13 = r12.getName()     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            r0.append(r13)     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            java.lang.String r0 = r0.toString()     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            java.lang.String r13 = new java.lang.String     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            java.lang.String r14 = "8859_1"
            byte[] r14 = r0.getBytes(r14)     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            java.lang.String r15 = "GB2312"
            r13.<init>(r14, r15)     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            java.io.File r0 = new java.io.File     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            r0.<init>(r13)     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            r14 = r0
            checkFile(r6, r14)     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            boolean r0 = r14.exists()     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            if (r0 != 0) goto L_0x00ad
            java.io.File r0 = r14.getParentFile()     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            if (r0 != 0) goto L_0x0085
            goto L_0x00ab
        L_0x0085:
            boolean r15 = r0.exists()     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            if (r15 != 0) goto L_0x009b
            boolean r15 = r0.mkdirs()     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            if (r15 != 0) goto L_0x009b
            java.lang.String r15 = "ZipUtils"
            r16 = r0
            java.lang.String r0 = "mkdir failed"
            android.util.Log.e(r15, r0)     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            goto L_0x00ab
        L_0x009b:
            r16 = r0
            boolean r0 = r14.createNewFile()     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            if (r0 != 0) goto L_0x00ad
            java.lang.String r0 = "ZipUtils"
            java.lang.String r15 = "create new file failed"
            android.util.Log.e(r0, r15)     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
        L_0x00ab:
            r0 = r11
            goto L_0x0032
        L_0x00ad:
            java.io.FileOutputStream r0 = new java.io.FileOutputStream     // Catch:{ all -> 0x0163 }
            r0.<init>(r14)     // Catch:{ all -> 0x0163 }
            r8 = r0
            r0 = 1048576(0x100000, float:1.469368E-39)
            byte[] r15 = new byte[r0]     // Catch:{ all -> 0x0163 }
        L_0x00b7:
            int r1 = r5 + r0
            r0 = 104857600(0x6400000, float:3.6111186E-35)
            if (r1 > r0) goto L_0x00d1
            int r1 = r9.read(r15)     // Catch:{ all -> 0x0163 }
            r18 = r1
            if (r1 <= 0) goto L_0x00d1
            r0 = 0
            r1 = r18
            r8.write(r15, r0, r1)     // Catch:{ all -> 0x0163 }
            int r5 = r5 + r1
            r0 = 1048576(0x100000, float:1.469368E-39)
            r1 = r23
            goto L_0x00b7
        L_0x00d1:
            int r4 = r4 + 1
            r1 = 1024(0x400, float:1.435E-42)
            if (r4 > r1) goto L_0x0150
            if (r5 > r0) goto L_0x0144
            java.lang.String r0 = "ZipUtils"
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x013d }
            r1.<init>()     // Catch:{ all -> 0x013d }
            r19 = r4
            java.lang.String r4 = "Unzip attachments files usage "
            r1.append(r4)     // Catch:{ all -> 0x0136 }
            long r17 = java.lang.System.currentTimeMillis()     // Catch:{ all -> 0x0136 }
            r20 = r5
            long r4 = r17 - r2
            r1.append(r4)     // Catch:{ all -> 0x015c }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x015c }
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x015c }
            r8.close()     // Catch:{ IOException -> 0x011b, FileNotFoundException -> 0x0114, RuntimeException -> 0x010d, Exception -> 0x0106, all -> 0x00fe }
        L_0x00fd:
            goto L_0x0125
        L_0x00fe:
            r0 = move-exception
            r1 = r0
            r4 = r19
            r5 = r20
            goto L_0x0272
        L_0x0106:
            r0 = move-exception
            r4 = r19
            r5 = r20
            goto L_0x01ba
        L_0x010d:
            r0 = move-exception
            r4 = r19
            r5 = r20
            goto L_0x01e8
        L_0x0114:
            r0 = move-exception
            r4 = r19
            r5 = r20
            goto L_0x0245
        L_0x011b:
            r0 = move-exception
            r1 = r0
            java.lang.String r1 = "ZipUtils"
            java.lang.String r4 = "out close failed"
            android.util.Log.d(r1, r4)     // Catch:{ FileNotFoundException -> 0x0114, IOException -> 0x012f, RuntimeException -> 0x010d, Exception -> 0x0106, all -> 0x00fe }
            goto L_0x00fd
        L_0x0125:
            r0 = r11
            r4 = r19
            r5 = r20
            r1 = r23
            goto L_0x0032
        L_0x012f:
            r0 = move-exception
            r4 = r19
            r5 = r20
            goto L_0x0216
        L_0x0136:
            r0 = move-exception
            r20 = r5
            r1 = r0
            r4 = r19
            goto L_0x0165
        L_0x013d:
            r0 = move-exception
            r19 = r4
            r20 = r5
            r1 = r0
            goto L_0x0165
        L_0x0144:
            r19 = r4
            r20 = r5
            java.lang.IllegalStateException r0 = new java.lang.IllegalStateException     // Catch:{ all -> 0x015c }
            java.lang.String r1 = "File being unzipped is too big."
            r0.<init>(r1)     // Catch:{ all -> 0x015c }
            throw r0     // Catch:{ all -> 0x015c }
        L_0x0150:
            r19 = r4
            r20 = r5
            java.lang.IllegalStateException r0 = new java.lang.IllegalStateException     // Catch:{ all -> 0x015c }
            java.lang.String r1 = "Too many files to unzip."
            r0.<init>(r1)     // Catch:{ all -> 0x015c }
            throw r0     // Catch:{ all -> 0x015c }
        L_0x015c:
            r0 = move-exception
            r1 = r0
            r4 = r19
            r5 = r20
            goto L_0x0165
        L_0x0163:
            r0 = move-exception
            r1 = r0
        L_0x0165:
            if (r8 == 0) goto L_0x0176
            r8.close()     // Catch:{ IOException -> 0x016b, FileNotFoundException -> 0x01ae, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
            goto L_0x0176
        L_0x016b:
            r0 = move-exception
            r15 = r0
            java.lang.String r15 = "ZipUtils"
            r21 = r0
            java.lang.String r0 = "out close failed"
            android.util.Log.d(r15, r0)     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
        L_0x0176:
            throw r1     // Catch:{ FileNotFoundException -> 0x01ae, IOException -> 0x01ab, RuntimeException -> 0x01a9, Exception -> 0x01a7 }
        L_0x0177:
            r7.close()     // Catch:{ IOException -> 0x017c }
            goto L_0x0185
        L_0x017c:
            r0 = move-exception
            r1 = r0
            java.lang.String r1 = "ZipUtils"
            java.lang.String r11 = "zf close failed!"
            android.util.Log.d(r1, r11)
        L_0x0185:
            if (r8 == 0) goto L_0x0194
            r8.close()     // Catch:{ IOException -> 0x018b }
            goto L_0x0194
        L_0x018b:
            r0 = move-exception
            r1 = r0
            java.lang.String r1 = "ZipUtils"
            java.lang.String r11 = "out close failed!"
            android.util.Log.d(r1, r11)
        L_0x0194:
            if (r9 == 0) goto L_0x0271
            r9.close()     // Catch:{ IOException -> 0x019b }
        L_0x0199:
            goto L_0x0271
        L_0x019b:
            r0 = move-exception
            r1 = r0
            java.lang.String r1 = "ZipUtils"
            java.lang.String r11 = "in close failed!"
            android.util.Log.d(r1, r11)
            goto L_0x0199
        L_0x01a5:
            r0 = move-exception
            goto L_0x01b4
        L_0x01a7:
            r0 = move-exception
            goto L_0x01ba
        L_0x01a9:
            r0 = move-exception
            goto L_0x01e8
        L_0x01ab:
            r0 = move-exception
            goto L_0x0216
        L_0x01ae:
            r0 = move-exception
            goto L_0x0245
        L_0x01b1:
            r0 = move-exception
            r10 = r22
        L_0x01b4:
            r1 = r0
            goto L_0x0272
        L_0x01b7:
            r0 = move-exception
            r10 = r22
        L_0x01ba:
            java.lang.String r1 = "ZipUtils"
            java.lang.String r11 = "got Exception!"
            android.util.Log.d(r1, r11)     // Catch:{ all -> 0x01a5 }
            if (r7 == 0) goto L_0x01d0
            r7.close()     // Catch:{ IOException -> 0x01c7 }
            goto L_0x01d0
        L_0x01c7:
            r0 = move-exception
            r1 = r0
            java.lang.String r1 = "ZipUtils"
            java.lang.String r11 = "zf close failed!"
            android.util.Log.d(r1, r11)
        L_0x01d0:
            if (r8 == 0) goto L_0x01df
            r8.close()     // Catch:{ IOException -> 0x01d6 }
            goto L_0x01df
        L_0x01d6:
            r0 = move-exception
            r1 = r0
            java.lang.String r1 = "ZipUtils"
            java.lang.String r11 = "out close failed!"
            android.util.Log.d(r1, r11)
        L_0x01df:
            if (r9 == 0) goto L_0x0271
            r9.close()     // Catch:{ IOException -> 0x019b }
            goto L_0x0199
        L_0x01e5:
            r0 = move-exception
            r10 = r22
        L_0x01e8:
            java.lang.String r1 = "ZipUtils"
            java.lang.String r11 = "got RuntimeException!"
            android.util.Log.d(r1, r11)     // Catch:{ all -> 0x01a5 }
            if (r7 == 0) goto L_0x01fe
            r7.close()     // Catch:{ IOException -> 0x01f5 }
            goto L_0x01fe
        L_0x01f5:
            r0 = move-exception
            r1 = r0
            java.lang.String r1 = "ZipUtils"
            java.lang.String r11 = "zf close failed!"
            android.util.Log.d(r1, r11)
        L_0x01fe:
            if (r8 == 0) goto L_0x020d
            r8.close()     // Catch:{ IOException -> 0x0204 }
            goto L_0x020d
        L_0x0204:
            r0 = move-exception
            r1 = r0
            java.lang.String r1 = "ZipUtils"
            java.lang.String r11 = "out close failed!"
            android.util.Log.d(r1, r11)
        L_0x020d:
            if (r9 == 0) goto L_0x0271
            r9.close()     // Catch:{ IOException -> 0x019b }
            goto L_0x0199
        L_0x0213:
            r0 = move-exception
            r10 = r22
        L_0x0216:
            java.lang.String r1 = "ZipUtils"
            java.lang.String r11 = "got IOException!"
            android.util.Log.d(r1, r11)     // Catch:{ all -> 0x01a5 }
            if (r7 == 0) goto L_0x022c
            r7.close()     // Catch:{ IOException -> 0x0223 }
            goto L_0x022c
        L_0x0223:
            r0 = move-exception
            r1 = r0
            java.lang.String r1 = "ZipUtils"
            java.lang.String r11 = "zf close failed!"
            android.util.Log.d(r1, r11)
        L_0x022c:
            if (r8 == 0) goto L_0x023b
            r8.close()     // Catch:{ IOException -> 0x0232 }
            goto L_0x023b
        L_0x0232:
            r0 = move-exception
            r1 = r0
            java.lang.String r1 = "ZipUtils"
            java.lang.String r11 = "out close failed!"
            android.util.Log.d(r1, r11)
        L_0x023b:
            if (r9 == 0) goto L_0x0271
            r9.close()     // Catch:{ IOException -> 0x019b }
            goto L_0x0199
        L_0x0242:
            r0 = move-exception
            r10 = r22
        L_0x0245:
            java.lang.String r1 = "ZipUtils"
            java.lang.String r11 = "file not found!"
            android.util.Log.d(r1, r11)     // Catch:{ all -> 0x01a5 }
            if (r7 == 0) goto L_0x025b
            r7.close()     // Catch:{ IOException -> 0x0252 }
            goto L_0x025b
        L_0x0252:
            r0 = move-exception
            r1 = r0
            java.lang.String r1 = "ZipUtils"
            java.lang.String r11 = "zf close failed!"
            android.util.Log.d(r1, r11)
        L_0x025b:
            if (r8 == 0) goto L_0x026a
            r8.close()     // Catch:{ IOException -> 0x0261 }
            goto L_0x026a
        L_0x0261:
            r0 = move-exception
            r1 = r0
            java.lang.String r1 = "ZipUtils"
            java.lang.String r11 = "out close failed!"
            android.util.Log.d(r1, r11)
        L_0x026a:
            if (r9 == 0) goto L_0x0271
            r9.close()     // Catch:{ IOException -> 0x019b }
            goto L_0x0199
        L_0x0271:
            return
        L_0x0272:
            if (r7 == 0) goto L_0x0281
            r7.close()     // Catch:{ IOException -> 0x0278 }
            goto L_0x0281
        L_0x0278:
            r0 = move-exception
            r11 = r0
            java.lang.String r11 = "ZipUtils"
            java.lang.String r12 = "zf close failed!"
            android.util.Log.d(r11, r12)
        L_0x0281:
            if (r8 == 0) goto L_0x0290
            r8.close()     // Catch:{ IOException -> 0x0287 }
            goto L_0x0290
        L_0x0287:
            r0 = move-exception
            r11 = r0
            java.lang.String r11 = "ZipUtils"
            java.lang.String r12 = "out close failed!"
            android.util.Log.d(r11, r12)
        L_0x0290:
            if (r9 == 0) goto L_0x029f
            r9.close()     // Catch:{ IOException -> 0x0296 }
            goto L_0x029f
        L_0x0296:
            r0 = move-exception
            r11 = r0
            java.lang.String r11 = "ZipUtils"
            java.lang.String r12 = "in close failed!"
            android.util.Log.d(r11, r12)
        L_0x029f:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.phone.recorder.ZipUtils.unZipFile(java.io.File, java.lang.String):void");
    }

    private static void zipFile(File resFile, ZipOutputStream zipout, String rootpath) throws FileNotFoundException, IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(rootpath);
        sb.append(rootpath.trim().length() == 0 ? "" : File.separator);
        sb.append(resFile.getName());
        String rootpath2 = new String(sb.toString().getBytes("8859_1"), "GB2312");
        if (resFile.isDirectory()) {
            File[] fileList = resFile.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    zipFile(file, zipout, rootpath2);
                }
            }
        } else {
            BufferedInputStream in = null;
            try {
                byte[] buffer = new byte[1048576];
                BufferedInputStream in2 = new BufferedInputStream(new FileInputStream(resFile), 1048576);
                zipout.putNextEntry(new ZipEntry(rootpath2));
                while (true) {
                    int read = in2.read(buffer);
                    int realLength = read;
                    if (read == -1) {
                        break;
                    }
                    zipout.write(buffer, 0, realLength);
                }
                zipout.flush();
                zipout.closeEntry();
                try {
                    in2.close();
                } catch (IOException e) {
                    Log.d("ZipUtils", "in close failed!");
                }
            } catch (ZipException e2) {
                Log.d("ZipUtils", "got ZipException!");
                if (in != null) {
                    in.close();
                }
            } catch (IOException e3) {
                Log.d("ZipUtils", "zipFile failed!");
                if (in != null) {
                    in.close();
                }
            } catch (Throwable th) {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e4) {
                        Log.d("ZipUtils", "in close failed!");
                    }
                }
                throw th;
            }
        }
    }

    private static void checkFile(File desDir, File desFile) throws IOException {
        if (!desFile.getCanonicalPath().startsWith(desDir.getCanonicalPath())) {
            throw new IllegalStateException("File is outside extraction target directory");
        }
    }
}
