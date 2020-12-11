package com.fitgreat.airfacerobot.introductionlist;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.automission.adapter.OperationAdapter;
import com.fitgreat.airfacerobot.automission.view.AutoMissionView;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.introductionlist.presenter.IntroductionListPresenter;
import com.fitgreat.airfacerobot.introductionlist.view.IntroductionListView;
import com.fitgreat.airfacerobot.launcher.ui.activity.RobotInitActivity;
import com.fitgreat.airfacerobot.launcher.widget.MyDialog;
import com.fitgreat.airfacerobot.model.CommonProblemEntity;
import com.fitgreat.airfacerobot.model.OperationInfo;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.archmvp.base.util.RouteUtils;

import java.util.List;

import butterknife.BindView;


/**
 * 院内介绍内容列表页面
 */
public class IntroductionListActivity extends MvpBaseActivity<IntroductionListView, IntroductionListPresenter> implements IntroductionListView {
    @BindView(R.id.introduction_list_recyclerView)
    RecyclerView introductionListRecyclerView;
    private MyDialog myDialog;
    private OperationAdapter operationAdapter;


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
//        mPresenter.getLocationList();
        //        OperationInfo operationInfo = operationAdapter.getData().get(position);
//        LogUtils.d("operationAdapter", "宣教任务信息:==>" + JSON.toJSONString(operationInfo));
//        String message = "";
//        message = RobotInfoUtils.getRobotInfo().getF_Name() + "将执行以下操作：" + "进行" + operationInfo.getF_Name() + ",确认执行该活动吗？";
//        if (myDialog != null) {
//            myDialog = null;
//        }
//        myDialog = new MyDialog(IntroductionListActivity.this, R.style.MyDialog);
//        myDialog.setTitle("提示");
//        myDialog.setMessage(message);
//        myDialog.setPositiveOnclicListener(getString(R.string.positive), () -> {
//            myDialog.dismiss();
//            mPresenter.startOperationTask(operationInfo);
//            //任务种类  2播放视频  3播放pdf  4 播放txt文本  取消指令时需要用
//            SpUtils.putString(MyApp.getContext(), "operationType", operationInfo.getF_Type());
//        });
//        myDialog.setNegativeOnclicListener(getString(R.string.negative), () -> myDialog.dismiss());
//        myDialog.show();
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

    /**
     * 弹窗提示确认操作任务信息
     *
     * @param position
     * @param operationAdapter
     */
    private void confirmOperationInfo(int position, OperationAdapter operationAdapter) {

    }

    @Override
    public void showQuestionList(List<CommonProblemEntity> commonProblemEntities) {

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
