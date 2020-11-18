package com.fitgreat.airfacerobot.launcher.ui.activity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.fitgreat.airfacerobot.constants.Constants;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.floatball.AccessibilityConstant;
import com.fitgreat.airfacerobot.floatball.DelayOnClickListener;
import com.fitgreat.airfacerobot.floatball.FloatWindow;
import com.fitgreat.airfacerobot.floatball.FloatWindowOption;
import com.fitgreat.airfacerobot.floatball.FloatWindowViewStateCallback;
import com.fitgreat.airfacerobot.launcher.contractview.MainView;
import com.fitgreat.airfacerobot.launcher.model.ActionEvent;
import com.fitgreat.airfacerobot.launcher.model.CommandDataEvent;
import com.fitgreat.airfacerobot.launcher.model.DaemonEvent;
import com.fitgreat.airfacerobot.launcher.model.InitEvent;
import com.fitgreat.airfacerobot.launcher.model.NavigationTip;
import com.fitgreat.airfacerobot.launcher.model.RobotSignalEvent;
import com.fitgreat.airfacerobot.launcher.presenter.MainPresenter;
import com.fitgreat.airfacerobot.launcher.utils.LanguageUtil;
import com.fitgreat.airfacerobot.launcher.utils.OperationUtils;
import com.fitgreat.airfacerobot.launcher.utils.ToastUtils;
import com.fitgreat.airfacerobot.launcher.widget.CommonTipDialog;
import com.fitgreat.airfacerobot.launcher.widget.WarnningDialog;
import com.fitgreat.airfacerobot.launcher.widget.MyDialog;
import com.fitgreat.airfacerobot.launcher.widget.TimeCountDownDialog;
import com.fitgreat.airfacerobot.remotesignal.model.FilePlayEvent;
import com.fitgreat.airfacerobot.remotesignal.model.InitUiEvent;
import com.fitgreat.airfacerobot.remotesignal.model.RobotInfoData;
import com.fitgreat.airfacerobot.remotesignal.model.SignalDataEvent;
import com.fitgreat.airfacerobot.settings.SettingActivity;
import com.fitgreat.airfacerobot.speech.SpeechManager;
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
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;

import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_LANGUAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.INIT_ROS_KEY_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_FLOATING_BALL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_GETROS_VERSION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_LIGHT_OFF;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_LIGHT_ON;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_ROS_NEXT_STEP;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PLAY_TASK_PROMPT_INFO;
import static com.fitgreat.airfacerobot.constants.RobotConfig.START_INTRODUCTION_WORK_FLOW_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.TYPE_CHECK_STATE_DONE;


/**
 * 启动桌面activity<p>
 *
 * @author zixuefei
 * @since 2020/3/11 0011 10:14
 */
public class MainActivity extends MvpBaseActivity<MainView, MainPresenter> implements MainView {
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

    @BindView(R.id.language_chinese)
    Button mLanguageChinese;
    @BindView(R.id.language_english)
    Button mLanguageEnglish;

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ALERT_CODE = 6666;
    private static final int CHECK_HARDWARE_VERSION_CODE = 6667;
    private static final int CHECK_HDVC = 6668;
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
    private MyDialog myDialog;
    private TimeCountDownDialog timeCountDownDialog = null;
    private Timer countDownTimer;
    private TimerTask countDownTimerTask;
    //机器人回充30秒倒计时
    private int countDownTime = 30;
    private static final int REQUEST_CODE_WRITE_SETTINGS = 1001;
    private WarnningDialog warnningDialog;
    //进入设置页面第一次点击标志
    private boolean firstClickTag = false;
    private long firstClickTime;
    private Timer introductionTimer;
    private TimerTask introductionTimerTask;
    private int introductionCountdown;

    @Override
    public int getLayoutResource() {
        return R.layout.activity_main_sy;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void initData() {
        LogUtils.d(TAG, "-------MainActivity create-----------");
        EventBus.getDefault().register(this);
        SpUtils.putBoolean(getContext(), "isLock", true);
        commonTipDialog = new CommonTipDialog(this);
        downloadingDialog = new DownloadingDialog(this);
        showFloatBall();
        requestSettingPermission();
        warnningDialog = new WarnningDialog(this);
        //启动机器人说话动画
        mWinkSpeakAnimation.setBackgroundResource(R.drawable.wink_speak_animation);
        AnimationDrawable animationDrawable = (AnimationDrawable) mWinkSpeakAnimation.getDrawable();
        animationDrawable.start();
        //医院介绍工作流启动标志
        SpUtils.putBoolean(MyApp.getContext(), START_INTRODUCTION_WORK_FLOW_TAG, false);
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
        initResume();
    }



    @Override
    protected void onPause() {
        super.onPause();
        //取消启动院内介绍工作流3分钟计时
        if (introductionTimer != null) {
            introductionTimer.cancel();
            introductionTimer=null;
        }
        if (introductionTimerTask != null) {
            introductionTimerTask.cancel();
            introductionTimerTask=null;
        }
        introductionCountdown = 0;
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

    @OnClick({R.id.bt_home_setting, R.id.me_want_go_image, R.id.common_problem_image, R.id.hospital_introduction_image, R.id.language_chinese, R.id.language_english})
    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.bt_home_setting: //跳转设置模块
                jumpSetModule();
                break;
            case R.id.me_want_go_image: //我要去
                RouteUtils.goToActivity(getContext(), ChoseDestinationActivity.class);
                break;
            case R.id.common_problem_image: //常见问题
                RouteUtils.goToActivity(getContext(), CommonProblemActivity.class);
                break;
            case R.id.hospital_introduction_image: //院内介绍
                startIntroductionWorkFlow();
                break;
            case R.id.language_chinese: //应用语言显示中文
                setLanguage("zh", mLanguageChinese, mLanguageEnglish);
                break;
            case R.id.language_english: //应用语言显示英文
                setLanguage("en", mLanguageEnglish, mLanguageChinese);
                break;
            default:
                break;
        }
    }
    private void initResume() {
        //切换当前机器显示语言选择状态
        String currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "null");
        if (currentLanguage.equals("en")) {
            mLanguageEnglish.setSelected(true);
            mLanguageChinese.setSelected(false);
        } else if (currentLanguage.equals("zh")) {
            mLanguageChinese.setSelected(true);
            mLanguageEnglish.setSelected(false);
        }
        //jRos初始化状态
        boolean rosInitTagState = SpUtils.getBoolean(getContext(), INIT_ROS_KEY_TAG, false);
        if (rosInitTagState) {
            if (mPresenter != null) {
                mPresenter.getNetSignalLevel(this);
            }
            //更新本地导航位置,执行任务信息
            mPresenter.getLocationInfo();
            //机器人名字显示
            RobotInfoData robotInfoData = RobotInfoUtils.getRobotInfo();
            if (robotInfoData != null) {
                mRobotName.setText(robotInfoData.getF_Name());
            }
            //进入首页语音播报  "您好，我是小白，很高兴为您服务。我可以为您带路有什么不懂的也可以问我哦。"
//            EventBus.getDefault().post(new ActionEvent(PLAY_TASK_PROMPT_INFO, getResources().getString(R.string.home_prompt_text)));
            mVoiceMsg.setText(getResources().getString(R.string.home_prompt_text));
            //启动空闲时3分钟计时,3分钟计时结束后启动院内介绍工作流
            startIntroductionCountdown();
        }
    }
    /**
     * 启动院内介绍工作流
     */
    private void startIntroductionWorkFlow() {
        //启动院内介绍工作流程次数限制
        boolean startIntroductionWorkflowTag = SpUtils.getBoolean(MyApp.getContext(), START_INTRODUCTION_WORK_FLOW_TAG, false);
        if (startIntroductionWorkflowTag) {
            playShowText("抱歉，机器人当前正在忙碌中，请稍后。。。");
            return;
        }
        OperationUtils.startSpecialWorkFlow(3);
    }

    /**
     * 播放并首页展示对应文案
     */
    public void playShowText(String content) {
        EventBus.getDefault().post(new ActionEvent(PLAY_TASK_PROMPT_INFO, content));
        EventBus.getDefault().post(new NavigationTip(content));
    }

    /**
     * 连续两次点击时间间隔大于0.5秒,跳转进入设置模块
     */
    private void jumpSetModule() {
        if (!firstClickTag) {
            firstClickTag = true;
            //记录第一次点击按钮时间单位为秒
            firstClickTime = System.currentTimeMillis();
        } else {
            //两次点击按钮时间相差大于1秒进入设置模块,小于1秒需再次点击,第一次点击时间重新记录
            if ((System.currentTimeMillis() - firstClickTime) > 500) {
                RouteUtils.goToActivity(getContext(), SettingActivity.class);
            }
            firstClickTag = false;
        }
    }

    /**
     * 切换机器人显示语言
     */
    public void setLanguage(String language, Button selectButton, Button normalButton) {
        SpUtils.putString(MyApp.getContext(), CURRENT_LANGUAGE, language);
        LanguageUtil.changeAppLanguage(MainActivity.this);
        finish();
        RouteUtils.goHome(MainActivity.this);
        selectButton.setSelected(true);
        normalButton.setSelected(false);
    }

    /**
     * 机器人空闲时,启动计时器3分钟后启动院内介绍工作流
     */
    public void startIntroductionCountdown() {
        introductionTimer = new Timer();
        introductionTimerTask = new TimerTask() {
            @Override
            public void run() {
                introductionCountdown++;
                if (introductionCountdown == 180) {
                    introductionCountdown = 0;
                    introductionTimer.cancel();
                    introductionTimerTask.cancel();
                    LogUtils.d("startSpecialWorkFlow", "空闲3分钟后再次启动院内介绍工作流----------");
                    OperationUtils.startSpecialWorkFlow(3);
                }
            }
        };
        introductionTimer.schedule(introductionTimerTask, 0, 1000);
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

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REQUEST_ALERT_CODE:
                    if (Settings.canDrawOverlays(MainActivity.this)) {
                        LogUtils.d(TAG, "------has permission: start check self-------");
                        showFloatBall();
                    } else {
                        handler.sendEmptyMessageDelayed(REQUEST_ALERT_CODE, 1000);
                    }
                    break;
                case CHECK_HARDWARE_VERSION_CODE:
                    LogUtils.d(TAG, "CHECK_HARDWARE_VERSION_CODE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    if (mPresenter != null) {
                        mPresenter.checkHardwareVersion();
                    }
                    break;
                case CHECK_HDVC:
                    LogUtils.d(TAG, "CHECK_HDVC  CHECK_HDVC  CHECK_HDVC  CHECK_HDVC !!!!!");
                    LogUtils.d(TAG, "robotstatus = " + RobotInfoUtils.getRobotRunningStatus());
                    if (RobotInfoUtils.getRobotRunningStatus().equals("5") || RobotInfoUtils.getRobotRunningStatus().equals("1")) {
                        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                        if (cn.getClassName().equals("com.fitgreat.airfacerobot.launcher.ui.activity.LauncherActivity")) {
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
                case DownloadUtils.DOWNLOAD_FAILED:
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
                case DownloadUtils.DOWNLOAD_SUCCESS:
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
                default:
                    break;
            }
        }
    };

    @Override
    public MainPresenter createPresenter() {
        return new MainPresenter();
    }

    @Override
    public void onBackPressed() {
    }


    /**
     * 取消回充30秒倒计时
     */
    private void cancelCountDown() {
        if (timeCountDownDialog != null) {
            timeCountDownDialog.dismiss();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        if (countDownTimerTask != null) {
            countDownTimerTask.cancel();
            countDownTimerTask = null;
        }
        //30秒倒计时,下一次提示
        countDownTime = 30;
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
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(DaemonEvent daemonEvent) {
        LogUtils.d(TAG, "daemonEvent:" + JsonUtils.encode(daemonEvent));
        switch (daemonEvent.type) {
            case RobotConfig.TYPE_SHOW_VERSION_TIPS:
                ExecutorManager.getInstance().cancelScheduledTask(checkVersionScheduledFuture);
                checkVersionScheduledFuture = ExecutorManager.getInstance().executeScheduledTask(() -> {
                    LogUtils.d(TAG, "check update RobotStatus = " + RobotInfoUtils.getRobotRunningStatus());
                    if ("1".equals(RobotInfoUtils.getRobotRunningStatus()) || "5".equals(RobotInfoUtils.getRobotRunningStatus())) {
                        handler.post(() -> {
                            foundAppNewVersion(daemonEvent.action, daemonEvent.extra);
                            ExecutorManager.getInstance().cancelScheduledTask(checkVersionScheduledFuture);
                        });
                    }
                }, 0, 30, TimeUnit.SECONDS);
                break;
            case RobotConfig.TYPE_SHOW_VERSION_PROGRESS:
                showDownloadingDialog(Integer.parseInt(daemonEvent.action));
                break;
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
            case RobotConfig.TASK_DIALOG:
                LogUtils.d(TAG, "TASK_DIALOG !!!!!    daemonEvent.getAction() = " + daemonEvent.getAction());
                if (daemonEvent.getAction().equals("2")) {
                    String filename = daemonEvent.extra;
                    LogUtils.d("LauncherActivity", "filename = " + filename);
                    if (myDialog != null) {
                        myDialog.dismiss();
                        myDialog = null;
                    }
                    myDialog = new MyDialog(getContext());
                    myDialog.setTitle("播放提示");
                    myDialog.setMessage("请问现在开始播放" + filename + "吗？");
                    myDialog.setPositiveOnclicListener("开始播放", () -> {
                        myDialog.dismiss();
                        FilePlayEvent filePlayEvent = new FilePlayEvent(RobotConfig.FILE_PLAY_OK, "2");
                        EventBus.getDefault().post(filePlayEvent);
                    });
                    myDialog.setNegativeOnclicListener("取消", () -> {
                        myDialog.dismiss();
                        FilePlayEvent filePlayEvent = new FilePlayEvent(RobotConfig.FILE_PLAY_CANCEL, "2");
                        EventBus.getDefault().post(filePlayEvent);
                    });
                    myDialog.show();
                } else if (daemonEvent.getAction().equals("3")) {
                    String filename = daemonEvent.extra;
                    LogUtils.d("LauncherActivity", "filename = " + filename);
                    if (myDialog != null) {
                        myDialog.dismiss();
                        myDialog = null;
                    }
                    myDialog = new MyDialog(getContext());
                    myDialog.setTitle("播放提示");
                    myDialog.setMessage("请问现在开始播放" + filename + "吗？");
                    myDialog.setPositiveOnclicListener("开始播放", () -> {
                        myDialog.dismiss();
                        FilePlayEvent filePlayEvent = new FilePlayEvent(RobotConfig.FILE_PLAY_OK, "3");
                        EventBus.getDefault().post(filePlayEvent);
                    });
                    myDialog.setNegativeOnclicListener("取消", () -> {
                        myDialog.dismiss();
                        FilePlayEvent filePlayEvent = new FilePlayEvent(RobotConfig.FILE_PLAY_CANCEL, "3");
                        EventBus.getDefault().post(filePlayEvent);
                    });
                    myDialog.show();
                } else if (daemonEvent.getAction().equals("4")) {
                    String filename = daemonEvent.extra;
                    LogUtils.d("LauncherActivity", "filename = " + filename);
                    if (myDialog != null) {
                        myDialog.dismiss();
                        myDialog = null;
                    }
                    myDialog = new MyDialog(getContext());
                    myDialog.setTitle("播放提示");
                    myDialog.setMessage("请问现在开始播放" + filename + "吗？");
                    myDialog.setPositiveOnclicListener("开始播放", () -> {
                        myDialog.dismiss();
                        FilePlayEvent filePlayEvent = new FilePlayEvent(RobotConfig.FILE_PLAY_OK, "4");
                        EventBus.getDefault().post(filePlayEvent);
                    });
                    myDialog.setNegativeOnclicListener("取消", () -> {
                        myDialog.dismiss();
                        FilePlayEvent filePlayEvent = new FilePlayEvent(RobotConfig.FILE_PLAY_CANCEL, "4");
                        EventBus.getDefault().post(filePlayEvent);
                    });
                    myDialog.show();
                }
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
                    int level = Integer.parseInt(robotSignalEvent.action);
                    mSignalImg.setImageLevel(level);
                }
                break;
            case RobotConfig.ROS_MSG_BATTERY:
                if (mTextBattery != null && isResume) {
                    int battery = Math.round(robotSignalEvent.getBattery());
                    if (robotSignalEvent.isPowerStatus()) { //机器人充电中
                        LogUtils.d("RobotSignalEvent", "机器人充电中,当前机器人状态:   , " + RobotInfoUtils.getRobotRunningStatus());
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
                        LogUtils.d(TAG, "机器人没有充电,当前机器人状态: " + RobotInfoUtils.getRobotRunningStatus() + "  当前机器人电量:  " + battery);
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
     * 识别指令后进行任务操作
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showBackCommandData(CommandDataEvent commandDataEvent) {
        LogUtils.json(TAG, "showBackCommandData:" + JSON.toJSONString(commandDataEvent));
        if (commandDataEvent.commandType == 1) {  //语音取消任务
            LogUtils.d(TAG, "showBackCommandData:取消任务指令,当前机器人状态  " + RobotInfoUtils.getRobotRunningStatus());
            if (!RobotInfoUtils.getRobotRunningStatus().equals("1")) {//机器人空闲时不接收取消指令
                EventBus.getDefault().post(new ActionEvent(PLAY_TASK_PROMPT_INFO, "确定取消当前任务吗?"));
            }
        }
    }

    /**
     * 提示信息首页音浪上部显示更新
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(NavigationTip navigationTip) {
        mVoiceMsg.setText(navigationTip.getTip());
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
     * APP发现新版本弹框
     */
    private void foundAppNewVersion(String title, String content) {
        long currenttime = System.currentTimeMillis();
        handler.removeMessages(CHECK_HDVC);
        show_svdialog = true;
//        LogUtils.d(TAG, " handler.removeMessages(CHECK_HDVC)!!!!!!!!!!!!!!!!!");
        if (currenttime - lasttime >= 30 * 1000 * 60) {
            if (!isFinishing() && commonTipDialog != null) {
                if (!commonTipDialog.isShowing()) {
                    LogUtils.d(TAG, "----------show soft version dialog--------");
                    commonTipDialog.show();
                    commonTipDialog.setCancelVisible(true);
                    commonTipDialog.setCancelable(false);
                    commonTipDialog.setOnDialogClick((int which) -> {
                        if (which == R.id.common_tip_ok) {

                            Bundle data = new Bundle();
                            data.putString("type", "install");
                            RouteUtils.sendDaemonBroadcast(this, Constants.ACTION_DAEMON_MSG, data);
                            RobotInfoUtils.setRobotRunningStatus("6");
                            showDownloadingDialog(0);
                            LogUtils.d("task_robot_status", "开始软件升级: " + " 机器人状态 , " + RobotInfoUtils.getRobotRunningStatus());
                        } else if (which == R.id.common_tip_cancel) {
                            show_svdialog = false;
                            handler.sendEmptyMessageDelayed(CHECK_HARDWARE_VERSION_CODE, 300);
                        }
                    });
                }
                commonTipDialog.setTitleAndContent(title, content);
            }
            lasttime = currenttime;
        }
    }

    /**
     * Hardware发现新版本弹框
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
                    commonTipDialog.show();
                    commonTipDialog.setCancelVisible(true);
                    commonTipDialog.setCancelable(false);
                    commonTipDialog.setOnDialogClick((int which) -> {
                        if (which == R.id.common_tip_ok) {
                            LogUtils.d("task_robot_status", "开始硬件升级" + "    " + (rosUpdateStep != null) + (!rosUpdateStep.isEmpty()));
                            if (rosUpdateStep != null && !rosUpdateStep.isEmpty()) {
                                RobotInfoUtils.setRobotRunningStatus("6");
                                excNextStep(rosUpdateStep);
                            } else {
                                RobotInfoUtils.setRobotRunningStatus("1");
                            }
                        }
                    });
                }
                commonTipDialog.setTitleAndContent(versionInfo.getF_UpdateTitle(), versionInfo.getF_UpdateContent());
            }
        }
        show_hvdialog = true;
    }

    /**
     * 执行下一步骤
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

