package android.media.voicerecorder;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import java.io.File;

public class CallRecorderFileObserver extends WrappedFileObserver {
    private String mFileName;
    private Handler mHandler;

    public CallRecorderFileObserver(String path) {
        super(new File(path).getParent(), 3648);
        this.mFileName = new File(path).getName();
    }

    public void onEvent(int event, String path) {
        if (!TextUtils.isEmpty(this.mFileName) && this.mFileName.equals(path)) {
            if (event == 64 || event == 512 || event == 1024 || event == 2048) {
                Message message = new Message();
                message.what = 2;
                if (this.mHandler != null) {
                    this.mHandler.sendMessage(message);
                }
            }
        }
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }
}
