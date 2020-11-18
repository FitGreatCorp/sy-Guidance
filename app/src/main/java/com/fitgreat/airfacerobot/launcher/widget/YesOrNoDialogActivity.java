package com.fitgreat.airfacerobot.launcher.widget;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.launcher.model.ActionEvent;
import com.fitgreat.airfacerobot.remotesignal.model.SignalDataEvent;
import org.greenrobot.eventbus.EventBus;
import java.util.Timer;
import java.util.TimerTask;

import static com.fitgreat.airfacerobot.constants.Constants.DIALOG_CONTENT;
import static com.fitgreat.airfacerobot.constants.Constants.DIALOG_NO;
import static com.fitgreat.airfacerobot.constants.Constants.DIALOG_TITLE;
import static com.fitgreat.airfacerobot.constants.Constants.DIALOG_YES;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PLAY_TASK_PROMPT_INFO;

/**
 * 下次导航提示框
 */
public class YesOrNoDialogActivity extends AppCompatActivity {

    private TextView dialogTitleView;
    private TextView dialogContentView;
    private RadioGroup choseRadioGroupView;
    private RadioButton choseNoBtView;
    private RadioButton choseYesBtView;
    private int countDown = 0;
    private Timer timer;
    private TimerTask timerTask;
    private String instructionId;
    private String currentFx;
    private String currentFy;
    private String currentFz;
    private String currentNavigationDestination;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yes_or_no);
        initData();
    }

    private void initData() {
        dialogTitleView = findViewById(R.id.dialog_title);
        dialogContentView = findViewById(R.id.dialog_content);
        choseRadioGroupView = findViewById(R.id.chose_radioGroup);
        choseNoBtView = findViewById(R.id.chose_no_bt);
        choseYesBtView = findViewById(R.id.chose_yes_bt);


        //设置弹窗标题  内容  按钮文字
        Intent intent = getIntent();
        String dialogTitle = intent.getStringExtra(DIALOG_TITLE);
        String dialogContent = intent.getStringExtra(DIALOG_CONTENT);
        String yesBtContent = intent.getStringExtra(DIALOG_YES);
        String noBtContent = intent.getStringExtra(DIALOG_NO);
        //导航任务信息
//        currentNavigationDestination = intent.getStringExtra(CURRENT_NAVIGATION_DESTINATION);
        if (null != getIntent().getStringExtra("instructionId") && !"".equals(getIntent().getStringExtra("instructionId")) && !"null".equals(getIntent().getStringExtra("instructionId"))) {
            instructionId = getIntent().getStringExtra("instructionId");
        }
        currentFx = intent.getStringExtra("current_fx");
        currentFy = intent.getStringExtra("current_fy");
        currentFz = intent.getStringExtra("current_fz");

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
        //1分钟计时器计时
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                countDown++;
                if (countDown == 30) {
                    //30秒语音提示
                    EventBus.getDefault().post(new ActionEvent(PLAY_TASK_PROMPT_INFO, "请确认是否需要带你前往" + currentNavigationDestination));
                } else if (countDown == 60) {
                    choseNoBt();
                }
            }
        };
        timer.schedule(timerTask, 0, 1000);

        //设置弹窗大小
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        Point point = new Point();
        defaultDisplay.getSize(point);
        attributes.width = (int) ((point.x) * 0.4);
        attributes.height = (int) ((point.y) * 0.35);
        window.setAttributes(attributes);
    }

    private void choseNoBt() {
        //取消当前导航任务
        SignalDataEvent instruct = new SignalDataEvent();
//        instruct.setType(CANCEL_GUIDE_WORK_FLOW);
        instruct.setInstructionId(instructionId);
        instruct.setAction("3");
        EventBus.getDefault().post(instruct);

        timerTask.cancel();
        timerTask = null;
        timer = null;
        finish();
    }

    private void choseYesBt() {
        //继续执行当前导航任务
        SignalDataEvent instruct = new SignalDataEvent();
//        instruct.setType(CARRY_ON_NAVIGATION_TASK);
        instruct.setInstructionId(instructionId);
        instruct.setPosition_X(Double.valueOf(currentFx));
        instruct.setPosition_Y(Double.valueOf(currentFy));
        instruct.setPosition_Z(Double.valueOf(currentFz));
        instruct.setF_InstructionName(currentNavigationDestination);
        instruct.setAction("1");
        EventBus.getDefault().post(instruct);

        timerTask.cancel();
        timerTask = null;
        timer = null;
        finish();
    }
}

