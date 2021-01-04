package com.fitgreat.airfacerobot.launcher.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.fitgreat.airfacerobot.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 提示弹窗  标题  内容 双选择按钮(可倒计时)
 */
public class MyCountDownDialog extends AlertDialog {
    private TextView myCountDownDialogTitleView, myCountDownDialogContentView;
    private RadioGroup myCountDownDialogRadioGroup;
    private RadioButton myCountDownDialogYesView, myCountDownDialogNoView;
    private String mBtSYesText, mBtSNoText, mDialogTitle, mDialogContent;
    private TipDialogYesNoListener mTipDialogYesNoListener = null;
    //弹窗倒计时模式
    private boolean mCountDownTag = false;
    //弹窗倒计时时间
    private int tipCountTime = 20;
    private Timer timer;
    private TimerTask timerTask;
    private Handler mHandler;

    public MyCountDownDialog(Context context) {
        super(context, R.style.MyDialog);
    }

    public MyCountDownDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置弹窗宽高
        setDialogWidthHeight();
        setContentView(R.layout.my_count_down_dialog_layout);
        setCanceledOnTouchOutside(false);
        //初始化界面控件
        initView();
        //设置展示数据
        initData();
        //初始控件事件
        initEvent();
    }

    private void setDialogWidthHeight() {
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        Display defaultDisplay = getWindow().getWindowManager().getDefaultDisplay();
        Point sizePoint = new Point();
        defaultDisplay.getSize(sizePoint);
        attributes.width = (int) ((sizePoint.x) * 0.4);
        attributes.height = (int) ((sizePoint.y) * 0.3);
        getWindow().setAttributes(attributes);
    }

    private void initView() {
        myCountDownDialogTitleView = findViewById(R.id.my_count_down_dialog_title);
        myCountDownDialogContentView = findViewById(R.id.my_count_down_dialog_content);
        myCountDownDialogRadioGroup = findViewById(R.id.my_count_down_dialog_radioGroup);
        myCountDownDialogYesView = findViewById(R.id.my_count_down_dialog_yes);
        myCountDownDialogNoView = findViewById(R.id.my_count_down_dialog_no);
    }

    private void initData() {
        if (mDialogTitle != null) {
            myCountDownDialogTitleView.setText(mDialogTitle);
        }
        if (mDialogContent != null) {
            myCountDownDialogContentView.setText(mDialogContent);
        }

        if (mBtSNoText != null) {
            myCountDownDialogNoView.setText(mBtSNoText);
        }
        if (mCountDownTag) { //倒计时模式
            myCountDownDialogYesView.setText(mBtSYesText + "(" + tipCountTime + ")");
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    tipCountTime--;
                    mHandler.post(() -> {
                        myCountDownDialogYesView.setText(mBtSYesText + "(" + tipCountTime + ")");
                    });
                    if (tipCountTime == 0) { //倒计时为0
                        mHandler.post(() -> {
                            myCountDownDialogYesView.setText(mBtSYesText);
                        });
                        timer.cancel();
                        tipCountTime = 0;
                        dismiss();
                    }
                }
            };
            timer.schedule(timerTask, 0, 1000);
        } else {
            if (mBtSYesText != null) {
                myCountDownDialogYesView.setText(mBtSYesText);
            }
        }
    }

    private void initEvent() {
        myCountDownDialogRadioGroup.setOnCheckedChangeListener((group, checkId) -> {
            switch (checkId) {
                case R.id.my_count_down_dialog_yes:
                    if (mTipDialogYesNoListener != null) {
                        dismiss();
                        mTipDialogYesNoListener.tipProgressChoseYes();
                    }
                    break;
                case R.id.my_count_down_dialog_no:
                    if (mTipDialogYesNoListener != null) {
                        dismiss();
                        mTipDialogYesNoListener.tipProgressChoseNo();
                    }
                    break;
            }
        });
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
     * 弹窗倒计时模式设置
     */
    public void setCountDownModel(boolean countDownTag, Handler handler) {
        mCountDownTag = countDownTag;
        mHandler = handler;
    }

    public interface TipDialogYesNoListener {
        void tipProgressChoseYes();

        void tipProgressChoseNo();
    }

    /**
     * 弹窗按钮事件监听
     */
    public void setTipDialogYesNoListener(String btSYesText, String btSNoText, TipDialogYesNoListener tipDialogYesNoListener) {
        if (btSYesText != null) {
            mBtSYesText = btSYesText;
        }
        if (btSNoText != null) {
            mBtSNoText = btSNoText;
        }
        mTipDialogYesNoListener = tipDialogYesNoListener;
    }
}
