package com.android.phone.recorder;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRecordService extends IInterface {

    public static abstract class Stub extends Binder implements IRecordService {
        public Stub() {
            attachInterface(this, "com.android.phone.recorder.IRecordService");
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String descriptor = "com.android.phone.recorder.IRecordService";
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(descriptor);
                        voiceRecord(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(descriptor);
                        addRecordServiceAdapter(com.android.phone.recorder.IRecordServiceAdapter.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(descriptor);
                        boolean _result = isAutoRecordNumber(data.readString(), data.readInt() != 0);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 4:
                        data.enforceInterface(descriptor);
                        String _result2 = getAutoRecordNumberName(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case 5:
                        data.enforceInterface(descriptor);
                        boolean _result3 = isRecording();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 6:
                        data.enforceInterface(descriptor);
                        setActiveSubscription(data.readInt());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(descriptor);
                return true;
            }
        }
    }

    void addRecordServiceAdapter(IRecordServiceAdapter iRecordServiceAdapter) throws RemoteException;

    String getAutoRecordNumberName(String str) throws RemoteException;

    boolean isAutoRecordNumber(String str, boolean z) throws RemoteException;

    boolean isRecording() throws RemoteException;

    void setActiveSubscription(int i) throws RemoteException;

    void voiceRecord(String str, String str2) throws RemoteException;
}
