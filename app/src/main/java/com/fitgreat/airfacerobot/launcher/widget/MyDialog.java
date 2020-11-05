package com.fitgreat.airfacerobot.launcher.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fitgreat.airfacerobot.R;

/**
 * 更新提示弹框
 */
public class MyDialog extends Dialog implements View.OnTouchListener {
    private Button btn_cancel, btn_ok;
    private TextView tv_version, tv_update_content, tv_title;

    private String str_yes, str_no, str_title, str_version, str_update_message;
    private onPositiveClicListener mPositiveClicListener;
    private onNegativeClickListener mNegativeClickListener;
    private boolean is_one = false;
    private boolean show_second_title = false;

    public MyDialog(Context context) {
        super(context, R.style.MyDialog);
    }

    public void setPositiveOnclicListener(String positivestr, onPositiveClicListener positiveClicListener) {
        if (positivestr != null) {
            str_yes = positivestr;
        }
        mPositiveClicListener = positiveClicListener;
    }
    public MyDialog(Context context, int themeResId) {
        super(context, themeResId);
    }
    public void setNegativeOnclicListener(String negativestr, onNegativeClickListener negativeClickListener) {
        if (negativestr != null) {
            str_no = negativestr;
        }
        mNegativeClickListener = negativeClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_dialog_layout);
        setCanceledOnTouchOutside(false);
        //初始化界面控件
        initView();
        //设置字符串
        initData();
        //初始化点击事件
        initEvent();
    }

    private void initView() {
        btn_ok = (Button) findViewById(R.id.btn_ok);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_version = (TextView) findViewById(R.id.tv_version);
        tv_update_content = (TextView) findViewById(R.id.tv_update_content);


        if (is_one) {
            btn_cancel.setVisibility(View.GONE);
            btn_ok.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        } else {
            btn_cancel.setVisibility(View.VISIBLE);
        }
    }

    private void initData() {
        if (str_title != null) {
            tv_title.setText(str_title);
        }

        if (str_version != null) {
            tv_version.setText(str_version);
        }

        if (str_update_message != null) {
            tv_update_content.setText(str_update_message);
        }
        if (str_yes != null) {
            btn_ok.setText(str_yes);
        }
        if (str_no != null) {
            btn_cancel.setText(str_no);
        }
    }

    private void initEvent() {
        btn_cancel.setOnTouchListener(this);
        btn_ok.setOnTouchListener(this);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPositiveClicListener != null) {
                    mPositiveClicListener.onPositiveClick();
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNegativeClickListener != null) {
                    mNegativeClickListener.onNegativeClick();
                }
            }
        });

    }

    public void showSecondTitle(boolean show_Second_title) {
        this.show_second_title = show_Second_title;
    }

    public void setOneButton(boolean isOneButton) {
        is_one = isOneButton;
    }

    public void setTitle(String title) {
        str_title = title;
    }

    public void setVersion(String version) {
        str_version = version;
    }

    public void setMessage(String message) {
        str_update_message = message;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        return false;
    }

    public interface onPositiveClicListener {
        public void onPositiveClick();
    }

    public interface onNegativeClickListener {
        public void onNegativeClick();
    }
}
