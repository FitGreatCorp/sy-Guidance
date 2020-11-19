package com.fitgreat.airfacerobot.model;

public class RecordInfo {
    /**
     * 0 机器人回复
     * 1 语音指令
     */
    private int type;
    private String content;

    public RecordInfo() {
    }

    public RecordInfo(int type, String content) {
        this.type = type;
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
