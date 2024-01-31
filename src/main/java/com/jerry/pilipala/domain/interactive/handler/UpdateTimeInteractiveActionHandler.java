package com.jerry.pilipala.domain.interactive.handler;

import com.jerry.pilipala.domain.interactive.entity.BaseInteractiveParam;
import com.jerry.pilipala.domain.interactive.entity.PlayInteractiveParam;
import com.jerry.pilipala.domain.interactive.entity.UpdateTimeInteractiveParam;
import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.domain.vod.service.VodService;
import com.jerry.pilipala.infrastructure.enums.redis.VodCacheKeyEnum;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import com.jerry.pilipala.infrastructure.utils.JsonHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class UpdateTimeInteractiveActionHandler extends InteractiveActionHandler {
    private final VodService vodService;
    private final JsonHelper jsonHelper;

    public UpdateTimeInteractiveActionHandler(MongoTemplate mongoTemplate,
                                              RedisTemplate<String, Object> redisTemplate,
                                              VodService vodService,
                                              JsonHelper jsonHelper) {
        super(mongoTemplate, redisTemplate);
        this.vodService = vodService;
        this.jsonHelper = jsonHelper;
    }

    @Override
    public VodInteractiveAction handle(BaseInteractiveParam interactiveParam) {
        UpdateTimeInteractiveParam param = (UpdateTimeInteractiveParam) interactiveParam;
        String uid = param.getSelfUid();
        String bvId = param.getBvId();
        Long cid = param.getCid();
        Integer time = param.getTime();

        // 更新播放时间
        vodService.updatePlayTime(uid, bvId, cid, time);

        // 统计在线人数
        if (StringUtils.isNotBlank(uid) && !StringUtils.equalsIgnoreCase(uid, "unknown")) {
            redisTemplate.opsForSet()
                    .add(VodCacheKeyEnum.SetKey.ONLINE.concat(String.valueOf(cid)), uid);
        }

        String playActionId = param.getPlayActionId();
        if (Objects.isNull(playActionId)) {
            return null;
        }
        VodInteractiveAction playAction =
                mongoTemplate.findById(playActionId, VodInteractiveAction.class);
        if (Objects.isNull(playAction)) {
            return null;
        }
        // valid 说明该播放已经视为一次有效播放
        PlayInteractiveParam playInteractiveParam = jsonHelper.convert(playAction.getParam(), PlayInteractiveParam.class);
        boolean valid = playInteractiveParam.getValid();
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
        List<String> keys = Collections.singletonList(playActionId);
        redisTemplate.execute(new DefaultRedisScript<>(luaScript, Void.class), keys);
        Integer count = (Integer) redisTemplate.opsForValue().get(playActionId);
        if (Objects.equals(count, 10)) {
            playInteractiveParam.setValid(true);
            redisTemplate.delete(playActionId);
            mongoTemplate.save(playAction);

            String cidStr = cid.toString();
            checkVodStatisticsExists(cidStr);

            incVodStatistics(cidStr, "viewCount", true);
        }
        return null;
    }


    @Override
    public VodInteractiveActionEnum action() {
        return VodInteractiveActionEnum.UPDATE_TIME;
    }
}
