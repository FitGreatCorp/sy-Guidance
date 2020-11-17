package com.fitgreat.airfacerobot.chosedestination;

import android.widget.RelativeLayout;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.automission.adapter.OperationAdapter;
import com.fitgreat.airfacerobot.automission.view.AutoMissionView;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.launcher.model.InitEvent;
import com.fitgreat.airfacerobot.launcher.model.OperationInfo;
import com.fitgreat.airfacerobot.launcher.widget.MyDialog;
import org.greenrobot.eventbus.EventBus;
import java.util.List;
import butterknife.BindView;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_FLOATING_BALL;

/**
 * 选择我要去目的地页面
 */
public class ChoseDestinationActivity extends MvpBaseActivity<ChoseDestinationView, ChoseDestinationPresenter> implements AutoMissionView {
//    @BindView(R.id.chose_destination_container)
//    RelativeLayout mChoseDestinationContainer;

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
