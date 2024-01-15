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
public class CollectInteractiveActionHandler extends InteractiveActionHandler {
    public CollectInteractiveActionHandler(MongoTemplate mongoTemplate,
                                           RedisTemplate<String, Object> redisTemplate) {
        super(mongoTemplate, redisTemplate);
    }

    @Override
    public VodInteractiveAction trigger(Map<String, Object> params) {
        VodInteractiveAction interactiveAction = super.trigger(params);

        String cid = params.get("cid").toString();
        // 是取消收藏事件，点赞数 - 1，set 移除 uid
        String uid = interactiveAction.getUid();
        String collectSet = VodCacheKeyEnum.SetKey.COLLECT_SET.concat(String.valueOf(uid));

        if (Objects.nonNull(redisTemplate.opsForZSet().score(collectSet, cid))) {
            redisTemplate.opsForZSet().remove(
                    collectSet,
                    cid
            );
            checkVodStatisticsExists(cid);
            incVodStatistics(cid, "collectCount", false);
        }
        // 如果是收藏事件，收藏数 +1，uid 放入 set
        else {
            redisTemplate.opsForZSet().add(collectSet, cid, System.currentTimeMillis());
            checkVodStatisticsExists(cid);
            incVodStatistics(cid, "collectCount", true);
        }

        return interactiveAction;

    }

    @Override
    public VodInteractiveActionEnum action() {
        return VodInteractiveActionEnum.COLLECT;
    }
}
