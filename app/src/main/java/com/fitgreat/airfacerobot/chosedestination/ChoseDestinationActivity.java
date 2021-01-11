package com.fitgreat.airfacerobot.chosedestination;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.chosedestination.presenter.ChoseDestinationPresenter;
import com.fitgreat.airfacerobot.chosedestination.view.ChoseDestinationView;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.launcher.ui.activity.RobotInitActivity;
import com.fitgreat.airfacerobot.launcher.widget.MyTipDialogSetText;
import com.fitgreat.airfacerobot.launcher.widget.TopTitleView;
import com.fitgreat.airfacerobot.model.ActionDdsEvent;
import com.fitgreat.airfacerobot.model.AskAnswerDataEvent;
import com.fitgreat.airfacerobot.model.InitEvent;
import com.fitgreat.airfacerobot.model.LocationEntity;
import com.fitgreat.airfacerobot.launcher.utils.CashUtils;
import com.fitgreat.airfacerobot.remotesignal.model.SignalDataEvent;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.archmvp.base.util.BitmapUtils;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.RouteUtils;
import com.fitgreat.archmvp.base.util.SpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.Constants.SINGLE_POINT_NAVIGATION;
import static com.fitgreat.airfacerobot.constants.Constants.currentChineseMapPath;
import static com.fitgreat.airfacerobot.constants.Constants.currentEnglishMapPath;
import static com.fitgreat.airfacerobot.constants.RobotConfig.AUTOMATIC_RECHARGE_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CLOSE_SELECT_NAVIGATION_PAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_LANGUAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.IS_CONTROL_MODEL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_FLOATING_BALL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PLAY_TASK_PROMPT_INFO;
import static com.fitgreat.airfacerobot.constants.RobotConfig.START_DDS_WAKE_TAG;

/**
 * 选择我要去目的地页面
 */
public class ChoseDestinationActivity extends MvpBaseActivity<ChoseDestinationView, ChoseDestinationPresenter> implements ChoseDestinationView, TopTitleView.BaseBackListener {

    private static final String TAG = "ChoseDestination";
    private List<LocationEntity> locationList;

    @BindView(R.id.chose_destination_container)
    RelativeLayout mChoseDestinationContainer;
    @BindView(R.id.chose_destination_map_back)
    ImageView mChoseDestinationMapBack;
    @BindView(R.id.chose_destination_top_title)
    TopTitleView mChoseDestinationTopTitle;


    private String currentLanguage;
    private CloseBroadcastReceiver closeBroadcastReceiver;
    private MyTipDialogSetText startNavigationDialog;

    @Override
    public ChoseDestinationPresenter createPresenter() {
        return new ChoseDestinationPresenter();
    }

    @Override
    public int getLayoutResource() {
        return R.layout.activity_chose_destination;
    }

    @Override
    protected void onResume() {
        LogUtils.d(DEFAULT_LOG_TAG,"onResume---------------ChoseDestinationActivity------------------");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解绑EventBus
        EventBus.getDefault().unregister(this);
        //解绑关闭页面广播
        unregisterReceiver(closeBroadcastReceiver);
    }

    @OnClick({R.id.chose_destination_top_title})
    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.chose_destination_top_title:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void initData() {
        //注册EventBus
        EventBus.getDefault().register(this);
        //根据当前语言加载展示地图
        currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "zh");
        //显示悬浮窗
        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(false);
        EventBus.getDefault().post(initUiEvent);
        //启动语音唤醒,打开one shot模式
        EventBus.getDefault().post(new ActionDdsEvent(START_DDS_WAKE_TAG, ""));
        //首页识别单点导航指令跳转到当前页面
        if (getIntent().hasExtra("bundle")) { //通过指令进入当前页面发起导航任务
            Bundle bundle = getIntent().getBundleExtra("bundle");
            LocationEntity locationEntity = (LocationEntity) bundle.getSerializable("LocationEntity");
            LogUtils.d(DEFAULT_LOG_TAG, "指令跳转选择导航页面: tipContent " + getNavigationTip(locationEntity) + " currentLanguage " + currentLanguage);
            //单点导航提示弹窗
            showDialogNavigation(true, getNavigationTip(locationEntity), MvpBaseActivity.getActivityContext().getString(R.string.answer_yes), MvpBaseActivity.getActivityContext().getString(R.string.answer_no), locationEntity);
        }
        //添加导航点按钮位置
        locationList = CashUtils.getLocationList();
        //去除不配置S_Y  S_X的位置信息
        for (int i = 0; i < locationList.size(); i++) {
            LocationEntity locationEntity = locationList.get(i);
            if ("null".equals(locationEntity.getS_Y()) || "null".equals(locationEntity.getS_X())) {
                locationList.remove(i);
            }
        }
        LogUtils.d(DEFAULT_LOG_TAG, "locationList:  " + JSON.toJSONString(locationList));
        //绘制页面,添加页面位置信息
        if (locationList != null && locationList.size() != 0) {
            flashView();
        }
        //注册监听关闭选择导航页面广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CLOSE_SELECT_NAVIGATION_PAGE);
        closeBroadcastReceiver = new CloseBroadcastReceiver();
        registerReceiver(closeBroadcastReceiver, intentFilter);
        //添加左上角返回按钮点击事件
        mChoseDestinationTopTitle.setBackKListener(this);
    }

    public void flashView() {
        //加载中英文地图
        if (currentLanguage.equals("zh")) {  //加载展示中文地图
            mChoseDestinationMapBack.setImageBitmap(BitmapUtils.getBitmapFromPath(currentChineseMapPath));
        } else {  //加载展示英文地图
            mChoseDestinationMapBack.setImageBitmap(BitmapUtils.getBitmapFromPath(currentEnglishMapPath));
        }
        for (int i = 0; i < locationList.size(); i++) {
            LocationEntity locationEntity = locationList.get(i);
            ImageButton imageButton = new ImageButton(this);
            imageButton.setBackgroundColor(getResources().getColor(R.color.transparent));
            imageButton.setImageDrawable(getResources().getDrawable(R.mipmap.ic_map_location_nor));
            //单点位置按钮添加点击事件
            imageButton.setOnClickListener(v -> {
                showDialogNavigation(true, getNavigationTip(locationEntity), getString(R.string.answer_yes), getString(R.string.answer_no), locationEntity);
                //自动回充工作流结束
                SpUtils.putBoolean(MyApp.getContext(), AUTOMATIC_RECHARGE_TAG, false);
            });
            //设置导航点按钮位置
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            if (!"null".equals(locationEntity.getS_Y()) && !"null".equals(locationEntity.getS_X())) {
                layoutParams.topMargin = Integer.parseInt(locationEntity.getS_Y()) - 85;
                layoutParams.leftMargin = Integer.parseInt(locationEntity.getS_X()) - 30;
                mChoseDestinationContainer.addView(imageButton, layoutParams);
            }
        }
    }

    @Override
    public Context getContext() {
        return null;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(AskAnswerDataEvent askAnswerDataEvent) {
        switch (askAnswerDataEvent.getCommandType()) {
            case SINGLE_POINT_NAVIGATION:
                LogUtils.d(DEFAULT_LOG_TAG, "----SINGLE_POINT_NAVIGATION---ChoseDestinationActivity-----");
                LocationEntity locationEntity = askAnswerDataEvent.getLocationEntity();
                showDialogNavigation(true, getNavigationTip(locationEntity), MvpBaseActivity.getActivityContext().getString(R.string.answer_yes), MvpBaseActivity.getActivityContext().getString(R.string.answer_no), locationEntity);
                break;
            default:
                break;
        }
    }

    @Override
    public void disconnectNetWork() {
        RouteUtils.goToActivity(ChoseDestinationActivity.this, RobotInitActivity.class);
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
        RouteUtils.goToActivity(ChoseDestinationActivity.this, RobotInitActivity.class);
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
    public void navigationSuccess() {
    }

    /**
     * 拼接获取选择导航提示弹窗内容信息
     *
     * @param locationEntity 导航位置信息
     * @return
     */
    public SpannableString getNavigationTip(LocationEntity locationEntity) {
        StringBuilder tipContent = new StringBuilder();
        tipContent.append(MvpBaseActivity.getActivityContext().getString(R.string.start_chose_destination_tip_one));
        if (currentLanguage.equals("zh")) {
            tipContent.append(locationEntity.getF_Name());
        } else {
            tipContent.append(locationEntity.getF_EName());
        }
        tipContent.append(getString(R.string.start_chose_destination_tip_two));
        //弹窗提示时,导航点位置信息加粗
        SpannableString spannableString = new SpannableString(tipContent);
        if (currentLanguage.equals("zh")) {
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), MvpBaseActivity.getActivityContext().getString(R.string.start_chose_destination_tip_one).length(), MvpBaseActivity.getActivityContext().getString(R.string.start_chose_destination_tip_one).length() + locationEntity.getF_Name().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), MvpBaseActivity.getActivityContext().getString(R.string.start_chose_destination_tip_one).length(), MvpBaseActivity.getActivityContext().getString(R.string.start_chose_destination_tip_one).length() + locationEntity.getF_EName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }

    @Override
    public void back() {
        finish();
    }

    private class CloseBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.d(TAG, "CloseBroadcastReceiver");
            if (intent.getAction().equals(CLOSE_SELECT_NAVIGATION_PAGE)) {
                LogUtils.d(TAG, "关闭选择导航页面");
                finish();
            }
        }
    }

    /**
     * 启动单点导航任务
     */
    public void showDialogNavigation(boolean isStartNavigation, SpannableString dialogContent, String yesBtText, String noBtText, LocationEntity locationEntity) {
        //机器人工作模式状态是否为控制模式
        boolean isControlModel = SpUtils.getBoolean(MyApp.getContext(), IS_CONTROL_MODEL, false);
        if (!isControlModel) { //当前不为控制模式则切换为控制模式
            SpUtils.putBoolean(MyApp.getContext(), IS_CONTROL_MODEL, true);
            //机器人切换为控制模式
            SignalDataEvent moveMode = new SignalDataEvent(RobotConfig.MSG_CHANGE_POWER_LOCK, "");
            moveMode.setPowerlock(1);
            EventBus.getDefault().post(moveMode);
        }
        //语音播报提示
        EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, dialogContent.toString()));
        //单点导航任务弹窗提示确认
        startNavigationDialog = new MyTipDialogSetText(MyApp.getContext());
        startNavigationDialog.setDialogTitle(MvpBaseActivity.getActivityContext().getString(R.string.start_chose_destination_dialog_title));
        startNavigationDialog.setDialogContent(dialogContent);
        startNavigationDialog.setTipDialogYesNoListener(yesBtText, noBtText, new MyTipDialogSetText.TipDialogYesNoListener() {
            @Override
            public void tipProgressChoseYes() {
                if (isStartNavigation) {
                    mPresenter.startLocationTask(locationEntity);
                }
            }

            @Override
            public void tipProgressChoseNo() {
            }
        });
        startNavigationDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        startNavigationDialog.show();
    }
}
