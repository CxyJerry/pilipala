package com.jerry.pilipala.domain.interactive.handler;

import com.jerry.pilipala.domain.interactive.entity.BaseInteractiveParam;
import com.jerry.pilipala.domain.interactive.entity.VodInteractiveParam;
import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.infrastructure.enums.redis.VodCacheKeyEnum;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class LeaveInteractiveActionHandler extends InteractiveActionHandler {
    public LeaveInteractiveActionHandler(MongoTemplate mongoTemplate,
                                         RedisTemplate<String, Object> redisTemplate) {
        super(mongoTemplate, redisTemplate);
    }

    @Override
    public VodInteractiveAction handle(BaseInteractiveParam interactiveParam) {
        VodInteractiveParam param = (VodInteractiveParam) interactiveParam;
        Long cid = param.getCid();
        String uid = param.getSelfUid();
        // 统计在线人数
        if (StringUtils.isNotBlank(uid) && !StringUtils.equalsIgnoreCase(uid, "unknown")) {
            redisTemplate.opsForSet()
                    .remove(VodCacheKeyEnum.SetKey.ONLINE.concat(String.valueOf(cid)), uid);
            return super.handle(interactiveParam);
        }
        return null;
    }

    @Override
    public VodInteractiveActionEnum action() {
        return VodInteractiveActionEnum.LEAVE;
    }
}
