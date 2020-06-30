package com.android.phone.recorder.autorecord;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import com.android.phone.recorder.HwCutoutSettingLayout;
import com.android.phone.recorder.R;
import com.android.phone.recorder.StatisticalHelper;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class AutoRecordCall extends PreferenceActivity implements OnPreferenceChangeListener {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable("AutoRecordCall", 3);
    private PreferenceScreen mAutoRecordObj;
    /* access modifiers changed from: private */
    public Context mContext = null;
    /* access modifiers changed from: private */
    public PreferenceScreen mCustNumber;
    private SwitchPreference mRecordAutoSwitch;
    /* access modifiers changed from: private */
    public Handler mUserCountHandler = new Handler() {
        public void handleMessage(Message msg) {
            int userCount = msg.arg1;
            if (AutoRecordCall.DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("mUserCountHandler Message");
                sb.append(msg.what);
                Log.d("AutoRecordCall", sb.toString());
            }
            switch (msg.what) {
                case 1001:
                    if (AutoRecordCall.this.mCustNumber == null) {
                        return;
                    }
                    if (userCount < 1) {
                        AutoRecordCall.this.mCustNumber.setSummary(R.string.undefined);
                        return;
                    }
                    AutoRecordCall.this.mCustNumber.setSummary(AutoRecordCall.this.getResources().getQuantityString(R.plurals.defined, userCount, new Object[]{Integer.valueOf(userCount)}));
                    return;
                case 1002:
                    if (AutoRecordCall.this.mCustNumber == null) {
                        return;
                    }
                    if (userCount < 1) {
                        Secure.putInt(AutoRecordCall.this.mContext.getContentResolver(), "enable_custom_list_key", 0);
                        AutoRecordCall.this.mCustNumber.setSummary(R.string.undefined);
                        return;
                    }
                    AutoRecordCall.this.mCustNumber.setSummary(AutoRecordCall.this.getResources().getQuantityString(R.plurals.defined, userCount, new Object[]{Integer.valueOf(userCount)}));
                    Secure.putInt(AutoRecordCall.this.mContext.getContentResolver(), "enable_custom_list_key", 1);
                    return;
                case 1003:
                    if (userCount > 0) {
                        AutoRecordCall.this.jumpToAutoRecordUserList(null);
                        return;
                    } else {
                        AutoRecordCall.this.addFromContacts();
                        return;
                    }
                default:
                    return;
            }
        }
    };

    private static class FindUserCountThread implements Runnable {
        private int mEvent;
        private WeakReference<Activity> mWeakReference;

        public FindUserCountThread(Activity activity, int event) {
            this.mWeakReference = new WeakReference<>(activity);
            this.mEvent = event;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x0030, code lost:
            if (r10 != null) goto L_0x0032;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0032, code lost:
            r10.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0059, code lost:
            if (r10 != null) goto L_0x0032;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0060, code lost:
            if (com.android.phone.recorder.autorecord.AutoRecordCall.access$500(r1) == null) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0062, code lost:
            r3 = com.android.phone.recorder.autorecord.AutoRecordCall.access$500(r1).obtainMessage(r12.mEvent);
            r3.arg1 = r11;
            r3.sendToTarget();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
            return;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r12 = this;
                java.lang.ref.WeakReference<android.app.Activity> r0 = r12.mWeakReference
                java.lang.Object r0 = r0.get()
                android.app.Activity r0 = (android.app.Activity) r0
                if (r0 == 0) goto L_0x0078
                boolean r1 = r0 instanceof com.android.phone.recorder.autorecord.AutoRecordCall
                if (r1 == 0) goto L_0x0078
                r1 = r0
                com.android.phone.recorder.autorecord.AutoRecordCall r1 = (com.android.phone.recorder.autorecord.AutoRecordCall) r1
                java.lang.String r2 = "content://com.android.phone.autorecord/numbers"
                android.net.Uri r2 = android.net.Uri.parse(r2)
                android.content.ContentResolver r9 = r1.getContentResolver()
                r10 = 0
                r3 = 0
                r11 = r3
                r5 = 0
                r6 = 0
                r7 = 0
                r8 = 0
                r3 = r9
                r4 = r2
                android.database.Cursor r3 = r3.query(r4, r5, r6, r7, r8)     // Catch:{ SQLException -> 0x0038 }
                r10 = r3
                if (r10 == 0) goto L_0x0030
                int r3 = r10.getCount()     // Catch:{ SQLException -> 0x0038 }
                r11 = r3
            L_0x0030:
                if (r10 == 0) goto L_0x005c
            L_0x0032:
                r10.close()
                goto L_0x005c
            L_0x0036:
                r3 = move-exception
                goto L_0x0072
            L_0x0038:
                r3 = move-exception
                boolean r4 = com.android.phone.recorder.autorecord.AutoRecordCall.DEBUG     // Catch:{ all -> 0x0036 }
                if (r4 == 0) goto L_0x0059
                java.lang.String r4 = "AutoRecordCall"
                java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0036 }
                r5.<init>()     // Catch:{ all -> 0x0036 }
                java.lang.String r6 = "getUsersCount occur exception when query!"
                r5.append(r6)     // Catch:{ all -> 0x0036 }
                java.lang.String r6 = r3.toString()     // Catch:{ all -> 0x0036 }
                r5.append(r6)     // Catch:{ all -> 0x0036 }
                java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0036 }
                android.util.Log.e(r4, r5)     // Catch:{ all -> 0x0036 }
            L_0x0059:
                if (r10 == 0) goto L_0x005c
                goto L_0x0032
            L_0x005c:
                android.os.Handler r3 = r1.mUserCountHandler
                if (r3 == 0) goto L_0x0078
                android.os.Handler r3 = r1.mUserCountHandler
                int r4 = r12.mEvent
                android.os.Message r3 = r3.obtainMessage(r4)
                r3.arg1 = r11
                r3.sendToTarget()
                goto L_0x0078
            L_0x0072:
                if (r10 == 0) goto L_0x0077
                r10.close()
            L_0x0077:
                throw r3
            L_0x0078:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.phone.recorder.autorecord.AutoRecordCall.FindUserCountThread.run():void");
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("onPreferenceChange preference = ");
            sb.append(preference.getKey());
            Log.d("AutoRecordCall", sb.toString());
        }
        if (preference == this.mRecordAutoSwitch) {
            boolean mRecordAutoNewValue = ((Boolean) newValue).booleanValue();
            if (mRecordAutoNewValue) {
                StatisticalHelper.report(4030);
            } else {
                StatisticalHelper.report(4031);
            }
            if (DEBUG) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("onPreferenceChange mRecordAutoNewValue = ");
                sb2.append(mRecordAutoNewValue);
                Log.d("AutoRecordCall", sb2.toString());
            }
            Secure.putInt(this.mContext.getContentResolver(), "enable_record_auto_key", mRecordAutoNewValue);
        }
        return true;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("onPreferenceTreeClick preference = ");
            sb.append(preference.getKey());
            Log.d("AutoRecordCall", sb.toString());
        }
        if (preference == this.mAutoRecordObj) {
            Intent intent = new Intent(this, SelectNumberActivity.class);
            intent.setAction("android.intent.action.MAIN");
            try {
                startActivityForResult(intent, 111);
            } catch (Exception e) {
                Log.e("AutoRecordCall", "onPreferenceTreeClick: Exception", e);
            }
        }
        if (preference == this.mCustNumber) {
            updateViewByUserCount(1003);
        }
        return true;
    }

    private int contactPickedCount(Intent data) {
        int count = 0;
        ArrayList<Integer> tempIds = data.getIntegerArrayListExtra("SelItemData_KeyValue");
        if (!NumberCompareUtils.isNullOrEmptyList(tempIds)) {
            count = 0 + tempIds.size();
        } else if (DEBUG) {
            Log.e("AutoRecordCall", "contactPickedCount: Select contact none");
        }
        ArrayList<Integer> tempIds2 = data.getIntegerArrayListExtra("SelItemCalls_KeyValue");
        if (!NumberCompareUtils.isNullOrEmptyList(tempIds2)) {
            return count + tempIds2.size();
        }
        if (!DEBUG) {
            return count;
        }
        Log.d("AutoRecordCall", "contactPickedCount: Select calllog none");
        return count;
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("onActivityResult requestCode = ");
            sb.append(requestCode);
            sb.append(", resultCode = ");
            sb.append(resultCode);
            Log.d("AutoRecordCall", sb.toString());
        }
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 109:
                if (data != null) {
                    handleCodePick(data);
                    break;
                } else {
                    return;
                }
            case 110:
                if (data != null) {
                    handleContactList(resultCode);
                    break;
                } else {
                    return;
                }
            case 111:
                handleSelectNumber(resultCode);
                break;
        }
    }

    private void handleContactList(int resultCode) {
        PreferenceScreen prefSceen = getPreferenceScreen();
        if (this.mCustNumber == null) {
            return;
        }
        if (resultCode == 0) {
            prefSceen.removePreference(this.mCustNumber);
            this.mAutoRecordObj.setSummary(R.string.all_numbers);
            return;
        }
        prefSceen.addPreference(this.mCustNumber);
        updateViewByUserCount(1002);
        this.mAutoRecordObj.setSummary(R.string.user_list);
    }

    private void handleCodePick(Intent data) {
        if (this.mCustNumber != null) {
            int userCount = contactPickedCount(data);
            if (userCount == 0) {
                this.mCustNumber.setSummary(R.string.undefined);
                Secure.putInt(this.mContext.getContentResolver(), "enable_custom_list_key", 0);
                return;
            }
            Secure.putInt(this.mContext.getContentResolver(), "enable_custom_list_key", 1);
            this.mCustNumber.setSummary(getResources().getQuantityString(R.plurals.defined, userCount, new Object[]{Integer.valueOf(userCount)}));
            if (DEBUG) {
                Log.d("AutoRecordCall", "onActivityResult: jumpToAutoRecordUserList");
            }
            jumpToAutoRecordUserList(data);
        }
    }

    private void handleSelectNumber(int resultCode) {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("onActivityResult resultCode = ");
            sb.append(resultCode);
            Log.d("AutoRecordCall", sb.toString());
        }
        Secure.putInt(getContentResolver(), "enable_all_numbers_key", resultCode);
        PreferenceScreen prefSceen = getPreferenceScreen();
        if (DEBUG) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("onActivityResult mAutoRecordCategoryKey2 = ");
            sb2.append(this.mCustNumber);
            Log.d("AutoRecordCall", sb2.toString());
        }
        if (this.mCustNumber == null) {
            return;
        }
        if (resultCode == 1) {
            prefSceen.removePreference(this.mCustNumber);
            this.mAutoRecordObj.setSummary(R.string.all_numbers);
            return;
        }
        prefSceen.addPreference(this.mCustNumber);
        updateViewByUserCount(1002);
        this.mAutoRecordObj.setSummary(R.string.user_list);
    }

    /* access modifiers changed from: private */
    public void addFromContacts() {
        if (DEBUG) {
            Log.d("AutoRecordCall", "addFromContacts");
        }
        Intent addContactsIntent = new Intent();
        addContactsIntent.setPackage("com.android.contacts");
        addContactsIntent.setAction("com.huawei.community.action.MULTIPLE_PICK");
        addContactsIntent.setAction("android.intent.action.PICK");
        addContactsIntent.setType("vnd.android.cursor.dir/phone_v2");
        addContactsIntent.putExtra("com.huawei.community.action.MULTIPLE_PICK", true);
        addContactsIntent.putExtra("com.huawei.community.action.MAX_SELECT_COUNT", 500);
        addContactsIntent.putExtra("com.huawei.community.action.EXPECT_INTEGER_LIST", true);
        try {
            startActivityForResult(addContactsIntent, 109);
        } catch (Exception e) {
            Log.e("AutoRecordCall", "clickAddFromContacts: Exception", e);
        }
    }

    /* access modifiers changed from: private */
    public void jumpToAutoRecordUserList(Intent intent) {
        if (DEBUG) {
            Log.d("AutoRecordCall", "jumpToAutoRecordUserList");
        }
        Intent autoUserRecordIntent = new Intent();
        autoUserRecordIntent.setAction("android.intent.action.MAIN");
        autoUserRecordIntent.setClassName(this, "com.android.phone.recorder.autorecord.AutoRecordUserList");
        Bundle bundle = new Bundle();
        bundle.putParcelable("extra_intent", intent);
        autoUserRecordIntent.putExtras(bundle);
        try {
            startActivityForResult(autoUserRecordIntent, 110);
        } catch (Exception e) {
            Log.e("AutoRecordCall", "clickAddFromContacts: Exception", e);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new HwCutoutSettingLayout(this);
        addPreferencesFromResource(R.xml.record_settings);
        updateListViewHeaderFooterDisappear(getResources(), getListView());
        PreferenceScreen prefSceen = getPreferenceScreen();
        this.mContext = this;
        this.mRecordAutoSwitch = (SwitchPreference) findPreference("button_record_auto_key");
        this.mRecordAutoSwitch.setChecked(Secure.getInt(this.mContext.getContentResolver(), "enable_record_auto_key", 0) != 0);
        this.mAutoRecordObj = (PreferenceScreen) findPreference("button_auto_record_object_key");
        this.mCustNumber = (PreferenceScreen) findPreference("button_custom_list_key");
        if (this.mAutoRecordObj != null) {
            int opened = Secure.getInt(this.mContext.getContentResolver(), "enable_all_numbers_key", 1);
            if (DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("opened = ");
                sb.append(opened);
                Log.d("AutoRecordCall", sb.toString());
            }
            if (opened == 1) {
                this.mAutoRecordObj.setSummary(R.string.all_numbers);
                if (DEBUG) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("onCreate custList = ");
                    sb2.append(this.mCustNumber);
                    Log.d("AutoRecordCall", sb2.toString());
                }
                if (this.mCustNumber != null) {
                    prefSceen.removePreference(this.mCustNumber);
                }
            } else {
                this.mAutoRecordObj.setSummary(R.string.user_list);
                updateViewByUserCount(1001);
            }
        }
        this.mRecordAutoSwitch.setOnPreferenceChangeListener(this);
        if (1 == Secure.getInt(this.mContext.getContentResolver(), "enable_record_auto_key", 0)) {
            if (1 == Secure.getInt(this.mContext.getContentResolver(), "enable_all_numbers_key", 0)) {
                Secure.putInt(this.mContext.getContentResolver(), "enable_custom_list_key", 0);
            } else if (1 == Secure.getInt(this.mContext.getContentResolver(), "enable_custom_list_key", 0)) {
                Secure.putInt(this.mContext.getContentResolver(), "enable_all_numbers_key", 0);
            }
        }
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        updateViewByUserCount(1001);
    }

    private void updateViewByUserCount(int event) {
        new Thread(new FindUserCountThread(this, event)).start();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        onBackPressed();
        return true;
    }

    public void updateListViewHeaderFooterDisappear(Resources resources, ListView listView) {
        if (resources != null && listView != null) {
            listView.setFooterDividersEnabled(false);
            listView.setHeaderDividersEnabled(false);
            listView.setOverscrollFooter(resources.getDrawable(R.color.transparent, null));
            listView.setOverscrollHeader(resources.getDrawable(R.color.transparent, null));
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        if (this.mUserCountHandler != null) {
            this.mUserCountHandler.removeCallbacksAndMessages(null);
            this.mUserCountHandler = null;
        }
    }
}
