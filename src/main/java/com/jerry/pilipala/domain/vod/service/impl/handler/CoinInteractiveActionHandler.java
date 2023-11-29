package com.jerry.pilipala.domain.vod.service.impl.handler;

import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.infrastructure.enums.redis.VodCacheKeyEnum;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CoinInteractiveActionHandler extends InteractiveActionHandler {
    public CoinInteractiveActionHandler(MongoTemplate mongoTemplate,
                                        RedisTemplate<String, Object> redisTemplate) {
        super(mongoTemplate, redisTemplate);
    }

    @Override
    public VodInteractiveAction trigger(Map<String, Object> params) {
        super.trigger(params);
        String cid = params.get("cid").toString();
        String uid = params.get("uid").toString();

        String coinSetKey = VodCacheKeyEnum.SetKey.COIN_SET.concat(cid);
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(coinSetKey, uid))) {
            return null;
        }

        // 加入 coin 集合
        redisTemplate.opsForSet().add(coinSetKey, uid);

        checkVodStatisticsExists(cid);
        incVodStatistics(cid, "coinCount", true);

        return null;
    }

    @Override
    public VodInteractiveActionEnum action() {
        return VodInteractiveActionEnum.COIN;
    }
}
