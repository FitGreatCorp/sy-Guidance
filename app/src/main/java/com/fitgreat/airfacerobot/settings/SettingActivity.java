package com.fitgreat.airfacerobot.settings;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotBrainService;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.business.ApiDomainManager;
import com.fitgreat.airfacerobot.constants.Constants;
import com.fitgreat.airfacerobot.launcher.ui.activity.AppListActivity;
import com.fitgreat.airfacerobot.launcher.ui.activity.RobotInitActivity;
import com.fitgreat.airfacerobot.launcher.utils.ToastUtils;
import com.fitgreat.airfacerobot.launcher.utils.WebPageUtils;
import com.fitgreat.airfacerobot.launcher.widget.InputDialog;
import com.fitgreat.airfacerobot.launcher.widget.MyDialog;
import com.fitgreat.airfacerobot.remotesignal.model.RobotInfoData;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.airfacerobot.versionupdate.VersionUtils;
import com.fitgreat.archmvp.base.ui.MvpBaseActivity;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.PhoneInfoUtils;
import com.fitgreat.archmvp.base.util.RouteUtils;
import com.fitgreat.archmvp.base.util.SpUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 设置activity<p>
 *
 * @author zixuefei
 * @since 2020/3/11 0011 10:14
 */
public class SettingActivity extends MvpBaseActivity {
    //    @BindView(R.id.settings_voice_point_switch)
//    Switch voicePointSwitch;
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
    @BindView(R.id.et_hello)
    EditText et_hello;
    @BindView(R.id.de_time)
    EditText de_time;

    LinearLayout info;
    LinearLayout hello;
    InputMethodManager inputMethodManager;
    private InputDialog inputDialog;
    private MyDialog myDialog;
    private String STRING_HELLO;
    private static final String TAG = "SettingActivity";

    @Override
    public int getLayoutResource() {
        return R.layout.activity_settings;
    }

    @Override
    public void initData() {
        initImmersionBar(true);
        titleBar.setText("艾菲仕设置");
        inputDialog = new InputDialog(this);
        rebootLayout.setVisibility(View.VISIBLE);
        inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        info = findViewById(R.id.inc_info);
        hello = findViewById(R.id.inc_hello);
//        voicePointSwitch.setChecked(RobotInfoUtils.isPlayVoiceEnabled());
//        voicePointSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
//            RobotInfoUtils.setPlayVoiceEnabled(isChecked);
//        });
        RobotInfoData robotInfoData = RobotInfoUtils.getRobotInfo();
        if (robotInfoData != null) {
            robotName.setText(robotInfoData.getF_Name());
            robotDepartment.setText(robotInfoData.getF_Account());
        }

        robotSerialNumber.setText(RobotInfoUtils.getAirFaceDeviceId());
        robotAppVersion.setText("V " + VersionUtils.getVersionName(this));
        robotOsVersion.setText("Android " + PhoneInfoUtils.getSystemVersion());
        LogUtils.d(TAG,"HARDVERSION = "+RobotInfoUtils.getHardwareVersion());
        robotHardwareVersion.setText("V " + RobotInfoUtils.getHardwareVersion().replace("v", "").replace("V", ""));

        if (TextUtils.equals(SpUtils.getString(MyApp.getContext(), ApiDomainManager.ENVIRONMENT_CONFIG_KEY, "debug"), "debug")) {
            Drawable selected = this.getDrawable(R.mipmap.btn_selected);
            selected.setBounds(0,0,selected.getMinimumWidth(),selected.getMinimumHeight());
            Drawable select = this.getDrawable(R.mipmap.btn_select);
            select.setBounds(0,0,selected.getMinimumWidth(),selected.getMinimumHeight());
            testRb.setCompoundDrawables(selected,null,null,null);
            productRb.setCompoundDrawables(select,null,null,null);
            developLayout.setVisibility(View.VISIBLE);
//            shutdownLayout.setVisibility(View.VISIBLE);
        } else {
            Drawable selected = this.getDrawable(R.mipmap.btn_selected);
            selected.setBounds(0,0,selected.getMinimumWidth(),selected.getMinimumHeight());
            Drawable select = this.getDrawable(R.mipmap.btn_select);
            select.setBounds(0,0,selected.getMinimumWidth(),selected.getMinimumHeight());
            testRb.setCompoundDrawables(select,null,null,null);
            productRb.setCompoundDrawables(selected,null,null,null);
        }

        STRING_HELLO = SpUtils.getString(getContext(), "hello_string", "");
        et_hello.setText(STRING_HELLO);


        if (SpUtils.getInt(getContext(), "de_time", 10) != 10) {
            de_time.setText(String.valueOf(SpUtils.getInt(getContext(), "de_time", 10)));
        } else {
            de_time.setText("10");
        }
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
    public Object createPresenter() {
        return null;
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
            R.id.settings_device_id_layout, R.id.settings_app_list_layout, R.id.btn_save,R.id.test_domains_radio,R.id.product_domains_radio})
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
                if(myDialog!=null){
                    if(myDialog.isShowing()){
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
                LogUtils.d(TAG, "btn_save !!!!!!!");
                SpUtils.putString(getContext(), "hello_string", et_hello.getText().toString());
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                if (de_time.getText().toString().equals("")) {
                    SpUtils.putInt(getContext(), "de_time", 10);
                } else {
                    if (Integer.valueOf(de_time.getText().toString().trim()) < 10) {
                        SpUtils.putInt(getContext(), "de_time", 10);
                        de_time.setText("10");
                        Toast.makeText(getContext(), "间隔时间不能小于10秒!", Toast.LENGTH_SHORT).show();
                    }else{
                        SpUtils.putInt(getContext(), "de_time", Integer.valueOf(de_time.getText().toString().trim()));
                        Toast.makeText(getContext(), "设置成功！", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.test_domains_radio:
                if(!TextUtils.equals(SpUtils.getString(MyApp.getContext(), ApiDomainManager.ENVIRONMENT_CONFIG_KEY, "debug"), "debug")){
                    if(myDialog!=null){
                        if(myDialog.isShowing()){
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
                            selected.setBounds(0,0,selected.getMinimumWidth(),selected.getMinimumHeight());
                            Drawable select = SettingActivity.this.getDrawable(R.mipmap.btn_select);
                            select.setBounds(0,0,selected.getMinimumWidth(),selected.getMinimumHeight());
                            testRb.setCompoundDrawables(selected,null,null,null);
                            productRb.setCompoundDrawables(select,null,null,null);

                            ToastUtils.showSmallToast("服务器环境已切换，即将重启生效");

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    RouteUtils.sendDaemonBroadcast(SettingActivity.this, Constants.ACTION_REBOOT, null);
                                }
                            },1500);
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
                if(TextUtils.equals(SpUtils.getString(MyApp.getContext(), ApiDomainManager.ENVIRONMENT_CONFIG_KEY, "debug"), "debug")){
                    if(myDialog!=null){
                        if(myDialog.isShowing()){
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
                            selected.setBounds(0,0,selected.getMinimumWidth(),selected.getMinimumHeight());
                            Drawable select = SettingActivity.this.getDrawable(R.mipmap.btn_select);
                            select.setBounds(0,0,selected.getMinimumWidth(),selected.getMinimumHeight());
                            testRb.setCompoundDrawables(select,null,null,null);
                            productRb.setCompoundDrawables(selected,null,null,null);

                            ToastUtils.showSmallToast("服务器环境已切换，即将重启生效");

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    RouteUtils.sendDaemonBroadcast(SettingActivity.this, Constants.ACTION_REBOOT, null);
                                }
                            },1500);
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
            default:
                break;
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
