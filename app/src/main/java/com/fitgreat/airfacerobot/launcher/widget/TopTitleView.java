package com.fitgreat.airfacerobot.launcher.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.fitgreat.airfacerobot.R;

public class TopTitleView extends ConstraintLayout {
    public TopTitleView(Context context) {
        super(context);
    }

    public TopTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context, attrs);
    }

    public TopTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(context, attrs);
    }

    public void initializeView(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TopTitleView);
        String titleName = typedArray.getString(R.styleable.TopTitleView_titleName);
        View inflate = LayoutInflater.from(context).inflate(R.layout.view_base_title, this, true);
        ImageView baseBack = inflate.findViewById(R.id.base_back);
        TextView baseTitle = inflate.findViewById(R.id.base_title);
        baseBack.setOnClickListener((view) -> {
            if (mBaseBackListener != null) {
                mBaseBackListener.back();
            }
        });
        baseTitle.setText(titleName);
        typedArray.recycle();
    }

    private BaseBackListener mBaseBackListener = null;

    public interface BaseBackListener {
        void back();
    }

    public void setBackKListener(BaseBackListener baseBackListener) {
        this.mBaseBackListener = baseBackListener;
    }
}
