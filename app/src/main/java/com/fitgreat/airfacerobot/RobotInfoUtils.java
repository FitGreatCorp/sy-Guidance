package com.fitgreat.airfacerobot;

import android.text.TextUtils;

import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.model.InitEvent;
import com.fitgreat.airfacerobot.remotesignal.model.GroupInfoData;
import com.fitgreat.airfacerobot.remotesignal.model.RobotInfoData;
import com.fitgreat.archmvp.base.util.JsonUtils;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.SpUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * 机器人token，基本信息<p>
 *
 * @author zixuefei
 * @since 2020/3/23 0023 15:30
 */
public class RobotInfoUtils {
    private final static String TAG = RobotInfoUtils.class.getSimpleName();
    private final static String TOKEN = "token";
    private final static String ROBOT_INFO = "robot_info";
    private final static String ROBOT_GROUP_INFO = "robot_group_info";
    private final static String ROBOT_VOICE_ENABLED = "robot_voice_enabled";
    private final static String ROBOT_DEVICE_ID = "robot_device_id";
    private final static String ROBOT_HARDWARE_VERSION = "robot_hardware_version";
    private final static String ROBOT_RUNNING_STATUS = "robot_running_status";
    private final static String ROBOT_LINKED_DEVICES_URL = "robot_linked_devices";


    /**
     * 获取机器人运行状态
     * 机器人状态（0:停机，1:空闲,2:移动中，3：执行操作中,4:视频，5:充电中 ,6:升级中）
     */
    public static String getRobotRunningStatus() {
        return SpUtils.getString(MyApp.getContext(), ROBOT_RUNNING_STATUS, "1");
    }

    /**
     * 设置机器人运行状态
     * 机器人状态（0:停机，1:空闲,2:移动中，3：执行操作中,4:视频，5:充电中 ,6:升级中）
     */
    public static void setRobotRunningStatus(String status) {
        SpUtils.putString(MyApp.getContext(), ROBOT_RUNNING_STATUS, status);
        //更新服务端机器人状态
        EventBus.getDefault().post(new InitEvent(RobotConfig.UPDATE_ROBOT_STATE_TAG, status));
    }


    public static String getToken() {
        return SpUtils.getString(MyApp.getContext(), TOKEN, "");
    }

    public static void saveToken(String token) {
        SpUtils.putString(MyApp.getContext(), TOKEN, token);
    }

    /**
     * 获取机器人信息
     */
    public static RobotInfoData getRobotInfo() {
        String robotInfo = SpUtils.getString(MyApp.getContext(), ROBOT_INFO, "");
        return TextUtils.isEmpty(robotInfo) ? null : JsonUtils.decode(robotInfo, RobotInfoData.class);
    }

    public static void saveRobotInfo(RobotInfoData robotInfoData) {
        SpUtils.putString(MyApp.getContext(), ROBOT_INFO, JsonUtils.encode(robotInfoData));
        EventBus.getDefault().post(robotInfoData);
    }

    public static boolean isPlayVoiceEnabled() {
        return SpUtils.getBoolean(MyApp.getContext(), ROBOT_VOICE_ENABLED, true);
    }

    public static void setPlayVoiceEnabled(boolean enabled) {
        SpUtils.putBoolean(MyApp.getContext(), ROBOT_VOICE_ENABLED, enabled);
    }

    /**
     * 获取机器人加入群组信息
     */
    public static GroupInfoData getRobotGroupInfo() {
        String groupInfo = SpUtils.getString(MyApp.getContext(), ROBOT_GROUP_INFO, "");
        return TextUtils.isEmpty(groupInfo) ? null : JsonUtils.decode(groupInfo, GroupInfoData.class);
    }

    public static void saveRobotGroupInfo(GroupInfoData groupInfoData) {
        SpUtils.putString(MyApp.getContext(), ROBOT_GROUP_INFO, JsonUtils.encode(groupInfoData));
    }

    /**
     * 获取设备ID序列号
     */
    public static String getAirFaceDeviceId() {
        return SpUtils.getString(MyApp.getContext(), ROBOT_DEVICE_ID, "");
    }

    /**
     * 设置设备ID序列号
     */
    public static void setAirFaceDeviceId(String deviceId) {
        LogUtils.d(TAG, "setAirFaceDeviceId:" + deviceId);
        SpUtils.putString(MyApp.getContext(), ROBOT_DEVICE_ID, deviceId);
    }


    /**
     * 获取硬件版本号
     */
    public static String getHardwareVersion() {
        return SpUtils.getString(MyApp.getContext(), ROBOT_HARDWARE_VERSION, "1.0.0");
    }

    /**
     * 保存硬件版本号
     */
    public static void setHardwareVersion(String version) {
        LogUtils.d(TAG, "setHardwareVersion:" + version);
        SpUtils.putString(MyApp.getContext(), ROBOT_HARDWARE_VERSION, version);
    }

    /**
     * 获取机器人连接设备地址
     */
    public static String getRobotLinkedDevicesUrl() {
        return SpUtils.getString(MyApp.getContext(), ROBOT_LINKED_DEVICES_URL, "");
    }

    /**
     * 设置机器人连接设备地址
     */
    public static void setRobotLinkedDevicesUrl(String url) {
        SpUtils.putString(MyApp.getContext(), ROBOT_LINKED_DEVICES_URL, url);
    }
}
