package com.fitgreat.airfacerobot.introductionlist;

import android.app.AlertDialog;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.introductionlist.adapter.IntroductionProcessAdapter;
import com.fitgreat.airfacerobot.introductionlist.presenter.IntroductionListPresenter;
import com.fitgreat.airfacerobot.introductionlist.view.IntroductionListView;
import com.fitgreat.airfacerobot.launcher.ui.activity.RobotInitActivity;
import com.fitgreat.airfacerobot.launcher.utils.OperationUtils;
import com.fitgreat.airfacerobot.model.OperationInfo;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.RouteUtils;
import com.fitgreat.archmvp.base.util.SpUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;

import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_LANGUAGE;

/**
 * 院内介绍内容列表页面
 */
public class IntroductionListActivity extends MvpBaseActivity<IntroductionListView, IntroductionListPresenter> implements IntroductionListView {
    @BindView(R.id.introduction_list_recyclerView)
    RecyclerView introductionListRecyclerView;

    private IntroductionProcessAdapter introductionProcessAdapter;
    private AlertDialog tipIntroductionAlertDialog = null;


    @Override
    public IntroductionListPresenter createPresenter() {
        return new IntroductionListPresenter();
    }

    @Override
    public int getLayoutResource() {
        return R.layout.activity_introduction_list;
    }

    @Override
    public void initData() {
        mPresenter.getIntroductionOperationList();
    }

    @Override
    public void disconnectNetWork() {
        RouteUtils.goToActivity(IntroductionListActivity.this, RobotInitActivity.class);
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
        RouteUtils.goToActivity(IntroductionListActivity.this, RobotInitActivity.class);
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
    public void showIntroductionOperationList(List<OperationInfo> operationInfoList) {
        LogUtils.d(DEFAULT_LOG_TAG, "showIntroductionOperationList::  " + operationInfoList.size());
        introductionProcessAdapter = new IntroductionProcessAdapter(operationInfoList, this);
        introductionProcessAdapter.setOnItemClickListener((adapter, view, position) -> {
            //启动院内介绍操作任务提示弹窗
            tipIntroductionAlertDialog = new AlertDialog.Builder(this).create();
            tipIntroductionAlertDialog.setCanceledOnTouchOutside(false);
            tipIntroductionAlertDialog.show();
            //设置布局
            Window dialogWindow = tipIntroductionAlertDialog.getWindow();
            dialogWindow.setContentView(R.layout.start_introduction_tip);
            //获取屏幕宽高
            Display defaultDisplay = getWindow().getWindowManager().getDefaultDisplay();
            Point screenSizePoint = new Point();
            defaultDisplay.getSize(screenSizePoint);
            //设置弹窗宽高 位置
            WindowManager.LayoutParams attributes = dialogWindow.getAttributes();
            attributes.gravity = Gravity.CENTER;
            attributes.width = (int) ((screenSizePoint.x) * (0.5));
            attributes.height = (int) ((screenSizePoint.y) * (0.5));
            dialogWindow.setAttributes(attributes);
            //发起院内介绍单任务
            OperationInfo operationInfo = introductionProcessAdapter.getData().get(position);
            mPresenter.startOperationTask(operationInfo);
            LogUtils.json(DEFAULT_LOG_TAG, JSON.toJSONString(operationInfo));
        });
        introductionListRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        introductionListRecyclerView.addItemDecoration(new SpacesItemDecoration(10));
        introductionListRecyclerView.setAdapter(introductionProcessAdapter);
    }

    @Override
    public void hideTipDialog() {
        baseHandler.postDelayed(() -> {
            if (tipIntroductionAlertDialog != null) {
                tipIntroductionAlertDialog.dismiss();
            }
        }, 2 * 1000);
    }

    @OnClick({R.id.introduction_list_container, R.id.introduction_list_close_bt})
    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.introduction_list_container: //关闭当前页面
            case R.id.introduction_list_close_bt:
                finish();
                break;
            default:
                break;
        }
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
}
