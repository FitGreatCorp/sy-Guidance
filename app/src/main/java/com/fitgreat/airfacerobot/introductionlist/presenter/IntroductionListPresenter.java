package com.fitgreat.airfacerobot.introductionlist.presenter;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.SyncTimeCallback;
import com.fitgreat.airfacerobot.business.ApiRequestUrl;
import com.fitgreat.airfacerobot.business.BusinessRequest;
import com.fitgreat.airfacerobot.introductionlist.view.IntroductionListView;
import com.fitgreat.airfacerobot.launcher.utils.ToastUtils;
import com.fitgreat.airfacerobot.model.CommonProblemEntity;
import com.fitgreat.airfacerobot.model.OperationInfo;
import com.fitgreat.airfacerobot.remotesignal.model.NextOperationData;
import com.fitgreat.airfacerobot.remotesignal.model.RobotInfoData;
import com.fitgreat.airfacerobot.remotesignal.model.SignalDataEvent;
import com.fitgreat.archmvp.base.ui.BasePresenterImpl;
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

import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_UPDATE_INSTARUCTION_STATUS;


public class IntroductionListPresenter extends BasePresenterImpl<IntroductionListView> {
    private static final String TAG = "IntroductionListPresenter";
    private List<OperationInfo> operationInfoList = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());

    /**
     * 获取操作任务集合(只包含播放宣教视频,pdf,text文本)
     */
    public void getIntroductionOperationList() {
        HashMap<String, String> parameterMap = new HashMap<>();
        parameterMap.put("hospitalId", RobotInfoUtils.getRobotInfo().getF_HospitalId());
        BusinessRequest.postStringRequest(JSON.toJSONString(parameterMap), ApiRequestUrl.GET_OPERATION_LIST_V, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.e(DEFAULT_LOG_TAG, "onError:获取执行任务列表信息失败-->" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String stringResponse = response.body().string();
                LogUtils.d(DEFAULT_LOG_TAG, "获取院内介绍任务列表数据成功-->" + StringEscapeUtils.unescapeJava(stringResponse));
                try {
                    JSONObject msbObj = new JSONObject(stringResponse);
                    String type = msbObj.getString("type");
                    String msgString = msbObj.getString("msg");
//                    LogUtils.json(DEFAULT_LOG_TAG, msgString);
                    if (type.equals("success")) {
                        handOperationCash(msgString);
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
        if (operationInfoList.size() > 0) {
            operationInfoList.clear();
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
            if (operationObj.has("F_EName")) {
                operationInfo.setF_EName(operationObj.getString("F_EName"));
            }
            if (operationObj.has("F_DescImg")) {
                operationInfo.setF_DescImg(operationObj.getString("F_DescImg"));
            }
            if (operationObj.has("F_EFileUrl")) {
                operationInfo.setF_EFileUrl(operationObj.getString("F_EFileUrl"));
            }
            if (operationObj.has("F_DescImg")) {
                operationInfo.setF_DescImg(operationObj.getString("F_DescImg"));
            }
            operationInfoList.add(operationInfo);
        }
        handler.post(() -> {
            mView.showIntroductionOperationList(operationInfoList);
        });
    }

    /**
     * 发起操作任务
     */
    public void startOperationTask(OperationInfo operationInfo) {
        JSONArray operationList = new JSONArray();
        JSONArray instructionList = new JSONArray();
        //添加操作任务
        JSONObject operation = new JSONObject();
        try {
            operation.put("Type", "Operation");
            operation.put("InstructionId", operationInfo.getF_id());
            operation.put("InstructionName", operationInfo.getF_Name());
            operation.put("Sort", "1");
            instructionList.put(operation);
            JSONObject operationObj = new JSONObject();
            operationObj.put("InstructionList", instructionList);
            operationObj.put("Sort", "1");
            operationList.put(operationObj);
        } catch (JSONException e) {
            e.printStackTrace();
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
            LogUtils.d(DEFAULT_LOG_TAG, "发起院内介绍操作任务");
            LogUtils.d(DEFAULT_LOG_TAG, "拼接参数:info=>" + info.toString());
            BusinessRequest.postStringRequest(info.toString(), ApiRequestUrl.CREATE_ACTION, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LogUtils.e(DEFAULT_LOG_TAG, ":发起活动流程失败:onFailure=>" + e.toString());
                    //隐藏操作任务提示弹窗
                    mView.hideTipDialog();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String stringResponse = response.body().string();
                    LogUtils.d(DEFAULT_LOG_TAG, "发起活动流程成功:onResponse=>" + stringResponse);
                    try {
                        JSONObject jsonObject = new JSONObject(stringResponse);
                        if (jsonObject.has("type") && jsonObject.getString("type").equals("success")) {
                            String actionId = jsonObject.getString("msg");
                            //改变当前机器人状态为操作中
                            RobotInfoUtils.setRobotRunningStatus(String.valueOf(3));
                            //更新时间戳
                            BusinessRequest.getServerTime(SyncTimeCallback.syncTimeCallback);
                            //获取流程中的下一步操作
                            getNextStepInfo(actionId);
                        } else {
                            //隐藏操作任务提示弹窗
                            mView.hideTipDialog();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        //隐藏操作任务提示弹窗
                        mView.hideTipDialog();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            //隐藏操作任务提示弹窗
            mView.hideTipDialog();
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
                //隐藏操作任务提示弹窗
                mView.hideTipDialog();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogUtils.d(DEFAULT_LOG_TAG, "获取下一步操作成功==>" + result);
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
                                //隐藏操作任务提示弹窗
                                mView.hideTipDialog();
                                //院内介绍为pdf文件播放时,判断文件格式不为pdf格式时添加提示
                                if (!TextUtils.isEmpty(nextOperationData.getF_FileUrl())) {
                                    if (nextOperationData.getF_FileUrl().equals(".pdf") && nextOperationData.getOperationType().equals("3")) {
                                        ToastUtils.showSmallToast("pdf文件格式不对");
                                        return;
                                    }
                                }
                                LogUtils.d(DEFAULT_LOG_TAG, "nextOperationData.getF_Type()=>" + nextOperationData.getF_Type());
                                autoMoveEvent.setType(MSG_UPDATE_INSTARUCTION_STATUS);
                                EventBus.getDefault().post(autoMoveEvent);
                            }
                        }
                    } else {
                        //隐藏操作任务提示弹窗
                        mView.hideTipDialog();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    //隐藏操作任务提示弹窗
                    mView.hideTipDialog();
                }
            }
        });
    }
}
