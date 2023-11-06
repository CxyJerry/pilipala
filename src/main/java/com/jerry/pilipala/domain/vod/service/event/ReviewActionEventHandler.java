package com.jerry.pilipala.domain.vod.service.event;

import com.jerry.pilipala.domain.vod.entity.mongo.event.VodHandleActionEvent;
import com.jerry.pilipala.infrastructure.enums.ActionStatusEnum;
import com.jerry.pilipala.infrastructure.enums.VodHandleActionEnum;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;


@Component
public class ReviewActionEventHandler extends ActionEventHandler {

    public ReviewActionEventHandler(ApplicationEventPublisher applicationEventPublisher, MongoTemplate mongoTemplate) {
        super(applicationEventPublisher, mongoTemplate);
    }

    @Override
    protected void handleFinished(VodHandleActionEvent event) {
        super.handleFinished(event);
        VodHandleActionEvent openActionEvent = new VodHandleActionEvent(VodHandleActionEnum.OPEN,
                ActionStatusEnum.init,
                event.getCid()
        );
        applicationEventPublisher.publishEvent(openActionEvent);
    }

    @Override
    public String action() {
        return VodHandleActionEnum.REVIEW;
    }
}
