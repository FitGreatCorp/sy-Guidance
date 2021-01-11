package com.fitgreat.airfacerobot.commonproblem;

import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.commonproblem.adapter.CommonProblemNodeAdapter;
import com.fitgreat.airfacerobot.commonproblem.node.CommonProblemFirstNode;
import com.fitgreat.airfacerobot.commonproblem.node.CommonProblemSecondNode;
import com.fitgreat.airfacerobot.commonproblem.presenter.CommonProblemPresenter;
import com.fitgreat.airfacerobot.commonproblem.view.CommonProblemView;
import com.fitgreat.airfacerobot.launcher.ui.activity.RobotInitActivity;
import com.fitgreat.airfacerobot.launcher.utils.CashUtils;
import com.fitgreat.airfacerobot.launcher.widget.TopTitleView;
import com.fitgreat.airfacerobot.model.ActionDdsEvent;
import com.fitgreat.airfacerobot.model.AskAnswerDataEvent;
import com.fitgreat.airfacerobot.model.CommonProblemEntity;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.airfacerobot.speech.model.ShowContent;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.RouteUtils;
import com.fitgreat.archmvp.base.util.SpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static com.fitgreat.airfacerobot.constants.Constants.COMMON_PROBLEM_TAG;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_LANGUAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.JUMP_COMMON_PROBLEM_PAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PLAY_TASK_PROMPT_INFO;
import static com.fitgreat.airfacerobot.constants.RobotConfig.START_DDS_WAKE_TAG;

/**
 * 常见问题展示选择页面
 */
public class CommonProblemActivity extends MvpBaseActivity<CommonProblemView, CommonProblemPresenter> implements CommonProblemView, TopTitleView.BaseBackListener {
    @BindView(R.id.common_problem_list)
    RecyclerView mCommonProblemList;
    @BindView(R.id.common_problem_robot_image)
    ImageView commonProblemRobotImage;

    @BindView(R.id.common_problem_title)
    TopTitleView mCommonProblemTitle;
    @BindView(R.id.linearLayout_no_data)
    LinearLayout mLinearLayoutNoData;
    private AnimationDrawable animationDrawable;
    private CommonProblemEntity mCommonProblemEntity;
    private CommonProblemNodeAdapter commonProblemNodeAdapter;
    private CommonProblemSecondNode commonProblemSecondNode;
    private List<BaseNode> secondNodeList;
    private CommonProblemFirstNode commonProblemFirstNode;

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
        //注册EventBus
        EventBus.getDefault().register(this);
        //获取常见问题列表
        mPresenter.getQuestionList();
        //启动语音唤醒,打开one shot模式
        EventBus.getDefault().post(new ActionDdsEvent(START_DDS_WAKE_TAG, ""));
        //启动机器人动画
        animationDrawable = (AnimationDrawable) commonProblemRobotImage.getBackground();
        animationDrawable.start();
        mCommonProblemTitle.setBackKListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //进入常见问题模块
        SpUtils.putBoolean(MyApp.getContext(), JUMP_COMMON_PROBLEM_PAGE, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //退出常见问题模块
        SpUtils.putBoolean(MyApp.getContext(), JUMP_COMMON_PROBLEM_PAGE, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解绑EventBus
        EventBus.getDefault().unregister(this);
        //mvp模式销毁当前页面时置空presenter持有view引用
        mPresenter.detachView();
    }

    @OnClick({R.id.common_problem_container})
    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.common_problem_container:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void disconnectNetWork() {
        RouteUtils.goToActivity(CommonProblemActivity.this, RobotInitActivity.class);
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
        RouteUtils.goToActivity(CommonProblemActivity.this, RobotInitActivity.class);
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
    public void showQuestionList(List<CommonProblemEntity> commonProblemEntities) {
        if (commonProblemEntities != null) { //服务端常见问题有数据
            mLinearLayoutNoData.setVisibility(View.GONE);
            mCommonProblemList.setVisibility(View.VISIBLE);
            //设置常见问题列表数据
            commonProblemNodeAdapter = new CommonProblemNodeAdapter(this);
            mCommonProblemList.setLayoutManager(new LinearLayoutManager(this));
            mCommonProblemList.setAdapter(commonProblemNodeAdapter);
            commonProblemNodeAdapter.setList(getEntity(commonProblemEntities));
            //通过指令从首页跳转到常见问题页面
            if (getIntent().hasExtra("bundle")) {
                Bundle bundle = getIntent().getBundleExtra("bundle");
                mCommonProblemEntity = (CommonProblemEntity) bundle.getSerializable("CommonProblemEntity");
                //更新当前播报问题选中状态
                int itemPosition = CashUtils.getProblemPosition(mCommonProblemEntity.getF_QId());
                List<BaseNode> data = commonProblemNodeAdapter.getData();
                commonProblemFirstNode = (CommonProblemFirstNode)commonProblemNodeAdapter.getData().get(itemPosition);
                commonProblemFirstNode.setExpanded(true);
                commonProblemNodeAdapter.expandOrCollapse(itemPosition);
                data.set(itemPosition,commonProblemFirstNode);
                commonProblemNodeAdapter.notifyItemChanged(itemPosition);
                //更新页面问题答案显示
                playProblem(mCommonProblemEntity);
                LogUtils.d(DEFAULT_LOG_TAG, "----getIntent().hasExtra(\"bundle\")---CommonProblemActivity-----");

            }
        } else { //服务端常见问题没有数据
            mLinearLayoutNoData.setVisibility(View.VISIBLE);
            mCommonProblemList.setVisibility(View.GONE);
        }
    }

    private List<BaseNode> getEntity(List<CommonProblemEntity> commonProblemEntities) {
        List<BaseNode> dataList = new ArrayList<>();
        for (int i = 0; i < commonProblemEntities.size(); i++) {
            mCommonProblemEntity = commonProblemEntities.get(i);
            secondNodeList = new ArrayList<>();
            commonProblemSecondNode = new CommonProblemSecondNode(mCommonProblemEntity.getF_Answer(), mCommonProblemEntity.getF_EAnswer());
            secondNodeList.add(commonProblemSecondNode);

            commonProblemFirstNode = new CommonProblemFirstNode(secondNodeList, mCommonProblemEntity);
            dataList.add(commonProblemFirstNode);
        }
        return dataList;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(AskAnswerDataEvent askAnswerDataEvent) {
        switch (askAnswerDataEvent.getCommandType()) {
            case COMMON_PROBLEM_TAG:
                LogUtils.d(DEFAULT_LOG_TAG, "----COMMON_PROBLEM_TAG---CommonProblemActivity-----");
                mCommonProblemEntity = askAnswerDataEvent.getCommonProblemEntity();
                //更新当前播报问题选中状态
                int itemPosition = CashUtils.getProblemPosition(mCommonProblemEntity.getF_QId());
//                commonProblemAdapter.notifyDataSetChanged();
                //更新页面问题答案显示
                playProblem(mCommonProblemEntity);
                break;
            default:
                break;
        }
    }

    /**
     * 更新页面问题答案显示
     */
    private void playProblem(CommonProblemEntity commonProblemEntity) {
        String currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "zh");
        if (currentLanguage != null && currentLanguage.equals("zh")) {
            EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, commonProblemEntity.getF_Answer()));
        } else {
            EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, commonProblemEntity.getF_EAnswer()));
        }
    }

    @Override
    public void back() {
        finish();
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
