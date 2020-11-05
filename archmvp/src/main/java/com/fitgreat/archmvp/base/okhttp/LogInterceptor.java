package com.fitgreat.archmvp.base.okhttp;


import com.fitgreat.archmvp.BuildConfig;
import com.fitgreat.archmvp.base.util.LogUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 网络请求拦截器<p>
 *
 * @author zixuefei
 * @since 2019/4/4 14:54
 */
public class LogInterceptor implements Interceptor {
    private final String TAG = LogInterceptor.class.getSimpleName();

    @Override
    public Response intercept(Chain chain) throws IOException {
        // 拦截请求，获取到该次请求的request
        Request request = chain.request();
        LogUtils.d(TAG,"Interceptor:url==>"+request.url().toString());
        // 执行本次网络请求操作，返回response信息
        Response response = chain.proceed(request);
        if (BuildConfig.DEBUG) {
            debug(request, response);
            // 注意，这样写，等于重新创建Request，获取新的Response，避免在执行以上代码时，
            // 调用了responseBody.string()而不能在返回体中再次调用。
        }
        return response.newBuilder().build();
    }

    private void debug(Request request, Response response) throws IOException {
        StringBuffer buffer = new StringBuffer();
        buffer.append("#################################");
        for (String key : request.headers().toMultimap().keySet()) {
            buffer.append("\nheader: {" + key + " : " + request.headers().toMultimap().get(key) + "}");
        }
        LogUtils.d(TAG, buffer.toString());
        LogUtils.d(TAG, "url: " + request.url().uri().toString());
//        ResponseBody responseBody = response.body();
//        LogUtils.d(TAG, "--------responseBody--------");
    }
}
