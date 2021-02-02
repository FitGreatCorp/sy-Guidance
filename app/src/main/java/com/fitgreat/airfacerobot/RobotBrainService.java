package com.fitgreat.airfacerobot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.launcher.widget.MyTipDialog;
import com.fitgreat.airfacerobot.model.WorkflowEntity;
import com.fitgreat.airfacerobot.speech.PlayTtsTask;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.business.BusinessRequest;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.headeractuator.HeaderActuatorManager;
import com.fitgreat.airfacerobot.launcher.utils.ToastUtils;
import com.fitgreat.airfacerobot.mediaplayer.TextPlayActivity;
import com.fitgreat.airfacerobot.model.ActionDdsEvent;
import com.fitgreat.airfacerobot.model.DaemonEvent;
import com.fitgreat.airfacerobot.model.InitEvent;
import com.fitgreat.airfacerobot.model.RobotSignalEvent;
import com.fitgreat.airfacerobot.model.NavigationTip;
import com.fitgreat.airfacerobot.launcher.service.UploadLogService;
import com.fitgreat.airfacerobot.launcher.utils.OperationUtils;
import com.fitgreat.airfacerobot.launcher.widget.YesOrNoDialogActivity;
import com.fitgreat.airfacerobot.mediaplayer.PdfPlayActivity;
import com.fitgreat.airfacerobot.mediaplayer.VideoPlayActivity;
import com.fitgreat.airfacerobot.remotesignal.SignalConfig;
import com.fitgreat.airfacerobot.remotesignal.SignalManager;
import com.fitgreat.airfacerobot.remotesignal.model.FilePlayEvent;
import com.fitgreat.airfacerobot.remotesignal.model.InitUiEvent;
import com.fitgreat.airfacerobot.remotesignal.model.JoinInfoData;
import com.fitgreat.airfacerobot.remotesignal.model.NextOperationData;
import com.fitgreat.airfacerobot.remotesignal.model.SignalDataEvent;
import com.fitgreat.airfacerobot.remotesignal.model.SpeakEvent;
import com.fitgreat.airfacerobot.ros.RosManager;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.airfacerobot.speech.observer.DuiCommandObserver;
import com.fitgreat.airfacerobot.speech.observer.DuiMessageObserver;
import com.fitgreat.airfacerobot.speech.observer.DuiUpdateObserver;
import com.fitgreat.airfacerobot.videocall.VideoCallConstant;
import com.fitgreat.airfacerobot.videocall.model.VideoMessageEve;
import com.fitgreat.archmvp.base.okhttp.BaseResponse;
import com.fitgreat.archmvp.base.okhttp.HttpCallback;
import com.fitgreat.archmvp.base.okhttp.HttpMainCallback;
import com.fitgreat.archmvp.base.util.Base64;
import com.fitgreat.archmvp.base.util.ExecutorManager;
import com.fitgreat.archmvp.base.util.FileUtils;
import com.fitgreat.archmvp.base.util.JsonUtils;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.NetworkUtils;
import com.fitgreat.archmvp.base.util.ShellCmdUtils;
import com.fitgreat.archmvp.base.util.SpUtils;
import com.fitgreat.headeractuator.ActuatorConstant;
import com.fitgreat.ros.CommandResult;
import com.fitgreat.ros.JRos;
import com.fitgreat.ros.JRosConfig;
import com.fitgreat.ros.JRosInitListener;
import com.fitgreat.ros.MoveStateObserver;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.fitgreat.airfacerobot.MyApp.getContext;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_ONE;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TWO;
import static com.fitgreat.airfacerobot.constants.Constants.DIALOG_CONTENT;
import static com.fitgreat.airfacerobot.constants.Constants.DIALOG_NO;
import static com.fitgreat.airfacerobot.constants.Constants.DIALOG_TITLE;
import static com.fitgreat.airfacerobot.constants.Constants.DIALOG_YES;
import static com.fitgreat.airfacerobot.constants.RobotConfig.AUTOMATIC_RECHARGE_ACTIVITY_ID;
import static com.fitgreat.airfacerobot.constants.RobotConfig.AUTOMATIC_RECHARGE_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CLICK_EMERGENCY_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CLOSE_DDS_WAKE_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CLOSE_START_INTRODUCTION_DIALOG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_FREE_OPERATION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_LANGUAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.DDS_OBSERVER_REGISTERED;
import static com.fitgreat.airfacerobot.constants.RobotConfig.DDS_STOP_DIALOG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.DDS_VOICE_TEXT_CANCEL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.FILE_PLAY_OK;
import static com.fitgreat.airfacerobot.constants.RobotConfig.FREE_OPERATION_STATE_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.GUIDE_WORK_FLOW_ACTION_ID;
import static com.fitgreat.airfacerobot.constants.RobotConfig.INIT_ROS_KEY_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.IS_CONTROL_MODEL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MAIN_PAGE_DIALOG_SHOW_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CANCEL_RECHARGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_POWER_LOCK;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_GETROS_VERSION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_INSTRUCTION_STATUS_FINISHED;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_ISLOCK_CHANGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_RETRY_INIT_SIGNAL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_SPEAK_TEXT;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_START_INIT_VOICE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_STOP_MOVE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TASK_END;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TASK_STATUS_FINISHED;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TTS;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TTS_CANCEL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TTS_SHOW_DIALOG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TTS_TASK_END;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_UPDATE_INSTARUCTION_STATUS;
import static com.fitgreat.airfacerobot.constants.RobotConfig.NETWORK_CONNECTION_CHECK_FAILURE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.NETWORK_CONNECTION_CHECK_SUCCESS;
import static com.fitgreat.airfacerobot.constants.RobotConfig.ROS_CONNECTION_CHECK_FAILURE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.ROS_MSG_BATTERY;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PLAY_TASK_PROMPT_INFO;
import static com.fitgreat.airfacerobot.constants.RobotConfig.RECHARGE_SPECIFIC_WORKFLOW;
import static com.fitgreat.airfacerobot.constants.RobotConfig.ROBOT_STOP_MOVE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.ROS_MSG_ROBOT_POSITION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.START_DDS_WAKE_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.START_INTRODUCTION_WORK_FLOW_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.UPDATE_ROBOT_STATUS_TO_SERVER;
import static com.fitgreat.airfacerobot.remotesignal.SignalConfig.MSG_CLOSE_ANDROID_SHARE;
import static com.fitgreat.airfacerobot.remotesignal.SignalConfig.MSG_FILE_CAN_NOT_PLAY;
import static com.fitgreat.airfacerobot.remotesignal.SignalConfig.MSG_RECEIVER_TASK_SUCCESS;
import static com.fitgreat.airfacerobot.remotesignal.SignalConfig.MSG_STOP_TASK;
import static com.fitgreat.airfacerobot.remotesignal.SignalConfig.OPERATION_TYPE_4_MOVE;
import static com.fitgreat.airfacerobot.remotesignal.SignalConfig.OPERATION_TYPE_8_MOVE;
import static com.fitgreat.airfacerobot.remotesignal.SignalConfig.OPERATION_TYPE_AUTO_MOVE;
import static com.fitgreat.airfacerobot.remotesignal.SignalConfig.OPERATION_TYPE_LIFT_VERTICAL;
import static com.fitgreat.airfacerobot.videocall.VideoCallConstant.MSG_MAIN_CLOSE;


/**
 * 机器人大脑服务，处理其他各个模块业务<p>
 *
 * @author zixuefei
 * @since 2020/3/23 0023 11:42
 */
public class RobotBrainService extends Service {
    private final String TAG = this.getClass().getSimpleName();
    private final static int NET_DELAY_INIT = 1008;
    private SignalManager signalManager;
    private HeaderActuatorManager headerActuatorManager;
    private ConnectivityManager connectivityManager;
    private PlayTtsTask playTtsTask;
    private String instructionId;
    private String instructionType;
    //导航位置中文名字
    private String instructionName;
    //导航位置英文名字
    private String instructionEnName;
    private String container;
    private String operationType;
    private String produceId;
    private String fileUrl, enFileUrl;
    private String instruction_status;
    private String targetUser;
    private String connectionId;
    private boolean isTerminal = false;
    private boolean taskVideoCall = false;
    private String string_hello;
    private Future speakTipsFuture;
    private JRos jRos;
    private SpeechManager speechManager = null;
    //定时获取机器人电量
    public static final String TIME_POWER = "time.detect.power";
    //定时任务一秒一次
    public static final String INTERVAL_SECOND_TASK = "interval.second.task";
    public int TIME_INTERVAL = 1000 * 60 * 10;//间隔10分钟获取一次电量
    public int TIMER_INTERVAL_SECOND = 1000;//定时器1秒一次
    private static final int MSG_SPEAK_POSITION = 10101;
    private static final int MSG_JROS_TIMEOUT = 10102;
    //导航失败,重试标志(默认导航任务失败后,再次发起导航任务,连续重试三次)
    private boolean navigation_failed_retry_tag = true;
    //终止自动回充提示标志
    private boolean stop_charging_no_tip = true;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntentPower;
    private PendingIntent pendingIntentEmergency;
    private String currentNavigationDestination;
    private String nextNavigationDestination;
    private int countdownTip = 0;
    private BroadcastReceiver timePowerReceiver;
    //当前导航任务信息
    private String currentNavigationX = null;
    private String currentNavigationY = null;
    private String currentNavigationZ = null;
    //导航失败重试次数
    private int navigationTimes = 0;

    private Timer batteryTimer = null;
    private TimerTask batteryTimerTask = null;
    private String currentLanguage;
    private RosManager rosManager;
    private MyTipDialog navigationIngDialog;
    private MyTipDialog tipRechargingDialog;
    private boolean rechargingStatus;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NET_DELAY_INIT:
                    //网络稳定开始从signal服务初始化
                    if (signalManager != null && !signalManager.isJoinSuccess()) {
                        EventBus.getDefault().post(new InitEvent(RobotConfig.TYPE_CHECK_STATE, RobotConfig.INIT_TYPE_SIGNAL_PROGRESS, "10"));
                        signalManager.getRobotToken();

                        InitEvent voiceevent = new InitEvent();
                        voiceevent.setType(MSG_START_INIT_VOICE);
                        EventBus.getDefault().post(voiceevent);
                    }
                    break;
                case MSG_SPEAK_POSITION:
                    break;
                case MSG_JROS_TIMEOUT:
                    InitUiEvent event = new InitUiEvent(RobotConfig.CANT_GET_BATTERY, "");
                    EventBus.getDefault().post(event);
                    break;
                default:
                    break;
            }
        }
    };
    private boolean freeOperationStartTag;
    private DuiMessageObserver mMessageObserver=new DuiMessageObserver();;
    private DuiCommandObserver mCommandObserver=new DuiCommandObserver();
    private DuiUpdateObserver mUpdateObserver=new DuiUpdateObserver();


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "-----RobotBrainService onCreate--------");
        EventBus.getDefault().register(this);
        createFunctionManager();
        //启动定时器
        timeDetectPower();
        //当前设备语言
        currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "zh");
    }

    /**
     * 创建各业务功能模块管理类
     */
    private void createFunctionManager() {
        signalManager = new SignalManager(this);
        headerActuatorManager = new HeaderActuatorManager();
        jRos = JRos.getInstance();
        speechManager = SpeechManager.instance(this);

        //注册网络监听器
        checkNetWorkState();
        playTtsTask = new PlayTtsTask(speechManager);
        rosManager = new RosManager();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "-----RobotBrainService onDestroy--------");
        EventBus.getDefault().unregister(this);
        if (signalManager != null) {
            signalManager.destroy();
            signalManager = null;
        }
        if (headerActuatorManager != null) {
            headerActuatorManager.reset();
            headerActuatorManager = null;
        }
        if (jRos != null) {
            jRos.release();
            rosManager.destroy();
        }
        if (connectivityManager != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
            connectivityManager = null;
        }
        //解绑定时器广播
        unregisterReceiver(timePowerReceiver);
    }

    /**
     * 语音指令终止当前任务
     */
    private void voiceTerminationTask() {
        String actionIdValue = SpUtils.getString(MyApp.getContext(), GUIDE_WORK_FLOW_ACTION_ID, null);
        LogUtils.d(TAG, "语音指令终止当前任务:actionIdValue===>" + actionIdValue);
        if (actionIdValue != null) {
//            BusinessRequest.stopOperateTask(actionIdValue, "3", stopTaskCallback);
        }
    }

    /**
     * 间隔10分钟定时获取机器人电量,间隔10秒获取急停按钮的状态
     */
    private void timeDetectPower() {
        //注册获取电量,更新急停按钮状态的广播接收者
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TIME_POWER);
        intentFilter.addAction(INTERVAL_SECOND_TASK);
        timePowerReceiver = new TimePowerReceiver();
        registerReceiver(timePowerReceiver, intentFilter);
        //触发定时任务
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //定时获取电量
            Intent powerIntent = new Intent();
            powerIntent.setAction(TIME_POWER);
            pendingIntentPower = PendingIntent.getBroadcast(this, 0, powerIntent, 0);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntentPower);
            //定时更新
            Intent emergencyIntent = new Intent();
            emergencyIntent.setAction(INTERVAL_SECOND_TASK);
            pendingIntentEmergency = PendingIntent.getBroadcast(this, 0, emergencyIntent, 0);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntentEmergency);
        }
    }

    private int checkBatteryTime = 0;
    private boolean isFirstTipLowPowerTag = true;

    /**
     * 定时器广播
     */
    class TimePowerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //ros初始化状态
            boolean rosInitTagState = SpUtils.getBoolean(getContext(), INIT_ROS_KEY_TAG, false);
            //定时任务1秒执行一次
            if (action.equals(INTERVAL_SECOND_TASK)) {
                if (rosInitTagState) {
                    //机器人紧急按钮状态更新
                    if (jRos.getRobotState().isUrgent) {
                        //机器人急停按钮是否按下标志,此时已经按下
                        SpUtils.putBoolean(MyApp.getContext(), CLICK_EMERGENCY_TAG, true);
                    } else {
                        //机器人急停按钮是否按下标志,此时没被按下
                        SpUtils.putBoolean(MyApp.getContext(), CLICK_EMERGENCY_TAG, false);
                    }
                    //电量小于20%弹窗提示
                    int batteryInt = Integer.parseInt(getBattery());
                    if (batteryInt != 0 && batteryInt < 20) {
                        if (isFirstTipLowPowerTag && (!RobotInfoUtils.getRobotRunningStatus().equals("5"))) {
                            isFirstTipLowPowerTag = false;
                            EventBus.getDefault().post(new InitEvent(RobotConfig.PROMPT_ROBOT_RECHARGE, ""));
                        } else if (!RobotInfoUtils.getRobotRunningStatus().equals("5")) {
                            if (checkBatteryTime == 900) {
                                checkBatteryTime = 0;
                                EventBus.getDefault().post(new InitEvent(RobotConfig.PROMPT_ROBOT_RECHARGE, ""));
                            }
                        }
                        checkBatteryTime++;
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + TIMER_INTERVAL_SECOND, pendingIntentEmergency);
                }
            }
        }
    }

    /**
     * 文件播放
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(FilePlayEvent fileEvent) {
        LogUtils.d(TAG, "FilePlayEvent: " + fileEvent.type);
        switch (fileEvent.type) {
            case FILE_PLAY_OK:
                ExecutorManager.getInstance().cancelScheduledTask(speakTipsFuture);
                String fileType = fileEvent.getAction();
                LogUtils.d(TAG, "filetype = " + fileType);
                //点击宣教播放按钮后切换机器人为  操作中状态
                saveRobotstatus(3);
                //开始播报txt,播放视频宣教,pdf前,移除当前播报提示
                speechManager.cancelTtsPlay();
                if (fileType.equals("2")) {
                    Intent videointent = new Intent(RobotBrainService.this, VideoPlayActivity.class);
                    videointent.putExtra("container", container);
                    videointent.putExtra("blob", fileUrl);
                    videointent.putExtra("instructionId", instructionId);
                    videointent.putExtra("status", instruction_status);
                    videointent.putExtra("instructionName", instructionName);
                    videointent.putExtra("F_Type", instructionType);
                    videointent.putExtra("operationType", operationType);
                    videointent.putExtra("operationProcedureId", produceId);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(videointent);
                        }
                    }, 2000);
                } else if (fileType.equals("3")) {
                    Intent pdfintent = new Intent(RobotBrainService.this, PdfPlayActivity.class);
                    pdfintent.putExtra("container", container);
                    pdfintent.putExtra("blob", fileUrl);
                    pdfintent.putExtra("instructionId", instructionId);
                    pdfintent.putExtra("instructionName", instructionName);
                    pdfintent.putExtra("status", instruction_status);
                    pdfintent.putExtra("F_Type", instructionType);
                    pdfintent.putExtra("operationType", operationType);
                    pdfintent.putExtra("operationProcedureId", produceId);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(pdfintent);
                        }
                    }, 2000);
                } else if (fileType.equals("4")) {
                    LogUtils.d(TAG, "filetype.equals(\"4\")!!!!!!!!!!!!!!!!!");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (playTtsTask != null) {
                                playTtsTask.setOnPlayDoneListener((String resultCode) -> {
                                    instruction_status = resultCode;
                                });
                                playTtsTask.exePlayTtsTask(container, fileUrl, instructionId, updateInstructionCallback);
                            }
                        }
                    }, 2000);
                }
                break;
            case RobotConfig.FILE_PLAY_CANCEL:
                ExecutorManager.getInstance().cancelScheduledTask(speakTipsFuture);
                LogUtils.d(TAG, "RobotConfig.FILE_PLAY_OK !!!!!! ");
                instruction_status = "4";
                BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                break;
        }
    }

    MoveStateObserver moveStateObserver = new MoveStateObserver() {
        @Override
        public void onMessage(String s) {
            LogUtils.d(TAG, "s ==== " + s);
            switch (s) {
                case "navigation_started"://开始导航
                    break;
                case "navigation_cancelled":
                    LogUtils.d(TAG, "任务被终止，导航停止！");
                    break;
                case "navigation_stopped":      //导航失败  TODO
                    LogUtils.d(DEFAULT_LOG_TAG, "isTerminal = " + isTerminal + " 本次导航失败导航任务信息--" + currentNavigationDestination + "---" + currentNavigationX + "---" + currentNavigationY + "---" + currentNavigationZ);
                    if (!isTerminal) {
                        handler.postDelayed(() -> {
                            playShowContent(appendStartNavigationPrompt(MvpBaseActivity.getActivityContext().getString(R.string.navigation_failed_tip_one), MvpBaseActivity.getActivityContext().getString(R.string.navigation_failed_tip_two)));
                        }, 500);
                    }
                    //本次导航任务失败,重试三次
                    if (navigationTimes < 3 && navigation_failed_retry_tag) {
                        navigationTimes++;
                        handler.postDelayed(() -> {
                            playShowContent(appendStartNavigationPrompt(MvpBaseActivity.getActivityContext().getString(R.string.start_navigation_tip_one), MvpBaseActivity.getActivityContext().getString(R.string.start_navigation_tip_two)));
                        }, 2000);
                        ExecutorManager.getInstance().executeTask(() -> {
                            jRos.op_setAutoMove((byte) 1, Double.valueOf(currentNavigationX), Double.valueOf(currentNavigationY), Double.valueOf(currentNavigationZ));
                            instruction_status = "1";
                            BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                        });
                    } else {
                        //单点导航连续三次失败
                        navigationTimes = 0;
                        //清空本次导航任务地点xyz信息
                        currentNavigationX = null;
                        currentNavigationY = null;
                        currentNavigationZ = null;
                        //当前机器人自动回充工作流启动状态
                        boolean startRechargeTag = SpUtils.getBoolean(getContext(), AUTOMATIC_RECHARGE_TAG, false);
                        LogUtils.d(DEFAULT_LOG_TAG, "导航失败后,自动回充工作流状态状态:   " + startRechargeTag);
                        if (startRechargeTag) { //自动回充工作流启动时
                            //关闭自动回充提示弹窗
                            if (tipRechargingDialog != null) {
                                tipRechargingDialog.dismiss();
                                tipRechargingDialog = null;
                            }
                            if (navigation_failed_retry_tag) {
                                //最终不能导航移动连续两次语音提示  "护士姐姐请帮帮我带我去充电"
                                handler.postDelayed(() -> {
                                    EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, MvpBaseActivity.getActivityContext().getString(R.string.take_charge_tip)));
                                }, 1000);
                                handler.postDelayed(() -> {
                                    EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, MvpBaseActivity.getActivityContext().getString(R.string.take_charge_tip)));
                                }, 2000);
                            }
                        } else {
                            //关闭导航提示弹窗
                            if (navigationIngDialog != null) {
                                navigationIngDialog.dismiss();
                                navigationIngDialog = null;
                            }
                            handler.postDelayed(() -> {
                                EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, MvpBaseActivity.getActivityContext().getString(R.string.prompt_task_exception)));
                            }, 1000);
                        }
                    }
                    break;
                case "navigation_arrived":  //导航成功  TODO
                    //关闭导航中提示弹窗
                    if (navigationIngDialog != null) {
                        navigationIngDialog.dismiss();
                        navigationIngDialog = null;
                    }
                    //清空本次导航任务地点xyz信息
                    currentNavigationX = null;
                    currentNavigationY = null;
                    currentNavigationZ = null;
                    //导航失败重试次数
                    navigationTimes = 0;
                    //更新导航任务状态
                    instruction_status = "2";
                    BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                    //自动回充工作流启动后,导航到目的地后不弹窗提示
                    rechargingStatus = SpUtils.getBoolean(getContext(), AUTOMATIC_RECHARGE_TAG, false);
                    //空闲操作启动状态
                    freeOperationStartTag = SpUtils.getBoolean(MyApp.getContext(), FREE_OPERATION_STATE_TAG, false);
                    //导航成功到达目的地,语音播报提示
                    StringBuffer startNavigationPrompt = new StringBuffer();
                    startNavigationPrompt.append(MvpBaseActivity.getActivityContext().getString(R.string.navigation_success_tip_one));
                    if (currentLanguage != null && currentLanguage.equals("zh")) {
                        startNavigationPrompt.append(instructionName);
                    } else {
                        startNavigationPrompt.append(instructionEnName);
                    }
                    LogUtils.d(DEFAULT_LOG_TAG, "导航成功,自动回充工作流启动状态  " + rechargingStatus + "  目的地名字, " + instructionName + "  " + instructionEnName);
                    //自动回充工作流没有启动时,空闲操作工作流没有启动时,导航成功到达目的地时弹窗提示
                    if (!rechargingStatus && !freeOperationStartTag) {
                        Intent intent = new Intent(MyApp.getContext(), YesOrNoDialogActivity.class);
                        intent.putExtra(DIALOG_TITLE, MvpBaseActivity.getActivityContext().getString(R.string.start_chose_destination_dialog_title));
                        intent.putExtra(DIALOG_CONTENT, appendStartNavigationPrompt(MvpBaseActivity.getActivityContext().getString(R.string.navigation_success_tip_one), MvpBaseActivity.getActivityContext().getString(R.string.navigation_success_tip_two)));
                        intent.putExtra(DIALOG_YES, MvpBaseActivity.getActivityContext().getString(R.string.need_yes_title));
                        intent.putExtra(DIALOG_NO, MvpBaseActivity.getActivityContext().getString(R.string.need_no_title));
                        intent.putExtra("instructionName", instructionName);
                        intent.putExtra("instructionEnName", instructionEnName);
                        startActivity(intent);
                    }
                    break;
                case "parking_success":   //充电成功
                    handler.postDelayed(() -> {
                        playShowContent(MvpBaseActivity.getActivityContext().getString(R.string.charge_success_tip));
                    }, 1000);
                    saveRobotstatus(5);
                    LogUtils.d(DEFAULT_LOG_TAG, "冲电成功:为冲电状态" + "robot status = " + RobotInfoUtils.getRobotRunningStatus());
                    BusinessRequest.updateRobotState(getBattery(), RobotInfoUtils.getRobotRunningStatus(), new HttpCallback() {
                        @Override
                        public void onResult(BaseResponse baseResponse) {
                            LogUtils.json("parking_success", "更新机器人信息成功: " + JSON.toJSONString(baseResponse));
                        }
                    });
                    instruction_status = "2";
                    BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                    //自动回充工作流结束
                    SpUtils.putBoolean(MyApp.getContext(), AUTOMATIC_RECHARGE_TAG, false);
                    break;
                case "parking_timeout": //充电失败  TODO
                    LogUtils.d(DEFAULT_LOG_TAG, "parking_timeout !!!!!!!!!!!!!!!!!!");
                    //语音提示冲电失败
                    handler.postDelayed(() -> {
                        playShowContent(MvpBaseActivity.getActivityContext().getString(R.string.charge_failed_tip));
                    }, 1000);
                    //语音提示护士姐姐请帮帮我,带我去充电
                    handler.postDelayed(() -> {
                        playShowContent(MvpBaseActivity.getActivityContext().getString(R.string.take_charge_tip));
                    }, 3500);
                    handler.postDelayed(() -> {
                        playShowContent(MvpBaseActivity.getActivityContext().getString(R.string.take_charge_tip));
                    }, 4500);
                    //冲电失败,关闭自动回充提示弹窗
                    if (tipRechargingDialog != null) {
                        tipRechargingDialog.dismiss();
                        tipRechargingDialog = null;
                    }
                    instruction_status = "-1";
                    BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                    //自动回充工作流结束
                    SpUtils.putBoolean(MyApp.getContext(), AUTOMATIC_RECHARGE_TAG, false);
                    break;
            }
        }
    };


    /**
     * 接受初始化UI进度反馈，开始下一个模块初始化
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(InitUiEvent initUiEvent) {
        LogUtils.d(TAG, "InitUiEvent:" + initUiEvent.type);
        switch (initUiEvent.type) {
            case RobotConfig.INIT_TYPE_ROS:
                if (headerActuatorManager != null) {
                    headerActuatorManager.setInitListener((boolean isSuccess) -> {
                        if (isSuccess) {
                            if (jRos != null) {
                                jrosInit();
                            }
                            if (rosManager != null) {
                                rosManager.initNode();
                            }
                        }
                    });
                    headerActuatorManager.initActuator();
                } else {
                    LogUtils.e(TAG, "-----header actuator manager is null-----");
                }
                break;
            case RobotConfig.INIT_TYPE_VOICE:
                ExecutorManager.getInstance().executeTask(() -> {
                    switch (initUiEvent.action) {
                        case "start":
                            if (speechManager != null) {
                                speechManager.initialization();
                            }
                            break;
                        case "stop":
                            //dds释放
                            speechManager.restoreToDo();
//                            // 消息监听器
//                            mMessageObserver = new DuiMessageObserver();
//                            // 命令监听器
//                            mCommandObserver = new DuiCommandObserver();
//                            //更新监听器
//                            mUpdateObserver = new DuiUpdateObserver();
                            try {
                                mCommandObserver.unregist();
                                mMessageObserver.unregist();
                                mUpdateObserver.unregist();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            break;
                    }
                });
                break;
            case RobotConfig.CANT_GET_BATTERY:
                ShellCmdUtils.execCmd(ShellCmdUtils.COMMAND_AIRPLANE_ON);
                handler.postDelayed(() -> {
                    LogUtils.d(TAG, " CANT_GET_BATTERY , 打开飞行模式 2秒后关闭");
                    ShellCmdUtils.execCmd(ShellCmdUtils.COMMAND_AIRPLANE_OFF);
                }, 2000);
                break;
            default:
                break;
        }
    }

    /**
     * dds操作汇总
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(ActionDdsEvent actionDdsEvent) {
        switch (actionDdsEvent.getmActionKind()) {
            case START_DDS_WAKE_TAG: //启动唤醒,打开one shot模式
                SpeechManager.startOneShotWakeup();
                break;
            case CLOSE_DDS_WAKE_TAG: //关闭唤醒,关闭one shot模式
                SpeechManager.closeOneShotWakeup();
                break;
            case PLAY_TASK_PROMPT_INFO:
//                LogUtils.d("CommandTodo", actionDdsEvent.getmActionContent());
                speechManager.textTtsPlay(actionDdsEvent.getmActionContent(), String.valueOf(actionDdsEvent.getmActionContent().length()), new SpeechManager.TtsBroadcastListener() {
                    @Override
                    public void ttsBroadcastBegin() {
                        EventBus.getDefault().post(new InitEvent(RobotConfig.START_SPEAK_ANIMATION_MSG, ""));
                    }

                    @Override
                    public void ttsBroadcastEnd(String ttsId) {
                        if (ttsId.equals(String.valueOf(actionDdsEvent.getmActionContent().length()))) {
                            EventBus.getDefault().post(new InitEvent(RobotConfig.START_BLINK_ANIMATION_MSG, ""));
                        }
                    }
                });
                break;
            case DDS_OBSERVER_REGISTERED://dds初始化成功 Observer注册
                LogUtils.d("CommandTodo", "Observer注册");
//                // 消息监听器
//                mMessageObserver = new DuiMessageObserver();
//                // 命令监听器
//                mCommandObserver = new DuiCommandObserver();
//                //更新监听器
//                mUpdateObserver = new DuiUpdateObserver();
                try {
                    mCommandObserver.regist();
                    mMessageObserver.regist();
                    mUpdateObserver.regist();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DDS_VOICE_TEXT_CANCEL://DDS语音播报取消
                speechManager.cancelTtsPlay();
                break;
            case DDS_STOP_DIALOG://DDS关闭对话
                speechManager.stopDialog();
                break;
        }
    }

    private void jrosInit() {
        jRos.init((new JRosConfig()).addConfig("SOCKET_URI", "ws://192.168.88.8:9090"), new JRosInitListener() {
            public void onInitComplete() {
                LogUtils.d(DEFAULT_LOG_TAG, "jRos onInitComplete ! ");
                jRos.subscribe(new String[]{"navigation_started", "navigation_stopped", "navigation_arrived", "parking_success", "parking_timeout"}, moveStateObserver);
                RobotInfoUtils.setHardwareVersion(jRos.op_GetHardwareVersion());
                jRos.op_switchControl((byte) 0);
                if (handler.hasMessages(MSG_JROS_TIMEOUT)) {
                    handler.removeMessages(MSG_JROS_TIMEOUT);
                }
                if (jRos.getRobotState().isCharge) {
                    RobotInfoUtils.setRobotRunningStatus("5");
                } else {
                    RobotInfoUtils.setRobotRunningStatus("1");
                }
                //连续获取电量更新电量状态显示
                batteryTimer = new Timer();
                batteryTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            RobotSignalEvent batteryEvent = new RobotSignalEvent(ROS_MSG_BATTERY, "");
                            batteryEvent.setBattery(jRos.getRobotState().percentage);
                            batteryEvent.setPowerTechnology(jRos.getRobotState().isUrgent);
                            batteryEvent.setPowerHealth(jRos.getRobotState().isLocked);
                            batteryEvent.setConnect(jRos.IsConnected());
                            batteryEvent.setPowerStatus(jRos.getRobotState().isCharge);
                            EventBus.getDefault().post(batteryEvent);
//                            LogUtils.json(DEFAULT_LOG_TAG, JSON.toJSONString(batteryEvent));
                            //保存机器人工作模式到本地
                            if (jRos.getRobotState().isLocked) {
                                SpUtils.putBoolean(MyApp.getContext(), IS_CONTROL_MODEL, true);
                            } else {
                                SpUtils.putBoolean(MyApp.getContext(), IS_CONTROL_MODEL, false);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            LogUtils.e(DEFAULT_LOG_TAG, "连续获取机器人电量异常:  " + e.getMessage());
                        }


                        //电量小于20%弹窗提示
//                        String batteryString = String.valueOf(jRos.getRobotState().percentage);
//                        int batteryInt = Integer.parseInt(batteryString);
//                        if (batteryInt != 0 && batteryInt < 20) {
////                        LogUtils.d(DEFAULT_LOG_TAG, "  当前电量过低弹窗提示冲电,  " + batteryInt + "  时间次数,  " + checkBatteryTime + " ,机器人状态, " + RobotInfoUtils.getRobotRunningStatus());
//                            if (!RobotInfoUtils.getRobotRunningStatus().equals("5")) {
//                                if (checkBatteryTime == 900) {
//                                    checkBatteryTime = 0;
//                                    EventBus.getDefault().post(new InitEvent(RobotConfig.PROMPT_ROBOT_RECHARGE, ""));
//                                }
//                            }
//                            checkBatteryTime++;
//                        }
                    }
                };
                batteryTimer.scheduleAtFixedRate(batteryTimerTask, 0, 1000);
                //ros初始化成功
                SpUtils.putBoolean(MyApp.getContext(), INIT_ROS_KEY_TAG, true);
            }

            public void onError(String msg) {
                //ros初始化错误
                Intent intent = new Intent();
                intent.setAction(ROS_CONNECTION_CHECK_FAILURE);
                sendBroadcast(intent);
                LogUtils.d(DEFAULT_LOG_TAG, "main onError msg:" + msg);
                //ros初始化报错
                SpUtils.putBoolean(MyApp.getContext(), INIT_ROS_KEY_TAG, false);
            }

            @Override
            public void onDisconnect(String s) {
                //ros连接断开
                Intent intent = new Intent();
                intent.setAction(ROS_CONNECTION_CHECK_FAILURE);
                sendBroadcast(intent);
                LogUtils.d(DEFAULT_LOG_TAG, "JRos onDisconnect:" + s);
                //ros断开
                OperationUtils.saveSpecialLog("RobotDisconnected", "机器人断开连接");
                SpUtils.putBoolean(MyApp.getContext(), INIT_ROS_KEY_TAG, false);
            }

            @Override
            public void onUpdate(int i) {
//                LogUtils.d(DEFAULT_LOG_TAG, "JRos onUpdate :" + i);
                if (i == 10) {
//                    handler.postDelayed(() -> {
//                        setAirPlaneMode(true);
//                        handler.postDelayed(() -> {
//                            setAirPlaneMode(false);
//                        }, 1000 * 4);
//                    }, 1000 * 15);
                }
                EventBus.getDefault().post(new InitEvent(RobotConfig.TYPE_CHECK_STATE, RobotConfig.INIT_TYPE_ROS_PROGRESS, String.valueOf(i)));   //广播JROS初始化进度
            }
        });
    }

    private void setAirPlaneMode(boolean enable) {
        int mode = enable ? 1 : 0;
        String cmd = "settings put global airplane_mode_on " + mode;
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接受视频的消息反馈
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(VideoMessageEve eve) {
        LogUtils.d(TAG, "InitUiEvent:" + eve.type);
        switch (eve.type) {
            case MSG_MAIN_CLOSE:
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("Type", "Video");
                    jsonObject.put("Result", "MainClose");
                    LogUtils.d(TAG, "json = " + jsonObject.toString());
                    signalManager.invokeToAllSendMessage(jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    public String getBattery() {
        String battery = "";
        if (jRos != null) {
            battery = String.valueOf(jRos.getRobotState().percentage);
        }
        return battery;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(InitEvent initEvent) {
        LogUtils.d(TAG, "initEvent:" + JsonUtils.encode(initEvent));
        switch (initEvent.type) {
            case RobotConfig.TYPE_CHECK_STATE_DONE:
                SpUtils.putBoolean(getContext(), "isVideocall", false);
                ExecutorManager.getInstance().executeScheduledTask(() -> {
                    SignalDataEvent batteryEvent = new SignalDataEvent();
                    batteryEvent.setType(UPDATE_ROBOT_STATUS_TO_SERVER);
                    EventBus.getDefault().post(batteryEvent);
                    LogUtils.d(TAG, "TYPE_CHECK_STATE_DONE : " + RobotInfoUtils.getRobotRunningStatus());
                }, 0, 3, TimeUnit.SECONDS);
                break;
            case MSG_GETROS_VERSION:
                LogUtils.d(TAG, "MSG_GETROS_VERSION !!!!!!!");
                ExecutorManager.getInstance().executeTask(new Runnable() {
                    @Override
                    public void run() {
                        RobotInfoUtils.setHardwareVersion(jRos.op_GetHardwareVersion());
                    }
                });
                break;
            case MSG_RETRY_INIT_SIGNAL:
                signalManager.getRobotToken();
                break;
            case RobotConfig.UPDATE_ROBOT_STATE_TAG: //更新机器人信息
//                LogUtils.d(DEFAULT_LOG_TAG, " UPDATE_ROBOT_STATE_TAG 机器人状态 : " + initEvent.getAction() + " : Battery : " + getBattery());
                BusinessRequest.updateRobotState(getBattery(), initEvent.getAction(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                    }
                });
                break;
        }
    }

    /**
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(SpeakEvent event) {
        switch (event.type) {
            case RobotConfig.MSG_TTS:
                if (speakTipsFuture != null) {
                    ExecutorManager.getInstance().cancelScheduledTask(speakTipsFuture);
                    speakTipsFuture = null;
                }
                speakTipsFuture = ExecutorManager.getInstance().executeScheduledTask(new Runnable() {
                    @Override
                    public void run() {
                        EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, event.getText()));
                    }
                }, 0, SpUtils.getInt(RobotBrainService.this, "de_time", 10), TimeUnit.SECONDS);
                break;
            case MSG_TTS_CANCEL:
                LogUtils.d(TAG, "MSG_TTS_CANCEL !!!!!!!!!");
                ExecutorManager.getInstance().cancelScheduledTask(speakTipsFuture);
                if (event.getAction() != null && event.getAction().equals("txt_finished")) {
                    instruction_status = "2";
                    BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                }
                break;
            case MSG_TTS_TASK_END:
                SpeakEvent speakEvent = new SpeakEvent();
                speakEvent.setType(MSG_TTS);
                speakEvent.setText(instructionName + "播放结束,请点击屏幕上的\"确认\"按钮");
                EventBus.getDefault().post(speakEvent);

                SpeakEvent speakEvent1 = new SpeakEvent();
                speakEvent1.setType(MSG_TTS_SHOW_DIALOG);
                speakEvent1.setText(instructionName);
                EventBus.getDefault().post(speakEvent1);
                //显示播报信息到首页记录
                EventBus.getDefault().post(new NavigationTip(instructionName + "播放结束,请点击屏幕上的\"确认\"按钮"));
                break;
            case MSG_SPEAK_TEXT:
                LogUtils.d(TAG, "MSG_SPEAK_TEXT:  " + event.getText());
                speechManager.textTtsPlay(event.getText(), "0", null);
                //显示播报信息到首页记录
                EventBus.getDefault().post(new NavigationTip(event.getText()));
                break;
        }
    }


    /**
     * 接受远程信号转发到各个模块处理业务
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(SignalDataEvent signalDataEvent) {
        LogUtils.d(TAG, "type:" + signalDataEvent.type);
        switch (signalDataEvent.type) {
            case RobotConfig.CONTROL_TYPE_HEADER_ACTUATOR:
                if (headerActuatorManager == null) {
                    return;
                }
                headerActuatorManager.sendAnglePacket(Integer.parseInt(signalDataEvent.action), ActuatorConstant.MOTOR_VERTICAL);
                break;
            //定时更新服务端机器人信息
            case UPDATE_ROBOT_STATUS_TO_SERVER:
                LogUtils.d(TAG, "robotstatus = " + RobotInfoUtils.getRobotRunningStatus());
                BusinessRequest.updateRobotState(getBattery(), RobotInfoUtils.getRobotRunningStatus(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String result = response.body().string();
//                        LogUtils.d(TAG, "updateRobotState result:" + result);
                    }
                });
                break;
            case OPERATION_TYPE_4_MOVE:
                LogUtils.d(TAG, "OPERATION_TYPE_4_MOVE !!!");
                try {
                    JSONObject allow4 = allowMove();
                    if (allow4 != null) {
                        signalManager.invokeToAllSendMessage(allow4.toString());
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String direction = signalDataEvent.getDirection();
                switch (direction) {
                    case "Up":
                        jRos.op_setVelControl(0.4f, 0);
                        break;
                    case "Down":
                        jRos.op_setVelControl(-0.1f, 0);
                        break;
                    case "Left":
                        jRos.op_setVelControl(0, 0.35f);
                        break;
                    case "Right":
                        jRos.op_setVelControl(0, -0.35f);
                        break;
                    case "Stop":
                        jRos.op_setVelControl(0.0f, 0.0f);
                        break;
                }

                break;
            case OPERATION_TYPE_8_MOVE:
                LogUtils.d(TAG, "OPERATION_TYPE_8_MOVE !!!");
                try {
                    JSONObject allow8 = allowMove();
                    if (allow8 != null) {
                        signalManager.invokeToAllSendMessage(allow8.toString());
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jRos.op_setVelControl(Float.valueOf(signalDataEvent.getX()), Float.valueOf(signalDataEvent.getY()));
                break;
            case OPERATION_TYPE_LIFT_VERTICAL:
                LogUtils.d(TAG, "OPERATION_TYPE_LIFT_VERTICAL !!!");
                ExecutorManager.getInstance().executeTask(new Runnable() {
                    @Override
                    public void run() {
                        String operate = signalDataEvent.getVertical();
                        byte mode = 0;
                        switch (operate) {
                            case "Up":
                                mode = 1;
                                break;
                            case "Down":
                                mode = 2;
                                break;
                            case "Stop":
                                mode = 0;
                                break;
                        }
                        jRos.op_setLift(mode);
                    }
                });

                break;
            case SignalConfig.MSG_GET_ROBOT_STATUS:
                try {
                    getRobotStatus(signalDataEvent.getAction());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case OPERATION_TYPE_AUTO_MOVE:   //  TODO 开始导航任务
                instructionType = signalDataEvent.getInstructionType();
                instructionName = signalDataEvent.getF_InstructionName();
                instructionEnName = signalDataEvent.getF_InstructionEnName();
                container = signalDataEvent.getContainer();
                operationType = signalDataEvent.getOperationType();
                produceId = signalDataEvent.getProduceId();
                fileUrl = signalDataEvent.getFileUrl();
                targetUser = signalDataEvent.getTargetUser();
                connectionId = signalDataEvent.getConnectionId();
                instructionId = signalDataEvent.getInstructionId();
                //记录当前导航目的地信息
                currentNavigationDestination = instructionName;
                currentNavigationX = signalDataEvent.getX();
                currentNavigationY = signalDataEvent.getY();
                currentNavigationZ = signalDataEvent.getE();
                //默认执行完当前任务后,查询并执行下一个任务
                isTerminal = false;
                LogUtils.d(DEFAULT_LOG_TAG, "开始导航任务");
                LogUtils.json(DEFAULT_LOG_TAG, JSON.toJSONString(signalDataEvent));
                //导航失败重试标志默认为true
                navigation_failed_retry_tag = true;
                rechargingStatus = SpUtils.getBoolean(getContext(), AUTOMATIC_RECHARGE_TAG, false);
                //首页是否有弹窗弹出
                SpUtils.putBoolean(MyApp.getContext(), MAIN_PAGE_DIALOG_SHOW_TAG, true);
                if (rechargingStatus) {
                    //自动回充时,任务提示弹窗,可以终止
                    rechargingDialog();
                } else {
                    //导航时,任务提示弹窗,可以终止
                    showNavigationIngDialog();
                }
                //开始导航任务提示语音
                handler.postDelayed(() -> {
                    //根据当前设备语言拼接提示语中英文版本
                    StringBuffer startNavigationPrompt = new StringBuffer();
                    startNavigationPrompt.append(MvpBaseActivity.getActivityContext().getString(R.string.start_navigation_tip_one));
                    if (currentLanguage != null && currentLanguage.equals("zh")) {
                        startNavigationPrompt.append(instructionName);
                    } else {
                        startNavigationPrompt.append(instructionEnName);
                    }
                    startNavigationPrompt.append(MvpBaseActivity.getActivityContext().getString(R.string.start_navigation_tip_two));
                    ToastUtils.showSmallToast(startNavigationPrompt.toString());
                    //语音提示
                    playShowContent(startNavigationPrompt.toString());
                }, 500);
                //发送导航指令到ros端
                ExecutorManager.getInstance().executeTask(() -> {
                    jRos.op_setAutoMove((byte) 1, Double.valueOf(signalDataEvent.getX()), Double.valueOf(signalDataEvent.getY()), Double.valueOf(signalDataEvent.getE()));
                    instruction_status = "1";
                    BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                });
                break;
            case MSG_UPDATE_INSTARUCTION_STATUS: //TODO 开始非导航操作任务
                instructionType = signalDataEvent.getInstructionType();
                instructionName = signalDataEvent.getF_InstructionName();
                instructionEnName = signalDataEvent.getF_InstructionEnName();
                container = signalDataEvent.getContainer();
                operationType = signalDataEvent.getOperationType();
                produceId = signalDataEvent.getProduceId();
                fileUrl = signalDataEvent.getFileUrl();
                enFileUrl = signalDataEvent.getEnFileUrl();
                targetUser = signalDataEvent.getTargetUser();
                connectionId = signalDataEvent.getConnectionId();
                instructionId = signalDataEvent.getInstructionId();
                instruction_status = "1";
                LogUtils.d(DEFAULT_LOG_TAG, "MSG_UPDATE_INSTARUCTION_STATUS !!!!!!!!!!!! instructionType = " + instructionType + " , operationType = " + operationType + " ,instructionId = " + instructionId);
                BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                break;
            case MSG_STOP_MOVE:
                ExecutorManager.getInstance().executeTask(() -> {
                    jRos.op_setAutoMove((byte) 0, 0, 0, 0);
                });
                break;
            case MSG_CANCEL_RECHARGE: //取消自动回充工作流
                ExecutorManager.getInstance().executeTask(() -> {
                    jRos.op_setAutoMove((byte) 0, 0, 0, 0);
                    jRos.op_runParking((byte) 0);
                });
                break;
            case ROBOT_STOP_MOVE:
                ExecutorManager.getInstance().executeTask(() -> jRos.op_setVelControl(0.0f, 0.0f));
                break;
            case MSG_STOP_TASK:
                isTerminal = true;
                LogUtils.d(DEFAULT_LOG_TAG, "--------MSG_STOP_TASK---------" + (speechManager != null));
                ExecutorManager.getInstance().cancelScheduledTask(speakTipsFuture);
                if (VideoPlayActivity.instance != null) {
                    VideoPlayActivity.instance.finishInstruction("3");
                }
                if (PdfPlayActivity.instance != null) {
                    PdfPlayActivity.instance.finishInstruction("3");
                }
                if (playTtsTask != null && playTtsTask.isRunning()) {
                    playTtsTask.stopTts();
                }
                jRos.op_runParking((byte) 0);
                if (speechManager != null) {
                    EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, MvpBaseActivity.getActivityContext().getString(R.string.task_terminated_title)));
                }
                //取消循环播放宣教提示语定时任务
                ExecutorManager.getInstance().cancelScheduledTask(speakTipsFuture);
                //更新提示信息到首页对话记录
                EventBus.getDefault().post(new NavigationTip(MvpBaseActivity.getActivityContext().getString(R.string.task_terminated_title)));
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("Type", "Tips");
                    jsonObject.put("Result", "zhoangzhirenwu");
                    signalManager.invokeToAllSendMessage(jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case MSG_TASK_END:
                try {
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("Type", "Tips");
                    jsonObject1.put("Result", "TaskEnd");
                    String endStr = jsonObject1.toString();
                    signalManager.invokeToAllSendMessage(endStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case MSG_CHANGE_POWER_LOCK:
                ExecutorManager.getInstance().executeTask(() -> jRos.op_setPowerlock((byte) signalDataEvent.getPowerlock()));
                break;
            case MSG_INSTRUCTION_STATUS_FINISHED:  //终止当前任务
                instruction_status = signalDataEvent.getAction();
                BusinessRequest.UpdateInstructionStatue(signalDataEvent.getInstructionId(), instruction_status, updateInstructionCallback);
                break;
            case MSG_TASK_STATUS_FINISHED:  //终止视频  txt pdf播放任务 TODO  不继续插下一步
                instruction_status = signalDataEvent.getAction();
                LogUtils.d(DEFAULT_LOG_TAG, "视频,pdf,text文本播放结束/终止 !!!!!!!!   instruction_status = " + instruction_status + ", InstructionId =" + signalDataEvent.getInstructionId());
                BusinessRequest.UpdateInstructionStatue(signalDataEvent.getInstructionId(), instruction_status, endUpdateInstructionCallback);
                break;
            case ROS_MSG_ROBOT_POSITION:
                String position_x = String.valueOf(signalDataEvent.getPosition_X());
                String position_y = String.valueOf(signalDataEvent.getPosition_Y());
                String position_z = String.valueOf(signalDataEvent.getPosition_Z());
                BusinessRequest.updateRobotPosition(position_x, position_y, position_z, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        LogUtils.e("RobotSignalEvent", e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        LogUtils.d("RobotSignalEvent", "response = " + response);
                    }
                });
                break;
            case MSG_FILE_CAN_NOT_PLAY:
                break;
            case MSG_RECEIVER_TASK_SUCCESS:
                LogUtils.d(TAG, "MSG_RECEIVER_TASK_SUCCESS!!!!!!!!!!!!!!");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("Type", "Tips");
                    jsonObject.put("Result", "taskSuccess");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                signalManager.invokeToAllSendMessage(jsonObject.toString());
                break;
            case RobotConfig.ROS_UPDATE_STATUS:
                ExecutorManager.getInstance().executeTask(() -> {
                    if (jRos != null && jRos.IsConnected()) {
                        switch (signalDataEvent.getOperationType()) {
                            case "transFile":
                                try {
                                    String fileName = "/home/radmin/HardwareVersion/" + signalDataEvent.getContainer();
                                    String filePath = signalDataEvent.getFileUrl();
                                    if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(filePath)) {
                                        LogUtils.e(TAG, "File is empty");
                                        return;
                                    }
                                    byte[] updateData = FileUtils.readLocalFile(filePath);
                                    if (updateData == null) {
                                        DaemonEvent daemonEvent = new DaemonEvent(RobotConfig.TYPE_SHOW_VERSION_STATUS,
                                                "rosUpdateFailed", "硬件升级失败，ros本地文件读取失败");
                                        EventBus.getDefault().post(daemonEvent);
                                        return;
                                    }
                                    String data = Base64.byteArrayToBase64(updateData);

                                    String cmd = signalDataEvent.action;
                                    String Step_id = signalDataEvent.getStep_id();
                                    String step = signalDataEvent.getRos_step();

                                    if (jRos.op_transFile(data, fileName, data.length())) {
                                        CommandResult cmdResult = jRos.op_ExecCmd(cmd);
                                        if (cmdResult.result) {
                                            DaemonEvent daemonEvent = new DaemonEvent(RobotConfig.TYPE_SHOW_VERSION_STATUS,
                                                    "rosUpdateSuccess", cmdResult.msg);
                                            daemonEvent.setStepId(Step_id);
                                            daemonEvent.setStep(step);
                                            EventBus.getDefault().post(daemonEvent);
                                        } else {
                                            DaemonEvent daemonEvent = new DaemonEvent(RobotConfig.TYPE_SHOW_VERSION_STATUS,
                                                    "rosUpdateFailed", "硬件升级失败，ros执行命令失败:" + cmdResult.msg);
                                            daemonEvent.setStepId(Step_id);
                                            EventBus.getDefault().post(daemonEvent);
                                        }
                                    } else {
                                        DaemonEvent daemonEvent = new DaemonEvent(RobotConfig.TYPE_SHOW_VERSION_STATUS,
                                                "rosUpdateFailed", "硬件升级失败，ros传输文件失败 !");
                                        EventBus.getDefault().post(daemonEvent);
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "command":
                                String cmd = signalDataEvent.action;
                                String Step_id = signalDataEvent.getStep_id();
                                String step = signalDataEvent.getRos_step();
                                CommandResult cmdResult = jRos.op_ExecCmd(cmd);
                                if (cmdResult.result) {
                                    DaemonEvent daemonEvent = new DaemonEvent(RobotConfig.TYPE_SHOW_VERSION_STATUS,
                                            "rosUpdateSuccess", cmdResult.msg);
                                    daemonEvent.setStepId(Step_id);
                                    daemonEvent.setStep(step);
                                    EventBus.getDefault().post(daemonEvent);
                                } else {
                                    DaemonEvent daemonEvent = new DaemonEvent(RobotConfig.TYPE_SHOW_VERSION_STATUS,
                                            "rosUpdateFailed", "硬件升级失败，ros执行命令失败:" + cmdResult.msg);
                                    daemonEvent.setStepId(Step_id);
                                    EventBus.getDefault().post(daemonEvent);
                                }
                                break;
                            default:
                                break;
                        }
                    } else {
                        DaemonEvent daemonEvent = new DaemonEvent(RobotConfig.TYPE_SHOW_VERSION_STATUS,
                                "rosUpdateFailed", "硬件升级失败，ros未连接");
                        EventBus.getDefault().post(daemonEvent);
                    }
                });
                break;
            case MSG_CLOSE_ANDROID_SHARE:
                JSONObject msgObj = new JSONObject();
                try {
                    msgObj.put("Type", "Tips");
                    msgObj.put("Result", "close_androidShare");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                signalManager.invokeToSendMessage(RobotInfoUtils.getRobotInfo().getF_Id(), signalDataEvent.getTargetUser(), msgObj.toString(), RobotInfoUtils.getRobotInfo().getF_Id(), signalDataEvent.getConnectionId());
                break;
            default:
                break;
        }
    }

    /**
     * 导航中引导提示弹窗
     */
    public void showNavigationIngDialog() {
        //导航中引导提示弹窗
        navigationIngDialog = new MyTipDialog(MyApp.getContext());
        navigationIngDialog.setDialogTitle(MvpBaseActivity.getActivityContext().getString(R.string.navigation_title));
        currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "zh");
        if (currentLanguage.equals("zh")) {   //中文地点名字
            navigationIngDialog.setDialogContent("我正在前往\t\"" + instructionName + "\"中...");
        } else {  //英文地点名字
            navigationIngDialog.setDialogContent("I am Going\t\"" + instructionEnName + "\"...");
        }
        navigationIngDialog.setTipSingleSelectModel(true);
        navigationIngDialog.setTipDialogSelectListener(MvpBaseActivity.getActivityContext().getString(R.string.end_boot_bt), new MyTipDialog.TipDialogSelectListener() {
            @Override
            public void tipSelect() {
                //首页是否有弹窗弹出
                SpUtils.putBoolean(MyApp.getContext(), MAIN_PAGE_DIALOG_SHOW_TAG, false);
                //获取下一步标志(禁止查询下一步操作)
                isTerminal = true;
                //导航失败失败不重试
                navigation_failed_retry_tag = false;
                LogUtils.d(DEFAULT_LOG_TAG, "终止单点导航任务");
                //更新任务指令状态
                instruction_status = "-1";
                BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                //终止移动
                SignalDataEvent stopMoveEvent = new SignalDataEvent();
                stopMoveEvent.setType(MSG_STOP_MOVE);
                EventBus.getDefault().post(stopMoveEvent);
            }
        });
        navigationIngDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        navigationIngDialog.show();
    }

    /**
     * 自动回充提示弹窗
     */
    public void rechargingDialog() {
        tipRechargingDialog = new MyTipDialog(MyApp.getContext());
        tipRechargingDialog.setDialogTitle(MvpBaseActivity.getActivityContext().getString(R.string.auto_recharge));
        tipRechargingDialog.setDialogContent(MvpBaseActivity.getActivityContext().getString(R.string.tip_recharge_tip_content));
        tipRechargingDialog.setTipSingleSelectModel(true);
        tipRechargingDialog.setTipDialogSelectListener(MvpBaseActivity.getActivityContext().getString(R.string.termination_bt_text), new MyTipDialog.TipDialogSelectListener() {
            @Override
            public void tipSelect() {
                boolean freeOperationStartTag = SpUtils.getBoolean(MyApp.getContext(), FREE_OPERATION_STATE_TAG, false);
                LogUtils.d(DEFAULT_LOG_ONE, "空闲操作工作流启动状态 自动回充工作流  freeOperationStartTag== " + freeOperationStartTag);
                if (freeOperationStartTag) { //空闲操作标签重置
                    SpUtils.putBoolean(MyApp.getContext(), FREE_OPERATION_STATE_TAG, false);
                    //启动计时器 等待空闲操作
                    EventBus.getDefault().post(new InitEvent(RobotConfig.START_FREE_OPERATION_MSG, ""));
                }
                LogUtils.d(DEFAULT_LOG_TAG, "终止自动回充任务");
                //首页是否有弹窗弹出
                SpUtils.putBoolean(MyApp.getContext(), MAIN_PAGE_DIALOG_SHOW_TAG, false);
                //获取下一步标志(禁止查询下一步操作)
                isTerminal = true;
                //终止自动回充,导航失败失败不重试导航
                navigation_failed_retry_tag = false;
                //更新任务指令状态
                instruction_status = "-1";
                BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                //终止移动,取消自动回充
                SignalDataEvent cancelRechargeEvent = new SignalDataEvent();
                cancelRechargeEvent.setType(MSG_CANCEL_RECHARGE);
                EventBus.getDefault().post(cancelRechargeEvent);
            }
        });
        tipRechargingDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        tipRechargingDialog.show();
    }

    /**
     * 发消息获取机器人状态
     */
    private void getRobotStatus(String baseResult) throws JSONException {
        LogUtils.d(TAG, "MSG_GET_ROBOT_STATUS  !!");
        LogUtils.d(TAG, "baseResult = " + baseResult);
        if (!TextUtils.isEmpty(baseResult)) {
            JoinInfoData joinInfoData = JsonUtils.decode(baseResult, JoinInfoData.class);
            String targetUser = "", connectionId = "";
            LogUtils.d(TAG, "joinInfoData = " + joinInfoData);
            if (joinInfoData != null) {
                targetUser = joinInfoData.getUserId();
                connectionId = joinInfoData.getConnectionId();

                JSONObject statusObj = new JSONObject();
                //替换为当前机器人的状态
                statusObj.put("Type", "RobotStatus");  //机器人当前状态
                statusObj.put("TimeStamp", String.valueOf(System.currentTimeMillis()));
                JSONObject status = new JSONObject();
                //替换为当前机器人的状态
                LogUtils.d(TAG, "锁轴状态 ： " + jRos.getRobotState().isLocked + "急停状态 ： " + jRos.getRobotState().isUrgent);
                status.put("connectionStatus", "1");
                if (jRos.getRobotState().isUrgent) {
                    status.put("isUrgent", "true");  //急停按钮是否按下
                } else {
                    status.put("isUrgent", "false");
                }
                if (jRos.getRobotState().isLocked) {
                    status.put("isManualCharging", "false");  //是否锁轴
                } else {
                    status.put("isManualCharging", "true");  //

                    SignalDataEvent controlMode = new SignalDataEvent(RobotConfig.MSG_CHANGE_POWER_LOCK, "");
                    controlMode.setPowerlock(1);
                    EventBus.getDefault().post(controlMode);
                    SpUtils.putBoolean(RobotBrainService.this, "isLock", true);

                    VideoMessageEve eve = new VideoMessageEve();
                    eve.setType(MSG_ISLOCK_CHANGE);
                    EventBus.getDefault().post(eve);

                }
                statusObj.put("Result", status.toString());
                statusObj.put("RobotType", RobotInfoUtils.getRobotInfo().getF_Account());
                String statusJson = statusObj.toString();
                LogUtils.d(TAG, "statusJson = " + statusJson);
                signalManager.invokeToSendMessage(RobotInfoUtils.getRobotInfo().getF_Id(), targetUser, statusJson, RobotInfoUtils.getRobotInfo().getF_Id(), connectionId);
            }
        }
    }

    /**
     * 开始导航语音提示语拼接
     */
    public String appendStartNavigationPrompt(String promptOne, String promptTwo) {
        StringBuffer startNavigationPrompt = new StringBuffer();
        startNavigationPrompt.append(promptOne);
        if (currentLanguage != null && currentLanguage.equals("zh") && (!TextUtils.isEmpty(instructionName))) {
            startNavigationPrompt.append(instructionName);
        } else if (currentLanguage != null && currentLanguage.equals("en") && (!TextUtils.isEmpty(instructionEnName))) {
            startNavigationPrompt.append(instructionEnName);
        }
        startNavigationPrompt.append(promptTwo);
        return startNavigationPrompt.toString();
    }

    /**
     * 语音播报,首页展示语音内容
     *
     * @param playContent
     */
    private void playShowContent(String playContent) {
        EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, playContent));
        //更新提示信息到首页对话记录
        EventBus.getDefault().post(new NavigationTip(playContent));
    }

    /**
     * 检测network 网络状态
     */
    private void checkNetWorkState() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().build(), networkCallback);
        }
    }

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        /**
         * 网络可用的回调
         */
        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            LogUtils.d(DEFAULT_LOG_TAG, "network = " + network.toString());
            /*网络可用，开始初始化服务*/
            if (handler.hasMessages(NET_DELAY_INIT)) {
                handler.removeMessages(NET_DELAY_INIT);
            }
            configVirtualNet();
//            if (ShellCmdUtils.checkNetConfig()) {
            handler.sendEmptyMessageDelayed(NET_DELAY_INIT, 3000);
            //网络连接成功,虚拟网卡配置成功
            Intent intent = new Intent();
            intent.setAction(NETWORK_CONNECTION_CHECK_SUCCESS);
            sendBroadcast(intent);
//            }
        }

        @Override
        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties);
            LogUtils.d(TAG, "-----linkProperties----" + linkProperties.toString());
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            LogUtils.d(DEFAULT_LOG_TAG, "-----onCapabilitiesChanged----" + networkCapabilities.toString());
            if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.d(TAG, "onCapabilitiesChanged: 网络类型为wifi");
                    ShellCmdUtils.execCmd(ShellCmdUtils.ADD_RULE);
                    ShellCmdUtils.execCmd(ShellCmdUtils.ADD_NETMASK);
                    ShellCmdUtils.execCmd(ShellCmdUtils.ADD_ROUTE);
                } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.d(TAG, "onCapabilitiesChanged: 蜂窝网络");
                    ShellCmdUtils.execCmd(ShellCmdUtils.ADD_RULE);
                    ShellCmdUtils.execCmd(ShellCmdUtils.ADD_NETMASK);
                    ShellCmdUtils.execCmd(ShellCmdUtils.ADD_ROUTE);
                } else {
                    ShellCmdUtils.execCmd(ShellCmdUtils.ADD_RULE);
                    ShellCmdUtils.execCmd(ShellCmdUtils.ADD_NETMASK);
                    ShellCmdUtils.execCmd(ShellCmdUtils.ADD_ROUTE);
                    Log.d(TAG, "onCapabilitiesChanged: 其他网络");
                }
                handler.sendEmptyMessageDelayed(NET_DELAY_INIT, 3000);
            }
        }

        /**
         * 网络丢失的回调
         */
        @Override
        public void onLost(Network network) {
            super.onLost(network);
            LogUtils.d(DEFAULT_LOG_TAG, "-----------onLost: network------------------");
            Intent intent = new Intent();
            intent.setAction(NETWORK_CONNECTION_CHECK_FAILURE);
            sendBroadcast(intent);
            //网络丢失
            OperationUtils.saveSpecialLog("SignalRDisconnected", "网络丢失");
//            showTips("网络状态", "网络中断，请检查网络");
        }
    };

    /**
     * UI展示弹框提示
     */
    private void showTips(String title, String content) {
        EventBus.getDefault().post(new InitEvent(RobotConfig.TYPE_SHOW_TIPS, title, content));
    }


    /**
     * 返回更新指令信息的结果
     */
    private Callback updateInstructionCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            LogUtils.e(DEFAULT_LOG_TAG, "onFailure : " + e.toString());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String result = response.body().string();
            LogUtils.d(DEFAULT_LOG_TAG, "updateInstructionCallback result =   " + result);
            string_hello = SpUtils.getString(RobotBrainService.this, "hello_string", "Hi");
            //空闲操作工作流启动状态

            try {
                JSONObject baseResult = new JSONObject(result);
                if (baseResult.has("type")) {
                    String type = baseResult.getString("type");
                    if (type.equals("success")) {
                        JSONObject jsonObject1 = new JSONObject();
                        jsonObject1.put("Type", "Tips");
                        jsonObject1.put("Result", "UpdateInstructionStatue");
                        String json = jsonObject1.toString();
                        signalManager.invokeToAllSendMessage(json);
                        LogUtils.d(DEFAULT_LOG_TAG, "fileUrl,  " + fileUrl + ", instruction_status,   " + instruction_status + "  ,prodiceId,  " + produceId + "  ,operationtype,  " + operationType + "  ,instructionType,  " + instructionType);
                        if (instruction_status.equals("1")) {
                            saveRobotstatus(3);
                            switch (instructionType) {
                                case "Location":
                                    taskVideoCall = false;
                                    break;
                                case "Check":
                                    break;
                                case "Operation":  //TODO 更新任务指令 执行操作
                                    if (operationType.equals("2")) {
                                        //点击宣教播放按钮后切换机器人为  操作中状态
                                        saveRobotstatus(3);
                                        Intent videoIntent = new Intent(RobotBrainService.this, VideoPlayActivity.class);
                                        videoIntent.putExtra("container", container);
                                        videoIntent.putExtra("blob", fileUrl);
                                        videoIntent.putExtra("enBlob", enFileUrl);
                                        videoIntent.putExtra("instructionId", instructionId);
                                        videoIntent.putExtra("status", instruction_status);
                                        videoIntent.putExtra("instructionName", instructionName);
                                        videoIntent.putExtra("instructionEnName", instructionEnName);
                                        videoIntent.putExtra("F_Type", instructionType);
                                        videoIntent.putExtra("operationType", operationType);
                                        videoIntent.putExtra("operationProcedureId", produceId);
                                        handler.postDelayed(() -> startActivity(videoIntent), 2000);
                                        LogUtils.d(DEFAULT_LOG_TAG, "开始播放视频任务\t\t");
                                    } else if (operationType.equals("3")) {
                                        Intent pdfIntent = new Intent(RobotBrainService.this, PdfPlayActivity.class);
                                        pdfIntent.putExtra("container", container);
                                        pdfIntent.putExtra("blob", fileUrl);
                                        pdfIntent.putExtra("enBlob", enFileUrl);
                                        pdfIntent.putExtra("instructionId", instructionId);
                                        pdfIntent.putExtra("instructionName", instructionName);
                                        pdfIntent.putExtra("instructionEnName", instructionEnName);
                                        pdfIntent.putExtra("status", instruction_status);
                                        pdfIntent.putExtra("F_Type", instructionType);
                                        pdfIntent.putExtra("operationType", operationType);
                                        pdfIntent.putExtra("operationProcedureId", produceId);
                                        handler.postDelayed(() -> startActivity(pdfIntent), 2000);
                                        LogUtils.d(DEFAULT_LOG_TAG, "开始播放pdf资料任务\t\t");
                                    } else if (operationType.equals("4")) {
                                        if (TextUtils.isEmpty(fileUrl)) {
                                            instruction_status = "-1";
                                            BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                                            return;
                                        } else {
                                            Intent textIntent = new Intent(RobotBrainService.this, TextPlayActivity.class);
                                            textIntent.putExtra("container", container);
                                            textIntent.putExtra("blob", fileUrl);
                                            textIntent.putExtra("enBlob", enFileUrl);
                                            textIntent.putExtra("instructionId", instructionId);
                                            textIntent.putExtra("instructionName", instructionName);
                                            textIntent.putExtra("instructionEnName", instructionEnName);
                                            textIntent.putExtra("status", instruction_status);
                                            textIntent.putExtra("F_Type", instructionType);
                                            textIntent.putExtra("operationType", operationType);
                                            textIntent.putExtra("operationProcedureId", produceId);
                                            startActivity(textIntent);
                                        }
                                        LogUtils.d(DEFAULT_LOG_TWO, "开始播放text文本任务\t\t" + fileUrl);
                                        LogUtils.d(DEFAULT_LOG_TWO, "开始播放text文本任务\t\t" + enFileUrl);
                                    } else if (operationType.equals("5")) {
                                        taskVideoCall = false;
                                        jRos.op_runParking((byte) 1);
                                        handler.postDelayed(() -> playShowContent(MvpBaseActivity.getActivityContext().getString(R.string.charging_prompt)), 2000);
                                    } else if (operationType.equals("6")) {
                                        taskVideoCall = true;
                                        instruction_status = "2";
                                        BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } else if (instruction_status.equals("2") || instruction_status.equals("4")) {
                            LogUtils.d(DEFAULT_LOG_TAG, "开始获取下一步操作任务, " + isTerminal + " produceId," + produceId);
                            if (!isTerminal) {
                                BusinessRequest.getNextStep(produceId, nextStepCallback);
                            }
                        } else if (instruction_status.equals("-1")) {
                            BusinessRequest.stopOperateTask(produceId, "3", stopTaskCallback);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    /**
     * 返回更新指令信息的结果
     */
    private Callback endUpdateInstructionCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            LogUtils.e(DEFAULT_LOG_TAG, "onFailure : " + e.toString());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String result = response.body().string();
            LogUtils.d(DEFAULT_LOG_TAG, "updateInstructionCallback result =   " + result);
            string_hello = SpUtils.getString(RobotBrainService.this, "hello_string", "Hi");
            try {
                JSONObject baseResult = new JSONObject(result);
                if (baseResult.has("type")) {
                    String type = baseResult.getString("type");
                    if (type.equals("success")) {
                        JSONObject jsonObject1 = new JSONObject();
                        jsonObject1.put("Type", "Tips");
                        jsonObject1.put("Result", "UpdateInstructionStatue");
                        String json = jsonObject1.toString();
                        signalManager.invokeToAllSendMessage(json);
                        LogUtils.d(DEFAULT_LOG_TAG, "fileUrl,  " + fileUrl + ", instruction_status,   " + instruction_status + "  ,prodiceId,  " + produceId + "  ,operationtype,  " + operationType + "  ,instructionType,  " + instructionType);
                        if (instruction_status.equals("1")) {
                            saveRobotstatus(3);
                            switch (instructionType) {
                                case "Location":
                                    taskVideoCall = false;
                                    break;
                                case "Check":
                                    break;
                                case "Operation":  //TODO 更新任务指令 执行操作
                                    if (operationType.equals("2")) {
                                        //点击宣教播放按钮后切换机器人为  操作中状态
                                        saveRobotstatus(3);
                                        Intent videoIntent = new Intent(RobotBrainService.this, VideoPlayActivity.class);
                                        videoIntent.putExtra("container", container);
                                        videoIntent.putExtra("blob", fileUrl);
                                        videoIntent.putExtra("enBlob", enFileUrl);
                                        videoIntent.putExtra("instructionId", instructionId);
                                        videoIntent.putExtra("status", instruction_status);
                                        videoIntent.putExtra("instructionName", instructionName);
                                        videoIntent.putExtra("instructionEnName", instructionEnName);
                                        videoIntent.putExtra("F_Type", instructionType);
                                        videoIntent.putExtra("operationType", operationType);
                                        videoIntent.putExtra("operationProcedureId", produceId);
                                        handler.postDelayed(() -> startActivity(videoIntent), 2000);
                                        LogUtils.d(DEFAULT_LOG_TAG, "开始播放视频任务\t\t");
                                    } else if (operationType.equals("3")) {
                                        Intent pdfIntent = new Intent(RobotBrainService.this, PdfPlayActivity.class);
                                        pdfIntent.putExtra("container", container);
                                        pdfIntent.putExtra("blob", fileUrl);
                                        pdfIntent.putExtra("enBlob", enFileUrl);
                                        pdfIntent.putExtra("instructionId", instructionId);
                                        pdfIntent.putExtra("instructionName", instructionName);
                                        pdfIntent.putExtra("instructionEnName", instructionEnName);
                                        pdfIntent.putExtra("status", instruction_status);
                                        pdfIntent.putExtra("F_Type", instructionType);
                                        pdfIntent.putExtra("operationType", operationType);
                                        pdfIntent.putExtra("operationProcedureId", produceId);
                                        handler.postDelayed(() -> startActivity(pdfIntent), 2000);
                                        LogUtils.d(DEFAULT_LOG_TAG, "开始播放pdf资料任务\t\t");
                                    } else if (operationType.equals("4")) {
                                        if (TextUtils.isEmpty(fileUrl)) {
                                            instruction_status = "-1";
                                            BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                                            return;
                                        } else {
                                            Intent textIntent = new Intent(RobotBrainService.this, TextPlayActivity.class);
                                            textIntent.putExtra("container", container);
                                            textIntent.putExtra("blob", fileUrl);
                                            textIntent.putExtra("enBlob", enFileUrl);
                                            textIntent.putExtra("instructionId", instructionId);
                                            textIntent.putExtra("instructionName", instructionName);
                                            textIntent.putExtra("instructionEnName", instructionEnName);
                                            textIntent.putExtra("status", instruction_status);
                                            textIntent.putExtra("F_Type", instructionType);
                                            textIntent.putExtra("operationType", operationType);
                                            textIntent.putExtra("operationProcedureId", produceId);
                                            startActivity(textIntent);
                                        }
                                        LogUtils.d(DEFAULT_LOG_TAG, "开始播放text文本任务\t\t" + fileUrl);
                                    } else if (operationType.equals("5")) {
                                        taskVideoCall = false;
                                        jRos.op_runParking((byte) 1);
                                        handler.postDelayed(() -> playShowContent(MvpBaseActivity.getActivityContext().getString(R.string.charging_prompt)), 2000);
                                    } else if (operationType.equals("6")) {
                                        taskVideoCall = true;
                                        instruction_status = "2";
                                        BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } else if (instruction_status.equals("2") || instruction_status.equals("4")) {
                            LogUtils.d(DEFAULT_LOG_TAG, "开始获取下一步操作任务, " + isTerminal + " produceId," + produceId);
                        } else if (instruction_status.equals("-1")) {
                            BusinessRequest.stopOperateTask(produceId, "3", stopTaskCallback);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    /**
     * 返回获取的下一步操作信息
     */
    private Callback nextStepCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            LogUtils.e(TAG, "onFailure : " + e.toString());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String result = response.body().string();
            try {
                JSONObject baseResObj = new JSONObject(result);  //TODO 获取下一步操作任务 执行操作
                if (baseResObj.has("type")) {
                    String type = baseResObj.getString("type");
                    if (type.equals("success")) {
                        String msg = baseResObj.getString("msg");
                        NextOperationData nextOperationData = JsonUtils.decode(msg, NextOperationData.class);
                        LogUtils.json(DEFAULT_LOG_TAG, JSON.toJSONString(nextOperationData));
                        instructionId = nextOperationData.getF_Id();
                        instructionType = nextOperationData.getF_Type();
                        instructionName = nextOperationData.getF_InstructionName();
                        container = nextOperationData.getF_Container();
                        operationType = nextOperationData.getOperationType();
                        fileUrl = nextOperationData.getF_FileUrl();
                        instructionEnName = nextOperationData.getF_InstructionEnName();
                        enFileUrl = nextOperationData.getF_EFileUrl();
                        isTerminal = false;
                        LogUtils.d(DEFAULT_LOG_TAG, "下一步任务获取成功  instructionType  " + instructionType);
                        if (!instructionType.equals("End")) {
                            saveRobotstatus(3);
                            if (instructionType.equals("Location")) {
                                LogUtils.d(DEFAULT_LOG_TAG, "下一步任务为导航任务开始启动定时器15秒后启动弹窗 ");
                                //上次导航目的地
                                nextNavigationDestination = currentNavigationDestination;
                                //本次导航目的地
                                currentNavigationDestination = instructionName;
                                //记录当前导航目的地信息
                                currentNavigationX = nextOperationData.getF_X();
                                currentNavigationY = nextOperationData.getF_Y();
                                currentNavigationZ = nextOperationData.getF_Z();
                                instruction_status = "1";
                                BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                            } else if (instructionType.equals("Operation")) {
                                saveRobotstatus(3);
                                instruction_status = "1";
                                BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                            }
                        } else if (instructionType.equals("End")) {
                            //工作流结束后机器人当前状态为空闲
                            RobotInfoUtils.setRobotRunningStatus("1");
                            boolean freeOperationStartTag = SpUtils.getBoolean(MyApp.getContext(), FREE_OPERATION_STATE_TAG, false);
                            if (freeOperationStartTag) { //空闲操作标签重置
                                SpUtils.putBoolean(MyApp.getContext(), FREE_OPERATION_STATE_TAG, false);
                                //启动计时器 等待空闲操作
                                EventBus.getDefault().post(new InitEvent(RobotConfig.START_FREE_OPERATION_MSG, ""));
                            }
                        } else {
                            if (!taskVideoCall) {
                                JSONObject jsonObject1 = new JSONObject();
                                jsonObject1.put("Type", "Tips");
                                jsonObject1.put("Result", "TaskEnd");
                                String endStr = jsonObject1.toString();
                                signalManager.invokeToAllSendMessage(endStr);
                            }
                            LogUtils.d(DEFAULT_LOG_TAG, "taskVideoCall === " + taskVideoCall + "充电状态, " + jRos.getRobotState().isCharge + " ,机器人运行状态, " + RobotInfoUtils.getRobotRunningStatus());
                            if (jRos.getRobotState().isCharge) {
                                saveRobotstatus(5);
                            } else {
                                saveRobotstatus(1);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 返回终止任务的信息
     */
    private Callback stopTaskCallback = new HttpMainCallback() {
        @Override
        public void onResult(BaseResponse baseResponse) {
            if (baseResponse != null) {
                String type = baseResponse.getType();
                if (type.equals("success")) {
                    ExecutorManager.getInstance().executeTask(() -> jRos.op_setAutoMove((byte) 0, 0, 0, 0));
                    try {
                        LogUtils.d(TAG, "operationType = " + operationType);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("Type", "Tips");
                        if (operationType == null || operationType.equals("null")) {
                            jsonObject.put("Result", "UnableAchieve");
                        } else {
                            switch (operationType) {
                                case "2":
                                case "3":
                                case "4":
                                    jsonObject.put("Result", "FileCannotPlayed");
                                    break;
                                case "5":
                                    jsonObject.put("Result", "exc_charge");
                                    break;
                            }
                        }
                        String json = jsonObject.toString();
                        LogUtils.d(TAG, "json = " + json);
                        if (!isTerminal) {
                            signalManager.invokeToAllSendMessage(json);
                        }
                        LogUtils.d("task_robot_status", "  saveRobotstatus(1)   ");
                        saveRobotstatus(1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //院内介绍工作流取消
                    boolean startIntroductionWorkflowTag = SpUtils.getBoolean(MyApp.getContext(), START_INTRODUCTION_WORK_FLOW_TAG, false);
                    if (startIntroductionWorkflowTag) {
                        SpUtils.putBoolean(MyApp.getContext(), START_INTRODUCTION_WORK_FLOW_TAG, false);
                        LogUtils.d(DEFAULT_LOG_TAG, "院内介绍工作流取消---启动计时器3分钟后再次启动院内介绍工作流----stopTaskCallback-------");
                    }
                    //自动回充工作流取消
                    boolean startAutomaticRechargeTag = SpUtils.getBoolean(MyApp.getContext(), AUTOMATIC_RECHARGE_TAG, false);
                    if (startAutomaticRechargeTag) {
                        SpUtils.putBoolean(MyApp.getContext(), AUTOMATIC_RECHARGE_TAG, false);
                        LogUtils.d(DEFAULT_LOG_TAG, "自动回充工作流取消----stopTaskCallback-------");
                    }
                    //取消dds语音播报
                    speechManager.cancelTtsPlay();
                }
            }
        }
    };


    /**
     * 更改机器人状态信息
     *
     * @param status
     */
    private void saveRobotstatus(int status) {
        RobotInfoUtils.setRobotRunningStatus(String.valueOf(status));
    }

    /**
     * 配置系统虚拟网卡
     */
    private void configVirtualNet() {
        ExecutorManager.getInstance().executeTask(() -> {
//            if (!ShellCmdUtils.checkNetConfig()) {
            ShellCmdUtils.execCmd(ShellCmdUtils.ADD_RULE);
            ShellCmdUtils.execCmd(ShellCmdUtils.ADD_NETMASK);
            ShellCmdUtils.execCmd(ShellCmdUtils.ADD_ROUTE);
//            }
        });
    }

    boolean flag = false;
    long flagTime = 0;

    private JSONObject allowMove() throws JSONException {
        JSONObject jsonObject = null;
        LogUtils.d(TAG, "isUrgent = " + jRos.getRobotState().isUrgent + "isLocked =" + jRos.getRobotState().isLocked + " ,isConnect = " + jRos.IsConnected());
        if (jRos.getRobotState().isUrgent) {
            jsonObject = new JSONObject();
            jsonObject.put("Type", "Tips");
            jsonObject.put("TimeStamp", System.currentTimeMillis());
            jsonObject.put("Result", "UrgentStop");
            jsonObject.put("RobotType", RobotInfoUtils.getRobotInfo().getF_Name());
            Log.d(TAG, "mPowerTechnology true");
        } else if (!jRos.getRobotState().isLocked) {
            jsonObject = new JSONObject();
            jsonObject.put("Type", "Tips");
            jsonObject.put("TimeStamp", System.currentTimeMillis());
            jsonObject.put("Result", "ChangeCharging");
            jsonObject.put("RobotType", RobotInfoUtils.getRobotInfo().getF_Name());
//            Bundle lockBundle = new Bundle();
//            lockBundle.putString("name", "power_lock");
//            lockBundle.putByte("state", (byte) 1);
            long currentTime = System.currentTimeMillis();
            if (Math.abs((currentTime - flagTime)) > VideoCallConstant.sTimeDiff + 600 && flag) {
                flag = false;
            }
            if (!flag) {
                flag = true;
                flagTime = currentTime;
                ExecutorManager.getInstance().executeTask(new Runnable() {
                    @Override
                    public void run() {
                        jRos.op_setPowerlock((byte) 1);
                    }
                });
                SpUtils.putBoolean(RobotBrainService.this, "isLock", true);
                VideoMessageEve eve = new VideoMessageEve();
                eve.setType(MSG_ISLOCK_CHANGE);
                EventBus.getDefault().post(eve);
                LogUtils.d("RobotRosService", "allowMove unlock");

            }
        } else if (!jRos.IsConnected()) {
            jsonObject = new JSONObject();
            jsonObject.put("Type", "Tips");
            jsonObject.put("TimeStamp", System.currentTimeMillis());
            jsonObject.put("Result", "BluetoothDisconnect");
            jsonObject.put("RobotType", RobotInfoUtils.getRobotInfo().getF_Name());
        } else {
            Log.d(TAG, "jsonObject is null");
        }
        return jsonObject;
    }
}
