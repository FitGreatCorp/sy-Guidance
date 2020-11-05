package com.fitgreat.archmvp.base.okhttp;

import android.os.Handler;
import android.os.Looper;

import com.fitgreat.archmvp.base.util.JsonUtils;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 网络请求回调接口主线程<p>
 *
 * @author zixuefei
 * @since 2019/4/26 14:39
 */
public abstract class HttpMainCallback implements Callback {
    private final String TAG = HttpMainCallback.class.getSimpleName();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public final void onFailure(Call call, IOException e) {
        handler.post(() -> {
            onFailed("IOException:" + e.getMessage());
        });
    }

    @Override
    public final void onResponse(Call call, final Response response) throws IOException {
        final String result = response.body().string();
        if (response.code() == 500) {
            onFailed("server error:500");
            return;
        }
        handler.post(() -> {
            try {
                LogUtils.d(TAG, "response code:" + response.code() + " message:" + result);
                onResult(JsonUtils.decode(result, BaseResponse.class));
            } catch (JsonSyntaxException e) {
                onFailed("JsonSyntaxException:" + e.getMessage());
            }
        });
    }

    public void onFailed(String e) {
        LogUtils.e(TAG, "onFailed:" + e);
    }

    public abstract void onResult(BaseResponse baseResponse);
}
