package com.jerry.pilipala.domain.interactive.handler;

import com.jerry.pilipala.domain.interactive.entity.BaseInteractiveParam;
import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class DefaultInteractiveActionHandler extends InteractiveActionHandler {
    public DefaultInteractiveActionHandler(MongoTemplate mongoTemplate,
                                           RedisTemplate<String, Object> redisTemplate) {
        super(mongoTemplate, redisTemplate);
    }

    @Override
    public VodInteractiveAction handle(BaseInteractiveParam interactiveParam) {
        return super.handle(interactiveParam);
    }

    @Override
    public VodInteractiveActionEnum action() {
        return VodInteractiveActionEnum.UNKNOWN;
    }
}
