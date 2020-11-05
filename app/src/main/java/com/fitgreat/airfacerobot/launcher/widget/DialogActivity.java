package com.fitgreat.airfacerobot.launcher.widget;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.launcher.utils.OperationUtils;
import com.fitgreat.archmvp.base.util.SpUtils;

import java.util.Timer;
import java.util.TimerTask;

import static com.fitgreat.airfacerobot.MyApp.getContext;
import static com.fitgreat.airfacerobot.constants.Constants.BASE_DIALOG_CONTENT;
import static com.fitgreat.airfacerobot.constants.Constants.BASE_DIALOG_NO;
import static com.fitgreat.airfacerobot.constants.Constants.BASE_DIALOG_TITLE;
import static com.fitgreat.airfacerobot.constants.Constants.BASE_DIALOG_YES;
import static com.fitgreat.airfacerobot.constants.RobotConfig.WHETHER_CARRY_ON_BOOT;

/**
 * 带选择按钮弹窗
 */
public class DialogActivity extends AppCompatActivity {

    private TextView dialogTitleView;
    private TextView dialogContentView;
    private RadioGroup choseRadioGroupView;
    private RadioButton choseNoBtView;
    private RadioButton choseYesBtView;
    private int countDown = 0;
    private Timer timer;
    private TimerTask timerTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        initData();
    }

    private void initData() {
        dialogTitleView = findViewById(R.id.dialog_title);
        dialogContentView = findViewById(R.id.dialog_content);
        choseRadioGroupView = findViewById(R.id.chose_radioGroup);
        choseNoBtView = findViewById(R.id.chose_no_bt);
        choseYesBtView = findViewById(R.id.chose_yes_bt);
        //30秒倒计时后关闭当前页面
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                countDown++;
                if (countDown == 30) {
                    timerTask.cancel();
                    timerTask = null;
                    timer = null;
                    finish();
                }
            }
        };
        timer.schedule(timerTask, 0, 1000);
        //设置弹窗标题  内容  按钮文字
        Intent intent = getIntent();
        String dialogTitle = intent.getStringExtra(BASE_DIALOG_TITLE);
        String dialogContent = intent.getStringExtra(BASE_DIALOG_CONTENT);
        String yesBtContent = intent.getStringExtra(BASE_DIALOG_YES);
        String noBtContent = intent.getStringExtra(BASE_DIALOG_NO);
        dialogTitleView.setText(dialogTitle);
        dialogContentView.setText(dialogContent);
        choseYesBtView.setText(yesBtContent);
        choseNoBtView.setText(noBtContent);
        choseRadioGroupView.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.chose_no_bt:
                    choseNoBt();
                    break;
                case R.id.chose_yes_bt:
                    choseYesBt();
                    break;
            }
        });
    }

    private void choseNoBt() {
        SpUtils.putInt(getContext(), WHETHER_CARRY_ON_BOOT, 200);
        timerTask.cancel();
        timerTask = null;
        timer = null;
        finish();
    }

    private void choseYesBt() {
        SpUtils.putInt(getContext(), WHETHER_CARRY_ON_BOOT, 100);
        timerTask.cancel();
        timerTask = null;
        timer = null;
        finish();
    }
}

