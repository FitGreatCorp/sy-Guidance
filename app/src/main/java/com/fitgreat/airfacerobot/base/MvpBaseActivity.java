package com.fitgreat.airfacerobot.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fitgreat.airfacerobot.launcher.utils.LanguageUtil;
import com.fitgreat.archmvp.base.ui.BasePresenterImpl;
import com.fitgreat.archmvp.base.ui.BaseView;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.gyf.barlibrary.ImmersionBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TWO;
import static com.fitgreat.airfacerobot.constants.RobotConfig.NETWORK_CONNECTION_CHECK_FAILURE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.NETWORK_CONNECTION_CHECK_SUCCESS;
import static com.fitgreat.airfacerobot.constants.RobotConfig.ROS_CONNECTION_CHECK_FAILURE;

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
    private BaseChangeBroadcastReceiver myReceiver;
    //页面集合
    private List<AppCompatActivity> activityList = new ArrayList<AppCompatActivity>();
    private static Context activityContext;
    public Handler baseHandler = new Handler(Looper.getMainLooper());
    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        //初始化沉浸式
        if (isImmersionBarEnabled()) {
            initImmersionBar(false);
        }
        //设置机器人最新的语言
        LanguageUtil.changeAppLanguage(this);
        setContentView(getLayoutResource());
        unbinder = ButterKnife.bind(this);
        mPresenter = createPresenter();
        if (mPresenter != null) {
            mPresenter.attachView((V) this);
        }
        initData();
        //注册监听广播
        myReceiver = new BaseChangeBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(NETWORK_CONNECTION_CHECK_SUCCESS);
        filter.addAction(NETWORK_CONNECTION_CHECK_FAILURE);
        filter.addAction(ROS_CONNECTION_CHECK_FAILURE);
        registerReceiver(myReceiver, filter);
        //添加当前页面
        activityList.add(this);
        activityContext = this;
        //注册网络状态监听广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            LogUtils.d(DEFAULT_LOG_TAG, "当前网络状态改变,网络是否可用  "+activeNetworkInfo.isAvailable());
            if (!activeNetworkInfo.isAvailable()) { //当前网络连接不可用
                disconnectNetWork();
            }
        }
    }

    public static Context getActivityContext() {
        return activityContext;
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
        if (myReceiver != null) {
            unregisterReceiver(myReceiver);
        }
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
        //清除当前页面
        activityList.remove(this);
    }

    /**
     * 退出页面时清空所有页面
     */
    public void clearActivity() {
        for (AppCompatActivity appCompatActivity : activityList
        ) {
            appCompatActivity.finish();
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

//    /**
//     * 重置App界面的字体大小，fontScale 值为 1 代表默认字体大小
//     *
//     * @return suyan
//     */
//    @Override
//    public Resources getResources() {
//        Resources res = getResources();
//        Configuration config = res.getConfiguration();
//        config.fontScale = 1;
//        res.updateConfiguration(config, res.getDisplayMetrics());
//        return res;
//    }

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

    private class BaseChangeBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.d(TAG, "NetWorkChangeBroadcastReceiver");
            if (intent.getAction().equals(NETWORK_CONNECTION_CHECK_FAILURE)) {
                LogUtils.d(DEFAULT_LOG_TAG, "--------BaseChangeBroadcastReceiver-----网路连接断开");

            } else if (intent.getAction().equals(NETWORK_CONNECTION_CHECK_SUCCESS)) {
                LogUtils.d(DEFAULT_LOG_TAG, "--------BaseChangeBroadcastReceiver-----网路连接可用");
            } else if (intent.getAction().equals(ROS_CONNECTION_CHECK_FAILURE)) {
                LogUtils.d(DEFAULT_LOG_TAG, "ros连接断开");
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

//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(LanguageUtil.attachBaseContext(newBase));
//    }
}
