package com.fitgreat.airfacerobot.launcher.presenter;

import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.business.BusinessRequest;
import com.fitgreat.airfacerobot.launcher.contractview.NormalHomeView;
import com.fitgreat.archmvp.base.ui.BasePresenterImpl;
import com.fitgreat.archmvp.base.util.LogUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import static com.fitgreat.airfacerobot.business.ApiRequestUrl.VERIFY_MODULE_PASSWORD;


public class NormalHomePresenter extends BasePresenterImpl<NormalHomeView> {
    private static final String TAG = "NormalHomePresenter";

    /**
     * 验证设置模块密码
     */
    public void verifyPassword(String password) {
        HashMap<String, String> mapParam = new HashMap();
        mapParam.put("id", RobotInfoUtils.getRobotInfo().getF_Id());
        mapParam.put("pwd", password);
        BusinessRequest.postStringRequest(JSON.toJSONString(mapParam), VERIFY_MODULE_PASSWORD, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.d(TAG, "密码验证接口连接失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                LogUtils.d(TAG, "密码验证接口连接成功: " + responseString);
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    if (jsonObject.has("type") && jsonObject.getString("type").equals("success")) {
                        String msgString = jsonObject.getString("msg");
                        JSONObject msgJsonObject = new JSONObject(msgString);
                        boolean isPass = msgJsonObject.getBoolean("isPass");
                        if (isPass) { //密码验证通过
                            mView.verifySuccess();
                        } else { //密码验证失败
                            mView.verifyFailure();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
