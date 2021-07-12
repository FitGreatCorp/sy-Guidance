package com.fitgreat.airfacerobot.remotesignal;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.business.ApiRequestUrl;
import com.fitgreat.airfacerobot.business.BusinessRequest;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.model.InitEvent;
import com.fitgreat.airfacerobot.remotesignal.model.AuthData;
import com.fitgreat.airfacerobot.remotesignal.model.GroupInfoData;
import com.fitgreat.airfacerobot.remotesignal.model.RobotInfoData;
import com.fitgreat.archmvp.base.okhttp.BaseResponse;
import com.fitgreat.archmvp.base.okhttp.HttpCallback;
import com.fitgreat.archmvp.base.util.JsonUtils;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.PhoneInfoUtils;
import com.google.gson.JsonSyntaxException;
import com.zsoft.signala.ConnectionState;
import com.zsoft.signala.hubs.HubConnection;
import com.zsoft.signala.hubs.HubInvokeCallback;
import com.zsoft.signala.hubs.IHubProxy;
import com.zsoft.signala.transport.StateBase;
import com.zsoft.signala.transport.longpolling.LongPollingTransport;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.Constants.INIT_SIGNAL_SUCCESS;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_START_INIT_VOICE;

/**
 * signal 远程服务器消息管理器<p>
 *
 * @author zixuefei
 * @since 2020/3/26 0026 14:12
 */
public class SignalManager {
    private final String TAG = SignalManager.class.getSimpleName();
    private HubConnection hubConnection = null;
    private IHubProxy hubProxy = null;
    private final int RE_AUTH_ROBOT = 2000;
    private final int RE_GET_ROBOT_INFO = 2001;
    private final int RE_CONNECT_SIGNALR = 2002;
    private final int MAX_RETRY_COUNT = 30;
    private int reAuthCount = 0;
    private int reGetCount = 0;
    private int reSignalCount = 0;
    private Context context;
    private boolean isJoinSuccess;

    public boolean isJoinSuccess() {
        return isJoinSuccess;
    }

    public void setJoinSuccess(boolean joinSuccess) {
        isJoinSuccess = joinSuccess;
    }

    /**
     * SignalR消息监听
     */
    private SignalDataCallback signalDataCallback;

    public SignalManager(Context context) {
        this.context = context;
        this.signalDataCallback = new SignalDataCallback(context);
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RE_AUTH_ROBOT:
                    getRobotToken();
                    break;
                case RE_CONNECT_SIGNALR:
                    initSignalR();
                    break;
                case RE_GET_ROBOT_INFO:
                    getRobotInfo();
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 创建signalR 通信
     */
    private void initSignalR() {
        updateServerProgress(100);
        resetRetryCount();
        setJoinSuccess(true);
        //signal初始化成功发送广播
        Intent intent = new Intent();
        intent.setAction(INIT_SIGNAL_SUCCESS);
        MyApp.getContext().sendBroadcast(intent);



//        try {
//            destroy();
//            hubConnection = new HubConnection(ApiRequestUrl.SIGNALR_URL, context, new LongPollingTransport()) {
//                @Override
//                public void OnError(Exception exception) {
//                    LogUtils.e(TAG, "HubConnection exception :" + exception.getMessage());
//                    reConnect(RE_CONNECT_SIGNALR);
//                }
//
//                @Override
//                public void OnMessage(String message) {
//                    LogUtils.d(TAG, "message :" + message);
//                }
//
//                @Override
//                public void OnStateChanged(StateBase oldState, StateBase newState) {
//                    LogUtils.d(TAG, "oldState = " + oldState.getState() + " , newState = " + newState.getState());
//                    if (oldState.getState() == ConnectionState.Connecting && newState.getState() == ConnectionState.Connected) {
//                        if (hubProxy != null) {
//                            invokeToJoin(hubProxy);
//                        }
//                    }
//
//                    if (oldState.getState() == ConnectionState.Connecting && newState.getState() == ConnectionState.Disconnected) {
//                        reConnect(RE_CONNECT_SIGNALR);
//                    }
//                }
//            };
////            打开SignalR通道，建立消息监听
//            hubProxy = hubConnection.CreateHubProxy(SignalConfig.SIGNALR_HUB);
//            hubConnection.Start();
//            if (hubProxy != null) {
//                hubProxy.On(SignalConfig.SIGNAL_RECEIVE_MESSAGE, signalDataCallback);
//            }
//        } catch (Exception e) {
//            LogUtils.e(TAG, "initSignalR Exception:" + e.getMessage());
//            reConnect(RE_CONNECT_SIGNALR);
//        }
    }


    /**
     * join SignalR服务端组群h
     */
//    public void invokeToJoin(IHubProxy hubProxy) {
//        if (null == hubConnection || null == hubProxy) {
//            return;
//        }
//
//        RobotInfoData robotInfoData = RobotInfoUtils.getRobotInfo();
//        if (robotInfoData != null) {
//            JSONArray jsonArray = new JSONArray();
//            jsonArray.put(robotInfoData.getF_Id());
//            jsonArray.put(robotInfoData.getF_Name());
//            jsonArray.put(robotInfoData.getF_Id());
//            jsonArray.put("robot");
//
//            LogUtils.d(TAG, "join data:" + jsonArray.toString());
//            hubProxy.Invoke("join", jsonArray, new HubInvokeCallback() {
//                @Override
//                public void OnResult(boolean join, String s) {
//                    LogUtils.d(TAG, "join result=" + join + ",msg =" + s);
//                    if (join) {
//                        updateServerProgress(100);
//                        resetRetryCount();
//                        setJoinSuccess(true);
//                        //signal初始化成功发送广播
//                        Intent intent = new Intent();
//                        intent.setAction(INIT_SIGNAL_SUCCESS);
//                        MyApp.getContext().sendBroadcast(intent);
//                    }
//                }
//
//                @Override
//                public void OnError(Exception e) {
//                    LogUtils.e(TAG, "join error:" + e.getMessage());
//                    setJoinSuccess(false);
//                }
//            });
//        }
//    }

    /**
     * 机器人服务器token获取
     */
    public void getRobotToken() {
        BusinessRequest.getAccessToken(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.e(TAG, "auth IOException:" + e.getMessage());
                reConnect(RE_AUTH_ROBOT);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final String result = response.body().string();
                    LogUtils.d(TAG, "auth result:" + result);
                    AuthData authData = JsonUtils.decode(result, AuthData.class);
                    if (authData != null) {
                        updateServerProgress(40);
                        RobotInfoUtils.saveToken(authData.getAccess_token());
                        getRobotInfo();
                    } else {
                        reConnect(RE_AUTH_ROBOT);
                    }
                } catch (JsonSyntaxException e) {
                    LogUtils.e(TAG, "JsonSyntaxException:" + e.getMessage());
                    reConnect(RE_AUTH_ROBOT);
                }
            }
        });
    }

    /**
     * 机器人信息获取
     */
    private void getRobotInfo() {
        LogUtils.d(TAG, "getAirFaceDeviceId:" + RobotInfoUtils.getAirFaceDeviceId());
        if (TextUtils.isEmpty(RobotInfoUtils.getAirFaceDeviceId())) {
            RobotInfoUtils.setAirFaceDeviceId(PhoneInfoUtils.getSn(context));
        }
        BusinessRequest.getRobotInfo(RobotInfoUtils.getAirFaceDeviceId(), new HttpCallback() {
            @Override
            public void onResult(BaseResponse baseResponse) {
                LogUtils.d(DEFAULT_LOG_TAG, "robot info:" + JsonUtils.encode(baseResponse));
                if (baseResponse != null) {
                    if (baseResponse.isSucceed() && !baseResponse.isEmptyContent()) {
                        RobotInfoData robotInfoData = JsonUtils.decode(baseResponse.getMsg(), RobotInfoData.class);
                        if (robotInfoData != null) {
                            LogUtils.d(TAG, "robot status = " + baseResponse.getMsg());
                            RobotInfoUtils.saveRobotInfo(robotInfoData);
                            String linkUrl = robotInfoData.getF_Setting();
                            if (!TextUtils.isEmpty(linkUrl)) {
                                try {
                                    JSONObject jsonObject = new JSONObject(linkUrl);
                                    LogUtils.d(TAG, "url:" + jsonObject.getString("ConnectingDevice"));
                                    RobotInfoUtils.setRobotLinkedDevicesUrl(jsonObject.getString("ConnectingDevice"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                RobotInfoUtils.setRobotLinkedDevicesUrl("");
                            }
                            updateServerProgress(80);
                            initSignalR();
                        }
                    } else {
                        LogUtils.e(TAG, "---get robot info failed---" + baseResponse.getMsg());
                        if (!TextUtils.isEmpty(baseResponse.getMsg())) {
                            if ("机器人id或设备唯一编码错误".contains(baseResponse.getMsg())) {
                                showTips("未注册机器人", "设备id:" + RobotInfoUtils.getAirFaceDeviceId() + "\n请联系管理员注册后再使用\n服务器异常：" + baseResponse.getMsg());
                            } else {
                                reConnect(RE_AUTH_ROBOT);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailed(String e) {
                LogUtils.e(TAG, "onFailed:" + e);
                reConnect(RE_GET_ROBOT_INFO);
            }
        });
    }


    /**
     * @param jsonStr 反馈的信息 json字串，例如 RoomInfo
     */
    public void invokeToSendMessage(String groupName, String userId, String jsonStr, String deviceId, String connectionId) {
        if (null == hubConnection || null == hubProxy) {
            return;
        }
        /**
         * linkGroupId      请求者的组id
         * linkUserId       请求者的userId
         * jsonStr          反馈的信息 json字串，例如 RoomInfo
         * deviceId         发送者的deviceId
         * linkConnectionId 请求者的connectionId
         */
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(groupName);
        jsonArray.put(userId);
        jsonArray.put(jsonStr);
        jsonArray.put(deviceId);
        jsonArray.put(connectionId);
        LogUtils.d(TAG, "send RoomInfo:" + jsonArray.toString());
        hubProxy.Invoke("sendMessage", jsonArray, new HubInvokeCallback() {
            @Override
            public void OnResult(boolean b, String s) {
                LogUtils.d(TAG, "send RoomInfo: OK");
            }

            @Override
            public void OnError(Exception e) {
                LogUtils.d(TAG, "join error");
            }
        });
    }

    /**
     * @param jsonStr 群发反馈的信息 json字串，例如 RoomInfo
     */
    public void invokeToAllSendMessage(String jsonStr) {
        if (null == hubConnection || null == hubProxy) {
            return;
        }

        GroupInfoData groupInfoData = RobotInfoUtils.getRobotGroupInfo();
        if (groupInfoData != null) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(groupInfoData.getGroupName());
            jsonArray.put(jsonStr);
            jsonArray.put(groupInfoData.getGroupName());
            hubProxy.Invoke("sendMessageAll", jsonArray, new HubInvokeCallback() {
                @Override
                public void OnResult(boolean b, String s) {
                    LogUtils.d(TAG, "send ALL: OK");
                }

                @Override
                public void OnError(Exception e) {
                    LogUtils.d(TAG, "send ALL error");
                }
            });
        }
    }

    /**
     * 机器人token认证失败
     */
    private void reConnect(int which) {
        switch (which) {
            case RE_AUTH_ROBOT:
                if (reAuthCount < MAX_RETRY_COUNT) {
                    reAuthCount++;
                    handler.sendEmptyMessageDelayed(RE_AUTH_ROBOT, 8000);
                } else {
//                    showTips("网络或服务器", "服务器繁忙，请联系管理员或稍后再试");
                }
                break;
            case RE_GET_ROBOT_INFO:
                if (reGetCount < MAX_RETRY_COUNT) {
                    reGetCount++;
                    handler.sendEmptyMessageDelayed(RE_GET_ROBOT_INFO, 8000);
                } else {
//                    showTips("网络或服务器", "服务器繁忙，请联系管理员或稍后再试");
                }
                break;
            case RE_CONNECT_SIGNALR:
                if (reSignalCount < MAX_RETRY_COUNT) {
                    reAuthCount++;
                    handler.sendEmptyMessageDelayed(RE_CONNECT_SIGNALR, 8000);
                } else {
//                    showTips("网络或服务器", "服务器繁忙，请联系管理员或稍后再试");
                }

                break;
            default:
                break;
        }
    }


    private void resetRetryCount() {
        reAuthCount = 0;
        reGetCount = 0;
        reSignalCount = 0;
    }

    public void destroy() {
        if (hubConnection != null) {
            hubConnection.Stop();
            hubConnection = null;
            hubProxy = null;
        }
        if (handler.hasMessages(RE_AUTH_ROBOT)) {
            handler.removeMessages(RE_AUTH_ROBOT);
        }
        if (handler.hasMessages(RE_GET_ROBOT_INFO)) {
            handler.removeMessages(RE_GET_ROBOT_INFO);
        }
        if (handler.hasMessages(RE_CONNECT_SIGNALR)) {
            handler.removeMessages(RE_CONNECT_SIGNALR);
        }
    }

    /**
     * 更新UI服务器初始化连接进度
     */
    private void updateServerProgress(int progress) {
        EventBus.getDefault().post(new InitEvent(RobotConfig.TYPE_CHECK_STATE, RobotConfig.INIT_TYPE_SIGNAL_PROGRESS, String.valueOf(progress)));
    }

    /**
     * UI展示弹框提示
     */
    private void showTips(String title, String content) {
        EventBus.getDefault().post(new InitEvent(RobotConfig.TYPE_SHOW_TIPS, title, content));
    }
}
