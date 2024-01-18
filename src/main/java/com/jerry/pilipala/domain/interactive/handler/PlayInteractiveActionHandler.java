package com.jerry.pilipala.domain.interactive.handler;

import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.infrastructure.enums.redis.VodCacheKeyEnum;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class PlayInteractiveActionHandler extends InteractiveActionHandler {

    public PlayInteractiveActionHandler(MongoTemplate mongoTemplate,
                                        RedisTemplate<String, Object> redisTemplate) {
        super(mongoTemplate, redisTemplate);
    }

    @Override
    public VodInteractiveAction trigger(Map<String, Object> params) {
        params.put("valid", false);
        if (Objects.nonNull(params.get("uid"))) {
            String uid = (String) params.get("uid");
            String authorUid = (String) params.get("vod_author_uid");
            String key = VodCacheKeyEnum.SetKey.OFTEN_INTERACTIVE_SET.concat(uid);
            redisTemplate.opsForZSet()
                    .incrementScore(key, authorUid, 1);
        }

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
