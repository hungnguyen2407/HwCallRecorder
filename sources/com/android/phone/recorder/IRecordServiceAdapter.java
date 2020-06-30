package com.android.phone.recorder;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRecordServiceAdapter extends IInterface {

    public static abstract class Stub extends Binder implements IRecordServiceAdapter {

        private static class Proxy implements IRecordServiceAdapter {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void onRecordStart() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.phone.recorder.IRecordServiceAdapter");
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onRecordTimeChange(String time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.phone.recorder.IRecordServiceAdapter");
                    _data.writeString(time);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onRecordStateChange(boolean recording) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.phone.recorder.IRecordServiceAdapter");
                    _data.writeInt(recording);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static IRecordServiceAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.android.phone.recorder.IRecordServiceAdapter");
            if (iin == null || !(iin instanceof IRecordServiceAdapter)) {
                return new Proxy(obj);
            }
            return (IRecordServiceAdapter) iin;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String descriptor = "com.android.phone.recorder.IRecordServiceAdapter";
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(descriptor);
                        onRecordStart();
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(descriptor);
                        onRecordTimeChange(data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(descriptor);
                        onRecordStateChange(data.readInt() != 0);
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

    void onRecordStart() throws RemoteException;

    void onRecordStateChange(boolean z) throws RemoteException;

    void onRecordTimeChange(String str) throws RemoteException;
}
