package com.fitgreat.airfacerobot.remotesignal.model;

import com.fitgreat.archmvp.base.event.BaseEvent;


/**
 * 初始化UI反馈事件<p>
 *
 * @author zixuefei
 * @since 2020/3/26 0026 10:06
 */
public class InitUiEvent extends BaseEvent {
    public InitUiEvent(String type, String action) {
        super(type, action);
    }
}
