package com.fitgreat.airfacerobot.launcher.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.fitgreat.airfacerobot.R;

public class ClickImageButton extends androidx.appcompat.widget.AppCompatImageButton {
    public ClickImageButton(Context context) {
        super(context);
    }

    public ClickImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        setBackgroundColor(getResources().getColor(R.color.transparent));
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
               setImageDrawable(getResources().getDrawable(R.mipmap.ic_map_location_select));
                break;
            case MotionEvent.ACTION_UP:
                setImageDrawable(getResources().getDrawable(R.mipmap.ic_map_location_nor));
                break;
        }
        return super.onTouchEvent(event);
    }
}
