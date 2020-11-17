package com.fitgreat.airfacerobot.remotesignal;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.SyncTimeCallback;
import com.fitgreat.airfacerobot.business.BusinessRequest;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.launcher.model.ActionEvent;
import com.fitgreat.airfacerobot.launcher.model.InitEvent;
import com.fitgreat.airfacerobot.launcher.model.NavigationTip;
import com.fitgreat.airfacerobot.remotesignal.model.BaseSignalMsg;
import com.fitgreat.airfacerobot.remotesignal.model.GroupInfoData;
import com.fitgreat.airfacerobot.remotesignal.model.JoinInfoData;
import com.fitgreat.airfacerobot.remotesignal.model.MoveResultData;
import com.fitgreat.airfacerobot.remotesignal.model.NextOperationData;
import com.fitgreat.airfacerobot.remotesignal.model.OperationData;
import com.fitgreat.airfacerobot.remotesignal.model.SignalDataEvent;
import com.fitgreat.airfacerobot.videocall.VideoCallConstant;
import com.fitgreat.airfacerobot.videocall.model.AgoraTokenInfo;
import com.fitgreat.airfacerobot.videocall.ui.VideoCallActivity;
import com.fitgreat.archmvp.base.util.CollectionUtils;
import com.fitgreat.archmvp.base.util.JsonUtils;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.SpUtils;
import com.zsoft.signala.hubs.HubOnDataCallback;

import org.apache.commons.lang.StringEscapeUtils;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_STOP_MOVE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.REMOTE_TASK_JOIN_SUCCESS;
import static com.fitgreat.airfacerobot.remotesignal.SignalConfig.MSG_CLOSE_ANDROID_SHARE;
import static com.fitgreat.airfacerobot.remotesignal.SignalConfig.MSG_STOP_TASK;
import static com.fitgreat.airfacerobot.remotesignal.SignalConfig.OPERATION_TYPE_4_MOVE;
import static com.fitgreat.airfacerobot.remotesignal.SignalConfig.OPERATION_TYPE_8_MOVE;
import static com.fitgreat.airfacerobot.remotesignal.SignalConfig.OPERATION_TYPE_LIFT_VERTICAL;

/**
 * SignalR 服务器消息处理类
 *
 * @author zixuefei
 * @since 2020/3/25 0025 09:46
 */
public class SignalDataCallback extends HubOnDataCallback {
    public static final String TAG = SignalDataCallback.class.getSimpleName();
    public final String RESPONSE_TYPE_SUCCESS = "success";
    private Context mcontext;
    private String joined_id, joined_connectionId, joined_groupName;
    private AgoraTokenInfo agoraTokenInfo;
    private String targetUser, connectionId;
    private String mini_deviceId;

    public SignalDataCallback(Context context) {
        mcontext = context;
    }

    @Override
    public void OnReceived(JSONArray jsonArray) {
        try {
            String baseResult = jsonArray.getString(0);
            LogUtils.json(TAG, StringEscapeUtils.escapeJava(baseResult));
            if (!TextUtils.isEmpty(baseResult)) {
                BaseSignalMsg baseSignalMsg = JsonUtils.decode(baseResult, BaseSignalMsg.class);
                if (baseSignalMsg != null) {
                    targetUser = baseSignalMsg.getUserId();
                    connectionId = baseSignalMsg.getConnectionId();
                }
                LogUtils.d(TAG, "baseSignalMsg=>" + baseSignalMsg.getType());
                if (baseSignalMsg != null && TextUtils.equals(RESPONSE_TYPE_SUCCESS, baseSignalMsg.getType())) {
                    BaseSignalMsg.SignalMsg signalMsg = JsonUtils.decode(baseSignalMsg.getMsg(), BaseSignalMsg.SignalMsg.class);
                    LogUtils.d(TAG, "signalMsg.getType()==>" + signalMsg.getType());
                    LogUtils.json(TAG, "signalMsg==>" + StringEscapeUtils.unescapeJava(JsonUtils.encode(signalMsg)));
                    if (signalMsg != null && signalMsg.getType() != null) {
                        switch (signalMsg.getType()) {
                            case SignalConfig.MSG_TYPE_GROUPLIST:
                                if (!TextUtils.isEmpty(signalMsg.getResult())) {
                                    LogUtils.json(TAG, "signalMsg.getResult():" + signalMsg.getResult());
                                    ArrayList<GroupInfoData> groupInfoDatas = JsonUtils.jsonToArrayList(signalMsg.getResult(), GroupInfoData.class);
                                    if (!CollectionUtils.isEmpty(groupInfoDatas)) {
                                        GroupInfoData groupInfo = groupInfoDatas.get(0);
                                        LogUtils.json(TAG, "groupInfo:" + JsonUtils.encode(groupInfo) + "----" + groupInfoDatas.size());
                                        if (groupInfo != null) {
                                            LogUtils.d(TAG, "保存机器人加入群组信息:" + groupInfo.getUserId());
                                            RobotInfoUtils.saveRobotGroupInfo(groupInfo);
                                        }
                                    }
                                    LogUtils.d(TAG, "groupInfoDatas:" + StringEscapeUtils.unescapeJava(JSON.toJSONString(groupInfoDatas)));
                                    boolean hasuser = false;
                                    String miniConnectionId = "";
                                    for (int i = 0; i < groupInfoDatas.size(); i++) {
                                        if (groupInfoDatas.get(i).getType().equals("user")) {
                                            hasuser = true;
                                            return;
                                        }
                                        if (groupInfoDatas.get(i).getType().equals("mini")) {
                                            mini_deviceId = groupInfoDatas.get(i).getUserId();
                                            miniConnectionId = groupInfoDatas.get(i).getConnectionId();
                                        }
                                    }
                                    LogUtils.d(TAG, "hasuser==>" + hasuser);
                                    if (!hasuser) {
                                        SignalDataEvent event = new SignalDataEvent();
                                        event.setType(MSG_CLOSE_ANDROID_SHARE);
                                        event.setTargetUser(mini_deviceId);
                                        event.setConnectionId(miniConnectionId);
                                        EventBus.getDefault().post(event);
                                    }
                                }
                                break;
                            case SignalConfig.MSG_TYPE_JOIN: //有远程任务加入
                                //Join成功
                                if (!TextUtils.isEmpty(baseResult)) {
                                    JoinInfoData joinInfoData = JsonUtils.decode(baseResult, JoinInfoData.class);
                                    if (joinInfoData != null) {
                                        LogUtils.d(TAG, "id = " + joinInfoData.getUserId() + " robot id = " + RobotInfoUtils.getRobotInfo().getF_Id());
                                        if (joinInfoData.getUserId().equals(RobotInfoUtils.getRobotInfo().getF_Id())) {

                                        } else {
                                            if (joinInfoData != null) {
                                                joined_id = joinInfoData.getUserId();
                                                joined_groupName = joinInfoData.getGroupName();
                                                joined_connectionId = joinInfoData.getConnectionId();
                                            }
                                        }
                                    }
                                }
                                EventBus.getDefault().post(new InitEvent(REMOTE_TASK_JOIN_SUCCESS, ""));
                                break;
                            case SignalConfig.MSG_TYPE_INIT_VIDEO:

                                break;
                            case SignalConfig.MSG_TYPE_HANG_ON:
                                String userId = "";
                                String runningStatus = RobotInfoUtils.getRobotRunningStatus();
                                LogUtils.d(TAG, "robot status = " + runningStatus);
                                if (!TextUtils.isEmpty(signalMsg.getResult())) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(signalMsg.getResult());
                                        if (jsonObject.has("userId")) {
                                            userId = jsonObject.getString("userId");
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (runningStatus.equals("1") || runningStatus.equals("5") || runningStatus.equals("4")) {
                                    LogUtils.d(TAG, "account = " + RobotInfoUtils.getRobotInfo().getF_Account() + "  ,id = " + RobotInfoUtils.getRobotInfo().getF_Id());

                                    String finalUserId = userId;
                                    BusinessRequest.getAgoraToken(RobotInfoUtils.getRobotInfo().getF_Account(), RobotInfoUtils.getRobotInfo().getF_Id(), new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            LogUtils.e(TAG, "onFailure e = " + e.toString());
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            String result = response.body().string();
                                            LogUtils.d(TAG, "onResponse = " + result);
                                            agoraTokenInfo = JsonUtils.decode(result, AgoraTokenInfo.class);
                                            Intent intent = new Intent(mcontext, VideoCallActivity.class);
                                            intent.putExtra("agoratoken", agoraTokenInfo.getToken());
                                            intent.putExtra("uid", agoraTokenInfo.getUid());
                                            intent.putExtra("userId", finalUserId);
                                            mcontext.startActivity(intent);
                                        }
                                    });
                                }
                                break;
                            case SignalConfig.MSG_TYPE_EXITGROUP:
                                EventBus.getDefault().post(new SignalDataEvent(RobotConfig.CONTROL_TYPE_HEADER_ACTUATOR, "45"));
                                break;
                            case SignalConfig.MSG_TYPE_OPERATION_MINI:
                            case SignalConfig.MSG_TYPE_OPERATION:
                                handleOperation(signalMsg.getResult());
                                break;
                            case SignalConfig.MSG_GET_ROBOT_STATUS:  //获取机器人状态
                                EventBus.getDefault().post(new SignalDataEvent(SignalConfig.MSG_GET_ROBOT_STATUS, baseResult));
                                break;
                            case SignalConfig.MSG_TYPE_TASK:  //todo 任务  004
                                //改变当前机器人状态为操作中
//                                saveRobotstatus(3);
//                                SignalDataEvent tasksuccess = new SignalDataEvent();
//                                tasksuccess.setType(MSG_RECEIVER_TASK_SUCCESS);
//                                EventBus.getDefault().post(tasksuccess);
//                                //更新时间戳
//                                BusinessRequest.getServerTime(SyncTimeCallback.syncTimeCallback);
//                                LogUtils.d("startSpecialWorkFlow", "signalMsg.getResult()\t\t"+signalMsg.getResult());
//                                //获取下一步操作
//                                BusinessRequest.getNextStep(signalMsg.getResult(), new Callback() {
//                                    @Override
//                                    public void onFailure(Call call, IOException e) {
//                                        LogUtils.e(TAG, "MSG_TYPE_TASK:onFailure=>" + e.toString());
//                                    }
//
//                                    @Override
//                                    public void onResponse(Call call, Response response) throws IOException {
//                                        String result = response.body().string();
//                                        try {
//                                            JSONObject baseResObj = new JSONObject(result);
//                                            if (baseResObj.has("type")) {
//                                                String type = baseResObj.getString("type");
//                                                if (type.equals("success")) {
//                                                    String msg = baseResObj.getString("msg");
//                                                    LogUtils.d("startSpecialWorkFlow", "获取到的下一步任务种类\t\tmsg");
//                                                    LogUtils.json("startSpecialWorkFlow", msg);
//                                                    NextOperationData nextOperationData = JsonUtils.decode(msg, NextOperationData.class);
//                                                    if (nextOperationData != null) {
//                                                        if (nextOperationData.getOperationType() == null || !nextOperationData.getOperationType().equals("End")) {
//                                                            SignalDataEvent autoMoveEvent = new SignalDataEvent();
//                                                            autoMoveEvent.setConnectionId(connectionId);
//                                                            autoMoveEvent.setTargetUser(targetUser);
//                                                            autoMoveEvent.setInstructionId(nextOperationData.getF_Id());
//                                                            autoMoveEvent.setInstructionType(nextOperationData.getF_Type());
//                                                            autoMoveEvent.setF_InstructionName(nextOperationData.getF_InstructionName());
//                                                            autoMoveEvent.setContainer(nextOperationData.getF_Container());
//                                                            autoMoveEvent.setFileUrl(nextOperationData.getF_FileUrl());
//                                                            autoMoveEvent.setProduceId(signalMsg.getResult());
//                                                            autoMoveEvent.setOperationType(nextOperationData.getOperationType());
//                                                            autoMoveEvent.setX(nextOperationData.getF_X());
//                                                            autoMoveEvent.setY(nextOperationData.getF_Y());
//                                                            autoMoveEvent.setE(nextOperationData.getF_Z());
//                                                            boolean startGuideWorkflowTag = SpUtils.getBoolean(MyApp.getContext(), START_GUIDE_WORK_FLOW_TAG, false);
//                                                            if (startGuideWorkflowTag){  //当前启动为引导流程
//                                                                //语音播报提示
//                                                                EventBus.getDefault().post(new ActionEvent(PLAY_TASK_PROMPT_INFO, "我将带您参观学术大厅，现在将去的是" + nextOperationData.getF_InstructionName()));
//                                                                //更新提示信息到首页对话记录
//                                                                EventBus.getDefault().post(new NavigationTip("我将带您参观学术大厅，现在将去的是" + nextOperationData.getF_InstructionName()));
//                                                            }
//                                                            LogUtils.d("startSpecialWorkFlow", "获取到的下一步任务种类\t\tautoMoveEvent");
//                                                            LogUtils.json("startSpecialWorkFlow", JSON.toJSONString(autoMoveEvent));
//                                                            if (nextOperationData.getF_Type().equals("Location")) {  //导航移动到某地
//                                                                autoMoveEvent.setType(OPERATION_TYPE_AUTO_MOVE);
//                                                            } else { //更新任务指令
//                                                                autoMoveEvent.setType(MSG_UPDATE_INSTARUCTION_STATUS);
//                                                            }
//                                                            EventBus.getDefault().post(autoMoveEvent);
//                                                        } else {
//                                                            SignalDataEvent taskendEvent = new SignalDataEvent();
//                                                            taskendEvent.setType(MSG_TASK_END);
//                                                            EventBus.getDefault().post(taskendEvent);
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        } catch (JSONException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                });
                                break;
                            case MSG_STOP_TASK:
                                //改变当前机器人状态为空闲
                                saveRobotstatus(1);
                                LogUtils.d(TAG, "MSG_STOP_TASK:当前导航任务终止");
                                SignalDataEvent stopmoveEvent = new SignalDataEvent();
                                stopmoveEvent.setType(MSG_STOP_MOVE);
                                EventBus.getDefault().post(stopmoveEvent);
                                SignalDataEvent stopTaskEvent = new SignalDataEvent();
                                stopTaskEvent.setType(MSG_STOP_TASK);
                                EventBus.getDefault().post(stopTaskEvent);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            LogUtils.e(TAG, "JSONException:" + e.getMessage());
        }
    }

    /**
     * 处理操控类消息
     */
    private void handleOperation(String operation) {
        LogUtils.d(TAG, "operation:" + operation);
        if (!TextUtils.isEmpty(operation)) {
            OperationData operationData = JsonUtils.decode(operation, OperationData.class);
            if (operationData != null) {
                SignalDataEvent signalDataEvent = new SignalDataEvent();
                double timeStamp = 0;
                if (operationData != null) {
                    timeStamp = operationData.getTimeStamp();
                }
                switch (operationData.getType()) {
                    case SignalConfig.OPERATION_TYPE_HAED_ANGLE:
                        int degree = 15 + 15 * (Integer.parseInt(operationData.getResult()) - 5);
                        signalDataEvent.setType(RobotConfig.CONTROL_TYPE_HEADER_ACTUATOR);
                        signalDataEvent.setAction(String.valueOf(degree));
                        EventBus.getDefault().post(signalDataEvent);
                        break;
                    case OPERATION_TYPE_4_MOVE:
//                        long timestamp4 = Long.valueOf(String.valueOf(timeStamp));
                        LogUtils.d("robot_move", "result = OPERATION_TYPE_4_MOVE ");
                        String direction = operationData.getResult();
                        signalDataEvent.setType(OPERATION_TYPE_4_MOVE);
                        signalDataEvent.setDirection(direction);
//                        LogUtils.d(TAG, " 时间差  = = " + (System.currentTimeMillis() + VideoCallConstant.sTimeDiff - timestamp4));
//                        if (System.currentTimeMillis() + VideoCallConstant.sTimeDiff - timestamp4 > 700) {
//                            return;
//                        }
                        EventBus.getDefault().post(signalDataEvent);
                        break;
                    case OPERATION_TYPE_8_MOVE:
                        String result = operationData.getResult();
                        LogUtils.d("robot_move", "result = OPERATION_TYPE_8_MOVE " + result);
                        MoveResultData resultData = JsonUtils.decode(result, MoveResultData.class);
                        long timestamp8 = Long.valueOf(resultData.getE());
                        signalDataEvent.setType(OPERATION_TYPE_8_MOVE);
                        LogUtils.d(TAG, "resultData = " + resultData);
                        if (resultData != null) {
                            signalDataEvent.setX(resultData.getX());
                            signalDataEvent.setY(resultData.getY());
                        }
                        LogUtils.d(TAG, "时间差  = " + (System.currentTimeMillis() + VideoCallConstant.sTimeDiff - timestamp8));
                        if (System.currentTimeMillis() + VideoCallConstant.sTimeDiff - timestamp8 > 700) {
                            return;
                        }
                        EventBus.getDefault().post(signalDataEvent);
                        break;
                    case OPERATION_TYPE_LIFT_VERTICAL:
                        String vertical = operationData.getResult();
                        signalDataEvent.setType(OPERATION_TYPE_LIFT_VERTICAL);
                        signalDataEvent.setVertical(vertical);
                        EventBus.getDefault().post(signalDataEvent);
                        break;
                    default:
                        break;
                }
            }
        }
    }


    /**
     * 更改机器人状态信息
     *
     * @param status
     */
    private void saveRobotstatus(int status) {
        //改变当前机器人状态为空闲
        RobotInfoUtils.setRobotRunningStatus(String.valueOf(status));
    }


}
