package com.jerry.pilipala.domain.interactive.handler;

import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CommentInteractiveActionHandler extends InteractiveActionHandler {
    public CommentInteractiveActionHandler(MongoTemplate mongoTemplate,
                                           RedisTemplate<String, Object> redisTemplate) {
        super(mongoTemplate, redisTemplate);
    }

    @Override
    public VodInteractiveAction trigger(Map<String, Object> params) {
        VodInteractiveAction interactiveAction = super.trigger(params);
        String cid = params.get("cid").toString();

        checkVodStatisticsExists(cid);
        incVodStatistics(cid, "commentCount", true);

        return interactiveAction;
    }

    @Override
    public VodInteractiveActionEnum action() {
        return VodInteractiveActionEnum.COMMENT;
    }
}
