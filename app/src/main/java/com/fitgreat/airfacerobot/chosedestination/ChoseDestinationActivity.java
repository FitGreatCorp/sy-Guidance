package com.fitgreat.airfacerobot.chosedestination;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.chosedestination.presenter.ChoseDestinationPresenter;
import com.fitgreat.airfacerobot.chosedestination.view.ChoseDestinationView;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.model.ActionDdsEvent;
import com.fitgreat.airfacerobot.model.CommandDataEvent;
import com.fitgreat.airfacerobot.model.InitEvent;
import com.fitgreat.airfacerobot.model.LocationEntity;
import com.fitgreat.airfacerobot.launcher.utils.CashUtils;
import com.fitgreat.airfacerobot.launcher.widget.YesOrNoDialogFragment;
import com.fitgreat.airfacerobot.model.MapEntity;
import com.fitgreat.airfacerobot.remotesignal.model.SignalDataEvent;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.SpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static com.fitgreat.airfacerobot.constants.Constants.SINGLE_POINT_NAVIGATION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.AUTOMATIC_RECHARGE_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_LANGUAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.IS_CONTROL_MODEL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MAP_INFO_CASH;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_FLOATING_BALL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PLAY_TASK_PROMPT_INFO;
import static com.fitgreat.airfacerobot.constants.RobotConfig.START_DDS_WAKE_TAG;

/**
 * 选择我要去目的地页面
 */
public class ChoseDestinationActivity extends MvpBaseActivity<ChoseDestinationView, ChoseDestinationPresenter> implements ChoseDestinationView {

    private static final String TAG = "ChoseDestination";
    private List<LocationEntity> locationList;
    private MapEntity mapEntity;
    private String mapInfoString;
    String basePath = Environment.getExternalStorageDirectory().getPath() + "/ExternalResource/";
    //中文地图本地存储路径
    String currentChineseMapPath = basePath + "currentChineseMap.png";
    //英文地图本地存储路径
    String currentEnglishMapPath = basePath + "currentEnglishMap.png";

    @BindView(R.id.chose_destination_container)
    RelativeLayout mChoseDestinationContainer;
    @BindView(R.id.chose_destination_map_back)
    ImageView mChoseDestinationMapBack;

    @Override
    public ChoseDestinationPresenter createPresenter() {
        return new ChoseDestinationPresenter();
    }

    @Override
    public int getLayoutResource() {
        return R.layout.activity_chose_destination;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Glide.get(this).clearMemory();
    }

    @OnClick({R.id.chose_destination_container})
    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.chose_destination_container:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void initData() {
        //显示悬浮窗
        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(false);
        EventBus.getDefault().post(initUiEvent);
        //启动语音唤醒,打开one shot模式
        EventBus.getDefault().post(new ActionDdsEvent(START_DDS_WAKE_TAG, ""));
        //首页识别单点导航指令跳转到当前页面
        if (getIntent().hasExtra("bundle")) {
            Bundle bundle = getIntent().getBundleExtra("bundle");
            LocationEntity locationEntity = (LocationEntity) bundle.getSerializable("LocationEntity");
            showDialogNavigation(true, "你现在要去\t" + locationEntity.getF_Name() + "\t吗?\t点击\"是\"我会带你过去哦", "是", "否", locationEntity);
        }
        //导航点信息汇总
        locationList = CashUtils.getLocationList();
        LogUtils.json(TAG, JSON.toJSONString(locationList) + "---" + locationList.size());
        //地图信息
        mapInfoString = SpUtils.getString(MyApp.getContext(), MAP_INFO_CASH, null);
        LogUtils.json(TAG, mapInfoString);
        mapEntity = JSON.parseObject(mapInfoString, MapEntity.class);
        //根据当前语言加载展示地图
        String currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, null);

        if (currentLanguage != null && currentLanguage.equals("zh")) {
            //加载展示中文地图
            Glide.with(this).load(currentChineseMapPath).into(mChoseDestinationMapBack);
        } else {
            //加载展示英文地图
            Glide.with(this).load(currentEnglishMapPath).into(mChoseDestinationMapBack);
        }
        //添加导航点按钮位置
        for (int i = 0; i < locationList.size(); i++) {
            LocationEntity locationEntity = locationList.get(i);
            ImageButton imageButton = new ImageButton(this);
            imageButton.setBackgroundColor(getResources().getColor(R.color.transparent));
            imageButton.setImageDrawable(getResources().getDrawable(R.mipmap.ic_map_location_nor));
            //单点位置按钮添加点击事件
            imageButton.setOnClickListener(v -> {
                showDialogNavigation(true, "你现在要去\t" + locationEntity.getF_Name() + "\t吗?\t点击\"是\"我会带你过去哦", "是", "否", locationEntity);
                //自动回充工作流结束
                SpUtils.putBoolean(MyApp.getContext(), AUTOMATIC_RECHARGE_TAG, false);
            });
            //设置导航点按钮位置
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.topMargin = Integer.parseInt(locationEntity.getS_Y()) - 85;
            layoutParams.leftMargin = Integer.parseInt(locationEntity.getS_X()) - 30;
            mChoseDestinationContainer.addView(imageButton, layoutParams);
        }
    }

    /**
     * 启动单点导航任务
     */
    public void showDialogNavigation(boolean isStartNavigation, String dialogContent, String yesBtText, String noBtText, LocationEntity locationEntity) {
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
        EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, dialogContent));
        //单点导航任务弹窗提示确认
        YesOrNoDialogFragment yesOrNoDialogFragment = YesOrNoDialogFragment.newInstance("提示", dialogContent, yesBtText, noBtText);
        yesOrNoDialogFragment.show(getSupportFragmentManager(), "navigation");
        yesOrNoDialogFragment.setSelectYesNoListener(new YesOrNoDialogFragment.SelectYesNoListener() {
            @Override
            public void selectYes() {
                if (isStartNavigation) {
                    mPresenter.startLocationTask(locationEntity);
                }
            }

            @Override
            public void selectNo() {
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(CommandDataEvent commandDataEvent) {
        switch (commandDataEvent.getCommandType()) {
            case SINGLE_POINT_NAVIGATION:
                LogUtils.d("CommandTodo", "----SINGLE_POINT_NAVIGATION---ChoseDestinationActivity-----");
                LocationEntity locationEntity = commandDataEvent.getLocationEntity();
                LogUtils.json("CommandTodo", JSON.toJSONString(locationEntity));
                showDialogNavigation(true, "你现在要去\t" + locationEntity.getF_Name() + "\t吗?\t点击\"是\"我会带你过去哦", "是", "否", locationEntity);
                break;
            default:
                break;
        }
    }

    @Override
    public void disconnectNetWork() {

    }

    @Override
    public void disconnectRos() {

    }

    @Override
    public void navigationSuccess() {

    }
}
