package com.fitgreat.airfacerobot.launcher.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.fitgreat.airfacerobot.R;

public class CountDownDialog extends Dialog {
    private TextView tv_message;

    public CountDownDialog(Context context) {
        super(context, R.style.MyDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_count_down_dialog);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        tv_message = (TextView) findViewById(R.id.tv_text);
        new DownTimer().start();
    }

    class DownTimer extends CountDownTimer {

        public DownTimer() {
            // 设置时间4秒
            super(10000, 1000);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onTick(long l) {
            tv_message.setText("机器人自检超时,"+ (l/1000) +"秒后将自动重启，请稍后！");
            tv_message.setTextColor(getContext().getColor(R.color.color_444));
        }

        @Override
        public void onFinish() {

        }
    }


}
