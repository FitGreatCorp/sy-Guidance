package com.fitgreat.airfacerobot.model;

import com.fitgreat.archmvp.base.event.BaseEvent;


/**
 * 机器人信号事件<p>
 *
 * @author zixuefei
 * @since 2020/3/31 0031 15:20
 */
public class RobotSignalEvent extends BaseEvent {

    //long power_supply_status;//是否充电，
    // float percentage; //电量，
    // long power_supply_technology;//急停，
    // long power_supply_health;//手动移动，
    // float charge;  //温度，
    // BOOL present; //是否可以充电
    private float battery;
    private float charge;
    private boolean powerTechnology;
    private boolean present;
    private boolean powerHealth;
    private boolean connect;
    private boolean powerStatus;

    private double x;
    private double y;
    private double z;


    public RobotSignalEvent(String type, String action) {
        super(type, action);
    }


    public float getBattery() {
        return battery;
    }

    public void setBattery(float battery) {
        this.battery = battery;
    }

    public float getCharge() {
        return charge;
    }

    public void setCharge(float charge) {
        this.charge = charge;
    }

    public boolean getPowerTechnology() {
        return powerTechnology;
    }

    public void setPowerTechnology(boolean powerTechnology) {
        this.powerTechnology = powerTechnology;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public boolean getPowerHealth() {
        return powerHealth;
    }

    public void setPowerHealth(boolean powerHealth) {
        this.powerHealth = powerHealth;
    }

    public boolean isConnect() {
        return connect;
    }

    public void setConnect(boolean connect) {
        this.connect = connect;
    }

    public boolean isPowerStatus() {
        return powerStatus;
    }

    public void setPowerStatus(boolean powerStatus) {
        this.powerStatus = powerStatus;
    }


    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }


}
