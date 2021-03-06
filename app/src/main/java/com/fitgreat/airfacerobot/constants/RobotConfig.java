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
     * dds初始化成功
     */
    String INIT_TYPE_DDS_SUCCESS = "init_type_dds_success";
    /**
     * dds重置需要重新初始化
     */
    String INIT_TYPE_DDS_RELEASE = "init_type_dds_release";
    /**
     * 弹窗提示30秒倒计时回充
     */
    String PROMPT_ROBOT_RECHARGE = "prompt_robot_recharge";
    /**
     * 控制端有远程任务加入
     */
    String REMOTE_TASK_JOIN_SUCCESS = "remote_task_join_success";
    /**
     * 引导工作流开启标志
     */
    String START_GUIDE_WORK_FLOW_TAG = "start_guide_work_flow_tag";
    /**
     * 通过语音终止当前任务
     */
    String VOICE_TERMINATION_TASK = "voice_termination_task";
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
     * 特定工作流引导工作流信息(需要服务端配置)
     */
    String GUIDE_SPECIFIC_WORKFLOW = "guide_specific_workflow";
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
     * SP存储默认编码
     */
    int DEFAULT_POSITION = 00;
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
     * DDS注册observer
     */
    String REGISTERED_DDS_OBSERVER = "registered_dds_observer";

    /**
     * 是否继续进行下一个导航任务
     * 100 继续进行下一个导航任务
     * 200  取消当前任务
     */
    String WHETHER_CARRY_ON_BOOT = "whether_carry_on_boot";
}
