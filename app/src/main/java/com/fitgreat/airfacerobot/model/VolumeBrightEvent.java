package com.fitgreat.airfacerobot.model;

import com.fitgreat.archmvp.base.event.BaseEvent;

public class VolumeBrightEvent extends BaseEvent {
    public String mActionKind;
    public int mCurrentProgress;

    public VolumeBrightEvent(String actionKind, int currentProgress) {
        this.mActionKind = actionKind;
        this.mCurrentProgress = currentProgress;
    }

    public VolumeBrightEvent(String actionKind) {
        this.mActionKind = actionKind;
    }

    public String getmActionKind() {
        return mActionKind;
    }

    public void setmActionKind(String mActionKind) {
        this.mActionKind = mActionKind;
    }

    public int getmCurrentProgress() {
        return mCurrentProgress;
    }

    public void setmCurrentProgress(int mCurrentProgress) {
        this.mCurrentProgress = mCurrentProgress;
    }
}
