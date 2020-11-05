package com.fitgreat.airfacerobot.launcher.service;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.business.BusinessRequest;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.SpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.fitgreat.airfacerobot.business.ApiRequestUrl.UPLOAD_LOCAL_LOG;
import static com.fitgreat.airfacerobot.constants.Constants.LOGFILE_CREATE_TIME;
import static com.fitgreat.airfacerobot.constants.Constants.LOG_FILE_PATH;

/**
 * 上传异常日志服务  每周日24：00上传一次本周异常日志。
 */
public class UploadLogService extends IntentService {
    private static final String TAG = "UploadLogService";

    public UploadLogService() {
        super("UploadLogService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        File uploadLogFile = new File(LOG_FILE_PATH);
        if (uploadLogFile.exists()) {
            HashMap<String, String> parms = new HashMap<>();
            parms.put("containerName", RobotInfoUtils.getRobotInfo().getF_Id());
            BusinessRequest.postFormAndFileRequest(parms, UPLOAD_LOCAL_LOG, uploadLogFile, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseString = response.body().string();
                    LogUtils.d(TAG, "异常日志上传成功: " + responseString);
                    try {
                        JSONObject jsonObject = new JSONObject(responseString);
                        if (jsonObject.has("type") && jsonObject.getString("type").equals("success")) {
                            //上传成功后删除本地日志文件
                            uploadLogFile.delete();
                            //清空创建原日志文件时间
                            SpUtils.putString(MyApp.getContext(),LOGFILE_CREATE_TIME,null);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }
}
