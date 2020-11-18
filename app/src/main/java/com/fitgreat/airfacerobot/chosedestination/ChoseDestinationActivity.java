package com.fitgreat.airfacerobot.chosedestination;

import android.view.View;

import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.automission.adapter.OperationAdapter;
import com.fitgreat.airfacerobot.automission.view.AutoMissionView;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.commonproblem.CommonProblemActivity;
import com.fitgreat.airfacerobot.launcher.model.InitEvent;
import com.fitgreat.airfacerobot.launcher.model.OperationInfo;
import com.fitgreat.airfacerobot.launcher.widget.MyDialog;
import com.fitgreat.airfacerobot.launcher.widget.YesOrNoDialogFragment;
import com.fitgreat.archmvp.base.util.RouteUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.OnClick;

import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_FLOATING_BALL;

/**
 * 选择我要去目的地页面
 */
public class ChoseDestinationActivity extends MvpBaseActivity<ChoseDestinationView, ChoseDestinationPresenter> implements AutoMissionView {
    private MyDialog myDialog;
    private OperationAdapter operationAdapter;


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
    }

    @OnClick({R.id.location_bt_one, R.id.location_bt_two, R.id.location_bt_three, R.id.location_bt_four})
    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.location_bt_one: //跳转设置模块
                showDialogNavigation();
//                jumpSetModule();
                break;
            case R.id.location_bt_two: //我要去
                RouteUtils.goToActivity(getContext(), ChoseDestinationActivity.class);
                break;
            case R.id.location_bt_three: //常见问题
                RouteUtils.goToActivity(getContext(), CommonProblemActivity.class);
                break;
            case R.id.location_bt_four: //院内介绍
//                startIntroductionWorkFlow();
                break;
            default:
                break;
        }
    }

    public void showDialogNavigation() {
        YesOrNoDialogFragment yesOrNoDialogFragment = YesOrNoDialogFragment.newInstance("提示", "你现在要去挂号处吗?点击\"是\"我会带你去的", "是", "否");
        yesOrNoDialogFragment.show(getSupportFragmentManager(), "navigation");
    }

    @Override
    public void disconnectNetWork() {

    }

    @Override
    public void disconnectRos() {

    }

    @Override
    public void showOperationList(List<OperationInfo> operationList) {

    }


    @Override
    public void startTaskSuccess() {
        finish();
    }
}
