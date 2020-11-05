package com.fitgreat.archmvp.base.okhttp;


import com.fitgreat.archmvp.base.util.LogUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 业务数据请求管理器<p>
 *
 * @author zixuefei
 * @since 2019/4/8 15:38
 */
public enum HttpRequestClient {
    INSTANCE;
    private static final int DEFAULT_TIMEOUT = 10;
    private OkHttpClient mOkHttpClient;

    HttpRequestClient() {
        OkHttpClient.Builder okhttpBuilder = new OkHttpClient.Builder();
        okhttpBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
//                .cache(new Cache(LwVideoApp.getAppContext().getCacheDir(), 1024 * 1024 * 10))
                .addNetworkInterceptor(new LogInterceptor());

        mOkHttpClient = okhttpBuilder.build();
    }


    public Call createCall(Request request) {
        if (request == null) {
            return null;
        }
        return mOkHttpClient.newCall(request);
    }

//    public void startHttpRequest(final Call call, final HttpCallback httpStateCallback) {
//        if (call == null || httpStateCallback == null) {
//            LogUtils.e("harris", "call or httpStateCallback is null");
//            return;
//        }
//        mExecutorService.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    if (!call.isExecuted()) {
//                        CommonResponse response = call.execute();
//                        LogUtils.d("harris", " response:" + response.code());
//                        HttpResult result = new HttpResult();
//                        result.setType(response.code());
//                        result.setBody(response.body().string());
//                        result.setUrl(call.request().url().toString());
//                        httpStateCallback.onRequestSuccess(result);
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    httpStateCallback.onRequestError(e);
//                }
//            }
//        });
//    }

    public void startHttpRequest(Call call, Callback callback) {
        if (call == null || callback == null) {
            LogUtils.e("harris", "call or callback is null");
            return;
        }
        if (!call.isExecuted()) {
            call.enqueue(callback);
        }
    }

    public void cancelHttpRequest(Call call) {
        if (call == null) {
            return;
        }
        if (!call.isCanceled()) {
            call.cancel();
        }
    }
}