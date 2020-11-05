package com.fitgreat.airfacerobot.launcher.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.archmvp.base.util.UIUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ConfirmCancelDialog extends AlertDialog implements RadioGroup.OnCheckedChangeListener {
    @BindView(R.id.confirm_cancel_dialog_title)
    TextView confirmCancelDialogTitle;
    @BindView(R.id.confirm_cancel_dialog_content)
    TextView confirmCancelDialogContent;
    @BindView(R.id.radiogroup_confirm_cancel)
    RadioGroup radioGroupConfirmCancel;

    private ConfirmCancelListener mConfirmCancelListener = null;


    public ConfirmCancelDialog(Context context) {
        super(context);
    }

    protected ConfirmCancelDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = UIUtils.dp2px(getContext(), 257);
        params.height = UIUtils.dp2px(getContext(), 157);
        window.setAttributes(params);
        window.setGravity(Gravity.CENTER);
        setContentView(R.layout.dialog_confirm_cancel);
        ButterKnife.bind(this);
        if (radioGroupConfirmCancel != null) {
            radioGroupConfirmCancel.setOnCheckedChangeListener(this);
        }
    }

    /**
     * 设置弹窗标题  内容
     *
     * @param titleData
     * @param contentData
     */
    public void setTitleContent(String titleData, String contentData) {
        if (confirmCancelDialogTitle != null) {
            confirmCancelDialogTitle.setText(titleData);
        }
        if (confirmCancelDialogContent != null) {
            confirmCancelDialogContent.setText(contentData);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.check_confirm_bt:
                if (mConfirmCancelListener != null) {
                    dismiss();
                    mConfirmCancelListener.confirmToDo();
                }
                break;
            case R.id.check_cancel_bt:
                if (mConfirmCancelListener != null) {
                    dismiss();
                    mConfirmCancelListener.cancelToDo();
                }
                break;
        }
    }

    public interface ConfirmCancelListener {

        /**
         * 点击确认时
         */
        void confirmToDo();

        /**
         * 点击取消时
         */
        void cancelToDo();
    }

    public void setOnConfirmCancelListener(ConfirmCancelListener confirmCancelListener) {
        this.mConfirmCancelListener = confirmCancelListener;
    }
}
