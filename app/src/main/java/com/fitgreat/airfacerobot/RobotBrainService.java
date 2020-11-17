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

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.aiui.AiuiManager;
import com.fitgreat.airfacerobot.aiui.PlayTtsTask;
import com.fitgreat.airfacerobot.business.BusinessRequest;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.headeractuator.HeaderActuatorManager;
import com.fitgreat.airfacerobot.launcher.model.ActionEvent;
import com.fitgreat.airfacerobot.launcher.model.DaemonEvent;
import com.fitgreat.airfacerobot.launcher.model.InitEvent;
import com.fitgreat.airfacerobot.launcher.model.RobotSignalEvent;
import com.fitgreat.airfacerobot.launcher.model.NavigationTip;
import com.fitgreat.airfacerobot.launcher.service.UploadLogService;
import com.fitgreat.airfacerobot.launcher.utils.OperationUtils;
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
import static com.fitgreat.airfacerobot.constants.RobotConfig.CLICK_EMERGENCY_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.FILE_PLAY_OK;
import static com.fitgreat.airfacerobot.constants.RobotConfig.GUIDE_WORK_FLOW_ACTION_ID;
import static com.fitgreat.airfacerobot.constants.RobotConfig.INIT_ROS_KEY_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.INIT_TYPE_DDS_SUCCESS;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_POWER_LOCK;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_GETROS_VERSION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_INSTRUCTION_STATUS_FINISHED;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_ISLOCK_CHANGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_RETRY_INIT_SIGNAL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_LIGHT_OFF;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_LIGHT_ON;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_SPEAK_TEXT;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_START_INIT_VOICE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_SET_LIGHT_SUCCESS;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_STOP_MOVE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TASK_END;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TTS;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TTS_CANCEL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TTS_SHOW_DIALOG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TTS_TASK_END;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_UPDATE_INSTARUCTION_STATUS;
import static com.fitgreat.airfacerobot.constants.RobotConfig.ROS_CONNECTION_CHECK_FAILURE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.ROS_MSG_BATTERY;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PLAY_TASK_PROMPT_INFO;
import static com.fitgreat.airfacerobot.constants.RobotConfig.RECHARGE_SPECIFIC_WORKFLOW;
import static com.fitgreat.airfacerobot.constants.RobotConfig.ROBOT_STOP_MOVE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.ROS_MSG_ROBOT_POSITION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.START_INTRODUCTION_WORK_FLOW_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.TASK_DIALOG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.UPDATE_ROBOT_STATUS_TO_SERVER;
import static com.fitgreat.airfacerobot.constants.RobotConfig.VOICE_TERMINATION_TASK;
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
    private AiuiManager aiuiManager;
    private HeaderActuatorManager headerActuatorManager;
    private ConnectivityManager connectivityManager;
    private PlayTtsTask playTtsTask;

    private String instructionId;
    private String instructionType;
    private String instructionName;
    private String container;
    private String operationType;
    private String produceId;
    private String fileUrl;
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
    private boolean ros_inited = false;
    public String NETWORK_CONNECTION_CHECK_SUCCESS = "network.connection.check_success";
    public String NETWORK_CONNECTION_CHECK_FAILURE = "network.connection.check_failure";
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
    private Timer waiteTimer;
    private TimerTask waiteTimerTask;
    private DuiMessageObserver mMessageObserver = new DuiMessageObserver();// 消息监听器
    private DuiCommandObserver mCommandObserver = new DuiCommandObserver();// 命令监听器
    private DuiUpdateObserver mUpdateObserver = new DuiUpdateObserver();   //更新监听器
    private Timer batteryTimer;
    private TimerTask batteryTimerTask;
    //院内介绍工作流程终止后空闲倒计时 3分钟后再次启动院内介绍工作流
    private Timer introductionTimer;
    private TimerTask introductionTimerTask;
    private int introductionCountdown = 0;

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
    }

    /**
     * 创建各业务功能模块管理类
     */
    private void createFunctionManager() {
        signalManager = new SignalManager(this);
        headerActuatorManager = new HeaderActuatorManager();
        aiuiManager = new AiuiManager(this);
        jRos = JRos.getInstance();
        speechManager = SpeechManager.instance(this);
        //注册网络监听器
        checkNetWorkState();
        playTtsTask = new PlayTtsTask(speechManager);
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
            BusinessRequest.stopOperateTask(actionIdValue, "3", stopTaskCallback);
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

    /**
     * 定时器广播
     */
    class TimePowerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.d(TAG, "  当前电量,  " + Integer.parseInt(getBattery()) + "  当前机器人运行状态,  " + RobotInfoUtils.getRobotRunningStatus() + "  speechManager是否为空,  " + (speechManager != null));
            //ros初始化状态
            boolean rosInitTagState = SpUtils.getBoolean(getContext(), INIT_ROS_KEY_TAG, false);
            //获取机器人电量判断是否过低
            if (action.equals(TIME_POWER)) {
                if (rosInitTagState) {
                    if (Integer.parseInt(getBattery()) < 30) {
                        String workFlowString = SpUtils.getString(getContext(), RECHARGE_SPECIFIC_WORKFLOW, null);
                        if (RobotInfoUtils.getRobotRunningStatus().equals("1")) { //机器人空闲时
                            if (workFlowString != null) {
                                if (!workFlowString.equals("null") && MyApp.isNeedLowBatteryPrompt()) {   //服务端配置自动回充工作流
                                    //电量低于20% 语音播报"当前电量过低，我要去充电了"
                                    if (speechManager != null) {
                                        speechManager.textTtsPlay(getResources().getString(R.string.chargereminder), "0");
                                    }
                                    //弹窗提示回充,倒计时30秒,结束后开始回充
                                    EventBus.getDefault().post(new InitEvent(RobotConfig.PROMPT_ROBOT_RECHARGE, ""));
                                }
                            }
                        }
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + TIME_INTERVAL, pendingIntentPower);
                }
            }
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
                    //机器人除去冲电,移动状态外,定时1秒发一次终止移动指令
                    LogUtils.d(TAG, "  发送停止指令间隔一秒  " + "  机器人当前状态,  " + RobotInfoUtils.getRobotRunningStatus());
                    if ((!RobotInfoUtils.getRobotRunningStatus().equals("2")) && (!RobotInfoUtils.getRobotRunningStatus().equals("5"))) { //机器人视频时,定时发送停止移动指令
                        LogUtils.d(TAG, "  MSG_STOP_MOVE  ");
                        SignalDataEvent stopmoveEvent = new SignalDataEvent();
                        stopmoveEvent.setType(ROBOT_STOP_MOVE);
                        EventBus.getDefault().post(stopmoveEvent);
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + TIMER_INTERVAL_SECOND, pendingIntentEmergency);
                }
            }
            //周日24:00上传异常日志
            Calendar calendar = Calendar.getInstance();
            if (calendar.get(Calendar.SUNDAY) == Calendar.SUNDAY) {  //当前为周日
                String currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
                if (currentTime.equals("24:00:00")) {  //晚上24:00:00
                    Intent uploadLogIntent = new Intent(RobotBrainService.this, UploadLogService.class);
                    startService(uploadLogIntent);
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
                String filetype = fileEvent.getAction();
                LogUtils.d(TAG, "filetype = " + filetype);
                //点击宣教播放按钮后切换机器人为  操作中状态
                saveRobotstatus(3);
                //开始播报txt,播放视频宣教,pdf前,移除当前播报提示
                speechManager.cancelTtsPlay();
                if (filetype.equals("2")) {
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
                } else if (filetype.equals("3")) {
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
                } else if (filetype.equals("4")) {
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
                    LogUtils.d("startSpecialWorkFlow", "isTerminal = " + isTerminal + " 本次导航失败导航任务信息--" + currentNavigationDestination + "---" + currentNavigationX + "---" + currentNavigationY + "---" + currentNavigationZ);
                    if (!isTerminal) {
                        speechManager.textTtsPlay("抱歉，我无法到达" + instructionName + "！", "0");
                        //更新提示信息到首页对话记录
                        EventBus.getDefault().post(new NavigationTip("抱歉，我无法到达" + instructionName + "！"));
                    }
                    //本次导航任务失败,重试三次如果还是不成功最后回到原点冲电
                    if (navigationTimes < 3) {
                        navigationTimes++;
                        handler.postDelayed(() -> {
                            speechManager.textTtsPlay("我要去" + currentNavigationDestination + "啦", "0");
                            //更新提示信息到首页对话记录
                            EventBus.getDefault().post(new NavigationTip("我要去" + currentNavigationDestination + "啦"));
                        }, 500);
                        ExecutorManager.getInstance().executeTask(() -> {
                            jRos.op_setAutoMove((byte) 1, Double.valueOf(currentNavigationX), Double.valueOf(currentNavigationY), Double.valueOf(currentNavigationZ));
                            instruction_status = "1";
                            BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                        });
                    } else {
                        speechManager.textTtsPlay("当前任务出现异常,我将回原点了", "0");
//                        boolean startGuideWorkflowTag = SpUtils.getBoolean(MyApp.getContext(), START_GUIDE_WORK_FLOW_TAG, false);
//                        if (startGuideWorkflowTag) {
//                            //引导工作流启动标志
//                            SpUtils.putBoolean(MyApp.getContext(), START_GUIDE_WORK_FLOW_TAG, false);
//                        }
                        //取消引导流程任务
                        String actionIdValue = SpUtils.getString(MyApp.getContext(), GUIDE_WORK_FLOW_ACTION_ID, null);
                        if (actionIdValue != null) {
                            BusinessRequest.stopOperateTask(actionIdValue, "3", stopTaskCallback);
                        }
                    }
                    break;
                case "navigation_arrived":  //导航成功  TODO
                    //清空本次导航任务地点xyz信息
                    currentNavigationX = null;
                    currentNavigationY = null;
                    currentNavigationZ = null;
                    //导航失败重试次数
                    navigationTimes = 0;
                    //语音播报到达目的地
                    speechManager.textTtsPlay("我已到达" + instructionName, "0");
                    //更新提示信息到首页对话记录
                    EventBus.getDefault().post(new NavigationTip("我已到达" + instructionName));
                    instruction_status = "2";
                    LogUtils.d("navigation_arrived", "instructionId = " + instructionId + " 导航成功到达  " + instructionName);
                    BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                    break;
                case "parking_success":         //充电成功
                    speechManager.textTtsPlay("充电成功!", "0");
                    //更新提示信息到首页对话记录
                    EventBus.getDefault().post(new NavigationTip("充电成功!"));
                    saveRobotstatus(5);
                    LogUtils.d("task_robot_status", "冲电成功:为冲电状态" + "robot status = " + RobotInfoUtils.getRobotRunningStatus());
                    BusinessRequest.updateRobotState(getBattery(), RobotInfoUtils.getRobotRunningStatus(), new HttpCallback() {
                        @Override
                        public void onResult(BaseResponse baseResponse) {
                            LogUtils.json("parking_success", "更新机器人信息成功: " + JSON.toJSONString(baseResponse));
                        }
                    });
                    instruction_status = "2";
                    BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                    break;
                case "parking_timeout":         //充电失败
                    LogUtils.d(TAG, "parking_success !!!!!!!!!!!!!!!!!!");
                    speechManager.textTtsPlay("充电失败，未扫描到充电桩!", "0");
                    //更新提示信息到首页对话记录
                    EventBus.getDefault().post(new NavigationTip("充电失败，未扫描到充电桩!"));
                    instruction_status = "-1";
                    BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
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
                        LogUtils.d(TAG, "isSuccess = " + isSuccess + "\t\tjRos != null\t" + (jRos != null));
                        if (isSuccess) {
                            if (jRos != null) {
//                                handler.sendEmptyMessageDelayed(MSG_JROS_TIMEOUT, 15000);
                                jrosInit();
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
                            mCommandObserver.unregist();
                            mMessageObserver.unregist();
                            mUpdateObserver.unregist();
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
     * @param actionEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(ActionEvent actionEvent) {
        switch (actionEvent.getmActionKind()) {
            case VOICE_TERMINATION_TASK: //语音指令终止当前任务
                voiceTerminationTask();
                break;
            case PLAY_TASK_PROMPT_INFO:
                LogUtils.d("CommandTodo", actionEvent.getmActionContent());
                speechManager.textTtsPlay(actionEvent.getmActionContent(), "0");
                break;
            case INIT_TYPE_DDS_SUCCESS://dds初始化成功
                LogUtils.d("CommandTodo", "dds初始化成功");
//                SpeechManager.setParameter();
                mCommandObserver.regist();
                mMessageObserver.regist();
                mUpdateObserver.regist();
                break;
        }
    }

    private void jrosInit() {
        jRos.init((new JRosConfig()).addConfig("SOCKET_URI", "ws://192.168.88.8:9090"), new JRosInitListener() {
            public void onInitComplete() {
                LogUtils.d("JRos", "jRos onInitComplete ! ");
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
                            LogUtils.json("RobotSignalEvent", JSON.toJSONString(batteryEvent));
                        } catch (Exception e) {
                            e.printStackTrace();
                            LogUtils.e("RobotSignalEvent", "连续获取机器人电量异常:  " + e.getMessage());
                        }
                    }
                };
                batteryTimer.scheduleAtFixedRate(batteryTimerTask, 0, 1000);
//                ExecutorManager.getInstance().executeScheduledTask(() -> {
//                    SignalDataEvent positionEvent = new SignalDataEvent(ROS_MSG_ROBOT_POSITION, "");
//                    positionEvent.setPosition_X(jRos.op_GetPose().x);
//                    positionEvent.setPosition_Y(jRos.op_GetPose().y);
//                    positionEvent.setPosition_Z(jRos.op_GetPose().yaw);
//                    EventBus.getDefault().post(positionEvent);
//                }, 0, 5, TimeUnit.SECONDS);
                //ros初始化成功
                SpUtils.putBoolean(MyApp.getContext(), INIT_ROS_KEY_TAG, true);
            }

            public void onError(String msg) {
                //ros初始化错误
                Intent intent = new Intent();
                intent.setAction(ROS_CONNECTION_CHECK_FAILURE);
                sendBroadcast(intent);
                LogUtils.d("JRos", "main onError msg:" + msg);
                //ros初始化报错
                SpUtils.putBoolean(MyApp.getContext(), INIT_ROS_KEY_TAG, false);
            }

            @Override
            public void onDisconnect(String s) {
                //ros连接断开
                Intent intent = new Intent();
                intent.setAction(ROS_CONNECTION_CHECK_FAILURE);
                sendBroadcast(intent);
                LogUtils.d("JRos", "JRos onDisconnect:" + s);
                //ros断开
                OperationUtils.saveSpecialLog("RobotDisconnected", "机器人断开连接");
                SpUtils.putBoolean(MyApp.getContext(), INIT_ROS_KEY_TAG, false);
            }

            @Override
            public void onUpdate(int i) {
                LogUtils.d(TAG, "JRos onUpdate :" + i);
                EventBus.getDefault().post(new InitEvent(RobotConfig.TYPE_CHECK_STATE, RobotConfig.INIT_TYPE_ROS_PROGRESS, String.valueOf(i)));   //广播JROS初始化进度
            }
        });
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
                        speechManager.textTtsPlay(event.getText(), "0");
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
                speechManager.textTtsPlay(event.getText(), "0");
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
            case OPERATION_TYPE_AUTO_MOVE:   //TODO 开始导航任务
                instructionType = signalDataEvent.getInstructionType();
                instructionName = signalDataEvent.getF_InstructionName();
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
                LogUtils.d("startSpecialWorkFlow", "开始导航任务");
                LogUtils.json("startSpecialWorkFlow", JSON.toJSONString(signalDataEvent));
//                handler.sendEmptyMessageDelayed(MSG_SPEAK_POSITION,1000);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        speechManager.textTtsPlay("我要去" + instructionName + "啦", "0");
                        //更新提示信息到首页对话记录
                        EventBus.getDefault().post(new NavigationTip("我要去" + instructionName + "啦"));
                    }
                }, 500);
                ExecutorManager.getInstance().executeTask(new Runnable() {
                    @Override
                    public void run() {
                        jRos.op_setAutoMove((byte) 1, Double.valueOf(signalDataEvent.getX()), Double.valueOf(signalDataEvent.getY()), Double.valueOf(signalDataEvent.getE()));
                        instruction_status = "1";
                        BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                    }
                });
                break;
            case MSG_UPDATE_INSTARUCTION_STATUS:
                instructionType = signalDataEvent.getInstructionType();
                instructionName = signalDataEvent.getF_InstructionName();
                container = signalDataEvent.getContainer();
                operationType = signalDataEvent.getOperationType();
                produceId = signalDataEvent.getProduceId();
                fileUrl = signalDataEvent.getFileUrl();
                targetUser = signalDataEvent.getTargetUser();
                connectionId = signalDataEvent.getConnectionId();
                instructionId = signalDataEvent.getInstructionId();
                instruction_status = "1";
                LogUtils.d(TAG, "MSG_UPDATE_INSTARUCTION_STATUS !!!!!!!!!!!!           instructionType = " + instructionType + " , operationType = " + operationType + " ,instructionId = " + instructionId);
                BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                break;
            case MSG_STOP_MOVE:
                ExecutorManager.getInstance().executeTask(new Runnable() {
                    @Override
                    public void run() {
                        jRos.op_setAutoMove((byte) 0, 0, 0, 0);
                    }
                });
                LogUtils.d(TAG, "MSG_STOP_MOVE:当前导航任务定时终止");
                break;
            case ROBOT_STOP_MOVE: //机器人移动中终止
                ExecutorManager.getInstance().executeTask(new Runnable() {
                    @Override
                    public void run() {
                        jRos.op_setVelControl(0.0f, 0.0f);
                    }
                });
                LogUtils.d(TAG, "MSG_STOP_MOVE:当前导航任务定时终止");
                break;
            case MSG_STOP_TASK:
                isTerminal = true;
                LogUtils.d("startSpecialWorkFlow", "--------MSG_STOP_TASK---------" + (speechManager != null));
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
                    speechManager.textTtsPlay("任务已终止", "0");
                }
                //取消循环播放宣教提示语定时任务
                ExecutorManager.getInstance().cancelScheduledTask(speakTipsFuture);
                //更新提示信息到首页对话记录
                EventBus.getDefault().post(new NavigationTip("任务已终止"));
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
            case MSG_INSTRUCTION_STATUS_FINISHED:  //视频  pdf播放结束/视频  pdf播放中结束当前工作流活动任务
                instruction_status = signalDataEvent.getAction();
                LogUtils.d("startSpecialWorkFlow", "医院介绍内容播放结束 !!!!!!!!   instruction_status = " + instruction_status + ", InstructionId =" + signalDataEvent.getInstructionId());
                BusinessRequest.UpdateInstructionStatue(signalDataEvent.getInstructionId(), instruction_status, updateInstructionCallback);
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
            case MSG_SET_LIGHT_SUCCESS:
                int light_status = signalDataEvent.getLight_status();
                switch (light_status) {
                    case 17:
                        LogUtils.d(TAG, "消毒灯关闭成功！");
//                        aiuiManager.onPlayLineTTS("消毒已结束");
                        speechManager.textTtsPlay("消毒已结束", "0");
                        InitEvent initEvent = new InitEvent();
                        initEvent.setType(MSG_LIGHT_OFF);
                        EventBus.getDefault().post(initEvent);
                        instruction_status = "2";
                        BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                        break;
                    case 18:
                        LogUtils.d(TAG, "消毒灯打开成功！");
//                        aiuiManager.onPlayLineTTS("注意注意，我要开始消毒啦，请大家速速远离，不要盯着我看哦，我散发的紫外光可是很强的。");
                        speechManager.textTtsPlay("注意注意，我要开始消毒啦，请大家速速远离，不要盯着我看哦，我散发的紫外光可是很强的。", "0");
                        InitEvent initEvent1 = new InitEvent();
                        initEvent1.setType(MSG_LIGHT_ON);
                        EventBus.getDefault().post(initEvent1);
                        instruction_status = "2";
                        BusinessRequest.UpdateInstructionStatue(instructionId, instruction_status, updateInstructionCallback);
                        break;
                }
            default:
                break;
        }
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

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        /**
         * 网络可用的回调
         */
        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            LogUtils.d(TAG, "network = " + network.toString());
            LogUtils.d(TAG, "-------onAvailable: network--------ip:" + NetworkUtils.getLocalIpAddress(getApplication()));
            /*网络可用，开始初始化服务*/
            if (handler.hasMessages(NET_DELAY_INIT)) {
                handler.removeMessages(NET_DELAY_INIT);
            }
            configVirtualNet();
            if (ShellCmdUtils.checkNetConfig()) {
                handler.sendEmptyMessageDelayed(NET_DELAY_INIT, 3000);
                //网络连接成功,虚拟网卡配置成功
                Intent intent = new Intent();
                intent.setAction(NETWORK_CONNECTION_CHECK_SUCCESS);
                sendBroadcast(intent);
            }
        }

        @Override
        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties);
            LogUtils.d(TAG, "-----linkProperties----" + linkProperties.toString());
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            LogUtils.d(TAG, "-----onCapabilitiesChanged----" + networkCapabilities.toString());
        }

        /**
         * 网络丢失的回调
         */
        @Override
        public void onLost(Network network) {
            super.onLost(network);
            LogUtils.d(TAG, "-----------onLost: network");
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
            LogUtils.e("update_instruction", "onFailure : " + e.toString());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String result = response.body().string();
            LogUtils.d("startSpecialWorkFlow", "updateInstructionCallback result =   " + result);
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
                        LogUtils.d("startSpecialWorkFlow", "fileUrl,  " + fileUrl + ", instruction_status,   " + instruction_status + "  ,prodiceId,  " + produceId + "  ,operationtype,  " + operationType + "  ,instructionType,  " + instructionType);
                        if (instruction_status.equals("1")) {
                            saveRobotstatus(3);
                            switch (instructionType) {
                                case "Location":
                                    taskVideoCall = false;
                                    break;
                                case "Check":
                                    break;
                                case "Operation":
                                    if (operationType.equals("2")) {
                                        //点击宣教播放按钮后切换机器人为  操作中状态
                                        saveRobotstatus(3);
                                        Intent videointent = new Intent(RobotBrainService.this, VideoPlayActivity.class);
                                        videointent.putExtra("container", container);
                                        videointent.putExtra("blob", fileUrl);
                                        videointent.putExtra("instructionId", instructionId);
                                        videointent.putExtra("status", instruction_status);
                                        videointent.putExtra("instructionName", instructionName);
                                        videointent.putExtra("F_Type", instructionType);
                                        videointent.putExtra("operationType", operationType);
                                        videointent.putExtra("operationProcedureId", produceId);
                                        handler.postDelayed(() -> startActivity(videointent), 2000);
                                        LogUtils.d("startSpecialWorkFlow", "开始播放视频任务\t\t");
                                    } else if (operationType.equals("3")) {
                                        Intent pdfintent = new Intent(RobotBrainService.this, PdfPlayActivity.class);
                                        pdfintent.putExtra("container", container);
                                        pdfintent.putExtra("blob", fileUrl);
                                        pdfintent.putExtra("instructionId", instructionId);
                                        pdfintent.putExtra("instructionName", instructionName);
                                        pdfintent.putExtra("status", instruction_status);
                                        pdfintent.putExtra("F_Type", instructionType);
                                        pdfintent.putExtra("operationType", operationType);
                                        pdfintent.putExtra("operationProcedureId", produceId);
                                        handler.postDelayed(() -> startActivity(pdfintent), 2000);
                                        LogUtils.d("startSpecialWorkFlow", "开始播放pdf资料任务\t\t");
                                    } else if (operationType.equals("4")) {
                                        speakTipsFuture = ExecutorManager.getInstance().executeScheduledTask(speakTipsRunnable, 2, SpUtils.getInt(RobotBrainService.this, "de_time", 10), TimeUnit.SECONDS);
                                        taskVideoCall = false;
                                        LogUtils.d("update_instruction", "txt url:" + fileUrl);
                                        DaemonEvent event = new DaemonEvent(TASK_DIALOG, "4");
                                        event.extra = instructionName;
                                        EventBus.getDefault().post(event);
                                    } else if (operationType.equals("5")) {
                                        taskVideoCall = false;
                                        jRos.op_runParking((byte) 1);
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                speechManager.textTtsPlay("我正在自动回充中!", "0");
                                                //更新提示信息到首页对话记录
                                                EventBus.getDefault().post(new NavigationTip("我正在自动回充中!"));
                                            }
                                        }, 2000);
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
                            LogUtils.d("startSpecialWorkFlow", "开始获取下一步操作任务, " + isTerminal + " produceId," + produceId);
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
                JSONObject baseResObj = new JSONObject(result);
                if (baseResObj.has("type")) {
                    String type = baseResObj.getString("type");
                    if (type.equals("success")) {
                        String msg = baseResObj.getString("msg");
                        NextOperationData nextOperationData = JsonUtils.decode(msg, NextOperationData.class);
                        LogUtils.json("startSpecialWorkFlow", JSON.toJSONString(nextOperationData));
                        instructionId = nextOperationData.getF_Id();
                        instructionType = nextOperationData.getF_Type();
                        instructionName = nextOperationData.getF_InstructionName();
                        container = nextOperationData.getF_Container();
                        operationType = nextOperationData.getOperationType();
                        fileUrl = nextOperationData.getF_FileUrl();
                        isTerminal = false;
                        LogUtils.d("startSpecialWorkFlow", "下一步任务获取成功  instructionType  " + instructionType);
                        if (!instructionType.equals("End")) {
                            saveRobotstatus(3);
                            if (instructionType.equals("Location")) {
                                LogUtils.d("startSpecialWorkFlow", "下一步任务为导航任务开始启动定时器15秒后启动弹窗 ");
                                //上次导航目的地
                                nextNavigationDestination = currentNavigationDestination;
                                //本次导航目的地
                                currentNavigationDestination = instructionName;
                                speechManager.textTtsPlay(nextNavigationDestination + "已讲完啦,我将去" + currentNavigationDestination + "啦", "0");
                                //更新显示语音数据到首页
                                EventBus.getDefault().post(new NavigationTip(nextNavigationDestination + "已讲完啦,我将去" + currentNavigationDestination + "啦"));
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
                        } else if (instructionType.equals("End")) {   //引导流程结束
                            LogUtils.d("startSpecialWorkFlow", "院内介绍工作流结束重启该工作流");
                            boolean startGuideWorkflowTag = SpUtils.getBoolean(MyApp.getContext(), START_INTRODUCTION_WORK_FLOW_TAG, false);
                            if (startGuideWorkflowTag) {
                                //重启院内介绍工作流程
                                OperationUtils.startSpecialWorkFlow(3);
                            }

                        } else {
                            LogUtils.d(TAG, "taskVideoCall === " + taskVideoCall);
                            if (!taskVideoCall) {
                                JSONObject jsonObject1 = new JSONObject();
                                jsonObject1.put("Type", "Tips");
                                jsonObject1.put("Result", "TaskEnd");
                                String endStr = jsonObject1.toString();
                                signalManager.invokeToAllSendMessage(endStr);
                            }
                            LogUtils.d("startSpecialWorkFlow", "充电状态, " + jRos.getRobotState().isCharge + " ,机器人运行状态, " + RobotInfoUtils.getRobotRunningStatus());
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
     * 取消终止引导流程
     */
    private void cancelTask() {
        //引导工作流启动标志
//        SpUtils.putBoolean(MyApp.getContext(), START_GUIDE_WORK_FLOW_TAG, false);
        String actionIdValue = SpUtils.getString(MyApp.getContext(), GUIDE_WORK_FLOW_ACTION_ID, null);
        speechManager.textTtsPlay("我要回去啦，下次有事在叫我哦。", "0");
        instruction_status = "-1";
        BusinessRequest.UpdateInstructionStatue(actionIdValue, instruction_status, updateInstructionCallback);
    }

    /**
     * 返回终止任务的信息
     */
    private Callback stopTaskCallback = new HttpMainCallback() {
        @Override
        public void onResult(BaseResponse baseResponse) {
            if (baseResponse != null) {
                String type = baseResponse.getType();
                if (type.equals("success")) {
                    ExecutorManager.getInstance().executeTask(new Runnable() {
                        @Override
                        public void run() {
                            jRos.op_setAutoMove((byte) 0, 0, 0, 0);
                        }
                    });
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
                    //院内介绍引导工作流结束
                    boolean startIntroductionWorkflowTag = SpUtils.getBoolean(MyApp.getContext(), START_INTRODUCTION_WORK_FLOW_TAG, false);
                    if (startIntroductionWorkflowTag) {
                        SpUtils.putBoolean(MyApp.getContext(), START_INTRODUCTION_WORK_FLOW_TAG, false);
                        LogUtils.d("startSpecialWorkFlow", "院内介绍工作流取消---启动计时器3分钟后再次启动院内介绍工作流----stopTaskCallback-------");
                        //医院介绍工作流程结束后,3分钟无操作后再次启动院内介绍工作流
                        introductionTimer = new Timer();
                        introductionTimerTask = new TimerTask() {
                            @Override
                            public void run() {
                                introductionCountdown++;
                                if (introductionCountdown == 180) {
                                    introductionTimer.cancel();
                                    introductionTimerTask.cancel();
                                    LogUtils.d("startSpecialWorkFlow", "空闲3分钟后再次启动院内介绍工作流----------");
                                    OperationUtils.startSpecialWorkFlow(3);
                                }
                            }
                        };
                        introductionTimer.schedule(introductionTimerTask, 0, 1000);
                    }
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


    Runnable speakTipsRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtils.d(TAG, "filename = " + instructionName);
//            aiuiManager.onPlayLineTTS(string_hello + ",现在给你播报" + instructionName + ",请点击屏幕上的\"开始播放\"按钮");
//            speechManager.textTtsPlay(string_hello + ",现在给你播报" + instructionName + ",请点击屏幕上的\"开始播放\"按钮", "0");
            //更新提示信息到首页对话记录
//            EventBus.getDefault().post(new NavigationTip(string_hello + ",现在给你播报" + instructionName + ",请点击屏幕上的\"开始播放\"按钮"));
        }
    };

}
