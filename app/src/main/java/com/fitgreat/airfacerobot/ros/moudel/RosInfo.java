package com.fitgreat.airfacerobot.ros.moudel;

public class RosInfo {
    /**
     * 1、set_lift  升降
     *  mode ：Up(1)，Down(2)，Stop(0)
     * 2、set_power_lock  锁轴，不锁轴
     *  mode ：1锁轴  2不锁
     * 3、auto_moving 自主移动
     *   type：1移动 0 停止   ，eid：10  ， x ，y ，theta角度。
     * 4、auto_tracking  定位原点
     *  type ：1开始 终止0  ，eid：60
     * 5、exec_file  执行cmd命令
     *  path空值 ， cmd 命令  返回值  result：YES，NO
     * 6、trans_file   上传文件
     *  filename文件路径名称，size文件大小，data文字二进制数据      返回值  result：YES，NO
     * 7、update_map  更新地图
     *  ID  files地图二进制数组，image名称，origin_image原图名称，origin SNumber[x,y,z]，resolution分辨率
     * 8、switch_control  设置模式
     *  type 0 自动模式，1，扫图 2手动模式
     *  返回  去订阅Publisher和Subscriber
     * 9、airface_driver/get_tf_pose  获取位置
     *  发送空
     *  返回  F_X ，F_Y ，F_Z、 hardwardId自己的设备唯一id
     * 10、manual_base/run_valid，手动控制 带避障
     *  返回  result = 255 有障碍物
     * 11、query_version 获取硬件版本
     *  发送空
     *  返回  version
     */
    /**
     * airface_drive_msgs/setmode
     */
    public static final String SET_LIFT = "/set_lift";
    /**
     *airface_drive_msgs/setmode
     */
    public static final String SET_PWOER_LOCK_ = "/set_power_lock";
    /**
     *airface_drive_msgs/automove
     */
    public static final String AUTO_MOVING = "/auto_moving";
    /**
     *airface_drive_msgs/autopark
     */
    public static final String AUTO_TRACKING = "/auto_parking";
    /**
     *airface_drive_msgs/hasPerson
     */
    public static final String JUDGMENT_HAS_PERSON = "/checkSwitch";
    /**
     *airface_control_msgs/CommandFile
     */
    public static final String EXEC_FILE = "/exec_file";
    /**
     *airface_control_msgs/TransferFile
     */
    public static final String TRANS_FILE = "/trans_file";
    /**
     *airface_control_msgs/SetMap
     */
    public static final String UPDATE_MAP = "/update_map";
    /**
     *airface_control_msgs/ControlSwitch
     */
    public static final String SWITCH_CONTROL = "/switch_control";
    /**
     *airface_drive_msgs/getpose
     */
    public static final String GET_TF_POSE = "/airface_driver/get_tf_pose";
    /**
     *move_base_msgs/RunValidState
     */
    public static final String RUN_VALID = "/manual_base/run_valid";
    /**
     *airface_control_msgs/QueryVersion
     */
    public static final String QUERY_VERSION = "/query_version";

    public static final int MOVE_UP = 1;
    public static final int MOVE_DOWN = 2;
    public static final int MOVE_LEFT = 3;
    public static final int MOVE_RIGHT = 4;
    public static final int MOVE_STOP = 0;


}
