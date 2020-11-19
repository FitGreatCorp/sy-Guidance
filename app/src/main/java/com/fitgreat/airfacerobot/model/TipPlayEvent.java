package com.fitgreat.airfacerobot.model;

import com.fitgreat.archmvp.base.event.BaseEvent;

public class TipPlayEvent extends BaseEvent {
    public String playContent;

    public TipPlayEvent(String playContent, String type) {
        this.playContent = playContent;
        this.type = type;
    }

    public TipPlayEvent() {
    }

    public String getPlayContent() {
        return playContent;
    }

    public void setPlayContent(String playContent) {
        this.playContent = playContent;
    }
}
