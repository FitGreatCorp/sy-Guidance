package com.fitgreat.airfacerobot.model;

public class CommandDataEvent {
    public String toData = null;
    public String doData = null;
    /**
     * 0 导航/执行宣教等其他/先导航后执行宣教等其他
     * 1 取消
     * 2 冲电
     */
    public int commandType = 0;

    public String getToData() {
        return toData;
    }

    public void setToData(String toData) {
        this.toData = toData;
    }

    public String getDoData() {
        return doData;
    }

    public void setDoData(String doData) {
        this.doData = doData;
    }

    public int getCommandType() {
        return commandType;
    }

    public void setCommandType(int commandType) {
        this.commandType = commandType;
    }
}
