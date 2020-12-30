package com.fitgreat.airfacerobot.settings.presenter;

import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.business.ApiRequestUrl;
import com.fitgreat.airfacerobot.business.BusinessRequest;
import com.fitgreat.airfacerobot.model.MapEntity;
import com.fitgreat.airfacerobot.model.WorkflowEntity;
import com.fitgreat.airfacerobot.settings.view.SettingsView;
import com.fitgreat.archmvp.base.ui.BasePresenterImpl;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.SpUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MAP_INFO_CASH;


public class SettingsPresenter extends BasePresenterImpl<SettingsView> {
    private static final String TAG = "SettingsPresenter";
    private List<WorkflowEntity> workflowEntityList;

    /**
     * 根据可是id 地图id获取工作流列表
     */
    public void getWorkflowList() {
        workflowEntityList = new ArrayList();
        //医院地图信息
        String mapInfoString = SpUtils.getString(MyApp.getContext(), MAP_INFO_CASH, null);
        if (mapInfoString != null) {
            //解析地图信息获取对象
            MapEntity mapEntity = JSON.parseObject(mapInfoString, MapEntity.class);
            LogUtils.d(DEFAULT_LOG_TAG, "获取空闲时工作流列表");
            ConcurrentHashMap<String, String> param = new ConcurrentHashMap<>();
            param.put("departmentId", mapEntity.getF_DepartmentId());
            param.put("mapId", mapEntity.getF_Id());
            param.put("type", "all");
            LogUtils.json(DEFAULT_LOG_TAG, JSON.toJSONString(param));
            BusinessRequest.getRequestWithParam(param, ApiRequestUrl.WORK_FLOW_LIST, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LogUtils.e(DEFAULT_LOG_TAG, "获取空闲时工作流列表失败: " + e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String stringResponse = response.body().string();
                    LogUtils.d(DEFAULT_LOG_TAG, "获取空闲时工作流列表成功: " + stringResponse);
                    try {
                        JSONObject jsonObject = new JSONObject(stringResponse);
                        if (jsonObject.has("type") && jsonObject.getString("type").equals("success")) {
                            String msgString = jsonObject.getString("msg");
                            JSONArray jsonArray = new JSONArray(msgString);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject workFlowObj = jsonArray.getJSONObject(i);
                                WorkflowEntity workflowEntity = new WorkflowEntity();
                                if (workFlowObj.has("F_Id")) {
                                    workflowEntity.setF_Id(workFlowObj.getString("F_Id"));
                                }
                                if (workFlowObj.has("F_Name")) {
                                    workflowEntity.setF_Name(workFlowObj.getString("F_Name"));
                                }
                                workflowEntityList.add(workflowEntity);
                            }
                            mView.showWorkflowList(workflowEntityList);
                            LogUtils.json(DEFAULT_LOG_TAG, "workflowEntityList::" + JSON.toJSONString(workflowEntityList));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
