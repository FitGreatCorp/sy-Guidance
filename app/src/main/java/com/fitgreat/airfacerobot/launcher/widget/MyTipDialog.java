package com.fitgreat.airfacerobot.launcher.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
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

/**
 * 提示弹窗有圆圈进度条
 */
public class MyTipDialog extends AlertDialog {
    private TextView myTipDialogTitleView, myTipDialogContentView;
    private RadioGroup myTipDialogRadioGroup;
    private RadioButton myTipDialogYesView, myTipDialogNoView;
    private Button myTipDialogButton;
    private ProgressBar myTipDialogProgressView;
    private ConstraintLayout constraintLayoutButtonsView;
    private String mBtSYesText, mBtSNoText, mDialogTitle, mSelfBtText, mDialogContent;
    private TipDialogYesNoListener mTipDialogYesNoListener = null;
    private TipDialogSelectListener mTipDialogSelectListener = null;
    //加载提示标题,内容,单选按钮,没有进度条模式
    private boolean mTipSingleSelectModel = false;
    //加载提示模式,有进度条没有按钮
    private boolean mTipLoadModel = false;
    private boolean mCountDownModel = false;

    public MyTipDialog(Context context) {
        super(context, R.style.MyDialog);
    }

    public MyTipDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置弹窗宽高
        setDialogWidthHeight();
        setContentView(R.layout.my_tip_dialog_layout);
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
        myTipDialogTitleView = findViewById(R.id.my_tip_dialog_title);
        myTipDialogContentView = findViewById(R.id.my_tip_dialog_content);
        myTipDialogRadioGroup = findViewById(R.id.my_tip_dialog_radioGroup);
        myTipDialogYesView = findViewById(R.id.my_tip_dialog_yes);
        myTipDialogNoView = findViewById(R.id.my_tip_dialog_no);
        myTipDialogButton = findViewById(R.id.my_tip_dialog_button);
        myTipDialogProgressView = findViewById(R.id.my_tip_dialog_progress);
        constraintLayoutButtonsView = findViewById(R.id.constraintLayout_buttons);
        if (mTipSingleSelectModel) {
            //加载提示模式 标题,内容,单选按钮
            myTipDialogButton.setVisibility(View.VISIBLE);
            myTipDialogContentView.setVisibility(View.VISIBLE);
            myTipDialogRadioGroup.setVisibility(View.GONE);
            myTipDialogProgressView.setVisibility(View.GONE);
        } else {
            if (mTipLoadModel) {
                //加载提示模式 标题,进度条
                constraintLayoutButtonsView.setVisibility(View.GONE);
                myTipDialogContentView.setVisibility(View.GONE);
                myTipDialogProgressView.setVisibility(View.VISIBLE);
            } else { //提示弹窗标题,内容,多选按钮模式
                myTipDialogProgressView.setVisibility(View.GONE);
                myTipDialogButton.setVisibility(View.GONE);
                myTipDialogContentView.setVisibility(View.VISIBLE);
                myTipDialogRadioGroup.setVisibility(View.VISIBLE);
            }
        }

    }

    private void initData() {
        if (mDialogTitle != null) {
            myTipDialogTitleView.setText(mDialogTitle);
        }
        if (mDialogContent != null) {
            myTipDialogContentView.setText(mDialogContent);
        }
        if (mBtSYesText != null) {
            myTipDialogYesView.setText(mBtSYesText);
        }
        if (mBtSNoText != null) {
            myTipDialogNoView.setText(mBtSNoText);
        }
        if (mSelfBtText != null) {
            myTipDialogButton.setText(mSelfBtText);
        }
    }

    private void initEvent() {
        if (mTipSingleSelectModel) {
            myTipDialogButton.setOnClickListener((view) -> {
                if (mTipDialogSelectListener != null) {
                    dismiss();
                    mTipDialogSelectListener.tipSelect();
                }
            });
        } else {
            myTipDialogRadioGroup.setOnCheckedChangeListener((group, checkId) -> {
                switch (checkId) {
                    case R.id.my_tip_dialog_yes:
                        if (mTipDialogYesNoListener != null) {
                            dismiss();
                            mTipDialogYesNoListener.tipProgressChoseYes();
                        }
                        break;
                    case R.id.my_tip_dialog_no:
                        if (mTipDialogYesNoListener != null) {
                            dismiss();
                            mTipDialogYesNoListener.tipProgressChoseNo();
                        }
                        break;
                }
            });
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
     * 加载弹窗,单选按钮可点击
     */
    public void setTipSingleSelectModel(boolean tipSingleSelectModel) {
        mTipSingleSelectModel = tipSingleSelectModel;
    }

    /**
     * 加载弹窗模式
     */
    public void setTipLoadModel(boolean tipLoadModel) {
        mTipLoadModel = tipLoadModel;
    }

    public interface TipDialogYesNoListener {
        void tipProgressChoseYes();

        void tipProgressChoseNo();
    }

    /**
     * 弹窗双选按钮事件监听
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

    public interface TipDialogSelectListener {
        void tipSelect();
    }

    /**
     * 进度条弹窗单选按钮选择事件监听
     */
    public void setTipDialogSelectListener(String selfBtText, TipDialogSelectListener tipDialogSelectListener) {
        if (selfBtText != null) {
            mSelfBtText = selfBtText;
        }
        mTipDialogSelectListener = tipDialogSelectListener;
    }
}
