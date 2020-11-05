package com.fitgreat.airfacerobot.launcher.utils;

import android.os.Handler;

import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.business.ApiRequestUrl;
import com.fitgreat.airfacerobot.business.BusinessRequest;
import com.fitgreat.airfacerobot.launcher.model.LocationEntity;
import com.fitgreat.airfacerobot.launcher.model.MapEntity;
import com.fitgreat.airfacerobot.launcher.model.OperationInfo;
import com.fitgreat.airfacerobot.launcher.model.WorkflowEntity;
import com.fitgreat.airfacerobot.remotesignal.model.RobotInfoData;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.SpUtils;

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
import static com.fitgreat.airfacerobot.constants.RobotConfig.GUIDE_SPECIFIC_WORKFLOW;
import static com.fitgreat.airfacerobot.constants.RobotConfig.GUIDE_WORK_FLOW_ACTION_ID;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MAP_INFO_CASH;
import static com.fitgreat.airfacerobot.constants.RobotConfig.RECHARGE_SPECIFIC_WORKFLOW;
import static com.fitgreat.airfacerobot.constants.RobotConfig.START_GUIDE_WORK_FLOW_TAG;

public class OperationUtils {
    private static final String TAG = "OperationUtils";
    private static String msgString;

    /**
     * 发起特殊工作流程
     * workFlowType   1 自动回充   2 引导讲解流程
     */
    public static void startSpecialWorkFlow(int workFlowType) {
        //获取机器人信息
        RobotInfoData robotInfo = RobotInfoUtils.getRobotInfo();
        if (workFlowType == 1) {
            msgString = SpUtils.getString(MyApp.getContext(), RECHARGE_SPECIFIC_WORKFLOW, null);
            //更新自动回充工作流信息
            automaticRechargeWorkflow("charge");
        } else {
            msgString = SpUtils.getString(MyApp.getContext(), GUIDE_SPECIFIC_WORKFLOW, null);
            //更新引导工作流信息
            automaticRechargeWorkflow("guide");
            //引导工作流启动标志
            SpUtils.putBoolean(MyApp.getContext(), START_GUIDE_WORK_FLOW_TAG, true);
        }
        if (msgString != null) {
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
                                if (workFlowType == 2) {
                                    SpUtils.putString(MyApp.getContext(), GUIDE_WORK_FLOW_ACTION_ID, actionId);
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
}
