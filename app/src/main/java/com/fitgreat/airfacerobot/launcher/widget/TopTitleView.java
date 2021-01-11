package com.fitgreat.airfacerobot.launcher.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.fitgreat.airfacerobot.R;

public class TopTitleView extends ConstraintLayout {

    private TextView baseTitleView;

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
        int titleColor = typedArray.getColor(R.styleable.TopTitleView_titleColor, Color.BLACK);
        int itemBackColor = typedArray.getColor(R.styleable.TopTitleView_itemBackColor, Color.WHITE);
        View inflate = LayoutInflater.from(context).inflate(R.layout.view_base_title, this, true);
        ConstraintLayout itemViewBaseView = inflate.findViewById(R.id.item_view_base);
        ConstraintLayout baseBackView = inflate.findViewById(R.id.container_base_back);
        baseBackView.setOnClickListener((view) -> {
            if (mBaseBackListener != null) {
                mBaseBackListener.back();
            }
        });
        baseTitleView = inflate.findViewById(R.id.base_title);
        baseTitleView.setText(titleName);
        baseTitleView.setTextColor(titleColor);
        itemViewBaseView.setBackgroundColor(itemBackColor);
        typedArray.recycle();
    }

    private BaseBackListener mBaseBackListener = null;

    /**
     * 左上角返回按钮点击事件
     */
    public interface BaseBackListener {
        void back();
    }

    public void setBackKListener(BaseBackListener baseBackListener) {
        this.mBaseBackListener = baseBackListener;
    }

    /**
     * 设置标题内容
     *
     * @param baseTitleValue
     */
    public void setBaseTitle(String baseTitleValue) {
        baseTitleView.setText(baseTitleValue);
    }
}
