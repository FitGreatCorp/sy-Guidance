package com.fitgreat.airfacerobot.automission;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.automission.adapter.OperationAdapter;
import com.fitgreat.airfacerobot.automission.presenter.AutoMissionPresenter;
import com.fitgreat.airfacerobot.automission.view.AutoMissionView;
import com.fitgreat.airfacerobot.launcher.model.OperationInfo;
import com.fitgreat.airfacerobot.launcher.ui.activity.RobotInitActivity;
import com.fitgreat.airfacerobot.launcher.widget.MyDialog;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.archmvp.base.ui.MvpBaseActivity;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.RouteUtils;
import com.fitgreat.archmvp.base.util.SpUtils;

import java.util.List;

import butterknife.BindView;

import static com.fitgreat.airfacerobot.constants.RobotConfig.CHECK_OPERATION_POSITION;


public class AutoMissionActivity extends MvpBaseActivity<AutoMissionView, AutoMissionPresenter> implements AutoMissionView {
    @BindView(R.id.recyclerView_mission_list)
    RecyclerView recyclerViewMissionList;
    private MyDialog myDialog;
    private OperationAdapter operationAdapter;


    @Override
    public AutoMissionPresenter createPresenter() {
        return new AutoMissionPresenter();
    }

    @Override
    public int getLayoutResource() {
        return R.layout.activity_auto_mission;
    }

    @Override
    public void initData() {
        mPresenter.getLocationList();
    }

    @Override
    public void disconnectNetWork() {
        RouteUtils.goToActivity(AutoMissionActivity.this, RobotInitActivity.class);
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
        RouteUtils.goToActivity(AutoMissionActivity.this, RobotInitActivity.class);
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
    public void showOperationList(List<OperationInfo> operationList) {
        operationAdapter = new OperationAdapter(operationList);
        operationAdapter.setOnItemClickListener((adapter, view, position) -> {
            SpUtils.putInt(AutoMissionActivity.this, CHECK_OPERATION_POSITION, position);
            operationAdapter.notifyDataSetChanged();
            confirmOperationInfo(position, operationAdapter);
        });
        recyclerViewMissionList.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewMissionList.addItemDecoration(new SpacesItemDecoration(10));
        recyclerViewMissionList.setAdapter(operationAdapter);
    }


    /**
     * 弹窗提示确认操作任务信息
     *
     * @param position
     * @param operationAdapter
     */
    private void confirmOperationInfo(int position, OperationAdapter operationAdapter) {
        OperationInfo operationInfo = operationAdapter.getData().get(position);
        LogUtils.d("operationAdapter", "宣教任务信息:==>" + JSON.toJSONString(operationInfo));
        String message = "";
        message = RobotInfoUtils.getRobotInfo().getF_Name() + "将执行以下操作：" + "进行" + operationInfo.getF_Name() + ",确认执行该活动吗？";
        if (myDialog != null) {
            myDialog = null;
        }
        myDialog = new MyDialog(AutoMissionActivity.this, R.style.MyDialog);
        myDialog.setTitle("提示");
        myDialog.setMessage(message);
        myDialog.setPositiveOnclicListener(getString(R.string.positive), () -> {
            myDialog.dismiss();
            mPresenter.startOperationTask(operationInfo);
            //任务种类  2播放视频  3播放pdf  4 播放txt文本  取消指令时需要用
            SpUtils.putString(MyApp.getContext(), "operationType", operationInfo.getF_Type());
        });
        myDialog.setNegativeOnclicListener(getString(R.string.negative), () -> myDialog.dismiss());
        myDialog.show();
    }

    /**
     * 列表间距添加
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

    @Override
    public void startTaskSuccess() {
        finish();
    }
}
