package com.fitgreat.airfacerobot.model;

import com.fitgreat.archmvp.base.event.BaseEvent;

public class ActionDdsEvent extends BaseEvent {
    public String mActionKind;
    public String mActionContent;

    public ActionDdsEvent(String actionKind, String actionContent) {
        this.mActionKind = actionKind;
        this.mActionContent = actionContent;
    }

    public ActionDdsEvent() {
    }

    public String getmActionKind() {
        return mActionKind;
    }

    public void setmActionKind(String mActionKind) {
        this.mActionKind = mActionKind;
    }

    public String getmActionContent() {
        return mActionContent;
    }

    public void setmActionContent(String mActionContent) {
        this.mActionContent = mActionContent;
    }
}
