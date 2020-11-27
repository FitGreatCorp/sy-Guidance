package com.fitgreat.airfacerobot.model;

import java.io.Serializable;

public class LocationEntity implements Serializable {

    String F_Id;
    String F_MapId;
    String F_Name;
    //位置英文名字
    String F_EName;
    String F_X;
    String F_Y;
    String F_Z;
    String F_Memo;
    //三亚显示地图点坐标
    String S_X;
    String S_Y;

    public String getF_Id() {
        return F_Id;
    }

    public void setF_Id(String f_Id) {
        F_Id = f_Id;
    }

    public String getF_MapId() {
        return F_MapId;
    }

    public void setF_MapId(String f_MapId) {
        F_MapId = f_MapId;
    }

    public String getF_Name() {
        return F_Name;
    }

    public void setF_Name(String f_Name) {
        F_Name = f_Name;
    }

    public String getF_X() {
        return F_X;
    }

    public void setF_X(String f_X) {
        F_X = f_X;
    }

    public String getF_Y() {
        return F_Y;
    }

    public void setF_Y(String f_Y) {
        F_Y = f_Y;
    }

    public String getF_Z() {
        return F_Z;
    }

    public void setF_Z(String f_Z) {
        F_Z = f_Z;
    }

    public String getF_Memo() {
        return F_Memo;
    }

    public void setF_Memo(String f_Memo) {
        F_Memo = f_Memo;
    }


    public String getS_X() {
        return S_X;
    }

    public void setS_X(String s_X) {
        S_X = s_X;
    }

    public String getS_Y() {
        return S_Y;
    }

    public void setS_Y(String s_Y) {
        S_Y = s_Y;
    }

    public String getF_EName() {
        return F_EName;
    }

    public void setF_EName(String f_EName) {
        F_EName = f_EName;
    }
}
