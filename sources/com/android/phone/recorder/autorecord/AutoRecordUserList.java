package com.android.phone.recorder.autorecord;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.os.StrictMode;
import android.os.StrictMode.VmPolicy;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Data;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.phone.recorder.HwCutoutSettingLayout;
import com.android.phone.recorder.R;
import com.huawei.android.os.SystemPropertiesEx;
import java.util.ArrayList;

public class AutoRecordUserList extends ListActivity {
    /* access modifiers changed from: private */
    public static final String[] COLUMN_NAMES = {"name", "number"};
    /* access modifiers changed from: private */
    public static String[] CONTACTS_COLUMN_NAMES = {"_id", "display_name", "data1"};
    /* access modifiers changed from: private */
    public static String[] CONTACTS_COLUMN_NAMES_CALL_LOG = {"_id", "name", "number"};
    public static final Uri CONTENT_URI = Uri.parse("content://com.android.phone.autorecord/numbers");
    /* access modifiers changed from: private */
    public static final boolean DBG;
    /* access modifiers changed from: private */
    public static final Uri PHONES_WITH_PRESENCE_URI = Data.CONTENT_URI;
    private static final int[] VIEW_NAMES = {R.id.name, R.id.number};
    /* access modifiers changed from: private */
    public LinearLayout empty = null;
    ListView listView;
    private ActionBar mActionBar = null;
    /* access modifiers changed from: private */
    public AutoRecordListAdapterWrapper mAdapter = null;
    private ImageView mBackView = null;
    private ImageView mCanceView = null;
    private ContentObserver mContentObserver;
    /* access modifiers changed from: private */
    public Cursor mCursor = null;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler();
    private boolean mIsAllNumbersSelected = false;
    /* access modifiers changed from: private */
    public boolean mIsAsyncTaskRunning = false;
    /* access modifiers changed from: private */
    public boolean mIsNeedSyncAgain = false;
    private View mLayout;
    private Menu mMenu = null;
    /* access modifiers changed from: private */
    public EditText mNameEditText;
    /* access modifiers changed from: private */
    public Button mOkButton;
    /* access modifiers changed from: private */
    public Param mParam = new Param();
    /* access modifiers changed from: private */
    public EditText mPhoneEditText;
    private ProgressDialog mProgressDialog;
    private QueryHandler mQueryHandler;
    private RelativeLayout mSelectNumLayout = null;
    private TextView mSelectNumTextView = null;
    private TextView mSelectTitleTextView = null;
    private TextView mTitleView = null;

    public static class DeleteDialog extends DialogFragment {
        private static int mCount = 0;

        public static DeleteDialog init(int count) {
            mCount = count;
            return new DeleteDialog();
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            SpannableString spanString = new SpannableString(getText(R.string.Delete));
            spanString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.delete_btn_text)), 0, spanString.length(), 33);
            return new Builder(getActivity()).setMessage(String.format(getResources().getString(R.string.delete_numbers_title), new Object[]{Integer.valueOf(mCount)})).setCancelable(true).setNegativeButton(17039360, new DialogOnClickListener()).setNeutralButton(spanString, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    ((AutoRecordUserList) DeleteDialog.this.getActivity()).deleteSelectedNumbers();
                }
            }).create();
        }
    }

    @SuppressLint({"HandlerLeak"})
    class DeleteNumbers extends Handler implements Runnable {
        int count = 0;
        int index = 0;
        private HandlerThread mHandlerThread;
        StringBuilder selectionBuilder = new StringBuilder();

        public DeleteNumbers(HandlerThread ht) {
            super(ht.getLooper());
            this.mHandlerThread = ht;
        }

        public void start() {
            post(this);
        }

        public void run() {
            StringBuilder sb = this.selectionBuilder;
            sb.append("_id");
            sb.append(" IN (");
            int selectedNumbersIdsSize = AutoRecordUserList.this.mParam.mSelectedNumbersIds.size();
            for (int i = 0; i < selectedNumbersIdsSize; i++) {
                this.selectionBuilder.append(((Long) AutoRecordUserList.this.mParam.mSelectedNumbersIds.get(i)).longValue());
                this.selectionBuilder.append(",");
                this.index++;
                if (this.index == 200) {
                    this.selectionBuilder.setLength(this.selectionBuilder.length() - 1);
                    this.selectionBuilder.append(")");
                    this.index = 0;
                    try {
                        this.count += AutoRecordUserList.this.getContentResolver().delete(AutoRecordUserList.CONTENT_URI, this.selectionBuilder.toString(), null);
                    } catch (RuntimeException e) {
                        Log.e("AutoRecordUserList", "delete record failed!");
                    }
                    this.selectionBuilder.setLength(0);
                    StringBuilder sb2 = this.selectionBuilder;
                    sb2.append("_id");
                    sb2.append(" IN (");
                }
            }
            if (this.index > 0) {
                this.selectionBuilder.setLength(this.selectionBuilder.length() - 1);
                this.selectionBuilder.append(")");
                try {
                    this.count += AutoRecordUserList.this.getContentResolver().delete(AutoRecordUserList.CONTENT_URI, this.selectionBuilder.toString(), null);
                } catch (RuntimeException e2) {
                    Log.e("AutoRecordUserList", "delete record failed!");
                }
            }
            AutoRecordUserList.this.updateOnUIThread();
            if (this.mHandlerThread != null) {
                this.mHandlerThread.quit();
            }
        }
    }

    private static class DialogOnClickListener implements OnClickListener {
        private DialogOnClickListener() {
        }

        public void onClick(DialogInterface dialog, int whichButton) {
        }
    }

    private class OnNumbersItemClickListener implements OnItemClickListener {
        private OnNumbersItemClickListener() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
            AutoRecordUserList.this.handleOnNumbersItemClick(index);
        }
    }

    private class OnNumbersItemLongClickListener implements OnItemLongClickListener {
        private OnNumbersItemLongClickListener() {
        }

        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long id) {
            AutoRecordUserList.this.handleOnNumbersItemLongClick(index);
            return true;
        }
    }

    static class Param implements Parcelable {
        public static final Creator<Param> CREATOR = new Creator<Param>() {
            public Param createFromParcel(Parcel in) {
                return new Param(in);
            }

            public Param[] newArray(int size) {
                return new Param[size];
            }
        };
        int mIconState;
        int mLastState;
        ArrayList<Long> mSelectedNumbersIds;
        int mShowingDialogId;
        int mState;

        public int describeContents() {
            return 0;
        }

        public Param() {
            this.mState = 1;
            this.mLastState = 1;
            this.mSelectedNumbersIds = new ArrayList<>();
            this.mShowingDialogId = -1;
            this.mIconState = 0;
        }

        public Param(Parcel in) {
            this.mState = 1;
            this.mLastState = 1;
            this.mSelectedNumbersIds = new ArrayList<>();
            this.mShowingDialogId = -1;
            this.mIconState = 0;
            if (in != null) {
                this.mState = in.readInt();
                this.mLastState = in.readInt();
                this.mIconState = in.readInt();
                int size = in.readInt();
                long[] ids = new long[size];
                in.readLongArray(ids);
                this.mSelectedNumbersIds.clear();
                for (int i = 0; i < size; i++) {
                    this.mSelectedNumbersIds.add(Long.valueOf(ids[i]));
                }
                this.mShowingDialogId = in.readInt();
            }
        }

        public void writeToParcel(Parcel dest, int flags) {
            if (dest != null) {
                dest.writeInt(this.mState);
                dest.writeInt(this.mLastState);
                dest.writeInt(this.mIconState);
                int size = this.mSelectedNumbersIds.size();
                long[] ids = new long[size];
                for (int i = 0; i < size; i++) {
                    ids[i] = ((Long) this.mSelectedNumbersIds.get(i)).longValue();
                }
                dest.writeInt(size);
                dest.writeLongArray(ids);
                dest.writeInt(this.mShowingDialogId);
            }
        }
    }

    private static class ProgressDialogOnKeyListener implements OnKeyListener {
        private ProgressDialogOnKeyListener() {
        }

        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            return true;
        }
    }

    private class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        /* access modifiers changed from: protected */
        public void onQueryComplete(int token, Object cookie, Cursor c) {
            if (AutoRecordUserList.DBG) {
                AutoRecordUserList autoRecordUserList = AutoRecordUserList.this;
                StringBuilder sb = new StringBuilder();
                sb.append("onQueryComplete: cursor.count=");
                sb.append(c.getCount());
                autoRecordUserList.log(sb.toString());
            }
            AutoRecordUserList.this.mCursor = c;
            AutoRecordUserList.this.setAdapter();
            if (AutoRecordUserList.this.mCursor == null || AutoRecordUserList.this.mCursor.getCount() > 0) {
                AutoRecordUserList.this.empty.setVisibility(8);
            } else {
                AutoRecordUserList.this.empty.setVisibility(0);
            }
            AutoRecordUserList.this.invalidateOptionsMenu();
        }

        /* access modifiers changed from: protected */
        public void onInsertComplete(int token, Object cookie, Uri uri) {
            if (AutoRecordUserList.DBG) {
                AutoRecordUserList.this.log("onInsertComplete: requery");
            }
            AutoRecordUserList.this.mParam.mIconState = 0;
            AutoRecordUserList.this.mAdapter.setIconState(0);
            AutoRecordUserList.this.reQuery();
        }

        /* access modifiers changed from: protected */
        public void onUpdateComplete(int token, Object cookie, int result) {
            if (AutoRecordUserList.DBG) {
                AutoRecordUserList.this.log("onUpdateComplete: requery");
            }
            AutoRecordUserList.this.mParam.mIconState = 0;
            AutoRecordUserList.this.mAdapter.setIconState(0);
            AutoRecordUserList.this.reQuery();
        }

        /* access modifiers changed from: protected */
        public void onDeleteComplete(int token, Object cookie, int result) {
            if (AutoRecordUserList.DBG) {
                AutoRecordUserList.this.log("onDeleteComplete: requery");
            }
            AutoRecordUserList.this.mParam.mIconState = 0;
            AutoRecordUserList.this.mAdapter.setIconState(0);
            AutoRecordUserList.this.reQuery();
        }
    }

    static {
        boolean z = false;
        if (SystemPropertiesEx.getInt("ro.debuggable", 0) == 1) {
            z = true;
        }
        DBG = z;
    }

    public void onCreate(Bundle savedInstanceState) {
        if (DBG) {
            log("onCreate()...");
        }
        super.onCreate(savedInstanceState);
        if (Log.isLoggable("AutoRecordUserListLeaks", 3)) {
            StrictMode.setVmPolicy(new VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().detectActivityLeaks().penaltyLog().build());
        }
        this.mProgressDialog = new ProgressDialog(this);
        this.mProgressDialog.setProgressStyle(0);
        this.mProgressDialog.setMessage(getString(R.string.please_wait));
        this.mProgressDialog.setCanceledOnTouchOutside(false);
        this.mProgressDialog.setOnKeyListener(new ProgressDialogOnKeyListener());
        init(savedInstanceState);
        this.mLayout = getLayoutInflater().inflate(R.layout.auto_record_create_number_dialog, null);
        this.mPhoneEditText = (EditText) this.mLayout.findViewById(R.id.auto_record_create_phone);
        this.mNameEditText = (EditText) this.mLayout.findViewById(R.id.auto_record_create_name);
        this.mOkButton = (Button) this.mLayout.findViewById(R.id.auto_record_create_ok_button);
        this.mPhoneEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int end, int count) {
                if (s.toString().trim().length() == 0) {
                    AutoRecordUserList.this.mOkButton.setEnabled(false);
                } else {
                    AutoRecordUserList.this.mOkButton.setEnabled(true);
                }
            }

            public void afterTextChanged(Editable arg0) {
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
        });
        this.mOkButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                String phone = AutoRecordUserList.this.mPhoneEditText.getText().toString();
                String name = AutoRecordUserList.this.mNameEditText.getText().toString();
                if (!TextUtils.isEmpty(phone)) {
                    AutoRecordUserList.this.createNewAutoRecordNumber(phone, name);
                }
            }
        });
        this.mContentObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                AutoRecordUserList autoRecordUserList = AutoRecordUserList.this;
                StringBuilder sb = new StringBuilder();
                sb.append("onChange selfChange = ");
                sb.append(selfChange);
                autoRecordUserList.log(sb.toString());
                AutoRecordUserList.this.sync();
            }
        };
        getContentResolver().registerContentObserver(Uri.parse("content://com.android.contacts/data/"), true, this.mContentObserver);
        Intent intent = getIntent();
        StringBuilder sb = new StringBuilder();
        sb.append("onCreate intent = ");
        sb.append(intent);
        log(sb.toString());
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            StringBuilder sb2 = new StringBuilder();
            sb2.append("onCreate bundle = ");
            sb2.append(bundle);
            log(sb2.toString());
            if (bundle != null) {
                Intent data = (Intent) bundle.getParcelable("extra_intent");
                StringBuilder sb3 = new StringBuilder();
                sb3.append("onCreate data = ");
                sb3.append(data);
                log(sb3.toString());
                if (data != null) {
                    processPickResult(data);
                    onPickResultFromCallLog(data);
                }
            }
        }
    }

    private void init(Bundle savedInstanceState) {
        initParams(savedInstanceState);
        initContentView();
        initActionBar();
    }

    private void initActionBar() {
        this.mActionBar = getActionBar();
        View deleteNumberActionBarView = LayoutInflater.from(this).inflate(R.layout.actionbar_editorview_customtitle, null);
        this.mSelectNumLayout = (RelativeLayout) deleteNumberActionBarView.findViewById(R.id.selected_delete_lyt);
        this.mSelectNumTextView = (TextView) deleteNumberActionBarView.findViewById(R.id.selected_delete_number);
        this.mSelectTitleTextView = (TextView) deleteNumberActionBarView.findViewById(R.id.title_select_textView);
        this.mCanceView = (ImageView) deleteNumberActionBarView.findViewById(R.id.cancel);
        this.mCanceView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (AutoRecordUserList.DBG) {
                    AutoRecordUserList.this.log("mCanceView.setOnClickListener");
                }
                AutoRecordUserList.this.finishBatchDeleteState();
            }
        });
        this.mTitleView = (TextView) deleteNumberActionBarView.findViewById(R.id.title);
        this.mBackView = (ImageView) deleteNumberActionBarView.findViewById(R.id.back);
        this.mBackView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AutoRecordUserList.this.onBackPressed();
            }
        });
        ActionBarUtils.setCustomTitle(this.mActionBar, deleteNumberActionBarView);
        setupActionBar();
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /* access modifiers changed from: private */
    public void finishBatchDeleteState() {
        displayProgress(false);
        this.mIsAllNumbersSelected = false;
        closeBatchDeleteMode();
        setupActionBar();
        this.mParam.mIconState = 0;
        this.mAdapter.setIconState(0);
        this.mAdapter.notifyDataSetChanged();
        invalidateOptionsMenu();
    }

    private void closeBatchDeleteMode() {
        clearSelectedState();
        this.mParam.mState = this.mParam.mLastState;
        this.mAdapter.setState(this.mParam.mState);
        this.mAdapter.notifyDataSetChanged();
        reQuery();
    }

    /* access modifiers changed from: private */
    public void clearSelectedState() {
        this.mParam.mSelectedNumbersIds.clear();
    }

    private void setAllNumbersSelectState() {
        int length = this.mAdapter.getCount();
        this.mParam.mSelectedNumbersIds.clear();
        for (int i = 0; i < length; i++) {
            this.mParam.mSelectedNumbersIds.add(Long.valueOf(this.mAdapter.getItemId(i)));
        }
    }

    private void setupActionBar() {
        if (this.mParam.mState == 3) {
            this.mSelectNumLayout.setVisibility(0);
            this.mSelectTitleTextView.setVisibility(0);
            this.mCanceView.setVisibility(0);
            this.mTitleView.setVisibility(8);
            this.mBackView.setVisibility(8);
            setBatchDeleteStateTitle();
            return;
        }
        this.mSelectNumLayout.setVisibility(8);
        this.mSelectTitleTextView.setVisibility(8);
        this.mCanceView.setVisibility(8);
        this.mTitleView.setVisibility(0);
        this.mBackView.setVisibility(0);
        this.mTitleView.setText(R.string.user_list);
    }

    private void initContentView() {
        setContentView(R.layout.autorecord_empty);
        new HwCutoutSettingLayout(this);
        setTitle(R.string.user_list);
        this.empty = (LinearLayout) findViewById(16908292);
        this.empty.setVisibility(8);
        this.listView = (ListView) findViewById(16908298);
        this.listView.setOnCreateContextMenuListener(this);
        setListAdapter(this.mAdapter);
        this.listView.setOnItemClickListener(new OnNumbersItemClickListener());
        this.listView.setOnItemLongClickListener(new OnNumbersItemLongClickListener());
        if (this.mParam.mState == 3) {
            changeStateModel(this.mParam.mState);
        }
        this.mAdapter.setState(this.mParam.mState);
        this.listView.setDivider(null);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        if (DBG) {
            log("onResume...");
        }
        query();
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        if (DBG) {
            log("onPause...");
        }
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        if (DBG) {
            log("onStop...");
        }
        if (this.mCursor != null) {
            this.mCursor.deactivate();
            this.mCursor.close();
        }
    }

    /* access modifiers changed from: private */
    public void sync() {
        if (DBG) {
            log("query: starting an sync");
        }
        this.mIsNeedSyncAgain = true;
        if (this.mIsAsyncTaskRunning) {
            if (DBG) {
                log("sync task is running ,do nothing");
            }
            return;
        }
        new AsyncTask<Void, Void, Void>() {
            /* access modifiers changed from: protected */
            /* JADX WARNING: Removed duplicated region for block: B:43:0x012f A[Catch:{ IllegalArgumentException -> 0x0176 }] */
            /* JADX WARNING: Removed duplicated region for block: B:45:0x0134 A[Catch:{ IllegalArgumentException -> 0x0176 }] */
            /* JADX WARNING: Removed duplicated region for block: B:62:0x0185 A[Catch:{ all -> 0x01ae }] */
            /* JADX WARNING: Removed duplicated region for block: B:64:0x018e  */
            /* JADX WARNING: Removed duplicated region for block: B:70:0x01b1  */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public java.lang.Void doInBackground(java.lang.Void... r22) {
                /*
                    r21 = this;
                    r1 = r21
                    com.android.phone.recorder.autorecord.AutoRecordUserList r0 = com.android.phone.recorder.autorecord.AutoRecordUserList.this
                    java.lang.String r2 = "sync doInBackground"
                    r0.log(r2)
                    com.android.phone.recorder.autorecord.AutoRecordUserList r0 = com.android.phone.recorder.autorecord.AutoRecordUserList.this
                    r2 = 0
                    r0.mIsNeedSyncAgain = r2
                    com.android.phone.recorder.autorecord.AutoRecordUserList r0 = com.android.phone.recorder.autorecord.AutoRecordUserList.this
                    r3 = 1
                    r0.mIsAsyncTaskRunning = r3
                    int r0 = android.os.Process.myTid()
                    int r3 = android.os.Process.getThreadPriority(r0)
                    r0 = 10
                    android.os.Process.setThreadPriority(r0)
                    r4 = 0
                    com.android.phone.recorder.autorecord.AutoRecordUserList r0 = com.android.phone.recorder.autorecord.AutoRecordUserList.this
                    android.content.ContentResolver r11 = r0.getContentResolver()
                    r12 = 0
                    r13 = 0
                    r14 = r13
                    android.net.Uri r6 = com.android.phone.recorder.autorecord.AutoRecordUserList.CONTENT_URI     // Catch:{ IllegalArgumentException -> 0x017c, all -> 0x0178 }
                    java.lang.String[] r7 = com.android.phone.recorder.autorecord.AutoRecordUserList.COLUMN_NAMES     // Catch:{ IllegalArgumentException -> 0x017c, all -> 0x0178 }
                    r8 = 0
                    r9 = 0
                    r10 = 0
                    r5 = r11
                    android.database.Cursor r0 = r5.query(r6, r7, r8, r9, r10)     // Catch:{ IllegalArgumentException -> 0x017c, all -> 0x0178 }
                    r12 = r0
                    java.util.ArrayList r0 = new java.util.ArrayList     // Catch:{ IllegalArgumentException -> 0x017c, all -> 0x0178 }
                    r0.<init>()     // Catch:{ IllegalArgumentException -> 0x017c, all -> 0x0178 }
                L_0x0040:
                    r15 = r0
                    com.android.phone.recorder.autorecord.AutoRecordUserList r0 = com.android.phone.recorder.autorecord.AutoRecordUserList.this     // Catch:{ IllegalArgumentException -> 0x017c, all -> 0x0178 }
                    boolean r0 = r0.hasData(r12)     // Catch:{ IllegalArgumentException -> 0x017c, all -> 0x0178 }
                    if (r0 == 0) goto L_0x0138
                    java.lang.String r0 = "number"
                    int r0 = r12.getColumnIndex(r0)     // Catch:{ IllegalArgumentException -> 0x017c, all -> 0x0178 }
                    java.lang.String r0 = r12.getString(r0)     // Catch:{ IllegalArgumentException -> 0x017c, all -> 0x0178 }
                    r10 = r0
                    java.lang.String r0 = "name"
                    int r0 = r12.getColumnIndex(r0)     // Catch:{ IllegalArgumentException -> 0x017c, all -> 0x0178 }
                    java.lang.String r0 = r12.getString(r0)     // Catch:{ IllegalArgumentException -> 0x017c, all -> 0x0178 }
                    r9 = r0
                    r16 = 0
                    android.content.ContentValues r0 = new android.content.ContentValues     // Catch:{ IllegalArgumentException -> 0x017c, all -> 0x0178 }
                    r0.<init>()     // Catch:{ IllegalArgumentException -> 0x017c, all -> 0x0178 }
                    r8 = r0
                    r17 = r13
                    android.net.Uri r0 = android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI     // Catch:{ all -> 0x0125 }
                    java.lang.String r5 = android.net.Uri.encode(r10)     // Catch:{ all -> 0x0125 }
                    android.net.Uri r6 = android.net.Uri.withAppendedPath(r0, r5)     // Catch:{ all -> 0x0125 }
                    java.lang.String r0 = "display_name"
                    java.lang.String[] r7 = new java.lang.String[]{r0}     // Catch:{ all -> 0x0125 }
                    r0 = 0
                    r18 = 0
                    r19 = 0
                    r5 = r11
                    r13 = r8
                    r8 = r0
                    r2 = r9
                    r9 = r18
                    r20 = r4
                    r4 = r10
                    r10 = r19
                    android.database.Cursor r0 = r5.query(r6, r7, r8, r9, r10)     // Catch:{ all -> 0x0121 }
                    r14 = r0
                    com.android.phone.recorder.autorecord.AutoRecordUserList r0 = com.android.phone.recorder.autorecord.AutoRecordUserList.this     // Catch:{ all -> 0x0121 }
                    boolean r0 = r0.hasData(r14)     // Catch:{ all -> 0x0121 }
                    if (r0 == 0) goto L_0x00c5
                    com.android.phone.recorder.autorecord.AutoRecordUserList r0 = com.android.phone.recorder.autorecord.AutoRecordUserList.this     // Catch:{ all -> 0x0121 }
                    java.lang.String r5 = "sync doInBackground contacts"
                    r0.log(r5)     // Catch:{ all -> 0x0121 }
                    java.lang.String r0 = "display_name"
                    int r0 = r14.getColumnIndex(r0)     // Catch:{ all -> 0x0121 }
                    java.lang.String r0 = r14.getString(r0)     // Catch:{ all -> 0x0121 }
                    com.android.phone.recorder.autorecord.AutoRecordUserList r5 = com.android.phone.recorder.autorecord.AutoRecordUserList.this     // Catch:{ all -> 0x0121 }
                    boolean r5 = r5.isRemoteAndLocNameDifferent(r2, r0)     // Catch:{ all -> 0x0121 }
                    if (r5 == 0) goto L_0x00c3
                    com.android.phone.recorder.autorecord.AutoRecordUserList r5 = com.android.phone.recorder.autorecord.AutoRecordUserList.this     // Catch:{ all -> 0x0121 }
                    java.lang.String r7 = "doInBackground, find contact."
                    r5.log(r7)     // Catch:{ all -> 0x0121 }
                    java.lang.String r5 = "name"
                    r13.put(r5, r0)     // Catch:{ all -> 0x0121 }
                    java.lang.String r5 = "number"
                    r13.put(r5, r4)     // Catch:{ all -> 0x0121 }
                    r15.add(r13)     // Catch:{ all -> 0x0121 }
                L_0x00c3:
                    r16 = 1
                L_0x00c5:
                    if (r16 != 0) goto L_0x00f1
                    com.android.phone.recorder.autorecord.AutoRecordUserList r0 = com.android.phone.recorder.autorecord.AutoRecordUserList.this     // Catch:{ all -> 0x0121 }
                    android.database.Cursor r0 = com.android.phone.recorder.Utils.getPredefineCursor(r0, r4)     // Catch:{ all -> 0x0121 }
                    r5 = r0
                    if (r5 == 0) goto L_0x00ef
                    boolean r0 = r5.moveToNext()     // Catch:{ all -> 0x010d }
                    if (r0 == 0) goto L_0x00ef
                    r0 = 0
                    java.lang.String r7 = r5.getString(r0)     // Catch:{ all -> 0x010d }
                    boolean r8 = android.text.TextUtils.isEmpty(r7)     // Catch:{ all -> 0x010d }
                    if (r8 != 0) goto L_0x00f4
                    java.lang.String r8 = "name"
                    r13.put(r8, r7)     // Catch:{ all -> 0x010d }
                    java.lang.String r8 = "number"
                    r13.put(r8, r4)     // Catch:{ all -> 0x010d }
                    r7 = 1
                    r16 = r7
                    goto L_0x00f4
                L_0x00ef:
                    r0 = 0
                    goto L_0x00f4
                L_0x00f1:
                    r0 = 0
                    r5 = r17
                L_0x00f4:
                    if (r16 != 0) goto L_0x010f
                    com.android.phone.recorder.autorecord.AutoRecordUserList r7 = com.android.phone.recorder.autorecord.AutoRecordUserList.this     // Catch:{ all -> 0x010d }
                    java.lang.String r8 = "sync doInBackground, contValuesList add values"
                    r7.log(r8)     // Catch:{ all -> 0x010d }
                    java.lang.String r7 = "name"
                    java.lang.String r8 = ""
                    r13.put(r7, r8)     // Catch:{ all -> 0x010d }
                    java.lang.String r7 = "number"
                    r13.put(r7, r4)     // Catch:{ all -> 0x010d }
                    r15.add(r13)     // Catch:{ all -> 0x010d }
                    goto L_0x010f
                L_0x010d:
                    r0 = move-exception
                    goto L_0x012d
                L_0x010f:
                    if (r14 == 0) goto L_0x0114
                    r14.close()     // Catch:{ IllegalArgumentException -> 0x0176 }
                L_0x0114:
                    if (r5 == 0) goto L_0x0119
                    r5.close()     // Catch:{ IllegalArgumentException -> 0x0176 }
                L_0x0119:
                    r2 = r0
                    r0 = r15
                    r4 = r20
                    r13 = 0
                    goto L_0x0040
                L_0x0121:
                    r0 = move-exception
                    r5 = r17
                    goto L_0x012d
                L_0x0125:
                    r0 = move-exception
                    r20 = r4
                    r13 = r8
                    r2 = r9
                    r4 = r10
                    r5 = r17
                L_0x012d:
                    if (r14 == 0) goto L_0x0132
                    r14.close()     // Catch:{ IllegalArgumentException -> 0x0176 }
                L_0x0132:
                    if (r5 == 0) goto L_0x0137
                    r5.close()     // Catch:{ IllegalArgumentException -> 0x0176 }
                L_0x0137:
                    throw r0     // Catch:{ IllegalArgumentException -> 0x0176 }
                L_0x0138:
                    r20 = r4
                    com.android.phone.recorder.autorecord.AutoRecordUserList r0 = com.android.phone.recorder.autorecord.AutoRecordUserList.this     // Catch:{ IllegalArgumentException -> 0x0176 }
                    java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ IllegalArgumentException -> 0x0176 }
                    r2.<init>()     // Catch:{ IllegalArgumentException -> 0x0176 }
                    java.lang.String r4 = "doInBackground, contValuesList.size() = "
                    r2.append(r4)     // Catch:{ IllegalArgumentException -> 0x0176 }
                    int r4 = r15.size()     // Catch:{ IllegalArgumentException -> 0x0176 }
                    r2.append(r4)     // Catch:{ IllegalArgumentException -> 0x0176 }
                    java.lang.String r2 = r2.toString()     // Catch:{ IllegalArgumentException -> 0x0176 }
                    r0.log(r2)     // Catch:{ IllegalArgumentException -> 0x0176 }
                    int r0 = r15.size()     // Catch:{ IllegalArgumentException -> 0x0176 }
                    if (r0 <= 0) goto L_0x016e
                    android.net.Uri r0 = com.android.phone.recorder.autorecord.AutoRecordUserList.CONTENT_URI     // Catch:{ IllegalArgumentException -> 0x0176 }
                    int r2 = r15.size()     // Catch:{ IllegalArgumentException -> 0x0176 }
                    android.content.ContentValues[] r2 = new android.content.ContentValues[r2]     // Catch:{ IllegalArgumentException -> 0x0176 }
                    java.lang.Object[] r2 = r15.toArray(r2)     // Catch:{ IllegalArgumentException -> 0x0176 }
                    android.content.ContentValues[] r2 = (android.content.ContentValues[]) r2     // Catch:{ IllegalArgumentException -> 0x0176 }
                    int r0 = r11.bulkInsert(r0, r2)     // Catch:{ IllegalArgumentException -> 0x0176 }
                    r4 = r0
                    goto L_0x0170
                L_0x016e:
                    r4 = r20
                L_0x0170:
                    if (r12 == 0) goto L_0x0193
                    r12.close()
                    goto L_0x0193
                L_0x0176:
                    r0 = move-exception
                    goto L_0x017f
                L_0x0178:
                    r0 = move-exception
                    r20 = r4
                    goto L_0x01af
                L_0x017c:
                    r0 = move-exception
                    r20 = r4
                L_0x017f:
                    boolean r2 = com.android.phone.recorder.autorecord.AutoRecordUserList.DBG     // Catch:{ all -> 0x01ae }
                    if (r2 == 0) goto L_0x018c
                    com.android.phone.recorder.autorecord.AutoRecordUserList r2 = com.android.phone.recorder.autorecord.AutoRecordUserList.this     // Catch:{ all -> 0x01ae }
                    java.lang.String r4 = "invalid URI starting sync"
                    r2.log(r4)     // Catch:{ all -> 0x01ae }
                L_0x018c:
                    if (r12 == 0) goto L_0x0191
                    r12.close()
                L_0x0191:
                    r4 = r20
                L_0x0193:
                    com.android.phone.recorder.autorecord.AutoRecordUserList r0 = com.android.phone.recorder.autorecord.AutoRecordUserList.this
                    java.lang.StringBuilder r2 = new java.lang.StringBuilder
                    r2.<init>()
                    java.lang.String r5 = "doInBackground, size = "
                    r2.append(r5)
                    r2.append(r4)
                    java.lang.String r2 = r2.toString()
                    r0.log(r2)
                    android.os.Process.setThreadPriority(r3)
                    r2 = 0
                    return r2
                L_0x01ae:
                    r0 = move-exception
                L_0x01af:
                    if (r12 == 0) goto L_0x01b4
                    r12.close()
                L_0x01b4:
                    throw r0
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.phone.recorder.autorecord.AutoRecordUserList.C00276.doInBackground(java.lang.Void[]):java.lang.Void");
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Void unused) {
                AutoRecordUserList.this.mIsAsyncTaskRunning = false;
                if (AutoRecordUserList.this.mIsNeedSyncAgain) {
                    if (AutoRecordUserList.DBG) {
                        AutoRecordUserList.this.log("sync again");
                    }
                    AutoRecordUserList.this.sync();
                    return;
                }
                AutoRecordUserList.this.reQuery();
                AutoRecordUserList.this.displayProgress(false);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    private void query() {
        if (DBG) {
            log("query: starting an async query");
        }
        this.mQueryHandler.startQuery(0, null, CONTENT_URI, null, null, null, null);
    }

    /* access modifiers changed from: private */
    public void reQuery() {
        query();
    }

    /* access modifiers changed from: private */
    public void setAdapter() {
        if (this.mAdapter == null) {
            this.mAdapter = newAdapter();
            setListAdapter(this.mAdapter);
            return;
        }
        this.mAdapter.changeCursor(this.mCursor);
    }

    /* access modifiers changed from: protected */
    public AutoRecordListAdapterWrapper newAdapter() {
        return new AutoRecordListAdapterWrapper(this, null);
    }

    private void setBatchDeleteStateTitle() {
        if (this.mParam.mSelectedNumbersIds.size() == 0) {
            this.mSelectTitleTextView.setText(R.string.NotSelected);
            this.mSelectNumLayout.setVisibility(8);
            return;
        }
        this.mSelectTitleTextView.setText(R.string.ActionBar_MultiSelect_Selected_618);
        this.mSelectNumLayout.setVisibility(0);
        this.mSelectNumTextView.setText(String.valueOf(this.mParam.mSelectedNumbersIds.size()));
    }

    /* access modifiers changed from: private */
    public void displayProgress(boolean display) {
        if (DBG) {
            StringBuilder sb = new StringBuilder();
            sb.append("displayProgress: ");
            sb.append(display);
            log(sb.toString());
        }
        if (display) {
            if (this.mProgressDialog != null && !this.mProgressDialog.isShowing()) {
                this.mProgressDialog.show();
            }
        } else if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.dismiss();
        }
    }

    /* access modifiers changed from: private */
    public void handleOnNumbersItemClick(int index) {
        if (this.mAdapter.getCount() != 0) {
            if (DBG) {
                log("number item click");
            }
            Long id = Long.valueOf(this.mAdapter.getItemId(index));
            StringBuilder sb = new StringBuilder();
            sb.append("number item click, id = ");
            sb.append(id);
            log(sb.toString());
            if (this.mParam.mState == 3) {
                if (this.mParam.mSelectedNumbersIds.contains(id)) {
                    this.mParam.mSelectedNumbersIds.remove(id);
                } else {
                    this.mParam.mSelectedNumbersIds.add(id);
                }
                setBatchDeleteStateTitle();
                setDeleteMenuEnable(this.mParam.mSelectedNumbersIds.size() > 0);
                setSelectAllMenuIcon();
                this.mAdapter.notifyDataSetChanged();
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleOnNumbersItemLongClick(int index) {
        this.mParam.mIconState = 1;
        this.mAdapter.setIconState(1);
        this.mAdapter.notifyDataSetChanged();
        if (isHandleNumbersItemLongClick()) {
            if (DBG) {
                log("number item long click");
            }
            Long numberId = Long.valueOf(this.mAdapter.getItemId(index));
            this.mParam.mSelectedNumbersIds.clear();
            this.mParam.mSelectedNumbersIds.add(numberId);
            this.mParam.mLastState = this.mParam.mState;
            changeStateModel(3);
        }
    }

    private boolean isHandleNumbersItemLongClick() {
        if (this.mParam.mState == 3) {
            return false;
        }
        return true;
    }

    private void changeStateModel(int state) {
        boolean needReload = this.mParam.mState == 1;
        this.mParam.mState = state;
        if (state == 1) {
            changeToAllState(needReload);
        } else if (state == 3) {
            changeToBatchDeleteState();
        }
    }

    private void changeToAllState(boolean reload) {
        if (DBG) {
            log("change to all numbers model");
        }
        setupActionBar();
        if (!reload) {
            invalidateOptionsMenu();
        }
        this.mAdapter.setState(this.mParam.mState);
        reQuery();
    }

    private void changeToBatchDeleteState() {
        if (DBG) {
            log("change to batch delete numbers model");
        }
        startBatchDeleteState();
        this.mAdapter.setState(this.mParam.mState);
        this.mAdapter.notifyDataSetChanged();
    }

    private void startBatchDeleteState() {
        if (DBG) {
            log("open action mode");
        }
        setupActionBar();
        invalidateOptionsMenu();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.auto_record_user_list, menu);
        this.mMenu = menu;
        setupOptionsMenu();
        return true;
    }

    private void setupOptionsMenu() {
        if (this.mMenu != null) {
            boolean z = false;
            if (this.mParam.mState == 1) {
                this.mMenu.setGroupVisible(R.id.menu_batch_delete_numbers, false);
                this.mMenu.setGroupVisible(R.id.menu_main, true);
            } else if (this.mParam.mState == 3) {
                this.mMenu.setGroupVisible(R.id.menu_batch_delete_numbers, true);
                this.mMenu.setGroupVisible(R.id.menu_main, false);
                setSelectAllMenuIcon();
                if (this.mParam.mSelectedNumbersIds.size() != 0) {
                    z = true;
                }
                setDeleteMenuEnable(z);
            } else {
                log("setupOptionsMenu() error mState!");
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == 16908332) {
            onBackPressed();
            return true;
        }
        switch (itemId) {
            case R.id.menu_delete /*2131427376*/:
                showDeleteDialog();
                break;
            case R.id.menu_select_all /*2131427377*/:
                handleOnSelectAllButtonClick(item);
                break;
            case R.id.menu_add_contacts /*2131427379*/:
                addFromContacts();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleOnSelectAllButtonClick(MenuItem item) {
        int i;
        int i2;
        boolean z = false;
        if (!this.mIsAllNumbersSelected) {
            selectAllNumbers();
            this.mIsAllNumbersSelected = true;
        } else {
            cancelSelectAllNumbers();
            this.mIsAllNumbersSelected = false;
        }
        setBatchDeleteStateTitle();
        if (this.mParam.mSelectedNumbersIds.size() != 0) {
            z = true;
        }
        setDeleteMenuEnable(z);
        this.mAdapter.notifyDataSetChanged();
        if (this.mIsAllNumbersSelected) {
            i = R.drawable.ic_public_select_all;
        } else {
            i = R.drawable.ic_public_deselect_all;
        }
        item.setIcon(i);
        if (this.mIsAllNumbersSelected) {
            i2 = R.string.DeselectAll;
        } else {
            i2 = R.string.SelectAll;
        }
        item.setTitle(i2);
        item.setChecked(this.mIsAllNumbersSelected);
    }

    private void selectAllNumbers() {
        setAllNumbersSelectState();
        if (DBG) {
            log("select all numbers");
        }
    }

    private void cancelSelectAllNumbers() {
        clearSelectedState();
        if (DBG) {
            log("unselect all numbers");
        }
    }

    private void setDeleteMenuEnable(boolean enabled) {
        MenuItem deleteMenu = this.mMenu.findItem(R.id.menu_delete);
        deleteMenu.setEnabled(enabled);
        if (enabled) {
            deleteMenu.setIcon(R.drawable.ic_public_delete);
        } else {
            deleteMenu.setIcon(R.drawable.ic_public_delete_disable);
        }
    }

    private void addFromContacts() {
        Intent addContactsIntent = new Intent();
        addContactsIntent.setPackage("com.android.contacts");
        addContactsIntent.setAction("com.huawei.community.action.MULTIPLE_PICK");
        addContactsIntent.setAction("android.intent.action.PICK");
        addContactsIntent.setType("vnd.android.cursor.dir/phone_v2");
        addContactsIntent.putExtra("com.huawei.community.action.MULTIPLE_PICK", true);
        addContactsIntent.putExtra("com.huawei.community.action.MAX_SELECT_COUNT", 500);
        log("clickAddFromContacts: addFromContacts");
        addContactsIntent.putExtra("com.huawei.community.action.EXPECT_INTEGER_LIST", true);
        try {
            startActivityForResult(addContactsIntent, 109);
        } catch (Exception e) {
            Log.e("AutoRecordUserList", "clickAddFromContacts: Exception", e);
        }
    }

    /* access modifiers changed from: private */
    public void createNewAutoRecordNumber(String phone, String name) {
        String phone2 = PhoneNumberUtils.stripSeparators(phone);
        if (!TextUtils.isEmpty(phone2)) {
            ContentResolver cr = getContentResolver();
            ContentValues values = new ContentValues();
            if (TextUtils.isEmpty(name)) {
                values.put("name", phone2);
            } else {
                values.put("name", name);
            }
            values.put("number", phone2);
            if (cr.insert(CONTENT_URI, values) != null) {
                reQuery();
            }
        }
    }

    private void showDeleteDialog() {
        DeleteDialog.init(this.mParam.mSelectedNumbersIds.size()).show(getFragmentManager(), "delete");
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == 109 && data != null) {
            processPickResult(data);
            onPickResultFromCallLog(data);
        }
    }

    private void onPickResultFromCallLog(Intent data) {
        ArrayList<Integer> tempIds = data.getIntegerArrayListExtra("SelItemCalls_KeyValue");
        if (NumberCompareUtils.isNullOrEmptyList(tempIds)) {
            if (DBG) {
                log("onPickResultFromCallLog: Select none");
            }
        } else if (tempIds != null && tempIds.size() > 0) {
            long[] contactsId = new long[tempIds.size()];
            int tempIdsSize = tempIds.size();
            for (int i = 0; i < tempIdsSize; i++) {
                contactsId[i] = (long) ((Integer) tempIds.get(i)).intValue();
            }
            if (contactsId.length > 50) {
                displayProgress(true);
            }
            getContactInfoAsync(this, contactsId, this.mHandler, true);
        }
    }

    private void processPickResult(Intent data) {
        ArrayList<Integer> tempIds = data.getIntegerArrayListExtra("SelItemData_KeyValue");
        if (NumberCompareUtils.isNullOrEmptyList(tempIds)) {
            if (DBG) {
                log("processPickResult: Select none");
            }
        } else if (tempIds != null && tempIds.size() > 0) {
            long[] contactsId = new long[tempIds.size()];
            int tempIdsSize = tempIds.size();
            for (int i = 0; i < tempIdsSize; i++) {
                contactsId[i] = (long) ((Integer) tempIds.get(i)).intValue();
            }
            if (contactsId.length > 50) {
                displayProgress(true);
            }
            getContactInfoAsync(this, contactsId, this.mHandler, false);
        }
    }

    private void setSelectAllMenuIcon() {
        MenuItem selectAllMenu = this.mMenu.findItem(R.id.menu_select_all);
        if (this.mParam.mSelectedNumbersIds.size() == this.mAdapter.getCount()) {
            selectAllMenu.setIcon(R.drawable.ic_public_select_all);
            selectAllMenu.setTitle(R.string.DeselectAll);
            selectAllMenu.setChecked(true);
            this.mIsAllNumbersSelected = true;
            return;
        }
        selectAllMenu.setIcon(R.drawable.ic_public_deselect_all);
        selectAllMenu.setTitle(R.string.SelectAll);
        selectAllMenu.setChecked(false);
        this.mIsAllNumbersSelected = false;
    }

    /* access modifiers changed from: private */
    public void deleteSelectedNumbers() {
        if (DBG) {
            log("startDelete() -> start delete numbers item");
        }
        if (this.mParam.mSelectedNumbersIds.size() > 50) {
            displayProgress(true);
        }
        HandlerThread workerThread = new HandlerThread("workerThread");
        workerThread.setPriority(10);
        workerThread.start();
        new DeleteNumbers(workerThread).start();
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    /* access modifiers changed from: protected */
    public void onSaveInstanceState(Bundle outState) {
        saveInstance(outState);
        super.onSaveInstanceState(outState);
    }

    private void saveInstance(Bundle outState) {
        if (outState != null) {
            outState.putParcelable("extra_savedinstance_state", this.mParam);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        handleConfigurationChange(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    private void handleConfigurationChange(Configuration newConfig) {
        if (this.listView != null) {
            this.listView.setAdapter(this.mAdapter);
        }
        handleToolBarConfigurationChange();
    }

    private void handleToolBarConfigurationChange() {
        invalidateOptionsMenu();
    }

    /* access modifiers changed from: private */
    public void updateOnUIThread() {
        if (DBG) {
            log("updateOnUIThread() -> update UIThread when it run");
        }
        try {
            runOnUiThread(new Runnable() {
                public void run() {
                    AutoRecordUserList.this.clearSelectedState();
                    AutoRecordUserList.this.finishBatchDeleteState();
                }
            });
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("updateOnUIThread occur exception !");
            sb.append(e);
            loge(sb.toString());
        }
    }

    public void onBackPressed() {
        this.mParam.mIconState = 0;
        this.mAdapter.setIconState(0);
        this.mAdapter.notifyDataSetChanged();
        if (this.mParam.mState != 3) {
            super.onBackPressed();
        } else {
            finishBatchDeleteState();
        }
    }

    private void getContactInfoAsync(final Context context, final long[] ids, Handler handler, final boolean isCallLog) {
        new AsyncTask<Void, Void, Void>() {
            /* access modifiers changed from: protected */
            public Void doInBackground(Void... params) {
                long[] jArr;
                Cursor cursor;
                int origPri = Process.getThreadPriority(Process.myTid());
                Process.setThreadPriority(10);
                if (ids.length == 0) {
                    return null;
                }
                StringBuilder idSetBuilder = new StringBuilder();
                boolean first = true;
                for (long id : ids) {
                    if (first) {
                        idSetBuilder.append(id);
                        first = false;
                    } else {
                        idSetBuilder.append(',');
                        idSetBuilder.append(id);
                    }
                }
                if (first) {
                    return null;
                }
                Cursor cursor2 = null;
                int result = 0;
                ContentResolver cr = context.getContentResolver();
                if (idSetBuilder.length() > 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("_id IN (");
                    sb.append(idSetBuilder.toString());
                    sb.append(")");
                    String whereClause = sb.toString();
                    try {
                        if (!isCallLog) {
                            cursor = cr.query(AutoRecordUserList.PHONES_WITH_PRESENCE_URI, AutoRecordUserList.CONTACTS_COLUMN_NAMES, whereClause, null, null);
                        } else {
                            cursor = cr.query(Calls.CONTENT_URI_WITH_VOICEMAIL, AutoRecordUserList.CONTACTS_COLUMN_NAMES_CALL_LOG, whereClause, null, null);
                        }
                        cursor2 = cursor;
                    } catch (Exception e) {
                        AutoRecordUserList autoRecordUserList = AutoRecordUserList.this;
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("processContactResultAsync occur exception when query contact!");
                        sb2.append(e);
                        autoRecordUserList.log(sb2.toString());
                    }
                }
                if (cursor2 == null) {
                    return null;
                }
                try {
                    ArrayList<ContentValues> contValuesList = new ArrayList<>();
                    while (cursor2.moveToNext()) {
                        String number = cursor2.getString(2);
                        String name = cursor2.getString(1);
                        ContentValues values = new ContentValues();
                        values.put("name", name);
                        values.put("number", PhoneNumberUtils.stripSeparators(number));
                        contValuesList.add(values);
                    }
                    if (contValuesList.size() > 0) {
                        result = cr.bulkInsert(AutoRecordUserList.CONTENT_URI, (ContentValues[]) contValuesList.toArray(new ContentValues[contValuesList.size()]));
                    }
                } catch (IllegalArgumentException e2) {
                    if (AutoRecordUserList.DBG) {
                        AutoRecordUserList.this.log("invalid URI starting getContactInfoAsync");
                    }
                } catch (Throwable th) {
                    cursor2.close();
                    throw th;
                }
                cursor2.close();
                if (result > 0) {
                    AutoRecordUserList.this.mHandler.post(new Runnable() {
                        public void run() {
                            AutoRecordUserList.this.reQuery();
                            AutoRecordUserList.this.displayProgress(false);
                        }
                    });
                }
                Process.setThreadPriority(origPri);
                return null;
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Void unused) {
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    private void initParams(Bundle savedInstance) {
        this.mQueryHandler = new QueryHandler(getContentResolver());
        this.mAdapter = new AutoRecordListAdapterWrapper(this, null);
        if (savedInstance != null) {
            this.mParam = (Param) savedInstance.getParcelable("extra_savedinstance_state");
        }
        if (this.mParam == null) {
            this.mParam = new Param();
        }
        this.mAdapter.getSelectedIds().addAll(this.mParam.mSelectedNumbersIds);
        this.mParam.mSelectedNumbersIds = this.mAdapter.getSelectedIds();
    }

    /* access modifiers changed from: protected */
    public void log(String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("[AutoRecordUserList] ");
        sb.append(msg);
        Log.d("AutoRecordUserList", sb.toString());
    }

    /* access modifiers changed from: protected */
    public void loge(String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("[AutoRecordUserList] ");
        sb.append(msg);
        Log.e("AutoRecordUserList", sb.toString());
    }

    /* access modifiers changed from: private */
    public boolean hasData(Cursor cursorLoc) {
        return cursorLoc != null && cursorLoc.moveToNext();
    }

    /* access modifiers changed from: private */
    public boolean isRemoteAndLocNameDifferent(String nameLoc, String nameRemote) {
        return !(nameRemote == null && nameLoc == null) && (nameRemote == null || !nameRemote.equals(nameLoc));
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(this.mContentObserver);
    }
}
