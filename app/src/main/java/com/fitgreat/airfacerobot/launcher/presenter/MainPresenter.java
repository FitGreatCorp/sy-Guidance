package com.fitgreat.airfacerobot.launcher.presenter;

import android.content.Context;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import androidx.annotation.RequiresApi;
import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.business.ApiRequestUrl;
import com.fitgreat.airfacerobot.business.BusinessRequest;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.launcher.contractview.MainView;
import com.fitgreat.airfacerobot.model.CommonProblemEntity;
import com.fitgreat.airfacerobot.model.DaemonEvent;
import com.fitgreat.airfacerobot.model.LocationEntity;
import com.fitgreat.airfacerobot.model.MapEntity;
import com.fitgreat.airfacerobot.model.OperationInfo;
import com.fitgreat.airfacerobot.model.RobotSignalEvent;
import com.fitgreat.airfacerobot.remotesignal.model.RobotInfoData;
import com.fitgreat.airfacerobot.versionupdate.DownloadUtils;
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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.Constants.basePath;
import static com.fitgreat.airfacerobot.constants.Constants.currentChineseMapPath;
import static com.fitgreat.airfacerobot.constants.Constants.currentEnglishMapPath;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MAP_INFO_CASH;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_ROS_NEXT_STEP;
import static com.fitgreat.airfacerobot.constants.RobotConfig.RECHARGE_OPERATION_INFO;


/**
 * 启动页数据接口<p>
 */
public class MainPresenter extends BasePresenterImpl<MainView> {
    private List<LocationEntity> locationList = new ArrayList<>();
    private List<OperationInfo> operationInfosList = new ArrayList<>();
    private static final String TAG = "MainPresenter";

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
        LogUtils.json(DEFAULT_LOG_TAG, "机器人信息==>" + StringEscapeUtils.unescapeJava(JsonUtils.encode(robotInfo)));
        if (robotInfo != null) {
            //设置参数
            HashMap<String, String> parameterMap = new HashMap<>();
            parameterMap.put("hardwareId", robotInfo.getF_HardwareId());
            //发起请求
            BusinessRequest.postStringRequest(JSON.toJSONString(parameterMap), ApiRequestUrl.GET_ONE_MAP, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LogUtils.e(DEFAULT_LOG_TAG, "获取地图,导航位置信息失败-->" + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String stringResponse = response.body().string();
                    LogUtils.d(DEFAULT_LOG_TAG, "获取地图,导航位置信息成功");
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
                LogUtils.e(TAG, "onError:获取执行任务列表信息失败-->" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String stringResponse = response.body().string();
//                LogUtils.json(DEFAULT_LOG_TAG, "获取执行任务列表信息成功-->" + StringEscapeUtils.unescapeJava(stringResponse));
                try {
                    JSONObject msbObj = new JSONObject(stringResponse);
                    String type = msbObj.getString("type");
                    String msgString = msbObj.getString("msg");
//                    LogUtils.json(DEFAULT_LOG_TAG, msgString);
                    if (type.equals("success")) {
                        handOperationCash(msgString);
                    } else {
                        mView.getOperationListFailure(msgString);
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
                locationEntity.setF_X(locationObj.getString("F_X"));
            }
            if (locationObj.has("F_Y")) {
                locationEntity.setF_Y(locationObj.getString("F_Y"));
            }
            if (locationObj.has("F_Z")) {
                locationEntity.setF_Z(locationObj.getString("F_Z"));
            }
            if (locationObj.has("F_Memo")) {
                locationEntity.setF_Memo(locationObj.getString("F_Memo"));
            }
            //显示设计完的地图上点坐标
            if (locationObj.has("S_X")) {
                locationEntity.setS_X(locationObj.getString("S_X"));
            }
            if (locationObj.has("S_Y")) {
                locationEntity.setS_Y(locationObj.getString("S_Y"));
            }
            //导航点位置英文名字
            if (locationObj.has("F_EName")) {
                locationEntity.setF_EName(locationObj.getString("F_EName"));
            }
            locationList.add(locationEntity);
        }
        //获取地图信息
        String mapString = msgObj.getString("map");
        LogUtils.d(DEFAULT_LOG_TAG, "医院地图信息:  " + mapString);
        //缓存地图信息到本地
        SpUtils.putString(MyApp.getContext(), MAP_INFO_CASH, mapString);
        //解析获取地图信息
        MapEntity mapEntity = JSON.parseObject(mapString, MapEntity.class);
        //下载地图文件到本地
        File parentFile = new File(basePath);
        if (!parentFile.exists()) {
            parentFile.mkdir();
        }
        //下载最新中文地图
        downloadLatestMap(currentChineseMapPath, "中文地图下载: ", mapEntity.getF_MapFileUrl());
        //下载英文地图
        downloadLatestMap(currentEnglishMapPath, "英文地图下载: ", mapEntity.getF_EMapUrl());
        //缓存导航地点信息以json的形式到本地
        SpUtils.putString(MyApp.getContext(), "locationList", JSON.toJSONString(locationList));
        //获取常见问题列表问题
       if (!TextUtils.isEmpty(mapEntity.getF_Floor())&&(!TextUtils.isEmpty(RobotInfoUtils.getRobotInfo().getF_HospitalId()))){
           ConcurrentHashMap<String, String> info = new ConcurrentHashMap<>();
           info.put("hospitalId", RobotInfoUtils.getRobotInfo().getF_HospitalId());
           info.put("floor", mapEntity.getF_Floor());
           LogUtils.d(DEFAULT_LOG_TAG, "拼接参数:info=>" + JSON.toJSONString(info));
           BusinessRequest.getRequestWithParam(info, ApiRequestUrl.COMMON_PROBLEM_LIST, new Callback() {
               @Override
               public void onFailure(Call call, IOException e) {
                   LogUtils.e(DEFAULT_LOG_TAG, "获取常见问题失败:onFailure=>" + e.toString());
               }

               @Override
               public void onResponse(Call call, Response response) throws IOException {
                   String stringResponse = response.body().string();
                   LogUtils.d(DEFAULT_LOG_TAG, "获取常见问题成功:onResponse=>" + stringResponse);
                   try {
                       JSONObject jsonObject = new JSONObject(stringResponse);
                       if (jsonObject.has("type") && jsonObject.getString("type").equals("success")) {
                           String msg = jsonObject.getString("msg");
                           if (msg != null && !msg.equals("null")) {
                               List<CommonProblemEntity> commonProblemEntities = JSON.parseArray(msg, CommonProblemEntity.class);
//                               LogUtils.json(DEFAULT_LOG_TAG, JSON.toJSONString(commonProblemEntities));
                               //保存常见问题到本地
                               SpUtils.putString(MyApp.getContext(), "problemList", msg);
                           }
                       }
                   } catch (JSONException e) {
                       e.printStackTrace();
                   }
               }
           });
       }else {
           SpUtils.putString(MyApp.getContext(), "problemList", null);
       }
    }

    /**
     * 下载最新地图到本地
     */
    private void downloadLatestMap(String currentMapPath, String s, String f_eMapUrl) {
        File currentMapFile = new File(currentMapPath);
        if (currentMapFile.exists()) {
            currentMapFile.delete();
        }
        LogUtils.d(DEFAULT_LOG_TAG, s + f_eMapUrl);
        downloadMap(f_eMapUrl, currentMapPath);
    }

    /**
     * 下载地图到本地
     *
     * @param downloadMapUrl 下载地图url
     * @param saveMapPath    本地保存地图文件路径
     */
    private void downloadMap(String downloadMapUrl, String saveMapPath) {
        DownloadUtils.download(downloadMapUrl, saveMapPath, false, new DownloadUtils.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(String filePath) {
                LogUtils.d(DEFAULT_LOG_TAG, "地图下载成功:" + filePath);
            }

            @Override
            public void onDownloading(int progress) {
            }

            @Override
            public void onDownloadFailed(Exception e) {
                LogUtils.e(DEFAULT_LOG_TAG, "地图下载失败:" + e.getMessage());
            }
        });
    }
}
