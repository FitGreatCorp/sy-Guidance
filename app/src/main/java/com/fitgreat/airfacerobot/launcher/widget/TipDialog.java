package com.fitgreat.airfacerobot.launcher.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.fitgreat.airfacerobot.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 提示选择弹窗,单按钮,双选按钮
 */
public class TipDialog extends AlertDialog {
    private Button singleOperationChoseView;
    private RadioGroup tipChoseRadioGroupView;
    private RadioButton tipChoseNoView, tipChoseYesView;
    private TextView tipDialogTitleView, tipDialogContentView;
    private String mSingleOperationBtText, mBtSYesText, mBtSNoText, mDialogTitle, mDialogContent;
    //是否单选弹窗模式
    private boolean mIsOneChoseModel = false;
    //倒计时
    private boolean mCountDownTag = false;
    private SingleOperationChoseListener mSingleOperationChoseListener = null;
    private SelectOperationChoseListener mSelectOperationChoseListener = null;
    private Timer timer;
    private TimerTask timerTask;
    private Handler mHandler;
    private int tipCountTime = 20;

    public TipDialog(Context context) {
        super(context, R.style.MyDialog);
    }

    public TipDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tip_dialog_layout);
        setCanceledOnTouchOutside(false);
        //初始化界面控件
        initView();
        //设置字符串
        initData();
    }

    private void initView() {
        singleOperationChoseView = findViewById(R.id.single_operation_chose);
        tipChoseRadioGroupView = findViewById(R.id.tip_chose_radioGroup);
        tipChoseNoView = findViewById(R.id.tip_chose_no);
        tipChoseYesView = findViewById(R.id.tip_chose_yes);
        tipDialogTitleView = findViewById(R.id.tip_dialog_title);
        tipDialogContentView = findViewById(R.id.tip_dialog_content);
        if (mIsOneChoseModel) {  //单选按钮弹窗
            tipChoseRadioGroupView.setVisibility(View.GONE);
            singleOperationChoseView.setVisibility(View.VISIBLE);
            singleOperationChoseView.setOnClickListener((view) -> {
                if (mSingleOperationChoseListener != null) {
                    dismiss();
                    mSingleOperationChoseListener.onPositiveClick();
                }
            });
        } else {  //多选按钮弹窗
            tipChoseRadioGroupView.setVisibility(View.VISIBLE);
            singleOperationChoseView.setVisibility(View.GONE);
            tipChoseRadioGroupView.setOnCheckedChangeListener((group, checkedId) -> {
                switch (checkedId) {
                    case R.id.tip_chose_no:
                        if (mSelectOperationChoseListener != null) {
                            dismiss();
                            stopTimer();
                            mSelectOperationChoseListener.onNoSelectClick();
                        }
                        break;
                    case R.id.tip_chose_yes:
                        if (mSelectOperationChoseListener != null) {
                            dismiss();
                            stopTimer();
                            mSelectOperationChoseListener.onYesSelectClick();
                        }
                        break;
                }
            });
        }
    }

    private void initData() {
        if (mDialogTitle != null) {
            tipDialogTitleView.setText(mDialogTitle);
        }
        if (mDialogContent != null) {
            tipDialogContentView.setText(mDialogContent);
        }
        if (mBtSYesText != null) {
            if (mCountDownTag) {//有倒计时
                tipChoseYesView.setText(mBtSYesText + "(" + tipCountTime + ")");
                timer = new Timer();
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        tipCountTime--;
                        mHandler.post(() -> {
                            tipChoseYesView.setText(mBtSYesText + "(" + tipCountTime + ")");
                        });
                        if (tipCountTime == 0) { //倒计时为0
                            mHandler.post(() -> {
                                tipChoseYesView.setText(mBtSYesText);
                            });
                            timer.cancel();
                            tipCountTime = 0;
                            if (mSelectOperationChoseListener != null) { //倒计时结束
                                mSelectOperationChoseListener.endCountdown();
                            }
                        }
                    }
                };
                timer.schedule(timerTask, 0, 1000);
            } else { //没有倒计时
                tipChoseYesView.setText(mBtSYesText);
            }
        }
        if (mBtSNoText != null) {
            tipChoseNoView.setText(mBtSNoText);
        }
        if (mSingleOperationBtText != null) {
            singleOperationChoseView.setText(mSingleOperationBtText);
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    /**
     * 弹窗标题设置
     */
    public void setDialogTitle(String dialogTitle) {
        mDialogTitle = dialogTitle;
    }

    /**
     * 弹窗内容设置
     */
    public void setDialogContent(String dialogContent) {
        mDialogContent = dialogContent;
    }

    /**
     * 单选弹窗模式设置
     */
    public void setSingleSelectModel(boolean isOneChoseModel, boolean countDownTag, Handler handler) {
        mIsOneChoseModel = isOneChoseModel;
        mCountDownTag = countDownTag;
        mHandler = handler;
    }


    public interface SingleOperationChoseListener {
        void onPositiveClick();
    }

    /**
     * 单选按钮监听事件
     */
    public void setSingleOperationChoseListener(String singleOperationBtText, SingleOperationChoseListener singleOperationChoseListener) {
        if (singleOperationBtText != null) {
            mSingleOperationBtText = singleOperationBtText;
        }
        mSingleOperationChoseListener = singleOperationChoseListener;
    }

    public interface SelectOperationChoseListener {
        void onYesSelectClick();

        void onNoSelectClick();

        void endCountdown();
    }

    /**
     * 双选按钮监听事件
     */
    public void setSelectOperationChoseListener(String btSYesText, String btSNoText, SelectOperationChoseListener selectOperationChoseListener) {
        if (btSYesText != null) {
            mBtSYesText = btSYesText;
        }
        if (btSNoText != null) {
            mBtSNoText = btSNoText;
        }
        mSelectOperationChoseListener = selectOperationChoseListener;
    }

    /**
     * 多选弹窗按钮文本设置
     */
    public void setSelectYesBtTextChange(String btSYesText) {
        if (btSYesText != null) {
            mBtSYesText = btSYesText;
        }
    }
}
