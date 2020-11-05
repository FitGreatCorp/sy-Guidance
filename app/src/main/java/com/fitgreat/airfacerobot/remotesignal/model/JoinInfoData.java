package com.fitgreat.airfacerobot.remotesignal.model;

public class JoinInfoData {

    /**
     * type : success
     * msg : {"Type":"JoinResult","Result":"8号机器人已加入"}
     * userId : 6caabb29-a6b0-4ecc-8b01-8891b343ba33
     * groupName : 6caabb29-a6b0-4ecc-8b01-8891b343ba33
     * connectionId : 58a9bed2-5d07-4c29-a0f1-690f74d90b85
     */

    private String type;
    private String msg;
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
}
