package com.fitgreat.airfacerobot.videocall.model;

public class AgoraTokenInfo {

    /**
     * result : true
     * token : 0068261ac1c92f342caaf9ab170ca885799IABU4oVJrCyiUJv1jcPP7kSQYxyXhzH0XaVbziIO9zaLxDwlZ8h6VG8WIgCqkH4FQhuIXgQAAQBiVodeAgBiVodeAwBiVodeBABiVode
     * uid : 1691
     */

    private boolean result;
    private String token;
    private int uid;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }
}
