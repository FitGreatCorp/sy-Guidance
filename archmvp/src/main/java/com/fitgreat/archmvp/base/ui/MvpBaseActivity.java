package com.fitgreat.archmvp.base.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fitgreat.archmvp.base.util.LogUtils;
import com.gyf.barlibrary.ImmersionBar;

import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * MVPPlugin
 * Mvp Activity基类
 *
 * @author zixuefei
 * @since 2019/5/23 20:38
 */

public abstract class MvpBaseActivity<V extends BaseView, T extends BasePresenterImpl<V>> extends AppCompatActivity implements BaseView {
    private static final String TAG = "MvpBaseActivity";
    public T mPresenter;
    protected ImmersionBar mImmersionBar;
    private Unbinder unbinder;
    private NetWorkChangeBroadcastReceiver myReceiver;
    public String NETWORK_CONNECTION_CHECK_SUCCESS = "network.connection.check_success";
    public String NETWORK_CONNECTION_CHECK_FAILURE = "network.connection.check_failure";
    public String ROS_CONNECTION_CHECK_FAILURE = "ros.connection.check_failure";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        //初始化沉浸式
        if (isImmersionBarEnabled()) {
            initImmersionBar(false);
        }
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        setContentView(getLayoutResource());
        unbinder = ButterKnife.bind(this);
        StringBuilder stringBuilder = new StringBuilder();
        mPresenter = createPresenter();
        if (mPresenter != null) {
            mPresenter.attachView((V) this);
        }
        initData();
        //注册监听广播
        myReceiver = new NetWorkChangeBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(NETWORK_CONNECTION_CHECK_SUCCESS);
        filter.addAction(NETWORK_CONNECTION_CHECK_FAILURE);
        filter.addAction(ROS_CONNECTION_CHECK_FAILURE);
        registerReceiver(myReceiver, filter);
    }

    private class NetWorkChangeBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.d(TAG, "NetWorkChangeBroadcastReceiver");
            if (intent.getAction().equals(NETWORK_CONNECTION_CHECK_FAILURE)) {
                LogUtils.d(TAG, "网路连接断开");
                disconnectNetWork();
            } else if (intent.getAction().equals(NETWORK_CONNECTION_CHECK_SUCCESS)) {
                LogUtils.d(TAG, "网路连接可用");
            }else if (intent.getAction().equals(ROS_CONNECTION_CHECK_FAILURE)) {
                LogUtils.d(TAG, "ros连接断开");
                disconnectRos();
            }
        }
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
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.statusBarDarkFont(isDarkFont, 0.3f)
                .statusBarColor(android.R.color.transparent)
                .init();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.detachView();
        }

        // 必须调用该方法，防止内存泄漏
        if (mImmersionBar != null) {
            mImmersionBar.destroy();
        }

        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        unregisterReceiver(myReceiver);
    }

    @Override
    public Context getContext() {
        return this;
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

    public abstract <T> T createPresenter();

    public abstract int getLayoutResource();

    public abstract void initData();

    /**
     * 网络断开连接
     */
    public abstract void disconnectNetWork();
    /**
     * ros连接断开
     */
    public abstract void disconnectRos();

}
