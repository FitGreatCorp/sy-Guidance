package com.fitgreat.archmvp.base.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * activity跳转工具<p>
 *
 * @author zixuefei
 * @since 2019/7/18 21:29
 */
public class RouteUtils {

    public static void goHome(Context context) {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        home.addCategory(Intent.CATEGORY_HOME);
        context.startActivity(home);
    }

    public static <T extends Activity> void goToActivity(Context context, String action, String category) {
        try {
            if (context == null || TextUtils.isEmpty(action)) {
                return;
            }
            Intent intent = new Intent(action);
            intent.addCategory(category);
            context.startActivity(intent);
        } catch (Exception e) {
            LogUtils.e("RouteUtils", "EXCEPTION:" + e.getMessage());
        }
    }

    public static <T extends Activity> void goToActivity(Context context, String action) {
        try {
            if (context == null || TextUtils.isEmpty(action)) {
                return;
            }
            Intent intent = new Intent(action);
            context.startActivity(intent);
        } catch (Exception e) {
            LogUtils.e("RouteUtils", "EXCEPTION:" + e.getMessage());
        }
    }

    public static <T extends Activity> void goToActivity(Context context, String action, Bundle data) {
        try {
            if (context == null || TextUtils.isEmpty(action)) {
                return;
            }
            Intent intent = new Intent(action);
            if (data != null) {
                intent.putExtras(data);
            }
            context.startActivity(intent);
        } catch (Exception e) {
            LogUtils.e("RouteUtils", "EXCEPTION:" + e.getMessage());
        }
    }

    public static <T extends Activity> void goToActivity(Context context, Class<T> activity) {
        try {
            if (context == null || activity == null) {
                return;
            }
            Intent intent = new Intent(context, activity);
            context.startActivity(intent);
        } catch (Exception e) {
            LogUtils.e("RouteUtils", "EXCEPTION:" + e.getMessage());
        }
    }

    public static <T extends Activity> void goToActivity(Context context, Class<T> activity, Bundle data) {
        try {
            if (context == null || activity == null) {
                return;
            }
            Intent intent = new Intent(context, activity);
            if (data != null) {
                intent.putExtras(data);
            }
            context.startActivity(intent);
        } catch (Exception e) {
            LogUtils.e("RouteUtils", "EXCEPTION:" + e.getMessage());
        }
    }

    public static <T extends Activity> void goToActivity(Context context, Class<T> activity, Bundle data, int flag) {
        try {
            if (context == null || activity == null) {
                return;
            }
            Intent intent = new Intent(context, activity);
            if (data != null) {
                intent.putExtras(data);
            }
            intent.addFlags(flag);
            context.startActivity(intent);
        } catch (Exception e) {
            LogUtils.e("RouteUtils", "EXCEPTION:" + e.getMessage());
        }
    }

//    public static void goToWebActivity(Context context, String title, String url, boolean isShowTitleBar, boolean isDarkFontStatus) {
//        WebPageEntity webPageEntity = new WebPageEntity();
//        webPageEntity.title = title;
//        webPageEntity.url = url;
//        webPageEntity.isShowTitleBar = isShowTitleBar;
//        webPageEntity.isDarkFont = isDarkFontStatus;
//        WebpageActivity.startWebpageActivity(context, webPageEntity);
//    }
//
//    public static void goToWebActivity(Context context, String title, String url, boolean isShowTitleBar, boolean isDarkFontStatus, int flag) {
//        WebPageEntity webPageEntity = new WebPageEntity();
//        webPageEntity.title = title;
//        webPageEntity.url = url;
//        webPageEntity.isShowTitleBar = isShowTitleBar;
//        webPageEntity.isDarkFont = isDarkFontStatus;
//        WebpageActivity.startWebpageActivity(context, webPageEntity, flag);
//    }

    public static void openBrowser(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            LogUtils.e("zxf", "------url is empty------------");
            return;
        }
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        // 注意此处的判断intent.resolveActivity()可以返回显示该Intent的Activity对应的组件名
        // 官方解释 : Name of the component implementing an activity that can display the intent
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            final ComponentName componentName = intent.resolveActivity(context.getPackageManager());
            LogUtils.d("zxf", "componentName = " + componentName.getClassName());
            context.startActivity(Intent.createChooser(intent, "请选择浏览器"));
        } else {
            LogUtils.e("zxf", "------沒有发现可打开程序------------");
        }
    }

    public static void sendDaemonBroadcast(Context context, String action, Bundle data) {
        Intent intent = new Intent(action);
        if (data != null) {
            intent.putExtra("robot", data);
        }
        intent.setPackage("com.fitgreat.airfacedaemon");
        context.sendBroadcast(intent);
    }
}
