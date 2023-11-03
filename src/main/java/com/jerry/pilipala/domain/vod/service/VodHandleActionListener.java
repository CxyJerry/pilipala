package com.jerry.pilipala.domain.vod.service;

import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import com.jerry.pilipala.domain.vod.entity.mongo.event.VodHandleActionEvent;
import com.jerry.pilipala.domain.vod.service.event.ActionEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class VodHandleActionListener implements ApplicationListener<VodHandleActionEvent> {
    private Map<String, ActionEventHandler> actionHandlerMap;

    @Autowired
    public void setActionHandlerMap(List<ActionEventHandler> actionEventHandlerList) {
        this.actionHandlerMap = new HashMap<>();
        actionEventHandlerList.forEach(handler -> this.actionHandlerMap.put(handler.action(), handler));
    }

    @Override
    public void onApplicationEvent(VodHandleActionEvent event) {
        ActionEventHandler actionEventHandler = actionHandlerMap.get(event.getAction());
        if (Objects.isNull(actionEventHandler)) {
            log.error("vod handle action not exists: {}", event.getAction());
            throw new BusinessException("稿件流程异常，请重试", StandardResponse.ERROR);
        }
        actionEventHandler.handle(event);
    }
}
