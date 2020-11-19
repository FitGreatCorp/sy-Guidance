package com.fitgreat.airfacerobot.model;

import android.graphics.drawable.Drawable;

/**
 * 已安装app信息<p>
 *
 * @author zixuefei
 * @since 2020/4/28 0028 17:55
 */
public class AppInfo {
    private Drawable icon;// 应用图标
    private String appName;// 应用名称
    private String packageName;// 包名
    private String activityName;

    public AppInfo(Drawable icon, String appName, String packageName) {
        this.icon = icon;
        this.appName = appName;
        this.packageName = packageName;
    }

    public AppInfo() {
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    @Override
    public String toString() {
        return "AppInfo [activityName=" + activityName + ", appName=" + appName
                + ", packageName=" + packageName + "]";
    }

}
