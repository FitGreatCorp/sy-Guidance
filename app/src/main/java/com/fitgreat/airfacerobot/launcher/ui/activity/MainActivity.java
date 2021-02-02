package com.fitgreat.airfacerobot.launcher.ui.activity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.chosedestination.ChoseDestinationActivity;
import com.fitgreat.airfacerobot.commonproblem.CommonProblemActivity;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.floatball.AccessibilityConstant;
import com.fitgreat.airfacerobot.floatball.DelayOnClickListener;
import com.fitgreat.airfacerobot.floatball.FloatWindow;
import com.fitgreat.airfacerobot.floatball.FloatWindowOption;
import com.fitgreat.airfacerobot.floatball.FloatWindowViewStateCallback;
import com.fitgreat.airfacerobot.introductionlist.IntroductionListActivity;
import com.fitgreat.airfacerobot.launcher.contractview.MainView;
import com.fitgreat.airfacerobot.launcher.widget.NormalOrCountDownDialog;
import com.fitgreat.airfacerobot.launcher.widget.MyTipDialog;
import com.fitgreat.airfacerobot.launcher.widget.ValidationOrPromptDialog;
import com.fitgreat.airfacerobot.model.ActionDdsEvent;
import com.fitgreat.airfacerobot.model.AppVersion;
import com.fitgreat.airfacerobot.model.CommandDataEvent;
import com.fitgreat.airfacerobot.model.DaemonEvent;
import com.fitgreat.airfacerobot.model.InitEvent;
import com.fitgreat.airfacerobot.model.MapEntity;
import com.fitgreat.airfacerobot.model.NavigationTip;
import com.fitgreat.airfacerobot.model.RobotSignalEvent;
import com.fitgreat.airfacerobot.launcher.presenter.MainPresenter;
import com.fitgreat.airfacerobot.launcher.utils.LanguageUtil;
import com.fitgreat.airfacerobot.launcher.utils.OperationUtils;
import com.fitgreat.airfacerobot.launcher.utils.ToastUtils;
import com.fitgreat.airfacerobot.launcher.widget.CommonTipDialog;
import com.fitgreat.airfacerobot.launcher.widget.WarnningDialog;
import com.fitgreat.airfacerobot.model.WorkflowEntity;
import com.fitgreat.airfacerobot.remotesignal.model.InitUiEvent;
import com.fitgreat.airfacerobot.remotesignal.model.RobotInfoData;
import com.fitgreat.airfacerobot.remotesignal.model.SignalDataEvent;
import com.fitgreat.airfacerobot.settings.SettingActivity;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.airfacerobot.speech.model.MessageBean;
import com.fitgreat.airfacerobot.versionupdate.DownloadUtils;
import com.fitgreat.airfacerobot.versionupdate.DownloadingDialog;
import com.fitgreat.airfacerobot.versionupdate.VersionInfo;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.archmvp.base.util.ExecutorManager;
import com.fitgreat.archmvp.base.util.JsonUtils;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.RouteUtils;
import com.fitgreat.archmvp.base.util.ShellCmdUtils;
import com.fitgreat.archmvp.base.util.SpUtils;
import com.fitgreat.archmvp.base.util.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;

import static com.fitgreat.airfacerobot.constants.Constants.COMMON_PROBLEM_TAG;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_FOUR;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_ONE;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_THREE;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TWO;
import static com.fitgreat.airfacerobot.constants.Constants.SINGLE_POINT_NAVIGATION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.AUTOMATIC_RECHARGE_ACTIVITY_ID;
import static com.fitgreat.airfacerobot.constants.RobotConfig.AUTOMATIC_RECHARGE_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CLICK_EMERGENCY_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CLOSE_DDS_WAKE_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CLOSE_RECHARGING_TIP_DIALOG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_FREE_OPERATION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_LANGUAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.DDS_OBSERVER_REGISTERED;
import static com.fitgreat.airfacerobot.constants.RobotConfig.DDS_VOICE_TEXT_CANCEL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.FREE_OPERATION_STATE_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.INIT_ROS_KEY_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.IS_CONTROL_MODEL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MAIN_PAGE_DIALOG_SHOW_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MAIN_PAGE_WHETHER_SHOW;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MAP_INFO_CASH;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_FLOATING_BALL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_GETROS_VERSION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_INSTRUCTION_STATUS_FINISHED;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_LIGHT_OFF;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_LIGHT_ON;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_ROS_NEXT_STEP;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PLAY_TASK_PROMPT_INFO;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PROMPT_ROBOT_RECHARGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.START_BLINK_ANIMATION_MSG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.START_DDS_WAKE_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.START_FREE_OPERATION_MSG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.START_INTRODUCTION_WORK_FLOW_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.START_SPEAK_ANIMATION_MSG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.TYPE_CHECK_STATE_DONE;


/**
 * 启动桌面activity<p>
 *
 * @author zixuefei
 * @since 2020/3/11 0011 10:14
 */
public class MainActivity extends MvpBaseActivity<MainView, MainPresenter> implements MainView, ValidationOrPromptDialog.ValidationFailListener {
    @BindView(R.id.signal_img)
    ImageView mSignalImg;
    @BindView(R.id.text_battery)
    TextView mTextBattery;
    @BindView(R.id.battery_img)
    ImageView mBatteryImg;
    @BindView(R.id.robot_name)
    TextView mRobotName;
    @BindView(R.id.voice_msg)
    TextView mVoiceMsg;
    @BindView(R.id.wink_speak_animation)
    ImageView mWinkSpeakAnimation;
    @BindView(R.id.language_chinese_relativeLayout)
    RelativeLayout mLanguageChineseRelativeLayout;
    @BindView(R.id.language_english_relativeLayout)
    RelativeLayout mLanguageEnglishRelativeLayout;
    @BindView(R.id.voice_msg_scrollView)
    ScrollView mVoiceMsgScrollView;

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ALERT_CODE = 6666;
    private static final int CHECK_HARDWARE_VERSION_CODE = 6667;
    private static final int CHECK_HDVC = 6668;
    private static final int CHECK_APP_VERSION_CODE = 6669;
    //机器人默认眨眼动画
    private static final int START_BLINK_ANIMATION_TAG = 8888;
    //机器人默认眨眼动画
    private static final int STOP_BLINK_ANIMATION_TAG = 7777;
    //机器人说话动画
    private static final int START_SPEAK_ANIMATION_TAG = 9999;
    private CommonTipDialog commonTipDialog;
    private FloatWindow floatWindow;
    private boolean isResume;
    private Future checkVersionScheduledFuture;
    private DownloadingDialog downloadingDialog;
    private List<VersionInfo.FStepBean> rosUpdateStep;
    private String rosSoftId;
    private VersionInfo.FStepBean fStepBean;
    private int currentStep = 0;
    private long lasttime = 0;
    private boolean upgrade_success = true;
    private boolean show_svdialog = false;
    private boolean show_hvdialog = false;
    private Future hdvScheduledFuture;
    private static final int REQUEST_CODE_WRITE_SETTINGS = 1001;
    private WarnningDialog warnningDialog;
    private String currentLanguage;
    private TimerTask introductionTimerTask;
    private Timer introductionTimer;
    //空闲操作倒计时5分钟
    private int freeOperationCountdown;
    private NormalOrCountDownDialog lowBatteryTipDialog;
    private ValidationOrPromptDialog validationOrPromptDialog;
    private RobotInfoData robotInfoData;
    private NormalOrCountDownDialog normalOrCountDownDialog;
    //对话框内容最低高度
    private int minDialogBoxHeight = 0;
    //对话框内容滑动位置x
    private int scrollPositionX = 0;
    //对话框内容滑动位置Y
    private int scrollPositionY = 0;
    private final int CONTENT_SLIDE_TAG = 2002;
    //首页自动回充提示弹窗弹出前,加载提示弹窗(防止多次点击自动回充)
    private MyTipDialog rechargeTipDialog;
    private String string_hello, en_string_hello;
    private CloseBroadcastReceiver closeBroadcastReceiver;
    private AnimationDrawable blinkDrawable;

    private AnimationDrawable speakDrawable;
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REQUEST_ALERT_CODE:
                    if (Settings.canDrawOverlays(MainActivity.this)) {
                        showFloatBall();
                    } else {
                        handler.sendEmptyMessageDelayed(REQUEST_ALERT_CODE, 1000);
                    }
                    break;
                case CHECK_HARDWARE_VERSION_CODE:
                    LogUtils.d("checkHardwareVersion", "CHECK_HARDWARE_VERSION_CODE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    if (mPresenter != null) {
                        mPresenter.checkHardwareVersion();
                    }
                    break;
                case CHECK_HDVC:
                    LogUtils.d("checkHardwareVersion", "robotstatus = " + RobotInfoUtils.getRobotRunningStatus());
                    if (RobotInfoUtils.getRobotRunningStatus().equals("5") || RobotInfoUtils.getRobotRunningStatus().equals("1")) {
                        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                        if (cn.getClassName().equals("com.fitgreat.airfacerobot.launcher.ui.activity.MainActivity")) {
                            if (mPresenter != null) {
                                mPresenter.checkHardwareVersion();
                            }
                        } else {
                            handler.sendEmptyMessageDelayed(CHECK_HDVC, 60000);
                        }
                    } else {
                        handler.sendEmptyMessageDelayed(CHECK_HDVC, 60000);
                    }
                    break;
                case DownloadUtils.DOWNLOAD_FAILED: //TODO 硬件下载失败
                    if (downloadingDialog != null && downloadingDialog.isShowing()) {
                        downloadingDialog.dismiss();
                    }
                    if (mPresenter != null) {
                        mPresenter.commitHardwareUpdateResult(msg.getData().getString("stepId"), "false", "硬件版本下载失败");
                    }
                    //硬件版本下载失败
                    OperationUtils.saveSpecialLog("ROSDownloadFail", "硬件版本下载失败");
                    ToastUtils.showSmallToast("硬件下载失败");
                    upgrade_success = false;
                    break;
                case DownloadUtils.DOWNLOAD_SUCCESS:  //TODO 硬件下载成功
                    if (fStepBean == null || msg.obj == null) {
                        if (downloadingDialog != null && downloadingDialog.isShowing()) {
                            downloadingDialog.dismiss();
                        }
                        return;
                    }
                    String filePath = msg.obj.toString();
                    LogUtils.d(TAG, "DOWNLOAD_SUCCESS:" + filePath);
                    handler.postDelayed(() -> {
                        SignalDataEvent signalDataEvent = new SignalDataEvent();
                        signalDataEvent.setType(RobotConfig.ROS_UPDATE_STATUS);
                        signalDataEvent.setContainer(fStepBean.getF_FileName());
                        signalDataEvent.setAction(fStepBean.getF_Command());
                        signalDataEvent.setStep_id(fStepBean.getF_StepId());
                        signalDataEvent.setRos_step(String.valueOf(currentStep));
                        signalDataEvent.setFileUrl(filePath);
                        signalDataEvent.setOperationType("transFile");
                        EventBus.getDefault().post(signalDataEvent);
                    }, 1000);
                    break;
                case DownloadUtils.DOWNLOADING:
                    showDownloadingDialog(msg.arg1);
                    break;
                case CHECK_APP_VERSION_CODE:  //检查app软件升级
                    mPresenter.checkSoftwareVersion(MainActivity.this);
                    break;
                case CONTENT_SLIDE_TAG:  //对话框内容滑动
                    if (mVoiceMsg.getHeight() == minDialogBoxHeight) {
                        mVoiceMsgScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    } else {
                        scrollPositionY = scrollPositionX + 1;
                        if (scrollPositionY > mVoiceMsg.getHeight()) {
                            mVoiceMsgScrollView.fullScroll(ScrollView.FOCUS_UP);
                            scrollPositionX = minDialogBoxHeight;
                            scrollPositionY = 0;
                        } else {
                            mVoiceMsgScrollView.smoothScrollTo(scrollPositionX, scrollPositionY);
                            scrollPositionX = scrollPositionY;
                        }
                        handler.sendEmptyMessageDelayed(CONTENT_SLIDE_TAG, 1000);
                    }
                    break;
                case START_BLINK_ANIMATION_TAG:  //眨眼动画连续播放
                    //移除机器人说话动画消息
                    if (handler.hasMessages(START_SPEAK_ANIMATION_TAG)) {
                        handler.removeMessages(START_SPEAK_ANIMATION_TAG);
                    }
                    //终止机器人说话动画
                    if (speakDrawable != null && speakDrawable.isRunning()) {
                        speakDrawable.stop();
                    }
                    //启动机器人说话动画
                    mWinkSpeakAnimation.setBackgroundResource(R.drawable.blink_animation);
                    blinkDrawable = (AnimationDrawable) mWinkSpeakAnimation.getBackground();
                    if (blinkDrawable.isRunning()) {
                        blinkDrawable.stop();
                    }
                    blinkDrawable.start();
                    //眨眼动画暂停5秒后在播放
                    handler.sendEmptyMessageDelayed(START_BLINK_ANIMATION_TAG, 5 * 1000);
                    break;
                case START_SPEAK_ANIMATION_TAG:  //说话动画启动
                    //移除机器人眨眼动画消息
                    if (handler.hasMessages(START_BLINK_ANIMATION_TAG)) {
                        handler.removeMessages(START_BLINK_ANIMATION_TAG);
                    }
                    //终止机器人眨眼动画
                    if (blinkDrawable != null && blinkDrawable.isRunning()) {
                        blinkDrawable.stop();
                    }
                    mWinkSpeakAnimation.setBackgroundResource(R.drawable.speak_animation);
                    speakDrawable = (AnimationDrawable) mWinkSpeakAnimation.getBackground();
                    speakDrawable.start();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public int getLayoutResource() {
        return R.layout.activity_main_sy;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void initData() {
        //空闲操作工作流启动标志默认没启动
        SpUtils.putBoolean(MyApp.getContext(), FREE_OPERATION_STATE_TAG, false);
        //首页是否有弹窗弹出
        SpUtils.putBoolean(MyApp.getContext(), MAIN_PAGE_DIALOG_SHOW_TAG, false);
        //注册EventBus
        EventBus.getDefault().register(this);
        //信号格默认显示满信号
        mSignalImg.setImageLevel(4);
        //第一次安装语言默认为中文
        currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "zh");
        SpUtils.putBoolean(getContext(), INIT_ROS_KEY_TAG, false);
        SpUtils.putBoolean(getContext(), "isLock", true);
        commonTipDialog = new CommonTipDialog(this);
        downloadingDialog = new DownloadingDialog(this);
        showFloatBall();
        requestSettingPermission();
        warnningDialog = new WarnningDialog(this);
        //院内介绍工作流启动标志默认为false
        SpUtils.putBoolean(MyApp.getContext(), START_INTRODUCTION_WORK_FLOW_TAG, false);
        //自动回充工作流启动标志默认为false
        SpUtils.putBoolean(MyApp.getContext(), AUTOMATIC_RECHARGE_TAG, false);
        //设置全屏隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 网络断开跳转初始化页面
     */
    @Override
    public void disconnectNetWork() {
        RouteUtils.goToActivity(MainActivity.this, RobotInitActivity.class);
        //机器人状态切换为停机离线状态
        RobotInfoUtils.setRobotRunningStatus("0");
        //释放sdk,需要重新初始化dds服务
        if (SpeechManager.isDdsInitialization()) {
            //DDS需要重新初始化
            SpeechManager.instance(this).restoreToDo();
        }
        finish();
    }

    @Override
    public void disconnectRos() {
        RouteUtils.goToActivity(MainActivity.this, RobotInitActivity.class);
        //机器人状态不在视频中时,切换为离线
        if (!RobotInfoUtils.getRobotRunningStatus().equals("4")) {
            RobotInfoUtils.setRobotRunningStatus("0");
        }
        //释放sdk,需要重新初始化dds服务
        if (SpeechManager.isDdsInitialization()) {
            //DDS需要重新初始化
            SpeechManager.instance(this).restoreToDo();
        }
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestSettingPermission() {
        if (!Settings.System.canWrite(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this,
                    android.R.style.Theme_Material_Light_Dialog_Alert);
            builder.setTitle("请开启修改系统设置的权限");
            builder.setMessage("请点击允许开启");
            // 拒绝, 无法修改
            builder.setNegativeButton("拒绝",
                    (dialog, which) -> {
                        Toast.makeText(MainActivity.this,
                                "您已拒绝修系统设置权限", Toast.LENGTH_SHORT)
                                .show();
                    });
            builder.setPositiveButton("去开启",
                    (dialog, which) -> {
                        // 打开允许修改Setting 权限的界面
                        Intent intent = new Intent(
                                Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri
                                .parse("package:"
                                        + getPackageName()));
                        startActivityForResult(intent,
                                REQUEST_CODE_WRITE_SETTINGS);
                    });
            builder.setCancelable(false);
            builder.show();
        }
    }

    private void requestAlertWindowPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + this.getPackageName()));
        startActivityForResult(intent, REQUEST_ALERT_CODE);
        if (handler.hasMessages(REQUEST_ALERT_CODE)) {
            handler.removeMessages(REQUEST_ALERT_CODE);
        }
        handler.sendEmptyMessageDelayed(REQUEST_ALERT_CODE, 1000);
    }


    @Override
    protected void onResume() {
        super.onResume();
        isResume = true;
        //首页是否显示标志
        SpUtils.putBoolean(MyApp.getContext(), MAIN_PAGE_WHETHER_SHOW, true);
        //切换当前机器显示语言选择状态
        currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "zh");
        if (currentLanguage.equals("en")) {
            mLanguageEnglishRelativeLayout.setSelected(true);
            mLanguageChineseRelativeLayout.setSelected(false);
        } else if (currentLanguage.equals("zh")) {
            mLanguageChineseRelativeLayout.setSelected(true);
            mLanguageEnglishRelativeLayout.setSelected(false);
        }
        //滑动首页文本框
        if (mVoiceMsg.getHeight() > minDialogBoxHeight) {
            scrollPositionX = 0;
//            scrollPositionY = mVoiceMsg.getHeight();
            handler.sendEmptyMessageDelayed(CONTENT_SLIDE_TAG, 4000);
        }
        //更新4g网络信号页面展示信息
        mPresenter.getNetSignalLevel(this);
        //机器人名字显示
        robotInfoData = RobotInfoUtils.getRobotInfo();
        if (robotInfoData != null) {
            //更新本地导航位置,执行任务信息
            mPresenter.getLocationInfo(handler);
            //机器人名字信息
            if (currentLanguage.equals("en") && !TextUtils.isEmpty(robotInfoData.getF_EName())) {
                mRobotName.setText(robotInfoData.getF_EName());
            } else if (currentLanguage.equals("zh") && !TextUtils.isEmpty(robotInfoData.getF_Name())) {
                mRobotName.setText(robotInfoData.getF_Name());
            }
            LogUtils.json(DEFAULT_LOG_TAG, "robotInfoData:::" + JSON.toJSONString(robotInfoData));
            if (SpeechManager.isDdsInitialization()) {
                //dds对话Observer注册
                EventBus.getDefault().post(new ActionDdsEvent(DDS_OBSERVER_REGISTERED, ""));
            }
            //启动语音唤醒,打开one shot模式
            EventBus.getDefault().post(new ActionDdsEvent(START_DDS_WAKE_TAG, ""));
            //检查app软件更新
            boolean checkUpdateTag = SpUtils.getBoolean(MyApp.getContext(), "checkUpdateTag", false);
            LogUtils.d(DEFAULT_LOG_TAG, "onResume:checkAppVersion   " + checkUpdateTag);
            if (checkUpdateTag) {
                SpUtils.putBoolean(MyApp.getContext(), "checkUpdateTag", false);
                mPresenter.checkSoftwareVersion(this);
            }
            //启动说话动画
            handler.sendEmptyMessage(START_BLINK_ANIMATION_TAG);
            //空闲操作计时器
            boolean freeOperationStartTag = SpUtils.getBoolean(MyApp.getContext(), FREE_OPERATION_STATE_TAG, false);
            LogUtils.d(DEFAULT_LOG_TWO, "onResume: startFreeOperation   freeOperationStartTag= :  " + freeOperationStartTag + ",  机器人状态=, " + RobotInfoUtils.getRobotRunningStatus());
            if (!freeOperationStartTag) {
                startFreeOperation();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResume = false;
        //首页是否显示标志
        SpUtils.putBoolean(MyApp.getContext(), MAIN_PAGE_WHETHER_SHOW, false);
        //首页是否有弹窗弹出
        SpUtils.putBoolean(MyApp.getContext(), MAIN_PAGE_DIALOG_SHOW_TAG, false);
        //关闭语音唤醒,关闭one shot模式
        EventBus.getDefault().post(new ActionDdsEvent(CLOSE_DDS_WAKE_TAG, ""));
        //关闭dds语音播报
        EventBus.getDefault().post(new ActionDdsEvent(DDS_VOICE_TEXT_CANCEL, ""));
        SpeechManager.closeOneShotWakeup();
        //停止三分钟倒计时启动院内介绍工作流
        stopIntroductionTimer();
        //去除对话框内容滑动
        handler.removeMessages(CONTENT_SLIDE_TAG);
        //自动回充加载提示弹窗关闭
        if (rechargeTipDialog != null) {
            rechargeTipDialog.dismiss();
            rechargeTipDialog = null;
        }
        handler.removeCallbacksAndMessages(null);
    }


    @Override
    protected void onStop() {
        super.onStop();
        isResume = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //退出首页DDS服务重置,再次启动首页需要充新初始化DDS
        if (SpeechManager.isDdsInitialization()) {
            EventBus.getDefault().post(new InitUiEvent(RobotConfig.INIT_TYPE_VOICE, "stop"));
        }
        EventBus.getDefault().unregister(this);
        if (floatWindow != null) {
            floatWindow.remove();
            floatWindow = null;
        }
        //应用退出时执行以下操作
        String[] kill = {"su", "-c", "am force-stop " + getPackageName()};
        ShellCmdUtils.execCmd(kill);
        System.gc();
        LogUtils.d(TAG, "onDestroy:=>");
        //首页销毁时,机器人状态为空闲
        RobotInfoUtils.setRobotRunningStatus("1");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_ALERT_CODE == requestCode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                LogUtils.d(TAG, "onActivityResult:" + resultCode);
                if (!Settings.canDrawOverlays(this)) {
                    requestAlertWindowPermission();
                }
            }
        }
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Settings.System.canWrite方法检测授权结果
                if (Settings.System.canWrite(getApplicationContext())) {
                    // 5.调用修改Settings屏幕亮度的方法 屏幕亮度值 200
                } else {
                    ToastUtils.showSmallToast("您已拒绝修系统Setting的屏幕亮度权限");
                }
            }
        }
    }

    @OnClick({R.id.bt_home_setting, R.id.constraintLayout_me_want_go, R.id.constraintLayout_common_problem, R.id.constraintLayout_hospital_introduction, R.id.language_chinese_relativeLayout, R.id.language_english_relativeLayout, R.id.auto_recharge_bt})
    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.bt_home_setting: //跳转设置模块
                jumpSystemSettingModel();
                break;
            case R.id.constraintLayout_me_want_go: //我要去
                goToChoseDestinationModel();
                break;
            case R.id.constraintLayout_common_problem: //常见问题
                jumpCommonProblemModel();
                break;
            case R.id.constraintLayout_hospital_introduction: //院内介绍
                jumpIntroductionListModel();
                break;
            case R.id.language_chinese_relativeLayout: //机器人中英文切换
            case R.id.language_english_relativeLayout:
                changeRobotLanguage();
                break;
            case R.id.auto_recharge_bt: //自动回充按钮
                rechargeToDo();
                break;
            default:
                break;
        }
    }


    @Override
    public void validationPassword(String password) {
        mPresenter.verifyPassword(password);
    }

    @Override
    public void verifyFailure() {
        runOnUiThread(() -> {
            validationOrPromptDialog = new ValidationOrPromptDialog(this);
            validationOrPromptDialog.setFailPrompt(true);
            validationOrPromptDialog.show();
            //首页是否有弹窗弹出
            SpUtils.putBoolean(MyApp.getContext(), MAIN_PAGE_DIALOG_SHOW_TAG, true);
        });
    }

    @Override
    public void verifySuccess() {
        //首页是否有弹窗弹出
        SpUtils.putBoolean(MyApp.getContext(), MAIN_PAGE_DIALOG_SHOW_TAG, false);
        RouteUtils.goToActivity(getContext(), SettingActivity.class);
    }

    /**
     * 空闲操作计时器启动   TODO
     */
    public void startFreeOperation() {
        //启动空闲时3分钟计时,3分钟计时结束后启动院内介绍工作流
        introductionTimer = new Timer();
        introductionTimerTask = new TimerTask() {
            @Override
            public void run() {
//                LogUtils.d(DEFAULT_LOG_TWO, "空闲操作工作流启动倒计时 :  " + freeOperationCountdown);
                freeOperationCountdown++;
                if (freeOperationCountdown == 300) {
                    freeOperationCountdown = 0;
                    String currentFreeOperation = SpUtils.getString(MyApp.getContext(), CURRENT_FREE_OPERATION, "null");
                    if (!"null".equals(currentFreeOperation)) {
                        WorkflowEntity workflowEntity = JSON.parseObject(currentFreeOperation, WorkflowEntity.class);
                        //首页是否有弹窗
                        LogUtils.d(DEFAULT_LOG_TWO, "空闲时启动操作工作流 freeOperationCountdown=" + freeOperationCountdown + ",  机器人状态, " + RobotInfoUtils.getRobotRunningStatus() + "   " + JSON.toJSONString(workflowEntity));
                        if (workflowEntity.getF_Name().equals("自动回充") && RobotInfoUtils.getRobotRunningStatus().equals("5")) {   //空闲时执行工作流为自动回充,机器人状态为冲电中时不执行该工作流
                            return;
                        } else if (RobotInfoUtils.getRobotRunningStatus().equals("1")) { //首页没有弹窗时执行空闲操作
                            setRobotModelCotrol();
                            if (workflowEntity.getF_Name().equals("自动回充")) {
                                //开启自动回充工作流
                                OperationUtils.startSpecialWorkFlow(1, handler);
                            } else {
                                OperationUtils.startActivity(workflowEntity, RobotInfoUtils.getRobotInfo(), 0);
                            }
                            SpUtils.putBoolean(MyApp.getContext(), FREE_OPERATION_STATE_TAG, true);
                            stopIntroductionTimer();
                        }
                    } else {
                        LogUtils.d(DEFAULT_LOG_TWO, "请配置空闲执行工作流 : ");
                    }
                }
            }
        };
        boolean freeOperationStartTag = SpUtils.getBoolean(MyApp.getContext(), FREE_OPERATION_STATE_TAG, false);
        boolean mainPageShowTag = SpUtils.getBoolean(MyApp.getContext(), MAIN_PAGE_WHETHER_SHOW, false);
        LogUtils.d(DEFAULT_LOG_TWO, "首页无操作 空闲操作计时器开始:  mainPageShowTag   " + mainPageShowTag + "----!freeOperationStartTag----" + !freeOperationStartTag + ",  机器人状态 , " + RobotInfoUtils.getRobotRunningStatus());
        if (mainPageShowTag && (!freeOperationStartTag)) {
            LogUtils.d(DEFAULT_LOG_TWO, "空闲时操作计时器启动 :  " + freeOperationCountdown);
            introductionTimer.schedule(introductionTimerTask, 0, 1000);
        }
    }

    /**
     * 切换机器人显示语言
     */
    public void setLanguage(String language, RelativeLayout selectView, RelativeLayout normalButtonView) {
        SpUtils.putString(MyApp.getContext(), CURRENT_LANGUAGE, language);
        selectView.setSelected(true);
        normalButtonView.setSelected(false);
        LanguageUtil.changeAppLanguage(MainActivity.this);
        clearActivity();
        //重启app
        RouteUtils.goHome(MainActivity.this);
    }

    /**
     * 弹窗提示切换机器人语言
     */
    public void changeRobotLanguage() {
        normalOrCountDownDialog = new NormalOrCountDownDialog(this);
        normalOrCountDownDialog.setDialogTitle("提示/Messages");
        normalOrCountDownDialog.setDialogContent("应用设置将重新启动机器人!\n Applying This Setting Will Restart Your Robot!");
        normalOrCountDownDialog.setTipDialogYesNoListener("继续/Continue", "取消/Cancel", new NormalOrCountDownDialog.TipDialogYesNoListener() {
            @Override
            public void tipProgressChoseYes() {
                //首页是否有弹窗弹出
                SpUtils.putBoolean(MyApp.getContext(), MAIN_PAGE_DIALOG_SHOW_TAG, false);
                if (currentLanguage.equals("zh")) {
                    setLanguage("en", mLanguageEnglishRelativeLayout, mLanguageChineseRelativeLayout);
                } else {
                    setLanguage("zh", mLanguageChineseRelativeLayout, mLanguageEnglishRelativeLayout);
                }
            }

            @Override
            public void tipProgressChoseNo() {

            }

            @Override
            public void endOfCountdown() {

            }
        });
        normalOrCountDownDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        normalOrCountDownDialog.show();
        //首页是否有弹窗弹出
        SpUtils.putBoolean(MyApp.getContext(), MAIN_PAGE_DIALOG_SHOW_TAG, true);
    }

    /**
     * 首页按钮自动化回充
     */
    private void rechargeToDo() {
        //如果急停按钮被按下语音提示
        boolean emergencyTag = SpUtils.getBoolean(MyApp.getContext(), CLICK_EMERGENCY_TAG, false);
        if (emergencyTag) {
            EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, MvpBaseActivity.getActivityContext().getString(R.string.emergency_click_recharge_tip)));
            return;
        }
        if (RobotInfoUtils.getRobotRunningStatus().equals("5")) {
            EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, MvpBaseActivity.getActivityContext().getString(R.string.charging_tip)));
            return;
        }
        setRobotModelCotrol();
        //自动回充提示弹窗关闭监听
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CLOSE_RECHARGING_TIP_DIALOG);
        closeBroadcastReceiver = new CloseBroadcastReceiver();
        registerReceiver(closeBroadcastReceiver, intentFilter);
        //自动回充提示弹窗弹出前,加载提示弹窗
        rechargeTipDialog = new MyTipDialog(this);
        rechargeTipDialog.setDialogTitle(MvpBaseActivity.getActivityContext().getString(R.string.loading_title));
        rechargeTipDialog.setTipLoadModel(true);
        rechargeTipDialog.show();
        //关闭dds语音播报
        EventBus.getDefault().post(new ActionDdsEvent(DDS_VOICE_TEXT_CANCEL, ""));
        //开启自动回充工作流
        OperationUtils.startSpecialWorkFlow(1, handler);
    }

    /**
     * 设置机器人状态为锁轴
     */
    private void setRobotModelCotrol() {
        //机器人工作模式状态是否为控制模式
        boolean isControlModel = SpUtils.getBoolean(MyApp.getContext(), IS_CONTROL_MODEL, false);
        if (!isControlModel) { //当前不为控制模式则切换为控制模式
            SpUtils.putBoolean(MyApp.getContext(), IS_CONTROL_MODEL, true);
            //机器人切换为控制模式
            SignalDataEvent moveMode = new SignalDataEvent(RobotConfig.MSG_CHANGE_POWER_LOCK, "");
            moveMode.setPowerlock(1);
            EventBus.getDefault().post(moveMode);
        }
    }

    private class CloseBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.d(TAG, "CloseBroadcastReceiver");
            if (intent.getAction().equals(CLOSE_RECHARGING_TIP_DIALOG)) {
                LogUtils.d(TAG, "关闭自动回充工作流加载提示框");
                if (rechargeTipDialog != null) {
                    rechargeTipDialog.dismiss();
                    rechargeTipDialog = null;
                }
            }
        }
    }

    /**
     * 电量低于20%时提示
     */
    private void lowBatteryPrompt() {
        if (isResume) {
            if (lowBatteryTipDialog == null) {
                lowBatteryTipDialog = new NormalOrCountDownDialog(this);
            }
            lowBatteryTipDialog.setDialogTitle(MvpBaseActivity.getActivityContext().getString(R.string.start_chose_destination_dialog_title));
            lowBatteryTipDialog.setDialogContent(MvpBaseActivity.getActivityContext().getString(R.string.battery_low_tip));
            lowBatteryTipDialog.setCountDownModel(true, handler);
            lowBatteryTipDialog.setTipDialogYesNoListener(MvpBaseActivity.getActivityContext().getString(R.string.sure_bt_text),
                    MvpBaseActivity.getActivityContext().getString(R.string.negative),
                    new NormalOrCountDownDialog.TipDialogYesNoListener() {
                        @Override
                        public void tipProgressChoseYes() { //启动自动回充工作流
                            setRobotModelCotrol();
                            OperationUtils.startSpecialWorkFlow(1, handler);
                            lowBatteryTipDialog = null;
                            //首页是否有弹窗弹出
                            SpUtils.putBoolean(MyApp.getContext(), MAIN_PAGE_DIALOG_SHOW_TAG, false);
                        }

                        @Override
                        public void tipProgressChoseNo() {
                            //首页是否有弹窗弹出
                            SpUtils.putBoolean(MyApp.getContext(), MAIN_PAGE_DIALOG_SHOW_TAG, false);
                            String rechargeActivityId = SpUtils.getString(MyApp.getContext(), AUTOMATIC_RECHARGE_ACTIVITY_ID, null);
                            if (!TextUtils.isEmpty(rechargeActivityId)) { //自动回充工作流已启动,终止自动回充工作流
                                SignalDataEvent instruct = new SignalDataEvent();
                                instruct.setType(MSG_INSTRUCTION_STATUS_FINISHED);
                                instruct.setInstructionId(rechargeActivityId);
                                instruct.setAction("-1");
                                EventBus.getDefault().post(instruct);
                            }
                            lowBatteryTipDialog = null;
                        }

                        @Override
                        public void endOfCountdown() {
                            setRobotModelCotrol();
                            OperationUtils.startSpecialWorkFlow(1, handler);
                            lowBatteryTipDialog = null;
                            //首页是否有弹窗弹出
                            SpUtils.putBoolean(MyApp.getContext(), MAIN_PAGE_DIALOG_SHOW_TAG, false);
                        }
                    });
            lowBatteryTipDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            if (lowBatteryTipDialog != null && (!downloadingDialog.isShowing())) {
                lowBatteryTipDialog.show();
                //首页是否有弹窗弹出
                SpUtils.putBoolean(MyApp.getContext(), MAIN_PAGE_DIALOG_SHOW_TAG, true);
            }
        }
    }

    /**
     * 跳转软件设置页面
     */
    public void jumpSystemSettingModel() {
        validationOrPromptDialog = new ValidationOrPromptDialog(this);
        validationOrPromptDialog.show();
        validationOrPromptDialog.setValidationFailListener(this);
        //首页是否有弹窗弹出
        SpUtils.putBoolean(MyApp.getContext(), MAIN_PAGE_DIALOG_SHOW_TAG, true);
        //                RouteUtils.goToActivity(getContext(), SettingActivity.class);
    }

    /**
     * 跳转院内介绍列表汇总页面
     */
    public void jumpIntroductionListModel() {
        RouteUtils.goToActivity(getContext(), IntroductionListActivity.class);
        //暂停空闲操作首页计时器
        stopIntroductionTimer();
    }

    /**
     * 跳转常见问题模块
     */
    public void jumpCommonProblemModel() {
        RouteUtils.goToActivity(getContext(), CommonProblemActivity.class);
        //暂停空闲操作首页计时器
        stopIntroductionTimer();
    }

    /**
     * 跳转我要去模块
     */
    private void goToChoseDestinationModel() {
        //解析获取地图信息
        String mapInfoString = SpUtils.getString(MyApp.getContext(), MAP_INFO_CASH, null);
        MapEntity mapEntity = JSON.parseObject(mapInfoString, MapEntity.class);
        if (TextUtils.isEmpty(mapEntity.getF_EMapUrl()) && currentLanguage.equals("en")) {
            ToastUtils.showSmallToast(MvpBaseActivity.getActivityContext().getString(R.string.no_english_map_tip));
            return;
        }
        if (TextUtils.isEmpty(mapEntity.getF_MapFileUrl()) && currentLanguage.equals("zh")) {
            ToastUtils.showSmallToast("请配置中文地图");
            return;
        }
        //下载最新中英文地图
        OperationUtils.downLoadMap(mapEntity);
        //添加加载提示框
        rechargeTipDialog = new MyTipDialog(this);
        rechargeTipDialog.setDialogTitle(MvpBaseActivity.getActivityContext().getString(R.string.loading_title));
        rechargeTipDialog.setTipLoadModel(true);
        rechargeTipDialog.show();
        handler.postDelayed(() -> {
            rechargeTipDialog.dismiss();
            rechargeTipDialog = null;
            RouteUtils.goToActivity(getContext(), ChoseDestinationActivity.class);
        }, 3 * 1000);
        //暂停空闲操作首页计时器
        stopIntroductionTimer();
    }

    /**
     * 取消循环院内介绍计时器
     */
    private void stopIntroductionTimer() {
        LogUtils.d(DEFAULT_LOG_TWO, "首页无操作 计时器取消: introductionTimer是否为空   " + (introductionTimer != null));
        if (introductionTimer != null) {
            freeOperationCountdown = 0;
            //取消无操作计时器计时
            introductionTimer.cancel();
            introductionTimer = null;
        }
    }


    /**
     * 显示悬浮球菜单
     */
    private void showFloatBall() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //是否拥有悬浮窗显示权限
            LogUtils.d(TAG, "hasPermission:" + Settings.canDrawOverlays(this));
            if (Settings.canDrawOverlays(this)) {
                if (floatWindow == null) {
                    ImageView floatBall = new ImageView(this);
                    floatBall.setBackgroundResource(R.drawable.float_icon_bg);
                    floatBall.setImageResource(R.drawable.ic_float);
                    floatBall.setOnClickListener(new DelayOnClickListener() {
                        @Override
                        public void onDelayClick(View view) {
                            LogUtils.d(TAG, "----ON HOME CLICK-----" + isResume);
                            if (!isResume) {
                                LogUtils.d("AirFaceApp", "-----------showFloatBall:-----isResume--" + isResume);
                                RouteUtils.goHome(MainActivity.this);
                            }
                        }
                    });
                    floatWindow = new FloatWindow(this, "float", floatBall, FloatWindowOption.create(new FloatWindowOption.Builder()
                            .setX(UIUtils.getPointFromScreenWidthRatio(this, 0.0f))
                            .setY(UIUtils.getPointFromScreenHeightRatio(this, 0.8f))
                            .desktopShow(true)
                            .setFloatMoveType(FloatWindow.FloatMoveEnum.INACTIVE)
                            .setDuration(250)
                            .setBoundOffset(UIUtils.dp2px(getApplicationContext(), 0))
                            .setViewStateCallback(new FloatWindowViewStateCallback() {
                                @Override
                                public void onPositionUpdate(int oldX, int oldY, int newX, int newY) {

                                }

                                @Override
                                public void onShow() {
                                    if (floatBall.getAlpha() != AccessibilityConstant.Config.ALPHA_HIDDEN) {
                                        floatBall.setAlpha(AccessibilityConstant.Config.ALPHA_HIDDEN);
                                    }
                                }

                                @Override
                                public void onHide() {

                                }

                                @Override
                                public void onRemove() {

                                }

                                @Override
                                public boolean isCanLongPress() {
                                    return false;
                                }

                                @Override
                                public void onLongPress() {

                                }

                                @Override
                                public void onPrepareDrag() {
                                    if (floatBall.getAlpha() != AccessibilityConstant.Config.ALPHA_SHOW) {
                                        floatBall.setAlpha(AccessibilityConstant.Config.ALPHA_SHOW);
                                    }
                                }

                                @Override
                                public void onDragging(float moveX, float moveY) {

                                }

                                @Override
                                public void onDragFinish() {
                                    if (floatBall.getAlpha() != AccessibilityConstant.Config.ALPHA_HIDDEN) {
                                        floatBall.setAlpha(AccessibilityConstant.Config.ALPHA_HIDDEN);
                                    }
                                }

                                @Override
                                public boolean isCanDrag(float moveX, float moveY) {
                                    return true;
                                }

                                @Override
                                public void onMoveAnimStart() {

                                }

                                @Override
                                public void onMoveAnimEnd() {
                                    if (floatBall.getAlpha() != AccessibilityConstant.Config.ALPHA_HIDDEN) {
                                        floatBall.setAlpha(AccessibilityConstant.Config.ALPHA_HIDDEN);
                                    }
                                }

                                @Override
                                public void onBackToDesktop() {

                                }

                                @Override
                                public void onClickFloatOutsideArea(float x, float y) {

                                }
                            })));
                    LogUtils.d(TAG, "----create float window-----");
                } else if (floatWindow != null && !floatWindow.isShowing()) {
                    floatWindow.show();
                    LogUtils.d(TAG, "----show float----");
                }
                jumpCheckSelf();
            } else {
                requestAlertWindowPermission();
            }
        }
    }

    /**
     * 跳转自检初始化界面
     */
    private void jumpCheckSelf() {
        if (handler.hasMessages(REQUEST_ALERT_CODE)) {
            handler.removeMessages(REQUEST_ALERT_CODE);
        }
        RouteUtils.goToActivity(MainActivity.this, RobotInitActivity.class);
    }

    public MainActivity getContext() {
        return MainActivity.this;
    }


    @Override
    public MainPresenter createPresenter() {
        return new MainPresenter();
    }

    @Override
    public void onBackPressed() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(InitEvent initEvent) {
        LogUtils.d(TAG, "initEvent:" + JsonUtils.encode(initEvent));
        switch (initEvent.type) {
            case RobotConfig.TYPE_SHOW_TIPS:
                if (isResume) {
                    showDialogTip(initEvent.action, initEvent.extra);
                }
                break;
            case MSG_CHANGE_FLOATING_BALL:
                if (floatWindow == null) {
                    return;
                }
                if (initEvent.isHideFloatBall()) {
                    floatWindow.hide();
                } else {
                    floatWindow.show();
                }
                break;
            case TYPE_CHECK_STATE_DONE:
                if (hdvScheduledFuture != null) {
                    ExecutorManager.getInstance().cancelScheduledTask(hdvScheduledFuture);
                    hdvScheduledFuture = null;
                }
                hdvScheduledFuture = ExecutorManager.getInstance().executeScheduledTask(new Runnable() {
                    @Override
                    public void run() {
                        handler.sendEmptyMessageDelayed(CHECK_HDVC, 10000);
                    }
                }, 0, 4, TimeUnit.HOURS);
                LogUtils.d("ActionEvent", "TYPE_CHECK_STATE_DONE");
                break;
            case MSG_LIGHT_ON:
                if (warnningDialog != null) {
                    if (!warnningDialog.isShowing()) {
                        warnningDialog.show();
                    }
                }
                break;
            case MSG_LIGHT_OFF:
                if (warnningDialog != null) {
                    if (warnningDialog.isShowing()) {
                        warnningDialog.dismiss();
                    }
                }
                break;
            case PROMPT_ROBOT_RECHARGE: //电量低于20%时弹窗提示
                lowBatteryPrompt();
                break;
            case START_SPEAK_ANIMATION_MSG: //启动说话动画
                handler.sendEmptyMessage(START_SPEAK_ANIMATION_TAG);
                break;
            case START_BLINK_ANIMATION_MSG: //启动眨眼动画
                handler.sendEmptyMessage(START_BLINK_ANIMATION_TAG);
                break;
            case START_FREE_OPERATION_MSG: //
                startFreeOperation();
                break;
            default:
                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(DaemonEvent daemonEvent) {
        LogUtils.d(TAG, "daemonEvent:" + JsonUtils.encode(daemonEvent));
        switch (daemonEvent.type) {
            case RobotConfig.TYPE_SHOW_VERSION_STATUS:
                switch (daemonEvent.action) {
                    case "updateFailed":
                        if (downloadingDialog != null && downloadingDialog.isShowing()) {
                            downloadingDialog.dismiss();
                        }
                        show_svdialog = false;
                        handler.sendEmptyMessage(CHECK_HARDWARE_VERSION_CODE);
                        break;
                    case "updateSuccess":
                    case "backupSuccess":
                    case "noNewVersion":
                        if (downloadingDialog != null && downloadingDialog.isShowing()) {
                            downloadingDialog.dismiss();
                        }
                        handler.sendEmptyMessage(CHECK_HARDWARE_VERSION_CODE);
                        break;
                    case "rosUpdateSuccess":
                        RobotInfoUtils.setRobotRunningStatus("1");
                        if (downloadingDialog != null && downloadingDialog.isShowing()) {
                            downloadingDialog.dismiss();
                        }
                        if (mPresenter != null) {
                            mPresenter.commitHardwareUpdateResult(daemonEvent.getStepId(), "true", TextUtils.isEmpty(daemonEvent.extra) ? "硬件升级失败" : daemonEvent.extra);
                        }
                        ToastUtils.showSmallToast("硬件升级步骤" + daemonEvent.getStep() + "完成！");
                        break;
                    case "rosUpdateFailed":
                        RobotInfoUtils.setRobotRunningStatus("1");
                        if (downloadingDialog != null && downloadingDialog.isShowing()) {
                            downloadingDialog.dismiss();
                        }
                        LogUtils.e(TAG, "reason:" + daemonEvent.extra);
                        if (mPresenter != null) {
                            mPresenter.commitHardwareUpdateResult(daemonEvent.getStepId(), "false", TextUtils.isEmpty(daemonEvent.extra) ? "硬件升级失败" : daemonEvent.extra);
                        }
                        //ros安装失败
                        OperationUtils.saveSpecialLog("ROSInstallFail", daemonEvent.extra);
                        ToastUtils.showSmallToast("硬件升级失败");
                        upgrade_success = false;
                        break;
                    default:
                        break;
                }
                break;
            case MSG_ROS_NEXT_STEP:
                excNextStep(rosUpdateStep);
                break;
            default:
                break;
        }
    }

    /**
     * 移动信号强度变化更新
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(RobotSignalEvent robotSignalEvent) {
        LogUtils.d(TAG, "robotSignalEvent:" + robotSignalEvent.type);
        switch (robotSignalEvent.type) {
            case RobotConfig.ROBOT_SIM_SIGNAL:
                if (mSignalImg != null) {
//                    int level = Integer.parseInt(robotSignalEvent.action);
                    mSignalImg.setImageLevel(4);
                }
                break;
            case RobotConfig.ROS_MSG_BATTERY: //TODO 机器人电量页面显示
                if (mTextBattery != null && isResume) {
                    int battery = Math.round(robotSignalEvent.getBattery());
                    if (robotSignalEvent.isPowerStatus()) { //机器人充电中
                        SpUtils.putBoolean(getContext(), "isCharge", true);
                        if (SpUtils.getBoolean(getContext(), "isVideocall", false)) {  //机器人视频中
                            saveRobotstatus(4);
                        } else {
                            //"执行操作中状态" 大于 "冲电状态"  "升级状态",机器充电时并且收到宣教/播放类任务时显示状态"执行操作中状态"
                            if (!RobotInfoUtils.getRobotRunningStatus().equals("3") && !RobotInfoUtils.getRobotRunningStatus().equals("6")) {
                                saveRobotstatus(5);
                            }
                        }
                        mBatteryImg.setImageLevel(5);
                    } else { //机器人没有充电
                        SpUtils.putBoolean(getContext(), "isCharge", false);
                        if (SpUtils.getBoolean(getContext(), "isVideocall", false)) {
                            saveRobotstatus(4);
                        } else {
                            if (!RobotInfoUtils.getRobotRunningStatus().equals("3") && !RobotInfoUtils.getRobotRunningStatus().equals("6")) {
                                saveRobotstatus(1);
                            }
                            if (!RobotInfoUtils.getRobotRunningStatus().equals("3") && !RobotInfoUtils.getRobotRunningStatus().equals("6")) {  //没充电时,机器人当前状态不为"执行操作中"  "升级中" 时切换为 "空闲"
                                saveRobotstatus(1);
                            }
                        }
                        if (battery <= 20) {
                            mBatteryImg.setImageLevel(0);
                        } else if (battery <= 40) {
                            mBatteryImg.setImageLevel(1);
                        } else if (battery <= 60) {
                            mBatteryImg.setImageLevel(2);
                        } else if (battery <= 80) {
                            mBatteryImg.setImageLevel(3);
                        } else if (battery <= 100) {
                            mBatteryImg.setImageLevel(4);
                        }
                    }
                    mTextBattery.setText(battery + "%");
                }
                break;
            default:
                break;
        }
    }

    /**
     * 提示信息首页音浪上部显示更新
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(NavigationTip navigationTip) {
        mVoiceMsg.setText(navigationTip.getTip());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(CommandDataEvent commandDataEvent) {
        Intent intent = null;
        Bundle bundle = null;
        switch (commandDataEvent.getCommandType()) {
            case SINGLE_POINT_NAVIGATION:
                LogUtils.d(DEFAULT_LOG_TAG, "----SINGLE_POINT_NAVIGATION---MainActivity-----");
                if (isResume) {
                    intent = new Intent(getContext(), ChoseDestinationActivity.class);
                    bundle = new Bundle();
                    bundle.putSerializable("LocationEntity", commandDataEvent.getLocationEntity());
                    intent.putExtra("bundle", bundle);
                    startActivity(intent);
                }
                break;
            case COMMON_PROBLEM_TAG:
                LogUtils.d(DEFAULT_LOG_TAG, "----COMMON_PROBLEM_TAG---MainActivity-----" + isResume);
                if (isResume) {
                    intent = new Intent(getContext(), CommonProblemActivity.class);
                    bundle = new Bundle();
                    bundle.putSerializable("CommonProblemEntity", commandDataEvent.getCommonProblemEntity());
                    intent.putExtra("bundle", bundle);
                    startActivity(intent);
                }
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(MessageBean messageBean) {
        String voiceMessageText = messageBean.getText().trim();
        if ((messageBean.getType() == MessageBean.TYPE_INPUT) && isResume) {
            mVoiceMsg.setText(voiceMessageText);
        }
    }

    /**
     * 显示通用提示框
     */
    private void showDialogTip(String title, String content) {
        if (!isFinishing() && commonTipDialog != null) {
            if (!commonTipDialog.isShowing()) {
                commonTipDialog.show();
                commonTipDialog.setCancelVisible(false);
            }
            commonTipDialog.setTitleAndContent(title, content);
        }
    }

    /**
     * 显示版本下载进度弹框
     */
    private void showDownloadingDialog(int progress) {
        if (!isFinishing() && downloadingDialog != null) {
            if (!downloadingDialog.isShowing()) {
                downloadingDialog.show();
            }
            downloadingDialog.updateProgress(progress);
        }
    }

    /**
     * APP发现新版本弹框  TODO
     */
    public void foundAppNewVersion(AppVersion appVersion) {
        long currenttime = System.currentTimeMillis();
        handler.removeMessages(CHECK_HDVC);
        show_svdialog = true;
        if (currenttime - lasttime >= 30 * 1000 * 60) {
            if (!isFinishing() && commonTipDialog != null) {
                if (!commonTipDialog.isShowing()) {
                    //首页是否有弹窗弹出
                    SpUtils.putBoolean(MyApp.getContext(), MAIN_PAGE_DIALOG_SHOW_TAG, true);
                    commonTipDialog.show();
                    commonTipDialog.setCancelVisible(true);
                    commonTipDialog.setCancelable(false);
                    commonTipDialog.setOnDialogClick((int which) -> {
                        if (which == R.id.common_tip_ok) {
                            RobotInfoUtils.setRobotRunningStatus("6");
                            showDownloadingDialog(0);
                            LogUtils.d(DEFAULT_LOG_TAG, "开始软件下载升级: ");
                            mPresenter.downloadSoftwareInstall(this, appVersion, handler);
                        } else if (which == R.id.common_tip_cancel) {
                            show_svdialog = false;
                            handler.sendEmptyMessageDelayed(CHECK_HARDWARE_VERSION_CODE, 300);
                        }
                        //首页是否有弹窗弹出
                        SpUtils.putBoolean(MyApp.getContext(), MAIN_PAGE_DIALOG_SHOW_TAG, false);
                    });
                }
                commonTipDialog.setTitleAndContent(appVersion.getF_UpdateTitle(), appVersion.getF_UpdateMsg());
            }
            lasttime = currenttime;
        }
    }

    @Override
    public void showDownloadAppProgress(int progress) {
        showDownloadingDialog(progress);
    }

    /**
     * Hardware发现新版本弹框   发现硬件升级  TODO
     */
    private void showHardwareNewVersion(VersionInfo versionInfo) {
        upgrade_success = true;
        handler.postDelayed(() -> {
            show_hvdialog = false;
        }, 30 * 60 * 1000);
        LogUtils.d(TAG, "show_svdialog = " + show_svdialog);
        if (!show_svdialog && !show_hvdialog) {
            if (!isFinishing() && commonTipDialog != null) {
                LogUtils.d(TAG, "versionInfo.getF_UpdateTitle() = " + versionInfo.getF_UpdateTitle() + ",versionInfo.getF_UpdateContent() = " + versionInfo.getF_UpdateContent());
                if (!commonTipDialog.isShowing()) {
                    LogUtils.d(TAG, "----------show hardware version dialog--------");
                    //首页是否有弹窗弹出
                    SpUtils.putBoolean(MyApp.getContext(), MAIN_PAGE_DIALOG_SHOW_TAG, true);
                    commonTipDialog.show();
                    commonTipDialog.setCancelVisible(true);
                    commonTipDialog.setCancelable(false);
                    commonTipDialog.setOnDialogClick((int which) -> {
                        if (which == R.id.common_tip_ok) {
                            LogUtils.d(DEFAULT_LOG_TAG, "开始硬件升级" + "    " + (rosUpdateStep != null) + (!rosUpdateStep.isEmpty()));
                            if (rosUpdateStep != null && !rosUpdateStep.isEmpty()) {
                                RobotInfoUtils.setRobotRunningStatus("6");
                                excNextStep(rosUpdateStep);
                            } else {
                                RobotInfoUtils.setRobotRunningStatus("1");
                            }
                        }
                        //首页是否有弹窗弹出
                        SpUtils.putBoolean(MyApp.getContext(), MAIN_PAGE_DIALOG_SHOW_TAG, false);
                    });
                }
                commonTipDialog.setTitleAndContent(versionInfo.getF_UpdateTitle(), versionInfo.getF_UpdateContent());
            }
        }
        show_hvdialog = true;
    }

    /**
     * 执行下一步骤  TODO 硬件升级下一步
     *
     * @param updateStep
     */
    private void excNextStep(List<VersionInfo.FStepBean> updateStep) {
        currentStep = currentStep + 1;
        if (currentStep <= updateStep.size()) {
            for (int i = 0; i < updateStep.size(); i++) {
                if (updateStep.get(i).getF_Step() == currentStep) {
                    fStepBean = updateStep.get(i);
                }
            }
            if (fStepBean != null && !TextUtils.isEmpty(fStepBean.getF_Command())) {
                if (!TextUtils.isEmpty(fStepBean.getF_FileUrl())) {
                    showDownloadingDialog(0);
                    DownloadUtils.downloadApp(handler, fStepBean.getF_StepId(), fStepBean.getF_FileUrl(), DownloadUtils.DOWNLOAD_PATH + fStepBean.getF_FileName(), false, null);
                } else {
                    LogUtils.e(TAG, "----file url is empty----");
                    handler.postDelayed(() -> {
                        SignalDataEvent signalDataEvent = new SignalDataEvent();
                        signalDataEvent.setType(RobotConfig.ROS_UPDATE_STATUS);
                        signalDataEvent.setAction(fStepBean.getF_Command());
                        signalDataEvent.setStep_id(fStepBean.getF_StepId());
                        signalDataEvent.setRos_step(String.valueOf(currentStep));
                        signalDataEvent.setOperationType("command");
                        EventBus.getDefault().post(signalDataEvent);
                    }, 1000);

                }
            } else {
                if (mPresenter != null) {
                    if (fStepBean.getF_StepId() != null) {
                        mPresenter.commitHardwareUpdateResult(fStepBean.getF_StepId(), "false", "升级命令为空，无法更新");
                    }
                }
                ToastUtils.showSmallToast("升级命令为空，无法更新");
                if (downloadingDialog != null && downloadingDialog.isShowing()) {
                    downloadingDialog.dismiss();
                }
                upgrade_success = false;
            }
        } else {
            LogUtils.d(TAG, "硬件升级完成！");
            if (upgrade_success) {
                Toast.makeText(this, "硬件升级成功！", Toast.LENGTH_SHORT).show();
                runOnUiThread(() -> {
                    InitEvent initEvent = new InitEvent();
                    initEvent.setType(MSG_GETROS_VERSION);
                    EventBus.getDefault().post(initEvent);
                });
            }
        }
    }

    /**
     * 硬件升级检查  TODO
     *
     * @param versionInfo
     */
    @Override
    public void foundHardwareNewVersion(VersionInfo versionInfo) {
        LogUtils.d(TAG, "------foundHardwareNewVersion-------");
        rosUpdateStep = versionInfo.getF_Step();
        rosSoftId = versionInfo.getF_Id();
        LogUtils.d(TAG, "show_svdialog = " + show_svdialog);
        if (!show_svdialog) {
            showHardwareNewVersion(versionInfo);
        }
    }

    @Override
    public void getLocationFailure(String msg) {

    }

    @Override
    public void getOperationListFailure(String msg) {

    }


    /**
     * 更改机器人状态信息
     *
     * @param status
     */
    private void saveRobotstatus(int status) {
        //改变当前机器人状态
        RobotInfoUtils.setRobotRunningStatus(String.valueOf(status));
    }
}

