package com.fitgreat.airfacerobot.launcher.widget;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.launcher.utils.ToastUtils;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.PhoneInfoUtils;
import com.fitgreat.archmvp.base.util.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 设置设备序列号dialog<p>
 *
 * @author zixuefei
 * @since 2020/4/7 0024 14:56
 */
public class InputDialog extends AlertDialog {
    @BindView(R.id.common_tip_title)
    TextView title;
    @BindView(R.id.common_tip_content)
    EditText content;
    @BindView(R.id.common_tip_cancel)
    TextView cancel;
    @BindView(R.id.common_tip_ok)
    TextView ok;
    private onInputDoneListener onInputDoneListener;

    public InputDialog(Context context) {
        super(context, R.style.UpdateDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = UIUtils.dp2px(getContext(), 257);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
        window.setGravity(Gravity.CENTER);
        setContentView(R.layout.dialog_input_device_id);
        ButterKnife.bind(this);
        setCancelable(false);
        setCancelVisible(false);

        content.setText(PhoneInfoUtils.getIMEI(getContext()));
    }

    public void setTitleAndContent(String titleS, String contentS) {
        if (title != null) {
            title.setText(titleS);
        }
        if (content != null) {
            content.setText(contentS);
        }
    }

    public void setOkVisible(boolean visible) {
        if (ok != null) {
            ok.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setCancelVisible(boolean visible) {
        if (cancel != null) {
            cancel.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @OnClick({R.id.common_tip_cancel, R.id.common_tip_ok})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.common_tip_cancel:
                dismiss();
                break;
            case R.id.common_tip_ok:
                if (!TextUtils.isEmpty(content.getText().toString().trim())) {
                    LogUtils.d("input", "设备序列号:" + content.getText().toString().trim());
                    RobotInfoUtils.setAirFaceDeviceId(content.getText().toString().trim());
                    if (onInputDoneListener != null) {
                        onInputDoneListener.onDone();
                    }
                    dismiss();
                } else {
                    ToastUtils.showSmallToast("请输入正确的设备序列号");
                }
                break;
            default:
                break;
        }
    }

    public void setOnInputDoneListener(onInputDoneListener onInputDoneListener) {
        this.onInputDoneListener = onInputDoneListener;
    }

    public interface onInputDoneListener {
        void onDone();
    }
}


