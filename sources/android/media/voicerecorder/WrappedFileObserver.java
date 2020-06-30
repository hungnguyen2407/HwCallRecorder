package android.media.voicerecorder;

import android.os.FileObserver;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public abstract class WrappedFileObserver {
    private static final HashMap<File, Set<WrappedFileObserver>> sObserverLists = new HashMap<>();
    private final File mFile;
    /* access modifiers changed from: private */
    public final int mMask;
    private FileObserver mObserver;

    public abstract void onEvent(int i, String str);

    public WrappedFileObserver(String path, int mask) {
        this.mFile = new File(path);
        this.mMask = mask;
    }

    public void startWatching() {
        synchronized (sObserverLists) {
            if (!sObserverLists.containsKey(this.mFile)) {
                sObserverLists.put(this.mFile, new HashSet());
            }
            final Set<WrappedFileObserver> wrappedObservers = (Set) sObserverLists.get(this.mFile);
            this.mObserver = wrappedObservers.size() > 0 ? ((WrappedFileObserver) wrappedObservers.iterator().next()).mObserver : new FileObserver(this.mFile.getPath()) {
                public void onEvent(int event, String path) {
                    for (WrappedFileObserver wrappedObserver : wrappedObservers) {
                        if ((wrappedObserver.mMask & event) != 0) {
                            wrappedObserver.onEvent(event, path);
                        }
                    }
                }
            };
            this.mObserver.startWatching();
            wrappedObservers.add(this);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void stopWatching() {
        /*
            r3 = this;
            java.util.HashMap<java.io.File, java.util.Set<android.media.voicerecorder.WrappedFileObserver>> r0 = sObserverLists
            monitor-enter(r0)
            java.util.HashMap<java.io.File, java.util.Set<android.media.voicerecorder.WrappedFileObserver>> r1 = sObserverLists     // Catch:{ all -> 0x0029 }
            java.io.File r2 = r3.mFile     // Catch:{ all -> 0x0029 }
            java.lang.Object r1 = r1.get(r2)     // Catch:{ all -> 0x0029 }
            java.util.Set r1 = (java.util.Set) r1     // Catch:{ all -> 0x0029 }
            if (r1 == 0) goto L_0x0027
            android.os.FileObserver r2 = r3.mObserver     // Catch:{ all -> 0x0029 }
            if (r2 != 0) goto L_0x0014
            goto L_0x0027
        L_0x0014:
            r1.remove(r3)     // Catch:{ all -> 0x0029 }
            int r2 = r1.size()     // Catch:{ all -> 0x0029 }
            if (r2 != 0) goto L_0x0022
            android.os.FileObserver r2 = r3.mObserver     // Catch:{ all -> 0x0029 }
            r2.stopWatching()     // Catch:{ all -> 0x0029 }
        L_0x0022:
            r2 = 0
            r3.mObserver = r2     // Catch:{ all -> 0x0029 }
            monitor-exit(r0)     // Catch:{ all -> 0x0029 }
            return
        L_0x0027:
            monitor-exit(r0)     // Catch:{ all -> 0x0029 }
            return
        L_0x0029:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0029 }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.voicerecorder.WrappedFileObserver.stopWatching():void");
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        stopWatching();
    }
}
