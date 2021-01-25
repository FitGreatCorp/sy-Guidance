package com.fitgreat.airfacerobot.launcher.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;

import com.fitgreat.archmvp.base.util.LogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;

public class AppVersionUtils {

    /**
     * 获取软件版本号
     */
    public static int getSoftwareVersionNumber(Context context) {
        int softwareVersionNumber = 0;
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> info = packageManager.getInstalledPackages(0);
        if (info == null || info.isEmpty()) {
            return softwareVersionNumber;
        }
//        LogUtils.d(DEFAULT_LOG_TAG, "install size:" + info.size());
        for (PackageInfo packageInfo : info) {
            if (packageInfo != null) {
                if ("com.fitgreat.airfacerobot".equals(packageInfo.packageName)) {
                    LogUtils.d(DEFAULT_LOG_TAG, "versionCode:" + packageInfo.versionCode);
                    return packageInfo.versionCode;
                }
            }
        }
        return softwareVersionNumber;
    }
}
