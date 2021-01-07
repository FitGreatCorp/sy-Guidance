package com.fitgreat.airfacerobot.model;

/**
 * 工作流对象
 */
public class WorkflowEntity {
    public String F_Id;
    public String F_Name;
    public boolean F_Select=false;

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

    public boolean isF_Select() {
        return F_Select;
    }

    public void setF_Select(boolean f_Select) {
        F_Select = f_Select;
    }
}
