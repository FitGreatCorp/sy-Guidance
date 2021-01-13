package com.fitgreat.airfacerobot.ros;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotBrainService;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.model.ActionDdsEvent;
import com.fitgreat.airfacerobot.model.NavigationTip;
import com.fitgreat.airfacerobot.remotesignal.model.SignalDataEvent;
import com.fitgreat.airfacerobot.ros.moudel.RosInfo;
import com.fitgreat.archmvp.base.util.ExecutorManager;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.SpUtils;

import org.greenrobot.eventbus.EventBus;
import org.ros.address.InetAddressFactory;
import org.ros.exception.RemoteException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import airface_control_msgs.CommandFileRequest;
import airface_control_msgs.CommandFileResponse;
import airface_control_msgs.ControlSwitchRequest;
import airface_control_msgs.ControlSwitchResponse;
import airface_control_msgs.QueryVersionRequest;
import airface_control_msgs.QueryVersionResponse;
import airface_control_msgs.SetMapRequest;
import airface_control_msgs.SetMapResponse;
import airface_control_msgs.TransferFileRequest;
import airface_control_msgs.TransferFileResponse;
import airface_drive_msgs.AutoMoveInfo;
import airface_drive_msgs.automoveRequest;
import airface_drive_msgs.automoveResponse;
import airface_drive_msgs.autopark;
import airface_drive_msgs.autoparkRequest;
import airface_drive_msgs.autoparkResponse;
import airface_drive_msgs.getposeRequest;
import airface_drive_msgs.getposeResponse;
import airface_drive_msgs.setmodeRequest;
import airface_drive_msgs.setmodeResponse;
import geometry_msgs.Twist;
import geometry_msgs.Vector3;
import move_base_msgs.RunValidStateRequest;
import move_base_msgs.RunValidStateResponse;
import sensor_msgs.BatteryState;
import std_msgs.Byte;

import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.BROADCAST_GREET_SWITCH_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MAIN_PAGE_DIALOG_SHOW_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MAIN_PAGE_WHETHER_SHOW;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_ROS_MOVE_STATUS;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PLAY_TASK_PROMPT_INFO;

/**
 * ros工控制主机管理者<p>
 *
 * @author zixuefei
 * @since 2020/3/11 0011 10:14
 */
public class RosManager {
    private final String TAG = RosManager.class.getSimpleName();
    //    private URI masterUri = URI.create("http://masterhost:11311");
    private final URI rosMasterUri = URI.create("http://192.168.88.8:11311");
    private final String rosClientHostName = "android";
    public static final int ACTION_GETPOST = 0x11;
    public static final int ACTION_DELAY_MOVE_STOP = 0x12;
    public static final int LONG_SYNC = 20000;
    public static final int SHORT_SYNC = 3000;
    public static final int DELAY_STOP_TIME = 500;
    public static final int CANT_GET_BATTERY = 50001;
    public static final int MSG_REBOOT = 50002;
    public boolean firstMsg = false;
    public int mCurrentSync = LONG_SYNC;
    private NodeMainExecutor nodeMainExecutor;
    private ConnectedNode mConnectedNode = null;
    private Publisher<Byte> ctrlPublisher = null;
    private Publisher<Twist> velPublisher = null;
    private Subscriber<BatteryState> batteryStateSubscriber;
    //是否在墙内订阅
    private Subscriber<Byte> whetherInsideWallSubscriber;

    /**
     * long power_supply_status;//是否充电，
     * float percentage; //电量，
     * long power_supply_technology;//急停，
     * long power_supply_health;//手动移动，
     * float charge;  //温度，
     * BOOL present; //是否可以充电
     */
    private boolean initCtrollOk = false;
    private float mBattery = 0;
    private long mPowerStatus = 0;
    private long mPowerTechnology = 0;
    private float mCharge = 0;
    private boolean mPresent = true;
    private String mPowerHealth = "unlock";
    private boolean mConnect = false;

    /**
     * 升降
     */
    private ServiceClient<setmodeRequest, setmodeResponse> clientLift = null;
    /**
     * 锁轴，不锁轴
     */
    private ServiceClient<setmodeRequest, setmodeResponse> clientPowerLock = null;
    /**
     * 开关消毒灯
     */
    private ServiceClient<setmodeRequest, setmodeResponse> clientSetLight = null;
    /**
     * 自主移动
     */
    private ServiceClient<automoveRequest, automoveResponse> clientAutoMove = null;
    /**
     * 定位原点
     */
    private ServiceClient<autoparkRequest, autoparkResponse> clientAutoTracking = null;
    /**
     * 执行cmd命令
     */
    private ServiceClient<CommandFileRequest, CommandFileResponse> clientExecFile = null;
    /**
     * 上传文件
     */
    private ServiceClient<TransferFileRequest, TransferFileResponse> clientTransFile = null;
    /**
     * 更新地图
     */
    private ServiceClient<SetMapRequest, SetMapResponse> clientUpdateMap = null;
    /**
     * 设置模式 自动手动
     */
    private ServiceClient<ControlSwitchRequest, ControlSwitchResponse> clientSwitchControl = null;
    /**
     * 获取位置 返回  F_X ，F_Y ，F_Z、 hardwardId自己的设备唯一id
     */
    private ServiceClient<getposeRequest, getposeResponse> clientGetPose = null;
    /**
     * 手动控制 带避障 返回  result = 255 有障碍物
     */
    private ServiceClient<RunValidStateRequest, RunValidStateResponse> clientRunValid = null;
    /**
     * 获取硬件版本
     */
    private ServiceClient<QueryVersionRequest, QueryVersionResponse> clientVersion = null;

    /**
     * 获取附近有没有人
     */
//    private ServiceClient<autoparkRequest, autoparkResponse> clientHasPerson = null;
    private ServiceClient<autoparkRequest, autoparkResponse> clientHasPerson = null;

    private Twist twist = null;
    private Vector3 angular = null;
    private Vector3 linear = null;

    private Twist stopTwist = null;
    private Vector3 stopAngular = null;
    private Vector3 stopLinear = null;
    private Timer rosManagerInitTimer = null;
    private TimerTask rosManagerInitTimerTask = null;
    private boolean mainPageShowTag;
    private boolean mainPageDialogShowTag;
    //迎宾语播放次数限制一直有人时默认播放一次
    private int playTipTime = 0;

    private Handler myHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ACTION_DELAY_MOVE_STOP:
//                    sendMoveStop();
                    break;
                case ACTION_GETPOST:
                    ExecutorManager.getInstance().executeTask(() -> {
//                        sendGetPose();
                    });
                    myHandler.removeMessages(ACTION_GETPOST);
                    myHandler.sendEmptyMessageDelayed(ACTION_GETPOST, mCurrentSync);
                    break;
                default:
                    break;
            }
        }
    };
    private boolean broadcastGreetSwitchTag;
    private String string_hello;

    public boolean isPowerStatus() {
        return mPowerStatus == 1;
    }

    /**
     * 判断附近是否有人接口
     *
     * @param type
     */
    public void judgmentHasPerson(byte type) {
        try {
            if (null == clientHasPerson) {
                try {
                    clientHasPerson = mConnectedNode.newServiceClient(RosInfo.JUDGMENT_HAS_PERSON, autopark._TYPE);
                } catch (ServiceNotFoundException e) {
                    LogUtils.e(TAG, "ServiceNotFoundException: " + e.getMessage());
                    e.printStackTrace();
                }
                if (null == clientHasPerson) {
                    return;
                }
            }
            autoparkRequest msg = clientHasPerson.newMessage();
            msg.setEid("10");
            msg.setType(type);
            clientHasPerson.call(msg, new ServiceResponseListener<autoparkResponse>() {
                @Override
                public void onSuccess(autoparkResponse setmodeResponse) {    //  getResult  为 false  机器附近有物体  true   没有物体
                    LogUtils.d(TAG, "judgmentHasPerson getMsg   " + setmodeResponse.getMsg() + " getResult      " + setmodeResponse.getResult());
                    if (!setmodeResponse.getResult()) {
                        playTipTime = 0;
                        playTipTime++;
                        //是否播放迎宾语
                        broadcastGreetSwitchTag = SpUtils.getBoolean(MyApp.getContext(), BROADCAST_GREET_SWITCH_TAG, false);
                        //程序首页是否显示
                        mainPageShowTag = SpUtils.getBoolean(MyApp.getContext(), MAIN_PAGE_WHETHER_SHOW, false);
                        //程序首页是否有弹窗弹出
                        mainPageDialogShowTag = SpUtils.getBoolean(MyApp.getContext(), MAIN_PAGE_DIALOG_SHOW_TAG, false);
                        //迎宾语内容
                        string_hello = SpUtils.getString(MyApp.getContext(), "hello_string", "Hi");
                        LogUtils.d(TAG, "机器人附近有障碍物, " + " string_hello, " + string_hello+ " mainPageShowTag, " + mainPageShowTag+ " playTipTime, " + playTipTime+ " mainPageDialogShowTag, " + mainPageDialogShowTag);
                        if (broadcastGreetSwitchTag && (!TextUtils.isEmpty(string_hello)) && mainPageShowTag && playTipTime == 1&&!mainPageDialogShowTag) { //播放迎宾语开关打开
                            playShowText(string_hello);
                        }
                    } else {
                        playTipTime = 0;
                        LogUtils.d(TAG, "机器人附近没有障碍物");
                    }
                }

                @Override
                public void onFailure(RemoteException e) {
                    LogUtils.d(TAG, "judgmentHasPerson Failure");
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "judgmentHasPerson error:" + e.getMessage());
        }
    }

    /**
     * 播放并首页展示对应文案
     */
    public static void playShowText(String content) {
        EventBus.getDefault().post(new NavigationTip(content));
        EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, content));
    }

    public synchronized void initNode() {
        nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
        LogUtils.d(TAG, "-----------------init ros node----------------");
        mConnect = false;
        nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(rosClientHostName, rosMasterUri);
        nodeConfiguration.setMasterUri(rosMasterUri);
        nodeMainExecutor.execute(new NodeMain() {
            @Override
            public void onStart(ConnectedNode connectedNode) {
                LogUtils.d(TAG, "--------------NodeMain onStart---------");
                if (null == connectedNode) {
                    LogUtils.e(TAG, "--------connectedNode is null--------");
                    return;
                }
                LogUtils.d(TAG, "---connectedNode:" + connectedNode.getName() + " current time:" + connectedNode.getCurrentTime());
                mConnectedNode = connectedNode;
                firstMsg = false;
                startCreatePublish();
                startSubscriberRosMsg();
                mConnect = true;
                try {
                    LogUtils.d(TAG, "------ros jar address:" + InetAddressFactory.newFromHostString("masterhost"));
                    LogUtils.d(TAG, "------android address:" + InetAddress.getByName(rosClientHostName) + " android masterhost:"
                            + InetAddress.getByName("masterhost"));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                //定时检测机器人旁边是否有人
                rosManagerInitTimer = new Timer();
                rosManagerInitTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        judgmentHasPerson((byte) 3);
                    }
                };
                rosManagerInitTimer.schedule(rosManagerInitTimerTask, 0, 5 * 1000);
            }

            @Override
            public void onShutdown(Node node) {
                LogUtils.d(TAG, "NodeMain onShutdown");
                mConnectedNode = null;
                mConnect = false;
            }

            @Override
            public void onShutdownComplete(Node node) {
                LogUtils.d(TAG, "NodeMain onShutdownComplete");
            }

            @Override
            public void onError(Node node, Throwable throwable) {
                LogUtils.e(TAG, "-----NodeMain onError:" + throwable.getMessage());
                try {
                    mConnectedNode.shutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mConnectedNode = null;
                mConnect = false;
            }

            @Override
            public GraphName getDefaultNodeName() {
                LogUtils.d(TAG, "NodeMain getDefaultNodeName");
                return GraphName.of("androidNode" + System.currentTimeMillis());
            }
        }, nodeConfiguration);
    }

    /**
     * 所有需要订阅的Ros信息 /move_state  /ip_battery_state
     */
    public void startSubscriberRosMsg() {
        try {
            if (checkConnectedNodeNull()) {
                LogUtils.e(TAG, "-------connect node is null or not init--------");
                return;
            }
            //create and listen /move_state

            Subscriber<AutoMoveInfo> moveStateSubscriber = mConnectedNode.newSubscriber("/move_state", AutoMoveInfo._TYPE);
            moveStateSubscriber.addMessageListener(moveStateMessageListener);
            LogUtils.d(TAG, "startSubscriberListen");
            //机器人是否在墙体内   footprint_state_       /move_base/rotate_recovery/footprint_state_
            whetherInsideWallSubscriber = mConnectedNode.newSubscriber("/move_base/rotate_recovery/footprint_state_", Byte._TYPE);
            whetherInsideWallSubscriber.addMessageListener(whetherInsideWallMessageListener);
        } catch (Exception e) {
            LogUtils.e(TAG, "startSubscriberListen error:" + e.getMessage());
        }
    }

    private int playTime = 0;
    /**
     * 是否在墙内消息返回
     */
    private MessageListener<Byte> whetherInsideWallMessageListener = new MessageListener<Byte>() {
        @Override
        public void onNewMessage(Byte aByte) {
            if ((int) aByte.getData() == 1) { //机器人在墙内 需要帮忙移动出来
                LogUtils.d(DEFAULT_LOG_TAG, "机器人在墙内 : " + aByte.getData() + "播放次数 , " + playTime);
                playTime++;
                if (playTime == 1) {
                    EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, MvpBaseActivity.getActivityContext().getString(R.string.ask_for_help_text)));
                } else if (playTime == 2) {
                    playTime = 0;
                }
            } else {
                playTime = 0;
            }
        }
    };

    /**
     * 是否急停状态
     *
     * @return
     */
    public boolean isPowerTechnology() {
        return mPowerTechnology != 0;
    }


    /**
     * 拖动、控制模式
     *
     * @return
     */
    public String getPowerHealth() {
        return mPowerHealth;
    }

    public boolean isConnect() {
        return !checkConnectedNodeNull() && mConnect;
    }

    /**
     * 获取ros版本信息
     */
    public void getRosVersion() {
        if (!isConnect()) {
            LogUtils.e(TAG, "-------connect node is null or not init--------");
            return;
        }
        LogUtils.d(TAG, "clientVersion = " + clientVersion + ", mConnectedNode = " + mConnectedNode);
//        if (clientVersion == null) {
        if (clientVersion != null) {
            clientVersion = null;
        }
//        try {
//            clientVersion = mConnectedNode.newServiceClient(RosInfo.QUERY_VERSION, QueryVersion._TYPE);
//            QueryVersionRequest msg = clientVersion.newMessage();
//            clientVersion.call(msg, new ServiceResponseListener<QueryVersionResponse>() {
//                @Override
//                public void onSuccess(QueryVersionResponse queryVersionResponse) {
//                    LogUtils.d(TAG, "getRosVersion success : " + queryVersionResponse.getVersion());
//                    RobotInfoUtils.setHardwareVersion(queryVersionResponse.getVersion());
//                }
//
//                @Override
//                public void onFailure(RemoteException e) {
//                    LogUtils.e(TAG, "getRosVersion failed:" + e.getMessage());
//                }
//            });
//        } catch (ServiceNotFoundException e) {
//            e.printStackTrace();
//        }
//        }
    }

    /**
     * 向ros发送文件
     */
//    public void transFile(String fileName, String filePath, String command,String stepId,String step) {
//        try {
//            if (!isConnect()) {
//                LogUtils.e(TAG, "-------connect node is null or not init--------");
//                return;
//            }
//            if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(filePath)) {
//                LogUtils.e(TAG, "File is empty");
//                return;
//            }
//            if (clientTransFile == null) {
//                clientTransFile = mConnectedNode.newServiceClient(RosInfo.TRANS_FILE, TransferFile._TYPE);
//            }
//
//            byte[] updateData = FileUtils.readLocalFile(filePath);
//
//            if (updateData == null) {
//                DaemonEvent daemonEvent = new DaemonEvent(RobotConfig.TYPE_SHOW_VERSION_STATUS,
//                        "rosUpdateFailed", "硬件升级失败，ros本地文件读取失败");
//                EventBus.getDefault().post(daemonEvent);
//                return;
//            }
//
//            String data = Base64.byteArrayToBase64(updateData);
//
//            TransferFileRequest msg = clientTransFile.newMessage();
//            msg.setFilename("/home/radmin/HardwareVersion/" + fileName);
//            msg.setSize(data.length());
//            msg.setData(data);
//            LogUtils.d(TAG,"fileName ========= "+fileName);
//            clientTransFile.call(msg, new ServiceResponseListener<TransferFileResponse>() {
//
//                @Override
//                public void onSuccess(TransferFileResponse transferFileResponse) {
//                    LogUtils.d(TAG, "transFile success : " + transferFileResponse.getResult() + " filepath:" + transferFileResponse.getPath());
//                    if (transferFileResponse.getResult()) {
//                        execFile(command,stepId,step);
//                    } else {
//                        DaemonEvent daemonEvent = new DaemonEvent(RobotConfig.TYPE_SHOW_VERSION_STATUS,
//                                "rosUpdateFailed", "硬件升级失败，ros传输文件失败：" + transferFileResponse.getPath());
//                        EventBus.getDefault().post(daemonEvent);
//                    }
//                }
//
//                @Override
//                public void onFailure(RemoteException e) {
//                    LogUtils.e(TAG, "transFile failed:" + e.getMessage());
//                    DaemonEvent daemonEvent = new DaemonEvent(RobotConfig.TYPE_SHOW_VERSION_STATUS,
//                            "rosUpdateFailed", "硬件升级失败，ros传输文件失败:" + e.getMessage());
//                    EventBus.getDefault().post(daemonEvent);
//                }
//            });
//        } catch (ServiceNotFoundException | IOException e) {
//            e.printStackTrace();
//        }
//    }


    /**
     * 执行ros升级命令
     */
//    public void execFile(String command,String stepId,String step) {
//        try {
//            if (!isConnect()) {
//                LogUtils.e(TAG, "-------connect node is null or not init--------");
//                return;
//            }
//            if (clientExecFile == null) {
//                clientExecFile = mConnectedNode.newServiceClient(RosInfo.EXEC_FILE, CommandFile._TYPE);
//            }
//            LogUtils.d(TAG, "command:" + command);
//            CommandFileRequest msg = clientExecFile.newMessage();
//            msg.setCmd(command);
//            clientExecFile.call(msg, new ServiceResponseListener<CommandFileResponse>() {
//                @Override
//                public void onSuccess(CommandFileResponse commandFileResponse) {
//                    LogUtils.d(TAG, "execFile success : " + commandFileResponse.getResult() + " MSG:" + commandFileResponse.getMsg());
//                    if (commandFileResponse.getResult()) {
//                        DaemonEvent daemonEvent = new DaemonEvent(RobotConfig.TYPE_SHOW_VERSION_STATUS,
//                                "rosUpdateSuccess", commandFileResponse.getMsg());
//                        daemonEvent.setStepId(stepId);
//                        daemonEvent.setStep(step);
//                        EventBus.getDefault().post(daemonEvent);
//                    } else {
//                        DaemonEvent daemonEvent = new DaemonEvent(RobotConfig.TYPE_SHOW_VERSION_STATUS,
//                                "rosUpdateFailed", "硬件升级失败，ros执行命令失败:" + commandFileResponse.getMsg());
//                        daemonEvent.setStepId(stepId);
//                        EventBus.getDefault().post(daemonEvent);
//                    }
//                }
//
//                @Override
//                public void onFailure(RemoteException e) {
//                    LogUtils.e(TAG, "execFile failed:" + e.getMessage());
//                    DaemonEvent daemonEvent = new DaemonEvent(RobotConfig.TYPE_SHOW_VERSION_STATUS,
//                            "rosUpdateFailed", "硬件升级失败，ros执行命令失败:" + e.getMessage());
//                    EventBus.getDefault().post(daemonEvent);
//                }
//            });
//        } catch (ServiceNotFoundException e) {
//            e.printStackTrace();
//        }
//    }


    /**
     * 开关消毒灯
     *
     * @param mode
     */
//    public void setLight(byte mode) {
//        try {
//            if (!isConnect()) {
//                LogUtils.e(TAG, "-------connect node is null or not init--------");
//                return;
//            }
//            if (null == clientSetLight) {
//                LogUtils.e(TAG, "clientSetLight is null");
//
//                try {
//                    clientSetLight = mConnectedNode.newServiceClient(RosInfo.SET_LIGHT, setmode._TYPE);
//                } catch (ServiceNotFoundException e) {
//                    e.printStackTrace();
//                    LogUtils.e(TAG,"clientSetLight error : "+ e.toString());
//                }
//
//                if (null == clientSetLight) {
//                    return;
//                }
//            }
//            setmodeRequest msg = clientSetLight.newMessage();
//            msg.setMode(mode);
//            clientSetLight.call(msg, new ServiceResponseListener<setmodeResponse>() {
//                @Override
//                public void onSuccess(setmodeResponse setmodeResponse) {
//                    LogUtils.d(TAG, "setLight success  result : "+setmodeResponse.getResult() + " ,status : "+setmodeResponse.getStatus());
//                    if(setmodeResponse.getResult()){
//                        SignalDataEvent signalDataEvent = new SignalDataEvent();
//                        signalDataEvent.setType(MSG_SET_LIGHT_SUCCESS);
//                        signalDataEvent.setLight_status(setmodeResponse.getStatus());
//                        EventBus.getDefault().post(signalDataEvent);
//                    }
//                }
//
//                @Override
//                public void onFailure(org.ros.exception.RemoteException e) {
//                    LogUtils.d(TAG, "setLight Failure");
//                }
//            });
//        } catch (Exception e) {
//            LogUtils.e(TAG, "setLight error:" + e.getMessage());
//        }
//    }


    /**
     * 移动中3S更新一次,停止状态20S更新一次
     */
//    public synchronized void sendGetPose() {
//        LogUtils.d(TAG, "sendGetPose");
//        try {
//            if (!isConnect()) {
//                LogUtils.e(TAG, "-------connect node is null or not init--------");
//                return;
//            }
//            if (null == clientGetPose) {
//                LogUtils.e(TAG, "sendGetPose is null,mConnectedNode is null?" + (mConnectedNode == null));
//                try {
//                    clientGetPose = mConnectedNode.newServiceClient(RosInfo.GET_TF_POSE, getpose._TYPE);
//                } catch (ServiceNotFoundException e) {
//                    e.printStackTrace();
//                }
//                if (null == clientGetPose) {
//                    return;
//                }
//            }
//            getposeRequest msg = clientGetPose.newMessage();
//            clientGetPose.call(msg, new ServiceResponseListener<getposeResponse>() {
//                @Override
//                public void onSuccess(getposeResponse getposeResponse) {
//                    LogUtils.d(TAG, "sendGetPose success");
//
//                    double x = getposeResponse.getX();
//                    double y = getposeResponse.getY();
//                    double z = getposeResponse.getYaw();
//
//                    SignalDataEvent positionEvent = new SignalDataEvent(ROS_MSG_ROBOT_POSITION, "");
//                    positionEvent.setPosition_X(x);
//                    positionEvent.setPosition_Y(y);
//                    positionEvent.setPosition_Z(z);
//                    EventBus.getDefault().post(positionEvent);
//                }
//
//                @Override
//                public void onFailure(org.ros.exception.RemoteException e) {
//                    LogUtils.d(TAG, "sendGetPose fail");
//                }
//            });
//        } catch (Exception e) {
//            LogUtils.e(TAG, "mConnectedNode is " + mConnectedNode + ",sendGetPose error:" + e.getMessage());
//            e.printStackTrace();
//        }
//    }

    /**
     * @param type switch_control  设置模式
     *             type 0 自动模式，1，扫图 2手动模式
     */
//    public void sendSwithCtrollService(byte type) {
//        LogUtils.d(TAG, "sendSwithCtrollService");
//        if (!isConnect()) {
//            LogUtils.e(TAG, "-------connect node is null or not init--------");
//            return;
//        }
//        if (initCtrollOk) {
//            return;
//        }
//        if (null == clientSwitchControl) {
//            try {
//                clientSwitchControl = mConnectedNode.newServiceClient(RosInfo.SWITCH_CONTROL, ControlSwitch._TYPE);
//            } catch (ServiceNotFoundException e) {
//                e.printStackTrace();
//            }
//            if (null == clientSwitchControl) {
//                return;
//            }
//        }
//        ControlSwitchRequest controlSwitchRequest = clientSwitchControl.newMessage();
//        controlSwitchRequest.setType(type);
//        clientSwitchControl.call(controlSwitchRequest, new ServiceResponseListener<ControlSwitchResponse>() {
//            @Override
//            public void onSuccess(ControlSwitchResponse controlSwitchResponse) {
//                LogUtils.d(TAG, "sendSwithCtrollService success");
//                initCtrollOk = true;
//            }
//
//            @Override
//            public void onFailure(org.ros.exception.RemoteException e) {
//                LogUtils.d(TAG, "sendSwithCtrollService Failure");
//            }
//        });
//    }


//    public void sendToAutoMovingService(byte type, double x, double y, double z) {
//        try {
//            if (!isConnect()) {
//                LogUtils.e(TAG, "-------connect node is null or not init--------");
//                return;
//            }
//            if (null == clientAutoMove) {
//                try {
//                    LogUtils.d(TAG, "mConnectedNode = " + mConnectedNode);
//                    clientAutoMove = mConnectedNode.newServiceClient(RosInfo.AUTO_MOVING, automove._TYPE);
//                } catch (ServiceNotFoundException e) {
//                    e.printStackTrace();
//                }
//                if (null == clientAutoMove) {
//                    return;
//                }
//            }
//
//            automoveRequest msg = clientAutoMove.newMessage();
//            msg.setEid("10");
//            msg.setType(type);
//            LogUtils.d(TAG, "type= " + type);
//            if (type == 0) {
//                msg.setX(0);
//                msg.setY(0);
//                msg.setTheta(0);
//            } else {
//                LogUtils.d(TAG, "x=" + x + ",y=" + y + ",z=" + z);
//                msg.setX(x);
//                msg.setY(y);
//                msg.setTheta(z);
//            }
//            LogUtils.d(TAG, "clientAutoMove = " + clientAutoMove);
//            clientAutoMove.call(msg, new ServiceResponseListener<automoveResponse>() {
//                @Override
//                public void onSuccess(automoveResponse setmodeResponse) {
//                    LogUtils.d(TAG, "sendToAutoMovingService success");
//                }
//
//                @Override
//                public void onFailure(org.ros.exception.RemoteException e) {
//                    LogUtils.d(TAG, "sendToAutoMovingService Failure");
//                }
//
//            });
//        } catch (Exception e) {
//            LogUtils.e(TAG, "sendToAutoMovingService error:" + e.getMessage());
//        }
//    }

//    public void sendLiftState(byte mode) {
//        try {
//            if (!isConnect()) {
//                LogUtils.e(TAG, "-------connect node is null or not init--------");
//                return;
//            }
//            if (null == clientLift) {
//                try {
//                    clientLift = mConnectedNode.newServiceClient(RosInfo.SET_LIFT, setmode._TYPE);
//                } catch (ServiceNotFoundException e) {
//                    e.printStackTrace();
//                }
//                if (null == clientLift) {
//                    return;
//                }
//            }
//            setmodeRequest msg = clientLift.newMessage();
//            msg.setMode(mode);
//            LogUtils.d(TAG, "clientLift = " + clientLift);
//            clientLift.call(msg, new ServiceResponseListener<setmodeResponse>() {
//                @Override
//                public void onSuccess(setmodeResponse setmodeResponse) {
//                    LogUtils.d(TAG, "sendLiftState success");
//                }
//
//                @Override
//                public void onFailure(org.ros.exception.RemoteException e) {
//                    LogUtils.d(TAG, "sendLiftState Failure");
//                }
//            });
//        } catch (Exception e) {
//            LogUtils.e(TAG, "sendLiftState error:" + e.getMessage());
//        }
//    }

//    public void sendMoveStop() {
//        try {
//            if (!isConnect()) {
//                LogUtils.e(TAG, "-------connect node is null or not init--------");
//                return;
//            }
//            if (velPublisher == null) {
//                velPublisher = mConnectedNode.newPublisher("/cmd_vel", Twist._TYPE);
//            }
//            if (stopTwist == null || stopAngular == null || stopLinear == null) {
//                stopTwist = velPublisher.newMessage();
//                stopAngular = stopTwist.getAngular();
//                stopLinear = stopTwist.getLinear();
//                stopLinear.setX(0);
//                stopAngular.setZ(0);
//                stopTwist.setAngular(stopAngular);
//                stopTwist.setLinear(stopLinear);
//            }
//            velPublisher.publish(stopTwist);
//        } catch (Exception e) {
//            LogUtils.e(TAG, "sendAutoMoveStop error:" + e.getMessage());
//        }
//    }

    /**
     * @param orientation
     */
//    public void sendMovePublisherMessage(int orientation) {
//        try {
//            if (!isConnect()) {
//                LogUtils.e(TAG, "-------connect node is null or not init--------");
//                return;
//            }
//            myHandler.removeMessages(ACTION_DELAY_MOVE_STOP);
//            if (velPublisher == null) {
//                velPublisher = mConnectedNode.newPublisher("/cmd_vel", Twist._TYPE);
//            }
//            if (twist == null || angular == null || linear == null) {
//                twist = velPublisher.newMessage();
//                angular = twist.getAngular();
//                linear = twist.getLinear();
//            } else {
//                linear.setX(0);
//                angular.setZ(0);
//            }
//            switch (orientation) {
//                case RosInfo.MOVE_UP:
//                    linear.setX(0.4);
//                    break;
//                case RosInfo.MOVE_DOWN:
//                    linear.setX(-0.1);
//                    break;
//                case RosInfo.MOVE_LEFT:
//                    angular.setZ(0.35);
//                    break;
//                case RosInfo.MOVE_RIGHT:
//                    angular.setZ(-0.35);
//                    break;
//                case RosInfo.MOVE_STOP:
//                    break;
//            }
//            LogUtils.d(TAG, "publish " + orientation);
//            twist.setAngular(angular);
//            twist.setLinear(linear);
//            velPublisher.publish(twist);
//            myHandler.sendEmptyMessageDelayed(ACTION_DELAY_MOVE_STOP, DELAY_STOP_TIME);
//        } catch (Exception e) {
//            LogUtils.e(TAG, "sendMovePublisherMessage error:" + e.getMessage());
//        }
//    }

    /**
     * @param x
     * @param y
     */
//    public void sendMovePublisherMessage(float x, float y) {
//        LogUtils.d(TAG, "sendMovePublisherMessage !!!!  x = " + x + " , z = " + y);
//        try {
//            if (!isConnect()) {
//                LogUtils.e(TAG, "-------connect node is null or not init--------");
//                return;
//            }
//            myHandler.removeMessages(ACTION_DELAY_MOVE_STOP);
//            if (velPublisher == null) {
//                velPublisher = mConnectedNode.newPublisher("/cmd_vel", Twist._TYPE);
//            }
//            LogUtils.d(TAG, "twist = " + twist + " ,angular = " + angular + " , angular = " + angular);
//            if (twist == null || angular == null || linear == null) {
//                twist = velPublisher.newMessage();
//                angular = twist.getAngular();
//                linear = twist.getLinear();
//            } else {
//                linear.setX(0);
//                angular.setZ(0);
//            }
//            linear.setX(x);
//            angular.setZ(y);
//            twist.setAngular(angular);
//            twist.setLinear(linear);
//            velPublisher.publish(twist);
//            myHandler.sendEmptyMessageDelayed(ACTION_DELAY_MOVE_STOP, DELAY_STOP_TIME);
//        } catch (Exception e) {
//            LogUtils.e(TAG, "sendMovePublisherMessage error:" + e.getMessage());
//        }
//    }

    /**
     * 开始发布所有消息点
     * /cmd_ctrl
     * /cmd_vel
     */
    public void startCreatePublish() {
        if (checkConnectedNodeNull()) {
            LogUtils.e(TAG, "-------connect node is null or not init--------");
            return;
        }
        ctrlPublisher = mConnectedNode.newPublisher("/cmd_ctrl", Byte._TYPE);
        velPublisher = mConnectedNode.newPublisher("/cmd_vel", Twist._TYPE);
    }

    private boolean checkConnectedNodeNull() {
        return (mConnectedNode == null) || nodeMainExecutor == null;
    }

    /**
     * 销毁ros连接对象
     */
    public synchronized void destroy() {
        LogUtils.d(TAG, "-----------------destroy ros node----------------");
        if (mConnectedNode != null) {
            mConnectedNode.shutdown();
            mConnectedNode = null;
        }
        if (null != nodeMainExecutor) {
            nodeMainExecutor.shutdown();
            nodeMainExecutor = null;
        }
        if (rosManagerInitTimer != null) {
            rosManagerInitTimer.cancel();
            rosManagerInitTimer = null;
        }
        mConnect = false;
        myHandler.removeMessages(0);
        EventBus.getDefault().unregister(this);
    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onMsg(RosMsgEvent rosMsgEvent) {
//        switch (rosMsgEvent.type) {
//            case MSG_ROS_CANT_CONNECT:
//                myHandler.sendEmptyMessageDelayed(MSG_REBOOT, 10000);
//                break;
//        }
//    }

    public String getBattery() {
        return String.valueOf(Math.round(mBattery));
    }

    /**
     * /movestate 发布 监听
     * 返回  eid =10 自主移动
     * code
     * 开始  31
     * 成功  999
     * 失败  30
     * 取消  80
     * <p>
     * eid =60 定位结果
     * code 开始 60031
     * 成功 60999
     * 失败 60030
     * 取消 60080
     */
    private MessageListener<AutoMoveInfo> moveStateMessageListener = new MessageListener<AutoMoveInfo>() {
        @Override
        public void onNewMessage(AutoMoveInfo autoMoveInfo) {
            try {
                switch (autoMoveInfo.getEid()) {
                    case "10":
                        LogUtils.d(TAG, "code =" + autoMoveInfo.getCode());
                        int codeSync = autoMoveInfo.getCode().equals("31") ? SHORT_SYNC : LONG_SYNC;
                        if (mCurrentSync != codeSync) {
                            mCurrentSync = codeSync;
                            LogUtils.d(TAG, "mCurrentSync change:" + mCurrentSync);
                        }
                        SignalDataEvent rosMoveStatue = new SignalDataEvent();
                        rosMoveStatue.setType(MSG_ROS_MOVE_STATUS);
                        rosMoveStatue.setRosMoveStatusCode(autoMoveInfo.getCode());
                        EventBus.getDefault().post(rosMoveStatue);
                        break;
                    case "60":
                        break;
                    case "60031":
                        LogUtils.d(TAG, "eid = 60031");
                        break;
                    case "60999":
                        LogUtils.d(TAG, "eid = 60999");
                        break;
                    case "60030":
                        LogUtils.d(TAG, "eid = 60030");
                        break;
                }
            } catch (Exception e) {
                LogUtils.e(TAG, "moveStateMessageListener error");
            }
        }
    };

    /**
     * 返回
     * long power_supply_status;//是否充电，
     * float percentage; //电量，
     * long power_supply_technology;//急停，
     * long power_supply_health;//手动移动(锁轴)，
     * float charge;  //温度，
     * BOOL present; //是否可以充电
     */
    private MessageListener<BatteryState> batteryStateMessageListener = new MessageListener<BatteryState>() {
        boolean change = false;
        boolean connect = false;

        @Override
        public void onNewMessage(BatteryState batteryState) {
//            LogUtils.d(TAG, "-----------onNewMessage  batteryState:" + firstMsg);
            change = false;
            if (!firstMsg) {
                getRosVersion();
                firstMsg = true;
            }
            if (mBattery != (int) (batteryState.getPercentage() * 100)) {
                mBattery = (int) (batteryState.getPercentage() * 100);
                LogUtils.d(TAG, batteryState.getPercentage() + "");
                change = true;
            }

            if (mCharge != batteryState.getCharge()) {
                mCharge = batteryState.getCharge();
                change = true;
            }

            if (mPowerTechnology != batteryState.getPowerSupplyTechnology()) {
                mPowerTechnology = batteryState.getPowerSupplyTechnology();
                change = true;
            }

            if (mPowerStatus != batteryState.getPowerSupplyStatus()) {
                mPowerStatus = batteryState.getPowerSupplyStatus();
                change = true;
            }

            String health = batteryState.getPowerSupplyHealth() == (byte) 0 ? "unlock" : "lock";
            if (mPowerHealth != health) {
                mPowerHealth = health;
            }

            if (mPresent != batteryState.getPresent()) {
                mPresent = batteryState.getPresent();
                change = true;
            }

            if (connect != mConnect) {
                connect = mConnect;
                change = true;
            }
//            LogUtils.d(TAG,"change ===="+change);
            if (change) {
//                updateBattery();
            }
        }
    };

    /**
     * float mBattery = 0;
     * long mPowerStatus = 0;
     * long mPowerTechnology = 0;
     * float mCharge = 0;
     * boolean mPresent = true;
     */
//    public void updateBattery() {
//        if (batteryStateSubscriber == null) {
//            LogUtils.d(TAG, "batteryStateSubscriber restart");
//            startSubscriberRosMsg();
//        }
//
//        if (!initCtrollOk) {
//            sendSwithCtrollService((byte) 0);
//        }
//
//        if (firstMsg) {
//            myHandler.removeMessages(ACTION_GETPOST);
//            myHandler.sendEmptyMessageDelayed(ACTION_GETPOST, 3000);
//        }
//
//        RobotSignalEvent batteryEvent = new RobotSignalEvent(ROS_MSG_BATTERY, "");
//        batteryEvent.setBattery(mBattery);
//        batteryEvent.setCharge(mCharge);
//        batteryEvent.setPowerTechnology(mPowerTechnology);
//        batteryEvent.setPresent(mPresent);
//        batteryEvent.setPowerHealth(mPowerHealth);
//        batteryEvent.setConnect(mConnect);
//        batteryEvent.setPowerStatus(mPowerStatus == 1);
//        EventBus.getDefault().post(batteryEvent);
//    }

    /**
     * 4向移动
     *
     * @param
     * @param direction
     */
//    public void sendMove4(String direction) {
//        if (!isConnect()) {
//            LogUtils.e(TAG, "-------connect node is null or not init--------");
//            return;
//        }
//        int orientation = 0;
//        switch (direction) {
//            case "Up":
//                orientation = RosInfo.MOVE_UP;
//                break;
//            case "Down":
//                orientation = RosInfo.MOVE_DOWN;
//                break;
//            case "Left":
//                orientation = RosInfo.MOVE_LEFT;
//                break;
//            case "Right":
//                orientation = RosInfo.MOVE_RIGHT;
//                break;
//            case "Stop":
//                orientation = RosInfo.MOVE_STOP;
//                break;
//        }
//        sendMovePublisherMessage(orientation);
//    }

    /**
     * 8向移动
     *
     * @param x
     * @param z
     */
//    public void sendMove8(float x, float z) {
//        if (!isConnect()) {
//            LogUtils.e(TAG, "-------connect node is null or not init--------");
//            return;
//        }
//        LogUtils.d(TAG, "sendMove8 !!!");
//        sendMovePublisherMessage(x, z);
//    }


    /**
     * 机器人其他控制(升降杆)
     *
     * @param type
     * @param operate
     */
//    public void sendOperat(String type, String operate) {
//        switch (type) {
//            case OPERATION_TYPE_LIFT_VERTICAL:
//                byte mode = 0;
//                switch (operate) {
//                    case "Up":
//                        mode = 1;
//                        break;
//                    case "Down":
//                        mode = 2;
//                        break;
//                    case "Stop":
//                        mode = 0;
//                        break;
//                }
//                sendLiftState(mode);
//                break;
//        }
//    }

    /**
     * 回充
     *
     * @param type
     */
//    public void sendCharging(byte type) {
//        if (!isConnect()) {
//            LogUtils.e(TAG, "-------connect node is null or not init--------");
//            return;
//        }
//        switch (type) {
//            case 0:
//                sendToAutoTrackingService((byte) 0);
//                break;
//            case 1:
//                sendToAutoTrackingService((byte) 1);
//                break;
//            default:
//                break;
//        }
//    }
}