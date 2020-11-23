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
import com.fitgreat.airfacerobot.model.ActionDdsEvent;
import com.fitgreat.airfacerobot.model.NavigationTip;
import com.fitgreat.airfacerobot.launcher.utils.OperationUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

import static com.fitgreat.airfacerobot.constants.Constants.DIALOG_CONTENT;
import static com.fitgreat.airfacerobot.constants.Constants.DIALOG_NO;
import static com.fitgreat.airfacerobot.constants.Constants.DIALOG_TITLE;
import static com.fitgreat.airfacerobot.constants.Constants.DIALOG_YES;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PLAY_TASK_PROMPT_INFO;

/**
 * 弹窗选择是  否
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
        //当前导航地点
        String instructionName = intent.getStringExtra("instructionName");

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
        //导航到达终点后语音提示
        playShowText("已到" + instructionName + "啦，是否还需要其他服务呢，如不需要请点击\"不需要\"让我回去吧。");
        //1分钟计时器计时
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                countDown++;
                if (countDown == 30) { //导航到达终点30秒后语音提示
                    playShowText("是否还需要其他服务呢");
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

    /**
     * 播放并首页展示对应文案
     */
    public void playShowText(String content) {
        EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, content));
        EventBus.getDefault().post(new NavigationTip(content));
    }

    private void choseNoBt() {
        playShowText("我要回去了");
        //返回原点充电
        OperationUtils.startSpecialWorkFlow(1);
        timerTask.cancel();
        timerTask = null;
        timer = null;
        finish();
    }

    private void choseYesBt() {
        timerTask.cancel();
        timerTask = null;
        timer = null;
        finish();
    }
}

