package com.jerry.pilipala.domain.vod.service.event;

import com.jerry.pilipala.domain.vod.entity.mongo.event.VodHandleActionEvent;
import com.jerry.pilipala.infrastructure.enums.ActionStatusEnum;
import com.jerry.pilipala.infrastructure.enums.VodHandleActionEnum;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;


@Component
public class PreUploadActionEventHandler extends ActionEventHandler {

    public PreUploadActionEventHandler(ApplicationEventPublisher applicationEventPublisher,
                                       MongoTemplate mongoTemplate) {
        super(applicationEventPublisher, mongoTemplate);
    }

    @Override
    protected void handleFinished(VodHandleActionEvent event) {
        super.handleFinished(event);
        // 触发上传开始
        applicationEventPublisher.publishEvent(new VodHandleActionEvent(VodHandleActionEnum.UPLOAD, ActionStatusEnum.init, event.getCid()));
    }

    @Override
    public String action() {
        return VodHandleActionEnum.PRE_UPLOAD;
    }
}
