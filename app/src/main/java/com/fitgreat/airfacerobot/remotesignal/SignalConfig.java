package com.fitgreat.airfacerobot.remotesignal;

/**
 * SIGNAL 配置相关<p>
 *
 * @author zixuefei
 * @since 2020/3/25 0025 14:26
 */
public interface SignalConfig {
    /************************************SIGNAL 配置相关**************************************/
    //Signal一对多发送消息
    String SIGNAL_SEND_MESSAGE_ALL = "sendMessageAll";
    //Signal一对一发行消息
    String SIGNAL_SEND_MESSAGE = "sendMessage";
    //Signal监听消息
    String SIGNAL_RECEIVE_MESSAGE = "receiveMessage";
    //Signal打开代理通道
    String SIGNALR_HUB = "webRtcHub";

    String MSG_TYPE_JOIN = "JoinResult";
    String MSG_TYPE_EXITGROUP = "ExitGroup";
    String MSG_TYPE_GROUPLIST = "GroupList";
    String MSG_TYPE_INIT_VIDEO = "InitVideo";
    String MSG_TYPE_HANG_ON = "hangOn";
    String MSG_TYPE_OPERATION = "operation";
    String MSG_TYPE_OPERATION_MINI = "operationMini";
    String MSG_TYPE_HEARTBEAT = "heartbeat";

    String OPERATION_TYPE_8_MOVE = "MiniDirection";
    String OPERATION_TYPE_4_MOVE = "Direction";
    String OPERATION_TYPE_HAED_ANGLE = "Angle";
    String OPERATION_TYPE_LIFT_VERTICAL = "Vertical";
    String OPERATION_TYPE_AUTO_MOVE = "auto_moving";
    String OPERATION_TYPE_CHARGING = "auto_moving";

    String MSG_TYPE_TASK = "task";
    String MSG_STOP_TASK = "StopTask";
    String MSG_GET_ROBOT_STATUS = "GetRobotStatus";
    String MSG_FILE_CAN_NOT_PLAY = "FileCannotPlayed";
    String MSG_RECEIVER_TASK_SUCCESS = "taskSuccess";
    String MSG_CLOSE_ANDROID_SHARE = "closeAndroidShare";
    /************************************SIGNAL 配置相关**************************************/
}
