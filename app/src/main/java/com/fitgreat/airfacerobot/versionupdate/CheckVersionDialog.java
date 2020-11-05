package com.fitgreat.airfacerobot.versionupdate;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fitgreat.airfacerobot.R;


/**
 * 获取版本更新信息<p>
 *
 * @author zixuefei
 * @since 2019/5/31 13:53
 */
public class CheckVersionDialog extends AlertDialog implements View.OnClickListener {
    private final String TAG = CheckVersionDialog.class.getSimpleName();
    private TextView versionInfo;
    private TextView upgradeBtn;
    private TextView notRemark;
    private TextView updateMineBtn;
    private ImageView closeBtn;
    private LinearLayout checkUpdateLayout;

    public CheckVersionDialog(Context context) {
        super(context, R.style.UpdateDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
        window.setGravity(Gravity.CENTER);
        setContentView(R.layout.dialog_find_version);
        initView();
    }

    private void initView() {
        versionInfo = findViewById(R.id.upgrade_text);
        versionInfo.setMovementMethod(ScrollingMovementMethod.getInstance());
        upgradeBtn = findViewById(R.id.upgrade_btn);
        closeBtn = findViewById(R.id.close_upgrade);
        notRemark = findViewById(R.id.check_version_not_remark);
        updateMineBtn = findViewById(R.id.update_mine_btn);
        checkUpdateLayout = findViewById(R.id.check_update_layout);

        upgradeBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
        notRemark.setOnClickListener(this);
        updateMineBtn.setOnClickListener(this);
    }


    @Override
    public void show() {
        super.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update_mine_btn:
            case R.id.upgrade_btn:
                closeBtn.setVisibility(View.GONE);
                upgradeBtn.setClickable(false);
                dismiss();
                break;
            case R.id.close_upgrade:
                if (isShowing()) {
                    dismiss();
                }
                break;
            case R.id.check_version_not_remark:
                dismiss();
                break;
            default:
                break;
        }
    }


}
