package com.fitgreat.airfacerobot.launcher.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.automission.AutoMissionActivity;
import com.fitgreat.airfacerobot.business.BusinessRequest;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.launcher.contractview.NormalHomeView;
import com.fitgreat.airfacerobot.model.ActionDdsEvent;
import com.fitgreat.airfacerobot.model.DaemonEvent;
import com.fitgreat.airfacerobot.model.IflytekAnswerData;
import com.fitgreat.airfacerobot.model.NavigationTip;
import com.fitgreat.airfacerobot.model.OperationEvent;
import com.fitgreat.airfacerobot.model.RecordInfo;
import com.fitgreat.airfacerobot.model.RobotSignalEvent;
import com.fitgreat.airfacerobot.model.VolumeBrightEvent;
import com.fitgreat.airfacerobot.launcher.presenter.NormalHomePresenter;
import com.fitgreat.airfacerobot.launcher.ui.adapter.RecordAdapter;
import com.fitgreat.airfacerobot.launcher.utils.ToastUtils;
import com.fitgreat.airfacerobot.launcher.widget.MyDialog;
import com.fitgreat.airfacerobot.launcher.widget.ValidationOrPromptDialog;
import com.fitgreat.airfacerobot.launcher.widget.VolumeBrightView;
import com.fitgreat.airfacerobot.launcher.widget.WaveView;
import com.fitgreat.airfacerobot.remotesignal.model.FilePlayEvent;
import com.fitgreat.airfacerobot.remotesignal.model.RobotInfoData;
import com.fitgreat.airfacerobot.remotesignal.model.SignalDataEvent;
import com.fitgreat.airfacerobot.remotesignal.model.SpeakEvent;
import com.fitgreat.airfacerobot.settings.SettingActivity;
import com.fitgreat.airfacerobot.speech.model.MessageBean;
import com.fitgreat.airfacerobot.videocall.model.VideoMessageEve;
import com.fitgreat.archmvp.base.okhttp.BaseResponse;
import com.fitgreat.archmvp.base.okhttp.HttpCallback;
import com.fitgreat.archmvp.base.ui.MvpBaseFragment;
import com.fitgreat.archmvp.base.util.JsonUtils;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.PhoneInfoUtils;
import com.fitgreat.archmvp.base.util.RouteUtils;
import com.fitgreat.archmvp.base.util.SpUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import static com.fitgreat.airfacerobot.constants.RobotConfig.BRIGHTNESS_KEY_DISPLAY;
import static com.fitgreat.airfacerobot.constants.RobotConfig.BRIGHTNESS_KEY_HIDDEN;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CANCEL_PLAY_TASK;
import static com.fitgreat.airfacerobot.constants.RobotConfig.FILE_PLAY_OK;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_ISLOCK_CHANGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_SPEAK_TEXT;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TTS_CANCEL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TTS_SHOW_DIALOG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PLAY_TASK_PROMPT_INFO;
import static com.fitgreat.airfacerobot.constants.RobotConfig.RECHARGE_SPECIFIC_WORKFLOW;
import static com.fitgreat.airfacerobot.constants.RobotConfig.VOLUME_KEY_DISPLAY;
import static com.fitgreat.airfacerobot.constants.RobotConfig.VOLUME_KEY_HIDDEN;
import static com.fitgreat.airfacerobot.remotesignal.SignalConfig.MSG_STOP_TASK;


/**
 * 通用HOME界面<p>
 *
 * @author zixuefei
 * @since 2020/3/20 0020 11:43
 */
public class NormalHomeFragment extends MvpBaseFragment<NormalHomeView, NormalHomePresenter> implements ValidationOrPromptDialog.ValidationFailListener, NormalHomeView {
    private static final String TAG = "NormalHomeFragment";

    @BindView(R.id.ic_signal)
    ImageView mSignalView;
    @BindView(R.id.tv_battery)
    TextView mBatteryTV;
    @BindView(R.id.robot_name)
    TextView mRobotNameTV;
    @BindView(R.id.robot_hospital)
    TextView mHospitalTV;
    @BindView(R.id.battery_img)
    ImageView batteryImage;
    @BindView(R.id.voice_anim)
    WaveView waveView;
    @BindView(R.id.power_mode_txt)
    TextView powerModeTxt;
    @BindView(R.id.home_control_mode_image)
    ImageView controlModeImage;
    @BindView(R.id.home_move_mode_image)
    ImageView moveModeImage;
    @BindView(R.id.without_recharge_home_control_mode_image)
    ImageView withoutRechargeControlModeImage;
    @BindView(R.id.without_recharge_home_move_mode_image)
    ImageView withoutRechargeMoveModeImage;
    @BindView(R.id.without_recharge_power_mode_txt)
    TextView withoutRechargePowerModeTxt;
    @BindView(R.id.without_recharge_auto_mission_image)
    ImageView withoutRechargeAutoMissionImage;
    @BindView(R.id.auto_mission_layout)
    LinearLayout autoMissionLayout;
    @BindView(R.id.auto_recharge_layout)
    LinearLayout autoRechargeLayout;
    @BindView(R.id.without_recharge_auto_mission_layout)
    LinearLayout withoutRechargeAutoMissionLayout;
    @BindView(R.id.home_bluetooth_linked_devices_layout)
    LinearLayout homeBluetoothLinkedDevicesLayout;
    @BindView(R.id.no_bluetooth_device)
    ImageView noBluetoothDevice;
    @BindView(R.id.record_recyclerView)
    RecyclerView recordRecyclerView;
    @BindView(R.id.volume_bright_view)
    VolumeBrightView volumeBrightView;
    @BindView(R.id.without_recharge_mode_switch)
    ConstraintLayout withoutRechargeModeSwitch;
    @BindView(R.id.without_recharge_auto_mission)
    ConstraintLayout withoutRechargeAutoMission;
    @BindView(R.id.constraintLayout_mode_switch)
    ConstraintLayout constraintLayoutModeSwitch;
    @BindView(R.id.constraintLayout_auto_recharge)
    ConstraintLayout constraintLayoutAutoRecharge;
    @BindView(R.id.constraintLayout_auto_mission)
    ConstraintLayout constraintLayoutAutoMission;


    private MyDialog myDialog;
    private boolean has_check = false;
    private RecordAdapter recordAdapter;
    private RecordInfo recordInfo;
    private Context mContext;
    private ValidationOrPromptDialog validationOrPromptDialog;
    private SignalDataEvent controlMode;
    private SignalDataEvent moveMode;

    public NormalHomeFragment(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public NormalHomePresenter createPresenter() {
        return new NormalHomePresenter();
    }

    @Override
    public int getLayoutResource() {
        return R.layout.fragment_normal;
    }

    @Override
    public void initData() {
        LogUtils.d(TAG, "initData");
        EventBus.getDefault().register(this);
        updateRobotInfo();
        showRecordData();
    }

    /**
     * 指令记录数据展示
     */
    private void showRecordData() {
        List<RecordInfo> recordData = new ArrayList<>();
        recordData.add(new RecordInfo(0, getString(R.string.show_t1).trim()));
        recordAdapter = new RecordAdapter(recordData);
        recordRecyclerView.setAdapter(recordAdapter);
        recordRecyclerView.addItemDecoration(new SpacesItemDecoration(10));
        recordRecyclerView.setLayoutManager(new LinearLayoutManager(MyApp.getContext(), RecyclerView.VERTICAL, true));
    }

    @Override
    public void validationPassword(String password) {
        mPresenter.verifyPassword(password);
    }

    /**
     * RecyclerView条目添加边距
     */
    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;
            outRect.top = space;
        }
    }

    /**
     * 更新页面内容显示
     */
    private void updateRobotInfo() {
        LogUtils.d(TAG, "updateRobotInfo ++++++++++++++++++++++");
//        versionName.setText("版本号V " + VersionUtils.getVersionName(getContext()));
        RobotInfoData robotInfoData = RobotInfoUtils.getRobotInfo();
        if (robotInfoData != null) {
            mRobotNameTV.setText(robotInfoData.getF_Name());
            mHospitalTV.setSelected(true);
            mHospitalTV.setText(robotInfoData.getF_Hospital() + (TextUtils.isEmpty(robotInfoData.getF_Department()) ? "" : "-" + robotInfoData.getF_Department()));
        }
        boolean isLockState = SpUtils.getBoolean(getContext(), "isLock", true);
        LogUtils.d("updateRobotInfo", "isLockState === " + isLockState);
        if (isLockState) {
            moveModeImage.setSelected(false);
            withoutRechargeMoveModeImage.setSelected(false);
            controlModeImage.setSelected(true);
            withoutRechargeControlModeImage.setSelected(true);
            powerModeTxt.setText("控制模式");
            withoutRechargePowerModeTxt.setText("控制模式");
        } else {
            moveModeImage.setSelected(true);
            withoutRechargeMoveModeImage.setSelected(true);
            controlModeImage.setSelected(false);
            withoutRechargeControlModeImage.setSelected(false);
            powerModeTxt.setText("拖动模式");
            withoutRechargePowerModeTxt.setText("拖动模式");
        }
    }

    @OnClick({R.id.btn_menu, R.id.home_move_mode_image, R.id.home_control_mode_image, R.id.home_bluetooth_linked_devices_layout, R.id.auto_mission_layout, R.id.auto_recharge_layout, R.id.without_recharge_home_control_mode_image,
            R.id.without_recharge_home_move_mode_image, R.id.without_recharge_auto_mission_layout})
    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.btn_menu:
//                validationOrPromptDialog = new ValidationOrPromptDialog(mContext);
//                validationOrPromptDialog.show();
//                validationOrPromptDialog.setValidationFailListener(this);
                RouteUtils.goToActivity(getContext(), SettingActivity.class);
                break;
            case R.id.home_move_mode_image:
                setDragMode(controlModeImage, moveModeImage, powerModeTxt);
                break;
            case R.id.home_control_mode_image:
                setControlMode(moveModeImage, controlModeImage, powerModeTxt);
                break;
            case R.id.without_recharge_home_control_mode_image:
                setControlMode(withoutRechargeMoveModeImage, withoutRechargeControlModeImage, withoutRechargePowerModeTxt);
                break;
            case R.id.without_recharge_home_move_mode_image:
                setDragMode(withoutRechargeControlModeImage, withoutRechargeMoveModeImage, withoutRechargePowerModeTxt);
                break;
            case R.id.home_bluetooth_linked_devices_layout:
                btStateSwitch(homeBluetoothLinkedDevicesLayout);
                LogUtils.d(TAG, "RobotInfoUtils.getRobotLinkedDevicesUrl() =============== " + RobotInfoUtils.getRobotLinkedDevicesUrl());
                if (!TextUtils.isEmpty(RobotInfoUtils.getRobotLinkedDevicesUrl())) {
                    RouteUtils.openBrowser(getContext(), RobotInfoUtils.getRobotLinkedDevicesUrl());
                } else {
                    ToastUtils.showSmallToast("抱歉，我没有绑定连接设备");
                    SpeakEvent speakEvent = new SpeakEvent();
                    speakEvent.setType(MSG_SPEAK_TEXT);
                    speakEvent.setText("抱歉，我没有绑定连接设备");
                    EventBus.getDefault().post(speakEvent);
                    getRobotInfo();
                }
                break;
            case R.id.auto_recharge_layout:
                rechargeToDo();
                break;
            case R.id.auto_mission_layout:
            case R.id.without_recharge_auto_mission_layout: //进入自主宣教页面
                jumpAutoMissionPage();
                break;
            default:
                break;
        }
    }

    private void jumpAutoMissionPage() {
        btStateSwitch(autoMissionLayout);
        if (RobotInfoUtils.getRobotRunningStatus().equals("1") || RobotInfoUtils.getRobotRunningStatus().equals("5")) {
            startActivity(new Intent(MyApp.getContext(), AutoMissionActivity.class));
        } else {
            ToastUtils.showSmallToast("正在执行任务请稍后再试");
        }
    }

    private void setControlMode(ImageView falseModeImage, ImageView trueImage, TextView modeText) {
        controlMode = new SignalDataEvent(RobotConfig.MSG_CHANGE_POWER_LOCK, "");
        controlMode.setPowerlock(1);
        EventBus.getDefault().post(controlMode);
        SpUtils.putBoolean(getContext(), "isLock", true);
        trueImage.setSelected(true);
        falseModeImage.setSelected(false);
        modeText.setText("控制模式");
    }

    private void setDragMode(ImageView falseModeImage, ImageView trueImage, TextView modeText) {
        moveMode = new SignalDataEvent(RobotConfig.MSG_CHANGE_POWER_LOCK, "");
        moveMode.setPowerlock(2);
        EventBus.getDefault().post(moveMode);
        SpUtils.putBoolean(getContext(), "isLock", false);
        trueImage.setSelected(true);
        falseModeImage.setSelected(false);
        modeText.setText("拖动模式");
    }

    @Override
    public void verifyFailure() {
        getActivity().runOnUiThread(() -> {
            validationOrPromptDialog = new ValidationOrPromptDialog(mContext);
            validationOrPromptDialog.setFailPrompt(true);
            validationOrPromptDialog.show();
        });
    }

    @Override
    public void verifySuccess() {
        RouteUtils.goToActivity(getContext(), SettingActivity.class);
    }

    /**
     * 首页按钮自动化回充
     */
    private void rechargeToDo() {
//        myDialog = new MyDialog(getContext());
//        myDialog.setMessage("请确认是否要进行自动回充?");
//        myDialog.setNegativeOnclicListener("取消", () -> {
//            myDialog.dismiss();
//        });
//        myDialog.setPositiveOnclicListener("确定", () -> {
//            myDialog.dismiss();
//            //自动回充
////            OperationUtils.robotRecharge();
//            btStateSwitch(autoRechargeLayout);
//            //如果当前机器人为拖动模式则改为控制模式
//            if (!SpUtils.getBoolean(getContext(), "isLock", true)) {
//                controlMode = new SignalDataEvent(RobotConfig.MSG_CHANGE_POWER_LOCK, "");
//                controlMode.setPowerlock(1);
//                EventBus.getDefault().post(controlMode);
//                SpUtils.putBoolean(getContext(), "isLock", true);
//                //更新页面ui显示机器人模式
//                updateRobotInfo();
//            }
//        });
//        myDialog.setTitle("自动回充提示");
//        myDialog.show();
    }

    /**
     * 首页 "自动回充" "自主宣教" "连接" 按钮状态点击后切换
     */
    public void btStateSwitch(LinearLayout currentLinearLayout) {
        currentLinearLayout.setAlpha((float) 0.7);
        currentLinearLayout.postDelayed(() -> {
            currentLinearLayout.setAlpha((float) 1);
        }, 1000);
    }

    /**
     * 机器人信息获取
     */
    private void getRobotInfo() {
        LogUtils.d(TAG, "getAirFaceDeviceId:" + RobotInfoUtils.getAirFaceDeviceId());
        if (TextUtils.isEmpty(RobotInfoUtils.getAirFaceDeviceId())) {
            RobotInfoUtils.setAirFaceDeviceId(PhoneInfoUtils.getIMEI(getContext()));
        }
        BusinessRequest.getRobotInfo(RobotInfoUtils.getAirFaceDeviceId(), new HttpCallback() {
            @Override
            public void onResult(BaseResponse baseResponse) {
                LogUtils.d(TAG, "robot info:" + JsonUtils.encode(baseResponse));
                if (baseResponse != null) {
                    if (baseResponse.isSucceed() && !baseResponse.isEmptyContent()) {
                        RobotInfoData robotInfoData = JsonUtils.decode(baseResponse.getMsg(), RobotInfoData.class);
                        if (robotInfoData != null) {
                            LogUtils.d(TAG, "robot status = " + baseResponse.getMsg());
                            RobotInfoUtils.saveRobotInfo(robotInfoData);
                            String linkUrl = robotInfoData.getF_Setting();
                            LogUtils.d(TAG, "linkUrl = " + linkUrl);
                            if (!TextUtils.isEmpty(linkUrl)) {
                                try {
                                    JSONObject jsonObject = new JSONObject(linkUrl);
                                    LogUtils.d(TAG, "url:" + jsonObject.getString("ConnectingDevice"));
                                    RobotInfoUtils.setRobotLinkedDevicesUrl(jsonObject.getString("ConnectingDevice"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                RobotInfoUtils.setRobotLinkedDevicesUrl("");
                            }
                        }
                    } else {
                        LogUtils.e(TAG, "---get robot info failed---" + baseResponse.getMsg());
                    }
                }
            }

            @Override
            public void onFailed(String e) {
                LogUtils.e(TAG, "onFailed:" + e);
            }
        });
    }


    /**
     * 机器人信息获取成功后更新
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(RobotInfoData robotInfoData) {
        LogUtils.d(TAG, "RobotInfoData:" + robotInfoData.getF_Name());
        updateRobotInfo();
    }

    /**
     * 导航任务状态更新到对话列表
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(NavigationTip navigationTip) {
        recordAdapter.updateData(new RecordInfo(0, navigationTip.getTip()));
    }

    /**
     * 音量键  亮度建操作
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(VolumeBrightEvent volumeBrightEvent) {
        switch (volumeBrightEvent.mActionKind) {
            case VOLUME_KEY_DISPLAY:
                volumeBrightView.displayVolume(volumeBrightEvent.mCurrentProgress);
                break;
            case VOLUME_KEY_HIDDEN:
                volumeBrightView.hiddenVolume();
                break;
            case BRIGHTNESS_KEY_DISPLAY:
                volumeBrightView.displayBright(volumeBrightEvent.mCurrentProgress);
                break;
            case BRIGHTNESS_KEY_HIDDEN:
                volumeBrightView.hiddenBright();
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
                if (mSignalView != null) {
                    int level = Integer.parseInt(robotSignalEvent.action);
                    mSignalView.setImageLevel(level);
                }
                break;
            case RobotConfig.ROS_MSG_BATTERY:
                LogUtils.d(TAG, "RobotConfig.ROS_MSG_BATTERY !!!!");
                if (mBatteryTV != null) {
                    int battery = Math.round(robotSignalEvent.getBattery());
                    if (robotSignalEvent.isPowerStatus()) { //机器人充电中
                        LogUtils.d("task_robot_status", "机器人充电中,当前机器人状态:   , " + RobotInfoUtils.getRobotRunningStatus());
                        SpUtils.putBoolean(getContext(), "isCharge", true);
                        if (SpUtils.getBoolean(getContext(), "isVideocall", false)) {  //机器人视频中
                            saveRobotstatus(4);
                        } else {
                            //"执行操作中状态" 大于 "冲电状态"  "升级状态",机器充电时并且收到宣教/播放类任务时显示状态"执行操作中状态"
                            if (!RobotInfoUtils.getRobotRunningStatus().equals("3") && !RobotInfoUtils.getRobotRunningStatus().equals("6")) {
                                saveRobotstatus(5);
                            }
                        }
                        batteryImage.setImageLevel(5);
                        mBatteryTV.setText(battery + "%");
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
                            LogUtils.d("task_robot_status", "机器人没有充电,当前机器人状态:,    " + RobotInfoUtils.getRobotRunningStatus());
                        }
                        if (battery <= 20) {
                            batteryImage.setImageLevel(0);
                        } else if (battery <= 40) {
                            batteryImage.setImageLevel(1);
                        } else if (battery <= 60) {
                            batteryImage.setImageLevel(2);
                        } else if (battery <= 80) {
                            batteryImage.setImageLevel(3);
                        } else if (battery <= 100) {
                            batteryImage.setImageLevel(4);
                        }
                        mBatteryTV.setText(battery + "%");
                    }
                    LogUtils.d(TAG, "robotstatus = " + RobotInfoUtils.getRobotRunningStatus());
                }
                LogUtils.d(TAG, "PowerHealth() ====== " + robotSignalEvent.getPowerHealth());
                boolean isLock = robotSignalEvent.getPowerHealth();
                LogUtils.d(TAG, "isLock = " + isLock);
                if (isLock) {
                    moveModeImage.setSelected(false);
                    controlModeImage.setSelected(true);
                    powerModeTxt.setText("控制模式");
                    SpUtils.putBoolean(getContext(), "isLock", true);

                } else {
                    controlModeImage.setSelected(isLock);
                    moveModeImage.setSelected(!isLock);
                    powerModeTxt.setText("拖动模式");
                    SpUtils.putBoolean(getContext(), "isLock", false);
                }

                break;
            case RobotConfig.ROBOT_VOICE_SIGNAL:
                if (waveView != null) {
                    switch (robotSignalEvent.action) {
                        case "start":
                            if (waveView != null) {
                                waveView.speechStarted();
                            }
                            break;
                        case "stop":
                            if (waveView != null) {
                                waveView.speechPaused();
                            }
                            break;
                        default:
                            break;
                    }
                }
                break;
            case RobotConfig.WAKE_WORD_DIALOG: //通过唤醒词发起新的对话后,清空首页上次对话数据
                wakeWordDialogToDo();
                break;
            case RobotConfig.UPDATE_ROBOT_MODE_DISPLAY: //通过唤醒词发起新的对话后,清空首页上次对话数据
                updateRobotInfo();
                break;
            default:
                break;
        }
    }

    /**
     * 通过唤醒词唤醒设备,并发起对话后
     */
    private void wakeWordDialogToDo() {
        //机器人移动中被唤醒后,语音提示 "不好意思，小白在执行任务哦"
        if (RobotInfoUtils.getRobotRunningStatus() == "2") {
            EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, "不好意思，小白在执行任务哦"));
            //显示播报信息到首页记录
            EventBus.getDefault().post(new NavigationTip("不好意思，小白在执行任务哦"));
        } else {
            //首页对话列表数据初始化
            recordAdapter.getData().clear();
            recordAdapter.getData().add(new RecordInfo(0, getString(R.string.show_t1)));
            recordAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 移动信号强度变化更新
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(IflytekAnswerData iflytekAnswerData) {
        LogUtils.d(TAG, "IflytekAnswerData:" + iflytekAnswerData.getText());
        if (!TextUtils.isEmpty(iflytekAnswerData.getText())) {
//            voiceTxt.setText(iflytekAnswerData.getText());
        }
    }

    /**
     * speech语音输入,输出信息页面展示
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(MessageBean messageBean) {
        String voiceMessageText = messageBean.getText().trim();
        LogUtils.d(TAG, "MessageBean:NormalHomeFragment----->" + voiceMessageText);
        if (!TextUtils.isEmpty(voiceMessageText)) {
            recordInfo = new RecordInfo();
            if (messageBean.getType() == MessageBean.TYPE_INPUT) {
                recordInfo.setType(1);
            } else if (messageBean.getType() == MessageBean.TYPE_OUTPUT) {
                recordInfo.setType(0);
            }
            recordInfo.setContent(voiceMessageText.trim());
            LogUtils.json(TAG, "本次对话记录数据==> " + JSON.toJSONString(recordInfo));
            recordAdapter.updateData(recordInfo);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(SignalDataEvent signalDataEvent) {
        switch (signalDataEvent.type) {
            case MSG_STOP_TASK:
                if (myDialog != null) {
                    if (myDialog.isShowing()) {
                        myDialog.dismiss();
                    }
                    myDialog = null;
                }
                FilePlayEvent filePlayEvent = new FilePlayEvent(RobotConfig.FILE_PLAY_CANCEL, "3");
                EventBus.getDefault().post(filePlayEvent);
                break;
        }
    }

    /**
     *
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(VideoMessageEve eve) {
        switch (eve.getType()) {
            case MSG_ISLOCK_CHANGE:
                updateRobotInfo();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(OperationEvent operationEvent) {
        switch (operationEvent.getType()) {
            case CANCEL_PLAY_TASK:
                cancelPlayTask();
                break;
        }
    }

    /**
     * 取消播放视频  pdf  txt任务通过语音指令
     */
    public void cancelPlayTask() {
        String operationType = SpUtils.getString(MyApp.getContext(), "operationType", null);
        LogUtils.d("CommandTodo", "取消播放类任务  " + (myDialog.isShowing()) + "   " + (myDialog != null));
        if (myDialog != null) {
            myDialog.dismiss();
            myDialog = null;
        }
        if (operationType.equals("2")) {
            FilePlayEvent filePlayEvent = new FilePlayEvent(RobotConfig.FILE_PLAY_CANCEL, "2");
            EventBus.getDefault().post(filePlayEvent);
        } else if (operationType.equals("3")) {
            FilePlayEvent filePlayEvent = new FilePlayEvent(RobotConfig.FILE_PLAY_CANCEL, "3");
            EventBus.getDefault().post(filePlayEvent);
        } else if (operationType.equals("4")) {
            FilePlayEvent filePlayEvent = new FilePlayEvent(RobotConfig.FILE_PLAY_CANCEL, "4");
            EventBus.getDefault().post(filePlayEvent);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(SpeakEvent eve) {
        switch (eve.getType()) {
            case MSG_TTS_SHOW_DIALOG:
//                if (myDialog != null) {
//                    if (myDialog.isShowing()) {
//                        myDialog.dismiss();
//                    }
//                    myDialog = null;
//                }
//                myDialog = new MyDialog(getContext());
//                myDialog.setTitle("播放提示");
//                myDialog.setMessage(eve.getText() + "播放结束！");
//                myDialog.setPositiveOnclicListener("再放一遍", () -> {
//                    myDialog.dismiss();
//                    FilePlayEvent filePlayEvent = new FilePlayEvent(FILE_PLAY_OK, "4");
//                    EventBus.getDefault().post(filePlayEvent);
//                });
//                myDialog.setNegativeOnclicListener("确认", () -> {
//                    myDialog.dismiss();
//                    SpeakEvent speakEvent = new SpeakEvent();
//                    speakEvent.setType(MSG_TTS_CANCEL);
//                    speakEvent.setAction("txt_finished");
//                    EventBus.getDefault().post(speakEvent);
//                    //播放结束弹窗提示,点击确认按钮后,机器人运行状态切换,充电状态大于执行操作中状态
//                    if (SpUtils.getBoolean(getContext(), "isCharge", false)) {
//                        saveRobotstatus(5);
//                    } else {
//                        saveRobotstatus(1);
//                    }
//                });
//                myDialog.show();
                break;
        }
    }


    /**
     *
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(DaemonEvent daemonEvent) {
        switch (daemonEvent.getType()) {
            case RobotConfig.TASK_DIALOG:
                LogUtils.d(TAG, "TASK_DIALOG !!!!!    daemonEvent.getAction() = " + daemonEvent.getAction());
//                if (daemonEvent.getAction().equals("2")) {
//                    String filename = daemonEvent.extra;
//                    LogUtils.d("LauncherActivity", "filename = " + filename);
//                    if (myDialog != null) {
//                        myDialog.dismiss();
//                        myDialog = null;
//                    }
//                    myDialog = new MyDialog(getContext());
//                    myDialog.setTitle("播放提示");
//                    myDialog.setMessage("请问现在开始播放" + filename + "吗？");
//                    myDialog.setPositiveOnclicListener("开始播放", () -> {
//                        myDialog.dismiss();
//                        FilePlayEvent filePlayEvent = new FilePlayEvent(RobotConfig.FILE_PLAY_OK, "2");
//                        EventBus.getDefault().post(filePlayEvent);
//                    });
//                    myDialog.setNegativeOnclicListener("取消", () -> {
//                        myDialog.dismiss();
//                        FilePlayEvent filePlayEvent = new FilePlayEvent(RobotConfig.FILE_PLAY_CANCEL, "2");
//                        EventBus.getDefault().post(filePlayEvent);
//                    });
//                    myDialog.show();
//                } else if (daemonEvent.getAction().equals("3")) {
//                    String filename = daemonEvent.extra;
//                    LogUtils.d("LauncherActivity", "filename = " + filename);
//                    if (myDialog != null) {
//                        myDialog.dismiss();
//                        myDialog = null;
//                    }
//                    myDialog = new MyDialog(getContext());
//                    myDialog.setTitle("播放提示");
//                    myDialog.setMessage("请问现在开始播放" + filename + "吗？");
//                    myDialog.setPositiveOnclicListener("开始播放", () -> {
//                        myDialog.dismiss();
//                        FilePlayEvent filePlayEvent = new FilePlayEvent(RobotConfig.FILE_PLAY_OK, "3");
//                        EventBus.getDefault().post(filePlayEvent);
//                    });
//                    myDialog.setNegativeOnclicListener("取消", () -> {
//                        myDialog.dismiss();
//                        FilePlayEvent filePlayEvent = new FilePlayEvent(RobotConfig.FILE_PLAY_CANCEL, "3");
//                        EventBus.getDefault().post(filePlayEvent);
//                    });
//                    myDialog.show();
//                } else if (daemonEvent.getAction().equals("4")) {
//                    String filename = daemonEvent.extra;
//                    LogUtils.d("LauncherActivity", "filename = " + filename);
//                    if (myDialog != null) {
//                        myDialog.dismiss();
//                        myDialog = null;
//                    }
//                    myDialog = new MyDialog(getContext());
//                    myDialog.setTitle("播放提示");
//                    myDialog.setMessage("请问现在开始播放" + filename + "吗？");
//                    myDialog.setPositiveOnclicListener("开始播放", () -> {
//                        myDialog.dismiss();
//                        FilePlayEvent filePlayEvent = new FilePlayEvent(RobotConfig.FILE_PLAY_OK, "4");
//                        EventBus.getDefault().post(filePlayEvent);
//                    });
//                    myDialog.setNegativeOnclicListener("取消", () -> {
//                        myDialog.dismiss();
//                        FilePlayEvent filePlayEvent = new FilePlayEvent(RobotConfig.FILE_PLAY_CANCEL, "4");
//                        EventBus.getDefault().post(filePlayEvent);
//                    });
//                    myDialog.show();
//                }
                break;
            default:
                break;
        }
    }


    @Override
    public void onStart() {
        LogUtils.d("djj", "onStart ！！！！！");
        updateRobotInfo();
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.d(TAG, "onResume !!!!!! ");
        //服务端是否配置自动回充工作
        String specificWorkflow = SpUtils.getString(MyApp.getContext(), RECHARGE_SPECIFIC_WORKFLOW, null);
        LogUtils.d("specificWorkflow", "NormalHomeFragment: onResume  " + specificWorkflow + "    " + TextUtils.isEmpty(specificWorkflow) + " ,specificWorkflow == null,   " + (specificWorkflow == null));
        if (specificWorkflow != null) {
            if (specificWorkflow.equals("null")) {
                //服务端没有配置自动回充工作流
                withoutRechargeModeSwitch.setVisibility(View.VISIBLE);
                withoutRechargeAutoMission.setVisibility(View.VISIBLE);
                constraintLayoutModeSwitch.setVisibility(View.GONE);
                constraintLayoutAutoRecharge.setVisibility(View.GONE);
                constraintLayoutAutoMission.setVisibility(View.GONE);
            } else {
                //服务端有配置自动回充工作流
                withoutRechargeModeSwitch.setVisibility(View.GONE);
                withoutRechargeAutoMission.setVisibility(View.GONE);
                constraintLayoutModeSwitch.setVisibility(View.VISIBLE);
                constraintLayoutAutoRecharge.setVisibility(View.VISIBLE);
                constraintLayoutAutoMission.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (waveView != null) {
            waveView.destroy();
            waveView = null;
        }
        EventBus.getDefault().unregister(this);
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
