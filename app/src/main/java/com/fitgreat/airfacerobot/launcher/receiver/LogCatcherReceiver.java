package com.fitgreat.airfacerobot.launcher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fitgreat.airfacerobot.launcher.service.CatchLogService;
import com.fitgreat.archmvp.base.util.LogUtils;

public class LogCatcherReceiver extends BroadcastReceiver {

    static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        LogUtils.i("LogCatcherReceiver","receive log");
        context.startService(new Intent(context,
                CatchLogService.class));
    }
}
