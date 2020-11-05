package com.fitgreat.airfacerobot.launcher.model;

import com.fitgreat.archmvp.base.event.BaseEvent;

public class ActionEvent extends BaseEvent {
    public String mActionKind;
    public String mActionContent;

    public ActionEvent(String actionKind, String actionContent) {
        this.mActionKind = actionKind;
        this.mActionContent = actionContent;
    }

    public ActionEvent() {
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
