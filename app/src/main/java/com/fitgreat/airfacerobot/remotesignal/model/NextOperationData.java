package com.fitgreat.airfacerobot.remotesignal.model;

/**
 * class des<p>
 *
 * @author dengj
 * @since 2020/4/14 12:13
 */
public class NextOperationData {

    /**
     * F_RobotOperationId : E6C58B47-1E08-4C4B-9B5F-4E89765E1B52
     * F_Id : 901BAE61-1C55-4D0C-B94C-16943D75FB07
     * F_Type : Location
     * F_X : -0.3200
     * F_Y : 0.1800
     * F_Z : 2.2200
     * F_FileUrl : null
     * F_InstructionName : 点1
     * F_Status : 0
     * operationType : null
     * F_Container : null
     */

    private String F_RobotOperationId;
    private String F_Id;
    private String F_Type;
    private String F_X;
    private String F_Y;
    private String F_Z;
    private String F_FileUrl;
    private String F_InstructionName;
    private String F_Status;
    private String operationType;
    private String F_Container;
    private String F_InstructionEnName;


    public String getF_RobotOperationId() {
        return F_RobotOperationId;
    }

    public void setF_RobotOperationId(String F_RobotOperationId) {
        this.F_RobotOperationId = F_RobotOperationId;
    }

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

    public String getF_X() {
        return F_X;
    }

    public void setF_X(String F_X) {
        this.F_X = F_X;
    }

    public String getF_Y() {
        return F_Y;
    }

    public void setF_Y(String F_Y) {
        this.F_Y = F_Y;
    }

    public String getF_Z() {
        return F_Z;
    }

    public void setF_Z(String F_Z) {
        this.F_Z = F_Z;
    }

    public String getF_FileUrl() {
        return F_FileUrl;
    }

    public void setF_FileUrl(String F_FileUrl) {
        this.F_FileUrl = F_FileUrl;
    }

    public String getF_InstructionName() {
        return F_InstructionName;
    }

    public void setF_InstructionName(String F_InstructionName) {
        this.F_InstructionName = F_InstructionName;
    }

    public String getF_Status() {
        return F_Status;
    }

    public void setF_Status(String F_Status) {
        this.F_Status = F_Status;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getF_Container() {
        return F_Container;
    }

    public void setF_Container(String F_Container) {
        this.F_Container = F_Container;
    }

    public String getF_InstructionEnName() {
        return F_InstructionEnName;
    }

    public void setF_InstructionEnName(String f_InstructionEnName) {
        F_InstructionEnName = f_InstructionEnName;
    }
}
