package com.jerry.pilipala.domain.vod.service.impl.handler;

import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.infrastructure.enums.redis.VodCacheKeyEnum;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LikeInteractiveActionHandler extends InteractiveActionHandler {
    public LikeInteractiveActionHandler(MongoTemplate mongoTemplate,
                                        RedisTemplate<String, Object> redisTemplate) {
        super(mongoTemplate, redisTemplate);
    }

    @Override
    public VodInteractiveAction trigger(Map<String, Object> params) {
        // 事件记录
        VodInteractiveAction interactiveAction = super.trigger(params);

        String cid = params.get("cid").toString();

        // 是取消点赞事件，点赞数 - 1，set 移除 uid
        String uid = interactiveAction.getUid();
        String likeSetKey = VodCacheKeyEnum.SetKey.LIKE_SET.concat(String.valueOf(uid));
        if (Boolean.TRUE.equals(redisTemplate.opsForSet()
                .isMember(likeSetKey, uid))) {
            redisTemplate.opsForSet().remove(
                    likeSetKey,
                    uid
            );
            checkVodStatisticsExists(cid);
            incVodStatistics(cid, "likeCount", false);
        }
        // 如果是点赞事件，点赞数 +1，uid 放入 set
        else {
            redisTemplate.opsForSet().add(
                    likeSetKey,
                    uid
            );
            checkVodStatisticsExists(cid);
            incVodStatistics(cid, "likeCount", true);
        }

        return interactiveAction;
    }

    @Override
    public VodInteractiveActionEnum action() {
        return VodInteractiveActionEnum.LIKE;
    }
}
