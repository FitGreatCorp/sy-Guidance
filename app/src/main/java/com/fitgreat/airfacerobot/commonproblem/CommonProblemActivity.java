package com.fitgreat.airfacerobot.commonproblem;

import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.chosedestination.ChoseDestinationActivity;
import com.fitgreat.airfacerobot.commonproblem.adapter.CommonProblemAdapter;
import com.fitgreat.airfacerobot.commonproblem.presenter.CommonProblemPresenter;
import com.fitgreat.airfacerobot.commonproblem.view.CommonProblemView;
import com.fitgreat.airfacerobot.launcher.ui.activity.RobotInitActivity;
import com.fitgreat.airfacerobot.launcher.utils.CashUtils;
import com.fitgreat.airfacerobot.model.ActionDdsEvent;
import com.fitgreat.airfacerobot.model.AskAnswerDataEvent;
import com.fitgreat.airfacerobot.model.CommandDataEvent;
import com.fitgreat.airfacerobot.model.CommonProblemEntity;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.airfacerobot.speech.model.MessageBean;
import com.fitgreat.airfacerobot.speech.model.ShowContent;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.RouteUtils;
import com.fitgreat.archmvp.base.util.SpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static com.fitgreat.airfacerobot.constants.Constants.COMMON_PROBLEM_BY_INSTRUCTION;
import static com.fitgreat.airfacerobot.constants.Constants.COMMON_PROBLEM_TAG;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CHOOSE_COMMON_PROBLEM_POSITION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_LANGUAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.JUMP_COMMON_PROBLEM_PAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PLAY_TASK_PROMPT_INFO;
import static com.fitgreat.airfacerobot.constants.RobotConfig.START_DDS_WAKE_TAG;

/**
 * 常见问题展示选择页面
 */
public class CommonProblemActivity extends MvpBaseActivity<CommonProblemView, CommonProblemPresenter> implements CommonProblemView {
    @BindView(R.id.common_problem_list)
    RecyclerView mCommonProblemList;
    @BindView(R.id.common_problem_robot_image)
    ImageView commonProblemRobotImage;
    @BindView(R.id.common_problem_answer)
    TextView mCommonProblemAnswer;
    private CommonProblemAdapter commonProblemAdapter;
    private static final String TAG = "CommonProblemActivity";
    private boolean isResume;

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
        AnimationDrawable animationDrawable = (AnimationDrawable) commonProblemRobotImage.getBackground();
        animationDrawable.start();
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

    @OnClick({R.id.close_bt, R.id.common_problem_container})
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
        //通过指令从首页跳转到常见问题页面
        if (getIntent().hasExtra("bundle")) {
            Bundle bundle = getIntent().getBundleExtra("bundle");
            CommonProblemEntity commonProblemEntity = (CommonProblemEntity) bundle.getSerializable("CommonProblemEntity");
            //更新当前播报问题选中状态
            int itemPosition = CashUtils.getProblemPosition(commonProblemEntity.getF_QId());
            SpUtils.putInt(MyApp.getContext(), CHOOSE_COMMON_PROBLEM_POSITION, itemPosition);
            commonProblemAdapter.notifyDataSetChanged();
            //更新页面问题答案显示
            playProblem(commonProblemEntity);
            LogUtils.d(DEFAULT_LOG_TAG, "getIntent().hasExtra(\"bundle\") CommonProblemEntity   ");
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(AskAnswerDataEvent askAnswerDataEvent) {
        switch (askAnswerDataEvent.getCommandType()) {
            case COMMON_PROBLEM_TAG:
                LogUtils.d(DEFAULT_LOG_TAG, "----COMMON_PROBLEM_TAG---CommonProblemActivity-----");
                CommonProblemEntity commonProblemEntity = askAnswerDataEvent.getCommonProblemEntity();
                //更新当前播报问题选中状态
                int itemPosition = CashUtils.getProblemPosition(commonProblemEntity.getF_QId());
                SpUtils.putInt(MyApp.getContext(), CHOOSE_COMMON_PROBLEM_POSITION, itemPosition);
                commonProblemAdapter.notifyDataSetChanged();
                //更新页面问题答案显示
                playProblem(commonProblemEntity);
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(ShowContent showContent) {
        String voiceMessageText = showContent.getContent1().trim();
        LogUtils.d(DEFAULT_LOG_TAG, "MessageBean:CommonProblemActivity----->" + voiceMessageText);
        mCommonProblemAnswer.setText(voiceMessageText);
    }
    /**
     * 更新页面问题答案显示
     */
    private void playProblem(CommonProblemEntity commonProblemEntity) {
        String currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, null);
        if (currentLanguage != null && currentLanguage.equals("zh")) {
            broadCastProblem(commonProblemEntity.getF_Answer());
        } else {
            broadCastProblem(commonProblemEntity.getF_EAnswer());
        }
    }

    private void broadCastProblem(String broadCastAnswer) {
        EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, broadCastAnswer));
        //页面答案显示
        mCommonProblemAnswer.setText(broadCastAnswer);
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
