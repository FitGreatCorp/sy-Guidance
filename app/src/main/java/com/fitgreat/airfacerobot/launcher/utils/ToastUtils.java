package com.fitgreat.airfacerobot.launcher.utils;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;

public class ToastUtils {

    public static void showSmallToast(String tvStr) {
        if (TextUtils.isEmpty(tvStr)) {
            return;
        }
        try {
            Toast toast2 = new Toast(MyApp.getContext());
            View view = LayoutInflater.from(MyApp.getContext()).inflate(R.layout.publish_success_tip, null);
            TextView tv = view.findViewById(R.id.toast_text);
            tv.setText(tvStr);
            toast2.setView(view);
            toast2.setGravity(Gravity.CENTER, 0, 0);
            toast2.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}