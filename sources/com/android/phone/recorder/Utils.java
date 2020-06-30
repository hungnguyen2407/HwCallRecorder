package com.android.phone.recorder;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.ContactsContract.Contacts;
import android.telecom.TelecomManager;
import android.telephony.CallerInfoHW;
import android.telephony.HwTelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Toast;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.storage.StorageManagerEx;
import com.huawei.android.os.storage.StorageVolumeEx;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.internal.telephony.CallEx;
import com.huawei.internal.telephony.CallManagerEx;
import com.huawei.internal.telephony.CallerInfoEx;
import com.huawei.internal.telephony.ConnectionEx;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static final boolean CHINA_RELEASE_VERSION = "CN".equalsIgnoreCase(SystemPropertiesEx.get("ro.product.locale.region", ""));
    private static final boolean LOG_DEBUGGABLE;
    public static final String RECORD_FOLDER;
    private static final Uri YELLOW_PAGE_DATA_URI = Uri.parse("content://com.android.contacts.app/yellow_page_data");
    private static final String[] YELLOW_PAGE_TABLE_COLUMNS = {"name", "photo", "photouri", "number"};
    private static final ExecutorService sExecutorService = Executors.newSingleThreadExecutor();
    private static String sExternalStorage;
    private static String sInternalStorage;
    static boolean sIsExternalStorge = true;

    private static class CallerInfoToken {
        public CallerInfoEx currentInfo;

        private CallerInfoToken() {
        }
    }

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("Sounds");
        sb.append(File.separator);
        sb.append("CallRecord");
        RECORD_FOLDER = sb.toString();
        boolean z = true;
        if (!SystemPropertiesEx.getBoolean("ro.debuggable", false) && !SystemPropertiesEx.getBoolean("persist.sys.huawei.debug.on", false)) {
            z = false;
        }
        LOG_DEBUGGABLE = z;
    }

    public static boolean isDsda() {
        return HwTelephonyManager.isDsdaEnabled();
    }

    public static boolean isDsds() {
        return HwTelephonyManager.isDsdsEnabled();
    }

    public static List<CallEx> getFgCalls(int sub) {
        ArrayList<CallEx> calls = new ArrayList<>();
        for (CallEx tempCall : CallManagerEx.getForegroundCalls()) {
            if (!isDsda() && !isDsds()) {
                calls.add(tempCall);
            } else if (tempCall.getPhone().getSubId() == sub) {
                calls.add(tempCall);
            }
        }
        return calls;
    }

    public static List<CallEx> getBackgroundCalls(int sub) {
        ArrayList<CallEx> calls = new ArrayList<>();
        for (CallEx tempCall : CallManagerEx.getBackgroundCalls()) {
            if (tempCall.getPhone().getSubId() == sub) {
                calls.add(tempCall);
            }
        }
        return calls;
    }

    public static List<CallEx> getAltSubActiveFgCalls(int sub) {
        ArrayList<CallEx> calls = new ArrayList<>();
        for (CallEx tempCall : CallManagerEx.getForegroundCalls()) {
            if (tempCall.getPhone().getSubId() != sub) {
                calls.add(tempCall);
            }
        }
        return calls;
    }

    public static String getStoragePath(Context context) {
        String rootPath = getSdCardRootPath(context, true);
        if (rootPath == null) {
            rootPath = getSdCardRootPath(context, false);
            sIsExternalStorge = false;
        } else {
            sIsExternalStorge = true;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("setupPathFileName()->mStoragePath=");
        sb.append(rootPath);
        Log.d("Utils", sb.toString());
        int toastThemeID = context.getResources().getIdentifier("androidhwext:style/Theme.Emui.Toast", null, null);
        if (rootPath == null) {
            ContextThemeWrapper toastThemeCon = new ContextThemeWrapper(context, toastThemeID);
            String text = context.getString(R.string.insufficient_memory_card_storage);
            Toast.makeText(toastThemeCon, text, 0).show();
            RecordRadar.reportRecordFail(text);
            return null;
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append(rootPath);
        sb2.append("/");
        sb2.append(RECORD_FOLDER);
        String path = sb2.toString();
        File parent = new File(path);
        if (parent.exists() || parent.mkdirs()) {
            makeNomedia(parent);
            return path;
        }
        ContextThemeWrapper toastThemeCon2 = new ContextThemeWrapper(context, toastThemeID);
        String text2 = context.getString(R.string.create_file_error);
        Toast.makeText(toastThemeCon2, text2, 0).show();
        RecordRadar.reportRecordFail(text2);
        return null;
    }

    public static void makeNomedia(final File path) {
        sExecutorService.execute(new Runnable() {
            public void run() {
                File dir = new File(path, ".nomedia");
                if (!dir.exists() && !dir.mkdirs()) {
                    Log.v("Utils", "initRecordFolder : can create the folder.");
                }
            }
        });
    }

    public static void initStorage(Context context) {
        StorageVolume[] volumes = StorageManagerEx.getVolumeList((StorageManager) context.getSystemService("storage"));
        if (volumes != null) {
            int length = volumes.length;
            for (int i = 0; i < length; i++) {
                StorageVolume volume = volumes[i];
                String path = volume == null ? null : StorageVolumeEx.getPath(volume);
                if (path != null) {
                    if (volume.isEmulated()) {
                        sInternalStorage = path;
                    } else if (!path.toLowerCase(Locale.ENGLISH).startsWith("/mnt/usb") && !path.toLowerCase(Locale.ENGLISH).startsWith("/storage/usb")) {
                        sExternalStorage = path;
                    }
                }
            }
            if (!getLocalPath(sExternalStorage).exists()) {
                sExternalStorage = sInternalStorage;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("got ExternalStorage ");
            sb.append(sExternalStorage);
            sb.append("\n mInternalStorage ");
            sb.append(sInternalStorage);
            Log.v("Utils", sb.toString());
        }
    }

    public static String getExternalStorage() {
        return sExternalStorage;
    }

    public static String getInternalStorage() {
        return sInternalStorage;
    }

    public static File getLocalPath(String path) {
        return path == null ? new File("") : new File(path);
    }

    public static String getSdCardRootPath(Context context, boolean external) {
        StorageVolume[] storageVolumes = StorageManagerEx.getVolumeList((StorageManager) context.getSystemService("storage"));
        if (storageVolumes == null) {
            return null;
        }
        try {
            for (StorageVolume sv : storageVolumes) {
                if (sv.isRemovable() == external) {
                    try {
                        String volumePath = StorageVolumeEx.getPath(sv);
                        File f = new File(volumePath);
                        if (f.exists() && f.canRead() && f.canWrite()) {
                            StatFs interSdStat = new StatFs(volumePath);
                            if (((long) interSdStat.getAvailableBlocks()) * ((long) interSdStat.getBlockSize()) > 2097152) {
                                return f.getAbsolutePath();
                            }
                        }
                    } catch (Exception e) {
                        e = e;
                        StringBuilder sb = new StringBuilder();
                        sb.append("Get path failed");
                        sb.append(e);
                        Log.d("Utils", sb.toString());
                        return null;
                    }
                }
            }
            boolean z = external;
        } catch (Exception e2) {
            e = e2;
            boolean z2 = external;
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Get path failed");
            sb2.append(e);
            Log.d("Utils", sb2.toString());
            return null;
        }
        return null;
    }

    public static File getFormattedFile(String path, String recoredFile) {
        String str;
        File f;
        StringBuilder sb = new StringBuilder();
        sb.append(path);
        sb.append("/");
        sb.append(recoredFile);
        String rawFile = sb.toString();
        int i = 0;
        do {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(rawFile);
            String str2 = "%s.amr";
            Object[] objArr = new Object[1];
            if (i == 0) {
                str = "";
            } else {
                StringBuilder sb3 = new StringBuilder();
                sb3.append("_");
                sb3.append(i);
                str = sb3.toString();
            }
            objArr[0] = str;
            sb2.append(String.format(str2, objArr));
            f = new File(sb2.toString());
            i++;
        } while (f.exists());
        StringBuilder sb4 = new StringBuilder();
        sb4.append("getFormattedFile rawFile = ");
        sb4.append(toLogSafePhoneNumber(rawFile));
        Log.d("Utils", sb4.toString());
        return f;
    }

    public static String generateAbsoluteFilePath(Context context, String name, String phoneNum) {
        String path = getStoragePath(context);
        StringBuilder sb = new StringBuilder();
        sb.append("call records storage path=");
        sb.append(path);
        Log.d("Utils", sb.toString());
        if (path == null) {
            return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        StringBuilder sb2 = new StringBuilder();
        sb2.append(getRecordFileName(context, name, phoneNum));
        sb2.append("_");
        sb2.append(dateFormat.format(new Date()));
        return getFormattedFile(path, sb2.toString()).getAbsolutePath();
    }

    private static String getRecordFileName(Context context, String name, String phoneNum) {
        String finalRecordName;
        String phoneNum2;
        Context context2 = context;
        StringBuilder result = new StringBuilder();
        String name2 = filterEmoji(name);
        boolean nameValid = !TextUtils.isEmpty(name2) && isValidFileName(name2);
        boolean numberValid = !TextUtils.isEmpty(phoneNum) && isValidFileName(phoneNum);
        if (nameValid) {
            finalRecordName = name2;
        } else if (numberValid) {
            finalRecordName = phoneNum;
        } else {
            finalRecordName = context2.getString(R.string.unknown);
        }
        if (!numberValid) {
            finalRecordName = "unknown";
        }
        List<ConnectionEx> connections = CallManagerEx.getActiveFgCall().getConnections();
        int activeConnCount = 0;
        for (ConnectionEx connection : connections) {
            if (connection != null && connection.isActive()) {
                activeConnCount++;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Connection detail : [");
            sb.append(connection);
            sb.append("]");
            Log.i("Utils", sb.toString());
        }
        if (connections.size() <= 1 || activeConnCount <= 1) {
            List list = connections;
            int i = activeConnCount;
            result.append(finalRecordName);
            if (!nameValid || !numberValid) {
                String str = phoneNum;
            } else {
                result.append("@");
                result.append(phoneNum);
            }
            return result.toString();
        }
        String finalRecordName2 = context2.getString(R.string.card_title_conf_call);
        Iterator it = getFgCalls(getActiveSub(context)).iterator();
        while (true) {
            if (!it.hasNext()) {
                List list2 = connections;
                int i2 = activeConnCount;
                phoneNum2 = phoneNum;
                break;
            }
            CallEx call = (CallEx) it.next();
            long time = 0;
            ConnectionEx latestActiveConnection = null;
            if (call == null || !call.isActive()) {
                name2 = name2;
                connections = connections;
                activeConnCount = activeConnCount;
            } else {
                for (ConnectionEx connection2 : call.getConnections()) {
                    String name3 = name2;
                    StringBuilder sb2 = new StringBuilder();
                    List list3 = connections;
                    sb2.append("connection.isActive = ");
                    sb2.append(connection2.isActive());
                    sb2.append(", connection.getCreateTime() = ");
                    int activeConnCount2 = activeConnCount;
                    sb2.append(connection2.getCreateTime());
                    Log.i("Utils", sb2.toString());
                    if (connection2.isActive()) {
                        long t = connection2.getCreateTime();
                        if (t > time) {
                            latestActiveConnection = connection2;
                            time = t;
                        }
                    }
                    name2 = name3;
                    connections = list3;
                    activeConnCount = activeConnCount2;
                }
                List list4 = connections;
                int i3 = activeConnCount;
                String name4 = getCallerInfo(context2, latestActiveConnection);
                if (name4 != null) {
                    phoneNum2 = name4.getPhoneNumber();
                } else {
                    phoneNum2 = phoneNum;
                }
                numberValid = !TextUtils.isEmpty(phoneNum2) && isValidFileName(phoneNum2);
            }
        }
        result.append(finalRecordName2);
        if (numberValid) {
            result.append("@");
            result.append(phoneNum2);
        }
        return result.toString();
    }

    private static CallerInfoEx getCallerInfo(Context context, ConnectionEx c) {
        CallerInfoEx info = null;
        if (c != null) {
            Object userDataObject = c.getUserData();
            if (userDataObject instanceof Uri) {
                info = CallerInfoEx.getCallerInfo(context, (Uri) userDataObject);
                if (info != null) {
                    c.setUserData(info);
                }
            } else {
                if (userDataObject instanceof CallerInfoToken) {
                    info = ((CallerInfoToken) userDataObject).currentInfo;
                } else {
                    info = (CallerInfoEx) userDataObject;
                }
                if (info == null) {
                    String number = c.getAddress();
                    if (!TextUtils.isEmpty(number)) {
                        info = CallerInfoEx.getCallerInfo(context, number);
                        if (info != null) {
                            c.setUserData(info);
                        }
                    }
                }
            }
        }
        return info;
    }

    public static String toLogSafePhoneNumber(String number) {
        if (number == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int numberLength = number.length();
        for (int i = 0; i < numberLength; i++) {
            char c = number.charAt(i);
            if (c == '-' || c == '@' || c == '.') {
                builder.append(c);
            } else {
                builder.append('x');
            }
        }
        return builder.toString();
    }

    private static boolean isValidFileName(CharSequence fileName) {
        return Pattern.matches("^[^\\\\/:*?<>\"|]+$", fileName);
    }

    private static String filterEmoji(CharSequence chars) {
        if (chars == null) {
            return null;
        }
        Matcher matcher = Pattern.compile("[🀀-🏿]|[🐀-🟿]|[☀-⟿]", 66).matcher(chars);
        if (!matcher.find()) {
            return chars.toString();
        }
        Log.i("Utils", "replace emoji");
        return matcher.replaceAll("");
    }

    public static boolean isValidSub(int sub) {
        return sub != -1;
    }

    public static int getActiveSub(Context context) {
        try {
            int sub = ((Integer) TelecomManager.class.getMethod("getActiveSubscription", new Class[0]).invoke((TelecomManager) context.getSystemService("telecom"), new Object[0])).intValue();
            StringBuilder sb = new StringBuilder();
            sb.append("getActiveSub sub ");
            sb.append(sub);
            Log.d("Utils", sb.toString());
            return sub;
        } catch (InvocationTargetException e) {
            Log.d("Utils", "getActiveSub InvocationTargetException");
            return -1;
        } catch (NoSuchMethodException e2) {
            Log.d("Utils", "getActiveSub NoSuchMethodException");
            return -1;
        } catch (IllegalAccessException e3) {
            Log.d("Utils", "getActiveSub IllegalAccessException");
            return -1;
        } catch (IllegalArgumentException e4) {
            Log.d("Utils", "getActiveSub IllegalArgumentException");
            return -1;
        }
    }

    public static Bitmap createRoundPhoto(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int size = bitmap.getWidth() < bitmap.getHeight() ? bitmap.getWidth() : bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(size, size, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Rect rect = new Rect(0, 0, size, size);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(-16777216);
        canvas.drawCircle(((float) size) / 2.0f, ((float) size) / 2.0f, ((float) size) / 2.0f, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        bitmap.recycle();
        return output;
    }

    public static int getDefaultAvatarResId(Context context, int s180DipInPixel, int extent) {
        return getDefaultAvatarResId(extent > s180DipInPixel);
    }

    public static int getDefaultAvatarResId(boolean hires) {
        StringBuilder sb = new StringBuilder();
        sb.append("getDefaultAvatarResId hires:");
        sb.append(hires);
        Log.d("Utils", sb.toString());
        if (hires) {
            return R.drawable.contact_avatar_180_holo;
        }
        return R.drawable.ic_contact_picture_holo_light;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0038, code lost:
        if (r0 != null) goto L_0x003a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003a, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0046, code lost:
        if (r0 == null) goto L_0x0049;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0049, code lost:
        return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int getUriId(android.content.Context r8, java.lang.String r9) {
        /*
            r0 = 0
            r1 = -1
            android.content.ContentResolver r2 = r8.getContentResolver()
            android.net.Uri r3 = android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI     // Catch:{ RuntimeException -> 0x0045, all -> 0x003e }
            java.lang.String r4 = android.net.Uri.encode(r9)     // Catch:{ RuntimeException -> 0x0045, all -> 0x003e }
            android.net.Uri r3 = android.net.Uri.withAppendedPath(r3, r4)     // Catch:{ RuntimeException -> 0x0045, all -> 0x003e }
            java.lang.String r4 = "_id"
            java.lang.String[] r4 = new java.lang.String[]{r4}     // Catch:{ RuntimeException -> 0x0045, all -> 0x003e }
            r5 = 0
            r6 = 0
            r7 = 0
            android.database.Cursor r4 = r2.query(r3, r4, r5, r6, r7)     // Catch:{ RuntimeException -> 0x0045, all -> 0x003e }
            r0 = r4
            if (r0 != 0) goto L_0x0027
            r4 = -1
            if (r0 == 0) goto L_0x0026
            r0.close()
        L_0x0026:
            return r4
        L_0x0027:
            boolean r4 = r0.moveToNext()     // Catch:{ RuntimeException -> 0x0045, all -> 0x003e }
            if (r4 == 0) goto L_0x0038
            java.lang.String r4 = "_id"
            int r4 = r0.getColumnIndex(r4)     // Catch:{ RuntimeException -> 0x0045, all -> 0x003e }
            int r4 = r0.getInt(r4)     // Catch:{ RuntimeException -> 0x0045, all -> 0x003e }
            r1 = r4
        L_0x0038:
            if (r0 == 0) goto L_0x0049
        L_0x003a:
            r0.close()
            goto L_0x0049
        L_0x003e:
            r3 = move-exception
            if (r0 == 0) goto L_0x0044
            r0.close()
        L_0x0044:
            throw r3
        L_0x0045:
            r3 = move-exception
            if (r0 == 0) goto L_0x0049
            goto L_0x003a
        L_0x0049:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.phone.recorder.Utils.getUriId(android.content.Context, java.lang.String):int");
    }

    public static Bitmap getPhotoByNumber(String number, Context context) {
        Log.d("Utils", "getPhotoByNumber()");
        String uriStr = getPhotoUriString(number, context);
        if (uriStr == null) {
            return null;
        }
        InputStream input = Contacts.openContactPhotoInputStream(context.getContentResolver(), Uri.parse(uriStr));
        StringBuilder sb = new StringBuilder();
        sb.append("getPhotoUri() input = ");
        sb.append(input);
        Log.d("Utils", sb.toString());
        return BitmapFactory.decodeStream(input);
    }

    public static String getPhotoUriString(String number, Context context) {
        Log.d("Utils", "getPhotoUri()");
        StringBuilder sb = new StringBuilder();
        sb.append("content://com.android.contacts/data/phones/filter/");
        sb.append(number);
        Cursor cursorCantacts = context.getContentResolver().query(Uri.parse(sb.toString()), null, null, null, null);
        if (cursorCantacts == null) {
            return null;
        }
        if (cursorCantacts.getCount() > 0) {
            cursorCantacts.moveToFirst();
            Long contactID = Long.valueOf(cursorCantacts.getLong(cursorCantacts.getColumnIndex("contact_id")));
            Uri uri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactID.longValue());
            StringBuilder sb2 = new StringBuilder();
            sb2.append("getPhotoUri() contactID = ");
            sb2.append(contactID);
            sb2.append(", uri = ");
            sb2.append(uri);
            Log.d("Utils", sb2.toString());
            if (uri != null) {
                cursorCantacts.close();
                return uri.toString();
            }
        }
        cursorCantacts.close();
        return null;
    }

    public static Cursor getPredefineCursor(Context context, String number) {
        try {
            ContentResolver resolver = context.getContentResolver();
            Cursor c = resolver.query(YELLOW_PAGE_DATA_URI, YELLOW_PAGE_TABLE_COLUMNS, "PHONE_NUMBERS_EQUAL(number,?)", new String[]{number}, null);
            if (c != null) {
                int fixedIndex = getCallerInfoHW(c, number, "number");
                if ((fixedIndex == -1 || !c.moveToPosition(fixedIndex)) && CHINA_RELEASE_VERSION) {
                    String parseResult = FixSpecialNumberUtils.getParseResult(context, number);
                    if (!TextUtils.isEmpty(parseResult)) {
                        c.close();
                        ContentResolver contentResolver = resolver;
                        c = contentResolver.query(YELLOW_PAGE_DATA_URI, YELLOW_PAGE_TABLE_COLUMNS, "PHONE_NUMBERS_EQUAL(number,?)", new String[]{parseResult}, null);
                    }
                }
                if (!(c == null || fixedIndex == -1 || !c.moveToPosition(fixedIndex))) {
                    c.moveToPrevious();
                }
            }
            return c;
        } catch (SQLException e) {
            Log.i("Utils", "SQLException invalid column query yellow_page");
            return null;
        } catch (Exception e2) {
            Log.i("Utils", "invalid column query yellow_page");
            return null;
        }
    }

    public static int getCallerInfoHW(Cursor cursor, String number, String num) {
        try {
            return CallerInfoHW.getInstance().getCallerIndex(cursor, number, num);
        } catch (NoExtAPIException e) {
            Log.i("Utils", "getCallerInfoHW NoExtAPIException!");
            return 0;
        }
    }

    public static Bitmap getPredefinePhoto(Context context, Uri photoUri) {
        Bitmap bitmap = null;
        if (photoUri == null) {
            return null;
        }
        AssetFileDescriptor fd = null;
        FileInputStream fis = null;
        try {
            AssetFileDescriptor fd2 = context.getContentResolver().openAssetFileDescriptor(photoUri, "r");
            if (fd2 != null) {
                fis = fd2.createInputStream();
                bitmap = BitmapFactory.decodeStream(fis);
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
            if (fd2 != null) {
                try {
                    fd2.close();
                } catch (IOException e2) {
                }
            }
        } catch (FileNotFoundException e3) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e4) {
                }
            }
            if (fd != null) {
                fd.close();
            }
        } catch (IOException e5) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e6) {
                }
            }
            if (fd != null) {
                fd.close();
            }
        } catch (IndexOutOfBoundsException e7) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e8) {
                }
            }
            if (fd != null) {
                fd.close();
            }
        } catch (Throwable th) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e9) {
                }
            }
            if (fd != null) {
                try {
                    fd.close();
                } catch (IOException e10) {
                }
            }
            throw th;
        }
        return bitmap;
    }

    private static String getCallingAppName(Context context, String packageName) {
        if (context == null || packageName == null) {
            return "unknown";
        }
        PackageManager pm = context.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = pm.getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            Log.e("Utils", "getCallingAppName() - Can't find ApplicationInfo.");
        }
        return (String) pm.getApplicationLabel(appInfo);
    }

    public static void logForMIIT(Context context, String info) {
        String packageName;
        if (LOG_DEBUGGABLE) {
            if (context == null) {
                packageName = "unknown";
            } else {
                packageName = context.getPackageName();
            }
            String appName = getCallingAppName(context, packageName);
            Log.i("ctaifs", String.format("<%s>[%s][%s]:%s", new Object[]{appName, appName, packageName, info}));
        }
    }
}
