package com.fitgreat.airfacerobot.launcher.presenter;

import android.content.Context;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.SyncTimeCallback;
import com.fitgreat.airfacerobot.business.ApiRequestUrl;
import com.fitgreat.airfacerobot.business.BusinessRequest;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.launcher.contractview.MainView;
import com.fitgreat.airfacerobot.launcher.model.DaemonEvent;
import com.fitgreat.airfacerobot.launcher.model.LocationEntity;
import com.fitgreat.airfacerobot.launcher.model.MapEntity;
import com.fitgreat.airfacerobot.launcher.model.OperationInfo;
import com.fitgreat.airfacerobot.launcher.model.RobotSignalEvent;
import com.fitgreat.airfacerobot.launcher.utils.LocalCashUtils;
import com.fitgreat.airfacerobot.remotesignal.model.NextOperationData;
import com.fitgreat.airfacerobot.remotesignal.model.RobotInfoData;
import com.fitgreat.airfacerobot.remotesignal.model.SignalDataEvent;
import com.fitgreat.airfacerobot.versionupdate.VersionInfo;
import com.fitgreat.airfacerobot.versionupdate.VersionUtils;
import com.fitgreat.archmvp.base.okhttp.BaseResponse;
import com.fitgreat.archmvp.base.okhttp.HttpMainCallback;
import com.fitgreat.archmvp.base.ui.BasePresenterImpl;
import com.fitgreat.archmvp.base.util.JsonUtils;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.SpUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import static com.fitgreat.airfacerobot.constants.RobotConfig.GUIDE_SPECIFIC_WORKFLOW;
import static com.fitgreat.airfacerobot.constants.RobotConfig.GUIDE_WORK_FLOW_ACTION_ID;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MAP_INFO_CASH;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_ROS_NEXT_STEP;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TASK_END;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_UPDATE_INSTARUCTION_STATUS;
import static com.fitgreat.airfacerobot.constants.RobotConfig.RECHARGE_OPERATION_INFO;
import static com.fitgreat.airfacerobot.constants.RobotConfig.RECHARGE_SPECIFIC_WORKFLOW;
import static com.fitgreat.airfacerobot.remotesignal.SignalConfig.OPERATION_TYPE_AUTO_MOVE;


/**
 * 启动页数据接口<p>
 *
 * @author zixuefei
 * @since 2020/3/11 0011 10:18
 */
public class MainPresenter extends BasePresenterImpl<MainView> {
    private List<LocationEntity> locationList = new ArrayList<>();
    private List<OperationInfo> operationInfosList = new ArrayList<>();
    private static final String TAG = "LauncherPresenter";
    //当前执行任务信息
    private OperationInfo operationOne;
    //当前导航点信息
    private LocationEntity locationOne;


    /**
     * 检查hardware版本更新
     */
    public void checkHardwareVersion() {
        BusinessRequest.checkHardwareVersion(new HttpMainCallback() {
            @Override
            public void onResult(BaseResponse baseResponse) {
                LogUtils.d(TAG, "check hardware version:" + JsonUtils.encode(baseResponse));
                if (baseResponse != null && !baseResponse.isEmptyData()) {
                    LogUtils.d(TAG, "check hardware version:" + baseResponse.getData());
                    VersionInfo versionInfo = JsonUtils.decode(baseResponse.getData(), VersionInfo.class);
                    if (versionInfo != null) {
                        String current = RobotInfoUtils.getHardwareVersion().replaceAll("v", "").replaceAll("V", "");
                        if (VersionUtils.compareVersion(versionInfo.getF_Version(), current) > 0) {
                            if (mView != null) {
                                mView.foundHardwareNewVersion(versionInfo);
                            }
                        } else {
                            LogUtils.d(TAG, "-------has not found new version------");
                        }
                    }
                }
            }

            @Override
            public void onFailed(String e) {
                LogUtils.e(TAG, "check version exception:" + e);
            }
        });
    }

    /**
     * 提交硬件更新结果
     */
    public void commitHardwareUpdateResult(String f_stepId, String status, String msg) {
        BusinessRequest.updateHardwareResult("", f_stepId, status, msg, new HttpMainCallback() {
            @Override
            public void onResult(BaseResponse baseResponse) {
                String result = JsonUtils.encode(baseResponse);
                LogUtils.d("updateHardwareResult", "提交硬件升级结果成功:" + result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.has("msg")) {
                        String msg = jsonObject.getString("msg");
                        if (msg.equals("成功")) {
                            DaemonEvent daemonEvent = new DaemonEvent(MSG_ROS_NEXT_STEP, "");
                            EventBus.getDefault().post(daemonEvent);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailed(String e) {
                super.onFailed(e);
                LogUtils.d("updateHardwareResult", "提交硬件升级结果失败:" + e);
            }
        });
    }


    /**
     * 获取4G网络的信号强度
     *
     * @param context
     */
    public void getNetSignalLevel(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //开始监听
        tm.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            String signalInfo = signalStrength.toString();
            LogUtils.d(TAG, "signalStrength:" + signalInfo);
            LogUtils.d(TAG, "signal:" + signalStrength.getLevel());
            EventBus.getDefault().post(new RobotSignalEvent(RobotConfig.ROBOT_SIM_SIGNAL, String.valueOf(signalStrength.getLevel())));
        }
    };

    /**
     * 获取导航位置信息
     */
    public void getLocationInfo() {
        //获取地图信息
        RobotInfoData robotInfo = RobotInfoUtils.getRobotInfo();
        LogUtils.json("robotInfo", "机器人信息==>" + StringEscapeUtils.unescapeJava(JsonUtils.encode(robotInfo)));
        if (robotInfo != null) {
            //设置参数
            HashMap<String, String> parameterMap = new HashMap<>();
            parameterMap.put("hardwareId", robotInfo.getF_HardwareId());
            //发起请求
            BusinessRequest.postStringRequest(JSON.toJSONString(parameterMap), ApiRequestUrl.GET_ONE_MAP, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LogUtils.e("robotInfo", "获取地图,导航位置信息失败-->" + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String stringResponse = response.body().string();
                    LogUtils.d("robotInfo", "获取地图,导航位置信息成功-->" + StringEscapeUtils.unescapeJava(stringResponse));
                    try {
                        JSONObject jsonObject = new JSONObject(stringResponse);
                        String type = jsonObject.getString("type");
                        String msg = jsonObject.getString("msg");
                        if (type.equals("success")) {
                            JSONObject msgObj = new JSONObject(msg);
                            if (msgObj.has("locationLists")) {
                                if (locationList.size() > 0) {
                                    locationList.clear();
                                }
                                //处理导航点位置信息并缓存到本地
                                handNavigationPointCash(msgObj);
                                //获取执行任务数据
                                getOperationList(robotInfo.getF_HospitalId());
                                LogUtils.json("robotInfo", "获取导航地点信息locationList->" + JSON.toJSONString(locationList));
                            }
                        } else {
                            mView.getLocationFailure(msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * 获取医院执行任务列表
     */
    private void getOperationList(String hospitalId) {
        HashMap<String, String> parameterMap = new HashMap<>();
        parameterMap.put("hospitalId", hospitalId);
        BusinessRequest.postStringRequest(JSON.toJSONString(parameterMap), ApiRequestUrl.GET_OPERATION_LIST, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.e("robotInfo", "onError:获取执行任务列表信息失败-->" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String stringResponse = response.body().string();
                LogUtils.json("robotInfo", "获取执行任务列表信息成功-->" + StringEscapeUtils.unescapeJava(stringResponse));
                try {
                    JSONObject msbObj = new JSONObject(stringResponse);
                    String type = msbObj.getString("type");
                    String msg = msbObj.getString("msg");
                    if (type.equals("success")) {
                        handOperationCash(msg);
                        LocalCashUtils.getOperationList();
                    } else {
                        mView.getOperationListFailure(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 获取处理医院操作任务信息,并缓存到本地
     */
    private void handOperationCash(String msg) throws JSONException {
        JSONTokener arrayTokener = new JSONTokener(msg);
        JSONArray msgArray = (JSONArray) arrayTokener.nextValue();
        if (operationInfosList.size() > 0) {
            operationInfosList.clear();
        }
        for (int i = 0; i < msgArray.length(); i++) {
            OperationInfo operationInfo = new OperationInfo();
            JSONObject operationObj = msgArray.getJSONObject(i);
            if (operationObj.has("F_Id")) {
                operationInfo.setF_id(operationObj.getString("F_Id"));
            }
            if (operationObj.has("F_Name")) {
                operationInfo.setF_Name(operationObj.getString("F_Name"));
            }
            if (operationObj.has("F_Type")) {
                operationInfo.setF_Type(operationObj.getString("F_Type"));
            }
            if (operationObj.has("F_FileUrl")) {
                operationInfo.setF_FileUrl(operationObj.getString("F_FileUrl"));
            }
            if (operationObj.has("F_HospitalId")) {
                operationInfo.setF_HospitalId(operationObj.getString("F_HospitalId"));
            }
            if (operationObj.has("F_Memo")) {
                operationInfo.setF_Memo(operationObj.getString("F_Memo"));
            }
            if (operationObj.has("F_IsBreak")) {
                operationInfo.setF_IsBreak(operationObj.getString("F_IsBreak"));
            }
            //缓存"自动回充"任务信息到本地
            if (operationObj.getString("F_Name").equals("自动回充")) {
                SpUtils.putString(MyApp.getContext(), RECHARGE_OPERATION_INFO, JSON.toJSONString(operationInfo));
            }
            operationInfosList.add(operationInfo);
        }
        //缓存操作任务信息以json的形式到本地
        SpUtils.putString(MyApp.getContext(), "operationList", JSON.toJSONString(operationInfosList));
    }

    /**
     * 获取处理导航点位置信息,并缓存到本地
     */
    private void handNavigationPointCash(JSONObject msgObj) throws JSONException {
        String locationStr = msgObj.getString("locationLists");
        JSONTokener locationTokener = new JSONTokener(locationStr);
        JSONArray locationArray = (JSONArray) locationTokener.nextValue();
        for (int i = 0; i < locationArray.length(); i++) {
            LocationEntity locationEntity = new LocationEntity();
            JSONObject locationObj = locationArray.getJSONObject(i);
            if (locationObj.has("F_Id")) {
                locationEntity.setF_Id(locationObj.getString("F_Id"));
            }
            if (locationObj.has("F_MapId")) {
                locationEntity.setF_MapId(locationObj.getString("F_MapId"));
            }
            if (locationObj.has("F_Name")) {
                locationEntity.setF_Name(locationObj.getString("F_Name"));
            }
            if (locationObj.has("F_X")) {
                locationEntity.setF_X(locationObj.getDouble("F_X"));
            }
            if (locationObj.has("F_Y")) {
                locationEntity.setF_Y(locationObj.getDouble("F_Y"));
            }
            if (locationObj.has("F_Z")) {
                locationEntity.setF_Z(locationObj.getDouble("F_Z"));
            }
            if (locationObj.has("F_Memo")) {
                locationEntity.setF_Memo(locationObj.getString("F_Memo"));
            }
            locationList.add(locationEntity);
        }
        //地图信息缓存
        String mapString = msgObj.getString("map");
        LogUtils.json("mapString", mapString);
        SpUtils.putString(MyApp.getContext(), MAP_INFO_CASH, mapString);
        //缓存导航地点信息以json的形式到本地
        SpUtils.putString(MyApp.getContext(), "locationList", JSON.toJSONString(locationList));
        //获取自动回充工作流
        automaticRechargeWorkflow("charge");
        //获取引导讲解工作流
        automaticRechargeWorkflow("guide");
    }

    /**
     * 获取自动回充工作流信息    charge 自动回充
     * guide    引导流程类型
     */
    public static void automaticRechargeWorkflow(String workflowType) {
        //医院地图信息
        String mapInfoString = SpUtils.getString(MyApp.getContext(), MAP_INFO_CASH, null);
        if (mapInfoString != null) {
            //解析地图信息获取对象
            MapEntity mapEntity = JSON.parseObject(mapInfoString, MapEntity.class);
            LogUtils.d(TAG, "获取特定工作流");
            HashMap<String, String> param = new HashMap<>();
            param.put("departmentId", mapEntity.getF_DepartmentId());
            param.put("mapId", mapEntity.getF_Id());
            param.put("type", workflowType);
            LogUtils.json(TAG, JSON.toJSONString(param));
            BusinessRequest.getRequestWithParam(param, ApiRequestUrl.SPECIFIC_WORK_FLOM, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LogUtils.e(TAG, "获取特定工作流失败: " + e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String stringResponse = response.body().string();
                    LogUtils.d(TAG, "获取特定工作流成功: " + stringResponse);
                    try {
                        JSONObject jsonObject = new JSONObject(stringResponse);
                        if (jsonObject.has("type") && jsonObject.getString("type").equals("success")) {
                            String msgString = jsonObject.getString("msg");
                            if (workflowType.equals("charge")) {
                                //缓存自动回充工作流信息到本地
                                SpUtils.putString(MyApp.getContext(), RECHARGE_SPECIFIC_WORKFLOW, msgString);
                            } else {
                                //缓存引导工作流信息到本地
                                SpUtils.putString(MyApp.getContext(), GUIDE_SPECIFIC_WORKFLOW, msgString);
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * 导航  执行任务
     */
    public void playTask(String commandToText, String commandDoText) {
        JSONArray operationList = new JSONArray();
        JSONArray instructionList = new JSONArray();
        try {
            if (commandToText != null && commandDoText != null) { //先导航,后执行任务
                operationOne = LocalCashUtils.getOperationOne(commandDoText);
                locationOne = LocalCashUtils.getLocationOne(commandToText);
                LogUtils.d("playTask", "开始导航,导航结束开始执行任务:");
                LogUtils.json("playTask", "导航任务信息:" + JSON.toJSONString(locationOne));
                LogUtils.json("playTask", "执行任务信息:" + JSON.toJSONString(operationOne));
                //添加导航点
                JSONObject locationObj = new JSONObject();
                locationObj.put("Type", "Location");
                locationObj.put("InstructionId", locationOne.getF_Id());
                locationObj.put("InstructionName", locationOne.getF_Name());
                locationObj.put("Sort", "1");
                instructionList.put(locationObj);
                //添加操作任务
                JSONObject operation = new JSONObject();
                operation.put("Type", "Operation");
                operation.put("InstructionId", operationOne.getF_id());
                operation.put("InstructionName", operationOne.getF_Name());
                operation.put("Sort", "2");
                instructionList.put(operation);
                initParamToDo(operationList, instructionList);
                //任务种类  2播放视频  3播放pdf  4 播放txt文本  取消指令时需要用
                SpUtils.putString(MyApp.getContext(), "operationType", operationOne.getF_Type());
            } else if (commandDoText != null) { //不导航,执行任务
                operationOne = LocalCashUtils.getOperationOne(commandDoText);
                LogUtils.d("playTask", "开始执行任务:");
                LogUtils.json("playTask", "执行任务信息:" + JSON.toJSONString(operationOne));
                //添加操作任务
                JSONObject operation = new JSONObject();
                operation.put("Type", "Operation");
                operation.put("InstructionId", operationOne.getF_id());
                operation.put("InstructionName", operationOne.getF_Name());
                operation.put("Sort", "1");
                instructionList.put(operation);
                initParamToDo(operationList, instructionList);
                //任务种类  2播放视频  3播放pdf  4 播放txt文本  取消指令时需要用
                SpUtils.putString(MyApp.getContext(), "operationType", operationOne.getF_Type());
            } else if (commandToText != null) { //导航,不执行任务
                locationOne = LocalCashUtils.getLocationOne(commandToText);
                LogUtils.d("playTask", "开始导航:");
                LogUtils.json("playTask", "导航任务信息:" + JSON.toJSONString(locationOne));
                //添加导航点
                JSONObject locationObj = new JSONObject();
                locationObj.put("Type", "Location");
                locationObj.put("InstructionId", locationOne.getF_Id());
                locationObj.put("InstructionName", locationOne.getF_Name());
                locationObj.put("Sort", "1");
                instructionList.put(locationObj);
                initParamToDo(operationList, instructionList);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化参数,发起活动流程
     */
    private void initParamToDo(JSONArray operationList, JSONArray instructionList) throws JSONException {
        //最后添加
        JSONObject operationObj = new JSONObject();
        operationObj.put("InstructionList", instructionList);
        operationObj.put("Sort", "1");
        operationList.put(operationObj);
        LogUtils.d("playTask", "发起活动前拼接参数==>" + operationList.toString());
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
            LogUtils.d("playTask", "拼接参数:info=>" + info.toString());
            BusinessRequest.postStringRequest(info.toString(), ApiRequestUrl.CREATE_ACTION, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LogUtils.e("playTask", ":发起活动流程失败:onFailure=>" + e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String stringResponse = response.body().string();
                    LogUtils.d("playTask", "发起活动流程成功:onResponse=>" + stringResponse);
                    try {
                        JSONObject jsonObject = new JSONObject(stringResponse);
                        if (jsonObject.has("type") && jsonObject.getString("type").equals("success")) {
                            String actionId = jsonObject.getString("msg");
                            //控制端当前活动id本地缓存
                            SpUtils.putString(MyApp.getContext(), GUIDE_WORK_FLOW_ACTION_ID, actionId);
                            //改变当前机器人状态为操作中
                            RobotInfoUtils.setRobotRunningStatus(String.valueOf(3));
                            //更新时间戳
                            BusinessRequest.getServerTime(SyncTimeCallback.syncTimeCallback);
                            LogUtils.d("playTask", "发起活动流程成功:开始获取下一步操作=>");
                            //获取流程中的下一步操作
                            getNextStepInfo(actionId);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据流程id,获取流程中的下一步操作
     *
     * @param actionId 流程id
     */
    private void getNextStepInfo(String actionId) {
        BusinessRequest.getNextStep(actionId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.e("playTask", "MSG_TYPE_TASK:onFailure=>" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogUtils.d("playTask", "获取下一步操作成功==>" + result);
                try {
                    JSONObject baseResObj = new JSONObject(result);
                    if (baseResObj.has("type")) {
                        String type = baseResObj.getString("type");
                        if (type.equals("success")) {
                            String msg = baseResObj.getString("msg");
                            LogUtils.json("playTask", JSON.toJSONString(msg));
                            NextOperationData nextOperationData = JsonUtils.decode(msg, NextOperationData.class);
                            LogUtils.d("playTask", "nextOperationData=>" + JsonUtils.encode(nextOperationData));
                            if (nextOperationData != null) {
                                if (nextOperationData.getOperationType() == null || !nextOperationData.getOperationType().equals("End")) {
                                    SignalDataEvent autoMoveEvent = new SignalDataEvent();
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
                                    LogUtils.d("startSpecialWorkFlow", "nextOperationData.getF_Type()=>" + nextOperationData.getF_Type());
                                    if (nextOperationData.getF_Type().equals("Location")) {
                                        LogUtils.d("playTask", "开始导航=OPERATION_TYPE_AUTO_MOVE==>");
                                        autoMoveEvent.setType(OPERATION_TYPE_AUTO_MOVE);
                                    } else {
                                        LogUtils.d("playTask", "开始执行任务=MSG_UPDATE_INSTARUCTION_STATUS==>");
                                        autoMoveEvent.setType(MSG_UPDATE_INSTARUCTION_STATUS);
                                    }
                                    EventBus.getDefault().post(autoMoveEvent);
                                } else {
                                    SignalDataEvent taskendEvent = new SignalDataEvent();
                                    taskendEvent.setType(MSG_TASK_END);
                                    EventBus.getDefault().post(taskendEvent);
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
