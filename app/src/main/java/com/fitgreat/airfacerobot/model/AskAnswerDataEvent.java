package com.fitgreat.airfacerobot.model;

public class AskAnswerDataEvent {
    public String commandData = null;

    public String commandType;
    /**
     * 导航点位置信息
     */
    public LocationEntity locationEntity = null;

    /**
     * 单个常见问题信息
     */
    public CommonProblemEntity commonProblemEntity = null;

    public String getCommandData() {
        return commandData;
    }

    public void setCommandData(String commandData) {
        this.commandData = commandData;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public LocationEntity getLocationEntity() {
        return locationEntity;
    }

    public void setLocationEntity(LocationEntity locationEntity) {
        this.locationEntity = locationEntity;
    }

    public CommonProblemEntity getCommonProblemEntity() {
        return commonProblemEntity;
    }

    public void setCommonProblemEntity(CommonProblemEntity commonProblemEntity) {
        this.commonProblemEntity = commonProblemEntity;
    }
}
