package com.fitgreat.airfacerobot.launcher.utils;

import android.content.Context;
import android.os.Environment;

import com.fitgreat.archmvp.base.util.LogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MoveFileUtils {


    /**
     * @param assetsName
     * @param endFilePath
     */
    public static void copyFileFromAssetsTo(String assetsName, String endFilePath, Context context) {
        InputStream assetInputStream = null;
        FileOutputStream endFileOutputStream = null;
        File endFile = new File(endFilePath+"/",assetsName);

//        File endFile = new File(context.getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath(), assetsName + ".bin");

        if (endFile.exists()) {
            endFile.delete();
        } else {
            endFile.mkdirs();
        }
        LogUtils.d("copyFileToDisk","  endFile   "+endFile.getAbsolutePath());
        try {
            assetInputStream = context.getAssets().open(assetsName);
            endFileOutputStream = new FileOutputStream(endFile);
            int length = -1;
            byte[] buf = new byte[1024];
            while ((length = assetInputStream.read(buf)) != -1) {
                endFileOutputStream.write(buf, 0, length);
            }
            endFileOutputStream.flush();
        } catch (IOException e) {
            LogUtils.e("copyFileToDisk","  IOException   "+e.getMessage());
//            endFile.delete();
//            copyFileFromAssetsTo(assetsName, endFilePath, context);
            e.printStackTrace();
        } finally {
            try {
                if (assetInputStream != null) {
                    assetInputStream.close();
                }
                if (endFileOutputStream != null) {
                    endFileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
