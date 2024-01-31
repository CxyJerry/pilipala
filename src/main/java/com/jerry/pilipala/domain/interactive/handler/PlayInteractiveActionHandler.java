package com.jerry.pilipala.domain.interactive.handler;

import com.jerry.pilipala.domain.interactive.entity.BaseInteractiveParam;
import com.jerry.pilipala.domain.interactive.entity.PlayInteractiveParam;
import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.infrastructure.enums.redis.VodCacheKeyEnum;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PlayInteractiveActionHandler extends InteractiveActionHandler {

    public PlayInteractiveActionHandler(MongoTemplate mongoTemplate,
                                        RedisTemplate<String, Object> redisTemplate) {
        super(mongoTemplate, redisTemplate);
    }

    @Override
    public VodInteractiveAction handle(BaseInteractiveParam interactiveParam) {
        PlayInteractiveParam param = (PlayInteractiveParam) interactiveParam;
        param.setValid(false);
        if (Objects.nonNull(param.getSelfUid()) &&
                !StringUtils.equalsIgnoreCase(param.getSelfUid(), "unknown")) {
            String uid = param.getSelfUid();
            String authorUid = param.getAuthorUid();
            // 用户经常互动 +1
            String key = VodCacheKeyEnum.SetKey.OFTEN_INTERACTIVE_SET.concat(uid);
            redisTemplate.opsForZSet()
                    .incrementScore(key, authorUid, 1);
        }

        return super.handle(interactiveParam);
    }

    @Override
    public VodInteractiveActionEnum action() {
        return VodInteractiveActionEnum.PLAY;
    }
}
