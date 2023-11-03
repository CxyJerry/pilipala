package com.jerry.pilipala.domain.vod.service.event;

import com.jerry.pilipala.infrastructure.enums.ActionStatusEnum;
import com.jerry.pilipala.infrastructure.enums.VodHandleActionEnum;
import com.jerry.pilipala.domain.vod.entity.mongo.event.VodHandleActionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class UploadActionEventHandler extends ActionEventHandler {

    public UploadActionEventHandler(ApplicationEventPublisher applicationEventPublisher, MongoTemplate mongoTemplate) {
        super(applicationEventPublisher, mongoTemplate);
    }

    @Override
    protected void handleFinished(VodHandleActionEvent event) {
        super.handleFinished(event);

        VodHandleActionEvent submitActionEvent = new VodHandleActionEvent(
                VodHandleActionEnum.SUBMIT,
                ActionStatusEnum.init,
                event.getCid()
        );
        // 触发提交开始
        applicationEventPublisher.publishEvent(submitActionEvent);
    }


    @Override
    public String action() {
        return VodHandleActionEnum.UPLOAD;
    }
}
