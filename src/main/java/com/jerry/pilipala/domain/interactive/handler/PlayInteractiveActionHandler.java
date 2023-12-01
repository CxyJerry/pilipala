package com.jerry.pilipala.domain.interactive.handler;

import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PlayInteractiveActionHandler extends InteractiveActionHandler {

    public PlayInteractiveActionHandler(MongoTemplate mongoTemplate,
                                        RedisTemplate<String, Object> redisTemplate) {
        super(mongoTemplate, redisTemplate);
    }

    @Override
    public VodInteractiveAction trigger(Map<String, Object> params) {
        params.put("valid", false);
        return super.trigger(params);
    }

    @Override
    public void cleaning() {

    }

    @Override
    public VodInteractiveActionEnum action() {
        return VodInteractiveActionEnum.PLAY;
    }
}
