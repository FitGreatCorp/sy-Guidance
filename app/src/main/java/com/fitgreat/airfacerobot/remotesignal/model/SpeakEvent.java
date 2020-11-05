package com.fitgreat.airfacerobot.remotesignal.model;

import com.fitgreat.archmvp.base.event.BaseEvent;

/**
 * class des<p>
 *
 * @author dengj
 * @since 2020-05-25 15:29
 */
public class SpeakEvent extends BaseEvent {
    String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
