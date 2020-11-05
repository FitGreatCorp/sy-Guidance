package com.fitgreat.airfacerobot.remotesignal.model;

/**
 * 操控类数据实体<p>
 *
 * @author zixuefei
 * @since 2020/4/13 0013 10:16
 */
public class OperationData {

    /**
     * Type : Angle
     * Result : 6
     * TimeStamp : 1586750633452
     */

    private String Type;
    private String Result;
    private double TimeStamp;

    public String getType() {
        return Type;
    }

    public void setType(String Type) {
        this.Type = Type;
    }

    public String getResult() {
        return Result;
    }

    public void setResult(String Result) {
        this.Result = Result;
    }

    public double getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(double TimeStamp) {
        this.TimeStamp = TimeStamp;
    }
}
