package com.fitgreat.airfacerobot.launcher.contractview;

import android.util.Log;

import com.fitgreat.airfacerobot.versionupdate.VersionInfo;
import com.fitgreat.archmvp.base.ui.BaseView;


/**
 * 启动页视图接口<p>
 *
 * @author zixuefei
 * @since 2020/3/11 0011 10:11
 */
public interface MainView extends BaseView {

    void foundHardwareNewVersion(VersionInfo versionInfo);

    void getLocationFailure(String msg);

    void getOperationListFailure(String msg);

}
