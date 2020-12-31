package com.fitgreat.airfacerobot.constants;

import android.os.Environment;

import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.archmvp.base.util.SpUtils;

/**
 * 常量<p>
 *
 * @author zixuefei
 * @since 2020/3/17 0017 15:37
 */
public interface Constants {
    /**
     * 接收到守护进程的广播
     */
    String HEART_BEAT_ACTION = "com.fitgreat.action.HEART_BEAT";

    /**
     * 发送给守护进程的广播
     */
    String ACTION_DAEMON_MSG = "com.fitgreat.action.COMMON_MSG";
    String ACTION_REBOOT = "com.fitgreat.action.reboot";
    String ACTION_SHUTDOWN = "com.fitgreat.action.shutdown";
    /**
     * 讯飞语音APPID
     */
    String XUNFEI_APP_ID = "5e86c744";

    /**
     * 入院宣教
     */
    String COMMAND_ADMISSION_MISSION = "入院宣教";
    /**
     * 出院宣教
     */
    String COMMAND_DISCHARGE_MISSION = "出院宣教";
    /**
     * 机器网络断开
     */
    String NETWORK_DISCONNECT_TAG = "network_disconnect_tag";

    /**
     * 初始化页面signal初始化成功
     */
    String INIT_SIGNAL_SUCCESS = "init_signal_success";
    /**
     * 初始化页面ros初始化成功
     */
    String INIT_ROS_SUCCESS = "init_ros_success";
    /**
     * 初始化页面voice初始化成功
     */
    String INIT_VOICE_SUCCESS = "init_voice_success";
    /**
     * 设置模块密码
     */
    String SETUP_MODULE_PASSWORD = "setup.module.password";

    /**
     * 异常日志文件创建时间
     */
    String LOGFILE_CREATE_TIME = "logfile_create_time";
    /**
     * 异常日志本地保存父目录
     */
    String LOG_PARENT_FILE_PATH = Environment.getExternalStorageDirectory() + "/Android/data/com.fitgreat.airfacerobot/log/";
    /**
     * 异常日志文件保存名字
     */
    String LOG_FILE_PATH = LOG_PARENT_FILE_PATH + RobotInfoUtils.getAirFaceDeviceId() + "-" + SpUtils.getString(MyApp.getContext(), LOGFILE_CREATE_TIME, null) + ".txt";

    /**
     * 公用弹窗标题
     */
    String DIALOG_TITLE = "dialog_title";

    /**
     * 公用弹窗提示内容
     */
    String DIALOG_CONTENT = "dialog_content";

    /**
     * 公用弹窗选择按钮是
     */
    String DIALOG_YES = "dialog_yes";

    /**
     * 公用弹窗选择按钮否
     */
    String DIALOG_NO = "dialog_no";
    /**
     * 单点导航指令
     */
    String SINGLE_POINT_NAVIGATION = "single_point_navigation";

    /**
     * 首页收到指令跳转常见问题模块
     */
    String COMMON_PROBLEM_TAG = "common_problem_tag";
    /**
     * 导航任务
     */
    String COMMON_PROBLEM_BY_INSTRUCTION = "common_problem_by_instruction";

    /**
     * 常见问题指令
     */
    String DEFAULT_LOG_TAG = "guidance_sy_hxk";

    String basePath = Environment.getExternalStorageDirectory().getPath() + "/ExternalResource/";
    /**
     * 中文地图本地存储路径
     */
    String currentChineseMapPath = basePath + "currentChineseMap.png";
    /**
     * 英文地图本地存储路径
     */
    String currentEnglishMapPath = basePath + "currentEnglishMap.png";

}
