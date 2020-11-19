package com.fitgreat.airfacerobot.model;

import com.fitgreat.archmvp.base.event.BaseEvent;

public class DialogStateEvent extends BaseEvent {
    public String mDialogState;

    public DialogStateEvent(String dialogState) {
        this.mDialogState = dialogState;
    }

    public DialogStateEvent() {
    }
}
