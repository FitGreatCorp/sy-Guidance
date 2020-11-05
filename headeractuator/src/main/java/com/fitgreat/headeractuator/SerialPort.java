package com.fitgreat.headeractuator;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 开发板串口通信<p>
 *
 * @author zixuefei
 * @since 2020/3/26 18:34
 */
public class SerialPort {
    private static final String TAG = SerialPort.class.getSimpleName();

    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public SerialPort(File device, int rate, int flags) throws IOException {
        mFd = open(device.getAbsolutePath(), rate, flags);
        if (null == mFd) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    // JNI
    private native static FileDescriptor open(String path, int baudrate, int flags);

    public native void close();

    static {
        System.loadLibrary("motor_serialport-jni");
    }

}