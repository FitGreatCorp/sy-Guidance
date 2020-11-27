package com.fitgreat.airfacerobot.remotesignal.model;

import com.fitgreat.archmvp.base.event.BaseEvent;


/**
 * 接收远程信号处理事件<p>
 *
 * @author zixuefei
 * @since 2020/4/8 0008 09:34
 */
public class SignalDataEvent extends BaseEvent {

    private String e;
    private String x;
    private String y;
    private String F_InstructionName;
    private String F_InstructionEnName;
    private String instructionId;
    private String instructionType;
    private String container;
    private String operationType;
    private String produceId;
    private String connectionId;
    private double position_X;
    private double position_Y;
    private double position_Z;
    private String rosMoveStatusCode;
    private boolean move;
    private String step_id;
    private String ros_step;
    private boolean isLocation;
    private String targetUser;
    private int powerlock;
    private String fileUrl;
    private String direction;
    private int light_status;
    private String vertical;

    public String getRos_step() {
        return ros_step;
    }

    public void setRos_step(String ros_step) {
        this.ros_step = ros_step;
    }

    public String getStep_id() {
        return step_id;
    }

    public void setStep_id(String step_id) {
        this.step_id = step_id;
    }

    public String getF_InstructionName() {
        return F_InstructionName;
    }

    public void setF_InstructionName(String f_InstructionName) {
        F_InstructionName = f_InstructionName;
    }

    public double getPosition_X() {
        return position_X;
    }

    public void setPosition_X(double position_X) {
        this.position_X = position_X;
    }

    public double getPosition_Y() {
        return position_Y;
    }

    public void setPosition_Y(double position_Y) {
        this.position_Y = position_Y;
    }

    public double getPosition_Z() {
        return position_Z;
    }

    public void setPosition_Z(double position_Z) {
        this.position_Z = position_Z;
    }

    public String getRosMoveStatusCode() {
        return rosMoveStatusCode;
    }

    public void setRosMoveStatusCode(String rosMoveStatusCode) {
        this.rosMoveStatusCode = rosMoveStatusCode;
    }

    public boolean isLocation() {
        return isLocation;
    }

    public void setLocation(boolean location) {
        isLocation = location;
    }

    public int getPowerlock() {
        return powerlock;
    }

    public void setPowerlock(int powerlock) {
        this.powerlock = powerlock;
    }

    public String getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(String targetUser) {
        this.targetUser = targetUser;
    }

    public boolean isMove() {
        return move;
    }

    public void setMove(boolean move) {
        this.move = move;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getProduceId() {
        return produceId;
    }

    public void setProduceId(String produceId) {
        this.produceId = produceId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getInstructionType() {
        return instructionType;
    }

    public void setInstructionType(String instructionType) {
        this.instructionType = instructionType;
    }

    public String getInstructionId() {
        return instructionId;
    }

    public void setInstructionId(String instructionId) {
        this.instructionId = instructionId;
    }

    public SignalDataEvent(String type, String action) {
        super(type, action);
    }

    public SignalDataEvent() {

    }

    public String getVertical() {
        return vertical;
    }

    public void setVertical(String vertical) {
        this.vertical = vertical;
    }


    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getE() {
        return e;
    }

    public void setE(String e) {
        this.e = e;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public int getLight_status() {
        return light_status;
    }

    public void setLight_status(int light_status) {
        this.light_status = light_status;
    }

    public String getF_InstructionEnName() {
        return F_InstructionEnName;
    }

    public void setF_InstructionEnName(String f_InstructionEnName) {
        F_InstructionEnName = f_InstructionEnName;
    }
}
