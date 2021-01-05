//package com.fitgreat.airfacerobot.launcher.receiver;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//
//import com.fitgreat.airfacerobot.RobotInfoUtils;
//import com.fitgreat.airfacerobot.constants.Constants;
//import com.fitgreat.airfacerobot.constants.RobotConfig;
//import com.fitgreat.airfacerobot.model.DaemonEvent;
//import com.fitgreat.airfacerobot.launcher.utils.OperationUtils;
//import com.fitgreat.airfacerobot.launcher.utils.ToastUtils;
//import com.fitgreat.archmvp.base.util.LogUtils;
//
//import org.greenrobot.eventbus.EventBus;
//
//
///**
// * 心率广播用于唤醒app作用<p>
// *
// * @author zixuefei
// * @since 2020/4/14 0014 13:26
// */
//public class HeartBeatBroadcast extends BroadcastReceiver {
//    private final String TAG = HeartBeatBroadcast.class.getSimpleName();
//    private final Handler handler = new Handler(Looper.getMainLooper());
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        String action = intent.getAction();
//        if (Constants.HEART_BEAT_ACTION.equals(action)) {
//            LogUtils.d(TAG, "---------HEART_BEAT_ACTION-------");
//            Bundle data = intent.getBundleExtra("daemon");
//            if (data != null) {
//                String type = data.getString("type", "");
//                LogUtils.d(TAG, "---------type:" + type);
//
//                switch (type) {
//                    case "showVersionTips":
//                        String content = data.getString("content", "");
//                        String extraMsg = data.getString("extraMsg", "");
//                        LogUtils.d(TAG, "-----content:" + content + " extraMsg:" + extraMsg);  //APP升级  TODO
////                        EventBus.getDefault().post(new DaemonEvent(RobotConfig.TYPE_SHOW_VERSION_TIPS, content, extraMsg));
//                        break;
//                    case "updateSuccess":
////                        LogUtils.d("task_robot_status", "-----------updateSuccess--------AirFace升级成功");
////                        RobotInfoUtils.setRobotRunningStatus("1");
////                        handler.postDelayed(() -> {
////                            ToastUtils.showSmallToast("AirFaceRobot升级成功");
////                            EventBus.getDefault().post(new DaemonEvent(RobotConfig.TYPE_SHOW_VERSION_STATUS, "updateSuccess"));
////                        }, 5000);
//                        break;
//                    case "downloadProgress":
//                        int progress = data.getInt("progress", 0);
//                        LogUtils.d(TAG, "-----downloadProgress:" + progress);
//                        EventBus.getDefault().post(new DaemonEvent(RobotConfig.TYPE_SHOW_VERSION_PROGRESS, String.valueOf(progress)));
//                        break;
//                    case "startInstall":
//                        LogUtils.d(TAG, "-----startInstall-------");
//                        break;
//                    case "backupSuccess":
//                        LogUtils.d("task_robot_status", "-----backupSuccess------");
////                        RobotInfoUtils.setRobotRunningStatus("1");
////                        handler.postDelayed(() -> {
////                            ToastUtils.showSmallToast("AirFaceRobot异常已恢复");
////                            EventBus.getDefault().post(new DaemonEvent(RobotConfig.TYPE_SHOW_VERSION_STATUS, "backupSuccess"));
////                        }, 5000);
//                        break;
//                    case "updateFailed":
////                        RobotInfoUtils.setRobotRunningStatus("1");
////                        String reason = data.getString("reason", "未知错误");
////                        LogUtils.d("task_robot_status","-----updateFailed------" + reason);
////                        ToastUtils.showSmallToast(reason);
////                        //AirFace安装失败
////                        OperationUtils.saveSpecialLog("APPInstallFail", reason);
////                        EventBus.getDefault().post(new DaemonEvent(RobotConfig.TYPE_SHOW_VERSION_STATUS, "updateFailed"));
//                        break;
//                    case "timerCheckUpdate":
//                        String status = data.getString("action", "noNewVersion");
//                        LogUtils.d(TAG, "-----timerCheckUpdate------" + status);
//                        EventBus.getDefault().post(new DaemonEvent(RobotConfig.TYPE_SHOW_VERSION_STATUS, status));
//                        break;
//                    default:
//                        break;
//                }
//            }
//        }
//    }
//
////    private boolean isLauncherRunning(AirFaceApp airFaceApp) {
////        return airFaceApp != null && airFaceApp.isLauncherActivityRunning();
////    }
//}
