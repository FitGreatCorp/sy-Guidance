package com.fitgreat.airfacerobot.launcher.ui.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;

import com.fitgreat.airfacerobot.R;
import com.fitgreat.archmvp.base.util.JsonUtils;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.geek.webpage.web.activity.BaseWebpageActivity;
import com.gyf.barlibrary.BarHide;
import com.gyf.barlibrary.ImmersionBar;


/**
 * H5页面加载<p>
 *
 * @author zixuefei
 * @since 2020/4/1 17:37
 */
public class WebPageActivity extends BaseWebpageActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d("zxf", "webPageEntity:" + JsonUtils.encode(webPageEntity));
        //初始化沉浸式
        if (isImmersionBarEnabled()) {
            initImmersionBar(webPageEntity.isDarkFont);
        }
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setTitleBarRes(R.color.white, R.mipmap.btn_back);
    }

    /**
     * 是否可以使用沉浸式
     * Is immersion bar enabled boolean.
     *
     * @return the boolean
     */
    protected boolean isImmersionBarEnabled() {
        return true;
    }

    protected void initImmersionBar(boolean isDarkFont) {
        //在BaseActivity里初始化
        ImmersionBar.with(this)
                .statusBarDarkFont(isDarkFont, 0.3f)
                .statusBarColor(R.color.transparent)
                .hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR)
                .init();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 友盟分享回调
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 重置App界面的字体大小，fontScale 值为 1 代表默认字体大小
     *
     * @return suyan
     */
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = res.getConfiguration();
        config.fontScale = 1;
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return super.onKeyDown(keyCode, event);
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}