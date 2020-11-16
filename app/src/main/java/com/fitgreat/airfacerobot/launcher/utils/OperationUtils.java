package com.fitgreat.airfacerobot.launcher.utils;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.SyncTimeCallback;
import com.fitgreat.airfacerobot.business.ApiRequestUrl;
import com.fitgreat.airfacerobot.business.BusinessRequest;
import com.fitgreat.airfacerobot.launcher.model.ActionEvent;
import com.fitgreat.airfacerobot.launcher.model.LocationEntity;
import com.fitgreat.airfacerobot.launcher.model.MapEntity;
import com.fitgreat.airfacerobot.launcher.model.NavigationTip;
import com.fitgreat.airfacerobot.launcher.model.OperationInfo;
import com.fitgreat.airfacerobot.launcher.model.WorkflowEntity;
import com.fitgreat.airfacerobot.remotesignal.model.NextOperationData;
import com.fitgreat.airfacerobot.remotesignal.model.RobotInfoData;
import com.fitgreat.airfacerobot.remotesignal.model.SignalDataEvent;
import com.fitgreat.archmvp.base.util.JsonUtils;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.SpUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.fitgreat.airfacerobot.constants.Constants.LOGFILE_CREATE_TIME;
import static com.fitgreat.airfacerobot.constants.Constants.LOG_FILE_PATH;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_WORK_STATUS_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.GUIDE_SPECIFIC_WORKFLOW;
import static com.fitgreat.airfacerobot.constants.RobotConfig.GUIDE_WORK_FLOW_ACTION_ID;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MAP_INFO_CASH;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_UPDATE_INSTARUCTION_STATUS;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PLAY_TASK_PROMPT_INFO;
import static com.fitgreat.airfacerobot.constants.RobotConfig.RECHARGE_SPECIFIC_WORKFLOW;
import static com.fitgreat.airfacerobot.constants.RobotConfig.START_GUIDE_WORK_FLOW_TAG;
import static com.fitgreat.airfacerobot.remotesignal.SignalConfig.OPERATION_TYPE_AUTO_MOVE;

public class OperationUtils {
    private static final String TAG = "OperationUtils";
    private static String msgString;

    /**
     * 发起特殊工作流程
     * workFlowType   1 自动回充 charge   2 引导讲解流程 guide   3 三亚院内介绍流程  introduction-sy
     */
    public static void startSpecialWorkFlow(int workFlowType) {
        //获取机器人信息
        RobotInfoData robotInfo = RobotInfoUtils.getRobotInfo();
        LogUtils.d("startSpecialWorkFlow", "----启动医院介绍工作流程---" + workFlowType);
        if (workFlowType == 1) {
            //自动回充工作流启动标志
            SpUtils.putInt(MyApp.getContext(), CURRENT_WORK_STATUS_TAG, 222);
            //更新自动回充工作流信息
            automaticRechargeWorkflow("charge", robotInfo, workFlowType);
        } else if (workFlowType == 3) {
            LogUtils.d("startSpecialWorkFlow", "启动医院介绍工作流程");
            //医院介绍工作流启动标志
            SpUtils.putInt(MyApp.getContext(), CURRENT_WORK_STATUS_TAG, 111);
            //更新引导工作流信息
            automaticRechargeWorkflow("introduction-sy", robotInfo, workFlowType);
        }
    }

    /**
     * 获取自动回充工作流信息    charge 自动回充
     * guide    引导流程类型
     */
    public static void automaticRechargeWorkflow(String workflowType, RobotInfoData robotInfo, int workFlowTypeInt) {
        //医院地图信息
        String mapInfoString = SpUtils.getString(MyApp.getContext(), MAP_INFO_CASH, null);
        if (mapInfoString != null) {
            //解析地图信息获取对象
            MapEntity mapEntity = JSON.parseObject(mapInfoString, MapEntity.class);
            LogUtils.d("startSpecialWorkFlow", "获取特定工作流");
            HashMap<String, String> param = new HashMap<>();
            param.put("departmentId", mapEntity.getF_DepartmentId());
            param.put("mapId", mapEntity.getF_Id());
            param.put("type", workflowType);
            LogUtils.json("startSpecialWorkFlow", JSON.toJSONString(param));
            BusinessRequest.getRequestWithParam(param, ApiRequestUrl.SPECIFIC_WORK_FLOM, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LogUtils.e(TAG, "获取特定工作流失败: " + e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String stringResponse = response.body().string();
                    LogUtils.d("startSpecialWorkFlow", "获取特定工作流成功: " + stringResponse);
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
                            //根据工作流id发起任务
                            WorkflowEntity workflowEntity = JSON.parseObject(msgString, WorkflowEntity.class);
                            //发起活动流程自动回充
                            LogUtils.d("startSpecialWorkFlow", "发起特殊活动流程\t\t" + msgString);
                            HashMap<String, String> param = new HashMap<>();
                            param.put("hospitalName", robotInfo.getF_Hospital());
                            param.put("departmentName", robotInfo.getF_Department());
                            param.put("hardwareId", robotInfo.getF_HardwareId());
                            param.put("robotAccountId", robotInfo.getF_Id());
                            param.put("promoter", robotInfo.getF_Id());
                            param.put("operationProcedreId", "");
                            param.put("operationName", workflowEntity.getF_Name());
                            param.put("goWhere", "");
                            param.put("doWhat", workflowEntity.getF_Id());
                            LogUtils.json("startSpecialWorkFlow", JSON.toJSONString(param));
                            BusinessRequest.postStringRequest(JSON.toJSONString(param), ApiRequestUrl.INITIATE_ACTION, new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    LogUtils.e(TAG, "发起活动流程失败: " + e.toString());
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String stringResponse = response.body().string();
                                    LogUtils.d("startSpecialWorkFlow", "发起活动流程成功: " + stringResponse);
                                    try {
                                        JSONObject jsonObject = new JSONObject(stringResponse);
                                        if (jsonObject.has("type") && jsonObject.getString("type").equals("success")) {
                                            String actionId = jsonObject.getString("msg");
                                            if (actionId != null) { //活动id存在
                                                if (workFlowTypeInt == 2) {
                                                    SpUtils.putString(MyApp.getContext(), GUIDE_WORK_FLOW_ACTION_ID, actionId);
                                                }
                                                //改变当前机器人状态为操作中
                                                saveRobotstatus(3);
                                                //更新时间戳
                                                BusinessRequest.getServerTime(SyncTimeCallback.syncTimeCallback);
                                                //获取下一步操作
                                                BusinessRequest.getNextStep(actionId, new Callback() {
                                                    @Override
                                                    public void onFailure(Call call, IOException e) {
                                                        LogUtils.e(TAG, "MSG_TYPE_TASK:onFailure=>" + e.toString());
                                                    }

                                                    @Override
                                                    public void onResponse(Call call, Response response) throws IOException {
                                                        String result = response.body().string();
                                                        try {
                                                            JSONObject baseResObj = new JSONObject(result);
                                                            if (baseResObj.has("type")) {
                                                                String type = baseResObj.getString("type");
                                                                if (type.equals("success")) {
                                                                    String msg = baseResObj.getString("msg");
                                                                    LogUtils.d("startSpecialWorkFlow", "MSG_TYPE_TASK:\t\t获取到的下一步任务种类\t\tmsg");
                                                                    LogUtils.json("startSpecialWorkFlow", msg);
                                                                    NextOperationData nextOperationData = JsonUtils.decode(msg, NextOperationData.class);
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
                                                                            int startGuideWorkflowTag = SpUtils.getInt(MyApp.getContext(), CURRENT_WORK_STATUS_TAG, 0);
//                                                                            if (startGuideWorkflowTag == 111) {  //当前启动为引导流程
//                                                                                //语音播报提示
//                                                                                EventBus.getDefault().post(new ActionEvent(PLAY_TASK_PROMPT_INFO, "我将带您参观学术大厅，现在将去的是" + nextOperationData.getF_InstructionName()));
//                                                                                //更新提示信息到首页对话记录
//                                                                                EventBus.getDefault().post(new NavigationTip("我将带您参观学术大厅，现在将去的是" + nextOperationData.getF_InstructionName()));
//                                                                            }
                                                                            LogUtils.d("startSpecialWorkFlow", "MSG_TYPE_TASK:\t\t获取到的下一步任务种类\t\tautoMoveEvent");
                                                                            LogUtils.json("startSpecialWorkFlow", JSON.toJSONString(autoMoveEvent));
                                                                            if (nextOperationData.getF_Type().equals("Operation")) {  //导航移动到某地
                                                                                autoMoveEvent.setType(MSG_UPDATE_INSTARUCTION_STATUS);
                                                                            }
                                                                            EventBus.getDefault().post(autoMoveEvent);
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
                                        } else {
                                            //当前机器人工作流程状态标志初始化,可再次发起引导工作流
                                            SpUtils.putInt(MyApp.getContext(), CURRENT_WORK_STATUS_TAG, 0);
                                            if (workFlowTypeInt == 3) {  //后台没有配置自动引导流程,需要语音提示
                                                playShowText("抱歉，当前引导讲解下没有具体内容，请联系管理员配置.");
                                                new Handler(Looper.getMainLooper()).post(() -> {
                                                    ToastUtils.showSmallToast("抱歉，当前引导讲解下没有具体内容，请联系管理员配置.");
                                                });
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }else {  //后台没有配置医院介绍工作流
                            playShowText("抱歉，当前引导讲解下没有具体内容，请联系管理员配置.");
                            new Handler(Looper.getMainLooper()).post(() -> {
                                ToastUtils.showSmallToast("抱歉，当前引导讲解下没有具体内容，请联系管理员配置.");
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * 播放并首页展示对应文案
     */
    public static void playShowText(String content) {
        EventBus.getDefault().post(new ActionEvent(PLAY_TASK_PROMPT_INFO, content));
        EventBus.getDefault().post(new NavigationTip(content));
    }

    /**
     * 保存异常日志
     */
    public static void saveSpecialLog(String errorType, String errorReason) {
        //当前发生时间
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        //拼接异常日志信息
        StringBuilder stringBuilderError = new StringBuilder();
        stringBuilderError.append(currentTime);
        stringBuilderError.append("\t");
        stringBuilderError.append(errorType);
        stringBuilderError.append("\t");
        stringBuilderError.append(errorReason);
        //判断日志文件创建时间是否为空,为空存取当前时间
        String createTime = SpUtils.getString(MyApp.getContext(), LOGFILE_CREATE_TIME, null);
        LogUtils.d(TAG, "  创建日志文件时间createTime  , " + createTime);
        if (createTime == null) {
            SpUtils.putString(MyApp.getContext(), LOGFILE_CREATE_TIME, currentTime);
        }
        //写入日志到本地
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        File file = new File(LOG_FILE_PATH);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            LogUtils.d(TAG, "  异常日志文件路径 , " + file.getPath());
            //追加内容输出流
            fileWriter = new FileWriter(file, true);
            bufferedWriter = new BufferedWriter(fileWriter);
            //换行输出流
            bufferedWriter.write(stringBuilderError.toString());
            bufferedWriter.newLine();
//            Intent uploadLogIntent = new Intent(AirFaceApp.getContext(), UploadLogService.class);
//            AirFaceApp.getContext().startService(uploadLogIntent);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * 更改机器人状态信息
     *
     * @param status
     */
    private static void saveRobotstatus(int status) {
        //改变当前机器人状态为空闲
        RobotInfoUtils.setRobotRunningStatus(String.valueOf(status));
    }
}
