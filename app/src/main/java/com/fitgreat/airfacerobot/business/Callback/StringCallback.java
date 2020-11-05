package com.fitgreat.airfacerobot.business.Callback;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

abstract class StringCallback implements Callback {

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        onResponseString(call, response.body().string());
    }

    abstract void onResponseString(Call call, String responseString) throws IOException;
}
