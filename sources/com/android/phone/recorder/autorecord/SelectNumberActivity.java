package com.android.phone.recorder.autorecord;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import com.android.phone.recorder.R;
import com.android.phone.recorder.StatisticalHelper;

public class SelectNumberActivity extends Activity implements OnCancelListener, OnClickListener {
    /* access modifiers changed from: private */
    public static final String TAG = SelectNumberActivity.class.getSimpleName();
    /* access modifiers changed from: private */
    public int selected = 1;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        this.selected = Secure.getInt(getContentResolver(), "enable_all_numbers_key", 1);
        showDialog(R.id.display_mode);
    }

    /* access modifiers changed from: protected */
    public Dialog onCreateDialog(int id, Bundle args) {
        if (id != R.id.display_mode) {
            return super.onCreateDialog(id, args);
        }
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("onCreateDialog selected = ");
        sb.append(this.selected);
        Log.d(str, sb.toString());
        Builder builder = new Builder(this).setTitle(R.string.auto_record_settings).setOnCancelListener(this).setNegativeButton(17039360, this);
        int i = 0;
        String[] items = {getString(R.string.all_numbers), getString(R.string.user_list)};
        if (this.selected != 1) {
            i = 1;
        }
        builder.setSingleChoiceItems(items, i, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String access$000 = SelectNumberActivity.TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("onCreateDialog which = ");
                sb.append(which);
                Log.d(access$000, sb.toString());
                switch (which) {
                    case 0:
                        StatisticalHelper.report(4032);
                        SelectNumberActivity.this.selected = 1;
                        break;
                    case 1:
                        StatisticalHelper.report(4033);
                        SelectNumberActivity.this.selected = 0;
                        break;
                }
                SelectNumberActivity.this.setResult(SelectNumberActivity.this.selected);
                SelectNumberActivity.this.finish();
            }
        });
        return builder.create();
    }

    public void onCancel(DialogInterface arg0) {
        setResult(this.selected);
        finish();
    }

    public void onClick(DialogInterface arg0, int arg1) {
        setResult(this.selected);
        finish();
    }

    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_down);
    }
}
