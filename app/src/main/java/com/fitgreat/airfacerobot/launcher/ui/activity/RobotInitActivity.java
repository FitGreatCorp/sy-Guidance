package com.fitgreat.airfacerobot.launcher.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aispeech.dui.dds.DDS;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotBrainService;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.SyncTimeCallback;
import com.fitgreat.airfacerobot.business.BusinessRequest;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.launcher.model.ActionEvent;
import com.fitgreat.airfacerobot.launcher.model.InitEvent;
import com.fitgreat.airfacerobot.launcher.model.MyException;
import com.fitgreat.airfacerobot.launcher.utils.OperationUtils;
import com.fitgreat.airfacerobot.launcher.utils.ToastUtils;
import com.fitgreat.airfacerobot.launcher.widget.CircularProgressView;
import com.fitgreat.airfacerobot.launcher.widget.CommonTipDialog;
import com.fitgreat.airfacerobot.launcher.widget.CountDownDialog;
import com.fitgreat.airfacerobot.remotesignal.model.InitUiEvent;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.archmvp.base.ui.MvpBaseActivity;
import com.fitgreat.archmvp.base.util.ExecutorManager;
import com.fitgreat.archmvp.base.util.JsonUtils;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.PhoneInfoUtils;
import com.fitgreat.archmvp.base.util.RouteUtils;
import com.fitgreat.archmvp.base.util.SpUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;

import static com.fitgreat.airfacerobot.MyApp.getContext;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_FLOATING_BALL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_RETRY_INIT_SIGNAL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.REGISTERED_DDS_OBSERVER;
import static com.fitgreat.airfacerobot.constants.RobotConfig.WHETHER_CARRY_ON_BOOT;


/**
 * 机器人初始化activity<p>
 *
 * @author zixuefei
 * @since 2020/3/19 0011 17:14
 */
public class RobotInitActivity extends MvpBaseActivity {
    private static final String TAG = "RobotInitActivity";
    @BindView(R.id.init_progress_net)
    CircularProgressView serverProgress;
    @BindView(R.id.init_progress_ros)
    CircularProgressView rosProgress;
    @BindView(R.id.init_progress_talk)
    CircularProgressView voiceProgress;
    @BindView(R.id.tv_net)
    TextView serverState;
    @BindView(R.id.tv_ros)
    TextView rosState;
    @BindView(R.id.tv_voice)
    TextView voiceState;
    @BindView(R.id.ll_net_timeout)
    LinearLayout ll_net_timeout;
    @BindView(R.id.btn_retry)
    Button btn_retry;
    @BindView(R.id.tv_reseasn)
    TextView tv_reseasn;

    private final int DEFAULT_ANIM_TIME = 1000;
    private CommonTipDialog commonTipDialog;
    private RxPermissions rxPermissions;
    private boolean signal_initaled = false;
    private boolean ros_initaled = false;
    private boolean voice_initaled = false;
    private final int MSG_GO_TO_HOME = 10001;
    private CountDownDialog countDownDialog;

    private static final int MSG_SERVER_TIME_OUT = 0X9999;

    private Timer timer;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GO_TO_HOME:
                    gotohome();
                    break;
                case MSG_SERVER_TIME_OUT:
                    checkInit();
                    break;
            }
        }
    };

    @Override
    public int getLayoutResource() {
        return R.layout.activity_robot_init;
    }

    @Override
    public void initData() {
        LogUtils.d(TAG, "-------RobotInitActivity  create--------");
        EventBus.getDefault().register(this);

        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(true);
        EventBus.getDefault().post(initUiEvent);

        commonTipDialog = new CommonTipDialog(this);
        rxPermissions = new RxPermissions(this);
        startCheckSelf();
        LogUtils.d("RobotInitToto", "---initData--------");
    }

    @Override
    public void disconnectNetWork() {
        finish();
        RouteUtils.goToActivity(RobotInitActivity.this, RobotInitActivity.class);
    }

    @Override
    public void disconnectRos() {
        //ros连接失败,重新了解ros机器人
        ros_initaled = false;
        updateCheckProgress(RobotConfig.INIT_TYPE_ROS_PROGRESS, "0");
        initRos();
    }

    @OnClick({R.id.btn_retry})
    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.btn_retry:
                EventBus.getDefault().post(new InitEvent(RobotConfig.TYPE_CHECK_STATE, RobotConfig.INIT_TYPE_SIGNAL_PROGRESS, String.valueOf(10)));
                InitEvent initEvent = new InitEvent();
                initEvent.setType(MSG_RETRY_INIT_SIGNAL);
                EventBus.getDefault().post(initEvent);
                break;
        }
    }

    /**
     * 开始自检
     */
    private void startCheckSelf() {
        if (!rxPermissions.isGranted(Manifest.permission.READ_PHONE_STATE) ||
                !rxPermissions.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                !rxPermissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                !rxPermissions.isGranted(Manifest.permission.CAMERA) ||
                !rxPermissions.isGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
                !rxPermissions.isGranted(Manifest.permission.RECORD_AUDIO) ||
                !rxPermissions.isGranted(Manifest.permission.MODIFY_AUDIO_SETTINGS)
        ) {
            rxPermissions.request(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO).subscribe((granted) -> {
                if (granted) {
                    LogUtils.d(TAG, "------rxPermissions granted-------");
                    startCheckService();
                } else {
                    ToastUtils.showSmallToast("请允许权限使用，否则app无法正常运行");
                    if (serverProgress != null) {
                        serverProgress.postDelayed(() -> {
                            startCheckSelf();
                        }, 3000);
                    }
                }
            });
        } else {
            LogUtils.d(TAG, "------has Permissions start-------");
            startCheckService();
        }
    }

    private void startCheckService() {
        LogUtils.d(TAG, "-----------start check self-------------");
        stopService(new Intent(this, RobotBrainService.class));
        if (TextUtils.isEmpty(RobotInfoUtils.getAirFaceDeviceId())) {
            RobotInfoUtils.setAirFaceDeviceId(PhoneInfoUtils.getIMEI(getContext()));
        }
        startService(new Intent(this, RobotBrainService.class));
        handler.postDelayed(() -> {
            initVoice();
        }, 2 * 1000);
    }

    @Override
    public Object createPresenter() {
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();
//        if (DDS.getInstance().getAgent() == null) {
//            LogUtils.e(TAG, "onStart agent is null, return ...");
//            return;
//        }
//        EventBus.getDefault().post(new ActionEvent(REGISTERED_DDS_OBSERVER, ""));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commonTipDialog != null && commonTipDialog.isShowing()) {
            commonTipDialog.dismiss();
            commonTipDialog = null;
        }

        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(false);
        EventBus.getDefault().post(initUiEvent);

        EventBus.getDefault().unregister(this);

        handler.removeMessages(MSG_SERVER_TIME_OUT);
        handler.removeMessages(MSG_GO_TO_HOME);
        LogUtils.d("RobotInitToto", "---onDestroy--------");
    }


    private void initRos() {
        EventBus.getDefault().post(new InitUiEvent(RobotConfig.INIT_TYPE_ROS, "start"));
    }

    private void initVoice() {
        //初始化DDS
//        EventBus.getDefault().post(new InitUiEvent(RobotConfig.INIT_TYPE_VOICE, "start"));
        EventBus.getDefault().post(new InitEvent(RobotConfig.TYPE_CHECK_STATE, RobotConfig.INIT_TYPE_VOICE_PROGRESS, "100"));

    }

    private void gotohome() {
        EventBus.getDefault().post(new InitEvent(RobotConfig.TYPE_CHECK_STATE_DONE, ""));
        RouteUtils.goToActivity(getContext(), MainActivity.class);
        finish();
    }

    /**
     * 初始化进度更新
     */
    private void updateCheckProgress(String action, String extra) {
        int progress = Integer.parseInt(extra);
        LogUtils.d(TAG, "progress , " + progress + "  action , " + action);
        switch (action) {
            case RobotConfig.INIT_TYPE_SIGNAL_PROGRESS:
                if (progress != 100) {
                    serverProgress.setProgress(Integer.parseInt(extra), DEFAULT_ANIM_TIME);
                } else {
                    serverProgress.setProgress(100, DEFAULT_ANIM_TIME);
                    if (timer != null) {
                        timer.cancel();
                    }
                    ll_net_timeout.setVisibility(View.GONE);
                    rosProgress.postDelayed(() -> {
                        if (serverState == null) {
                            return;
                        }
                        serverState.setText(R.string.net_success);
                        rosState.setText(R.string.ros_starting);
                        initRos();
                    }, DEFAULT_ANIM_TIME);
//                    EventBus.getDefault().post(new InitEvent(RobotConfig.TYPE_CHECK_STATE, RobotConfig.INIT_TYPE_ROS_PROGRESS, String.valueOf(progress)));
                    signal_initaled = true;
                }
                if (signal_initaled && ros_initaled && voice_initaled) { //
                    handler.sendEmptyMessageDelayed(MSG_GO_TO_HOME, 1500);
                }
                break;
            case RobotConfig.INIT_TYPE_ROS_PROGRESS:
                if (progress != 100) {
                    rosProgress.setProgress(Integer.parseInt(extra), DEFAULT_ANIM_TIME);
                } else {
                    rosProgress.setProgress(100, 500);
                    voiceProgress.postDelayed(() -> {
                        if (rosState == null) {
                            return;
                        }
                        rosState.setText(R.string.ros_success);
                    }, DEFAULT_ANIM_TIME);
                    ExecutorManager.getInstance().executeScheduledTask(() -> {
                        BusinessRequest.getServerTime(SyncTimeCallback.syncTimeCallback);
                    }, 0, 3, TimeUnit.MINUTES);
                    ros_initaled = true;
                }
                if (signal_initaled && ros_initaled && voice_initaled) { //
                    handler.sendEmptyMessageDelayed(MSG_GO_TO_HOME, 1500);
                }
                break;
            case RobotConfig.INIT_TYPE_VOICE_PROGRESS:
                if (progress != 100) {
                    voiceProgress.setProgress(Integer.parseInt(extra), DEFAULT_ANIM_TIME);
                } else {
                    voiceProgress.setProgress(100, 1000);
                    voiceState.postDelayed(() -> {
                        if (voiceState == null) {
                            return;
                        }
                        voiceState.setText(R.string.voice_success);
                    }, DEFAULT_ANIM_TIME);
                    voice_initaled = true;
                }
                if (signal_initaled && ros_initaled && voice_initaled) { //
                    handler.sendEmptyMessageDelayed(MSG_GO_TO_HOME, 1500);
                }
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(InitEvent initEvent) {
        switch (initEvent.type) {
            case RobotConfig.TYPE_CHECK_STATE:
                updateCheckProgress(initEvent.action, initEvent.extra);
                break;
            case RobotConfig.TYPE_SHOW_TIPS:
                showDialogTip(initEvent.action, initEvent.extra);
                break;
            case RobotConfig.MSG_START_INIT_VOICE:
                handler.removeMessages(MSG_SERVER_TIME_OUT);
                handler.sendEmptyMessageDelayed(MSG_SERVER_TIME_OUT, 60 * 1 * 1000);
                voiceState.setText(R.string.voice_starting);

                break;
            default:
                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(InitUiEvent initUiEvent) {
        switch (initUiEvent.type) {
            case RobotConfig.CANT_GET_BATTERY:
                LogUtils.d(TAG, "CANT_GET_BATTERY !!!!!!!!!!!!");
//                if(countDownDialog!=null){
//                    if(countDownDialog.isShowing()){
//                        countDownDialog.dismiss();
//                    }
//                    countDownDialog = null;
//                }
//                countDownDialog = new CountDownDialog(this);
//                countDownDialog.show();
                break;
        }
    }


    private void checkInit() {
        if (!signal_initaled) {
            serverState.setText(getResources().getString(R.string.net_fail));
            ll_net_timeout.setVisibility(View.VISIBLE);
            tv_reseasn.setText("网络质量差，请检查网络后重试。");
        }
        if (!ros_initaled) {
            if (rosState != null) {
                rosState.setText(getResources().getString(R.string.ros_fail));
            }
        }
        if (!voice_initaled) {
            voiceState.setText(getResources().getString(R.string.voice_fail));
        }
    }

    private void showDialogTip(String title, String content) {
        if (!isFinishing() && commonTipDialog != null && !commonTipDialog.isShowing()) {
            commonTipDialog.show();
            commonTipDialog.setCancelVisible(false);
            commonTipDialog.setTitleAndContent(title, content);
        }
    }

    @Override
    public void onBackPressed() {
    }
}
