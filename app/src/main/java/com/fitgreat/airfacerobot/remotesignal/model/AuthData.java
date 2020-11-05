package com.fitgreat.airfacerobot.remotesignal.model;

/**
 * 获取token信息<p>
 *
 * @author zixuefei
 * @since 2020/3/23 0023 17:19
 */
public class AuthData {

    /**
     * access_token : tzW4KANt4C5Qroy5RH7ZOvV3dMpmDz5dkzNe6HYcsL
     * expires_in : 864000
     * scope : basic
     * token_type : Bearer
     */

    private String access_token;
    private int expires_in;
    private String scope;
    private String token_type;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }
}
