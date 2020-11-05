package com.fitgreat.airfacerobot.launcher.widget;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.fitgreat.airfacerobot.R;
import com.fitgreat.archmvp.base.util.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 通用提示dialog<p>
 *
 * @author zixuefei
 * @since 2020/3/24 0024 14:56
 */
public class CommonTipDialog extends AlertDialog {
    @BindView(R.id.common_tip_title)
    TextView title;
    @BindView(R.id.common_tip_content)
    TextView content;
    @BindView(R.id.common_tip_cancel)
    TextView cancel;
    @BindView(R.id.common_tip_ok)
    TextView ok;
    private OnDialogClick onDialogClick;

    public CommonTipDialog(Context context) {
        super(context, R.style.UpdateDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = UIUtils.dp2px(getContext(), 300);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
        window.setGravity(Gravity.CENTER);
        setContentView(R.layout.dialog_common_tip);
        ButterKnife.bind(this);
    }

    public void setTitleAndContent(String titleS, String contentS) {
        if (title != null && !TextUtils.isEmpty(titleS)) {
            title.setText(titleS);
        }
        if (content != null && !TextUtils.isEmpty(contentS)) {
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

    public void setOnDialogClick(OnDialogClick onDialogClick) {
        this.onDialogClick = onDialogClick;
    }

    @OnClick({R.id.common_tip_cancel, R.id.common_tip_ok})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.common_tip_cancel:
                dismiss();
                if (onDialogClick != null) {
                    onDialogClick.onClick(R.id.common_tip_cancel);
                }
                break;
            case R.id.common_tip_ok:
                dismiss();
                if (onDialogClick != null) {
                    onDialogClick.onClick(R.id.common_tip_ok);
                }
                break;
            default:
                break;
        }
    }

    public interface OnDialogClick {
        void onClick(int which);
    }

}
