package com.fitgreat.airfacerobot.launcher.widget;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.fitgreat.airfacerobot.R;
import java.util.Timer;
import java.util.TimerTask;
import static com.fitgreat.airfacerobot.constants.Constants.DIALOG_CONTENT;
import static com.fitgreat.airfacerobot.constants.Constants.DIALOG_TITLE;


/**
 * 内容带圆形进度条弹窗
 */
public class DialogProgressActivity extends AppCompatActivity {

    private TextView dialogProgressTitleView;
    private TextView dialogProgressContentView;
    private int countDown = 0;
    private Timer timer;
    private TimerTask timerTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_progress);
        initData();
    }

    private void initData() {
        dialogProgressTitleView = findViewById(R.id.dialog_progress_title);
        dialogProgressContentView = findViewById(R.id.dialog_progress_content);
        //30秒倒计时后关闭当前页面
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                countDown++;
                if (countDown == 5) {
                    timerTask.cancel();
                    timerTask = null;
                    timer = null;
                    setResult(RESULT_OK);
                    finish();
                }
            }
        };
        timer.schedule(timerTask, 0, 1000);
        //设置弹窗标题  内容  按钮文字
        Intent intent = getIntent();
        String dialogTitle = intent.getStringExtra(DIALOG_TITLE);
        String dialogContent = intent.getStringExtra(DIALOG_CONTENT);
        dialogProgressTitleView.setText(dialogTitle);
        dialogProgressContentView.setText(dialogContent);
    }
}

