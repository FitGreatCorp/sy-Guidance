package com.fitgreat.airfacerobot.model;

/**
 * App升级信息<p>
 *
 * @author zixuefei
 * @since 2020/4/20 0020 17:51
 */
public class AppVersion {


    /**
     * F_Id : b2025c9c-1e85-4383-b3b8-e2d3a7c17cb9
     * F_AppType : ios control
     * F_VersionCode : 3.0.0
     * F_VersionNumber : 301
     * F_Status : true
     * F_VersionType : true
     * F_FileUrl : https://airfaceblobdev.blob.core.chinacloudapi.cn/appversion752dbe57-82a5-4c30-b447-ba16b6fc0404/AirFaceRobot.apk?sv=2015-02-21&sr=b&sig=X%2FhGZfsiS27T0CGZoQxQghBR%2F9JUQKsP%2Fyfk2bT2EHo%3D&se=2020-04-22T03%3A33%3A12Z&sp=r
     * F_UpdateTitle : 1.app架构升级\n2.性能优化\n3.bug修复
     * F_UpdateMsg : 1.app架构升级\n2.性能优化\n3.bug修复
     * F_Memo : 1.app架构升级\n2.性能优化\n3.bug修复
     */

    private String F_Id;
    private String F_AppType;
    private String F_VersionCode;
    private int F_VersionNumber;
    private boolean F_Status;
    private boolean F_VersionType;
    private String F_FileUrl;
    private String F_UpdateTitle;
    private String F_UpdateMsg;
    private String F_Memo;

    public String getF_Id() {
        return F_Id;
    }

    public void setF_Id(String F_Id) {
        this.F_Id = F_Id;
    }

    public String getF_AppType() {
        return F_AppType;
    }

    public void setF_AppType(String F_AppType) {
        this.F_AppType = F_AppType;
    }

    public String getF_VersionCode() {
        return F_VersionCode;
    }

    public void setF_VersionCode(String F_VersionCode) {
        this.F_VersionCode = F_VersionCode;
    }

    public int getF_VersionNumber() {
        return F_VersionNumber;
    }

    public void setF_VersionNumber(int F_VersionNumber) {
        this.F_VersionNumber = F_VersionNumber;
    }

    public boolean isF_Status() {
        return F_Status;
    }

    public void setF_Status(boolean F_Status) {
        this.F_Status = F_Status;
    }

    public boolean isF_VersionType() {
        return F_VersionType;
    }

    public void setF_VersionType(boolean F_VersionType) {
        this.F_VersionType = F_VersionType;
    }

    public String getF_FileUrl() {
        return F_FileUrl;
    }

    public void setF_FileUrl(String F_FileUrl) {
        this.F_FileUrl = F_FileUrl;
    }

    public String getF_UpdateTitle() {
        return F_UpdateTitle;
    }

    public void setF_UpdateTitle(String F_UpdateTitle) {
        this.F_UpdateTitle = F_UpdateTitle;
    }

    public String getF_UpdateMsg() {
        return F_UpdateMsg;
    }

    public void setF_UpdateMsg(String F_UpdateMsg) {
        this.F_UpdateMsg = F_UpdateMsg;
    }

    public String getF_Memo() {
        return F_Memo;
    }

    public void setF_Memo(String F_Memo) {
        this.F_Memo = F_Memo;
    }
}
