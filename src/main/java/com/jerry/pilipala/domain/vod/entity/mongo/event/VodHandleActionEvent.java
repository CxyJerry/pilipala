package com.jerry.pilipala.domain.vod.entity.mongo.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;


@Getter
public class VodHandleActionEvent extends ApplicationEvent {
    private final String action;
    private final String status;
    private final Long cid;

    public VodHandleActionEvent(String action, String status, Long cid) {
        super(action);
        this.action = action;
        this.status = status;
        this.cid = cid;
    }
}
