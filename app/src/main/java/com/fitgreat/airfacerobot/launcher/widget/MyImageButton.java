package com.fitgreat.airfacerobot.launcher.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.fitgreat.airfacerobot.R;

public class MyImageButton extends androidx.appcompat.widget.AppCompatImageButton {
    public MyImageButton(Context context) {
        super(context);
    }

    public MyImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setBackgroundColor(getResources().getColor(R.color.transparent));
                setImageDrawable(getResources().getDrawable(R.mipmap.ic_map_location_select));
                break;
            case MotionEvent.ACTION_UP:
                setBackgroundColor(getResources().getColor(R.color.transparent));
                setImageDrawable(getResources().getDrawable(R.mipmap.ic_map_location_nor));
                break;
        }
        return super.onTouchEvent(event);
    }
}
