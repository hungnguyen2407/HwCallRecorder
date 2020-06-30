package android.media.voicerecorder;

import android.content.Context;
import java.io.File;

public class VoiceRecorderFactory {
    public static BaseVoiceRecorder createVoiceRecorder(Context context) {
        if (new File("/dev/msm_voicememo").exists() == 0) {
            return new TIVoiceRecorder(context);
        }
        return new QCVoiceRecorder(context);
    }
}
