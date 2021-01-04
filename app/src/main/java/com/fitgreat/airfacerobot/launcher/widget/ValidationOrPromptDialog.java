package com.fitgreat.airfacerobot.launcher.widget;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.launcher.utils.ToastUtils;
import com.fitgreat.archmvp.base.util.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 密码验证输入/密码验证不通过提示弹窗
 */
public class ValidationOrPromptDialog extends AlertDialog {

    @BindView(R.id.validation_prompt_content)
    EditText confirmPasswordContent;
    @BindView(R.id.validation_fail_tip)
    TextView validationFailTip;
    @BindView(R.id.validation_prompt_title)
    TextView validationPromptTitle;
    /**
     * 验证密码不通过提示弹窗
     */
    boolean mFailPromptTag = false;

    private ValidationFailListener mValidationFailListener = null;

    public ValidationOrPromptDialog(Context context) {
        super(context, R.style.UpdateDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.getDecorView().setPadding(0, 0, 0, 0);
        //设置弹窗款高,根据屏幕宽高
        WindowManager.LayoutParams params = window.getAttributes();
        Display defaultDisplay = window.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        params.width = (int) ((point.x)*0.4);
        params.height = (int) ((point.y)*0.4);
        window.setAttributes(params);
        window.setGravity(Gravity.CENTER);
        //设置弹窗布局
        setContentView(R.layout.dialog_validation_or_prompt);
        ButterKnife.bind(this);
        setCancelable(false);
        //更新页面显示
        if (mFailPromptTag) { //密码验证不通过提示
            validationFailTip.setVisibility(View.VISIBLE);
            confirmPasswordContent.setVisibility(View.GONE);
            validationPromptTitle.setText("提示:");
        } else { //验证密码提示
            validationFailTip.setVisibility(View.GONE);
            confirmPasswordContent.setVisibility(View.VISIBLE);
        }
    }

    @OnClick({R.id.validation_prompt_bt})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.validation_prompt_bt:
                dismiss();
                if (!mFailPromptTag) {
                    if (mValidationFailListener != null) {
                        if (TextUtils.isEmpty(confirmPasswordContent.getText().toString())){
                            ToastUtils.showSmallToast("请输入密码");
                            return;
                        }
                        mValidationFailListener.validationPassword(confirmPasswordContent.getText().toString());
                    }
                }
                break;
            default:
                break;
        }
    }

    public void setValidationFailListener(ValidationFailListener validationFailListener) {
        this.mValidationFailListener = validationFailListener;
    }

    public interface ValidationFailListener {
        void validationPassword(String password);
    }

    /**
     * 密码验证没通过提示
     *
     * @param failPromptTag
     */
    public void setFailPrompt(boolean failPromptTag) {
        this.mFailPromptTag = failPromptTag;
    }
}


