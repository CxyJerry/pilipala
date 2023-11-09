package com.jerry.pilipala.domain.vod.service.event;

import com.jerry.pilipala.domain.vod.entity.mongo.event.VodHandleActionEvent;
import com.jerry.pilipala.infrastructure.enums.ActionStatusEnum;
import com.jerry.pilipala.infrastructure.enums.VodHandleActionEnum;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;


@Component
public class SubmitActionEventHandler extends ActionEventHandler {

    public SubmitActionEventHandler(ApplicationEventPublisher applicationEventPublisher, MongoTemplate mongoTemplate) {
        super(applicationEventPublisher, mongoTemplate);
    }

    @Override
    protected void handleFinished(VodHandleActionEvent event) {
        super.handleFinished(event);
        VodHandleActionEvent transcodeActionEvent = new VodHandleActionEvent(
                VodHandleActionEnum.TRANSCODE,
                ActionStatusEnum.init,
                event.getCid()
        );
        // 触发转码
        applicationEventPublisher.publishEvent(transcodeActionEvent);
    }

    @Override
    public String action() {
        return VodHandleActionEnum.SUBMIT;
    }
}
