package com.jerry.pilipala.domain.interactive.handler;

import com.jerry.pilipala.domain.interactive.entity.BaseInteractiveParam;
import com.jerry.pilipala.domain.interactive.entity.VodInteractiveParam;
import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.infrastructure.enums.redis.VodCacheKeyEnum;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CoinInteractiveActionHandler extends InteractiveActionHandler {
    public CoinInteractiveActionHandler(MongoTemplate mongoTemplate,
                                        RedisTemplate<String, Object> redisTemplate) {
        super(mongoTemplate, redisTemplate);
    }

    @Override
    public VodInteractiveAction handle(BaseInteractiveParam interactiveParam) {
        super.handle(interactiveParam);
        VodInteractiveParam param = (VodInteractiveParam) interactiveParam;
        String cid = param.getCid().toString();
        String uid = param.getSelfUid();

        String coinSetKey = VodCacheKeyEnum.SetKey.COIN_SET.concat(uid);
        if (Objects.nonNull(redisTemplate.opsForZSet().score(coinSetKey, cid))) {
            return null;
        }

        // 加入 coin 集合
        redisTemplate.opsForZSet().add(coinSetKey, cid, System.currentTimeMillis());

        checkVodStatisticsExists(cid);
        incVodStatistics(cid, "coinCount", true);

        return null;
    }

    @Override
    public VodInteractiveActionEnum action() {
        return VodInteractiveActionEnum.COIN;
    }
}
