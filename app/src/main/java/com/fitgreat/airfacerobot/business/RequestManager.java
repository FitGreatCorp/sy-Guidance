package com.fitgreat.airfacerobot.business;

import android.text.TextUtils;
import android.util.Base64;

import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.archmvp.base.okhttp.HttpRequestClient;

import java.nio.charset.StandardCharsets;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * http 请求管理<p>
 *
 * @author zixuefei
 * @since 2019/10/23 21:39
 */
public class RequestManager {


    public static void startPost(String url, RequestBody requestBody, Callback callback) {
        HttpRequestClient.INSTANCE.startHttpRequest(createPostRequestCall(url, requestBody), callback);
    }

    public static void startPostForm(String url, FormBody formBody, Callback callback) {
        HttpRequestClient.INSTANCE.startHttpRequest(createPostRequestCall(url, formBody), callback);
    }
    public static void startPostFormAndFile(String url, MultipartBody multipartBody, Callback callback) {
        HttpRequestClient.INSTANCE.startHttpRequest(createPostRequestCall(url, multipartBody), callback);
    }
    public static void startRobotPost(String url, RequestBody requestBody, Callback callback) {
        HttpRequestClient.INSTANCE.startHttpRequest(createRobotAuthPostRequestCall(url, requestBody), callback);
    }

    public static void startAgoraPost(String url, RequestBody requestBody, Callback callback) {
        HttpRequestClient.INSTANCE.startHttpRequest(createAgoraAuthPostRequestCall(url, requestBody), callback);
    }

    public static void startGet(String url, Callback callback) {
        HttpRequestClient.INSTANCE.startHttpRequest(createGetRequestCall(url), callback);
    }

    public static void startPostNoHeader(String url, RequestBody requestBody, Callback callback) {
        HttpRequestClient.INSTANCE.startHttpRequest(createPostRequestCallNoHeader(url, requestBody), callback);
    }

    public static void cancelRequest(Call call) {
        HttpRequestClient.INSTANCE.cancelHttpRequest(call);
    }

    /**
     * 创建下载请求
     */
    public static void createDownload(String url, boolean needHeader, Callback callback) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Request request;
        if (needHeader) {
            request = new Request.Builder().headers(createCommonHeaders()).url(url).build();
        } else {
            request = new Request.Builder().url(url).build();
        }
        HttpRequestClient.INSTANCE.startHttpRequest(HttpRequestClient.INSTANCE.createCall(request), callback);
    }

    /**
     * 创建无header post请求
     */
    public static Call createPostRequestCallNoHeader(String url, RequestBody requestBody) {
        if (TextUtils.isEmpty(url) || requestBody == null) {
            return null;
        }
        Request request = null;
        try {
            request = new Request.Builder().url(url).post(requestBody)
                    .addHeader("Content-Type", "application/json;charset=utf-8").build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return HttpRequestClient.INSTANCE.createCall(request);
    }

    /**
     * 创建post请求
     */
    public static Call createPostRequestCall(String url, RequestBody requestBody) {
        if (TextUtils.isEmpty(url) || requestBody == null) {
            return null;
        }
        Request request = null;
        try {
            request = new Request.Builder()
                    .url(url).post(requestBody).headers(createCommonHeaders()).build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return HttpRequestClient.INSTANCE.createCall(request);
    }
    /**
     * 创建post请求
     */
    public static Call createPostRequestCall(String url, FormBody formBody) {
        if (TextUtils.isEmpty(url) || formBody == null) {
            return null;
        }
        Request request = null;
        try {
            request = new Request.Builder()
                    .url(url).post(formBody).headers(createCommonHeaders()).build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return HttpRequestClient.INSTANCE.createCall(request);
    }

    /**
     * 创建post请求
     */
    public static Call createRobotAuthPostRequestCall(String url, RequestBody requestBody) {
        if (TextUtils.isEmpty(url) || requestBody == null) {
            return null;
        }
        Request request = null;
        try {
            request = new Request.Builder().url(url).post(requestBody).headers(createRobotHeaders()).build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return HttpRequestClient.INSTANCE.createCall(request);
    }


    /**
     * 创建agora的post form请求
     *
     * @param url
     * @param requestBody
     * @return
     */
    public static Call createAgoraAuthPostRequestCall(String url, RequestBody requestBody) {
        if (TextUtils.isEmpty(url) || requestBody == null) {
            return null;
        }
        Request request = null;
        try {
            request = new Request.Builder().url(url).post(requestBody).headers(createAgoraHeaders()).build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return HttpRequestClient.INSTANCE.createCall(request);
    }


    /**
     * 创建get请求
     */
    public static Call createGetRequestCall(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        Request request = null;
        try {
            request = new Request.Builder().url(url).headers(createCommonHeaders()).get().build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return HttpRequestClient.INSTANCE.createCall(request);
    }

    /**
     * 创建通用服务器header参数
     */
    public static Headers createCommonHeaders() {
        final Headers.Builder builder = new Headers.Builder();

        String token = RobotInfoUtils.getToken();
        builder.add("Authorization", "Bearer " + token);

        builder.add("Content-Type", "application/x-www-form-urlencoded");

//        Map<String, String> extHeaders = extHeaders();
//        for (Map.Entry<String, String> entry : extHeaders.entrySet()) {
//            builder.add(entry.getKey(), entry.getValue());
//        }
        return builder.build();
    }


    /**
     * 创建机器人认证服务器header参数
     */
    public static Headers createRobotHeaders() {
        final Headers.Builder builder = new Headers.Builder();
        builder.add("Authorization", "Basic " + Base64.encodeToString(ApiRequestUrl.AUTH_STR
                .getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP));
        builder.add("Content-Type", "application/x-www-form-urlencoded");
        return builder.build();
    }


    /**
     * 创建声网认证服务器header参数
     */
    public static Headers createAgoraHeaders() {
        final Headers.Builder builder = new Headers.Builder();
        String token = RobotInfoUtils.getToken();
        builder.add("Authorization", "Bearer " + token);
        builder.add("Content-Type", "application/x-www-form-urlencoded");
        return builder.build();
    }

//    private static Map<String, String> extHeaders() {
//        final Map<String, String> extHeaderMaps = new HashMap<>();
//        extHeaderMaps.put("request-id", "");
//        extHeaderMaps.put("request-agent", "1");  //1：android、2：iOS、3：PC、4：H5、5：wechat
//        extHeaderMaps.put("device-id", PhoneInfoUtils.getUUID());
//        extHeaderMaps.put("os-version", "0");  //0：android、1：iOS
//        extHeaderMaps.put("sdk-version", "28");
//        extHeaderMaps.put("phone-model", android.os.Build.BRAND);
//        extHeaderMaps.put("market", ChannelUtil.getChannel());
//        extHeaderMaps.put("app-version", VersionUtils.getVersionName(LwVideoApp.getAppContext()));
//        extHeaderMaps.put("app-name", "");
//        extHeaderMaps.put("timestamp", String.valueOf(System.currentTimeMillis()));
//        extHeaderMaps.put("customer-id", "");
//        extHeaderMaps.put("access-token", "");
//        extHeaderMaps.put("language", "cn");
//        extHeaderMaps.put("waistcoat", "");
//        extHeaderMaps.put("oaid", (String) SpUtils.get(SpKeyConstants.ANDROID_OAID, ""));
//        return extHeaderMaps;
//    }
}
