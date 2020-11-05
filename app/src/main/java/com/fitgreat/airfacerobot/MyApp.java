package com.fitgreat.airfacerobot;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.fitgreat.airfacerobot.launcher.utils.CrashHandler;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.RouteUtils;


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
//        LogUtils.DEBUG = TextUtils.equals(SpUtils.getString(this, ApiDomainManager.ENVIRONMENT_CONFIG_KEY, "debug"), "debug");
//        LogUtils.init(true, "AirFaceRobotHxk");
        LogUtils.init(true);
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        mContext = this;
        registerLifecycle();
        //app进程唤醒5秒后如果主页没启动，则启动主页
        handler.postDelayed(() -> {
            LogUtils.d(TAG, "  isMainActivityRunning  ,  " + !isMainActivityRunning);
            if (!isMainActivityRunning) {
                if (SpeechManager.isDdsInitialization()) {
                    //DDS需要重新初始化
                    SpeechManager.instance(this).restoreToDo();
                }
                //终止RobotBrainService服务
                stopService(new Intent(this, RobotBrainService.class));
                RouteUtils.goHome(this);
            }
        }, 5 * 1000);
        LogUtils.d(TAG, "-----------START_APP_TAG------");
        MyCrashHandler handler = new MyCrashHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }

    private void registerLifecycle() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//                LogUtils.d(TAG, "-----------onActivityCreated------" + activity.getClass().getSimpleName());
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
//                LogUtils.d(TAG, "-----------onActivityDestroyed------" + activity.getLocalClassName());
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
