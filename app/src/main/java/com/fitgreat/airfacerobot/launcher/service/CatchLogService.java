package com.fitgreat.airfacerobot.launcher.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

import com.fitgreat.archmvp.base.util.LogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CatchLogService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.i("LogCatcherReceiver", "start service");
        catchLog();
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
    }

    public void catchLog() {
        new Thread(new Runnable() {

            public void run() {
                // TODO Auto-generated method stub
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                String shell = "logcat";
                try {
                    Process process = Runtime.getRuntime().exec(shell);
                    InputStream inputStream = process.getInputStream();
                    boolean sdCardExist = Environment.getExternalStorageState().equals(
                            android.os.Environment.MEDIA_MOUNTED);
                    File dir = null;
                    if (sdCardExist) {
                        dir = new File(Environment.getExternalStorageDirectory().toString()
                                + File.separator + "logcat.txt");
                        LogUtils.i("LogCatcherReceiver", Environment.getExternalStorageDirectory()
                                .toString() + File.separator + "logcat.txt");

                        if(dir.exists()){
                            dir.delete();
                        }
//                        if (!dir.exists()) {
                            dir.createNewFile();
//                        }

                    }
                    byte[] buffer = new byte[1024];
                    try {
                        if (dir == null) {
                            LogUtils.i("LogCatcherReceiver", "file==null");
                        }
                        FileOutputStream fos = new FileOutputStream(dir);
                        int read = 0;
                        try {
                            {
                                while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
                                    fos.write(buffer, 0, read);
                                }
                            }
                        } finally {
                            fos.close();
                        }
                    } finally {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

}
