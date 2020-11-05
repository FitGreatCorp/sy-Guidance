package com.fitgreat.airfacerobot.launcher.model;

import com.fitgreat.archmvp.base.event.BaseEvent;

/**
 * 初始化事件<p>
 *
 * @author zixuefei
 * @since 2020/3/24 0024 16:22
 */
public class InitEvent extends BaseEvent {
    public String extra;


    public boolean isHideFloatBall() {
        return hideFloatBall;
    }

    public void setHideFloatBall(boolean hideFloatBall) {
        this.hideFloatBall = hideFloatBall;
    }

    private boolean hideFloatBall;

    public InitEvent(String type, String action, String extra) {
        this.type = type;
        this.action = action;
        this.extra = extra;
    }

    public InitEvent(String type, String action) {
        this.type = type;
        this.action = action;
    }

    public InitEvent() {
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
