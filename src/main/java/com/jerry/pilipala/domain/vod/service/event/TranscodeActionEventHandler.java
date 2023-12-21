package com.jerry.pilipala.domain.vod.service.event;

import com.jerry.pilipala.domain.vod.entity.mongo.distribute.VodDistributeInfo;
import com.jerry.pilipala.domain.vod.entity.mongo.event.VodHandleActionEvent;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.VodProfiles;
import com.jerry.pilipala.domain.vod.service.VodService;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import com.jerry.pilipala.infrastructure.enums.ActionStatusEnum;
import com.jerry.pilipala.infrastructure.enums.VodHandleActionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class TranscodeActionEventHandler extends ActionEventHandler {
    private final VodService vodService;
    private final TaskExecutor taskExecutor;

    public TranscodeActionEventHandler(VodService vodService,
                                       ApplicationEventPublisher applicationEventPublisher,
                                       MongoTemplate mongoTemplate,
                                       @Qualifier("asyncServiceExecutor") TaskExecutor taskExecutor) {
        super(applicationEventPublisher, mongoTemplate);
        this.vodService = vodService;
        this.taskExecutor = taskExecutor;
    }

    @Override
    protected void handleInit(VodHandleActionEvent event) {
        super.handleInit(event);
        VodHandleActionEvent transcodeActionEvent = new VodHandleActionEvent(
                VodHandleActionEnum.TRANSCODE,
                ActionStatusEnum.running,
                event.getCid()
        );
        applicationEventPublisher.publishEvent(transcodeActionEvent);
    }

    @Override
    protected void handleRunning(VodHandleActionEvent event) {
        super.handleRunning(event);
        CompletableFuture.runAsync(() -> {
            VodHandleActionEvent actionEvent;
            try {
                // todo 缩略图生产
                // vodService.transcodeThumbnails(event.getCid());
                // 视频转码
                vodService.transcode(event.getCid());

                // 触发转码完成
                actionEvent = new VodHandleActionEvent(VodHandleActionEnum.TRANSCODE, ActionStatusEnum.finished, event.getCid());
                Query query = new Query(Criteria.where("_id").is(event.getCid()));
                VodProfiles vodProfiles = mongoTemplate.findOne(query, VodProfiles.class);
                if (Objects.isNull(vodProfiles)) {
                    throw new BusinessException("vod profiles miss", StandardResponse.ERROR);
                }
                vodProfiles.setCompleted(true);
                mongoTemplate.save(vodProfiles);
            } catch (Exception e) {
                log.error("transcode fail,", e);
                // 触发转码失败
                actionEvent = new VodHandleActionEvent(VodHandleActionEnum.TRANSCODE, ActionStatusEnum.fail, event.getCid());
            }
            applicationEventPublisher.publishEvent(actionEvent);
        }, taskExecutor);
    }

    @Override
    protected void handleFinished(VodHandleActionEvent event) {
        super.handleFinished(event);
        VodHandleActionEvent reviewActionEvent = new VodHandleActionEvent(
                VodHandleActionEnum.REVIEW,
                ActionStatusEnum.init,
                event.getCid()
        );
        Query query = new Query(Criteria.where("_id").is(event.getCid()));
        VodDistributeInfo vodDistributeInfo = mongoTemplate.findOne(query, VodDistributeInfo.class);
        if (Objects.isNull(vodDistributeInfo)) {
            throw new BusinessException("稿件丢失", StandardResponse.ERROR);
        }
        vodDistributeInfo.setReady(true);
        mongoTemplate.save(vodDistributeInfo);

        // 触发审核开始
        applicationEventPublisher.publishEvent(reviewActionEvent);
    }

    @Override
    public String action() {
        return VodHandleActionEnum.TRANSCODE;
    }
}
