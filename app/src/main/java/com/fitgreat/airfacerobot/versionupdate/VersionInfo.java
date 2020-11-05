package com.fitgreat.airfacerobot.versionupdate;

import java.util.List;

/**
 * 升级信息<p>
 *
 * @author zixuefei
 * @since 2020/4/20 0020 17:51
 */
public class VersionInfo {


    /**
     * F_Id : ee91e4b1-16fa-45b4-be0c-6b9a817636a9
     * F_Type : android_airface_robot_hardware
     * F_Version : 2.1.5
     * F_VersionNumber : 215
     * F_Status : true
     * F_UpdateType : False
     * F_Demo : null
     * F_UpdateTitle : ddccc
     * F_UpdateContent : ccvcvc
     * F_Step : [{"F_Command":"unzip v2.0.1.zip && cd v2.0.1 && sudo dpkg -i *.deb","F_Step":1,"F_FileUrl":"https://airfaceblobdev.blob.core.chinacloudapi.cn/hardwareversionc1880357-3e2d-4692-bc87-62cb1419e944/v2.0.1.zip?sv=2015-02-21&sr=b&sig=n0Rlu02UbZUD%2B9vjgivfRolKra3Le6qqzpC7oDYXVUM%3D&se=2020-05-19T06%3A14%3A43Z&sp=r"}]
     */

    private String F_Id;
    private String F_Type;
    private String F_Version;
    private String F_VersionNumber;
    private boolean F_Status;
    private String F_UpdateType;
    private Object F_Demo;
    private String F_UpdateTitle;
    private String F_UpdateContent;
    private List<FStepBean> F_Step;

    public String getF_Id() {
        return F_Id;
    }

    public void setF_Id(String F_Id) {
        this.F_Id = F_Id;
    }

    public String getF_Type() {
        return F_Type;
    }

    public void setF_Type(String F_Type) {
        this.F_Type = F_Type;
    }

    public String getF_Version() {
        return F_Version;
    }

    public void setF_Version(String F_Version) {
        this.F_Version = F_Version;
    }

    public String getF_VersionNumber() {
        return F_VersionNumber;
    }

    public void setF_VersionNumber(String F_VersionNumber) {
        this.F_VersionNumber = F_VersionNumber;
    }

    public boolean isF_Status() {
        return F_Status;
    }

    public void setF_Status(boolean F_Status) {
        this.F_Status = F_Status;
    }

    public String getF_UpdateType() {
        return F_UpdateType;
    }

    public void setF_UpdateType(String F_UpdateType) {
        this.F_UpdateType = F_UpdateType;
    }

    public Object getF_Demo() {
        return F_Demo;
    }

    public void setF_Demo(Object F_Demo) {
        this.F_Demo = F_Demo;
    }

    public String getF_UpdateTitle() {
        return F_UpdateTitle;
    }

    public void setF_UpdateTitle(String F_UpdateTitle) {
        this.F_UpdateTitle = F_UpdateTitle;
    }

    public String getF_UpdateContent() {
        return F_UpdateContent;
    }

    public void setF_UpdateContent(String F_UpdateContent) {
        this.F_UpdateContent = F_UpdateContent;
    }

    public List<FStepBean> getF_Step() {
        return F_Step;
    }

    public void setF_Step(List<FStepBean> F_Step) {
        this.F_Step = F_Step;
    }

    public static class FStepBean {

        /**
         * F_FileName : v2.0.1.zip
         * F_Command : unzip v2.0.1.zip && cd v2.0.1 && sudo dpkg -i *.deb
         * F_Step : 1
         * F_FileUrl : https://airfaceblobdev.blob.core.chinacloudapi.cn/hardwareversionc1880357-3e2d-4692-bc87-62cb1419e944/v2.0.1.zip?sv=2015-02-21&sr=b&sig=ybCPWZs0%2B%2B4At9V9vGeWKkNp3J8R1flgmvQPWwzgTm8%3D&se=2020-05-19T10%3A42%3A00Z&sp=r
         */

        private String F_FileName;
        private String F_Command;
        private int F_Step;
        private String F_FileUrl;

        public String getF_StepId() {
            return F_StepId;
        }

        public void setF_StepId(String f_StepId) {
            F_StepId = f_StepId;
        }

        private String F_StepId;

        public String getF_FileName() {
            return F_FileName;
        }

        public void setF_FileName(String F_FileName) {
            this.F_FileName = F_FileName;
        }

        public String getF_Command() {
            return F_Command;
        }

        public void setF_Command(String F_Command) {
            this.F_Command = F_Command;
        }

        public int getF_Step() {
            return F_Step;
        }

        public void setF_Step(int F_Step) {
            this.F_Step = F_Step;
        }

        public String getF_FileUrl() {
            return F_FileUrl;
        }

        public void setF_FileUrl(String F_FileUrl) {
            this.F_FileUrl = F_FileUrl;
        }
    }
}
