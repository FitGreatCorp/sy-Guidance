package com.voice.caePk.track;

import android.util.Log;


public class MyPcmPlayer {
    Thread mThread = null;
    MyThreadRunnable myThreadRunnable;

    public void play(byte[] data) {
        if (data == null) {
            Log.d("MyPcmPlayer", "No data...");
            return;
        }
        if (mThread == null) {
            myThreadRunnable = new MyThreadRunnable();
            mThread = new Thread(myThreadRunnable);
            mThread.start();
        }

        myThreadRunnable.playData(data);
    }

    public void pause() {
        if (myThreadRunnable != null) {
            myThreadRunnable.pausePlay();
        }

    }

    public void resume() {
        if (myThreadRunnable != null) {
            myThreadRunnable.resumePlay();
        }

    }

    public void destory() {
        if (myThreadRunnable != null) {
            myThreadRunnable.destoryObj();

        }
    }

    public void stopPlay() {
        if (myThreadRunnable != null) {
            myThreadRunnable.stopPlay();
            myThreadRunnable = null;
        }
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
    }

}

