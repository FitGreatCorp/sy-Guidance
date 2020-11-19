package com.fitgreat.airfacerobot.commonproblem.presenter;

import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.business.ApiRequestUrl;
import com.fitgreat.airfacerobot.business.BusinessRequest;
import com.fitgreat.airfacerobot.commonproblem.view.CommonProblemView;
import com.fitgreat.airfacerobot.model.CommonProblemEntity;
import com.fitgreat.archmvp.base.ui.BasePresenterImpl;
import com.fitgreat.archmvp.base.util.LogUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CommonProblemPresenter extends BasePresenterImpl<CommonProblemView> {
    private static final String TAG = "CommonProblemPresenter";

    /**
     * 获取操作任务信息
     */
    public void getQuestionList() {
        ConcurrentHashMap<String, String> info = new ConcurrentHashMap<>();
        info.put("hospitalId", RobotInfoUtils.getRobotInfo().getF_HospitalId());
        info.put("floor", "1");
        LogUtils.d(TAG, "拼接参数:info=>" + JSON.toJSONString(info));
        BusinessRequest.getRequestWithParam(info, ApiRequestUrl.COMMON_PROBLEM_LIST, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.e(TAG, "获取常见问题失败:onFailure=>" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String stringResponse = response.body().string();
                LogUtils.d(TAG, "获取常见问题成功:onResponse=>" + stringResponse);
                try {
                    JSONObject jsonObject = new JSONObject(stringResponse);
                    if (jsonObject.has("type") && jsonObject.getString("type").equals("success")) {
                        String msg = jsonObject.getString("msg");
                        if (msg != null && !msg.equals("null")) {
                            List<CommonProblemEntity> commonProblemEntities = JSON.parseArray(msg, CommonProblemEntity.class);
                            LogUtils.json(TAG, JSON.toJSONString(commonProblemEntities));
                            mView.showQuestionList(commonProblemEntities);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
