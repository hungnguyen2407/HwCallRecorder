package com.android.phone.recorder.autorecord;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.phone.recorder.R;
import com.android.phone.recorder.Utils;
import com.huawei.android.os.SystemPropertiesEx;
import java.util.ArrayList;

public class AutoRecordListAdapterWrapper extends CursorAdapter {
    private static final boolean DBG;
    private static int s180DipInPixel = -1;
    private int mIconState = 0;
    private ArrayList<Long> mSeletedIds = new ArrayList<>();
    private int mState = 0;

    public static class AdapterViewHolder {
        public CheckBox checkBox;
        public TextView name;
        public TextView number;
        public ImageView peoplePhoto;
    }

    static {
        boolean z = false;
        if (SystemPropertiesEx.getInt("ro.debuggable", 0) == 1) {
            z = true;
        }
        DBG = z;
    }

    public AutoRecordListAdapterWrapper(Context context, Cursor c) {
        super(context, c, 2);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return newListItemView(context);
    }

    public void bindView(View view, Context context, Cursor cursor) {
        getListItemView(view, cursor);
    }

    public void setState(int state) {
        this.mState = state;
    }

    public void setIconState(int state) {
        this.mIconState = state;
    }

    public ArrayList<Long> getSelectedIds() {
        return this.mSeletedIds;
    }

    private View newListItemView(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.autorecord_list_item, null);
    }

    private View getListItemView(View convertView, Cursor cursor) {
        AdapterViewHolder holder;
        if (DBG) {
            Log.d("AutoRecordListAdapterWrapper", "get list item view");
        }
        if (convertView.getTag() == null) {
            holder = new AdapterViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.number = (TextView) convertView.findViewById(R.id.number);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox_delete_item);
            holder.peoplePhoto = (ImageView) convertView.findViewById(R.id.people_photo);
            convertView.setTag(holder);
        } else {
            holder = (AdapterViewHolder) convertView.getTag();
        }
        bindListView(holder, cursor, convertView.getContext(), convertView);
        return convertView;
    }

    /* JADX INFO: finally extract failed */
    private void bindListView(AdapterViewHolder holder, Cursor cursor, Context context, View view) {
        AdapterViewHolder adapterViewHolder = holder;
        Cursor cursor2 = cursor;
        Context context2 = context;
        View view2 = view;
        String name = cursor2.getString(cursor2.getColumnIndex("name"));
        String number = cursor2.getString(cursor2.getColumnIndex("number"));
        boolean isInContacts = false;
        boolean isPredefined = false;
        Uri predefinePhotoUri = null;
        if (number != null) {
            int id = Utils.getUriId(context2, number);
            StringBuilder sb = new StringBuilder();
            sb.append("bindListView() id=");
            sb.append(id);
            Log.d("AutoRecordListAdapterWrapper", sb.toString());
            if (id > -1) {
                isInContacts = true;
            } else {
                try {
                    Cursor c = Utils.getPredefineCursor(context2, number);
                    if (c != null && c.moveToNext()) {
                        String photoUri = c.getString(2);
                        if (!TextUtils.isEmpty(photoUri)) {
                            predefinePhotoUri = Uri.parse(photoUri);
                            isPredefined = true;
                        }
                    }
                    if (c != null) {
                        c.close();
                    }
                } catch (Throwable th) {
                    if (0 != 0) {
                        null.close();
                    }
                    throw th;
                }
            }
        }
        displayNameAndNumber(adapterViewHolder, context2, name, number);
        if (isInContacts) {
            try {
                Bitmap bitmap = Utils.getPhotoByNumber(number, context2);
                StringBuilder sb2 = new StringBuilder();
                sb2.append("bindListView() bitmap = ");
                sb2.append(bitmap);
                Log.d("AutoRecordListAdapterWrapper", sb2.toString());
                handleBitmap(bitmap, adapterViewHolder, context2, view2);
                adapterViewHolder.peoplePhoto.setVisibility(0);
            } catch (Exception e) {
                Log.d("AutoRecordListAdapterWrapper", "bindListView()", e);
                adapterViewHolder.peoplePhoto.setVisibility(8);
            }
        } else if (isPredefined) {
            Bitmap bitmap2 = Utils.getPredefinePhoto(context2, predefinePhotoUri);
            StringBuilder sb3 = new StringBuilder();
            sb3.append("bindListView() predefine bitmap = ");
            sb3.append(bitmap2);
            Log.d("AutoRecordListAdapterWrapper", sb3.toString());
            handleBitmap(bitmap2, adapterViewHolder, context2, view2);
        } else {
            if (s180DipInPixel == -1) {
                s180DipInPixel = (int) TypedValue.applyDimension(1, 180.0f, context.getResources().getDisplayMetrics());
            }
            adapterViewHolder.peoplePhoto.setImageResource(Utils.getDefaultAvatarResId(view.getContext(), s180DipInPixel, 0));
            adapterViewHolder.peoplePhoto.setVisibility(0);
        }
        setCheckBoxStatus(holder, cursor);
    }

    private void displayNameAndNumber(AdapterViewHolder holder, Context context, String name, String number) {
        if (TextUtils.isEmpty(name)) {
            holder.name.setVisibility(8);
            holder.number.setTextSize(context.getResources().getDimension(R.dimen.autorecord_user_list_noname_size) / 3.0f);
            holder.number.setTextColor(context.getResources().getColor(R.color.autorecord_list_item_name_text_color));
            holder.number.requestLayout();
        } else {
            holder.name.setText(name);
            holder.name.setVisibility(0);
            holder.number.setTextSize(context.getResources().getDimension(R.dimen.autorecord_user_list_hasname_size) / 3.0f);
            holder.number.setTextColor(context.getResources().getColor(R.color.autorecord_list_item_number_text));
            holder.number.requestLayout();
        }
        holder.number.setText(number);
    }

    private void setCheckBoxStatus(AdapterViewHolder holder, Cursor cursor) {
        if (this.mIconState != 0) {
            holder.checkBox.setChecked(this.mSeletedIds.contains(Long.valueOf(cursor.getLong(0))));
            if (3 == this.mState) {
                holder.checkBox.setVisibility(0);
            } else {
                holder.checkBox.setVisibility(8);
            }
        } else {
            holder.checkBox.setChecked(false);
            holder.checkBox.setVisibility(8);
        }
    }

    private void handleBitmap(Bitmap bitmap, AdapterViewHolder holder, Context context, View view) {
        if (bitmap != null) {
            Bitmap roundBitmap = Utils.createRoundPhoto(bitmap);
            StringBuilder sb = new StringBuilder();
            sb.append("bindListView() roundBitmap = ");
            sb.append(roundBitmap);
            Log.d("AutoRecordListAdapterWrapper", sb.toString());
            if (roundBitmap != null) {
                holder.peoplePhoto.setImageBitmap(roundBitmap);
                return;
            }
            return;
        }
        if (s180DipInPixel == -1) {
            s180DipInPixel = (int) TypedValue.applyDimension(1, 180.0f, context.getResources().getDisplayMetrics());
        }
        holder.peoplePhoto.setImageResource(Utils.getDefaultAvatarResId(view.getContext(), s180DipInPixel, 0));
    }
}
