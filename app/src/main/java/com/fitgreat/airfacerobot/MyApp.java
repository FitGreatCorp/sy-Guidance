package com.fitgreat.airfacerobot;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.fitgreat.airfacerobot.launcher.ui.activity.RobotInitActivity;
import com.fitgreat.airfacerobot.launcher.utils.CrashHandler;
import com.fitgreat.airfacerobot.launcher.utils.LanguageUtil;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.RouteUtils;

import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;

/**
 * app启动入口<p>
 *
 * @author zixuefei
 * @since 2020/3/17 0017 15:35
 */
public class MyApp extends Application {
    private final String TAG = "MyApp";
    private static Context mContext;
    private static final int START_APP_TAG = 300;
    //电量过低提示
    private static boolean lowBatteryPromptTag = false;
    private boolean isMainActivityRunning = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.init(true);
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        mContext = this;
        registerLifecycle();
        //app进程唤醒15秒后如果主页没启动，则启动主页
        handler.postDelayed(() -> {
            LogUtils.d(DEFAULT_LOG_TAG, "  isMainActivityRunning  ,  " + !isMainActivityRunning);
            if (!isMainActivityRunning) {
                //重启应用
                RouteUtils.goHome(this);
            }
        }, 5 * 1000);
        LogUtils.d(DEFAULT_LOG_TAG, "-----------START_APP_TAG------");
        MyCrashHandler handler = new MyCrashHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }
    private void registerLifecycle() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                if ("MainActivity".equals(activity.getClass().getSimpleName())) {
                    isMainActivityRunning = true;
                }
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                if ("MainActivity".equals(activity.getClass().getSimpleName())) {
                    lowBatteryPromptTag = true;
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                if ("MainActivity".equals(activity.getClass().getSimpleName())) {
                    lowBatteryPromptTag = false;
                }
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                if ("MainActivity".equals(activity.getClass().getSimpleName())) {
                    isMainActivityRunning = false;
                }
            }
        });
    }

    public static Context getContext() {
        return mContext;
    }

    public static boolean isNeedLowBatteryPrompt() {
        return lowBatteryPromptTag;
    }
}
