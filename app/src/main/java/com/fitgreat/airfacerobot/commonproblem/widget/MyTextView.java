package com.fitgreat.airfacerobot.commonproblem.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.fitgreat.airfacerobot.R;

public class MyTextView extends androidx.appcompat.widget.AppCompatTextView {
    public MyTextView(Context context) {
        super(context);
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                setBackground(getResources().getDrawable(R.drawable.power_mode_layout_bg));
                break;
            case MotionEvent.ACTION_UP:
//                setBackground(getResources().getDrawable(R.drawable.item_common_problem_select));
                break;
        }
        return super.onTouchEvent(event);
    }
}
