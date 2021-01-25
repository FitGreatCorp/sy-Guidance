package com.fitgreat.airfacerobot.constants;

/**
 * 机器人配置常量<p>
 *
 * @author zixuefei
 * @since 2020/3/20 0020 11:46
 */
public interface RobotConfig {

    String ROS_MSG_BATTERY = "ros_battery";
    String ROS_MSG_ROBOT_POSITION = "ros_robot_position";
    String ROBOT_SIM_SIGNAL = "sim_signal";
    String ROBOT_VOICE_SIGNAL = "voice_signal";
    /**
     * 展示提示框
     */
    String TYPE_SHOW_TIPS = "show_tips";
    String TYPE_SHOW_VERSION_TIPS = "show_version_tips";
    String TYPE_SHOW_VERSION_PROGRESS = "show_version_progress";
    String TYPE_SHOW_VERSION_STATUS = "show_version_status";
    String TASK_DIALOG = "task_dialog";
    String FILE_PLAY_OK = "play_file_ok";
    String FILE_PLAY_CANCEL = "play_file_cancel";
    /**
     * 初始化类型
     */
    String TYPE_CHECK_STATE_DONE = "check_state_done";
    String TYPE_CHECK_STATE = "check_state";
    String INIT_TYPE_SIGNAL = "init_signal";
    String INIT_TYPE_ROS = "init_ros";
    String INIT_TYPE_VOICE = "init_voice";
    String MSG_START_INIT_ROS = "msg_start_init_ros";
    String MSG_START_INIT_VOICE = "msg_start_init_voice";
    /**
     * dds语音唤醒操作
     */
    String INIT_VOICE_START_WAKE = "init_voice_start_wake";
    String INIT_VOICE_CANCEL_WAKE = "init_voice_cancel_wake";

    String INIT_TYPE_SIGNAL_PROGRESS = "init_signal_progress";
    String INIT_TYPE_ROS_PROGRESS = "init_ros_progress";
    String INIT_TYPE_VOICE_PROGRESS = "init_voice_progress";
    String CANT_GET_BATTERY = "cant_get_battery";
    String MSG_RETRY_INIT_SIGNAL = "msg_retry_init_signal";

    /**
     * 控制任务信号类型
     */
    String CONTROL_TYPE_HEADER_ACTUATOR = "control_header_actuator";
    String MSG_INSTRUCTION_STATUS_FINISHED = "instructino_status_finished";
    String MSG_TASK_STATUS_FINISHED = "instruction_task_status_finished";
    String MSG_UPDATE_INSTARUCTION_STATUS = "update_instruction";
    String ROS_UPDATE_STATUS = "ros_update";

    String UPDATE_ROBOT_STATUS_TO_SERVER = "update_status_to_server";
    String MSG_TASK_END = "task_end";
    String MSG_STOP_MOVE = "stop_move";
    String MSG_CHANGE_POWER_LOCK = "power_lock";
    String MSG_ROS_MOVE_STATUS = "ros_move_status";
    String MSG_LIGHT_OFF = "msg_light_off";
    String MSG_LIGHT_ON = "msg_light_on";
    String MSG_SET_LIGHT_SUCCESS = "msg_set_light_success";

    /**
     * 视频消息
     */
    String MSG_ISLOCK_CHANGE = "msg_change_lock";
    String MSG_CHANGE_FLOATING_BALL = "msg_change_floating_ball";

    /**
     * TTS
     */
    String MSG_TTS = "msg_tts";
    String MSG_TTS_CANCEL = "msg_tts_cancel";
    String MSG_TTS_TASK_END = "msg_tts_task_end";
    String MSG_TTS_SHOW_DIALOG = "msg_tts_show_dialog";
    String MSG_SPEAK_TEXT = "msg_speak_text";


    /**
     * ros msg
     */
    String MSG_ROS_CANT_CONNECT = "msg_cant_connect";
    String MSG_ROS_NEXT_STEP = "msg_ros_nextStep";
    String MSG_UPDATE_HARDWARE = "msg_check_update_hardware";
    String MSG_GETROS_VERSION = "msg_get_ros_version";
    String MSG_UPDATE_BATTERY_INFO = "msg_update_battery_info";
    /**
     * ros初始化连接成功
     */
    String INIT_TYPE_ROS_SUCCESS = "init_type_ros_success";
    /**
     * dds对话Observer注册
     */
    String DDS_OBSERVER_REGISTERED = "dds_Observer_registered";
    /**
     * dds对话Observer解绑
     */
    String DDS_OBSERVER_UNTIE = "dds_Observer_untie";
    /**
     * dds语音播报取消
     */
    String DDS_VOICE_TEXT_CANCEL = "dds_voice_text_cancel";
    /**
     * 关闭dds唤醒
     */
    String CLOSE_DDS_WAKE_TAG = "close_dds_wake_tag";
    /**
     * 打开dds唤醒
     */
    String START_DDS_WAKE_TAG = "start_dds_wake_tag";
    /**
     * 控制端有远程任务加入
     */
    String REMOTE_TASK_JOIN_SUCCESS = "remote_task_join_success";
    /**
     * 院内介绍工作流开启标志(中英文版)
     */
    String START_INTRODUCTION_WORK_FLOW_TAG = "start_introduction_work_flow_tag";

    /**
     * 自助宣教页面,选中列表条目宣教任务编号
     */
    String CHECK_OPERATION_POSITION = "check_operation_position";
    /**
     * 唤醒词开启对话
     */
    String WAKE_WORD_DIALOG = "wake_word_dialog";
    /**
     * 机器人移动中终止
     */
    String ROBOT_STOP_MOVE = "robot_stop_move";
    /**
     * 特定工作流自动回充信息(需要服务端配置)
     */
    String RECHARGE_SPECIFIC_WORKFLOW = "recharge_specific_workflow";
    /**
     * 地图信息本地缓存
     */
    String MAP_INFO_CASH = "map_info_cash";
    /**
     * 自动回充任务信息
     */
    String RECHARGE_OPERATION_INFO = "recharge_operation_info";
    /**
     * 取消播放任务  视频  txt pdf
     */
    String CANCEL_PLAY_TASK = "cancel_play_task";

    /**
     * 机器人急停按钮是否已按下状态
     */
    String CLICK_EMERGENCY_TAG = "click_emergency_tag";
    /**
     * ros连接断开/错误
     */
    String ROS_CONNECTION_CHECK_FAILURE = "ros.connection.check_failure";
    /**
     * 更新页面机器人模式ui
     */
    String UPDATE_ROBOT_MODE_DISPLAY = "update_robot_mode_display";
    /**
     * 播放任务提示信息
     */
    String PLAY_TASK_PROMPT_INFO = "play_task_prompt_info";
    /**
     * DDS初始化成功
     */
    String DDS_INIT_COMPLETE = "dds.intent.action.init_complete";
    /**
     * DDS初始化失败
     */
    String DDS_INIT_FAILURE = "dds.intent.action.init_failure";
    /**
     * 引导工作流活动id
     */
    String GUIDE_WORK_FLOW_ACTION_ID = "guide.work.flow.action.id";
    /**
     * SP int默认存储编码
     */
    int DEFAULT_POSITION =888888888;
    /**
     * 音量键显示
     */
    String VOLUME_KEY_DISPLAY = "volume.key.display";
    /**
     * 音量键隐藏
     */
    String VOLUME_KEY_HIDDEN = "volume.key.hidden";
    /**
     * 亮度键显示
     */
    String BRIGHTNESS_KEY_DISPLAY = "brightness.key.display";
    /**
     * 亮度键隐藏
     */
    String BRIGHTNESS_KEY_HIDDEN = "brightness.key.hidden";
    /**
     * ROS初始化标志 true成功   false失败
     */
    String INIT_ROS_KEY_TAG = "init_ros_key_tag";

    /**
     * 单点导航任务发起标志
     */
    String NAVIGATION_START_TAG = "navigation_start_tag";

    /**
     * 是否继续进行下一个导航任务
     * 100 继续进行下一个导航任务
     * 200  取消当前任务
     */
    String WHETHER_CARRY_ON_BOOT = "whether_carry_on_boot";

    /**
     * 当前显示语言种类
     */
    String CURRENT_LANGUAGE = "current_language";

    /**
     * 自动回充工作流程启动状态
     */
    String AUTOMATIC_RECHARGE_TAG = "automatic_recharge_tag";

    /**
     * 进入常见问题汇总页面
     */
    String JUMP_COMMON_PROBLEM_PAGE = "jump_common_problem_page";
    /**
     * 机器人是否为控制模式
     */
    String IS_CONTROL_MODEL = "is_control_model";

    /**
     * 关闭选择导航页面
     */
    String CLOSE_SELECT_NAVIGATION_PAGE = "close_select_navigation_page";

    /**
     * 关闭首页点击启动院内工作流提示弹窗
     */
    String CLOSE_START_INTRODUCTION_DIALOG = "close_start_introduction_dialog";
    /**
     * 当前网络连接可用
     */
    String NETWORK_CONNECTION_CHECK_SUCCESS = "network.connection.check_success";
    /**
     * 当前网络连接不可用
     */
    String NETWORK_CONNECTION_CHECK_FAILURE = "network.connection.check_failure";

    /**
     * 安卓系统重启
     */
    String ANDROID_SYSTEM_REBOOT_TAG = "android.intent.action.REBOOT";

    /**
     * 安卓系统开机
     */
    String ANDROID_SYSTEM_BOOT_UP_TAG = "android.intent.action.BOOT_COMPLETED";

    /**
     * 程序首页是否显示
     */
    String MAIN_PAGE_WHETHER_SHOW = "main_page_whether_show";
    /**
     * 选择导航页面是否显示
     */
    String NAVIGATION_PAGE_WHETHER_SHOW = "navigation_page_whether_show";

    /**
     * 单点导航任务开启标志
     */
    String CURRENT_NAVIGATION_START_TAG = "current_navigation_start_tag";
    /**
     * 播放迎宾语开关
     */
    String BROADCAST_GREET_SWITCH_TAG = "broadcast_greet_switch_tag";

    /**
     * 当前空闲工作流数据
     */
    String CURRENT_FREE_OPERATION = "current_free_operation";
    /**
     * 自动回充工作流活动id
     */
    String AUTOMATIC_RECHARGE_ACTIVITY_ID = "automatic_recharge_activity_id";
    /**
     * 电量低于20%时弹窗提示
     */
    String PROMPT_ROBOT_RECHARGE = "prompt_robot_recharge";

    /**
     * 取消自动回充工作流
     */
    String MSG_CANCEL_RECHARGE = "cancel_recharge";

    /**
     * 空闲操作工作流选中编号
     */
    String FREE_OPERATION_SELECT_POSITION = "free_operation_select_position";

    /**
     * 首页是否有弹窗弹出(有弹窗时,不播报迎宾语,不执行空闲操作)
     */
    String MAIN_PAGE_DIALOG_SHOW_TAG = "main_page_dialog_show_tag";
    /**
     * 常见问题选择题目编号
     */
    String CHOOSE_COMMON_PROBLEM_POSITION = "choose_common_problem_position";
    /**
     * 关闭自动回充工作流加载提示框
     */
    String CLOSE_RECHARGING_TIP_DIALOG = "close_recharging_tip_dialog";

    /**
     * 启动首页说话动画
     */
    String START_SPEAK_ANIMATION_MSG = "start_speak_animation_msg";
    /**
     * 启动首页眨眼动画
     */
    String START_BLINK_ANIMATION_MSG = "start_blink_animation_msg";
    /**
     * 更新机器人信息到服务端
     */
    String UPDATE_ROBOT_STATE_TAG = "update_robot_state_tag";
    /**
     * 空闲操作工作流开始标志
     */
    String FREE_OPERATION_STATE_TAG = "free_operation_start_tag";

    /**
     * 启动计时器开始空闲操作
     */
    String START_FREE_OPERATION_MSG = "start_free_operation_msg";
}
