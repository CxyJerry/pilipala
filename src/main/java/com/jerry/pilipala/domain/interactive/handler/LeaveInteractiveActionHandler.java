package com.jerry.pilipala.domain.interactive.handler;

import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class LeaveInteractiveActionHandler extends InteractiveActionHandler {
    public LeaveInteractiveActionHandler(MongoTemplate mongoTemplate,
                                         RedisTemplate<String, Object> redisTemplate) {
        super(mongoTemplate, redisTemplate);
    }


    @Override
    public VodInteractiveActionEnum action() {
        return VodInteractiveActionEnum.LEAVE;
    }
}
