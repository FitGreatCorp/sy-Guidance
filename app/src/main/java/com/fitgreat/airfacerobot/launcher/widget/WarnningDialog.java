package com.fitgreat.airfacerobot.launcher.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.fitgreat.airfacerobot.R;

public class WarnningDialog extends Dialog {
    private static final String TAG = "WarnningDialog";

    private LinearLayout ll_warnning;
    private AnimatorSet animatorSet;


    public WarnningDialog(Context context) {
        super(context, R.style.MyDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_warnning);
        setCanceledOnTouchOutside(false);
        initView();
        initAnimator();
    }

    private void initView(){
        ll_warnning = (LinearLayout) findViewById(R.id.ll_warnning);
    }
    private void initAnimator(){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(ll_warnning,"alpha",0.3f,1f,0.3f);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setDuration(3600);

        animatorSet = new AnimatorSet();
        animatorSet.playSequentially(objectAnimator);

    }

    @Override
    protected void onStart() {
        super.onStart();
        animatorSet.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        animatorSet.end();
    }
}
