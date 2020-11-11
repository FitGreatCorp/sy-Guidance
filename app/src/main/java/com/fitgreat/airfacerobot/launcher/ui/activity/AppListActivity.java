package com.fitgreat.airfacerobot.launcher.ui.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.launcher.model.AppInfo;
import com.fitgreat.airfacerobot.launcher.ui.adapter.AppListAdapter;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.RouteUtils;

import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;


/**
 * 已安装应用列表<p>
 *
 * @author zixuefei
 * @since 2020/4/28 0028 17:43
 */
public class AppListActivity extends MvpBaseActivity {
    @BindView(R.id.app_list_recycler)
    RecyclerView recyclerView;
    private AppListAdapter appListAdapter;
    private List<AppInfo> appInfoList;
    private static final String TAG = "AppListActivity";

    @Override
    public Object createPresenter() {
        return null;
    }

    @Override
    public int getLayoutResource() {
        return R.layout.activity_app_list;
    }

    @Override
    public void initData() {
        appInfoList = new ArrayList<>();
        appListAdapter = new AppListAdapter(appInfoList, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(appListAdapter);

        appInfoList.clear();
        appInfoList.addAll(getAllAppInfos());
        appListAdapter.notifyDataSetChanged();
    }

    @Override
    public void disconnectNetWork() {
        RouteUtils.goToActivity(AppListActivity.this, RobotInitActivity.class);
        //机器人状态切换为停机离线状态
        RobotInfoUtils.setRobotRunningStatus("0");
        //释放sdk,需要重新初始化dds服务
        if (SpeechManager.isDdsInitialization()) {
            //DDS需要重新初始化
            SpeechManager.instance(this).restoreToDo();
        }
        finish();
    }

    @Override
    public void disconnectRos() {
        RouteUtils.goToActivity(AppListActivity.this, RobotInitActivity.class);
        //机器人状态不在视频中时,切换为离线
        if (!RobotInfoUtils.getRobotRunningStatus().equals("4")) {
            RobotInfoUtils.setRobotRunningStatus("0");
        }
        //释放sdk,需要重新初始化dds服务
        if (SpeechManager.isDdsInitialization()) {
            //DDS需要重新初始化
            SpeechManager.instance(this).restoreToDo();
        }
        finish();
    }

    /**
     * 得到手机中所有应用信息的列表
     * AppInfo
     * Drawable icon  图片对象
     * String appName
     * String packageName
     */
    protected List<AppInfo> getAllAppInfos() {
        List<AppInfo> list = new ArrayList<>();
        // 得到应用的packgeManager
        PackageManager packageManager = getPackageManager();
        // 创建一个主界面的intent
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        // 得到包含应用信息的列表
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, 0);
        if (resolveInfoList != null && !resolveInfoList.isEmpty()) {
            // 遍历
            for (ResolveInfo ri : resolveInfoList) {
                // 得到包名
                String packageName = ri.activityInfo.packageName;
                // 得到图标
                Drawable icon = ri.loadIcon(packageManager);
                // 得到应用名称
                String appName = ri.loadLabel(packageManager).toString();
                String activityName = ri.activityInfo.name;
                LogUtils.d(TAG, "packageName:" + packageName + " appName:" + appName + " activityName:" + activityName);
                // 封装应用信息对象
                AppInfo appInfo = new AppInfo(icon, appName, packageName);
                appInfo.setActivityName(activityName);
                // 添加到list
                list.add(appInfo);
            }
        }
        return list;
    }

}
