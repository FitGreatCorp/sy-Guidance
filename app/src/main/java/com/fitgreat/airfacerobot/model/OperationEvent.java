package com.fitgreat.airfacerobot.model;

import com.fitgreat.archmvp.base.event.BaseEvent;

public class OperationEvent extends BaseEvent {
    public OperationEvent(String type, String action) {
        super(type, action);
    }

    public OperationEvent() {
    }
}