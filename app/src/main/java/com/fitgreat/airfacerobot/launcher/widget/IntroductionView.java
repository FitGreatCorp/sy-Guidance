package com.fitgreat.airfacerobot.launcher.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.fitgreat.airfacerobot.R;

/**
 * 院内介绍操作任务列表条目自定义控件
 */

public class IntroductionView extends ConstraintLayout {

    private Bitmap introductionBackImageBitmap, introductionKindBitmap;
    private String mIntroductionTitle;
    private int introductionBackImageId;
    private int introductionKindId;

    public IntroductionView(Context context) {
        super(context);
    }

    public IntroductionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context, attrs);
    }

    public IntroductionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(context, attrs);
    }

    public void initializeView(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IntroductionView);
        mIntroductionTitle = typedArray.getString(R.styleable.IntroductionView_introductionTitle);
        introductionBackImageId = typedArray.getResourceId(R.styleable.IntroductionView_introductionBackImage, 0);
        introductionKindId = typedArray.getResourceId(R.styleable.IntroductionView_introductionKindImage, 0);

        View inflate = LayoutInflater.from(context).inflate(R.layout.item_introduction_process, this, true);
        ImageView introductionImageView = inflate.findViewById(R.id.main_introduction_process_image);
        ImageView introductionKindView = inflate.findViewById(R.id.introduction_process_kind_image);
        TextView introductionTitleView = inflate.findViewById(R.id.item_introduction_process_title);

        introductionTitleView.setText(mIntroductionTitle);
        if (introductionBackImageId != 0) {
            introductionBackImageBitmap = BitmapFactory.decodeResource(getResources(), introductionBackImageId);
        }
        if (introductionBackImageBitmap != null) {
            introductionImageView.setImageBitmap(introductionBackImageBitmap);
        }
        if (introductionKindId != 0) {
            introductionKindBitmap = BitmapFactory.decodeResource(getResources(), introductionKindId);
        }
        if (introductionKindBitmap != null) {
            introductionKindView.setImageBitmap(introductionKindBitmap);
        }
        typedArray.recycle();
    }

    /**
     * 设置条目标题
     */
    public void setIntroductionTitle(String introductionTitle) {
        this.mIntroductionTitle = introductionTitle;
    }

    /**
     * 设置条目背景图
     */
    public void setIntroductionBackImage(int backResourceId) {
        this.introductionBackImageId = backResourceId;
    }

    /**
     * 设置种类表示图
     */
    public void setIntroductionKindImage(int kindResourceId) {
        this.introductionKindId = kindResourceId;
    }
}
