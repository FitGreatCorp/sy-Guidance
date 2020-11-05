package com.fitgreat.airfacerobot.remotesignal.model;

/**
 * Signal 监听返回一级消息<p>
 *
 * @author zixuefei
 * @since 2020/3/24 0024 17:57
 */
public class BaseSignalMsg {


    /**
     * type : success
     * msg : {"Type":"heartbeat","Result":"3/24/2020 10:12:29 AM"}
     */

    private String type;
    private String msg;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    private String userId;
    private String groupName;
    private String connectionId;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


    public static class SignalMsg {

        /**
         * Type : heartbeat
         * Result : 3/24/2020 10:12:29 AM
         */

        private String Type;
        private String Result;

        public String getType() {
            return Type;
        }

        public void setType(String Type) {
            this.Type = Type;
        }

        public String getResult() {
            return Result;
        }

        public void setResult(String Result) {
            this.Result = Result;
        }
    }
}
