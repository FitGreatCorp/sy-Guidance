package com.fitgreat.airfacerobot.chosedestination;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.chosedestination.presenter.ChoseDestinationPresenter;
import com.fitgreat.airfacerobot.chosedestination.view.ChoseDestinationView;
import com.fitgreat.airfacerobot.model.InitEvent;
import com.fitgreat.airfacerobot.model.LocationEntity;
import com.fitgreat.airfacerobot.launcher.utils.LocalCashUtils;
import com.fitgreat.airfacerobot.launcher.widget.YesOrNoDialogFragment;
import com.fitgreat.airfacerobot.model.MapEntity;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.SpUtils;
import org.greenrobot.eventbus.EventBus;
import java.util.List;
import butterknife.BindView;
import static com.fitgreat.airfacerobot.constants.RobotConfig.AUTOMATIC_RECHARGE_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MAP_INFO_CASH;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_FLOATING_BALL;

/**
 * 选择我要去目的地页面
 */
public class ChoseDestinationActivity extends MvpBaseActivity<ChoseDestinationView, ChoseDestinationPresenter> implements ChoseDestinationView {

    private static final String TAG = "ChoseDestination";
    private List<LocationEntity> locationList;
    private MapEntity mapEntity;
    private String mapInfoString;
    //默认原点坐标
    int[] locationCurrentX = {504, 1009, 1195, 626, 813, 695};
    int[] locationCurrentY = {397, 505, 160, 172, 172, 552};

    @BindView(R.id.chose_destination_container)
    RelativeLayout mChoseDestinationContainer;
    @BindView(R.id.location_position)
    TextView mLocationPosition;
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
    public void initData() {
        //显示悬浮窗
        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(false);
        EventBus.getDefault().post(initUiEvent);
        //导航点信息汇总
        locationList = LocalCashUtils.getLocationList();
        LogUtils.json(TAG, JSON.toJSONString(locationList));
        //地图信息
        mapInfoString = SpUtils.getString(MyApp.getContext(), MAP_INFO_CASH, null);
        LogUtils.json(TAG, mapInfoString);
        mapEntity = JSON.parseObject(mapInfoString, MapEntity.class);
        //加载演示地图
        Glide.with(this).load(mapEntity.getF_MapFileUrl()).into(mChoseDestinationMapBack);
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
