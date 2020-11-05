package com.fitgreat.airfacerobot.remotesignal.model;

public class RoomInfoResult {
        public String getType() {
                return Type;
        }

        public void setType(String type) {
                Type = type;
        }

        public String getTimeStamp() {
                return TimeStamp;
        }

        public void setTimeStamp(String timeStamp) {
                TimeStamp = timeStamp;
        }

        public ResultResultDate getResult() {
                return Result;
        }

        public void setResult(ResultResultDate result) {
                Result = result;
        }

        public String getRobotType() {
                return RobotType;
        }

        public void setRobotType(String robotType) {
                RobotType = robotType;
        }

        String Type;
        String TimeStamp;
        ResultResultDate Result;
        String RobotType;

}
