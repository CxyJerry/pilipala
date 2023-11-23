package com.jerry.pilipala.domain.vod.service.impl.handler;

import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DefaultInteractiveActionHandler extends InteractiveActionHandler {
    public DefaultInteractiveActionHandler(MongoTemplate mongoTemplate,
                                           RedisTemplate<String, Object> redisTemplate) {
        super(mongoTemplate, redisTemplate);
    }

    @Override
    public VodInteractiveAction trigger(Map<String, Object> params) {
        return super.trigger(params);
    }

    @Override
    public VodInteractiveActionEnum action() {
        return VodInteractiveActionEnum.UNKNOWN;
    }
}
