package com.fitgreat.airfacerobot.introductionlist;

import android.app.AlertDialog;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.introductionlist.adapter.IntroductionProcessAdapter;
import com.fitgreat.airfacerobot.introductionlist.presenter.IntroductionListPresenter;
import com.fitgreat.airfacerobot.introductionlist.view.IntroductionListView;
import com.fitgreat.airfacerobot.launcher.ui.activity.RobotInitActivity;
import com.fitgreat.airfacerobot.launcher.utils.OperationUtils;
import com.fitgreat.airfacerobot.launcher.utils.ToastUtils;
import com.fitgreat.airfacerobot.launcher.widget.IntroductionView;
import com.fitgreat.airfacerobot.model.OperationInfo;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.RouteUtils;
import com.fitgreat.archmvp.base.util.SpUtils;
import com.geek.webpage.web.activity.BaseWebpageActivity;

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
    @BindView(R.id.linearLayout_no_data)
    LinearLayout mLinearLayoutNoData;

    @BindView(R.id.introduction_list_one_linearLayout)
    LinearLayout introductionListOneLinearLayout;
    @BindView(R.id.introduction_list_one_item_one_back_image)
    ImageView introduction_list_one_item_one_back_image;
    @BindView(R.id.introduction_list_one_item_one_kind_image)
    ImageView introduction_list_one_item_one_kind_image;
    @BindView(R.id.introduction_list_one_item_one_title)
    TextView introduction_list_one_item_one_title;


    @BindView(R.id.introduction_list_two_linearLayout)
    LinearLayout introduction_list_two_linearLayout;
    @BindView(R.id.introduction_list_two_item_one_back_image)
    ImageView introduction_list_two_item_one_back_image;
    @BindView(R.id.introduction_list_two_item_one_kind_image)
    ImageView introduction_list_two_item_one_kind_image;
    @BindView(R.id.introduction_list_two_item_one_title)
    TextView introduction_list_two_item_one_title;
    @BindView(R.id.introduction_list_two_item_two_back_image)
    ImageView introduction_list_two_item_two_back_image;
    @BindView(R.id.introduction_list_two_item_two_kind_image)
    ImageView introduction_list_two_item_two_kind_image;
    @BindView(R.id.introduction_list_two_item_two_title)
    TextView introduction_list_two_item_two_title;


    @BindView(R.id.introduction_list_three_linearLayout)
    LinearLayout introduction_list_three_linearLayout;

    @BindView(R.id.introduction_list_three_item_one_back_image)
    ImageView introduction_list_three_item_one_back_image;
    @BindView(R.id.introduction_list_three_item_one_kind_image)
    ImageView introduction_list_three_item_one_kind_image;
    @BindView(R.id.introduction_list_three_item_one_title)
    TextView introduction_list_three_item_one_title;

    @BindView(R.id.introduction_list_three_item_two_back_image)
    ImageView introduction_list_three_item_two_back_image;
    @BindView(R.id.introduction_list_three_item_two_kind_image)
    ImageView introduction_list_three_item_two_kind_image;
    @BindView(R.id.introduction_list_three_item_two_title)
    TextView introduction_list_three_item_two_title;

    @BindView(R.id.introduction_list_three_item_three_back_image)
    ImageView introduction_list_three_item_three_back_image;
    @BindView(R.id.introduction_list_three_item_three_kind_image)
    ImageView introduction_list_three_item_three_kind_image;
    @BindView(R.id.introduction_list_three_item_three_title)
    TextView introduction_list_three_item_three_title;


    @BindView(R.id.introduction_list_one_constraintLayout)
    ConstraintLayout introduction_list_one_constraintLayout;


    @BindView(R.id.introduction_list_two_item_one_constraintLayout)
    ConstraintLayout introduction_list_two_item_one_constraintLayout;
    @BindView(R.id.introduction_list_two_item_two_constraintLayout)
    ConstraintLayout introduction_list_two_item_two_constraintLayout;


    @BindView(R.id.introduction_list_three_item_one_constraintLayout)
    ConstraintLayout introduction_list_three_item_one_constraintLayout;
    @BindView(R.id.introduction_list_three_item_two_constraintLayout)
    ConstraintLayout introduction_list_three_item_two_constraintLayout;
    @BindView(R.id.introduction_list_three_item_three_constraintLayout)
    ConstraintLayout introduction_list_three_item_three_constraintLayout;

    private IntroductionProcessAdapter introductionProcessAdapter;
    private AlertDialog tipIntroductionAlertDialog = null;
    private String currentLanguage;


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
        currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "zh");
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
        if (operationInfoList != null && operationInfoList.size() > 0) {
            if (operationInfoList.size() > 3) { //院内操作数量大于3
                mLinearLayoutNoData.setVisibility(View.GONE);
                introductionListRecyclerView.setVisibility(View.VISIBLE);
                introductionProcessAdapter = new IntroductionProcessAdapter(operationInfoList, this);
                introductionProcessAdapter.setOnItemClickListener((adapter, view, position) -> {
                    //发起院内介绍单任务
                    OperationInfo operationInfo = introductionProcessAdapter.getData().get(position);
                    //当前机器人语言
                    if (currentLanguage.equals("en") && "null".equals(operationInfo.getF_EFileUrl())) { //英文状态下,操作任务中,英文播放文件是否添加
                        ToastUtils.showSmallToast(MvpBaseActivity.getActivityContext().getString(R.string.no_en_file_tip));
                        return;
                    } else {
                        taskTipDialog();
                        mPresenter.startOperationTask(operationInfo);
                    }
                    LogUtils.json(DEFAULT_LOG_TAG, JSON.toJSONString(operationInfo));
                });
                introductionListRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                introductionListRecyclerView.addItemDecoration(new SpacesItemDecoration(10));
                introductionListRecyclerView.setAdapter(introductionProcessAdapter);
            } else if (operationInfoList.size() == 1) {
                introductionListOneLinearLayout.setVisibility(View.VISIBLE);
                mLinearLayoutNoData.setVisibility(View.GONE);
                loadIntroductionData(operationInfoList.get(0), introduction_list_one_item_one_back_image, introduction_list_one_item_one_kind_image, introduction_list_one_item_one_title);
                itemClickEvent(operationInfoList, introduction_list_one_constraintLayout, 0);
            } else if (operationInfoList.size() == 2) {
                introduction_list_two_linearLayout.setVisibility(View.VISIBLE);
                mLinearLayoutNoData.setVisibility(View.GONE);
                loadIntroductionData(operationInfoList.get(0), introduction_list_two_item_one_back_image, introduction_list_two_item_one_kind_image, introduction_list_two_item_one_title);
                itemClickEvent(operationInfoList, introduction_list_two_item_one_constraintLayout, 0);
                loadIntroductionData(operationInfoList.get(1), introduction_list_two_item_two_back_image, introduction_list_two_item_two_kind_image, introduction_list_two_item_two_title);
                itemClickEvent(operationInfoList, introduction_list_two_item_two_constraintLayout, 1);
            } else if (operationInfoList.size() == 3) {
                introduction_list_three_linearLayout.setVisibility(View.VISIBLE);
                mLinearLayoutNoData.setVisibility(View.GONE);
                loadIntroductionData(operationInfoList.get(0), introduction_list_three_item_one_back_image, introduction_list_three_item_one_kind_image, introduction_list_three_item_one_title);
                itemClickEvent(operationInfoList, introduction_list_three_item_one_constraintLayout, 0);
                loadIntroductionData(operationInfoList.get(1), introduction_list_three_item_two_back_image, introduction_list_three_item_two_kind_image, introduction_list_three_item_two_title);
                itemClickEvent(operationInfoList, introduction_list_three_item_two_constraintLayout, 1);
                loadIntroductionData(operationInfoList.get(2), introduction_list_three_item_three_back_image, introduction_list_three_item_three_kind_image, introduction_list_three_item_three_title);
                itemClickEvent(operationInfoList, introduction_list_three_item_three_constraintLayout, 2);
            }
        } else {
            mLinearLayoutNoData.setVisibility(View.VISIBLE);
            introductionListRecyclerView.setVisibility(View.GONE);
        }
    }

    private void taskTipDialog() {
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
    }

    private void itemClickEvent(List<OperationInfo> operationInfoList, ConstraintLayout introduction_list_one_constraintLayout, int i) {
        introduction_list_one_constraintLayout.setOnClickListener((view) -> {
            //当前机器人语言
            if (currentLanguage.equals("en") && "null".equals(operationInfoList.get(i).getF_EFileUrl())) { //英文状态下,操作任务中,英文播放文件是否添加
                ToastUtils.showSmallToast(MvpBaseActivity.getActivityContext().getString(R.string.no_en_file_tip));
                return;
            } else {
                taskTipDialog();
                mPresenter.startOperationTask(operationInfoList.get(i));
            }
        });
    }

    /**
     * 加载数据 (数据只有1条 2条  3条时加载显示)
     */
    private void loadIntroductionData(OperationInfo operationInfo, ImageView backImageView, ImageView kindImageView, TextView titleView) {
        if (currentLanguage.equals("zh")) {
            titleView.setText(operationInfo.getF_Name());
        } else {
            titleView.setText(operationInfo.getF_EName());
        }
        if (operationInfo.getF_Type().equals("2")) {
            kindImageView.setImageDrawable(getDrawable(R.drawable.ic_introduction_video));
        } else if (operationInfo.getF_Type().equals("3")) {
            kindImageView.setImageDrawable(getDrawable(R.drawable.ic_introduction_ppt));
        } else if (operationInfo.getF_Type().equals("4")) {
            kindImageView.setImageDrawable(getDrawable(R.drawable.ic_introduction_word));
        }
        if (!("null".equals(operationInfo.getF_DescImg()))) {
            Glide.with(this).load(operationInfo.getF_DescImg()).into(backImageView);
        } else {
            backImageView.setImageDrawable(getDrawable(R.drawable.img_introduction));
        }
    }

    @Override
    public void hideTipDialog() {
        baseHandler.postDelayed(() -> {
            if (tipIntroductionAlertDialog != null) {
                tipIntroductionAlertDialog.dismiss();
            }
        }, 2 * 1000);
    }

    @OnClick({R.id.introduction_list_close_bt})
    public void onclick(View view) {
        switch (view.getId()) {
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
