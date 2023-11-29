package com.jerry.pilipala.domain.vod.service.impl.handler;

import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class UpdateTimeInteractiveActionHandler extends InteractiveActionHandler {

    public UpdateTimeInteractiveActionHandler(MongoTemplate mongoTemplate,
                                              RedisTemplate<String, Object> redisTemplate) {
        super(mongoTemplate, redisTemplate);

    }

    @Override
    public VodInteractiveAction trigger(Map<String, Object> params) {
        Object playActionId = params.get("play_action_id");
        if (Objects.isNull(playActionId)) {
            return null;
        }
        VodInteractiveAction playAction =
                mongoTemplate.findById(playActionId.toString(), VodInteractiveAction.class);
        if (Objects.isNull(playAction)) {
            return null;
        }
        // valid 说明该播放已经视为一次有效播放
        boolean valid = (boolean) playAction.getParams().getOrDefault("valid", false);
        if (valid) {
            return null;
        }

        // 更新播放次数，连续更新 10 次，视为完成一次播放
        String luaScript = "local count = redis.call('GET', KEYS[1]) " +
                "if not count then " +
                "    redis.call('SET', KEYS[1], 0, 'EX', 10) " +
                "else " +
                "    redis.call('SET', KEYS[1], count + 1, 'EX', 10) " +
                "end ";
        List<String> keys = Collections.singletonList(playActionId.toString());
        redisTemplate.execute(new DefaultRedisScript<>(luaScript, Void.class), keys);
        Integer count = (Integer) redisTemplate.opsForValue().get(playActionId.toString());
        if (Objects.equals(count, 10)) {
            playAction.getParams().put("valid", true);
            redisTemplate.delete(playActionId.toString());
            mongoTemplate.save(playAction);

            String cid = params.get("cid").toString();
            checkVodStatisticsExists(cid);

            incVodStatistics(cid, "viewCount", true);
        }
        return null;
    }


    @Override
    public VodInteractiveActionEnum action() {
        return VodInteractiveActionEnum.UPDATE_TIME;
    }
}
