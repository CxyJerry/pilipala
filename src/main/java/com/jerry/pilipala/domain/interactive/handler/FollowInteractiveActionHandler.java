package com.jerry.pilipala.domain.interactive.handler;

import com.jerry.pilipala.domain.interactive.entity.BaseInteractiveParam;
import com.jerry.pilipala.domain.interactive.entity.FollowInteractiveParam;
import com.jerry.pilipala.domain.user.service.FansService;
import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class FollowInteractiveActionHandler extends InteractiveActionHandler {
    private final FansService fansService;

    public FollowInteractiveActionHandler(MongoTemplate mongoTemplate,
                                          RedisTemplate<String, Object> redisTemplate,
                                          FansService fansService) {
        super(mongoTemplate, redisTemplate);
        this.fansService = fansService;
    }

    @Override
    public VodInteractiveAction handle(BaseInteractiveParam interactiveParam) {
        FollowInteractiveParam param = (FollowInteractiveParam) interactiveParam;

        // 关注事件
        fansService.put(param.getSelfUid(), param.getUpUid());

        return super.handle(interactiveParam);
    }

    @Override
    public VodInteractiveActionEnum action() {
        return VodInteractiveActionEnum.FOLLOW;
    }
}
