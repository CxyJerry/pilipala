package com.jerry.pilipala.domain.vod.service.event;

import com.jerry.pilipala.domain.vod.entity.mongo.event.VodHandleActionRecord;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import com.jerry.pilipala.infrastructure.enums.ActionStatusEnum;
import com.jerry.pilipala.domain.vod.entity.mongo.event.VodHandleActionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Objects;

@Slf4j
public abstract class ActionEventHandler {
    protected final ApplicationEventPublisher applicationEventPublisher;
    protected final MongoTemplate mongoTemplate;

    protected ActionEventHandler(ApplicationEventPublisher applicationEventPublisher, MongoTemplate mongoTemplate) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.mongoTemplate = mongoTemplate;
    }

    public final void handle(VodHandleActionEvent event) {
        switch (event.getStatus()) {
            case ActionStatusEnum.init:
                handleInit(event);
                break;
            case ActionStatusEnum.running:
                handleRunning(event);
                break;
            case ActionStatusEnum.finished:
                handleFinished(event);
                break;
            case ActionStatusEnum.abort:
                handleAbort(event);
                break;
            default:
                throw new BusinessException("任务状态异常", StandardResponse.ERROR);
        }
    }

    private VodHandleActionRecord findVodHandleActionRecord(VodHandleActionEvent event) {
        Criteria criteriaDefinition = Criteria.where("cid").is(event.getCid())
                .and("action").is(event.getAction());
        Query query = new Query(criteriaDefinition);
        VodHandleActionRecord actionRecord = mongoTemplate.findOne(query, VodHandleActionRecord.class);
        if (Objects.isNull(actionRecord)) {
            throw new BusinessException("vod handle action record miss", StandardResponse.ERROR);
        }
        return actionRecord;
    }

    protected void handleInit(VodHandleActionEvent event) {
        log.debug("receive event: {} with status: init", event.getAction());
        Criteria criteriaDefinition = Criteria.where("cid").is(event.getCid())
                .and("action").is(event.getAction());
        Query query = new Query(criteriaDefinition);
        VodHandleActionRecord actionRecord = mongoTemplate.findOne(query, VodHandleActionRecord.class);
        if (Objects.nonNull(actionRecord)) {
            actionRecord.setStatus(ActionStatusEnum.init)
                    .setMtime(System.currentTimeMillis());
            mongoTemplate.save(actionRecord);
            return;
        }
        actionRecord = new VodHandleActionRecord()
                .setCid(event.getCid())
                .setAction(event.getAction())
                .setStatus(ActionStatusEnum.init);
        mongoTemplate.save(actionRecord);
    }

    protected void handleRunning(VodHandleActionEvent event) {
        log.debug("receive event: {} with status: running", event.getAction());
        VodHandleActionRecord actionRecord = findVodHandleActionRecord(event);
        actionRecord.setAction(action())
                .setStatus(ActionStatusEnum.running)
                .setMtime(System.currentTimeMillis());
        mongoTemplate.save(actionRecord);
    }


    protected void handleFinished(VodHandleActionEvent event) {
        log.debug("receive event: {} with status: finished", event.getAction());
        VodHandleActionRecord actionRecord = findVodHandleActionRecord(event);
        actionRecord.setAction(action())
                .setStatus(ActionStatusEnum.finished)
                .setMtime(System.currentTimeMillis());
        mongoTemplate.save(actionRecord);
    }

    protected void handleAbort(VodHandleActionEvent event) {
        log.debug("receive event: {} with status: abort", event.getAction());
        VodHandleActionRecord actionRecord = findVodHandleActionRecord(event);
        actionRecord.setAction(action())
                .setStatus(ActionStatusEnum.abort)
                .setMtime(System.currentTimeMillis());
        mongoTemplate.save(actionRecord);
    }

    public abstract String action();

}
