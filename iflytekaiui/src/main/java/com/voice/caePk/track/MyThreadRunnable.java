package com.voice.caePk.track;

import android.media.AudioFormat;
import android.util.Log;

import java.util.concurrent.ConcurrentLinkedQueue;

public class MyThreadRunnable implements Runnable {
    boolean isStop = false;
    boolean isExit = false;
    byte[] data;
    private ConcurrentLinkedQueue<AudioData> mAudioQueue = new ConcurrentLinkedQueue<AudioData>();

    public MyThreadRunnable() {
        // TODO Auto-generated constructor stub
        isStop = false;
        isExit = false;
    }

    public void stopPlay() {
        isStop = true;
        //清除数据
        clearBuffer();
    }

    // 开始播放数据，添加到队列中
    public void playData(byte[] buffer) {
        if (mAudioQueue != null) {
            mAudioQueue.add(new AudioData(buffer, buffer.length));
        }

    }

    private void clearBuffer() {
        //清除当前缓存数据
        if (mAudioQueue != null) {
            mAudioQueue.clear();
        }

    }

    public void pausePlay() {
        //不清除数据
        isStop = true;
    }

    public void resumePlay() {
        isStop = false;
    }

    public void destoryObj() {
        isExit = true;
        //清除数据
        clearBuffer();
    }

    public void run() {
        Log.i("MyThread", "run..");
        isStop = false;
        // MyAudioTrack:   ��AudioTrack���м򵥷�װ����
        MyAudioTrack myAudioTrack = new MyAudioTrack(16000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        myAudioTrack.init();

        int playSize = myAudioTrack.getPrimePlaySize();
        AudioData data;
        while (!isExit) {
            try {
                if (!isStop) {
                    //从队列中获取音频数据 大小 playSize
                    data = mAudioQueue.poll();
                    if (data != null) {
                        myAudioTrack.playAudioTrack(data.mData, 0, data.mDataLen);
                        data.mData = null;
                        data = null;
                    }
                } else {
                    Thread.sleep(100);
                }

            } catch (Exception e) {
                // TODO: handle exception
                break;
            }
        }
        clearBuffer();
        myAudioTrack.release();
    }


    class AudioData {
        public byte[] mData;
        public int mDataLen;

        public AudioData(byte[] data, int dataLen) {
            mData = data;
            mDataLen = dataLen;
        }
    }


}