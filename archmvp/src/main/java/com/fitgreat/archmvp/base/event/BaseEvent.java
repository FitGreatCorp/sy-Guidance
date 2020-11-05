package com.fitgreat.archmvp.base.event;

/**
 * 事件基类 不能直接使用，否则会收到重复消息，需继承后订阅<p>
 *
 * @author zixuefei
 * @since 2020/3/18 0018 16:06
 */
public class BaseEvent {
    public String type;
    public String action;

    public BaseEvent(String type, String action) {
        this.type = type;
        this.action = action;
    }

    public BaseEvent() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
