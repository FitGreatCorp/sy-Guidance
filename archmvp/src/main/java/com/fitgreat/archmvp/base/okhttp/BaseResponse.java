package com.fitgreat.archmvp.base.okhttp;

import android.text.TextUtils;

/**
 * base返回数据体<p>
 *
 * @author zixuefei
 * @since 2019/4/23 15:59
 */
public class BaseResponse {
    /**
     * type : 0000
     * msg : {"categorys":"401"}
     */
    private String msg;
    private String type;
    private Integer result;
    private String data;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSucceed() {
        return "success".equals(type);
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isEmptyContent() {
        return msg == null || TextUtils.isEmpty(msg) || "null".equals(msg);
    }

    public boolean isEmptyData() {
        return data == null || TextUtils.isEmpty(data) || "null".equals(data);
    }
}
