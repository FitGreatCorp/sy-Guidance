package com.fitgreat.airfacerobot.videocall.model;

import com.fitgreat.archmvp.base.event.BaseEvent;

public class VideoMessageEve extends BaseEvent {

    int islock;

    public int getIslock() {
        return islock;
    }

    public void setIslock(int islock) {
        this.islock = islock;
    }
}
