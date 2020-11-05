package com.fitgreat.headeractuator;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * 头部舵机连接器<p>
 *
 * @author zixuefei
 * @since 2020/3/25 18:34
 */
public class ActuatorConnector {
    private final String TAG = ActuatorConnector.class.getSimpleName();
    private static final String DEVICE_NAME = "/dev/ttysWK2";
    private static final int SPEED = 115200;
    private SerialPort mSerialPortDevice;
    private ReadThread mReadThread;
    private DataListener mDataListeners;

    public ActuatorConnector(DataListener mDataListeners) {
        this.mDataListeners = mDataListeners;
    }

    /**
     * 打开指定串口
     */
    public int initSerialPort() {
        try {
            mReadThread = new ReadThread();
            mReadThread.start();
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 通过串口发数据给motor
     *
     * @param data
     * @return
     */
    public int send(byte[] data) {
        if (mSerialPortDevice == null) {
            return -1;
        }
        try {
            mSerialPortDevice.getOutputStream().write(data);
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 关闭串口通信,并终止读取线程
     */
    public void destroy() {
        if (mSerialPortDevice != null) {
            mSerialPortDevice.close();
            mSerialPortDevice = null;
        }
        if (mReadThread != null) {
            mReadThread.interrupt();
        }
    }

    /**
     * 不断读取串口数据,如是指定格式的发给监听者
     */
    private class ReadThread extends Thread {
        private boolean waitLen = true;
        private byte[] recvBuf = new byte[10];
        private int recvIndex = 0;

        public ReadThread() throws IOException {
            super();
            mSerialPortDevice = new SerialPort(new File(DEVICE_NAME), SPEED, 0);
        }

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    if (mSerialPortDevice == null) {
                        return;
                    }
                    byte[] buffer = new byte[64];
                    InputStream input = mSerialPortDevice.getInputStream();
                    if (input == null) {
                        return;
                    }
                    size = input.read(buffer);
                    if (size <= 0) {
                        continue;
                    }

                    recvData(buffer, 0, size);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }


        private void recvData(byte[] buffer, int offset, int size) {
            //当前buffer剩余可写字节数
            int writeLen = Math.min(recvBuf.length - recvIndex, size);
            System.arraycopy(buffer, offset, recvBuf, recvIndex, writeLen);
            recvIndex += writeLen;

            //当前buffer写满
            if (recvIndex == recvBuf.length) {
                if (waitLen) {
                    //消息头是否符合串口消息格式定义
                    if (recvBuf[0] != (byte) 0xFD) {
                        Log.d(TAG, "recv data not SYNC HEAD, drop " + Arrays.toString(recvBuf));
                        recvIndex = 0;
                    } else {
                        //Log.d(TAG, "head msg" + Arrays.toString(recvBuf));
                        //定长10个字节
                        int msgLen = 10;
                        byte[] msgBuf = new byte[msgLen];
                        //将已接收的消息头拷贝到申请的消息buf中
                        System.arraycopy(recvBuf, 0, msgBuf, 0, recvBuf.length);
                        recvBuf = msgBuf;
                        waitLen = false;
                    }
                } else {
                    //完整消息接收完成
                    if (mDataListeners != null) {
                        mDataListeners.onReceive(recvBuf);
                    }
                    recvIndex = 0;
                    recvBuf = new byte[10];
                    waitLen = true;
                }
            }

            //如果还有剩余字节未处理,这里一定是size更大
            if (writeLen != size) {
                recvData(buffer, offset + writeLen, size - writeLen);
            }
        }
    }

    public interface DataListener {
        void onReceive(byte[] data);
    }
}
