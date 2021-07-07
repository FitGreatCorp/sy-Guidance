package com.fitgreat.airfacerobot.chosedestination;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.adapter.PositionAdapter;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.chosedestination.presenter.ChoseDestinationPresenter;
import com.fitgreat.airfacerobot.chosedestination.view.ChoseDestinationView;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.launcher.ui.activity.RobotInitActivity;
import com.fitgreat.airfacerobot.launcher.utils.MyBitmapUtils;
import com.fitgreat.airfacerobot.launcher.utils.ToastUtils;
import com.fitgreat.airfacerobot.launcher.widget.ClickImageButton;
import com.fitgreat.airfacerobot.launcher.widget.MyTipDialogSetText;
import com.fitgreat.airfacerobot.launcher.widget.TopTitleView;
import com.fitgreat.airfacerobot.model.ActionDdsEvent;
import com.fitgreat.airfacerobot.model.AskAnswerDataEvent;
import com.fitgreat.airfacerobot.model.InitEvent;
import com.fitgreat.airfacerobot.model.LocationEntity;
import com.fitgreat.airfacerobot.launcher.utils.CashUtils;
import com.fitgreat.airfacerobot.model.MapEntity;
import com.fitgreat.airfacerobot.model.PositionEvent;
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

import static com.fitgreat.airfacerobot.adapter.PositionAdapter.EVE_SELECTED_POSITION;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.Constants.SINGLE_POINT_NAVIGATION;
import static com.fitgreat.airfacerobot.constants.Constants.currentChineseMapPath;
import static com.fitgreat.airfacerobot.constants.Constants.currentEnglishMapPath;
import static com.fitgreat.airfacerobot.constants.RobotConfig.AUTOMATIC_RECHARGE_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CLOSE_SELECT_NAVIGATION_PAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_LANGUAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.DDS_VOICE_TEXT_CANCEL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.IS_CONTROL_MODEL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MAIN_PAGE_WHETHER_SHOW;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MAP_INFO_CASH;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_FLOATING_BALL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.NAVIGATION_PAGE_WHETHER_SHOW;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PLAY_TASK_PROMPT_INFO;
import static com.fitgreat.airfacerobot.constants.RobotConfig.START_DDS_WAKE_TAG;

/**
 * 选择我要去目的地页面
 */
public class ChoseDestinationActivity extends MvpBaseActivity<ChoseDestinationView, ChoseDestinationPresenter> implements ChoseDestinationView, View.OnClickListener {

    private static final String TAG = "ChoseDestination";
    private List<LocationEntity> locationList;
    private PositionAdapter positionAdapter;
    private GridLayoutManager gridLayoutManager;

    @BindView(R.id.position_list)
    RecyclerView rv_position;
    @BindView(R.id.chose_destination_top_title)
    ImageView mChoseDestinationTopTitle;

    private String currentLanguage;
    private CloseBroadcastReceiver closeBroadcastReceiver;
    private MyTipDialogSetText startNavigationDialog;
    private Bitmap showMapBitmap;
    private String localMapInfoString;
    private MapEntity mapEntity;
    private String f_showMapFileUrl;

    @Override
    public ChoseDestinationPresenter createPresenter() {
        return new ChoseDestinationPresenter();
    }

    @Override
    public int getLayoutResource() {
        return R.layout.position_list_layout;
    }

    @Override
    protected void onResume() {
        LogUtils.d(DEFAULT_LOG_TAG, "onResume---------------ChoseDestinationActivity------------------");
        super.onResume();
        loadShowMap();
    }
    /**
     * 加载展示地图
     */
    private void loadShowMap() {
        //选择导航页面是否显示标志
        SpUtils.putBoolean(MyApp.getContext(), NAVIGATION_PAGE_WHETHER_SHOW, true);
        //获取屏幕宽高
        WindowManager systemService = (WindowManager) getSystemService(WINDOW_SERVICE);
        Point point = new Point();
        systemService.getDefaultDisplay().getSize(point);
        //加载中英文地图
        if (currentLanguage.equals("zh")&&mapEntity!=null) {  //加载展示中文地图
            showMapBitmap = MyBitmapUtils.decodeSampledBitmapFromFile(currentChineseMapPath, point.x, point.y);
            f_showMapFileUrl = mapEntity.getF_MapFileUrl();
        } else  if (currentLanguage.equals("en")&&mapEntity!=null){  //加载展示英文地图
            showMapBitmap = MyBitmapUtils.decodeSampledBitmapFromFile(currentEnglishMapPath, point.x, point.y);
            f_showMapFileUrl = mapEntity.getF_EMapUrl();
        }
//        Glide.with(this).load(showMapBitmap).into(mChoseDestinationMapBack);
//        Glide.with(this).load(f_showMapFileUrl).into(mChoseDestinationMapBack);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //选择导航页面是否显示标志
        SpUtils.putBoolean(MyApp.getContext(), NAVIGATION_PAGE_WHETHER_SHOW, false);
        //关闭dds语音播报
        EventBus.getDefault().post(new ActionDdsEvent(DDS_VOICE_TEXT_CANCEL, ""));
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (showMapBitmap!=null){
            showMapBitmap.recycle();
            showMapBitmap = null;
        }
        //显示应用返回首页悬浮按钮\
        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(false);
        EventBus.getDefault().post(initUiEvent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解绑EventBus
        EventBus.getDefault().unregister(this);
        //解绑关闭页面广播
        unregisterReceiver(closeBroadcastReceiver);

    }

    @Override
    public void initData() {
        //注册EventBus
        EventBus.getDefault().register(this);
        //根据当前语言加载展示地图
        currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "zh");
        //地图信息
        localMapInfoString = SpUtils.getString(MyApp.getContext(), MAP_INFO_CASH, null);
        if (localMapInfoString!=null){
            mapEntity = JSON.parseObject(localMapInfoString, MapEntity.class);
        }
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
        //绘制页面,添加页面位置信息
        if (locationList != null && locationList.size() != 0) {
//            flashView();

//            for(int i = 0;i<locationList.size();i++){
//                Log.d(TAG,"name = "+locationList.get(i).getF_Name() + " : ("+ locationList.get(i).getF_X() + ","+locationList.get(i).getF_Y()+")");
//            }
            positionAdapter = new PositionAdapter(this,locationList);
            gridLayoutManager = new GridLayoutManager(this,4);
            rv_position.setLayoutManager(gridLayoutManager);
            rv_position.setAdapter(positionAdapter);

        }
        //注册监听关闭选择导航页面广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CLOSE_SELECT_NAVIGATION_PAGE);
        closeBroadcastReceiver = new CloseBroadcastReceiver();
        registerReceiver(closeBroadcastReceiver, intentFilter);
        //添加左上角返回按钮点击事件
        mChoseDestinationTopTitle.setOnClickListener(this);
    }

    /**
     * 添加导航点位置信息
     */
    public void flashView() {
        for (int i = 0; i < locationList.size(); i++) {
            LocationEntity locationEntity = locationList.get(i);
            ClickImageButton imageButton = new ClickImageButton(this);
            imageButton.setBackgroundColor(getResources().getColor(R.color.transparent));
            imageButton.setImageDrawable(getResources().getDrawable(R.mipmap.ic_map_location_nor));
            //单点位置按钮添加点击事件
            imageButton.setOnClickListener(v -> {
                //关闭dds语音播报
                EventBus.getDefault().post(new ActionDdsEvent(DDS_VOICE_TEXT_CANCEL, ""));
                //弹窗提示导航
                showDialogNavigation(true, getNavigationTip(locationEntity), getString(R.string.answer_yes), getString(R.string.answer_no), locationEntity);
                //自动回充工作流结束
                SpUtils.putBoolean(MyApp.getContext(), AUTOMATIC_RECHARGE_TAG, false);
            });
            //设置导航点按钮位置
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.topMargin = Integer.parseInt(locationEntity.getS_Y()) - 85;
            layoutParams.leftMargin = Integer.parseInt(locationEntity.getS_X()) - 30;
//            mChoseDestinationContainer.addView(imageButton, layoutParams);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(PositionEvent posEvent) {
        switch (posEvent.getType()) {
            case EVE_SELECTED_POSITION:
                int selected = posEvent.getSelected();
                LocationEntity locationEntity = locationList.get(selected);
                //关闭dds语音播报
                EventBus.getDefault().post(new ActionDdsEvent(DDS_VOICE_TEXT_CANCEL, ""));
                //弹窗提示导航
                showDialogNavigation(true, getNavigationTip(locationEntity), getString(R.string.answer_yes), getString(R.string.answer_no), locationEntity);
                //自动回充工作流结束
                SpUtils.putBoolean(MyApp.getContext(), AUTOMATIC_RECHARGE_TAG, false);
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
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.chose_destination_top_title:
                finish();
                break;
        }
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
        LogUtils.json(DEFAULT_LOG_TAG, JSON.toJSONString(locationEntity));
        if (locationEntity.getF_EName().equals("null") && currentLanguage.equals("en")) {
            ToastUtils.showSmallToast(MvpBaseActivity.getActivityContext().getString(R.string.english_navigation_tip));
            return;
        }
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
        startNavigationDialog.setTipDialogYesNoListener(noBtText, yesBtText, new MyTipDialogSetText.TipDialogYesNoListener() {
            @Override
            public void tipProgressChoseYes() {

            }

            @Override
            public void tipProgressChoseNo() {
                if (isStartNavigation) {
                    mPresenter.startLocationTask(locationEntity);
                }
            }
        });
        startNavigationDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        startNavigationDialog.show();
    }
}
