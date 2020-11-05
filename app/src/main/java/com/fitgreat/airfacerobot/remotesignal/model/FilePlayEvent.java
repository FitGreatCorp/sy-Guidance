package com.fitgreat.airfacerobot.remotesignal.model;

import com.fitgreat.archmvp.base.event.BaseEvent;

/**
 * class des<p>
 *
 * @author dengj
 * @since 2020-05-21 15:25
 */
public class FilePlayEvent extends BaseEvent {

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    String extra;

    public FilePlayEvent(String type, String action) {
        super(type, action);
    }
}
