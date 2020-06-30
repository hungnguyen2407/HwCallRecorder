package com.android.phone.recorder;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceActivity;
import android.view.WindowManager.LayoutParams;
import huawei.android.widget.ActionBarEx;
import huawei.android.widget.HwToolbar;

public class HwCutoutSettingLayout {
    private Activity mActivity;
    private final Context mContext;
    private HwToolbar mHwToolbar;

    public HwCutoutSettingLayout(Context context) {
        this.mContext = context;
        if (this.mContext instanceof Activity) {
            this.mActivity = (Activity) this.mContext;
            this.mActivity.getWindow().addFlags(67108864);
            this.mActivity.getWindow().getDecorView().setSystemUiVisibility(3328);
            LayoutParams lp = this.mActivity.getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = 1;
            this.mActivity.getWindow().setAttributes(lp);
            if (this.mContext instanceof PreferenceActivity) {
                this.mActivity.setContentView(R.layout.hw_phone_setting);
            }
            this.mHwToolbar = this.mActivity.findViewById(R.id.hwtoolbar);
            if (this.mHwToolbar != null) {
                this.mHwToolbar.setPadding(this.mHwToolbar.getPaddingLeft(), getStatusBarHeight(), this.mHwToolbar.getPaddingRight(), this.mHwToolbar.getPaddingBottom());
                this.mActivity.setActionBar(this.mHwToolbar);
                ActionBar actionBar = this.mActivity.getActionBar();
                if (actionBar != null) {
                    ActionBarEx.setStartIcon(actionBar, this.mHwToolbar, false, null, null);
                    ActionBarEx.setEndIcon(actionBar, this.mHwToolbar, false, null, null);
                }
            }
        }
    }

    private int getStatusBarHeight() {
        return this.mContext.getResources().getDimensionPixelSize(this.mContext.getResources().getIdentifier("status_bar_height", "dimen", "android"));
    }
}
