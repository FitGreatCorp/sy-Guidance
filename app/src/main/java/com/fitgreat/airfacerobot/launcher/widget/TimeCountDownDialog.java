package com.fitgreat.airfacerobot.launcher.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.fitgreat.airfacerobot.R;
import com.fitgreat.archmvp.base.util.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TimeCountDownDialog extends AlertDialog {
    @BindView(R.id.count_down_dialog_title)
    TextView countDownDialogTitle;
    @BindView(R.id.count_down_dialog_content)
    TextView countDownDialogContent;

    private CountDownListener mCountDownListener = null;


    public TimeCountDownDialog(Context context) {
        super(context);
    }

    protected TimeCountDownDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = UIUtils.dp2px(getContext(), 357);
        params.height = UIUtils.dp2px(getContext(), 157);
        window.setAttributes(params);
        window.setGravity(Gravity.CENTER);
        setContentView(R.layout.dialog_time_count_down);
        ButterKnife.bind(this);
    }

    /**
     * 设置弹窗标题  内容
     *
     * @param titleData
     */
    public void setTitleContent(String titleData, String contentData) {
        if (countDownDialogTitle != null) {
            countDownDialogTitle.setText(titleData);
        }
        if (countDownDialogContent != null) {
            countDownDialogContent.setText(contentData);
        }
    }

    /**
     * 更新弹窗内容
     *
     * @param contentData
     */
    public void updateContent(String contentData) {
        if (countDownDialogContent != null) {
            countDownDialogContent.setText(contentData);
        }
    }


    public interface CountDownListener {
        /**
         * 只有一个按钮是点击
         */
        void clickSelfToDo();

    }

    public void setOnCountDownListener(CountDownListener countDownListener) {
        this.mCountDownListener = countDownListener;
    }

    @OnClick({R.id.cancel_self_bt})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel_self_bt:
                if (mCountDownListener != null) {
                    mCountDownListener.clickSelfToDo();
                }
                dismiss();
                break;
            default:
                break;
        }
    }
}
