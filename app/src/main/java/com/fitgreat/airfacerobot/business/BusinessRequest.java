package com.fitgreat.airfacerobot.business;

import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.remotesignal.model.RobotInfoData;
import com.fitgreat.airfacerobot.versionupdate.VersionUtils;
import com.fitgreat.archmvp.base.util.JsonUtils;
import com.fitgreat.archmvp.base.util.LogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * 业务数据请求接口类<p>
 *
 * @author zixuefei
 * @since 2019/4/4 14:38
 */
public class BusinessRequest {
    private final static String TAG = BusinessRequest.class.getSimpleName();

    /**
     * check app version
     */
    public static void checkAppVersion(Callback callback) {
        HashMap<String, String> bodyStr = new HashMap<>();
        bodyStr.put("appCode", "");
        bodyStr.put("appType", "android control");
        bodyStr.put("versionCode", VersionUtils.getVersionName(MyApp.getContext()));
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JsonUtils.encode(bodyStr));
        LogUtils.d(TAG, "checkAppVersion body:" + JsonUtils.encode(bodyStr));
        RequestManager.startPost(ApiRequestUrl.CHECK_APP_VERSION, body, callback);
    }

    /**
     * check hardware version
     */
    public static void checkHardwareVersion(Callback callback) {
        HashMap<String, String> bodyStr = new HashMap<>();
        bodyStr.put("appType", "android_airface_robot_hardware");
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JsonUtils.encode(bodyStr));
        RequestManager.startPost(ApiRequestUrl.CHECK_HARDWARE_VERSION, body, callback);
    }

    /**
     * 硬件升级结果反馈
     */
    public static void updateHardwareResult(String f_step, String F_StepId, String status, String msg, Callback callback) {
        LogUtils.d("updateHardwareResult", "RobotInfoUtils.getAirFaceDeviceId() = ========== " + RobotInfoUtils.getAirFaceDeviceId());
        HashMap<String, String> bodyStr = new HashMap<>();
        bodyStr.put("F_HardwareId", RobotInfoUtils.getRobotInfo().getF_HardwareId());
        bodyStr.put("F_Step", f_step);
        bodyStr.put("F_StepId", F_StepId);
        bodyStr.put("F_State", status);
        bodyStr.put("F_Message", msg);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JsonUtils.encode(bodyStr));
        LogUtils.d("updateHardwareResult", "updateHardwareResult body:" + JsonUtils.encode(bodyStr));
        RequestManager.startPost(ApiRequestUrl.HARDWARE_UPDATE_RESULT, body, callback);
    }


    /**
     * 获取token接口
     */
    public static void getAccessToken(Callback callback) {
        //创建参数
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("grant_type", "client_credentials")
                .addFormDataPart("scope", "basic");
        RequestManager.startRobotPost(ApiRequestUrl.ROBOT_AUTH, builder.build(), callback);
        LogUtils.d("ApiRequestUrl.ROBOT_AUTH==>" + ApiRequestUrl.ROBOT_AUTH);
    }

    /**
     * 获取机器人注册信息
     */
    public static void getRobotInfo(String deviceId, Callback callback) {
        HashMap<String, String> bodyStr = new HashMap<>();
        bodyStr.put("robotId", deviceId);
        bodyStr.put("hardwareId", deviceId);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JsonUtils.encode(bodyStr));
        LogUtils.d(TAG, "getRobotInfo body:" + JsonUtils.encode(bodyStr));
        RequestManager.startPost(ApiRequestUrl.ROBOT_INFO, body, callback);
    }

    /**
     * 根据uid获取成员信息
     *
     * @param uid
     */
    public static void getUserInfo(int uid, Callback callback) {
        RequestManager.startGet(ApiRequestUrl.GET_USERINFO_BY_ID + "/" + uid, callback);
    }

    /**
     * 同步服务器时间
     */
    public static void getServerTime(Callback callback) {
        HashMap<String, String> bodyStr = new HashMap<>();
        bodyStr.put("client_t1", String.valueOf(System.currentTimeMillis()));
        bodyStr.put("server_t2", "");
        bodyStr.put("server_t3", "");
        bodyStr.put("server_t4", "");

        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JsonUtils.encode(bodyStr));
        LogUtils.d(TAG, "getServerTime body:" + JsonUtils.encode(bodyStr));
        RequestManager.startPost(ApiRequestUrl.SYNC_SERVER_TIME, body, callback);
    }

    /**
     * 获取下一步操作
     *
     * @param operationProcedureId
     */
    public static void getNextStep(String operationProcedureId, Callback callback) {
        RobotInfoData robotInfoData = RobotInfoUtils.getRobotInfo();
        if (robotInfoData != null) {
            HashMap<String, String> bodyStr = new HashMap<>();
            bodyStr.put("operationProcedureId", operationProcedureId);
            bodyStr.put("robotAccountId", robotInfoData.getF_Id());
            RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JsonUtils.encode(bodyStr));
            RequestManager.startPost(ApiRequestUrl.GET_NEXT_STEP, body, callback);
        }
    }


    /**
     * 更新任务指令状态
     *
     * @param instructionId 指令ID
     * @param status        状态ID   （-1异常 0未开始 1开始 2结束 3终止 4取消）
     */
    public static void UpdateInstructionStatue(String instructionId, String status, Callback callback) {
        RobotInfoData robotInfoData = RobotInfoUtils.getRobotInfo();
        if (robotInfoData != null) {
            HashMap<String, String> bodyStr = new HashMap<>();
            bodyStr.put("instructionId", instructionId);
            bodyStr.put("instructionStatue", status);
            bodyStr.put("userId", robotInfoData.getF_Id());
            RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JsonUtils.encode(bodyStr));
            LogUtils.json("playmission", "UpdateInstructionStatue body:" + JsonUtils.encode(bodyStr));
            RequestManager.startPost(ApiRequestUrl.UPDATE_INSTRUCTION, body, callback);
        }
    }

    /**
     * 强行终止任务
     */
    public static void stopOperateTask(String OperationProcedureId, String statue, Callback callback) {
        RobotInfoData robotInfoData = RobotInfoUtils.getRobotInfo();
        if (robotInfoData != null) {
            HashMap<String, String> bodyStr = new HashMap<>();
            bodyStr.put("operationProcedureId", OperationProcedureId);
            bodyStr.put("status", statue);
            bodyStr.put("userId", robotInfoData.getF_Id());

            RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JsonUtils.encode(bodyStr));
            LogUtils.d(TAG, "stopOperateTask body:" + JsonUtils.encode(bodyStr));
            RequestManager.startPost(ApiRequestUrl.STOP_OPERATE_TASK, body, callback);
        }
    }


    /**
     * 定时同步信息到服务器
     * 机器人状态（0:停机，1:空闲,2:移动中，3：执行操作中,4:视频，5:充电中, 6:升级中）
     */
    public static void updateRobotState(String power, String statue, Callback callback) {
        RobotInfoData robotInfoData = RobotInfoUtils.getRobotInfo();
        LogUtils.d(TAG, "robotId = " + RobotInfoUtils.getRobotInfo());
        if (robotInfoData != null) {
            HashMap<String, String> bodyStr = new HashMap<>();
            bodyStr.put("deviceId", robotInfoData.getF_Id());
            bodyStr.put("F_AppPower", "");                 //app电池电量
            bodyStr.put("F_Status", statue);               //运行状态
            bodyStr.put("F_Temperature", "");   //温度
            bodyStr.put("F_Power", power);               //设备电量
            bodyStr.put("F_Angle", "");               //仰角
            bodyStr.put("F_Height", "");             //高度
            bodyStr.put("F_Exp", "false");                   //是否充电
            bodyStr.put("F_Exp1", "false");                 //是否急停
            bodyStr.put("F_Exp2", "false");                 //是否手动充电
            bodyStr.put("F_Exp3", "false");                 //是否正在移动

            RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JsonUtils.encode(bodyStr));
            LogUtils.d(TAG, "updateRobotState body:" + JsonUtils.encode(bodyStr));
            RequestManager.startPost(ApiRequestUrl.UPLOAD_ROBOT_STATUS, body, callback);
        }
    }

    /**
     * 获取声网token
     *
     * @param robotaccount
     * @param channel
     * @param callback
     */
    public static void getAgoraToken(String robotaccount, String channel, Callback callback) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("userAccount", robotaccount)
                .addFormDataPart("channelName", channel);
        RequestManager.startAgoraPost(ApiRequestUrl.AGORA_LOGIN, builder.build(), callback);
    }

    /**
     * 绑定声网UId
     *
     * @param groupname
     * @param type
     * @param uid
     * @param userId
     */
    public static void bindUid(String groupname, String type, String uid, String userId, Callback callback) {
        HashMap<String, String> bodyStr = new HashMap<>();
        bodyStr.put("groupName", groupname);
        bodyStr.put("type", type);
        bodyStr.put("uid", uid);
        bodyStr.put("userId", userId);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JsonUtils.encode(bodyStr));
        RequestManager.startPost(ApiRequestUrl.BIND_UID, body, callback);
    }

    /**
     * 更新机器人位置信息
     *
     * @param x
     * @param y
     * @param z
     * @param callback
     */
    public static void updateRobotPosition(String x, String y, String z, Callback callback) {
        RobotInfoData robotInfoData = RobotInfoUtils.getRobotInfo();
        HashMap<String, String> bodyStr = new HashMap<>();
        bodyStr.put("hardwardId", robotInfoData.getF_HardwareId());
        bodyStr.put("x", x);
        bodyStr.put("y", y);
        bodyStr.put("z", z);
        bodyStr.put("userId", robotInfoData.getF_Id());
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JsonUtils.encode(bodyStr));
        RequestManager.startPost(ApiRequestUrl.UPDATE_POSITION, body, callback);

    }

    public static void createAction(JSONArray operationList, String userId, Callback callback) {
        RobotInfoData robotInfoData = RobotInfoUtils.getRobotInfo();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("HardwareId", robotInfoData.getF_HardwareId());
            jsonObject.put("RobotAccountId", robotInfoData.getF_Id());
            jsonObject.put("Hospital", robotInfoData.getF_Hospital());
            jsonObject.put("Department", robotInfoData.getF_Department());
            jsonObject.put("OperationProcedureId", "");
            jsonObject.put("F_Promoter", userId);
            jsonObject.put("OperationList", operationList);

            HashMap<String, String> bodyStr = new HashMap<>();
            bodyStr.put("Info", jsonObject.toString());
            String json = JsonUtils.encode(bodyStr);
            json = json.replace("\"{", "{");
            json = json.replace("}\"", "}");
            json = json.replace("\\", "");
            RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json);
            LogUtils.d(TAG, "json = " + json);
            RequestManager.startPost(ApiRequestUrl.CREATE_TASK, body, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void updateOPS(String operationProcedureId, String status, Callback callback) {
        HashMap<String, String> bodyStr = new HashMap<>();
        bodyStr.put("operationProcedureId", operationProcedureId);
        bodyStr.put("status", status);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JsonUtils.encode(bodyStr));
        RequestManager.startPost(ApiRequestUrl.UPDATE_OPERATION_STATUS, body, callback);
    }

    /**
     * post请求,所传参数以json的形式上传
     */
    public static void postStringRequest(String parameterJson, String url, Callback callback) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), parameterJson);
        RequestManager.startPost(url, requestBody, callback);
    }

    /**
     * post请求,所传参数以form表单的形式上传
     */
    public static void postFormRequest(ConcurrentHashMap<String, String> paramsMap, String url, Callback callback) {
        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            //追加表单信息
            builder.add(key, paramsMap.get(key));
        }
        RequestManager.startPostForm(url, builder.build(), callback);
    }

    /**
     * post请求,上传键值对,上传文件
     */
    public static void postFormAndFileRequest(ConcurrentHashMap<String, String> paramsMap, String url, File file, Callback callback) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (String key : paramsMap.keySet()) {
            //追加表单信息
            builder.addFormDataPart(key, paramsMap.get(key));
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/pain"), file);
        builder.addFormDataPart(file.getName(), file.getName(), requestBody);
        RequestManager.startPostFormAndFile(url, builder.build(), callback);
    }

    /**
     * get请求带参数
     */
    public static void getRequestWithParam(ConcurrentHashMap<String, String> paramsMap, String url, Callback callback) {
        RequestManager.startGet(attachHttpGetParams(url, paramsMap), callback);
    }

    /**
     * get请求不带参数
     */
    public static void getRequestWithOutParam(String url, Callback callback) {
        RequestManager.startGet(url, callback);
    }

    /**
     * get请求拼接参数到url尾部
     *
     * @param url
     * @param params
     * @return
     */
    public static String attachHttpGetParams(String url, ConcurrentHashMap<String, String> params) {
        Iterator<String> keys = params.keySet().iterator();
        Iterator<String> values = params.values().iterator();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("?");
        for (int i = 0; i < params.size(); i++) {
            String value = null;
            try {
                value = URLEncoder.encode(values.next(), "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            stringBuffer.append(keys.next() + "=" + value);
            if (i != params.size() - 1) {
                stringBuffer.append("&");
            }
            LogUtils.d(TAG, "stringBuffer", stringBuffer.toString());
        }
        return url + stringBuffer.toString();
    }
}
