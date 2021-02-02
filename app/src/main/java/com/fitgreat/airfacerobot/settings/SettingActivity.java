package com.fitgreat.airfacerobot.settings;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aispeech.dui.dds.DDS;
import com.aispeech.dui.dds.agent.wakeup.word.WakeupWord;
import com.aispeech.dui.dds.exceptions.DDSNotInitCompleteException;
import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotBrainService;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.business.ApiDomainManager;
import com.fitgreat.airfacerobot.constants.Constants;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.launcher.ui.activity.AppListActivity;
import com.fitgreat.airfacerobot.launcher.ui.activity.RobotInitActivity;
import com.fitgreat.airfacerobot.launcher.utils.PinYinUtil;
import com.fitgreat.airfacerobot.launcher.utils.ToastUtils;
import com.fitgreat.airfacerobot.launcher.utils.WebPageUtils;
import com.fitgreat.airfacerobot.launcher.widget.InputDialog;
import com.fitgreat.airfacerobot.launcher.widget.MyDialog;
import com.fitgreat.airfacerobot.model.InitEvent;
import com.fitgreat.airfacerobot.model.WorkflowEntity;
import com.fitgreat.airfacerobot.remotesignal.model.RobotInfoData;
import com.fitgreat.airfacerobot.remotesignal.model.SignalDataEvent;
import com.fitgreat.airfacerobot.settings.adapter.WorkFlowListAdapter;
import com.fitgreat.airfacerobot.settings.presenter.SettingsPresenter;
import com.fitgreat.airfacerobot.settings.view.SettingsView;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.airfacerobot.versionupdate.VersionUtils;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.PhoneInfoUtils;
import com.fitgreat.archmvp.base.util.RouteUtils;
import com.fitgreat.archmvp.base.util.SpUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TWO;
import static com.fitgreat.airfacerobot.constants.RobotConfig.BROADCAST_GREET_SWITCH_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_FREE_OPERATION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.FREE_OPERATION_SELECT_POSITION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.IS_CONTROL_MODEL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_FLOATING_BALL;

/**
 * 设置activity<p>
 *
 * @author zixuefei
 * @since 2020/3/11 0011 10:14
 */
public class SettingActivity extends MvpBaseActivity<SettingsView, SettingsPresenter> implements SettingsView {
    @BindView(R.id.title_bar_title)
    TextView titleBar;
    @BindView(R.id.settings_robot_name)
    TextView robotName;
    @BindView(R.id.settings_robot_department)
    TextView robotDepartment;
    @BindView(R.id.settings_robot_model)
    TextView robotModel;
    @BindView(R.id.settings_robot_brand_name)
    TextView robotBrandName;
    @BindView(R.id.settings_robot_serial_number)
    TextView robotSerialNumber;
    @BindView(R.id.settings_robot_os_version)
    TextView robotOsVersion;
    @BindView(R.id.settings_robot_hardware_version)
    TextView robotHardwareVersion;
    @BindView(R.id.settings_robot_app_version)
    TextView robotAppVersion;
    @BindView(R.id.settings_robot_official_website)
    TextView robotOfficialWebsite;
    @BindView(R.id.settings_develop_layout)
    LinearLayout developLayout;
    @BindView(R.id.settings_reboot_layout)
    LinearLayout rebootLayout;
    @BindView(R.id.settings_shutdown_layout)
    LinearLayout shutdownLayout;
    @BindView(R.id.settings_app_list_layout)
    LinearLayout appListLayout;
    @BindView(R.id.test_domains_radio)
    TextView testRb;
    @BindView(R.id.product_domains_radio)
    TextView productRb;
    @BindView(R.id.ll_info)
    LinearLayout ll_info;
    @BindView(R.id.ll_voice)
    LinearLayout ll_voice;
    @BindView(R.id.btn_save)
    Button btn_save;
    @BindView(R.id.en_et_hello)
    EditText en_et_hello;
    @BindView(R.id.et_hello)
    EditText et_hello;
    @BindView(R.id.drag_mode_radio)
    TextView dragModeRadio;
    @BindView(R.id.control_mode_radio)
    TextView controlModeRadio;
    @BindView(R.id.broadcast_greet_radioGroup)
    RadioGroup broadcastGreetRadioGroup;
    @BindView(R.id.broadcast_greet_yes)
    RadioButton broadcastGreetYes;
    @BindView(R.id.broadcast_greet_no)
    RadioButton broadcastGreetNo;
    @BindView(R.id.free_operation)
    TextView freeOperation;
    @BindView(R.id.switch_free_operation_log)
    ImageView switchFreeOperationLog;
    @BindView(R.id.wake_word)
    EditText wake_word;

    LinearLayout info;
    LinearLayout hello;
    InputMethodManager inputMethodManager;
    private InputDialog inputDialog;
    private MyDialog myDialog;
    private String STRING_HELLO, EN_STRING_HELLO;
    private static final String TAG = "SettingActivity";
    private Drawable selectedDrawable;
    private Drawable normalDrawable;
    private boolean broadcastGreetSwitchTag;
    private List<WorkflowEntity> mWorkflowEntityList;
    private String currentFreeOperation;
    private WorkflowEntity workflowEntity;
    private PopupWindow popupWindow;

    @Override
    public int getLayoutResource() {
        return R.layout.activity_settings;
    }

    @Override
    public void initData() {
        //获取工作流列表
        mPresenter.getWorkflowList();
        initImmersionBar(true);
        titleBar.setText(MvpBaseActivity.getActivityContext().getString(R.string.setup_module_title));
        inputDialog = new InputDialog(this);
        rebootLayout.setVisibility(View.VISIBLE);
        inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        info = findViewById(R.id.inc_info);
        hello = findViewById(R.id.inc_hello);
        RobotInfoData robotInfoData = RobotInfoUtils.getRobotInfo();
        if (robotInfoData != null) {
            robotName.setText(robotInfoData.getF_Name());
            robotDepartment.setText(robotInfoData.getF_Account());
        }
        robotSerialNumber.setText(RobotInfoUtils.getAirFaceDeviceId());
        robotAppVersion.setText("V " + VersionUtils.getVersionName(this));
        robotOsVersion.setText("Android " + PhoneInfoUtils.getSystemVersion());
        robotHardwareVersion.setText("V " + RobotInfoUtils.getHardwareVersion().replace("v", "").replace("V", ""));
        if (TextUtils.equals(SpUtils.getString(MyApp.getContext(), ApiDomainManager.ENVIRONMENT_CONFIG_KEY, "debug"), "debug")) {
            Drawable selected = this.getDrawable(R.mipmap.btn_selected);
            selected.setBounds(0, 0, selected.getMinimumWidth(), selected.getMinimumHeight());
            Drawable select = this.getDrawable(R.mipmap.btn_select);
            select.setBounds(0, 0, selected.getMinimumWidth(), selected.getMinimumHeight());
            testRb.setCompoundDrawables(selected, null, null, null);
            productRb.setCompoundDrawables(select, null, null, null);
            developLayout.setVisibility(View.VISIBLE);
        } else {
            Drawable selected = this.getDrawable(R.mipmap.btn_selected);
            selected.setBounds(0, 0, selected.getMinimumWidth(), selected.getMinimumHeight());
            Drawable select = this.getDrawable(R.mipmap.btn_select);
            select.setBounds(0, 0, selected.getMinimumWidth(), selected.getMinimumHeight());
            testRb.setCompoundDrawables(select, null, null, null);
            productRb.setCompoundDrawables(selected, null, null, null);
        }
        //中英文迎宾语默认显示值
        STRING_HELLO = SpUtils.getString(getContext(), "hello_string", "");
        EN_STRING_HELLO = SpUtils.getString(getContext(), "en_hello_string", "");
        et_hello.setText(STRING_HELLO);
        en_et_hello.setText(EN_STRING_HELLO);
        if (SpUtils.getInt(getContext(), "de_time", 10) != 10) {
//            de_time.setText(String.valueOf(SpUtils.getInt(getContext(), "de_time", 10)));
        } else {
//            de_time.setText("10");
        }
        //机器人工作模式状态
        boolean isControlModel = SpUtils.getBoolean(MyApp.getContext(), IS_CONTROL_MODEL, false);
        selectedDrawable = SettingActivity.this.getDrawable(R.mipmap.btn_selected);
        selectedDrawable.setBounds(0, 0, selectedDrawable.getMinimumWidth(), selectedDrawable.getMinimumHeight());
        normalDrawable = SettingActivity.this.getDrawable(R.mipmap.btn_select);
        normalDrawable.setBounds(0, 0, normalDrawable.getMinimumWidth(), normalDrawable.getMinimumHeight());
        if (isControlModel) {
            controlModeRadio.setCompoundDrawables(selectedDrawable, null, null, null);
            dragModeRadio.setCompoundDrawables(normalDrawable, null, null, null);
        } else {
            dragModeRadio.setCompoundDrawables(selectedDrawable, null, null, null);
            controlModeRadio.setCompoundDrawables(normalDrawable, null, null, null);
        }
        //是否播放迎宾语
        broadcastGreetSwitchTag = SpUtils.getBoolean(MyApp.getContext(), BROADCAST_GREET_SWITCH_TAG, false);
        //进入设置页面播放迎宾语开关显示状态
        if (broadcastGreetSwitchTag) {
            broadcastGreetYes.setChecked(true);
        } else {
            broadcastGreetNo.setChecked(true);
        }
        //设置播放迎宾语开关
        broadcastGreetRadioGroup.setOnCheckedChangeListener(((group, checkedId) -> {
            switch (checkedId) {
                case R.id.broadcast_greet_yes:
                    SpUtils.putBoolean(MyApp.getContext(), BROADCAST_GREET_SWITCH_TAG, true);
                    break;
                case R.id.broadcast_greet_no:
                    SpUtils.putBoolean(MyApp.getContext(), BROADCAST_GREET_SWITCH_TAG, false);
                    break;
            }
        }));
        //空闲工作流内容
        currentFreeOperation = SpUtils.getString(MyApp.getContext(), CURRENT_FREE_OPERATION, "null");
        LogUtils.d(DEFAULT_LOG_TAG, "当前空闲时工作流信息::" + currentFreeOperation);
        if (!"null".equals(currentFreeOperation)) {
            workflowEntity = JSON.parseObject(currentFreeOperation, WorkflowEntity.class);
            freeOperation.setText(workflowEntity.getF_Name());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //显示应用返回首页悬浮按钮\
        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(false);
        EventBus.getDefault().post(initUiEvent);
    }

    @Override
    public void disconnectNetWork() {
        RouteUtils.goToActivity(SettingActivity.this, RobotInitActivity.class);
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
        RouteUtils.goToActivity(SettingActivity.this, RobotInitActivity.class);
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

    @Override
    public SettingsPresenter createPresenter() {
        return new SettingsPresenter();
    }


    /**
     * Settings.ACTION_WIFI_SETTINGS  打开WiFi设置
     * Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS 打开开发者选项
     * Settings.ACTION_BLUETOOTH_SETTINGS 打开蓝牙设置
     * Settings.ACTION_DATE_SETTINGS   打开时间日期设置
     * Settings.ACTION_INPUT_METHOD_SETTINGS 打开输入法设置
     * Settings.ACTION_SOUND_SETTINGS  打开声音设置
     * Settings.ACTION_APPLICATION_SETTINGS  打开应用信息
     * Settings.ACTION_CAST_SETTINGS  打开投屏设置
     * Settings.ACTION_LOCALE_SETTINGS  打开语言设置
     * Settings.ACTION_DISPLAY_SETTINGS  打开显示设置
     * Settings.ACTION_WIRELESS_SETTINGS 打开热点，移动 Ethernet网络设置
     * 电池不支持打开二级界面
     */
    @OnClick({R.id.ll_info, R.id.ll_voice, R.id.title_bar_title, R.id.settings_change_map_layout, R.id.settings_wireless_layout,
            R.id.settings_bluetooth_layout, R.id.settings_display_layout, R.id.settings_sound_layout
            , R.id.settings_apps_layout, R.id.settings_date_time_layout, R.id.settings_language_layout,
            R.id.settings_inputmethod_layout, R.id.settings_develop_layout, R.id.settings_reboot_layout,
            R.id.settings_shutdown_layout, R.id.settings_robot_official_website_layout, R.id.settings_wifi_layout,
            R.id.settings_device_id_layout, R.id.settings_app_list_layout, R.id.btn_save, R.id.test_domains_radio, R.id.product_domains_radio,
            R.id.drag_mode_radio, R.id.control_mode_radio, R.id.free_operation, R.id.main_item_page})
    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.ll_info:
                info.setVisibility(View.VISIBLE);
                hello.setVisibility(View.GONE);
                break;
            case R.id.ll_voice:
                info.setVisibility(View.GONE);
                hello.setVisibility(View.VISIBLE);
                break;
            case R.id.settings_wifi_layout:
                RouteUtils.goToActivity(this, Settings.ACTION_WIFI_SETTINGS);
                break;
            case R.id.settings_wireless_layout:
                RouteUtils.goToActivity(this, Settings.ACTION_WIRELESS_SETTINGS);
                break;
            case R.id.settings_robot_official_website_layout:
                WebPageUtils.goToWebActivity(this, "", "http://www.fitgreat.cn/Home/Index", false, true);
                break;
            case R.id.settings_change_map_layout:
                ToastUtils.showSmallToast("暂不支持地图设置");
                break;
            case R.id.settings_bluetooth_layout:
                RouteUtils.goToActivity(this, Settings.ACTION_BLUETOOTH_SETTINGS);
                break;
            case R.id.settings_display_layout:
                RouteUtils.goToActivity(this, Settings.ACTION_DISPLAY_SETTINGS);
                break;
            case R.id.settings_sound_layout:
                RouteUtils.goToActivity(this, Settings.ACTION_SOUND_SETTINGS);
                break;
            case R.id.settings_apps_layout:
                RouteUtils.goToActivity(this, Settings.ACTION_APPLICATION_SETTINGS);
                break;
            case R.id.settings_date_time_layout:
                RouteUtils.goToActivity(this, Settings.ACTION_DATE_SETTINGS);
                break;
            case R.id.settings_language_layout:
                RouteUtils.goToActivity(this, Settings.ACTION_LOCALE_SETTINGS);
                break;
            case R.id.settings_inputmethod_layout:
                RouteUtils.goToActivity(this, Settings.ACTION_INPUT_METHOD_SETTINGS);
                break;
            case R.id.settings_device_id_layout:
                startCheckSelf();
                break;
            case R.id.settings_develop_layout:
                RouteUtils.goToActivity(this, Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                break;
            case R.id.settings_app_list_layout:
                RouteUtils.goToActivity(this, AppListActivity.class);
                break;
            case R.id.settings_reboot_layout:
                if (myDialog != null) {
                    if (myDialog.isShowing()) {
                        myDialog.dismiss();
                    }
                    myDialog = null;
                }
                myDialog = new MyDialog(this);
                myDialog.setTitle("重新启动");
                myDialog.setMessage("请确认是否要进行系统重启");
                myDialog.setCanceledOnTouchOutside(false);
                myDialog.setPositiveOnclicListener("确定", new MyDialog.onPositiveClicListener() {
                    @Override
                    public void onPositiveClick() {
                        myDialog.dismiss();
                        ToastUtils.showSmallToast("重启Android系统");
                        RouteUtils.sendDaemonBroadcast(SettingActivity.this, Constants.ACTION_REBOOT, null);
                    }
                });
                myDialog.setNegativeOnclicListener("取消", new MyDialog.onNegativeClickListener() {
                    @Override
                    public void onNegativeClick() {
                        myDialog.dismiss();

                    }
                });
                myDialog.show();
                break;
            case R.id.settings_shutdown_layout:
                ToastUtils.showSmallToast("关闭Android系统");
                RouteUtils.sendDaemonBroadcast(this, Constants.ACTION_SHUTDOWN, null);
                break;
            case R.id.btn_save:
                if ((et_hello.getText().toString().equals("")) || TextUtils.isEmpty(et_hello.getText().toString())) {
                    SpUtils.putString(getContext(), "hello_string", null);
                } else {
                    SpUtils.putString(getContext(), "hello_string", et_hello.getText().toString());
                }
                if ((en_et_hello.getText().toString().equals("")) || TextUtils.isEmpty(en_et_hello.getText().toString())) {
                    SpUtils.putString(getContext(), "en_hello_string", null);
                } else {
                    SpUtils.putString(getContext(), "en_hello_string", en_et_hello.getText().toString());
                }
                LogUtils.d(DEFAULT_LOG_TWO, "添加唤醒词中文::" + (wake_word.getText().toString()) + "  ::" +(wake_word.getText().toString().equals("")));
                if (!TextUtils.isEmpty(wake_word.getText().toString()) && !(wake_word.getText().toString().equals(""))) {
                    updateWakeupWordList(wake_word.getText().toString());
                }
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                ToastUtils.showSmallToast("设置成功");
                break;
            case R.id.test_domains_radio:
                if (!TextUtils.equals(SpUtils.getString(MyApp.getContext(), ApiDomainManager.ENVIRONMENT_CONFIG_KEY, "debug"), "debug")) {
                    if (myDialog != null) {
                        if (myDialog.isShowing()) {
                            myDialog.dismiss();
                        }
                        myDialog = null;
                    }
                    myDialog = new MyDialog(this);
                    myDialog.setTitle("切换环境");
                    myDialog.setMessage("请确认是否要切换到测试环境");
                    myDialog.setCanceledOnTouchOutside(false);
                    myDialog.setPositiveOnclicListener("确定", new MyDialog.onPositiveClicListener() {
                        @Override
                        public void onPositiveClick() {
                            myDialog.dismiss();
                            SpUtils.putString(SettingActivity.this, ApiDomainManager.ENVIRONMENT_CONFIG_KEY, "debug");
                            Bundle test = new Bundle();
                            test.putString("type", "environment");
                            test.putString("action", "debug");
                            RouteUtils.sendDaemonBroadcast(SettingActivity.this, Constants.ACTION_DAEMON_MSG, test);
                            Drawable selected = SettingActivity.this.getDrawable(R.mipmap.btn_selected);
                            selected.setBounds(0, 0, selected.getMinimumWidth(), selected.getMinimumHeight());
                            Drawable select = SettingActivity.this.getDrawable(R.mipmap.btn_select);
                            select.setBounds(0, 0, selected.getMinimumWidth(), selected.getMinimumHeight());
                            testRb.setCompoundDrawables(selected, null, null, null);
                            productRb.setCompoundDrawables(select, null, null, null);
                            ToastUtils.showSmallToast("服务器环境已切换，即将重启生效");
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    RouteUtils.sendDaemonBroadcast(SettingActivity.this, Constants.ACTION_REBOOT, null);
                                }
                            }, 1500);
                        }
                    });
                    myDialog.setNegativeOnclicListener("取消", new MyDialog.onNegativeClickListener() {
                        @Override
                        public void onNegativeClick() {
                            myDialog.dismiss();
                        }
                    });
                    myDialog.show();
                }

                break;
            case R.id.product_domains_radio:
                if (TextUtils.equals(SpUtils.getString(MyApp.getContext(), ApiDomainManager.ENVIRONMENT_CONFIG_KEY, "debug"), "debug")) {
                    if (myDialog != null) {
                        if (myDialog.isShowing()) {
                            myDialog.dismiss();
                        }
                        myDialog = null;
                    }
                    myDialog = new MyDialog(this);
                    myDialog.setTitle("切换环境");
                    myDialog.setMessage("请确认是否要切换到正式环境");
                    myDialog.setCanceledOnTouchOutside(false);
                    myDialog.setPositiveOnclicListener("确定", new MyDialog.onPositiveClicListener() {
                        @Override
                        public void onPositiveClick() {
                            myDialog.dismiss();
                            SpUtils.putString(SettingActivity.this, ApiDomainManager.ENVIRONMENT_CONFIG_KEY, "product");
                            Bundle product = new Bundle();
                            product.putString("type", "environment");
                            product.putString("action", "product");
                            RouteUtils.sendDaemonBroadcast(SettingActivity.this, Constants.ACTION_DAEMON_MSG, product);

                            Drawable selected = SettingActivity.this.getDrawable(R.mipmap.btn_selected);
                            selected.setBounds(0, 0, selected.getMinimumWidth(), selected.getMinimumHeight());
                            Drawable select = SettingActivity.this.getDrawable(R.mipmap.btn_select);
                            select.setBounds(0, 0, selected.getMinimumWidth(), selected.getMinimumHeight());
                            testRb.setCompoundDrawables(select, null, null, null);
                            productRb.setCompoundDrawables(selected, null, null, null);

                            ToastUtils.showSmallToast("服务器环境已切换，即将重启生效");

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    RouteUtils.sendDaemonBroadcast(SettingActivity.this, Constants.ACTION_REBOOT, null);
                                }
                            }, 1500);
                        }
                    });
                    myDialog.setNegativeOnclicListener("取消", new MyDialog.onNegativeClickListener() {
                        @Override
                        public void onNegativeClick() {
                            myDialog.dismiss();
                        }
                    });
                    myDialog.show();
                }
                break;
            case R.id.drag_mode_radio:  //切换拖动模式
                switchRobotModel(false, controlModeRadio, dragModeRadio);
                break;
            case R.id.control_mode_radio: //切换控制模式
                switchRobotModel(true, controlModeRadio, dragModeRadio);
                break;
            case R.id.free_operation: //空闲操作选择
                choseFreeOperation();
                break;
            case R.id.main_item_page:
                if (popupWindow != null && popupWindow.isShowing()) {
                    //选择空闲操作logo切换朝下
                    switchFreeOperationLog.setImageDrawable(getDrawable(R.drawable.ic_triangle_down));
                    popupWindow.dismiss();
                    popupWindow = null;
                }
                break;
            default:
                break;
        }
    }

    public static void updateWakeupWordList(String chineseWord) {
        List<WakeupWord> mainWordList = new ArrayList<>();
        LogUtils.d(DEFAULT_LOG_TWO, "添加唤醒词中文::" + chineseWord + "  添加唤醒词拼音::" + PinYinUtil.cn2Spell(chineseWord));
        WakeupWord addMainWord = new WakeupWord()
                .setPinyin(PinYinUtil.cn2Spell(chineseWord))
                .setWord(chineseWord)
                .addGreeting("我在,请问有什么可以帮你?")
                .setThreshold("0.15");
        mainWordList.add(addMainWord);
        try {
            //添加默认唤醒词
            DDS.getInstance().getAgent().getWakeupEngine().addMainWakeupWords(mainWordList);
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
            LogUtils.e("CommandTodo", "添加唤醒词报错::" + e.getMessage());
        }
    }

    /**
     * 选择机器人空闲执行工作流  首页
     */
    public void choseFreeOperation() {
        if (mWorkflowEntityList != null && mWorkflowEntityList.size() != 0) {
            View inflate = View.inflate(this, R.layout.free_operation_list, null);
            popupWindow = new PopupWindow(this);
            //设置PopupWindow属性显示
            popupWindow.setContentView(inflate);
            popupWindow.setWidth(freeOperation.getWidth());
            popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setBackgroundDrawable(getDrawable(R.drawable.white_back));
            //设置列表数据并显示
            RecyclerView freeOperationRecyclerView = inflate.findViewById(R.id.free_operation_recyclerView);
            WorkFlowListAdapter workFlowListAdapter = new WorkFlowListAdapter(mWorkflowEntityList);
            freeOperationRecyclerView.setAdapter(workFlowListAdapter);
            freeOperationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            workFlowListAdapter.setOnItemClickListener((adapter, view, position) -> {
                SpUtils.putInt(MyApp.getContext(), FREE_OPERATION_SELECT_POSITION, position);
                workFlowListAdapter.notifyDataSetChanged();
                //缓存当前选择空闲工作流
                workflowEntity = (WorkflowEntity) adapter.getData().get(position);
                if (workflowEntity.getF_Id().equals("")) {
                    SpUtils.putString(MyApp.getContext(), CURRENT_FREE_OPERATION, "null");
                } else {
                    SpUtils.putString(MyApp.getContext(), CURRENT_FREE_OPERATION, JSON.toJSONString(workflowEntity));
                }
                //关闭popupWindow
                popupWindow.dismiss();
                //选择空闲操作logo切换朝下
                switchFreeOperationLog.setImageDrawable(getDrawable(R.drawable.ic_triangle_down));
                //显示当前选择空闲工作流名字
                freeOperation.setText(workflowEntity.getF_Name());
            });
            //选择空闲操作logo切换朝上
            switchFreeOperationLog.setImageDrawable(getDrawable(R.drawable.ic_triangle_top));
            //显示PopupWindow
            popupWindow.showAsDropDown(freeOperation);
        }
    }

    @Override
    public void showWorkflowList(List<WorkflowEntity> workflowEntityList) {
        mWorkflowEntityList = workflowEntityList;
    }

    /**
     * 切换机器人模式
     */
    public void switchRobotModel(boolean isControl, TextView controlView, TextView dragView) {
        SignalDataEvent moveMode = new SignalDataEvent(RobotConfig.MSG_CHANGE_POWER_LOCK, "");
        if (isControl) { //控制模式
            //更新JRos设置
            moveMode.setPowerlock(1);
            EventBus.getDefault().post(moveMode);
            //更新页面ui显示
            controlView.setCompoundDrawables(selectedDrawable, null, null, null);
            dragView.setCompoundDrawables(normalDrawable, null, null, null);
            //当前为控制模式
            SpUtils.putBoolean(MyApp.getContext(), IS_CONTROL_MODEL, true);
        } else { //拖动模式
            //更新JRos设置
            moveMode.setPowerlock(2);
            EventBus.getDefault().post(moveMode);
            //更新页面ui显示
            dragView.setCompoundDrawables(selectedDrawable, null, null, null);
            controlView.setCompoundDrawables(normalDrawable, null, null, null);
            //当前为拖动模式
            SpUtils.putBoolean(MyApp.getContext(), IS_CONTROL_MODEL, false);
        }
    }

    /**
     * 跳转自检初始化界面
     */
    private void startCheckSelf() {
        if (TextUtils.isEmpty(RobotInfoUtils.getAirFaceDeviceId())) {
            stopService(new Intent(this, RobotBrainService.class));
            if (inputDialog != null && !inputDialog.isShowing()) {
                inputDialog.show();
                inputDialog.setOnInputDoneListener(() -> {
                    RouteUtils.goToActivity(SettingActivity.this, RobotInitActivity.class);
                });
            }
        } else {
            ToastUtils.showSmallToast("当前设备ID:" + RobotInfoUtils.getAirFaceDeviceId());
        }
    }
}
