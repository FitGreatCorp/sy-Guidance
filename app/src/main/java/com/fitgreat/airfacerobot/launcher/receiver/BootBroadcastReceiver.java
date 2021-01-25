package com.fitgreat.airfacerobot.launcher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.SpUtils;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.FREE_OPERATION_STATE_TAG;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BootBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            SpUtils.putBoolean(MyApp.getContext(), FREE_OPERATION_STATE_TAG, false);
            LogUtils.d(DEFAULT_LOG_TAG, "Android操作系统开机了，运行中.......");
            SpUtils.putBoolean(MyApp.getContext(),"checkUpdateTag",true);
        } else if (Intent.ACTION_SHUTDOWN.equals(action)) {
            SpUtils.putBoolean(MyApp.getContext(),"checkUpdateTag",false);
            LogUtils.d(DEFAULT_LOG_TAG, "Android操作系统关机了.......");
        }
    }
}
