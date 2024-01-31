package com.jerry.pilipala.domain.interactive.handler;

import com.jerry.pilipala.domain.interactive.entity.BaseInteractiveParam;
import com.jerry.pilipala.domain.interactive.entity.VodInteractiveParam;
import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ShareInteractiveActionHandler extends InteractiveActionHandler {
    public ShareInteractiveActionHandler(MongoTemplate mongoTemplate,
                                         RedisTemplate<String, Object> redisTemplate) {
        super(mongoTemplate, redisTemplate);
    }

    @Override
    public VodInteractiveAction handle(BaseInteractiveParam interactiveParam) {
        VodInteractiveAction interactiveAction = super.handle(interactiveParam);
        VodInteractiveParam param = (VodInteractiveParam) interactiveParam;
        String cid = param.getCid().toString();

        checkVodStatisticsExists(cid);
        incVodStatistics(cid, "shareCount", true);

        return interactiveAction;
    }

    @Override
    public VodInteractiveActionEnum action() {
        return VodInteractiveActionEnum.SHARE;
    }
}
