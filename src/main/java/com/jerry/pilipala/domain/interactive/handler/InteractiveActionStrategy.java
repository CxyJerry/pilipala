package com.jerry.pilipala.domain.interactive.handler;

import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class InteractiveActionStrategy {

    private final Map<VodInteractiveActionEnum, InteractiveActionHandler> handlerMap;
    private final DefaultInteractiveActionHandler defaultHandler;

    public InteractiveActionStrategy(List<InteractiveActionHandler> handlers,
                                     DefaultInteractiveActionHandler defaultHandler) {
        handlerMap = handlers.stream().collect(
                Collectors.toMap(InteractiveActionHandler::action, h -> h)
        );
        this.defaultHandler = defaultHandler;
    }

    public CompletableFuture<VodInteractiveAction> trigger(VodInteractiveActionEnum action,
                                                           Map<String, Object> params) {
        String uid = StpUtil.getLoginId("unknown");
        params.put("uid", uid);
        return CompletableFuture.supplyAsync(() -> handlerMap.getOrDefault(action, defaultHandler).trigger(params));
    }

}
