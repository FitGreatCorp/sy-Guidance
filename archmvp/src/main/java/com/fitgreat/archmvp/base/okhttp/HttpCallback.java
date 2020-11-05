package com.fitgreat.archmvp.base.okhttp;


import com.fitgreat.archmvp.base.util.JsonUtils;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 网络请求回调接口后台线程<p>
 *
 * @author zixuefei
 * @since 2019/4/23 16:40
 */
public abstract class HttpCallback implements Callback {
    private final String TAG = HttpCallback.class.getSimpleName();

    @Override
    public final void onFailure(Call call, IOException e) {
        onFailed("IOException:" + e.getMessage());
    }

    @Override
    public final void onResponse(Call call, final Response response) {
        LogUtils.i(TAG, "response code:" + response.code() + " message:" + response.message());
        if (response.code() == 500) {
            onFailed("server error:500");
            return;
        }
        try {
            final String result = response.body().string();
            LogUtils.d(TAG, "result:" + result);
            onResult(JsonUtils.decode(result, BaseResponse.class));
        } catch (JsonSyntaxException e) {
            onFailed("JsonSyntaxException:" + e.getMessage());
        } catch (IOException e) {
            onFailed("IOException:" + e.getMessage());
        }
    }

    public void onFailed(String e) {
        LogUtils.e(TAG, "onFailed:" + e);
    }

    public abstract void onResult(BaseResponse baseResponse);
}
