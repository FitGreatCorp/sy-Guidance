package com.fitgreat.airfacerobot.remotesignal.model;

/**
 * 机器人加入群组信息<p>
 *
 * @author zixuefei
 * @since 2020/3/25 0025 09:46
 */
public class GroupInfoData {


    /**
     * Id : 1554
     * Username : 8号机器人
     * UserId : 6caabb29-a6b0-4ecc-8b01-8891b343ba33
     * ConnectionId : a2acfa8d-f201-4853-b6e9-bd2132fa275d
     * InCall : false
     * DeviceId : 6caabb29-a6b0-4ecc-8b01-8891b343ba33
     * IsDevice : false
     * Type : robot
     * GroupName : 6caabb29-a6b0-4ecc-8b01-8891b343ba33
     * LastModifiedTime : 2020-03-25 03:23:07
     * Uid : null
     * HospitalName : null
     */

    private Long Id;
    private String Username;
    private String UserId;
    private String ConnectionId;
    private boolean InCall;
    private String DeviceId;
    private boolean IsDevice;
    private String Type;
    private String GroupName;
    private String LastModifiedTime;
    private String Uid;
    private String HospitalName;

    public Long getId() {
        return Id;
    }

    public void setId(Long Id) {
        this.Id = Id;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String Username) {
        this.Username = Username;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String UserId) {
        this.UserId = UserId;
    }

    public String getConnectionId() {
        return ConnectionId;
    }

    public void setConnectionId(String ConnectionId) {
        this.ConnectionId = ConnectionId;
    }

    public boolean isInCall() {
        return InCall;
    }

    public void setInCall(boolean InCall) {
        this.InCall = InCall;
    }

    public String getDeviceId() {
        return DeviceId;
    }

    public void setDeviceId(String DeviceId) {
        this.DeviceId = DeviceId;
    }

    public boolean isIsDevice() {
        return IsDevice;
    }

    public void setIsDevice(boolean IsDevice) {
        this.IsDevice = IsDevice;
    }

    public String getType() {
        return Type;
    }

    public void setType(String Type) {
        this.Type = Type;
    }

    public String getGroupName() {
        return GroupName;
    }

    public void setGroupName(String GroupName) {
        this.GroupName = GroupName;
    }

    public String getLastModifiedTime() {
        return LastModifiedTime;
    }

    public void setLastModifiedTime(String LastModifiedTime) {
        this.LastModifiedTime = LastModifiedTime;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String Uid) {
        this.Uid = Uid;
    }

    public String getHospitalName() {
        return HospitalName;
    }

    public void setHospitalName(String HospitalName) {
        this.HospitalName = HospitalName;
    }
}
