package com.fitgreat.airfacerobot.commonproblem;

import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.Spannable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.commonproblem.adapter.CommonProblemAdapter;
import com.fitgreat.airfacerobot.commonproblem.adapter.CommonProblemNodeAdapter;
import com.fitgreat.airfacerobot.commonproblem.node.CommonProblemFirstNode;
import com.fitgreat.airfacerobot.commonproblem.node.CommonProblemSecondNode;
import com.fitgreat.airfacerobot.commonproblem.presenter.CommonProblemPresenter;
import com.fitgreat.airfacerobot.commonproblem.view.CommonProblemView;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.launcher.ui.activity.MainActivity;
import com.fitgreat.airfacerobot.launcher.ui.activity.RobotInitActivity;
import com.fitgreat.airfacerobot.launcher.utils.CashUtils;
import com.fitgreat.airfacerobot.launcher.widget.TopTitleView;
import com.fitgreat.airfacerobot.model.ActionDdsEvent;
import com.fitgreat.airfacerobot.model.AskAnswerDataEvent;
import com.fitgreat.airfacerobot.model.CommonProblemEntity;
import com.fitgreat.airfacerobot.model.InitEvent;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.RouteUtils;
import com.fitgreat.archmvp.base.util.SpUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import static com.fitgreat.airfacerobot.constants.Constants.COMMON_PROBLEM_TAG;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CHOOSE_COMMON_PROBLEM_POSITION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_LANGUAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.DDS_VOICE_TEXT_CANCEL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.DEFAULT_POSITION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.JUMP_COMMON_PROBLEM_PAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PLAY_TASK_PROMPT_INFO;
import static com.fitgreat.airfacerobot.constants.RobotConfig.START_BLINK_ANIMATION_MSG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.START_DDS_WAKE_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.START_SPEAK_ANIMATION_MSG;

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
    private String currentLanguage;
    private List<BaseNode> currentData;
    private CommonProblemAdapter commonProblemAdapter;
    //机器人默认眨眼动画
    private static final int START_BLINK_ANIMATION_TAG = 8888;
    //机器人说话动画
    private static final int START_SPEAK_ANIMATION_TAG = 9999;
    private AnimationDrawable blinkDrawable, speakDrawable;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case START_BLINK_ANIMATION_TAG:  //眨眼动画连续播放
                    //移除机器人说话动画消息
                    if (handler.hasMessages(START_SPEAK_ANIMATION_TAG)) {
                        handler.removeMessages(START_SPEAK_ANIMATION_TAG);
                    }
                    //终止机器人说话动画
                    if (speakDrawable != null && speakDrawable.isRunning()) {
                        speakDrawable.stop();
                    }
                    //启动机器人说话动画
                    commonProblemRobotImage.setBackgroundResource(R.drawable.blink_animation);
                    blinkDrawable = (AnimationDrawable) commonProblemRobotImage.getBackground();
                    if (blinkDrawable.isRunning()) {
                        blinkDrawable.stop();
                    }
                    blinkDrawable.start();
                    //眨眼动画暂停5秒后在播放
                    handler.sendEmptyMessageDelayed(START_BLINK_ANIMATION_TAG, 5 * 1000);
                    break;
                case START_SPEAK_ANIMATION_TAG:  //说话动画启动
                    //移除机器人眨眼动画消息
                    if (handler.hasMessages(START_BLINK_ANIMATION_TAG)) {
                        handler.removeMessages(START_BLINK_ANIMATION_TAG);
                    }
                    //终止机器人眨眼动画
                    if (blinkDrawable != null && blinkDrawable.isRunning()) {
                        blinkDrawable.stop();
                    }
                    commonProblemRobotImage.setBackgroundResource(R.drawable.speak_animation);
                    speakDrawable = (AnimationDrawable) commonProblemRobotImage.getBackground();
                    speakDrawable.start();
                    break;
            }
        }
    };
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(InitEvent initEvent) {
        switch (initEvent.type) {
            case START_SPEAK_ANIMATION_MSG: //启动说话动画
                handler.sendEmptyMessage(START_SPEAK_ANIMATION_TAG);
                break;
            case START_BLINK_ANIMATION_MSG: //启动眨眼动画
                handler.sendEmptyMessage(START_BLINK_ANIMATION_TAG);
                break;
            default:
                break;
        }
    }
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
        //获取当前机器人语言
        currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "zh");
        //注册EventBus
        EventBus.getDefault().register(this);
        //获取常见问题列表
        mPresenter.getQuestionList();
        //启动语音唤醒,打开one shot模式
        EventBus.getDefault().post(new ActionDdsEvent(START_DDS_WAKE_TAG, ""));
        mCommonProblemTitle.setBackKListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //启动机器人眨眼动画
        handler.sendEmptyMessage(START_BLINK_ANIMATION_TAG);
        //进入常见问题模块
        SpUtils.putBoolean(MyApp.getContext(), JUMP_COMMON_PROBLEM_PAGE, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //关闭dds语音播报
        EventBus.getDefault().post(new ActionDdsEvent(DDS_VOICE_TEXT_CANCEL, ""));
        //退出常见问题模块
        SpUtils.putBoolean(MyApp.getContext(), JUMP_COMMON_PROBLEM_PAGE, false);
        //常见问题条目选中编号回复原始值
        SpUtils.putInt(MyApp.getContext(), CHOOSE_COMMON_PROBLEM_POSITION, DEFAULT_POSITION);
        handler.removeCallbacksAndMessages(null);
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
        LogUtils.d(DEFAULT_LOG_TAG,"常见问题页面断网");
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
            commonProblemAdapter = new CommonProblemAdapter(commonProblemEntities, this);
            mCommonProblemList.setLayoutManager(new LinearLayoutManager(this));
            commonProblemAdapter.setOnItemClickListener((adapter, view, position) -> {
                LogUtils.d(DEFAULT_LOG_TAG, " commonProblemAdapter  position    " + position);
                //保存当前条目编号
                SpUtils.putInt(MyApp.getContext(), CHOOSE_COMMON_PROBLEM_POSITION, position);
                //更新适配器数据并刷新
                adapter.notifyDataSetChanged();
                mCommonProblemEntity = (CommonProblemEntity) adapter.getData().get(position);
                //关闭dds语音播报
                EventBus.getDefault().post(new ActionDdsEvent(DDS_VOICE_TEXT_CANCEL, ""));
                //语音播报当前问题答案
                if (currentLanguage.equals("zh")) {
                    EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, mCommonProblemEntity.getF_Answer()));
                } else {
                    EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, mCommonProblemEntity.getF_EAnswer()));
                }
            });
            mCommonProblemList.setAdapter(commonProblemAdapter);
            //通过指令从首页跳转到常见问题页面
            if (getIntent().hasExtra("bundle")) {
                Bundle bundle = getIntent().getBundleExtra("bundle");
                mCommonProblemEntity = (CommonProblemEntity) bundle.getSerializable("CommonProblemEntity");
                reFreshListData(mCommonProblemEntity);
                LogUtils.d(DEFAULT_LOG_TAG, "----getIntent().hasExtra(\"bundle\")---CommonProblemActivity-----");
            }
        } else { //服务端常见问题没有数据
            mLinearLayoutNoData.setVisibility(View.VISIBLE);
            mCommonProblemList.setVisibility(View.GONE);
        }
    }

    public void reFreshListData(CommonProblemEntity commonProblemEntity) {
        //更新当前播报问题选中状态
        int itemPosition = CashUtils.getProblemPosition(commonProblemEntity.getF_QId());
        //保存当前条目编号
        SpUtils.putInt(MyApp.getContext(), CHOOSE_COMMON_PROBLEM_POSITION, itemPosition);
        //更新适配器数据并刷新
        commonProblemAdapter.notifyDataSetChanged();
        if (currentLanguage.equals("zh")) {
            EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, commonProblemEntity.getF_Answer()));
        } else {
            EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, commonProblemEntity.getF_EAnswer()));
        }
    }

//    private List<BaseNode> getEntity(List<CommonProblemEntity> commonProblemEntities) {
//        List<BaseNode> dataList = new ArrayList<>();
//        for (int i = 0; i < commonProblemEntities.size(); i++) {
//            mCommonProblemEntity = commonProblemEntities.get(i);
//            secondNodeList = new ArrayList<>();
//            commonProblemSecondNode = new CommonProblemSecondNode(mCommonProblemEntity.getF_Answer(), mCommonProblemEntity.getF_EAnswer());
//            secondNodeList.add(commonProblemSecondNode);
//
//            commonProblemFirstNode = new CommonProblemFirstNode(secondNodeList, mCommonProblemEntity);
//            dataList.add(commonProblemFirstNode);
//        }
//        return dataList;
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(AskAnswerDataEvent askAnswerDataEvent) {
        switch (askAnswerDataEvent.getCommandType()) {
            case COMMON_PROBLEM_TAG:
                LogUtils.d(DEFAULT_LOG_TAG, "----COMMON_PROBLEM_TAG---CommonProblemActivity-----");
                mCommonProblemEntity = askAnswerDataEvent.getCommonProblemEntity();
                reFreshListData(mCommonProblemEntity);
                break;
            default:
                break;
        }
    }


    @Override
    public void back() {
        finish();
    }
}
