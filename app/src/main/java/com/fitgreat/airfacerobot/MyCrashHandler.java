package com.fitgreat.airfacerobot;

import android.os.Environment;
import android.text.TextUtils;

import com.fitgreat.airfacerobot.launcher.utils.OperationUtils;
import com.fitgreat.archmvp.base.util.LogUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;

public class MyCrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "MyCrashHandler";

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LogUtils.e(DEFAULT_LOG_TAG, "Thread = " + t.getName() + "\nThrowable = " + e.getMessage());
        String stackTraceInfo = getStackTraceInfo(e);
        LogUtils.e(DEFAULT_LOG_TAG, stackTraceInfo);
        saveThrowableMessage(stackTraceInfo);
    }

    /**
     * 获取错误的信息
     *
     * @param throwable
     * @return
     */
    private String getStackTraceInfo(final Throwable throwable) {
        PrintWriter pw = null;
        Writer writer = new StringWriter();
        try {
            pw = new PrintWriter(writer);
            throwable.printStackTrace(pw);
        } catch (Exception e) {
            return "";
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
        return writer.toString();
    }

    private String logFilePath = Environment.getExternalStorageDirectory() + File.separator + "Android" +
            File.separator + "data" + File.separator + "com.fitgreat.airfacerobot " + File.separator + "crashLog";

    private void saveThrowableMessage(String errorMessage) {
        if (TextUtils.isEmpty(errorMessage)) {
            return;
        }
        LogUtils.e(DEFAULT_LOG_TAG,errorMessage);
        OperationUtils.saveSpecialLog("DangerError", errorMessage);
        try {
            Thread.sleep(2*3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void writeStringToFile(final String errorMessage, final File file) {
        new Thread(() -> {
            FileOutputStream outputStream = null;
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(errorMessage.getBytes());
                outputStream = new FileOutputStream(new File(file, System.currentTimeMillis() + ".txt"));
                int len = 0;
                byte[] bytes = new byte[1024];
                while ((len = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, len);
                }
                outputStream.flush();
                LogUtils.e(DEFAULT_LOG_TAG, "写入本地文件成功：" + file.getAbsolutePath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

}
