package com.jerry.pilipala.domain.interactive.handler;

import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.domain.interactive.entity.BaseInteractiveParam;
import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InteractiveActionTrigger {
    private final Map<VodInteractiveActionEnum, InteractiveActionHandler> handlerMap;
    private final DefaultInteractiveActionHandler defaultHandler;

    public InteractiveActionTrigger(List<InteractiveActionHandler> handlers,
                                    DefaultInteractiveActionHandler defaultHandler) {
        handlerMap = handlers.stream().collect(
                Collectors.toMap(InteractiveActionHandler::action, h -> h)
        );
        this.defaultHandler = defaultHandler;
    }

    public CompletableFuture<VodInteractiveAction> trigger(VodInteractiveActionEnum action,
                                                           BaseInteractiveParam interactiveParam) {
        String uid = StpUtil.getLoginId("unknown");
        interactiveParam.setSelfUid(uid);
        InteractiveActionHandler handler = handlerMap.getOrDefault(action, defaultHandler);
        CompletableFuture<VodInteractiveAction> suppliedAsync =
                CompletableFuture.supplyAsync(() -> handler.handle(interactiveParam));
        suppliedAsync.exceptionally(e -> {
            log.warn("互动事件触发失败: {} -> uid: {},cause ", handler, interactiveParam.getSelfUid(), e);
            return null;
        });
        return suppliedAsync;
    }

}
