package com.fitgreat.airfacerobot.versionupdate;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;

import com.fitgreat.airfacerobot.R;
import com.fitgreat.archmvp.base.util.LogUtils;


/**
 * 自定义App升级通知栏工具<p>
 *
 * @author zixuefei
 * @since 2019/4/15 20:18
 */
public class AppUpdateNotification {
    private final static String TAG = AppUpdateNotification.class.getSimpleName();
    private final static int DOWNLOAD_NOTIFICATION_ID = 666;
    private NotificationManager notificationManager;
    private Notification notification;
    private Context context;
    private Notification.Builder builder;

    public AppUpdateNotification(Context context) {
        this.context = context;
        initNotification();
    }

    private void initNotification() {
        LogUtils.d(TAG, "----initActuator notification----------");
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new Notification.Builder(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel_id", "APP升级通知", NotificationManager.IMPORTANCE_LOW);
            channel.enableLights(true);//闪光
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);//锁屏显示通知
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId("channel_id");
        }

        builder.setContentTitle("正在更新...") //设置通知标题
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round)) //设置通知的大图标
                .setDefaults(Notification.DEFAULT_LIGHTS) //设置通知的提醒方式： 呼吸灯
                .setPriority(Notification.PRIORITY_MAX) //设置通知的优先级：最大
                .setAutoCancel(false)//设置通知被点击一次是否自动取消
                .setContentText("下载进度:" + "0%")
                .setProgress(100, 0, false);
        notification = builder.build();//构建通知对象
    }

//    private void initNotification() {
//        LogUtils.d(TAG, "----initActuator notification----------");
//        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        Notification.Builder mBuilder = new Notification.Builder(context);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel("channel_id", "APP升级通知", NotificationManager.IMPORTANCE_LOW);
//            channel.setLightColor(Color.GREEN);
//            channel.enableLights(true);
//            channel.enableVibration(true);
//            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
//            notificationManager.createNotificationChannel(channel);
//            mBuilder.setChannelId("channel_id");
//        }
//
//        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.mine_app_update_notification_layout);
//
////        Intent intentLast = new Intent(DOWNLOAD_NOTIFICATION_ACTION);
////        intentLast.putExtra("status", 1);
////        intentLast.setPackage(context.getPackageName());
////        PendingIntent pIntentLast = PendingIntent.getBroadcast(context, 1, intentLast, PendingIntent.FLAG_UPDATE_CURRENT);
////        contentView.setOnClickPendingIntent(R.id.notification_music_last, pIntentLast);//为控件注册事件
////
////
////        Intent intentNext = new Intent(DOWNLOAD_NOTIFICATION_ACTION);
////        intentNext.putExtra("status", 2);
////        intentNext.setPackage(context.getPackageName());
////        PendingIntent pIntentNext = PendingIntent.getBroadcast(context, 2, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
////        contentView.setOnClickPendingIntent(R.id.notification_music_next, pIntentNext);
//
//        Intent mainIntent = new Intent(DOWNLOAD_NOTIFICATION_ACTION);
//        mainIntent.putExtra("status", 0);
//        mainIntent.setPackage(context.getPackageName());
//        PendingIntent intent = PendingIntent.getBroadcast(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        mBuilder.setContent(contentView)
//                .setOngoing(true)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setPriority(Notification.PRIORITY_DEFAULT)
//                .setContentIntent(intent);
//
//        notification = mBuilder.build();
//        notification.flags = Notification.FLAG_AUTO_CANCEL;//设置通知点击或滑动时不被清除
//    }

    public void show() {
        LogUtils.d(TAG, "----show notification----------");
        notificationManager.notify(DOWNLOAD_NOTIFICATION_ID, notification);//开启通知
    }

    public void updateNotification(int progresss) {
        LogUtils.d(TAG, "----update notification----------" + progresss);
        builder.setProgress(100, progresss, false)
                .setContentText("下载进度:" + progresss + "%");
        notification = builder.build();
        notificationManager.notify(DOWNLOAD_NOTIFICATION_ID, notification);//开启通知
    }

    public void clickNotificationInstall(Context context, String filePath) {
        if (context == null || TextUtils.isEmpty(filePath)) {
            return;
        }
        LogUtils.d(TAG, "----click notification install-------");
        builder.setProgress(100, 100, false)
                .setContentTitle("下载完成")
                .setContentText("点击安装")
                .setAutoCancel(false);
        //点击安装代码块
        PendingIntent pi = PendingIntent.getActivity(context, 0, DownloadUtils.createInstallIntent(context, filePath), 0);
        builder.setContentIntent(pi);

        notification = builder.build();
        notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_AUTO_CANCEL;//设置通知点击或滑动时不被清除
        notificationManager.notify(DOWNLOAD_NOTIFICATION_ID, notification);//开启通知
    }

    public void clear() {
        LogUtils.d(TAG, "----clear notification----------");
        if (notificationManager != null) {
            notificationManager.cancel(DOWNLOAD_NOTIFICATION_ID);
            notificationManager = null;
            notification = null;
        }
    }
}

