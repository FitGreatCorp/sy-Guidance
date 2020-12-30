package com.fitgreat.airfacerobot.chosedestination.presenter;

import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.SyncTimeCallback;
import com.fitgreat.airfacerobot.business.ApiRequestUrl;
import com.fitgreat.airfacerobot.business.BusinessRequest;
import com.fitgreat.airfacerobot.model.LocationEntity;
import com.fitgreat.airfacerobot.remotesignal.model.NextOperationData;
import com.fitgreat.airfacerobot.remotesignal.model.RobotInfoData;
import com.fitgreat.airfacerobot.remotesignal.model.SignalDataEvent;
import com.fitgreat.archmvp.base.ui.BasePresenterImpl;
import com.fitgreat.archmvp.base.util.LogUtils;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import com.fitgreat.airfacerobot.chosedestination.view.ChoseDestinationView;
import com.fitgreat.archmvp.base.util.SpUtils;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.NAVIGATION_START_TAG;
import static com.fitgreat.airfacerobot.remotesignal.SignalConfig.OPERATION_TYPE_AUTO_MOVE;

public class ChoseDestinationPresenter extends BasePresenterImpl<ChoseDestinationView> {
    private static final String TAG = "ChoseDestinationPresent";

    /**
     * 发起操作任务
     */
    public void startLocationTask(LocationEntity locationInfo) {
        JSONArray operationList = new JSONArray();
        JSONArray instructionList = new JSONArray();
        //添加操作任务
        JSONObject location = new JSONObject();
        try {
            location.put("Type", "Location");
            location.put("InstructionId", locationInfo.getF_Id());
            location.put("InstructionName", locationInfo.getF_Name());
            location.put("Sort", "1");
            instructionList.put(location);
            JSONObject operationObj = new JSONObject();
            operationObj.put("InstructionList", instructionList);
            operationObj.put("Sort", "1");
            operationList.put(operationObj);
        } catch (JSONException e) {
            e.printStackTrace();
            //单点导航任务结束
            SpUtils.putBoolean(MyApp.getContext(), NAVIGATION_START_TAG, false);
        }
        //获取机器人信息
        RobotInfoData robotInfo = RobotInfoUtils.getRobotInfo();
        //添加新的活动流程
        CreateAction(robotInfo.getF_Id(), robotInfo.getF_HardwareId(), robotInfo.getF_Id(), robotInfo.getF_Hospital(), robotInfo.getF_Department(), operationList.toString());
    }

    /**
     * 根据指令包含的任务,导航地点 发起活动流程
     *
     * @param processInitiatorId 流程发起人id,控制端就是机器人id
     * @param hardwareId         机器人设备序列号
     * @param robotAccountId     机器人id
     * @param hospital           医院名字
     * @param department         医院部门
     * @param operationList      导航,操作任务的拼接数据
     */
    private void CreateAction(String processInitiatorId, String hardwareId, String robotAccountId, String hospital, String department, String operationList) {
        try {
            //设置参数
            JSONObject info = new JSONObject();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("HardwareId", hardwareId);
            jsonObject.put("RobotAccountId", robotAccountId);
            jsonObject.put("Hospital", hospital);
            jsonObject.put("Department", department);
            jsonObject.put("OperationProcedureId", "");
            jsonObject.put("F_Promoter", processInitiatorId);
            jsonObject.put("OperationList", operationList);
            info.put("Info", jsonObject.toString());
            BusinessRequest.postStringRequest(info.toString(), ApiRequestUrl.CREATE_ACTION, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LogUtils.e(DEFAULT_LOG_TAG, "选择导航,发起活动流程失败:onFailure=>" + e.toString());
                    //单点导航任务结束
                    SpUtils.putBoolean(MyApp.getContext(), NAVIGATION_START_TAG, false);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String stringResponse = response.body().string();
                    LogUtils.d(DEFAULT_LOG_TAG, "选择导航,发起活动流程成功:onResponse=>" + stringResponse);
                    try {
                        JSONObject jsonObject = new JSONObject(stringResponse);
                        if (jsonObject.has("type") && jsonObject.getString("type").equals("success")) {
                            String actionId = jsonObject.getString("msg");
                            LogUtils.d(DEFAULT_LOG_TAG, "当前活动id , " + actionId);
                            //改变当前机器人状态为操作中
                            RobotInfoUtils.setRobotRunningStatus(String.valueOf(3));
                            //更新时间戳
                            BusinessRequest.getServerTime(SyncTimeCallback.syncTimeCallback);
                            //获取流程中的下一步操作
                            getNextStepInfo(actionId);
                        }else {
                            //单点导航任务结束
                            SpUtils.putBoolean(MyApp.getContext(), NAVIGATION_START_TAG, false);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        //单点导航任务结束
                        SpUtils.putBoolean(MyApp.getContext(), NAVIGATION_START_TAG, false);
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            //单点导航任务结束
            SpUtils.putBoolean(MyApp.getContext(), NAVIGATION_START_TAG, false);
        }
    }

    /**
     * 根据流程id,获取流程中的下一步操作
     *
     * @param actionId 流程id
     */
    public void getNextStepInfo(String actionId) {
        BusinessRequest.getNextStep(actionId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.e(DEFAULT_LOG_TAG, "MSG_TYPE_TASK:onFailure=>" + e.toString());
                //单点导航任务结束
                SpUtils.putBoolean(MyApp.getContext(), NAVIGATION_START_TAG, false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogUtils.d(DEFAULT_LOG_TAG, "选择导航,获取下一步操作成功==>" + result);
                try {
                    JSONObject baseResObj = new JSONObject(result);
                    if (baseResObj.has("type") && baseResObj.getString("type").equals("success")) {
                        String msgString = baseResObj.getString("msg");
                        LogUtils.json(DEFAULT_LOG_TAG, msgString);
                        if (msgString != null) {
                            NextOperationData nextOperationData = JSON.parseObject(msgString, NextOperationData.class);
                            LogUtils.d(DEFAULT_LOG_TAG, JSON.toJSONString(nextOperationData));
                            SignalDataEvent autoMoveEvent = new SignalDataEvent();
                            if (!nextOperationData.getF_Type().equals("End")) {
                                autoMoveEvent.setInstructionId(nextOperationData.getF_Id());
                                autoMoveEvent.setInstructionType(nextOperationData.getF_Type());
                                autoMoveEvent.setF_InstructionName(nextOperationData.getF_InstructionName());
                                autoMoveEvent.setContainer(nextOperationData.getF_Container());
                                autoMoveEvent.setFileUrl(nextOperationData.getF_FileUrl());
                                autoMoveEvent.setProduceId(actionId);
                                autoMoveEvent.setOperationType(nextOperationData.getOperationType());
                                autoMoveEvent.setX(nextOperationData.getF_X());
                                autoMoveEvent.setY(nextOperationData.getF_Y());
                                autoMoveEvent.setE(nextOperationData.getF_Z());
                                autoMoveEvent.setF_InstructionEnName(nextOperationData.getF_InstructionEnName());
                                LogUtils.d(DEFAULT_LOG_TAG, "nextOperationData.getF_Type()=>" + nextOperationData.getF_Type());
                                autoMoveEvent.setType(OPERATION_TYPE_AUTO_MOVE);
                                EventBus.getDefault().post(autoMoveEvent);
                            }else {
                                //单点导航任务结束
                                SpUtils.putBoolean(MyApp.getContext(), NAVIGATION_START_TAG, false);
                            }
                        }else {
                            //单点导航任务结束
                            SpUtils.putBoolean(MyApp.getContext(), NAVIGATION_START_TAG, false);
                        }
                    }else {
                        //单点导航任务结束
                        SpUtils.putBoolean(MyApp.getContext(), NAVIGATION_START_TAG, false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
