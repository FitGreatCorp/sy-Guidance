package com.fitgreat.airfacerobot;

import com.fitgreat.airfacerobot.videocall.VideoCallConstant;
import com.fitgreat.archmvp.base.util.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * class des<p>
 *
 * @author dengj
 * @since 2020-05-18 10:07
 */
public class SyncTimeCallback {

    private static final String TAG = "SyncTimeCallback";
    public static final Callback syncTimeCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            LogUtils.d(TAG, "onFailure e : " + e.toString());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String res = response.body().string();
            try {
                JSONObject jsonObject = new JSONObject(res);
                if (jsonObject.has("type")) {
                    String type = jsonObject.getString("type");
                    String msg = "";
                    if (type.equals("success")) {
                        msg = jsonObject.getString("msg");
                        LogUtils.d(TAG, "msg = " + msg);
                        JSONObject time = new JSONObject(msg);
                        long client_t1 = 0, server_t2 = 0, server_t3 = 0, client_t4 = 0;
                        if (time.has("client_t1")) {
                            client_t1 = time.getLong("client_t1");
                        }
                        if (time.has("server_t2")) {
                            server_t2 = time.getLong("server_t2");
                        }
                        if (time.has("server_t3")) {
                            server_t3 = time.getLong("server_t3");
                        }
                        if (time.has("client_t4")) {
                            client_t4 = time.getLong("client_t4");
                        }
                        VideoCallConstant.sTimeDiff = ((server_t2 - client_t1) + (server_t3 - System.currentTimeMillis())) / 2;
                        LogUtils.d(TAG, "VideoCallConstant.sTimeDiff =========" + VideoCallConstant.sTimeDiff);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
