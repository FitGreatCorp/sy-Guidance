package com.fitgreat.airfacerobot.commonproblem;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.commonproblem.adapter.CommonProblemAdapter;
import com.fitgreat.airfacerobot.commonproblem.presenter.CommonProblemPresenter;
import com.fitgreat.airfacerobot.commonproblem.view.CommonProblemView;
import com.fitgreat.airfacerobot.model.ActionEvent;
import com.fitgreat.airfacerobot.model.CommandDataEvent;
import com.fitgreat.airfacerobot.model.CommonProblemEntity;
import com.fitgreat.airfacerobot.model.LocationEntity;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.SpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static com.fitgreat.airfacerobot.constants.Constants.COMMON_PROBLEM_TAG;
import static com.fitgreat.airfacerobot.constants.Constants.SINGLE_POINT_NAVIGATION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CHOOSE_COMMON_PROBLEM_POSITION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.DDS_OBSERVER_REGISTERED;
import static com.fitgreat.airfacerobot.constants.RobotConfig.DDS_OBSERVER_UNTIE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PLAY_TASK_PROMPT_INFO;

/**
 * 常见问题展示选择页面
 */
public class CommonProblemActivity extends MvpBaseActivity<CommonProblemView, CommonProblemPresenter> implements CommonProblemView {
    @BindView(R.id.common_problem_list)
    RecyclerView mCommonProblemList;
    @BindView(R.id.common_problem_robot_animation)
    ImageView mCommonProblemRobotAnimation;
    @BindView(R.id.common_problem_answer)
    TextView mCommonProblemAnswer;
    private CommonProblemAdapter commonProblemAdapter;

    @Override
    public CommonProblemPresenter createPresenter() {
        return new CommonProblemPresenter();
    }

    @Override
    public int getLayoutResource() {
        return R.layout.activity_common_problem;
    }

    @Override
    public void initData() {
        //获取常见问题列表
        mPresenter.getQuestionList();
        if (getIntent().hasExtra("bundle")) {
            LogUtils.d("CommandTodo", "----CommonProblemActivity---getIntent().hasExtra(\"bundle\")-----");
            Bundle bundle = getIntent().getBundleExtra("bundle");
            CommonProblemEntity commonProblemEntity = (CommonProblemEntity) bundle.getSerializable("CommonProblemEntity");
            playProblem(commonProblemEntity);
        }

        //dds对话Observer注册
//        EventBus.getDefault().post(new ActionEvent(DDS_OBSERVER_REGISTERED, ""));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mvp模式销毁当前页面时置空presenter持有view引用
        mPresenter.detachView();
        //dds对话Observer解绑
//        EventBus.getDefault().post(new ActionEvent(DDS_OBSERVER_UNTIE, ""));
    }

    @OnClick({R.id.close_bt,R.id.common_problem_container})
    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.common_problem_container:
            case R.id.close_bt: //跳转设置模块
                finish();
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
    public void showQuestionList(List<CommonProblemEntity> commonProblemEntities) {
        //常见问题指令识别,跳转常见问题展示页面

        runOnUiThread(() -> {
            commonProblemAdapter = new CommonProblemAdapter(commonProblemEntities);
            commonProblemAdapter.setOnItemClickListener((adapter, view, position) -> {
                SpUtils.putInt(MyApp.getContext(), CHOOSE_COMMON_PROBLEM_POSITION, position);
                commonProblemAdapter.notifyDataSetChanged();
                //语音播报问题答案
                CommonProblemEntity commonProblemEntity = (CommonProblemEntity) adapter.getData().get(position);
                playProblem(commonProblemEntity);
            });
            mCommonProblemList.setLayoutManager(new GridLayoutManager(this, 3));
            mCommonProblemList.addItemDecoration(new MyItemDecoration(10));
            mCommonProblemList.setAdapter(commonProblemAdapter);
        });
    }

    private void playProblem(CommonProblemEntity commonProblemEntity) {
        EventBus.getDefault().post(new ActionEvent(PLAY_TASK_PROMPT_INFO, commonProblemEntity.getF_Answer()));
        //页面答案显示
        mCommonProblemAnswer.setText(commonProblemEntity.getF_Answer());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(CommandDataEvent commandDataEvent) {
        switch (commandDataEvent.getCommandType()) {
            case COMMON_PROBLEM_TAG:
                LogUtils.d("CommandTodo", "----SINGLE_POINT_NAVIGATION---ChoseDestinationActivity-----");
                LocationEntity locationEntity = commandDataEvent.getLocationEntity();
                LogUtils.json("CommandTodo", JSON.toJSONString(locationEntity));
                break;
            default:
                break;
        }
    }
    /**
     * 常见问题列表条目添加边距
     */
    public class MyItemDecoration extends RecyclerView.ItemDecoration {
        private int mSpace;

        public MyItemDecoration(int space) {
            this.mSpace = space;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.bottom = mSpace;
            outRect.top = mSpace;
            outRect.left = mSpace;
            outRect.right = mSpace;
        }
    }
}
