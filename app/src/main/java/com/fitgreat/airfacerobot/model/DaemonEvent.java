package com.fitgreat.airfacerobot.model;

import com.fitgreat.archmvp.base.event.BaseEvent;


/**
 * 处理守护进程消息<p>
 *
 * @author zixuefei
 * @since 2020/4/24 0024 17:06
 */
public class DaemonEvent extends BaseEvent {
    public String extra;

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String step;

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String stepId;

    public DaemonEvent(String type, String action, String extra) {
        this.type = type;
        this.action = action;
        this.extra = extra;
    }

    public DaemonEvent(String type, String action) {
        super(type, action);
    }
}
