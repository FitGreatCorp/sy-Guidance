package com.fitgreat.airfacerobot.remotesignal.model;

public class ResultResultDate {
    public String getConnectionId() {
        return ConnectionId;
    }

    public void setConnectionId(String connectionId) {
        ConnectionId = connectionId;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getIsMain() {
        return isMain;
    }

    public void setIsMain(String isMain) {
        this.isMain = isMain;
    }

    public String getRoomId() {
        return RoomId;
    }

    public void setRoomId(String roomId) {
        RoomId = roomId;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    String ConnectionId;
    String Type;
    String isMain;
    String RoomId;
    String Uid;

}
