package com.fitgreat.airfacerobot.launcher.model;

/**
 * 控制端自动回充,第一步获取特定工作流
 */
public class WorkflowEntity {
    public String F_Id;
    public String F_Name;

    public WorkflowEntity(String f_Id, String f_Name) {
        F_Id = f_Id;
        F_Name = f_Name;
    }

    public WorkflowEntity() {
    }

    public String getF_Id() {
        return F_Id;
    }

    public void setF_Id(String f_Id) {
        F_Id = f_Id;
    }

    public String getF_Name() {
        return F_Name;
    }

    public void setF_Name(String f_Name) {
        F_Name = f_Name;
    }
}
