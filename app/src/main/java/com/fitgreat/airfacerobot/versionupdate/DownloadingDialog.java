package com.fitgreat.airfacerobot.versionupdate;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fitgreat.airfacerobot.R;

/**
 * app升级下载弹框<p>
 *
 * @author zixuefei
 * @since 2019/6/9 14:48
 */
public class DownloadingDialog extends AlertDialog {
    private final String TAG = DownloadingDialog.class.getSimpleName();
    private ProgressBar mProgressBar;
    private TextView mUpdatePercent,message;

    public DownloadingDialog(Context context) {
        super(context, R.style.UpdateDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
        window.setGravity(Gravity.CENTER);
        setContentView(R.layout.dialog_version_upgrade_downloading);
        initView();
        setCancelable(false);
    }


    private void initView() {
        message = findViewById(R.id.message);
        mProgressBar = findViewById(R.id.update_progress);
        mUpdatePercent = findViewById(R.id.percent);
    }

    public void updateProgress(int progress) {
        if (!isShowing() || mProgressBar == null || mUpdatePercent == null) {
            return;
        }
        mProgressBar.setProgress(progress);
        mUpdatePercent.setText(progress + "%");
    }

    public void setMessage(String msg){
        message.setText(msg);
    }

}
