package com.fitgreat.airfacerobot.business.Callback;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MyStringCallback implements Callback {
    private Handler handler = new Handler(Looper.getMainLooper());
    private ResponseListener mResponseListener = null;

    public MyStringCallback(ResponseListener responseListener) {
        this.mResponseListener = responseListener;
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        String string = response.body().string();
        handler.post(() -> {
            mResponseListener.onResponseString(string);
        });
    }

    @Override
    public void onFailure(Call call, IOException e) {
        handler.post(() -> {
            mResponseListener.onFailureString(e);
        });
    }

    public interface ResponseListener {
        void onResponseString(String responseString);

        void onFailureString(IOException e);
    }
}
